package com.spark.bitrade.controller;
import com.spark.bitrade.entity.MessageTypeEnum;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import com.spark.bitrade.handler.MessageHandler;
import com.spark.bitrade.handler.NettyHandler;
import com.spark.bitrade.notice.IWaitAckTaskService;
import com.spark.bitrade.notice.task.ChatWaitAckTask;
import com.spark.bitrade.notice.task.OtcEnventWaitAckTask;
import com.spark.bitrade.service.optfor.*;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/***
 * 
 * @author yangch
 * @time 2018.05.26 17:08
 */

@RestController
public class TestController {

    @Autowired
    private RedisKeyService redisKeyService;
    @Autowired
    private RedisStringService redisStringService;
    @Autowired
    private RedisHashService redisHashService;
    @Autowired
    private IWaitAckTaskService waitAckTaskService;
    @Autowired
    private RedisListService redisListService;
    @Autowired
    private RedisZSetService redisZSetService;

    @Autowired
    private NettyHandler nettyHandler;
    @Autowired
    private MessageHandler chatMessageHandler ;


    int i =0 ;

    /***
      * 负载均衡健康检查接口
      * @author yangch
      * @time 2018.05.26 17:04 
      */
    @RequestMapping("test")
    public MessageResult test(){
        redisStringService.set("test:yangch:key",MessageResult.success("测试看看啦看电视了11111").toString());
        redisStringService.set("test:yangch:key2", 123);

        redisKeyService.expire("test:yangch:key", 1000, TimeUnit.SECONDS);

        if(i==0){
            redisHashService.hPut("test:yangch:71639","enventCnt",0);
        } else {
            redisHashService.hIncrBy("test:yangch:71639", "enventCnt", 1);
        }
        i++;

        redisHashService.hIncrBy("test:yangch:71639","chatCnt",1);

        OrderAddParam addParam = getService().getOrderAddParam(); //spring 缓存测试
        System.out.println("addParam="+addParam);

        /*Map<String, RedisTemplate> map = SpringContextUtil.getApplicationContext().getBeansOfType(RedisTemplate.class);
        if(map == null){
            System.out.println("map is null");
        } else {
            map.forEach((k,v) ->{
                System.out.println("k="+k+",v="+v);
            });
        }*/

        System.out.println("DelayQueue 任务-----------");
        /*DelayQueue<WaitAckTask> queue = new DelayQueue<>();
        queue.add(new WaitAckTask(4000L, TimeUnit.MILLISECONDS, "t4"));
        queue.add(new WaitAckTask(2000L, TimeUnit.MILLISECONDS, "rt2"));
        queue.add(new WaitAckTask(1000L, TimeUnit.MILLISECONDS, "t1"));
        queue.add(new WaitAckTask(3000L, TimeUnit.MILLISECONDS, "rt3"));
        queue.add(new WaitAckTask(5000L, TimeUnit.MILLISECONDS, "t5"));

        queue.removeIf(t->t.getTaskId().startsWith("r"));
        queue.remove(new WaitAckTask(1000L, TimeUnit.MILLISECONDS, "t5"));

        while(!queue.isEmpty()) {
            try {
                WaitAckTask task = queue.take();
                System.out.println(task.getTaskId() + ":" + System.currentTimeMillis());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        /*waitAckTaskService.addTask(new WaitAckTask(4000L, TimeUnit.MILLISECONDS, "t4-"));
        waitAckTaskService.addTask(new WaitAckTask(2000L, TimeUnit.MILLISECONDS, "rt2-"));
        waitAckTaskService.addTask(new WaitAckTask(1000L, TimeUnit.MILLISECONDS, "t1-"));
        waitAckTaskService.addTask(new WaitAckTask(3000L, TimeUnit.MILLISECONDS, "rt3-"));
        waitAckTaskService.addTask(new WaitAckTask(5000L, TimeUnit.MILLISECONDS, "t5-"));
        waitAckTaskService.addTask(new WaitAckTask(1000L, TimeUnit.MILLISECONDS, "t5-"));
        waitAckTaskService.removeTask(new WaitAckTask(3000L, TimeUnit.MILLISECONDS, "rt3-"));*/

//        waitAckTaskService.addTask(new WaitAckTask(4000L, TimeUnit.MILLISECONDS, "t4", null));
//        waitAckTaskService.addTask(new WaitAckTask(2000L, TimeUnit.MILLISECONDS, "rt2", null));
//        waitAckTaskService.addTask(new WaitAckTask(1000L, TimeUnit.MILLISECONDS, "t1", null));
//        waitAckTaskService.addTask(new WaitAckTask(3000L, TimeUnit.MILLISECONDS, "rt3", null));
//        waitAckTaskService.addTask(new ChatWaitAckTask(5000L, TimeUnit.MILLISECONDS, "ct5", null));
//        waitAckTaskService.addTask(new ChatWaitAckTask(5000L, TimeUnit.MILLISECONDS, "ct55", null));
        waitAckTaskService.addTask(new ChatWaitAckTask("ct5", null));
        waitAckTaskService.addTask(new ChatWaitAckTask("ct55", null));
        waitAckTaskService.removeTask(new ChatWaitAckTask("ct55", null));
//        waitAckTaskService.addTask(new OtcEnventWaitAckTask(5000L, TimeUnit.MILLISECONDS, "ot5", null));
//        waitAckTaskService.addTask(new OtcEnventWaitAckTask(5000L, TimeUnit.MILLISECONDS, "ot55", null));
        waitAckTaskService.addTask(new OtcEnventWaitAckTask("ot5", null));
        waitAckTaskService.addTask(new OtcEnventWaitAckTask("ot55", null));
        waitAckTaskService.addTask(new OtcEnventWaitAckTask("ot55", null));
        //waitAckTaskService.addTask(new ChatWaitAckTask(1000L, TimeUnit.MILLISECONDS, "t5", null));
        //waitAckTaskService.removeTask(new ChatWaitAckTask(3000L, TimeUnit.MILLISECONDS, "rt3", null));

        return MessageResult.success();
    }

