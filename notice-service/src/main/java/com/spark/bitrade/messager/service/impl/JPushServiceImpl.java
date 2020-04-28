package com.spark.bitrade.messager.service.impl;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.DeviceType;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosAlert;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.constant.messager.NoticeTag;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.messager.dto.JPushEntity;
import com.spark.bitrade.messager.service.IJPushService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author ww
 * @time 2019.09.11 17:28
 */
@Slf4j
@Component
public class JPushServiceImpl implements IJPushService {

    @Value("${jpush.MASTER_SECRET}")
    public  String MASTER_SECRET;
    @Value("${jpush.APP_KEY}")
    public  String APP_KEY;

    @Autowired
    KafkaTemplate kafkaTemplate;



    @Override
    public void sendToUser(JPushEntity jPushEntity) {
        ClientConfig clientConfig = ClientConfig.getInstance();
        JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);

        Platform.Builder platform = Platform.newBuilder();

        if(jPushEntity.getDeviceType().size()==0){
            platform.setAll(true);
        }else{
            platform.setAll(false);
            for(JPushDeviceType jPushDeviceType:  jPushEntity.getDeviceType()){
                if(jPushDeviceType.equals(JPushDeviceType.IOS)) platform.addDeviceType(DeviceType.IOS);
                else if(jPushDeviceType.equals(JPushDeviceType.ANDROID)) platform.addDeviceType(DeviceType.Android);
                else if(jPushDeviceType.equals(JPushDeviceType.WINPHONE)) platform.addDeviceType(DeviceType.WinPhone);
            }
        }

        //合并用户别名和标签

        Audience  audience = Audience.all();

        Audience.Builder builder = Audience.newBuilder();
        boolean bbuild = false;
        if (jPushEntity.getAlias().size()>0){
            bbuild = true;
            builder.addAudienceTarget(AudienceTarget.alias(jPushEntity.getAlias()));
        }
        if (jPushEntity.getTags().size()>0){
            bbuild = true;
            builder.addAudienceTarget(AudienceTarget.tag(jPushEntity.getTags()));
        }

        if(bbuild) audience = builder.build();



        IosAlert isoAlert = IosAlert.newBuilder()
                .setTitleAndBody(jPushEntity.getTitle(), jPushEntity.getSubTitle(), "") //test ios alert json
                //.setActionLocKey("PLAY")
                .build();

        JsonObject iosSound = new JsonObject();
        iosSound.add("critical", new JsonPrimitive(1));
        iosSound.add("name", new JsonPrimitive("default"));
        iosSound.add("volume", new JsonPrimitive(0.5));

        //离线通知方式 保留
      /*  PushPayload payload = PushPayload.newBuilder()
                .setPlatform(platform)
                .setAudience(audience)
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(AndroidNotification.newBuilder()

                                .setTitle(jPushEntity.getTitle())
                                .setAlert(jPushEntity.getSubTitle())

                                .addExtras(jPushEntity.getExtras())
                                .addExtra("jsonData", jPushEntity.getJsonData())
                                .build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .incrBadge(1)
                                .setAlert(isoAlert)
                                .addExtras(jPushEntity.getExtras())
                                .setSound(iosSound)

                                .addExtra("jsonData", jPushEntity.getJsonData())
                                .build())
                        .build())
                .build();
*/

        //使用在线自定义通知的方式


        PushPayload payload = PushPayload.newBuilder()

                .setPlatform(platform.build())
                .setAudience(audience
                        //Audience.newBuilder()
                        //.addAudienceTarget(Audience.all())
                        //.addAudienceTarget(AudienceTarget.tag("tag1", "tag2"))
                        //.addAudienceTarget(AudienceTarget.alias("alias1", "alias2"))
                        //.build()
                )

                .setNotification(Notification.newBuilder()
                        //anroid 走的在线
//                        .addPlatformNotification(AndroidNotification.newBuilder()
//
//                                .setTitle(jPushEntity.getTitle())
//                                .setAlert(jPushEntity.getSubTitle())
//
//                                .addExtras(jPushEntity.getExtras())
//                                .addExtra("jsonData", jPushEntity.getJsonData())
//                                .build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .incrBadge(1)
                                .setAlert(isoAlert)
                                .addExtras(jPushEntity.getExtras())
                                .setSound(iosSound)

                                .addExtra("jsonData", jPushEntity.getJsonData())
                                .build())
                        .build())

                .setMessage(Message.newBuilder()
                        .setMsgContent(jPushEntity.getJsonData())
                        .addExtras(jPushEntity.getExtras())
                        .build())
                .build();

        try {
            PushResult result = jpushClient.sendPush(payload);
            log.info("Got result - " + result);
            System.out.println(result);
            // 如果使用 NettyHttpClient，需要手动调用 close 方法退出进程
            // If uses NettyHttpClient, call close when finished sending request, otherwise process will not exit.
            // jpushClient.close();
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);
            log.error("Sendno: " + payload.getSendno());

        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
            log.info("Msg ID: " + e.getMsgId());
            log.error("Sendno: " + payload.getSendno());
        }
    }

    @Override
    public MessageResult send(JPushEntity jPushEntity) {
        kafkaTemplate.send(NoticeType.JPUSH_NOTICE.getLable(), JSON.toJSONString(jPushEntity));


        return null;
    }

    @KafkaListener(topics = NoticeTag.JPUSH_NOTICE,groupId = "group-handle")
    @Override
    public boolean processKafkaConsumerMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            JPushEntity jPushEntity =   JSON.parseObject(record.value(),JPushEntity.class);
            //
            log.info("----------------- record 2=" + record);
            log.info("------------------ message 2 =" + jPushEntity);
            sendToUser(jPushEntity);
            log.info("------------------ MessageResult =" + "void");

        }
        ack.acknowledge();
        return true;
    }

}
