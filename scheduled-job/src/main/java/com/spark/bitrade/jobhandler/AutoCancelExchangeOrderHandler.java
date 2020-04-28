package com.spark.bitrade.jobhandler;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.ExchangeOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * 自动撤销币币交易的委托超时订单 任务（从exchange-api模块迁移过来）
  *  任务周期：每分钟执行一次（1 * * * * ? *）
 * @author yangch
 * @time 2018.07.26 14:45
 */

@JobHandler(value="autoCancelExchangeOrderHandler")
@Component
public class AutoCancelExchangeOrderHandler extends IJobHandler {
    @Autowired
    private ExchangeOrderService orderService;
    @Autowired
    private ExchangeCoinService coinService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        //核心处理逻辑描述：
        //1、获取所有交易端
        //2、判断交易端是否设置了超时时间，如设置了超时时间则进入第3步
        //3、获取超时的订单
        //4、将订单信息发送kafka的撤销队列中

        XxlJobLogger.log("start autoCancelOrder...");
        List<ExchangeCoin> coinList = coinService.findAllEnabled();
        coinList.forEach(coin->{
            if(coin.getMaxTradingTime() > 0){
                //List<ExchangeOrder> orders =  orderService.findOvertimeOrder(coin.getSymbol(),coin.getMaxTradingTime());
                List<ExchangeOrder> orders =  orderService.findOvertimeOrderRreadOnly(coin.getSymbol(),coin.getMaxTradingTime());
                orders.forEach(order -> {
                    // 发送消息至Exchange系统
                    kafkaTemplate.send("exchange-order-cancel",order.getSymbol(), JSON.toJSONString(order));
                    XxlJobLogger.log("orderId:"+order.getOrderId()+",time:"+order.getTime());
                });
            }
        });
        XxlJobLogger.log("end autoCancelOrder...");
        return SUCCESS;
    }
}
