package com.spark.bitrade.consumer;

import com.alibaba.druid.sql.visitor.functions.Isnull;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.ActivityRewardType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.MemberWalletDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.GeneratorUtil;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;

@Component
public class MemberConsumer {
    private Logger logger = LoggerFactory.getLogger(MemberConsumer.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardActivitySettingService rewardActivitySettingService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private MemberWalletDao memberWalletDao;

    @Value("${spark.system.coins}")
    private String coins;

    /**
     * 重置用户钱包地址
     *
     * @param record
     */
    @KafkaListener(topics = {"reset-member-address"})
    public void resetAddress(ConsumerRecord<String, String> record) {
        String content = record.value();
        JSONObject json = JSON.parseObject(content);
        Coin coin = coinService.findByUnit(record.key());
        Assert.notNull(coin, "coin null");
        if (coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(json.getString("unit"), json.getLong("uid"));
            Assert.notNull(memberWallet, "wallet null");
            String account = "U" + json.getLong("uid");
            //远程RPC服务URL,后缀为币种单位
            String serviceName = "SERVICE-RPC-" + coin.getUnit();
            try {
                String url = "http://" + serviceName + "/rpc/address/{account}";
                ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, account);
                logger.info("remote call:service={},result={}", serviceName, result);
                if (result.getStatusCode().value() == 200) {
                    MessageResult mr = result.getBody();
                    logger.info("mr={}", mr);
                    if (mr.getCode() == 0) {
                        String address = mr.getData().toString();
                        memberWallet.setAddress(address);
                    }
                }
            } catch (Exception e) {
                logger.error("call {} failed,error={}", serviceName, e.getMessage());
            }
            memberWalletService.save(memberWallet);
        }

    }


    /**
     * 客户注册消息
     *
     * @param content
     */
    @KafkaListener(topics = {"member-register"})
    public void handle(String content) {
        logger.info("handle member-register,data={}", content);
        getService().handleWallet(content);

    }

    @Transactional
    public void handleActivity(JSONObject json) {

        logger.info("处理活动");

        //edit by tansitao 时间： 2018/5/11 原因：修改注册活动奖励，
//            RewardActivitySetting rewardActivitySetting = rewardActivitySettingService.findByType(ActivityRewardType.REGISTER);
        List<RewardActivitySetting> rewardActivitySettingList = rewardActivitySettingService.findListByType(ActivityRewardType.REGISTER);
        if (rewardActivitySettingList != null) {
            for (RewardActivitySetting rewardActivitySetting : rewardActivitySettingList) {
                if (rewardActivitySetting != null) {
                    long memberId = json.getLong("uid");
                    //MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(rewardActivitySetting.getCoin(), json.getLong("uid"));
                    MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(rewardActivitySetting.getCoin(), memberId);
                    if (memberWallet == null) {
                        //edit by tansitao 时间： 2018/11/10 原因：如果用户活动币种钱包记录不存在，创建一个新的记录
                        memberWallet = memberWalletService.createMemberWallet(memberId, rewardActivitySetting.getCoin());
                    }
                    BigDecimal amount3 = JSONObject.parseObject(rewardActivitySetting.getInfo()).getBigDecimal("amount");
                    //add|edit|del by tansitao 时间： 2018/5/17 原因：修改钱包的操作方式为sql
//                    memberWallet.setBalance(BigDecimalUtils.add(memberWallet.getBalance(), amount3));
//                    memberWalletService.save(memberWallet);
                    memberWalletDao.increaseBalance(memberWallet.getId(), amount3);
                    Member member = memberService.findOne(memberId);
                    RewardRecord rewardRecord3 = new RewardRecord();
                    rewardRecord3.setAmount(amount3);
                    rewardRecord3.setCoin(rewardActivitySetting.getCoin());
                    rewardRecord3.setMember(member);
                    rewardRecord3.setRemark(rewardActivitySetting.getType().getCnName());
                    rewardRecord3.setType(RewardRecordType.ACTIVITY);
                    rewardRecordService.save(rewardRecord3);
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setFee(BigDecimal.ZERO);
                    memberTransaction.setAmount(amount3);
                    memberTransaction.setSymbol(rewardActivitySetting.getCoin().getUnit());
                    memberTransaction.setType(TransactionType.ACTIVITY_AWARD);
                    memberTransaction.setMemberId(member.getId());
                    memberTransactionService.save(memberTransaction);
                }
            }
        }
    }


