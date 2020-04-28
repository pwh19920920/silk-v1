package com.spark.bitrade.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.CollectConstant;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.business.IBusinessEnventDispatch;
import com.spark.bitrade.envent.coin.ICoinEnventDispatch;
import com.spark.bitrade.envent.customizing.ICustomizingEnventDispatch;
import com.spark.bitrade.envent.exchange.IExchangeEnventDispatch;
import com.spark.bitrade.envent.otc.IOtcEnventDispatch;
import com.spark.bitrade.envent.uc.IUcEnventDispatch;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/***
  * 消费收集的数据
  * @author yangch
  * @time 2018.11.02 15:54
  */
@Component
@Slf4j
public class ConsumerCollectCarrier {

    @Autowired
    private IOtcEnventDispatch otcEnventDispatch;
    @Autowired
    private ICoinEnventDispatch coinEnventDispatch;
    @Autowired
    private IExchangeEnventDispatch exchangeEnventDispatch;
    @Autowired
    private IUcEnventDispatch ucEnventDispatch;
    @Autowired
    private IBusinessEnventDispatch businessEnventDispatch;
    @Autowired
    private ICustomizingEnventDispatch customizingEnventDispatch;

    @KafkaListener(topics = CollectConstant.MSG_COLLECT_CARRIER)
    public void handleMessage(ConsumerRecord<String, String> record) {
        this.getService().dispatch(record);
    }

    @Async
    protected void dispatch(ConsumerRecord<String, String> record) {
        log.info("topic={},key={},value={}"
                , record.topic(), record.key(), record.value());
        if (StringUtils.isEmpty(record.value())) {
            return;
        }
        String key = record.key();
        CollectCarrier carrier = JSON.parseObject(record.value(), CollectCarrier.class);
        //按模块分发消息
        switch (key) {
            case "OTC":
                otcEnventDispatch.dispatch(carrier);
                break;
            case "EXCHANGE":
                exchangeEnventDispatch.dispatch(carrier);
                break;
            case "COIN":
                coinEnventDispatch.dispatch(carrier);
                break;
            case "UC":
                ucEnventDispatch.dispatch(carrier);
                break;
            case "BUSINESS":
                businessEnventDispatch.dispatch(carrier);
                break;
            case "INSTATION":
                customizingEnventDispatch.dispatch(carrier);
                break;
            default:
                log.info("消费未找到相关key---------------------------");
        }
    }

    public ConsumerCollectCarrier getService() {
        return SpringContextUtil.getBean(ConsumerCollectCarrier.class);
    }
}
