package com.spark.bitrade.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aqmd.netty.annotation.HawkBean;
import com.aqmd.netty.annotation.HawkMethod;

import com.aqmd.netty.push.HawkPushServiceApi;
import com.spark.bitrade.constant.NettyCommand;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.netty.NettyCacheUtilsDK;
import com.spark.bitrade.netty.QuoteMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 处理Netty订阅与取消订阅
 */
@HawkBean
@Slf4j
public class NettyHandler implements MarketHandler {
    @Autowired
    private HawkPushServiceApi hawkPushService;

    private String topicOfSymbol = "SYMBOL_THUMB";

    //add by yangch 时间： 2018.04.24 原因：合并新增
    public void subscribeTopic(Channel channel, String topic) {
        String userKey = channel.id().asLongText();
        if (!NettyCacheUtilsDK.keyChannelCache.containsKey(channel)) {
            NettyCacheUtilsDK.keyChannelCache.put(channel, userKey);
        }
        NettyCacheUtilsDK.storeChannel(topic, channel);
        if (NettyCacheUtilsDK.userKey.containsKey(userKey)) {
            NettyCacheUtilsDK.userKey.get(userKey).add(topic);
        } else {
            //Set<String> userkeys=new HashSet<>();
            Set<String> userkeys = Collections.synchronizedSet(new HashSet()); //修改为线程安全
            userkeys.add(topic);
            NettyCacheUtilsDK.userKey.put(userKey, userkeys);
        }
    }

