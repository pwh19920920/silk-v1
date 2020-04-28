package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.SmsProviderConfig;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.entity.Advertise;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.chat.ChatMessageRecord;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.ValidateUtil;
import com.spark.bitrade.vendor.provider.SMSProvider;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Calendar;


/***
 * 推送订单信息
  *
 * @author yangch
 * @time 2018.07.12 14:57
 */

@Service
@Slf4j
public class PushOrderMessageService {
    @Autowired
    private SMSProviderProxy smsProvider;

    @Autowired
    private SmsProviderConfig smsProviderConfig;

    /*@Autowired
    private MongoTemplate mongoTemplate ;*/

    @Autowired
    private MemberService memberService;

    @Value("${spark.system.order.sms:0}")
    private int notice;

    @Autowired
    private IChatService chatService;

    /***
     * 异步推送c2c创建订单成功后的自动消息回复
     * @author yangch
     * @time 2018.07.12 15:31 
       * @param advertise 广告信息
     * @param order c2c订单信息
     */
    @Async
    public void pushAutoResponseMessage2Mongodb(Advertise advertise, Order order){
        RealTimeChatMessage chatMessageRecord = new RealTimeChatMessage();
        chatMessageRecord.setOrderId(order.getOrderSn());
        chatMessageRecord.setUidFrom(order.getMemberId().toString());
        chatMessageRecord.setUidTo(order.getCustomerId().toString());
        chatMessageRecord.setNameFrom(order.getMemberName());
        chatMessageRecord.setNameTo(order.getCustomerName());
        chatMessageRecord.setContent(advertise.getAutoword());

        //chatMessageRecord.setSendTime(Calendar.getInstance().getTimeInMillis());
        //chatMessageRecord.setSendTimeStr(DateUtil.getDateTime());

        //自动回复消息 推送到chat模块
        chatService.chatPush(JSON.toJSONString(chatMessageRecord));

        /*ChatMessageRecord chatMessageRecord = new ChatMessageRecord();
        chatMessageRecord.setOrderId(order.getOrderSn());
        chatMessageRecord.setUidFrom(order.getMemberId().toString());
        chatMessageRecord.setUidTo(order.getCustomerId().toString());
        chatMessageRecord.setNameFrom(order.getMemberName());
        chatMessageRecord.setNameTo(order.getCustomerName());
        chatMessageRecord.setContent(advertise.getAutoword());
        chatMessageRecord.setSendTime(Calendar.getInstance().getTimeInMillis());
        chatMessageRecord.setSendTimeStr(DateUtil.getDateTime());

        //自动回复消息保存到mogondb
        //mongoTemplate.insert(chatMessageRecord,"chat_message_"+chatMessageRecord.getOrderId());
        mongoTemplate.insert(chatMessageRecord,"chat_message");*/
    }

    /***
     * 异步推送c2c创建订单成功后的短信提示
     * @author yangch
     * @time 2018.07.12 15:20 
     * @param advertise 广告信息
     * @param order c2c订单信息
     * @param user 授权用户信息
     */
    @Async
    public void pushCreateOrderMessage4SMS(Advertise advertise, Order order, AuthMember user){
        if (notice==1){
            Member smsMember = advertise.getMember();
            try {
                if ("86".equals(smsMember.getCountry().getAreaCode())) {
                    if (!ValidateUtil.isMobilePhone(smsMember.getMobilePhone().trim())) {
                        //return error(msService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
                        log.warn("{}用户（{}）的手机号为空或格式错误",smsMember.getRealName(), smsMember.getId());
                        return;
                    }
                    smsProvider.sendSingleMessage(smsMember.getMobilePhone(), String.format(smsProviderConfig.getNewOrderContent(),order.getOrderSn(),user.getName()));
                } //add by tansitao 时间： 2018/9/12 原因：判断是否为港澳台，发送繁体短信
                else if ("+886".equals(smsMember.getCountry().getAreaCode()) || "+853".equals(smsMember.getCountry().getAreaCode()) || "+852".equals(smsMember.getCountry().getAreaCode()))
                {
                    smsProvider.sendSingleMessage(smsMember.getCountry().getAreaCode() + smsMember.getMobilePhone(), String.format(smsProviderConfig.getNewOrderContentCom(),order.getOrderSn(),user.getName()));
                }else {
                    smsProvider.sendSingleMessage(smsMember.getCountry().getAreaCode() + smsMember.getMobilePhone(), String.format(smsProviderConfig.getNewOrderContentEng(),order.getOrderSn(),user.getName()));
                }
            } catch (Exception e) {
                log.error("sms 发送失败");
                e.printStackTrace();
            }
        }
    }

