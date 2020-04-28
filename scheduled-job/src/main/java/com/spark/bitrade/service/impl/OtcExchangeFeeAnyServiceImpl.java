package com.spark.bitrade.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.mysema.commons.lang.Assert;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dao.FeeAdExRecordDao;
import com.spark.bitrade.dto.FeeOtcExchangeDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.MemberTransactionCoreMapper;
import com.spark.bitrade.mapper.dao.RewardRecordMapper;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  
 *  手续费统计  
 *  @author liaoqinghui  
 *  @time 2019.09.09 17:38  
 */
@Service
public class OtcExchangeFeeAnyServiceImpl implements IOtcExchangeFeeAnyService {

    @Autowired
    private MemberTransactionCoreMapper transactionCoreMapper;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private FeeAdExRecordService feeAdExRecordService;
    @Autowired
    private FeeAdExRecordDao feeAdExRecordDao;
    @Resource
    private RewardRecordMapper rewardRecordMapper;
    @Autowired
    private IExchange2FeignService exchange2FeignService;
    @Override
    public void run(String yesterdayStart, String yesterdayEnd, Long id) {
        //查询当天是否统计过
        Date date = new Date();
        String dateStr = DateUtil.dateToString(date, "yyyy-MM-dd");
        List<FeeOtcExchangeDto> feeDtos = transactionCoreMapper.findFeeDto(yesterdayStart, yesterdayEnd);
        feeDtos.addAll(remoteExchangeFee(yesterdayStart,yesterdayEnd));
        List<RewardRecord> bbReward = rewardRecordMapper.findBBReward(yesterdayStart, yesterdayEnd);
        XxlJobLogger.log("统计数量:" + feeDtos.size());
        for (FeeOtcExchangeDto d : feeDtos) {
            String coinUnit = d.getCoinUnit();
            BigDecimal feeTotal = d.getFee();
            TransactionType transactionType = d.getTransactionType();
            if (transactionType == TransactionType.EXCHANGE) {
                if (!CollectionUtils.isEmpty(bbReward)) {
                    for (RewardRecord rewardRecord : bbReward) {
                        if (coinUnit.equals(rewardRecord.getFromCoinUnit())) {
                            feeTotal = feeTotal.subtract(rewardRecord.getAmount());
                            XxlJobLogger.log("币币交易扣除三级返佣,币种:" + coinUnit + ",数量:" + rewardRecord.getAmount());
                        }
                    }
                }
            }
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(coinUnit, id);
            if (memberWallet == null) {
                Coin coin = coinService.findByUnit(coinUnit);
                if(coin==null){
                    XxlJobLogger.log(coinUnit+"币种不存在");
                    continue;
                }
                memberWallet = memberWalletService.createMemberWallet(id, coin);
            }

            String todayStart = dateStr + " 00:00:00";
            String todayEnd = dateStr + " 23:59:59";
            Wrapper wrapper = Condition.create().eq("coin", coinUnit).eq("type", d.getTransactionType().getOrdinal())
                    .ge("create_time", todayStart).le("create_time", todayEnd);
            List list = feeAdExRecordService.selectList(wrapper);
            if (!CollectionUtils.isEmpty(list)) {
                XxlJobLogger.log("币种:" + coinUnit + ",金额:" + feeTotal + ",已统计过");
                continue;
            }
            try {
                getService().saveFee(id, feeTotal, coinUnit, transactionType, memberWallet.getId());
                XxlJobLogger.log("统计成功币种:" + coinUnit + ",金额:" + feeTotal);
            } catch (Exception e) {
                XxlJobLogger.log("统计失败币种:" + coinUnit + ",金额:" + feeTotal);
            }

        }
    }


    public OtcExchangeFeeAnyServiceImpl getService() {
        return SpringContextUtil.getBean(OtcExchangeFeeAnyServiceImpl.class);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerPrimary")
    public void saveFee(Long id, BigDecimal fee, String coin, TransactionType type, Long walletId) {

        FeeAdExRecord record = new FeeAdExRecord();

        record.setCoin(coin);
        record.setFee(fee);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        record.setMemberId(id);
        record.setType(type.getOrdinal());
        FeeAdExRecord exRecord = feeAdExRecordDao.save(record);

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setMemberId(id);
        memberTransaction.setRefId(exRecord.getId() + "");
        if (type == TransactionType.EXCHANGE) {
            memberTransaction.setType(TransactionType.EXCHANGE_FEE_COLLECTION);
        }
        if (type == TransactionType.ADVERTISE_FEE) {
            memberTransaction.setType(TransactionType.ADVERTISE_FEE_COLLECTION);
        }
        if (type == TransactionType.WITHDRAW) {
            memberTransaction.setType(TransactionType.UP_COIN_FEE_COLLECTION);
        }
        memberTransaction.setAmount(fee);
        memberTransaction.setSymbol(coin);
        memberTransactionService.save(memberTransaction);
        MessageResult messageResult = memberWalletService.increaseBalance(walletId, fee);
        Assert.isTrue(messageResult.isSuccess(), "保存手续费失败!");
    }

    private List<FeeOtcExchangeDto> remoteExchangeFee(String start,String end){
        List<FeeOtcExchangeDto> list=new ArrayList<>();
        try {
            MessageRespResult o = exchange2FeignService.feignExchangeFee(start, end);
            list = JSONArray.parseArray(JSON.toJSONString(o.getData()), FeeOtcExchangeDto.class);
            list.forEach(l->l.setTransactionType(TransactionType.EXCHANGE));
        }catch (Exception e){
            XxlJobLogger.log("============================调用V2手续费接口失败!=================================");
        }
        return list;
    }
}