    @Async
    public void handleWallet(String content) {

        logger.info("处理钱包");
        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }

        //add by tansitao 时间： 2018/11/10 原因：生成配置文件中的币种记录
        long memberId = json.getLong("uid");
        String [] coinList = coins.split(",");
        if(coinList != null && coinList.length > 0){
            for (String coinstr : coinList) {
                Coin coin = coinService.findByUnit(coinstr);
                if(coin != null){
                    memberWalletService.createMemberWallet(memberId, coin);
                }
            }
        }

       /* //获取所有支持的币种
        List<Coin> coins =  coinService.findAll();
        //edit by shenzucai 时间： 2018.04.22 原因：选出所有主币（非代币）
        List<Coin> baseCoins =  coinService.findAllBaseCoin();
        for(Coin coin:baseCoins) {
            MemberWallet wallet = new MemberWallet();
            wallet.setCoin(coin);
            wallet.setMemberId(json.getLong("uid"));
            wallet.setBalance(new BigDecimal(0));
            wallet.setFrozenBalance(new BigDecimal(0));
            wallet.setLockBalance(new BigDecimal(0));
            if (coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
                String account = "U" + json.getLong("uid");
                //远程RPC服务URL,后缀为币种单位
                String serviceName = "SERVICE-RPC-" + coin.getUnit();
                try {
                    String url = "http://" + serviceName + "/rpc/address/{account}";
                    ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, account);
                    logger.info("remote call:service={},result={}", serviceName, result);
                    if (result.getStatusCode().value() == 200) {
                        MessageResult mr = result.getBody();
                        logger.info("mr={}", mr);
                        if (mr.getCode() == 0) {
                            //返回地址成功，调用持久化
                            String address = (String) mr.getData();
                            wallet.setAddress(address);
                        }
                    }
                } catch (Exception e) {
                    logger.error("call {} failed,error={}", serviceName, e.getMessage());
                    wallet.setAddress("");
                }
            } else {
                wallet.setAddress("");
            }
            //保存
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(wallet.getCoin().getUnit(), wallet.getMemberId());
            if (memberWallet == null) {
                wallet.setIsLock(BooleanEnum.IS_FALSE);
                memberWalletService.save(wallet);
            }

        }

        //add by shenzucai 时间： 2018.04.22 原因：代币直接使用对应主币的地址 start ---------------
        for(Coin coin:coins) {
            if(StringUtils.isEmpty(coin.getBaseCoinUnit())){
                continue;
            }
            MemberWallet wallet = new MemberWallet();
            wallet.setCoin(coin);
            wallet.setMemberId(json.getLong("uid"));
            wallet.setBalance(new BigDecimal(0));
            wallet.setFrozenBalance(new BigDecimal(0));
            wallet.setLockBalance(new BigDecimal(0));
            Coin baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
            MemberWallet baseWallet = memberWalletService.findByCoinAndMemberId(baseCoin,wallet.getMemberId());
            if(StringUtils.isNotEmpty(baseWallet.getAddress())) {
                wallet.setAddress(baseWallet.getAddress());
            }
            else{
                wallet.setAddress("");
            }
            //保存
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(wallet.getCoin().getUnit(),wallet.getMemberId());
            if(memberWallet == null) {
                wallet.setIsLock(BooleanEnum.IS_FALSE);
                memberWalletService.save(wallet);
            }
        }
        //add by shenzucai 时间： 2018.04.22 原因：代币直接使用对应主币的地址 end --------------*/


        getService().handleActivity(json);//edit by tansitao 时间： 2018/7/6 原因：修改为最后执行
    }


    private MemberConsumer getService() {
        return SpringContextUtil.getBean(this.getClass());
    }
}
