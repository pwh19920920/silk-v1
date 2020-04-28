package com.spark.bitrade.feign;

import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/***
  * 提供个人消息提醒 API服务
  * @author zhongxj
  * @time 2019.10.21
  */
@FeignClient("service-ucenter")
public interface IUserCenterConfigurationApiService {

    /**
     * 新增
     *
     * @param memberId        会员ID
     * @param triggeringEvent 事件{0:充值到账提醒,1:新订单创建提醒,2:交易即将过期,3:已付款提醒,4:已释放提醒,5:申诉处理结果提醒}
     * @param isSms           短信
     * @param isEmail         邮件
     * @param isApns          APP通知
     * @return
     */
    @PostMapping(value = "/uc2/api/v2/userCenterConfiguration/no-auth/addUserConfigurationCenter")
    MessageRespResult addUserConfigurationCenter(@RequestParam("memberId") Long memberId, @RequestParam("triggeringEvent") Integer triggeringEvent
            , @RequestParam("isSms") Integer isSms, @RequestParam("isEmail") Integer isEmail, @RequestParam("isApns") Integer isApns);

    /**
     * 修改
     *
     * @param memberId        会员ID
     * @param triggeringEvent 事件{0:充值到账提醒,1:新订单创建提醒,2:交易即将过期,3:已付款提醒,4:已释放提醒,5:申诉处理结果提醒}
     * @param channel         渠道，isSms-短信，isEmail-邮件，isApns-APP
     * @param status          开关，1-开启，2-关闭
     * @return
     */
    @PostMapping(value = "/uc2/api/v2/userCenterConfiguration/no-auth/updateUserConfigurationCenter")
    MessageRespResult updateUserConfigurationCenter(@RequestParam("memberId") Long memberId, @RequestParam("triggeringEvent") Integer triggeringEvent
            , @RequestParam("channel") String channel, @RequestParam("status") Integer status);

}
