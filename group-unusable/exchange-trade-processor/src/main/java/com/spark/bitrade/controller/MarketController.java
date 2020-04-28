package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MarketController {
    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;

    @Autowired
    private ExchangeOrderService exchangeOrderService;


    @Autowired
    private ExchangeMemberDiscountRuleService exchangeMemberDiscountRuleService;

    /***
     * 业务重做接口
     * 访问地址：/market/redo?id=
     * @author yangch
     * @time 2018.06.09 14:16 
       * @param id 异常业务ID
     */
    @RequestMapping("redo")
    //@Transactional(rollbackFor = Exception.class)  不能在此提供事务，方法中有try catch 会报 org.springframework.transaction.TransactionSystemException: Could not commit JPA transaction; nested exception is javax.persistence.RollbackException: Transaction marked as rollbackOnly
    public MessageResult redo(Long id){
        BusinessErrorMonitor businessErrorMonitor = businessErrorMonitorService.findOne(id);
        if(null == businessErrorMonitor){
            return MessageResult.error("指定的业务重做记录不存在");
        }

        //已处理则返回
        if(businessErrorMonitor.getMaintenanceStatus() == BooleanEnum.IS_TRUE){
            return MessageResult.success();
        }

        try {
            MessageResult result;
            if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE
                    || businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_BUY
                    || businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_SELL ) {
                //币币交易--撮单成功后成交明细处理错误
                //恢复实体
                ExchangeTrade trade = JSON.parseObject(businessErrorMonitor.getInData(), ExchangeTrade.class);
                if(null == trade){
                    return MessageResult.error("撮单成交明细参数为空");
                }
                if(businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE) {
                    result = exchangeOrderService.redoProcessExchangeTrade(trade);
                } else if(businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_BUY) {
                    result = exchangeOrderService.processExchangeTrade(trade, ExchangeOrderDirection.BUY);
                } else if(businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_SELL){
                    result = exchangeOrderService.processExchangeTrade(trade, ExchangeOrderDirection.SELL);
                } else{
                    result = MessageResult.error("重做类型错误");
                }
            } else if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__TRADE_COMPLETED) {
                //币币交易--撮单成功后订单完成处理出错
                //恢复实体
                ExchangeOrder order = JSON.parseObject(businessErrorMonitor.getInData(), ExchangeOrder.class);
                if(null == order) {
                    return MessageResult.error("订单参数为空");
                }

                //result = exchangeOrderService.tradeCompleted(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
                result = exchangeOrderService.tradeCompleted(order, BooleanEnum.IS_TRUE);
                if(result.getCode()!=0){
                    //判断订单状态为成功或撤单，则认为处理成功
                    ExchangeOrder orderNow = exchangeOrderService.findOne(order.getOrderId());
                    if( orderNow.getStatus() == ExchangeOrderStatus.CANCELED || orderNow.getStatus() == ExchangeOrderStatus.COMPLETED) {
                        result = MessageResult.success("订单状态发生变化，当前状态为"+orderNow.getStatus().name());
                    }
                }
            } else if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__ORDER_RETURN_BALANCE_FAIL) {
                //币币交易--归还订单余额失败
                //恢复实体
                ExchangeOrder order = JSON.parseObject(businessErrorMonitor.getInData(), ExchangeOrder.class);
                if(null == order) {
                    return MessageResult.error("订单参数为空");
                }
                result = exchangeOrderService.returnOrderBalance(order, BooleanEnum.IS_TRUE);

            } else {
                return MessageResult.error("该接口不支持该业务重做");
            }

            if(result.getCode() == 0 ) {
                //更改重做记录状态
                businessErrorMonitor.setMaintenanceStatus(BooleanEnum.IS_TRUE);
                businessErrorMonitor.setMaintenanceResult("业务重做成功");
                businessErrorMonitorService.save(businessErrorMonitor);

                return  result;
            } else {
                throw new Exception(result.getMessage());
            }
        }catch (Exception e){
            try {
                //保存重做记录错误信息
                businessErrorMonitor.setMaintenanceResult(e.getMessage());
                businessErrorMonitorService.update4NoRollback(businessErrorMonitor);
            }catch (Exception ex) { }
            return MessageResult.error("业务重做出错，错误信息："+e.getMessage());
        }
    }


    //刷新会员优惠规则缓存，访问地址： /exTradeProcesser/flushMemberDiscountRuleCache?memberId=0
    @RequestMapping("flushMemberDiscountRuleCache")
    public MessageResult flushMemberDiscountRuleCache(@RequestParam(value = "memberId",required = false) Long memberId){
        if(null == memberId) {
            exchangeMemberDiscountRuleService.flushCache();
            //return MessageResult.success("缓存数据", exchangeMemberDiscountRuleService.getMapDiscountRuleCache());
        } else{
            exchangeMemberDiscountRuleService.flushCache(memberId);
        }

        return MessageResult.success("更新成功");
    }

    //获取会员优惠规则缓存，访问地址： /exTradeProcesser/getMemberDiscountRuleCache?memberId=0
    @RequestMapping("getMemberDiscountRuleCache")
    public MessageResult getMemberDiscountRuleCache(@RequestParam(value = "memberId",required = false) Long memberId){
        if(null == memberId) {
            return MessageResult.success("缓存数据", exchangeMemberDiscountRuleService.getMapDiscountRuleCache());
        } else {
            return MessageResult.success("缓存数据", exchangeMemberDiscountRuleService.getMapDiscountRuleCache(memberId));
        }
    }
}