    //add by yangch 时间： 2018.04.24 原因：合并新增
    public void unsubscribeTopic(Channel channel, String topic) {
        String userKey = channel.id().asLongText();
        if (NettyCacheUtilsDK.userKey.containsKey(userKey)) {
            //edit by yangch 时间： 2018.05.25 原因：删除时加锁
            /*Set<String> sets= NettyCacheUtilsDK.userKey.get(userKey);
            synchronized (sets){
                sets.remove(topic);
            }*/
            NettyCacheUtilsDK.userKey.get(userKey).remove(topic);
        }
        //edit by yangch 时间： 2018.05.25 原因：删除时加锁
        /*synchronized (NettyCacheUtilsDK.keyChannelCache) {
            NettyCacheUtilsDK.keyChannelCache.remove(channel);
        }*/
        NettyCacheUtilsDK.keyChannelCache.remove(channel);
    }

    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_SYMBOL_THUMB, version = NettyCommand.COMMANDS_VERSION)
    public QuoteMessage.SimpleResponse subscribeSymbolThumb(byte[] body, ChannelHandlerContext ctx) {
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        try {
            //add by yangch 时间： 2018.04.24 原因：合并新增
            subscribeTopic(ctx.channel(), topicOfSymbol);
            response.setCode(0).setMessage("订阅成功");

            //edit by yangch 时间： 2018.04.24 原因：代码冲突，临时屏蔽
        /*String key = ctx.channel().id().asLongText();
        if(!NettyCacheUtilsDK.keyChannelCache.containsKey(ctx.channel())){
            NettyCacheUtilsDK.storeChannel(topicOfSymbol,ctx.channel());
            NettyCacheUtilsDK.keyChannelCache.put(ctx.channel(),key);
            response.setCode(0).setMessage("订阅成功");
        }
        else{
            response.setCode(500).setMessage("请不要重复订阅");
        }*/
        } catch (Exception e) {
            response.setCode(500).setMessage("订阅失败");
            log.warn(e.getLocalizedMessage());
        }
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_SYMBOL_THUMB)
    public QuoteMessage.SimpleResponse unsubscribeSymbolThumb(byte[] body, ChannelHandlerContext ctx) {
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        try {
            //add by yangch 时间： 2018.04.24 原因：合并新增
            unsubscribeTopic(ctx.channel(), topicOfSymbol);
            response.setCode(0).setMessage("取消成功");

            //edit by yangch 时间： 2018.04.24 原因：代码冲突，临时屏蔽
        /*if(NettyCacheUtilsDK.keyChannelCache.containsKey(ctx.channel())){
            NettyCacheUtilsDK.keyChannelCache.remove(ctx.channel());
            NettyCacheUtilsDK.getChannel(topicOfSymbol).remove(ctx.channel());
            response.setCode(0).setMessage("取消订阅成功");
        }
        else{
            response.setCode(500).setMessage("您没有订阅");
        }*/
        } catch (Exception e) {
            response.setCode(500).setMessage("取消失败");
            log.warn(e.getLocalizedMessage());
        }
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_EXCHANGE)
    public QuoteMessage.SimpleResponse subscribeExchange(byte[] body, ChannelHandlerContext ctx) {
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        try {
            JSONObject json = JSON.parseObject(new String(body));
            //System.out.println("订阅Exchange："+json.toJSONString());
            log.info("订阅Exchange：{}", json.toJSONString());
            String symbol = json.getString("symbol");
            //edit by yangch 时间： 2018.04.24 原因：代码冲突，临时屏蔽
            //NettyCacheUtilsDK.storeChannel(symbol,ctx.channel());

            //add by yangch 时间： 2018.04.24 原因：合并新增
            String uid = json.getString("uid");
            if (StringUtils.isNotEmpty(uid)) {
                subscribeTopic(ctx.channel(), symbol + "-" + uid);
            }
            subscribeTopic(ctx.channel(), symbol);
            response.setCode(0).setMessage("订阅成功");
        } catch (Exception e) {
            response.setCode(500).setMessage("订阅失败");
            log.warn(e.getLocalizedMessage());
        }

        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_EXCHANGE)
    public QuoteMessage.SimpleResponse unsubscribeExchange(byte[] body, ChannelHandlerContext ctx) {
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        try {
            JSONObject json = JSON.parseObject(new String(body));
            //System.out.println("取消订阅Exchange："+json.toJSONString());
            log.info("取消订阅Exchange：{}", json.toJSONString());
            String symbol = json.getString("symbol");
            //edit by yangch 时间： 2018.04.24 原因：代码冲突，临时屏蔽
            //NettyCacheUtilsDK.getChannel(symbol).remove(ctx.channel());

            //add by yangch 时间： 2018.04.24 原因：合并新增
            String uid = json.getString("uid");
            if (StringUtils.isNotEmpty(uid)) {
                unsubscribeTopic(ctx.channel(), symbol + "-" + uid);
            }
            unsubscribeTopic(ctx.channel(), symbol);
            response.setCode(0).setMessage("取消订阅成功");
        } catch (Exception e) {
            response.setCode(500).setMessage("取消失败");
            log.warn(e.getLocalizedMessage());
        }
        return response.build();
    }

    @Override
    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        handleExchangeTrade(symbol, exchangeTrade);
        handleThumb(symbol, thumb);

