package com.spark.bitrade.messager.service;

import com.spark.bitrade.messager.dto.JPushEntity;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/****
  * 提供极光推送接入服务
  * @author ww
  * @time 2019.09.30
  */
//@FeignClient("service-aurora-push")

public interface IJPushService extends IKafkaConsumerService {


    public MessageResult send(JPushEntity jPushEntity);
    public void sendToUser(JPushEntity jPushEntity);


}
