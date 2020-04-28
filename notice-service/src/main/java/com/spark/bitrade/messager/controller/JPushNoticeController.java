package com.spark.bitrade.messager.controller;

import com.alibaba.fastjson.JSON;
import com.mysql.cj.util.StringUtils;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.messager.dto.JPushEntity;
import com.spark.bitrade.messager.service.IJPushService;
import com.spark.bitrade.util.MessageResult;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Administrator
 * @time 2019.09.10 18:28
 */


@RestController
@RequestMapping ("/jpush")

public class JPushNoticeController extends  NoticeController {


    @Autowired
    IJPushService jPushService;

    @RequestMapping("/send/jpushEntity")
    public MessageResult jpushSend(JPushEntity jpushEntity) {


        //为ios 提供notification 的内容需求
        jPushService.send(jpushEntity);

        return  MessageResult.success();
    }




    @ApiOperation(value="向手机客户端口使用极光推送", notes="alias : md5(memberId)  \n tags: zh_CN en_US  \n deviceType: all android ios winphone ios"
            ,httpMethod = "POST")


    @RequestMapping("/send")
    public MessageResult jpushSend(String alias, String tags,String title,String subtitle,@RequestParam(required = true) String content,@RequestParam(defaultValue= "")String deviceType) {  //,String titles

        JPushEntity jPushEntity = new JPushEntity();
        if(!StringUtils.isNullOrEmpty(alias)){
            jPushEntity.setAlias(Arrays.asList(alias.split(",")));
        }
        if(!StringUtils.isNullOrEmpty(tags)){
            jPushEntity.setTags(Arrays.asList(tags.split(",")));
        }
        jPushEntity.setTitle(title);
        jPushEntity.setSubTitle(subtitle);
        jPushEntity.setJsonData(content);

        try{
            if(!StringUtil.isNullOrEmpty(deviceType)){
                for (String s: deviceType.trim().split(" ")
                ) {
                    jPushEntity.getDeviceType().add(JPushDeviceType.valueOf(s.toUpperCase()));
                }
            }
        }catch (Exception e){
            return  MessageResult.error("device type error");
        }

        return jpushSend(jPushEntity);
    }




    @RequestMapping("/send/jpushEntityJson")
    public MessageResult jpushSend(@RequestBody String jpushEntityJson) {

        try {
            JPushEntity jpushEntity = JSON.parseObject(jpushEntityJson, JPushEntity.class);
            return jpushSend(jpushEntityJson);
        }catch (Exception e){
            return MessageResult.error("jpushEntityJson 参数不正确");
        }
    }


    @RequestMapping("/test")
    public MessageResult test() {


        JPushEntity jPushEntity = new JPushEntity();
        jPushEntity.setAlias(Arrays.asList("kingvo".split(",")));
        if(!StringUtils.isNullOrEmpty("ww,kk")){
            jPushEntity.setTags(Arrays.asList("ww,kk".split(",")));
        }
        jPushEntity.setTitle(" jpush alert / title");
        jPushEntity.setSubTitle("jpush Content");

        HashMap<String,String> hm = new HashMap<>();
        hm.put("123123","123123");
        jPushEntity.setExtras(hm);

        //jPushEntity.setDeviceType(JPushDeviceType.ALL);
        return jpushSend(jPushEntity);
    }






}
