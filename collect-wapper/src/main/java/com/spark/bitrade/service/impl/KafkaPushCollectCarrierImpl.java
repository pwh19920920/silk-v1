package com.spark.bitrade.service.impl;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.CollectConstant;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.service.IPushCollectCarrier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/***
 * kafka方式推送
 * @author yangch
 * @time 2018.11.01 19:35
 */
@Service
public class KafkaPushCollectCarrierImpl implements IPushCollectCarrier {
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    public void push(CollectCarrier carrier) {
        kafkaTemplate.send(CollectConstant.MSG_COLLECT_CARRIER,carrier.getCollectType().getCnType(), JSON.toJSONString(carrier));
    }
}
