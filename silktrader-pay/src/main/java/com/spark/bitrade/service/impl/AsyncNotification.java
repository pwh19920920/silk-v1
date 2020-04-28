package com.spark.bitrade.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.spark.bitrade.entity.AsyncNotificationBusiness;
import com.spark.bitrade.service.IAsyncNotification;
import com.spark.bitrade.service.IAsyncNotificationBusinessService;
import com.spark.bitrade.utils.DesBase64Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shenzucai
 * @time 2019.07.29 13:53
 */
@Service
@Slf4j
public class AsyncNotification implements IAsyncNotification {


    @Autowired
    private IAsyncNotificationBusinessService iAsyncNotificationBusinessService;


    /**
     * @param appid   应用渠道表示
     * @param orderId 种子商城订单号id
     * @param tradeSn 云端转账交易编号
     * @param status  转账结果，成功为ok，错误为出错说明
     * @param tag     转账标志
     * @return true
     * @author shenzucai
     * @time 2019.07.29 13:52
     */
    @Async
    @Override
    public void asyncNotification(String appid, String orderId, String tradeSn, String status, String tag) {

        RestTemplate restTemplate = new RestTemplate();
        try {
            // 保存业务关联信息
            AsyncNotificationBusiness asyncNotificationBusiness = new AsyncNotificationBusiness();
            asyncNotificationBusiness.setId(IdWorker.getId());
            asyncNotificationBusiness.setAppId(appid);
            asyncNotificationBusiness.setOrderId(orderId);
            asyncNotificationBusiness.setStatus(status);
            asyncNotificationBusiness.setTag(tag);
            asyncNotificationBusiness.setTraderSn(tradeSn);
            asyncNotificationBusiness.setCreateTime(new Date());
            iAsyncNotificationBusinessService.insert(asyncNotificationBusiness);
            log.info("异步通知内容为： {}", asyncNotificationBusiness);
            String caseTag = StringUtils.lowerCase(tag);
            switch (caseTag) {
                case "zzsc":
                    postContent("http://shop.bttc.bi/seed/payCallBack", orderId, tradeSn, status, caseTag, restTemplate);
                    break;
                case "etgsc":
                    postContent("http://etg.taokevip.com.cn/btBack", orderId, tradeSn, status, caseTag, restTemplate);
                    break;
                case "zzscpaidui":
                    postContent("http://golden.bttc.bi/api/seed/payCallBack", orderId, tradeSn, status, caseTag, restTemplate);
                    break;
                default:
                    log.info("无法进行通知 {}", caseTag);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("远程获取通知链接失败");
        }
    }

    private void postContent(String asyncNotificationUrl, String orderId, String tradeSn, String status, String tag, RestTemplate restTemplate) {
        if (StringUtils.isNotEmpty(asyncNotificationUrl)) {
            // 进行异步通知
            Map<String, String> stringStringMap = new HashMap<>(3);
            stringStringMap.put("orderId", orderId);
            stringStringMap.put("tradeSn", tradeSn);
            stringStringMap.put("status", status);
            String json = JSON.toJSONString(stringStringMap);
            // 进行加密
            String notifyBody = null;
            String caseTag = StringUtils.lowerCase(tag);
            switch (caseTag) {
                case "zzsc":
                    notifyBody=DesBase64Util.encodeDES(json, "zzsc2019" );
                    break;
                case "etgsc":
                    notifyBody=DesBase64Util.encodeDES(json, "EtgSc2019");
                    break;
                case "zzscpaidui":
                    notifyBody=DesBase64Util.encodeDES(json, "paid2019");
                    break;
                default:
                    log.info("无法进行通知 {}", caseTag);
                    break;
            }

            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
            postParameters.add("result", notifyBody);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, Object>> r = new HttpEntity<>(postParameters, headers);
            ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(asyncNotificationUrl, r, String.class);
            if (stringResponseEntity.getStatusCode() != HttpStatus.OK) {
                // 重试三次
                int count = 0;
                while (stringResponseEntity.getStatusCode() != HttpStatus.OK && count < 3) {
                    try {
                        Thread.sleep((count + 1) * 5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stringResponseEntity = restTemplate.postForEntity(asyncNotificationUrl, r, String.class);
                    count++;
                }
            }
        }
    }
}
