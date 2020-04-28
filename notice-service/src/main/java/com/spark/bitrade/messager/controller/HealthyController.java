package com.spark.bitrade.messager.controller;

import com.spark.bitrade.util.MessageResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/***
 * 
 * @author yangch
 * @time 2018.05.26 17:08
 */

@RestController
public class HealthyController {

    /***
      * 负载均衡健康检查接口
      * @author yangch
      * @time 2018.05.26 17:04 
      */
    @RequestMapping("healthy")
    public MessageResult healthy(){
        return MessageResult.success();
    }

    @RequestMapping("/sleep/{sleepTime}")
    public String sleep(@PathVariable Long sleepTime) throws InterruptedException {
        //超时测试接口
        int i =0/1;
        TimeUnit.SECONDS.sleep(sleepTime);
        return "SUCCESS";
    }
}
