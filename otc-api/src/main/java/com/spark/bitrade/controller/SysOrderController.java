package com.spark.bitrade.controller;

import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.util.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 后台系统埋点事件处理controller
 *
 * @author Zhongxj
 * @date 2019.09.10
 */
@Api(description = "后台系统埋点事件处理controller")
@RestController
@RequestMapping(value = "/sys/order", method = RequestMethod.POST)
@Slf4j
public class SysOrderController {

    /**
     * 申诉结果（胜诉），后采用kafka的发送消息的方式，此接口先保留，备用
     *
     * @param appealId 申诉ID
     * @return
     */
    @RequestMapping(value = "/appeal/result", method = RequestMethod.POST)
    public MessageResult appealResult(String appealId) {
        getService().appealResultEvent(appealId);
        return MessageResult.success();
    }


    /**
     * 申诉结果，事件埋点
     *
     * @param appealId 申诉ID
     */
    @CollectActionEvent(collectType = CollectActionEventType.OTC_APPEAL_ORDER, refId = "#appealId")
    public void appealResultEvent(String appealId) {
    }

    public SysOrderController getService() {
        return SpringContextUtil.getBean(SysOrderController.class);
    }
}
