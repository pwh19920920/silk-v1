package com.spark.bitrade.jobhandler;

import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.service.OrderService;
import com.spark.bitrade.service.RedisCountorService;
import com.spark.bitrade.util.SpringContextUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

import java.util.List;

import static com.spark.bitrade.entity.QOrder.order;

/***
  * 自动撤销C2C的超时订单 任务（从otc-api模块迁移过来）
 *  任务周期：每分钟执行一次（1 * * * * ? *）
  * @author yangch
  * @time 2018.07.26 14:45
  */

@JobHandler(value = "autoCancelOtcOrderHandler")
@Component
public class AutoCancelOtcOrderHandler extends IJobHandler {
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisCountorService redisCountorService;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        //核心处理逻辑描述：
        //1、获取所有超时的订单ID
        //3、获取超时的订单
        //4、将订单信息发送kafka的撤销队列中

        XxlJobLogger.log("=========开始检查过期订单===========");
        List<String> list = orderService.checkExpiredOrder();
        list.stream().forEach(orderSn -> {
                    try {
                        orderService.cancelOrderTask(orderSn);
                        //add by tansitao 时间： 2019/1/3 原因：自动取消订单监控数据
                        getService().cancelOrder(orderSn);
                        //add by tansitao 时间： 2018/11/20 原因：自动取消订单成功，减去交易中的订单数
                        Order order = orderService.findOneByOrderSn(orderSn);
                        if (order != null) {
                            redisCountorService.subtractHash(SysConstant.C2C_MONITOR_ORDER + order.getMemberId() + "-" + order.getAdvertiseId(), SysConstant.C2C_ONLINE_NUM);
                            XxlJobLogger.log("=========订单{0}超时自动减去同时在线交易量===========", orderSn);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        XxlJobLogger.log("订单编号{0}:自动取消失败->{1}", orderSn, e.getMessage());
                    }
                }
        );
        XxlJobLogger.log("=========检查过期订单结束===========");
        return SUCCESS;
    }

    @CollectActionEvent(collectType = CollectActionEventType.OTC_CANCEL_ORDER, refId = "#orderSn")
    public void cancelOrder(String orderSn) {
    }

    public AutoCancelOtcOrderHandler getService() {
        return SpringContextUtil.getBean(AutoCancelOtcOrderHandler.class);
    }
}
