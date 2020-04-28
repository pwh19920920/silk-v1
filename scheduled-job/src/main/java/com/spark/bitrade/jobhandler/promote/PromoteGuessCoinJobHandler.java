package com.spark.bitrade.jobhandler.promote;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.PushType;
import com.spark.bitrade.entity.GuessCoin;
import com.spark.bitrade.service.GuessCoinService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.yunpian.sdk.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 获取非小号竞猜币种全网数据定时任务
 * @author Zhang Yanjun
 * @time 2018.09.19 10:40
 */
@JobHandler(value = "PromoteGuessCoinJobHandler")
@Component
public class PromoteGuessCoinJobHandler extends IJobHandler{
    @Autowired
    GuessCoinService guessCoinService;
    @Autowired
    KafkaTemplate kafkaTemplate;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        String symbol ="bitcoin";
        if(s!=null){
            symbol = s;
        }

        //删除缓存
        guessCoinService.flushGuessCoin(symbol);
        XxlJobLogger.log("删除非小号"+symbol+"全网数据");
        //日志 获取非小号接口数据
        GuessCoin guessCoin=guessCoinService.getGuessCoin(symbol);
        XxlJobLogger.log("获取非小号"+symbol+"全网数据"+guessCoin);
        //推送消息
        String m=this.sendKafkaMessage(guessCoin);
        XxlJobLogger.log(m);
        return SUCCESS;
    }

    //发送kafka消息
    private String sendKafkaMessage(GuessCoin guessCoin){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("type", PushType.BTCPRICE.getOrdinal());
        //BTC实时数据
        jsonObject.put("data", JsonUtil.toJson(guessCoin));
        kafkaTemplate.send("msg-promote-guess-handler", PushType.BTCPRICE.getOrdinal()+"", jsonObject.toJSONString());
        return "kafka发送成功";
    }
}
