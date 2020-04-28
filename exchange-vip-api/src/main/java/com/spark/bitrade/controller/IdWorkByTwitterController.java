package com.spark.bitrade.controller;

import com.spark.bitrade.config.IdWorkByTwitterConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/***
 * 提供分布式ID的注册信息
 * @author yangch
 * @time 2018.11.21 10:19
 */

@RestController
public class IdWorkByTwitterController {

    @RequestMapping("/idWorkByTwitter/registerInfo")
    @ResponseBody
    public String registerInfo(){
        return IdWorkByTwitterConfig.getRegisterInfo();
    }
}
