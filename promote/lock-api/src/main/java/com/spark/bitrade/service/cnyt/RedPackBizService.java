package com.spark.bitrade.service.cnyt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dto.ExchangeReleaseLockRequestDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.feign.IExchangeReleaseLockApiService;
import com.spark.bitrade.mapper.dao.RedPackReceiveRecordMapper;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.AssertUtil;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.RedPackVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  
 *   无需国际化
 *  @author liaoqinghui  
 *  @time 2019.11.25 17:42  
 */
@Service
@Slf4j
public class RedPackBizService {
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private RedPackManageService redPackManageService;
    @Autowired
    private RedPackReceiveRecordService recordService;
    @Autowired
    private LocaleMessageSourceService msgService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private ISilkDataDistService silkDataDistService;
    @Autowired
    private IExchangeReleaseLockApiService iExchangeReleaseLockApiServicel;

    @Resource
    private RedPackReceiveRecordMapper redPackReceiveRecordMapper;
    public RedPackManage findValidRedPack() {
        return redPackManageService.findValidRedPack();
    }

    public RedPackManage findManageById(Long id) {
        return redPackManageService.findById(id);
    }

    public RedPackReceiveRecord findValidRecordById(Long id) {
        return recordService.findValidRecordById(id);
    }

    /**
     * 生成红包
     *
     * @param manage
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public RedPackReceiveRecord generateRedPack(RedPackManage manage) {
        RedPackReceiveRecord record = new RedPackReceiveRecord();
        BigDecimal amount = getRedPack(manage.getRedPacketBalance(), manage.getMinAmount(),
                manage.getMaxAmount(), getScale(manage.getUnit()));

        record.setRedpackId(manage.getId());
        record.setRedpackName(manage.getRedpackName());
        record.setReceiveUnit(manage.getUnit());
        record.setReceiveAmount(amount);
        record.setWithin(manage.getWithin());
        record.setReceiveStatus(0);
        record.setUserType(2);
        record.setCreateTime(new Date());
        //保存并返回
        RedPackReceiveRecord save = recordService.save(record);
        int i = redPackManageService.updateBalance(amount, manage.getId(), manage.getReceiveType() == 2 ? 1 : 0);
        Assert.isTrue(i > 0, msgService.getMessage("RED_PACK_HAS_ALREADY_ZERO"));
        return save;
    }

    /**
     * 随机生成一个数额的红包
     *
     * @param
     * @param min
     * @param max
     * @param coinScale
     * @return
     */
    public BigDecimal getRedPack(BigDecimal total, BigDecimal min, BigDecimal max, Integer coinScale) {
        double minD = min.doubleValue();
        double maxD = max.doubleValue();
        BigDecimal db = new BigDecimal(Math.random() * (maxD - minD) + minD);
        BigDecimal scale = db.setScale(coinScale, BigDecimal.ROUND_DOWN);
        if (total.compareTo(scale) < 0) {
            return total;
        }
        return scale;
    }



    public void findRecordByMemberId(long id, Long packManageId) {
        List<RedPackReceiveRecord> packReceiveRecord = recordService.findByMemberIdAndRedpackIdStatus(id, packManageId);
        Assert.isTrue(CollectionUtils.isEmpty(packReceiveRecord), msgService.getMessage("HAS_ALREADY_JOINED_THIS_ACTIVITY"));
    }