    @RequestMapping("test2")
    public MessageResult test2() {
        String key = SysConstant.NOTICE_OTC_CHAT_PREFIX+i;
        if(i%2==0){
            key = SysConstant.NOTICE_OTC_CHAT_PREFIX+"0";
        } else {
            key = SysConstant.NOTICE_OTC_CHAT_PREFIX+"1";
        }

        //列表消息缓存保存
        //redisZSetService.zAdd(key, "orderId"+i, System.currentTimeMillis());
        redisZSetService.zRemove(SysConstant.NOTICE_OTC_CHAT_PREFIX+"0", "order00");
        Set set2= redisZSetService.zRange(SysConstant.NOTICE_OTC_CHAT_PREFIX+"3",0, -1);
        System.out.println("set2=="+set2);

        System.out.println("zSize="+redisZSetService.zSize(key));
        System.out.println("zZCard="+redisZSetService.zZCard(key));

        Set set= redisZSetService.zRange(key,0, -1);
        if(set != null){
            set.forEach(s-> System.out.println(s));
        }
        i++;

        return MessageResult.success("测试成功");
    }

    //发送聊天或事件消息
    @RequestMapping("test3")
    public MessageResult test3(String uid,int type) {
        RealTimeChatMessage message = new RealTimeChatMessage();
        message.setContent("content");
        message.setAvatar("avatar");
        message.setOrderId("orderId");
        message.setUidTo(uid);
        message.setUidFrom(uid);
        message.setNameTo("nameTo");
        message.setNameFrom("nameFrom");
        if(type == 0) {
            message.setMessageType(MessageTypeEnum.OTC_EVENT);
        } else {
            message.setMessageType(MessageTypeEnum.NORMAL_CHAT);

        }
        System.out.println(message);
        nettyHandler.handleMessage(message);

        return MessageResult.success("发送成功");
    }

    @RequestMapping("test4")
    public MessageResult test4(String uid,int type){
        RealTimeChatMessage message = new RealTimeChatMessage();
        message.setContent("content");
        message.setAvatar("avatar");
        message.setOrderId("orderId");
        message.setUidTo(uid);
        message.setUidFrom(uid);
        message.setNameTo("nameTo");
        message.setNameFrom("nameFrom");
        if(type == 0) {
            message.setMessageType(MessageTypeEnum.OTC_EVENT);
        } else {
            message.setMessageType(MessageTypeEnum.NORMAL_CHAT);

        }
        chatMessageHandler.handleReadAckMessage(message);

        return MessageResult.success("已读取");
    }

    @Cacheable(cacheNames = "addParam", key = "'entity2:addParam:1' ")
    public OrderAddParam getOrderAddParam(){
        OrderAddParam orderAddParam= new OrderAddParam();
        orderAddParam.setSymbol("test/usd");
        orderAddParam.setPrice(new BigDecimal("234"));
        orderAddParam.setAmount(new BigDecimal("345"));
        orderAddParam.setCustomContent("testdd");
        return orderAddParam;
    }

    //订单下单实体类
    @Data
    public static class OrderAddParam{
        private String symbol;  //交易对
        private BigDecimal price;   //委托价格
        private BigDecimal amount;  //委托数量
        private String customContent; //自定义内容，非必须

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

    public TestController getService(){
        return SpringContextUtil.getBean(TestController.class);
    }
}
