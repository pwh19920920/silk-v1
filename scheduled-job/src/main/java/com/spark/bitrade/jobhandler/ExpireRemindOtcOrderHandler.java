package com.spark.bitrade.jobhandler;

import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.service.OrderService;
import com.spark.bitrade.service.optfor.RedisKeyService;
import com.spark.bitrade.service.optfor.RedisZSetService;
import com.spark.bitrade.util.SpringContextUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/***
  * C2C订单，还有指定时间过期提醒 任务周期：每分钟执行一次（1 * * * * ? *）
  * @author zhongxj
  * @time 2019.09.20
  */

@JobHandler(value = "expireRemindOtcOrderHandler")
@Component
public class ExpireRemindOtcOrderHandler extends IJobHandler {
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisKeyService redisKeyService;
    @Autowired
    private RedisZSetService redisZSetService;
    private String prefix = SysConstant.OTC_EXPIRE_REMIND;
    private long expireTime = 3L;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("=========开始检查即将过期订单===========");
        // 还剩10分钟到期提醒，后期可考虑配置文件灵活读取
        List<String> list = orderService.expireRemindOrder(10L);
        if (list != null && list.size() > 0) {
            list.stream().forEach(orderSn -> {
                try {
                    String key = prefix + orderSn;
                    // 判断缓存是否存在
                    Set setChat = redisZSetService.zRange(key, 0, -1);
                    if (setChat != null && setChat.size() > 0) {
                        XxlJobLogger.log("=========订单{0}已推送过即将过期提醒，不再推送===========", orderSn);
                    } else {
                        // 写入缓存
                        redisZSetService.zAdd(key, orderSn, System.currentTimeMillis());
                        redisKeyService.expire(key, expireTime, TimeUnit.DAYS);
                        getService().expireRemindOrder(orderSn);
                        XxlJobLogger.log("=========订单{0}即将过期推送消息提醒===========", orderSn);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    XxlJobLogger.log("订单编号{0}:推送消息提醒失败->{1}", orderSn, e.getMessage());
                }
            });
        }
        XxlJobLogger.log("=========检查即将过期订单结束===========");
        return SUCCESS;
    }

    @CollectActionEvent(collectType = CollectActionEventType.EXPIRE_REMIND_ORDER, refId = "#orderSn")
    public void expireRemindOrder(String orderSn) {
    }

    public ExpireRemindOtcOrderHandler getService() {
        return SpringContextUtil.getBean(ExpireRemindOtcOrderHandler.class);
    }
}