    /**
     * 领取红包 余额增加
     *
     * @param record
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult pickRedPack(RedPackReceiveRecord record) {
        record.setUpdateTime(new Date());
        recordService.save(record);
        //总账户配置查询
        SilkDataDist silkDataDist=silkDataDistService.findByIdAndKey("RED_PACK_CONFIG","TOTAL_ACCOUNT_ID");
        Assert.notNull(silkDataDist,"红包支付账户未配置,请联系管理员");
        Long totalAccountId = Long.valueOf(silkDataDist.getDictVal());
        MemberWallet walletTotal = memberWalletService.findByCoinUnitAndMemberId(record.getReceiveUnit(), totalAccountId);
        Assert.notNull(walletTotal,"红包支付钱包不存在,请联系管理员");
        //获取钱包
        Coin coin = coinService.findByUnit(record.getReceiveUnit());
        //红包领取人钱包判断
        MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(record.getReceiveUnit(), record.getMemberId());
        if (wallet == null) {
            wallet = memberWalletService.createMemberWallet(record.getMemberId(), coin);
        }

        //总账户扣减冻结余额
        MessageResult messageResult = memberWalletService.decreaseFrozen(walletTotal.getId(), record.getReceiveAmount());
        Assert.isTrue(messageResult.isSuccess(),"红包总账户冻结余额不足,请联系管理员");
        //资金流水
        MemberTransaction transactionTotal=new MemberTransaction();
        transactionTotal.setMemberId(totalAccountId);
        transactionTotal.setAmount(BigDecimal.ZERO.subtract(record.getReceiveAmount()));
        transactionTotal.setCreateTime(new Date());
        transactionTotal.setType(TransactionType.ACTIVITY_AWARD);
        transactionTotal.setSymbol(record.getReceiveUnit());
        transactionTotal.setRefId(String.valueOf(record.getId()));
        transactionTotal.setComment("红包奖励扣除");
        memberTransactionService.save(transactionTotal);

        //增加用户余额
        MessageResult rl = memberWalletService.increaseBalance(wallet.getId(), record.getReceiveAmount());
        Assert.isTrue(rl.isSuccess(), "用户红包余额处理失败");
        //资金流水
        MemberTransaction transaction=new MemberTransaction();
        transaction.setMemberId(record.getMemberId());
        transaction.setAmount(record.getReceiveAmount());
        transaction.setCreateTime(new Date());
        transaction.setType(TransactionType.ACTIVITY_AWARD);
        transaction.setSymbol(record.getReceiveUnit());
        transaction.setRefId(String.valueOf(record.getId()));
        transaction.setComment("红包奖励");
        memberTransactionService.save(transaction);
        return  rl;
    }


    public PageInfo<RedPackVo> findRedpackRecordByManageId(Long packManageId, Integer page, Integer size) {
        Page<RedPackVo> objects = PageHelper.startPage(page, size);
        redPackReceiveRecordMapper.findRedpackRecordByManageId(packManageId,page,size);
        return objects.toPageInfo();
    }


    @Cacheable(cacheNames = "exchangeCoin",key = "'entity:exchangeCoin:scale:'+#coin")
    public Integer getScale(String coin){
        Integer scale=redPackReceiveRecordMapper.findScaleByCoin(coin);
        scale=scale==null?4:scale;
        return scale;
    }

    /**
     * ESP红包释放锁仓规则,异步执行
     * @parm
     * @return
     */
    public void exchangeReleaseLock(RedPackReceiveRecord record) {
        ExchangeReleaseLockRequestDTO requestDTO = new ExchangeReleaseLockRequestDTO();
        //获取钱包
        Coin coin = coinService.findByUnit(record.getReceiveUnit());
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin,record.getMemberId());
            try {
                requestDTO.setCoinSymbol(record.getReceiveUnit());
                requestDTO.setLockAmount(record.getReceiveAmount().toPlainString());
                requestDTO.setMemberId(Integer.valueOf(record.getMemberId().toString()));
                //红包记录id
                requestDTO.setRefId(String.valueOf(record.getId()));
                log.info("执行ESP红包锁仓规则,requestDTO{}", requestDTO);
                //延迟处理
                Thread.sleep(500);
                MessageRespResult respResult = iExchangeReleaseLockApiServicel.exchangeReleaseLock(JSON.toJSONString(requestDTO));
                if(!respResult.isSuccess()){
                    //冻结用户余额
                    memberWalletService.freezeBalance(memberWallet, record.getReceiveAmount());
                    log.info("ESP红包锁仓失败执行冻结,record{}", record);
                }
                log.info("==============================="+respResult);
            }catch (Exception e){
                log.error("红包ESP锁仓失败,错误信息:{}",e.getMessage());
                //锁仓失败冻结
                log.info("ESP红包锁仓失败执行冻结,record{}", record);
                if (memberWallet == null) {
                    memberWallet = memberWalletService.createMemberWallet(record.getMemberId(), coin);
                }
                Assert.notNull(memberWallet,"用户钱包不存在");
                //冻结用户余额
                memberWalletService.freezeBalance(memberWallet, record.getReceiveAmount());
            }
        }


}













