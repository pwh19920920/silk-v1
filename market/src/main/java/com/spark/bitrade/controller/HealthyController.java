package com.spark.bitrade.controller;

import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.service.KLineService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/***
 * 
 * @author yangch
 * @time 2018.05.26 17:08
 */

@RestController
public class HealthyController {
    @Autowired
    private KLineService kLineService;
    int i=0;

    /***
      * 负载均衡健康检查接口
      * @author yangch
      * @time 2018.05.26 17:04 
      */
    @RequestMapping("healthy")
    public MessageResult healthy(){
        return MessageResult.success();
    }

//    //测试接口
//    //@RequestMapping("healthy")
//    @RequestMapping("mongodb/kline")
//    public MessageResult klineTest(int type,String symbol, long fromTime, long toTime){
//        // mongodb/kline?type=1&symbol=DOGE/USDT&fromTime=1529766669261&toTime=1529778088203
//        MessageResult  result = MessageResult.success();
//        if(type==1) {
//            Map<String,BigDecimal> map = kLineService.getKLineBaseData(symbol, fromTime, toTime);
//            result.setData(map);
//        }else if(type==2) {
//            Map<String,BigDecimal> map =kLineService.getKLinExtendData(symbol, fromTime, toTime);
//            result.setData(map);
//        } else if(type==3){
//            KLine kLine = kLineService.getKlineFromTrade(symbol,"test", fromTime, toTime);
//            result.setData(kLine);
//        } else if(type==4){
//            boolean flag = kLineService.existsKlineByTime(symbol, "1min",  fromTime);
//            result.setData(flag);
//        }
//
//        return result;
//    }

    @RequestMapping("/sleep/{sleepTime}")
    public String sleep(@PathVariable Long sleepTime) throws InterruptedException {
        //超时测试接口
        int i =0/1;
        TimeUnit.SECONDS.sleep(sleepTime);
        return "SUCCESS";
    }
}
