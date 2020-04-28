package com.spark.bitrade.service;

import com.spark.bitrade.util.MessageResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
  * 公告
  * @author tansitao
  * @time 2018/11/6 13:57 
  */
@FeignClient("ucenter-api")
public interface IAnnouncementService {

    @RequestMapping("/uc/announcement/getLatelyAnnounce")
    MessageResult getAnnouncement();
}
