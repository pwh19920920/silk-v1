package com.spark.bitrade.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.dto.ExchangeReleaseLockRequestDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.feign.IExchangeReleaseLockApiService;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.nio.protocol.PipeliningClientExchangeHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class FinanceConsumer {
    private Logger logger = LoggerFactory.getLogger(FinanceConsumer.class);
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private WithdrawRecordService withdrawRecordService;
    @Autowired
    private RechargeConfigService rechargeConfigService;
    @Autowired
    private FinancialAuditService financialAuditService;
    @Autowired
    private MemberService memberService;
    //add by qhliao btlf锁仓
    @Autowired
    private LockBTLFService lockBTLFService;
    @Autowired
    private IExchangeReleaseLockApiService iExchangeReleaseLockApiServicel;

    @Autowired
    private MemberWalletService memberWalletService;

    /**
     * 处理充值消息，key值为币种的名称（注意是全称，如Bitcoin）
     *
     * @param record
     */
    @KafkaListener(topics = {"deposit"})
    public void handleDeposit(ConsumerRecord<String, String> record) {
        getService().asyncDeposit(record);
    }

    /**
     * 充值的异步处理
     *
     * @param record
     */
    @Async
    public void asyncDeposit(ConsumerRecord<String, String> record) {
        logger.info("topic={},key={},value={}", record.topic(), record.key(), record.value());
        if (StringUtils.isEmpty(record.value())) {
            return;
        }
        JSONObject json = JSON.parseObject(record.value());
        if (json == null) {
            return;
        }
        BigDecimal amount = json.getBigDecimal("amount");

        String txid = json.getString("txid");
        String address = json.getString("address");
        Coin coin = coinService.findOne(record.key());

        if (coin != null && amount != null) {
            if (amount.compareTo(new BigDecimal(Double.toString(coin.getMinDepositAmount()))) < 0) {
                logger.info("该笔交易 {} 地址 {} 金额 {} 小于最小到账金额 {}", txid, address, amount, coin.getMinDepositAmount());
                return;
            }
        }


        logger.info("coin={}", coin);
        //edit by yangch 时间： 2018.04.24 原因：合并前
        //if(coin != null) {
        //add by yangch 时间： 2018.04.24 原因：合并后
        if (coin != null) {
            // 获取用户信息
            MemberWallet memberWallet = walletService.findByCoinAndAddress(coin, address);
            // 增加btlf判断
            if(BooleanEnum.IS_FALSE.equals(coin.getCanRecharge())){
                if(!org.apache.commons.lang3.StringUtils.equalsIgnoreCase("BTLF",coin.getUnit())){
                    return;
                }
                if(memberWallet.getMemberId() != 408124L && memberWallet.getMemberId() != 408313L && memberWallet.getMemberId() != 408308L && memberWallet.getMemberId() != 397987L){
                    return;
                }
            }

            if (walletService.findDeposit(address, txid, record.key(), amount) == null) {

                Wrapper<RechargeConfig> rechargeConfigEntityWrapper = new EntityWrapper<RechargeConfig>()
                        .eq("unit", coin.getUnit()).eq("recharge_switch", 1);
                RechargeConfig rechargeConfig = rechargeConfigService.selectOne(rechargeConfigEntityWrapper);
                if (rechargeConfig == null || rechargeConfig.getRechargeAmount().compareTo(amount) == 1) {
                    MessageResult mr = walletService.recharge(coin, address, amount, txid);
                    if (mr.isSuccess()) {
                        lockBtlf(memberWallet.getMemberId(),amount,coin.getUnit());
                        //edit by lc 时间： 2019.12.16 原因:新币需求,链上充值成功后交易锁仓释放
                        exchangeReleaseLock(coin.getUnit(),amount,memberWallet.getMemberId(),mr.getData());
                        // 充值到账，业务逻辑处理成功后，事件采集
                        logger.info("充值到账，调用事件采集，memberId={},txid={}", memberWallet.getMemberId(), txid);
                        getService().coinIn(memberWallet.getMemberId().toString(), txid);
                    }
                    logger.info("wallet recharge result:{}", mr);
                } else {
                    if (memberWallet == null) {
                        Coin baseCoin = coinService.findOne(coin.getBaseCoinUnit());
                        if (baseCoin != null) {
                            memberWallet = walletService.findByCoinAndAddress(baseCoin, address);
                        }
                    }
                    if (memberWallet == null) {
                        return;
                    }
                    Member member = memberService.findOne(memberWallet.getMemberId());
                    // 初始化运营审核记录
                    FinancialAudit financialAudit = new FinancialAudit();
                    financialAudit.setId(IdWorker.getId());
                    financialAudit.setUserName(member.getUsername());
                    financialAudit.setMemberId(member.getId());
                    financialAudit.setTransactionNumber(txid);
                    financialAudit.setUnit(coin.getUnit());
                    financialAudit.setAddress(address);
                    financialAudit.setAmount(amount);
                    financialAudit.setApplyTime(new Date());
                    financialAudit.setCreateTime(new Date());
                    financialAudit.setUpdateTime(new Date());

                    Wrapper<FinancialAudit> financialAuditWrapper = new EntityWrapper<FinancialAudit>()
                            .eq("unit", coin.getUnit())
                            .eq("member_id", member.getId())
                            .eq("transaction_number", txid)
                            .eq("address", address);
                    FinancialAudit financialAudit1 = financialAuditService.selectOne(financialAuditWrapper);
                    if (financialAudit1 == null) {
                        financialAuditService.insertOrUpdateNew(financialAudit);
                        //将数据库切回鞋库
                    }
                }
            }
        }
    }

    /**
     * 充值到账，采集事件
     *
     * @param memberId 到账会员ID
     * @param txid     交易哈希
     */
    @CollectActionEvent(collectType = CollectActionEventType.COIN_IN, memberId = "#memberId", refId = "#txid")
    public void coinIn(String memberId, String txid) {
        logger.info("充值到账，事件采集，memberId={},txid={}", memberId, txid);
    }

    /**
     * 处理充值消息，key值为币种的名称（注意是全称，如Bitcoin）
     *
     * @param record
     */
    @KafkaListener(topics = {"deposit-audit"})
    public void handleDepositAudit(ConsumerRecord<String, String> record) {
        getService().asyncDepositAudit(record);
    }


    /**
     * 充值审核的异步处理
     *
     * @param record
     */
    @Async
    public void asyncDepositAudit(ConsumerRecord<String, String> record) {
        logger.info("topic={},key={},value={}", record.topic(), record.value());
        if (StringUtils.isEmpty(record.value())) {
            return;
        }
        JSONObject json = JSON.parseObject(record.value());
        if (json == null) {
            return;
        }
        BigDecimal amount = json.getBigDecimal("amount");

        String txid = json.getString("txid");
        String address = json.getString("address");
        String unit = json.getString("unit");
        Boolean result = json.getBoolean("result");
        Coin coin = coinService.findOne(unit);

        logger.info("coin={}", coin);
        //edit by yangch 时间： 2018.04.24 原因：合并前
        //if(coin != null) {
        //add by yangch 时间： 2018.04.24 原因：合并后
        if (Boolean.TRUE.equals(result)) {
            if (coin != null && walletService.findDeposit(address, txid, unit, amount) == null) {
                financialAuditService.virtualWriteSwitch();
                MessageResult mr = walletService.recharge(coin, address, amount, txid);
                if (mr.isSuccess()) {
                    // 充值到账，业务逻辑处理成功后，事件采集
                    MemberWallet memberWallet = walletService.findByCoinAndAddress(coin, address);
                    logger.info("充值到账，调用事件采集，memberId={},txid={}", memberWallet.getMemberId().toString(), txid);
                    getService().coinIn(memberWallet.getMemberId().toString(), txid);
                    //edit by lc 时间： 2019.12.16 原因:新币需求,链上充值成功后交易锁仓释放
                    exchangeReleaseLock(unit,amount,memberWallet.getMemberId(),mr.getData());
                    lockBtlf(memberWallet.getMemberId(),amount,coin.getUnit());
                }
                logger.info("wallet recharge result:{}", mr);
            }
        } else {
            logger.info("审核失败 {}", record);
        }
    }


    /**
     * 处理提交请求,调用钱包rpc，自动转账
     *
     * @param record
     */
    @KafkaListener(topics = {"withdraw"})
    public void handleWithdraw(ConsumerRecord<String, String> record) {
        getService().asyncWithdraw(record);

    }

    /**
     * 转账的异步处理
     *
     * @param record
     * @return true
     * @author shenzucai
     * @time 2018.08.28 7:44
     */
    @Async
    public void asyncWithdraw(ConsumerRecord<String, String> record) {
        logger.info("topic={},key={},value={}", record.topic(), record.key(), record.value());
        if (StringUtils.isEmpty(record.value())) {
            return;
        }
        //add by  shenzucai 时间： 2018.08.01  原因：延时1秒处理
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JSONObject json = JSON.parseObject(record.value());
        getService().rpcWithdraw(json);
    }

    /**
     * 事物保证
     *
     * @param json
     * @return true
     * @author shenzucai
     * @time 2018.08.28 7:44
     */
    @Transactional
    public void rpcWithdraw(JSONObject json) {
        try {

            Long withdrawId = json.getLong("withdrawId");
            // 判断是否是平台内部地址
            Coin coin = json.getObject("coin", Coin.class);
            MemberWallet memberWallet = walletService.findByCoinAndAddress(coin, json.getString("address"));
            if (memberWallet != null) {
                logger.info("进入平台互转 wallet={}", memberWallet);
                String txid = UUIDUtil.getUUID();
                walletService.recharge(json.getObject("coin", Coin.class),
                        json.getString("address"), json.getBigDecimal("arriveAmount"), txid);
                try {

                    WithdrawRecord record = withdrawRecordService.findOne(withdrawId);
                    if ("BTLF".equals(coin.getUnit()) && !Objects.isNull(record)) {
                        Long memberId = record.getMemberId();
                        if(Objects.isNull(memberWallet)){
                            throw new Exception("memberWallet is null");
                        }
                        if (memberId.longValue() == 408124L || memberId.longValue() == 408313L || memberId.longValue() == 408308L || memberId.longValue() == 397987L) {
                            lockBTLFService.lockBTLF(memberWallet.getMemberId(), record.getArrivedAmount(), coin.getUnit());
                        }
                    }
                }catch(Exception e){
                    logger.info("BTLF 充值锁仓失败 {}",e);
                }
                //处理成功,data为txid，更新业务订单
                withdrawRecordService.withdrawSuccess(withdrawId, txid);
            } else {
                logger.info("进入区块链 wallet={}", memberWallet);
                String url = null;
                MessageResult result = null;
                if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                    url = "http://SERVICE-RPC-ETH/rpc/transfer?address={1}&amount={2}&fee={3}&withdrawId={4}&coinUnit={5}";
                    result = restTemplate.getForObject(url,
                            MessageResult.class, json.getString("address"), json.getBigDecimal("arriveAmount"), json.getBigDecimal("fee"), withdrawId, coin.getUnit());
                } else if ("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())) {
                    url = "http://SERVICE-RPC-SLU/rpc/transfer?address={1}&amount={2}&fee={3}&withdrawId={4}&coinUnit={5}";
                    result = restTemplate.getForObject(url,
                            MessageResult.class, json.getString("address"), json.getBigDecimal("arriveAmount"), json.getBigDecimal("fee"), withdrawId, coin.getUnit());
                } else {
                    url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/transfer?address={1}&amount={2}&fee={3}&withdrawId={4}";
                    result = restTemplate.getForObject(url,
                            MessageResult.class, json.getString("address"), json.getBigDecimal("arriveAmount"), json.getBigDecimal("fee"), withdrawId);
                }
                logger.info("result = {}", result);
            }
        } catch (Exception e) {
            // 进行手动强制回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            //e.printStackTrace();
            logger.error("auto withdraw failed,error={}", e.getMessage());
        }
    }


    /**
     * 添加异步处理
     *
     * @param
     * @return true
     * @author shenzucai
     * @time 2018.08.27 19:06
     */
    private FinanceConsumer getService() {
        return SpringContextUtil.getBean(this.getClass());
    }

    /**
     * btlf锁仓
     * @param memberId
     * @param amount
     * @param coinUnit
     */
    private void lockBtlf(Long memberId,BigDecimal amount,String coinUnit){
        try {
            lockBTLFService.lockBTLF(memberId,amount,coinUnit);
        }catch (Exception e){
            logger.info("锁仓失败!");
        }
    }

    /**
      * 币币交易释放锁仓规则
      * @parm
      * @return
     */
    private void exchangeReleaseLock(String unit, BigDecimal amount, Long memberId, Object memberDepositRecord){
        String refId = "";
        try {
            if(!"ESP".equals(unit)){
                return;
            }
            Long [] eliminateMember={426137L,426138L,125871L,389788L,389789L,74655L};
            if(ArrayUtils.contains(eliminateMember, memberId)){
                return;
            }
            MemberWallet promoteMemberWalletCeche = memberWalletService.findCacheByCoinUnitAndMemberId(unit,memberId);
            log.info("用户钱包={}",promoteMemberWalletCeche);
            if(promoteMemberWalletCeche == null){
                //对应币种的账户不存在，则创建对应的账户（解决买币账户不存在的问题）
                Coin coin = coinService.findByUnit(unit);
                if(null == coin){
                    log.warn("币种不存在。 币种名称={}", unit);
                    return;
                }
                promoteMemberWalletCeche = memberWalletService.createMemberWallet(memberId, coin);
                if(null == promoteMemberWalletCeche){
                    log.warn("用户账户不存在。用户id={},币种名称={}",memberId,  unit);
                    return;
                }
            }
            refId = JSONObject.parseObject(JSON.toJSONString(memberDepositRecord),MemberDeposit.class).getId().toString();
            ExchangeReleaseLockRequestDTO requestDTO = new ExchangeReleaseLockRequestDTO();
            requestDTO.setCoinSymbol(unit);
            requestDTO.setLockAmount(amount.toPlainString());
            requestDTO.setMemberId(Integer.valueOf(memberId.toString()));
            requestDTO.setRefId(refId);//充值记录id
            TimeUnit.MILLISECONDS.sleep(1500);
            iExchangeReleaseLockApiServicel.exchangeReleaseLock(JSON.toJSONString(requestDTO));
        } catch (InterruptedException e) {
            logger.info("充值锁仓失败失败");
        }
    }

}
