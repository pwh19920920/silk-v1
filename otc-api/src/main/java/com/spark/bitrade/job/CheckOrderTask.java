package com.spark.bitrade.job;

import com.spark.bitrade.entity.Order;
import com.spark.bitrade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年01月22日
 */
//del by yangch 时间： 2018.07.27 原因：迁移到 schedule-job模块中，类名为 AutoCancelOtcOrderHandler
//@Component
//@Slf4j
//public class CheckOrderTask {
//    @Autowired
//    private OrderService orderService;
//
//    @Scheduled(fixedRate = 60000)
//    public void checkExpireOrder() {
//        log.info("=========开始检查过期订单===========");
//        List<Order> list = orderService.checkExpiredOrder();
//        list.stream().forEach(x -> {
//                    try {
//                        orderService.cancelOrderTask(x);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        log.warn("订单编号{}:自动取消失败", x.getOrderSn());
//                    }
//                }
//        );
//        log.info("=========检查过期订单结束===========");
//    }
//}
