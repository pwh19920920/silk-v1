package com.spark.bitrade.core;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.AlarmType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.AlarmMonitor;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.service.AlarmMonitorService;
import com.spark.bitrade.service.MemberService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 告警监控推送处理
 * @author Zhang Yanjun
 * @time 2018.09.27 11:03
 */
@Component
public class AlarmMonitorConsumer {

    @Autowired
    AlarmMonitorService alarmMonitorService;
    @Autowired
    MemberService memberService;

    private Logger logger = LoggerFactory.getLogger(AlarmMonitorConsumer.class);

    //接收告警消息，并存入表、同时禁止该会员交易
    @KafkaListener(topics = "msg-alarm-monitor",group = "group-handle")
    public void saveMessage(ConsumerRecord<String,String> record){
        logger.info("topic={},key={},value={}",record.topic(),record.key(),record.value());
        if(StringUtils.isEmpty(record.value())){
            return ;
        }
        AlarmMonitor alarmMonitor = JSON.parseObject(record.value(), AlarmMonitor.class);
        alarmMonitorService.save(alarmMonitor);//存入告警监控表
        if (alarmMonitor.getMemberId()!=null){
            Member member=memberService.findOne(alarmMonitor.getMemberId());
            Assert.notNull(member,"没有此会员");
            member.setTransactionStatus(BooleanEnum.IS_FALSE);//禁止交易
            memberService.save(member);
        }


        /*JSONObject json = JSON.parseObject(record.value());
        if(json == null){
            return ;
        }
        String msg = record.value();
        JSONObject jsonObject= JSON.parseObject(msg);
        AlarmMonitor alarmMonitor=new AlarmMonitor();
        alarmMonitor.setMemberId(jsonObject.getLong("memberId"));
        alarmMonitor.setAlarmMsg(jsonObject.getString("alarmMsg"));
        alarmMonitor.setAlarmType(AlarmType.valueOf(jsonObject.getString("alarmType")));
        alarmMonitor.setStatus(BooleanEnum.valueOf(jsonObject.getString("status")));
        alarmMonitorService.save(alarmMonitor);//存入告警监控表
        if (alarmMonitor.getMemberId()!=null){
            Member member=memberService.findOne(alarmMonitor.getMemberId());
            Assert.notNull(member,"没有此会员");
            member.setTransactionStatus(BooleanEnum.IS_FALSE);//禁止交易
            memberService.save(member);
        }*/
    }
}