    /***
     * 异步推送c2c订单支付成功后的短信提示
     * @author yangch
     * @time 2018.07.12 15:57 
     * @param order 订单信息
     * @param user  授权用户信息
     */
    @Async
    public void pushPayOrderMessage4SMS(Order order, AuthMember user){
        if (notice==1) {
            try {
                Member smsMember;
                if(order.getAdvertiseType() == AdvertiseType.BUY) {
                    smsMember = memberService.findOne(order.getCustomerId());
                } else {
                    smsMember = memberService.findOne(order.getMemberId());
                }
                if ("86".equals(smsMember.getCountry().getAreaCode())) {
                    if (!ValidateUtil.isMobilePhone(smsMember.getMobilePhone().trim())) {
                        log.warn("{}用户（{}）的手机号为空或格式错误",smsMember.getRealName(), smsMember.getId());
                        return;
                        //return error(msService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
                    }
                    smsProvider.sendSingleMessage(smsMember.getMobilePhone(), String.format(smsProviderConfig.getPayedContent(),order.getOrderSn(),user.getName()));
                }//add by tansitao 时间： 2018/9/12 原因：判断是否为港澳台，发送繁体短信
                else if ("+886".equals(smsMember.getCountry().getAreaCode()) || "+853".equals(smsMember.getCountry().getAreaCode()) || "+852".equals(smsMember.getCountry().getAreaCode()))
                {
                    smsProvider.sendSingleMessage(smsMember.getCountry().getAreaCode() + smsMember.getMobilePhone(), String.format(smsProviderConfig.getPayedContentCom(),order.getOrderSn(),user.getName()));
                }
                else {
                    smsProvider.sendSingleMessage(smsMember.getCountry().getAreaCode() + smsMember.getMobilePhone(), String.format(smsProviderConfig.getPayedContentEng(),order.getOrderSn(),user.getName()));
                }
            } catch(Exception e) {
                log.error("sms 发送失败");
                e.printStackTrace();
            }
        }
    }

    /***
      * 异步推送c2c订单放行成功后的短信提示
      * @author yangch
      * @time 2018.07.12 15:57 
     * @param order 订单信息
     * @param user  授权用户信息
     */
    @Async
    public void pushReleasedOrderMessage4SMS(Order order, AuthMember user){
        if (notice==1) {
            try {
                Member smsMember;
                if(order.getAdvertiseType() == AdvertiseType.BUY) {
                    smsMember = memberService.findOne(order.getMemberId());
                } else {
                    smsMember = memberService.findOne(order.getCustomerId());
                }

                if ("86".equals(smsMember.getCountry().getAreaCode())) {
                    if (!ValidateUtil.isMobilePhone(smsMember.getMobilePhone().trim())) {
                        log.warn("{}用户（{}）的手机号为空或格式错误",smsMember.getRealName(), smsMember.getId());
                        return;
                        //return error(msService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
                    }
                    smsProvider.sendSingleMessage(smsMember.getMobilePhone(), String.format(smsProviderConfig.getReleasedContent(),order.getOrderSn(),user.getName()));
                }//add by tansitao 时间： 2018/9/12 原因：判断是否为港澳台，发送繁体短信
                else if ("+886".equals(smsMember.getCountry().getAreaCode()) || "+853".equals(smsMember.getCountry().getAreaCode()) || "+852".equals(smsMember.getCountry().getAreaCode()))
                {
                    smsProvider.sendSingleMessage(smsMember.getCountry().getAreaCode() + smsMember.getMobilePhone(), String.format(smsProviderConfig.getReleasedContentCom(),order.getOrderSn(),user.getName()));
                }else {
                    smsProvider.sendSingleMessage(smsMember.getCountry().getAreaCode() + smsMember.getMobilePhone(), String.format(smsProviderConfig.getReleasedContentEng(),order.getOrderSn(),user.getName()));
                }
            } catch(Exception e) {
                log.error("sms 发送失败");
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @author shenzucai
     * @time 2019.10.01 18:49
     * @param order
    * @param user
     * @return true
     */
    @Async
    public void pushReleasedOrderMessage4SMS(Order order, Member user){
        if (notice==1) {
            try {
                Member smsMember;
                if(order.getAdvertiseType() == AdvertiseType.BUY) {
                    smsMember = memberService.findOne(order.getMemberId());
                } else {
                    smsMember = memberService.findOne(order.getCustomerId());
                }

                if ("86".equals(smsMember.getCountry().getAreaCode())) {
                    if (!ValidateUtil.isMobilePhone(smsMember.getMobilePhone().trim())) {
                        log.warn("{}用户（{}）的手机号为空或格式错误",smsMember.getRealName(), smsMember.getId());
                        return;
                        //return error(msService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
                    }
                    smsProvider.sendSingleMessage(smsMember.getMobilePhone(), String.format(smsProviderConfig.getReleasedContent(),order.getOrderSn(),user.getUsername()));
                }//add by tansitao 时间： 2018/9/12 原因：判断是否为港澳台，发送繁体短信
                else if ("+886".equals(smsMember.getCountry().getAreaCode()) || "+853".equals(smsMember.getCountry().getAreaCode()) || "+852".equals(smsMember.getCountry().getAreaCode()))
                {
                    smsProvider.sendSingleMessage(smsMember.getCountry().getAreaCode() + smsMember.getMobilePhone(), String.format(smsProviderConfig.getReleasedContentCom(),order.getOrderSn(),user.getUsername()));
                }else {
                    smsProvider.sendSingleMessage(smsMember.getCountry().getAreaCode() + smsMember.getMobilePhone(), String.format(smsProviderConfig.getReleasedContentEng(),order.getOrderSn(),user.getUsername()));
                }
            } catch(Exception e) {
                log.error("sms 发送失败");
                e.printStackTrace();
            }
        }
    }

}
