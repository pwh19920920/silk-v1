package com.spark.bitrade.service;

import com.spark.bitrade.constant.RewardBusinessType;
import com.spark.bitrade.constant.SmsStatus;
import com.spark.bitrade.dao.SmsRecordDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.ValidateUtil;
import com.spark.bitrade.vendor.provider.SMSProvider;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 短信发送service
 * @author tansitao
 * @time 2018/9/19 19:18 
 */
@Service
@Slf4j
public class ActivitySmsSendService extends BaseService{

    @Autowired
    private BettingRecordService bettingRecordService;
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private SMSProviderProxy smsProvider;
    @Autowired
    private SmsRecordDao smsRecordDao;
    @Autowired
    private RewardService rewardService;
    @Value("${sms.winSms:}")
    private String winSms;
    @Value("${sms.notWinSms:}")
    private String notWinSms;

    @Value("${sms.winSmsCom:}")
    private String winSmsCom;
    @Value("${sms.notWinSmsCom:}")
    private String notWinSmsCom;

    @Value("${sms.winSmsEng:}")
    private String winSmsEng;
    @Value("${sms.notWinSmsEng:}")
    private String notWinSmsEng;


    /**
     * 异步发送短信
     * @author tansitao
     * @time 2018/9/19 20:21 
     */
    // @Async
    public void dealSendSms(Optional<BettingPriceRange> bettingPriceRange)
    {
        log.info("===========================发送开奖短信===============================期数：" + bettingPriceRange.get().getPeriodId());
        BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingPriceRange.get().getPeriodId());
        List<BettingRecord>  bettingRecordList = bettingRecordService.findOpenSmsRecord(bettingConfig.getId());
        if(bettingRecordList != null){
            log.info("=======需要发送的短信数========" + bettingRecordList.size());
            for (BettingRecord bettingRecord:bettingRecordList) {
                SmsRecord smsRecord = new SmsRecord();
                smsRecord.setRemark("疯狂的比特币发送中奖短信");
                smsRecord.setRefId(bettingRecord.getId()+"");
                smsRecord.setPhone(bettingRecord.getPhone());
                if(bettingPriceRange.get().getId().equals(bettingRecord.getRangeId())){
                    if(!ValidateUtil.isMobilePhone(bettingRecord.getPhone().trim())){
                        log.warn("{}用户（{}）的手机号为空或格式错误",bettingRecord.getMemberId(), bettingRecord.getPhone());
                        smsRecord.setReason(bettingRecord.getMemberId()+ "===手机号为空或格式错误==" + bettingRecord.getPhone());
                        smsRecord.setSendStatus(SmsStatus.FAIL);
                    }else{
                        try {
                            Reward reward = rewardService.findByBettingId(bettingRecord.getMemberId(), bettingRecord.getId(), RewardBusinessType.GUESS, -1);
                            if(reward != null){
                                MessageResult mr = null;
                                if ("86".equals(bettingRecord.getAreaCode())) {
                                    String content = String.format(winSms,bettingConfig.getPeriod(), reward.getRewardNum(),reward.getSymbol(),  DateUtil.dateToString(bettingConfig.getPrizeEndTime()));
                                    smsRecord.setSmsContent(content);
                                    mr = smsProvider.sendSingleMessage(bettingRecord.getPhone(), content);
                                } //add by tansitao 时间： 2018/9/12 原因：判断是否为港澳台，发送繁体短信
                                else if ("+886".equals(bettingRecord.getAreaCode()) || "+853".equals(bettingRecord.getAreaCode()) || "+852".equals(bettingRecord.getAreaCode()))
                                {
                                    String content = String.format(winSmsCom,bettingConfig.getPeriod(), reward.getRewardNum(),reward.getSymbol(),  DateUtil.dateToString(bettingConfig.getPrizeEndTime()));
                                    smsRecord.setSmsContent(content);
                                    mr = smsProvider.sendSingleMessage(bettingRecord.getAreaCode() + bettingRecord.getPhone(), content);
                                }else {
                                    String content = String.format(winSmsEng,bettingConfig.getPeriod(), reward.getRewardNum(), reward.getSymbol(),  bettingConfig.getPrizeEndTime());
                                    smsRecord.setSmsContent(content);
                                    mr = smsProvider.sendSingleMessage(bettingRecord.getAreaCode() + bettingRecord.getPhone(), content);
                                }
                                if(mr != null && mr.getCode() == 0){
                                    smsRecord.setSendStatus(SmsStatus.success);
                                }else
                                {
                                    smsRecord.setReason(mr.getMessage());
                                    smsRecord.setSendStatus(SmsStatus.FAIL);
                                }
                            }
                            else {
                                smsRecord.setReason("奖金记录表中无中奖信息");
                                smsRecord.setSendStatus(SmsStatus.FAIL);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }else {
                    if(!ValidateUtil.isMobilePhone(bettingRecord.getPhone().trim())){
                        log.warn("{}用户（{}）的手机号为空或格式错误",bettingRecord.getMemberId(), bettingRecord.getPhone());
                        smsRecord.setReason(bettingRecord.getMemberId()+ "===手机号为空或格式错误==" + bettingRecord.getPhone());
                        smsRecord.setSendStatus(SmsStatus.FAIL);
                    }
                    else{
                        try {
                            //中奖失败发送短信
                            MessageResult mr = null;
                            //发送简体短信
                            if ("86".equals(bettingRecord.getAreaCode())) {
                                String content = String.format(notWinSms,bettingConfig.getPeriod(), DateUtil.dateToString(bettingConfig.getPrizeEndTime()));
                                smsRecord.setSmsContent(content);
                                mr = smsProvider.sendSingleMessage(bettingRecord.getPhone(), content);
                            } //判断是否为港澳台，发送繁体短信
                            else if ("+886".equals(bettingRecord.getAreaCode()) || "+853".equals(bettingRecord.getAreaCode()) || "+852".equals(bettingRecord.getAreaCode()))
                            {
                                String content = String.format(notWinSmsCom,bettingConfig.getPeriod(), DateUtil.dateToString(bettingConfig.getPrizeEndTime()));
                                smsRecord.setSmsContent(content);
                                mr = smsProvider.sendSingleMessage(bettingRecord.getAreaCode() + bettingRecord.getPhone(), content);
                            } //发送英文短信
                            else {
                                String content = String.format(notWinSmsEng,bettingConfig.getPeriod(), DateUtil.dateToString(bettingConfig.getPrizeEndTime()));
                                smsRecord.setSmsContent(content);
                                mr = smsProvider.sendSingleMessage(bettingRecord.getAreaCode() + bettingRecord.getPhone(), content);
                            }
                            //判断短信是否发送成功
                            if(mr.getCode() == 0){
                                smsRecord.setSendStatus(SmsStatus.success);
                            }else {
                                smsRecord.setReason(mr.getMessage());
                                smsRecord.setSendStatus(SmsStatus.FAIL);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                save(smsRecord);
            }
        }else {
            log.info("=======无需要发送的中奖短信========");
        }

    }
    public SmsRecord save(SmsRecord smsRecord){
        return smsRecordDao.save(smsRecord);
    }

}
