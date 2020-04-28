package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.business.IBusinessEnventDispatch;
import com.spark.bitrade.envent.coin.ICoinEnventDispatch;
import com.spark.bitrade.envent.customizing.ICustomizingEnventDispatch;
import com.spark.bitrade.envent.otc.IOtcEnventDispatch;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/***
  * 
  * @author yangch
  * @time 2018.05.26 17:08
  */

@RestController
public class TestController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    int idx = 0;
    @Autowired
    private ICoinEnventDispatch coinEnventDispatch;
    @Autowired
    private IOtcEnventDispatch otcEnventDispatch;
    @Autowired
    private IBusinessEnventDispatch businessEnventDispatch;
    @Autowired
    private ICustomizingEnventDispatch customizingEnventDispatch;

    /**
     * 手动发送站内信
     *
     * @return
     */
    @RequestMapping("/customizing")
    public MessageResult customizing(String refId) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.MANUAL_INSTATION);
        carrier.setRefId(refId);
        customizingEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------customizing Method-----------");
    }

    /**
     * C2C订单即将过期
     *
     * @return
     */
    @RequestMapping("/expire/remind/order")
    public MessageResult expireRemindOrder(String refId) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.EXPIRE_REMIND_ORDER);
        carrier.setRefId(refId);
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------expireRemindOrder Method---------");
    }

    /**
     * 充值到账
     *
     * @param memberId
     * @param txHash
     * @return
     */
    @RequestMapping("coin/in")
    public MessageResult coinIn(String memberId, String txHash) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.COIN_IN);
        carrier.setMemberId(memberId);
        carrier.setRefId(txHash);
        coinEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------coinIn---------");
    }

    /**
     * 商家认证审核通过
     *
     * @param memberId
     * @return
     */
    @RequestMapping("business/approved")
    public MessageResult businessApproved(String memberId) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.BUSINESS_APPROVE);
        carrier.setRefId(memberId);
        String msg = JSON.toJSONString(carrier);
        kafkaTemplate.send("msg-collectcarrier", "BUSINESS", msg);
        return MessageResult.success("--------businessApproved---------");
    }

    /**
     * 创建法币交易订单
     *
     * @param memberId 会员ID
     * @param refId    关联订单号
     * @param locale   语言，zh_CN-中文，en_US-英文
     * @return
     */
    @RequestMapping("create/order")
    public MessageResult createOrder(String memberId, String refId, String locale) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_ADD_ORDER);
        carrier.setMemberId(memberId);
        carrier.setRefId(refId);
        carrier.setLocale(locale);
        carrier.setCreateTime(new Date());
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------createOrder---------");
    }

    /**
     * 标记已付款
     *
     * @param memberId 会员ID
     * @param refId    关联订单号
     * @param locale   语言，zh_CN-中文，en_US-英文
     * @return
     */
    @RequestMapping("pay/order")
    public MessageResult payOrder(String memberId, String refId, String locale) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_PAY_CASH);
        carrier.setMemberId(memberId);
        carrier.setRefId(refId);
        carrier.setLocale(locale);
        carrier.setCreateTime(new Date());
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------payOrder---------");
    }

    /**
     * 释放货币
     *
     * @param memberId 会员ID
     * @param refId    关联订单号
     * @param locale   语言，zh_CN-中文，en_US-英文
     * @return
     */
    @RequestMapping("release/order")
    public MessageResult releaseOrder(String memberId, String refId, String locale) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_PAY_COIN);
        carrier.setMemberId(memberId);
        carrier.setRefId(refId);
        carrier.setLocale(locale);
        carrier.setCreateTime(new Date());
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------releaseOrder---------");
    }

    /**
     * 发起申诉
     *
     * @param memberId 会员ID
     * @param refId    关联订单号
     * @param locale   语言，zh_CN-中文，en_US-英文
     * @return
     */
    @RequestMapping("appeal/order")
    public MessageResult appealOrder(String memberId, String refId, String locale) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_APPEAL_ORDER);
        carrier.setMemberId(memberId);
        carrier.setRefId(refId);
        carrier.setLocale(locale);
        carrier.setCreateTime(new Date());
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------appealOrder---------");
    }

    /**
     * 取消申诉
     *
     * @param memberId 会员ID
     * @param refId    关联订单号
     * @param locale   语言，zh_CN-中文，en_US-英文
     * @return
     */
    @RequestMapping("appeal/cancle/order")
    public MessageResult appealCancleOrder(String memberId, String refId, String locale) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_APPEAL_ORDER);
        carrier.setMemberId(memberId);
        carrier.setRefId(refId);
        carrier.setLocale(locale);
        carrier.setCreateTime(new Date());
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------appealCancleOrder---------");
    }

    /**
     * 申诉胜诉
     *
     * @param refId 关联订单号
     * @return
     */
    @RequestMapping("finished/appeal/order")
    public MessageResult finishedAppealOrder(String refId) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_APPEAL_ORDER_COMPLETE);
        carrier.setRefId(refId);
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------finishedAppealOrder---------");
    }

    /**
     * 主动取消订单
     *
     * @param memberId 会员ID
     * @param refId    关联订单号
     * @param locale   语言，zh_CN-中文，en_US-英文
     * @return
     */
    @RequestMapping("manual/cancle/order")
    public MessageResult manualCancleOrder(String memberId, String refId, String locale) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_CANCEL_ORDER);
        carrier.setMemberId(memberId);
        carrier.setRefId(refId);
        carrier.setLocale(locale);
        carrier.setCreateTime(new Date());
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------manualCancleOrder---------");
    }

    /**
     * 被动取消订单
     *
     * @param refId 关联订单号
     * @return
     */
    @RequestMapping("auto/cancle/order")
    public MessageResult autoCancleOrder(String refId) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_CANCEL_ORDER);
        carrier.setRefId(refId);
        otcEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------autoCancleOrder---------");
    }

    /**
     * 发送kafka消息
     *
     * @param refId
     * @return
     */
    @RequestMapping("send/kafka")
    public MessageResult sendKafka(String refId) {
        CollectCarrier carrier = new CollectCarrier();
        carrier.setCollectType(CollectActionEventType.OTC_APPEAL_ORDER_COMPLETE);
        carrier.setRefId(refId);
        String msg = JSON.toJSONString(carrier);
        kafkaTemplate.send("msg-collectcarrier", "OTC", msg);
        return MessageResult.success("--------sendKafka11---------");
    }

    @RequestMapping("test1")
    public MessageResult test1(Integer i) {
        CollectCarrier carrier = new CollectCarrier();
        if (i % 2 == 0) {
            carrier.setCollectType(CollectActionEventType.COIN_IN);
        } else {
            carrier.setCollectType(CollectActionEventType.COIN_OUT);
        }
        carrier.setMemberId("22222");
        carrier.setRefId("33333");
        carrier.setExtend("44444");
        carrier.setCreateTime(new Date());


        coinEnventDispatch.dispatch(carrier);
        return MessageResult.success("--------test---------");
    }

    @RequestMapping("healthy3")
    public MessageResult test3() {
        idx++;

        System.out.println("---00-----------------------------------");
        System.out.println(getService().testActionEventMonitor(idx));

        System.out.println("---11-----------------------------------");
        System.out.println(getService().testActionEventMonitor2(idx, idx));

        System.out.println("---22-----------------------------------");
        TUser tUser = new TUser();
        tUser.setId(idx);
        tUser.setName("tname-" + idx);
        System.out.println(getService().testActionEventMonitor3(tUser));

        return MessageResult.success();
    }

    //注解使用，1个spring EL表达式
    @CollectActionEvent(
            collectType = CollectActionEventType.NONE, memberId = "1", refId = "#id")
    public String testActionEventMonitor(int id) {
        System.out.println("--------testActionEventMonitor----------");
        return "testActionEventMonitor-" + id;
    }

    //注解使用，2个spring EL表达式
    //@CollectActionEvent(
    //        collectType=CollectActionEventType.OTC_APPEAL_ORDER, memberId ="#uid" , refId = "#id+5")
    @CollectActionEvent(
            collectType = CollectActionEventType.OTC_APPEAL_ORDER, memberId = "77639", refId = "#id+5")
    public String testActionEventMonitor2(long uid, int id) {
        System.out.println("--------testActionEventMonitor2----------");
        return "testActionEventMonitor2-" + id;
    }

    //注解使用，2个spring EL表达式，使用实体类传参
    //@ActionEventMonitor(module= BusinessModule.OTC, memberId ="#tUser.getId()" , refId = "#tUser.getId()+'---'+#tUser.getName()")
    @CollectActionEvent(
            collectType = CollectActionEventType.OTC_ADD_ORDER, memberId = "77639", refId = "#tUser.getId()+'---'+#tUser.getName()")
    public String testActionEventMonitor3(TUser tUser) {
        System.out.println("--------testActionEventMonitor3----------");
        return "testActionEventMonitor3-" + tUser.getId() + "," + tUser.getName();
    }

    @Data
    private static class TUser {
        private long id;
        private String name;
    }

    public TestController getService() {
        return SpringContextUtil.getBean(TestController.class);
    }
}
