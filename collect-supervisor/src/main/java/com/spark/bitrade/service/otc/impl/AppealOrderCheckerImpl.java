package com.spark.bitrade.service.otc.impl;

import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.otc.IAppealOrderEnventHandler;
import com.spark.bitrade.service.AppealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
 *C2C订单申诉事件通知处理实现类
 *@authoryangch
 *@time2018.11.0215:17
 */
@Service
@Slf4j
public class AppealOrderCheckerImpl implements IAppealOrderEnventHandler {


    @Autowired
    private AppealService appealService;

    @Override
    public void handle(CollectCarrier carrier) {
//        log.info("=================AppealOrderCheckerImpl=================AppealId:" + carrier.getRefId() + "=================memberId:" + carrier.getMemberId());
//        if (StringUtils.isEmpty(carrier.getMemberId())) {
//            log.info("==============会员id不存在，无法进行申诉订单监控===================");
//            return;
//        }
//
//        Appeal appeal = appealService.findOne(Long.parseLong(carrier.getRefId()));
//        // add by tansitao 时间： 2018/12/24 原因：只处理完成了的订单申诉
//        if (appeal == null || appeal.getStatus() != AppealStatus.PROCESSED) {
//            log.info("==============申诉记录不存在或者不是处理申诉事件，不进行申诉订单监控===================appealId:{}====memberId:{}", carrier.getRefId(), carrier.getMemberId());
//            return;
//        }

    }
}