//        try {
//            //System.out.println("推送Thumb:"+JSON.toJSONString(thumb)+",size="+NettyCacheUtilsDK.getChannel(symbol));
//            log.debug("推送Thumb:{},size={}", JSON.toJSONString(thumb), NettyCacheUtilsDK.getChannel(symbol));
//            byte[] body = JSON.toJSONString(thumb).getBytes();
//            //edit by yangch 时间： 2018.05.05 原因：代码合并
//            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(symbol),NettyCommand.PUSH_SYMBOL_THUMB, body);
//            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(topicOfSymbol), NettyCommand.PUSH_SYMBOL_THUMB, body);
//            pushMsg(NettyCacheUtilsDK.getChannel(topicOfSymbol), NettyCommand.PUSH_SYMBOL_THUMB, body);
//
//            //System.out.println("推送Trade:"+JSON.toJSONString(exchangeTrade));
//            log.debug("推送Trade:{}", JSON.toJSONString(exchangeTrade));
//            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_TRADE, JSONObject.toJSONString(exchangeTrade).getBytes());
//            pushMsg(NettyCacheUtilsDK.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_TRADE, JSONObject.toJSONString(exchangeTrade).getBytes());
//        }catch (Exception e){
//            log.warn(e.getLocalizedMessage());
//        }
    }

    public void handleExchangeTrade(String symbol, ExchangeTrade exchangeTrade) {
        try {
            //System.out.println("推送Trade:"+JSON.toJSONString(exchangeTrade));
            log.debug("推送Trade:{}", JSON.toJSONString(exchangeTrade));
            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_TRADE, JSONObject.toJSONString(exchangeTrade).getBytes());
            pushMsg(NettyCacheUtilsDK.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_TRADE, JSONObject.toJSONString(exchangeTrade).getBytes());
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    public void handleThumb(String symbol, CoinThumb thumb) {
        try {
            //System.out.println("推送Thumb:"+JSON.toJSONString(thumb)+",size="+NettyCacheUtilsDK.getChannel(symbol));
            log.debug("推送Thumb:{},size={}", JSON.toJSONString(thumb), NettyCacheUtilsDK.getChannel(symbol));
            byte[] body = JSON.toJSONString(thumb).getBytes();
            //edit by yangch 时间： 2018.05.05 原因：代码合并
            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(symbol),NettyCommand.PUSH_SYMBOL_THUMB, body);
            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(topicOfSymbol), NettyCommand.PUSH_SYMBOL_THUMB, body);
            pushMsg(NettyCacheUtilsDK.getChannel(topicOfSymbol), NettyCommand.PUSH_SYMBOL_THUMB, body);
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        try {
            //log.info("symbol={},kline={}",symbol,kLine);
            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_KLINE, JSONObject.toJSONString(kLine).getBytes());
            pushMsg(NettyCacheUtilsDK.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_KLINE, JSONObject.toJSONString(kLine).getBytes());
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    public void handlePlate(String symbol, TradePlate plate) {
        try {
            //System.out.println("推送盘口:"+JSON.toJSONString(plate));
            log.debug("推送盘口:{}", JSON.toJSONString(plate));
            //edit by yangch 时间： 2018.04.24 原因：合并前的代码
            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(symbol),NettyCommand.PUSH_EXCHANGE_PLATE, JSONObject.toJSONString(plate).getBytes());
            //add by yangch 时间： 2018.04.24 原因：合并时添加的代码
            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_PLATE, plate.toJSON(10).toJSONString().getBytes());
            pushMsg(NettyCacheUtilsDK.getChannel(symbol), NettyCommand.PUSH_EXCHANGE_PLATE, plate.toJSON(20).toJSONString().getBytes());
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    //edit by yangch 时间： 2018.05.10 原因：合并代码
    //add by yangch 时间： 2018.04.24 原因：合并新增
    /*public void handleOrder(short command, ExchangeOrder order){
        String topic = order.getSymbol()+"-"+order.getMemberId();
        System.out.println("推送订单:"+JSON.toJSONString(order));
        hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(topic),command,JSON.toJSONString(order).getBytes());
    }*/
    public void handleOrder(short command, ExchangeOrder order) {
        try {
            String topic = order.getSymbol() + "-" + order.getMemberId();
            //System.out.println("推送订单:" + JSON.toJSONString(order));
            log.debug("推送订单:{}", JSON.toJSONString(order));

            //hawkPushService.pushMsg(NettyCacheUtilsDK.getChannel(topic), command, JSON.toJSONString(order).getBytes());
            pushMsg(NettyCacheUtilsDK.getChannel(topic), command, JSON.toJSONString(order).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("推送出错:{}", JSON.toJSONString(order));
            //System.out.println("推送出错");
        }
    }


    /**
     *  * 简单封装消息推送的方法，引用消息推送方法
     *  * @author yangch
     *  * @time 2018.05.25 10:56 
     *
     * @param channels
     * @param cmd
     * @param msg       
     */
    public void pushMsg(Set<Channel> channels, short cmd, byte[] msg) {
        if (channels == null) {
            return;
        }
        synchronized (channels) {
            hawkPushService.pushMsg(channels, cmd, msg);
        }
    }
}
