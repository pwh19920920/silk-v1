package com.spark.bitrade.service;

import com.spark.bitrade.constant.NettyCommand;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.handler.MarketHandler;
import com.spark.bitrade.handler.MongoMarketHandler;
import com.spark.bitrade.handler.NettyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 推送交易信息
 *
 * @author yangch
 * @time 2018.06.30 15:22   
 */

@Service
@Slf4j
public class PushTradeMessage {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NettyHandler nettyHandler;

    /**
     * socket方式异步推送部分成交订单信息
     *
     * @param order  
     * @author yangch
     * @time 2018.06.30 15:28 
     */
    @Async
    public void pushOrderTrade4Socket(ExchangeOrder order) {
        ///log.info("/topic/market/order-trade/{}/{}", order.getSymbol() , order.getMemberId());
        messagingTemplate.convertAndSend("/topic/market/order-trade/" + order.getSymbol() + "/" + order.getMemberId(), order);
    }

    /**
     *  netty方式异步推送部分成交订单信息
     *  @author yangch
     *  @time 2018.06.30 15:28 
     *
     * @param order  
     */
    @Async
    public void pushOrderTrade4Netty(ExchangeOrder order) {
        nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_TRADE, order);
    }


    /**
     *  socket方式异步推送成交订单信息
     *  @author yangch
     *  @time 2018.06.30 15:28 
     *
     * @param order  
     */
    @Async
    public void pushOrderCompleted4Socket(ExchangeOrder order) {
        ///log.info("/topic/market/order-completed/{}/{}" ,order.getSymbol() , order.getMemberId());
        messagingTemplate.convertAndSend("/topic/market/order-completed/" + order.getSymbol() + "/" + order.getMemberId(), order);
    }

    /**
     * netty方式异步推送成交订单信息
     *
     * @param order  
     * @author yangch
     * @time 2018.06.30 15:28 
     */
    @Async
    public void pushOrderCompleted4Netty(ExchangeOrder order) {
        nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_COMPLETED, order);
    }


    /**
     *  socket方式异步推送最新的成交信息
     *  @author yangch
     *  @time 2018.06.30 15:28
     *
     * @param trades  
     */
    ///@Async
    public void pushNewestTrade4Socket(String symbol, List<ExchangeTrade> trades) {
        ///log.info("/topic/market/trade/{}" , symbol);
        messagingTemplate.convertAndSend("/topic/market/trade/" + symbol, trades);
    }

    /**
     *  Netty方式推送最新的成交信息
     *  @author yangch
     *  @time 2018.06.30 15:28
     *
     * @param trade  
     */
    public void pushNewestTrade4Netty(String symbol, ExchangeTrade trade) {
        nettyHandler.handleExchangeTrade(symbol, trade);
    }

    /**
     *  socket方式异步推送撤单成功信息
     *  @author yangch
     *  @time 2018.06.30 15:28 
     *
     * @param order  
     */
    @Async
    public void pushOrderCanceled4Socket(ExchangeOrder order) {
        ///log.info("/topic/market/order-canceled/{}/{}" ,order.getSymbol() , order.getMemberId());
        messagingTemplate.convertAndSend("/topic/market/order-canceled/" + order.getSymbol() + "/" + order.getMemberId(), order);
    }

    /**
     *  netty方式异步推送撤单成功信息
     *  @author yangch
     *  @time 2018.06.30 15:28 
     *
     * @param order  
     */
    @Async
    public void pushOrderCanceled4Netty(ExchangeOrder order) {
        nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_CANCELED, order);
    }

    /**
     *  socket方式推送盘口信息
     *  @author yangch
     *  @time 2018.07.03 15:01 
     *
     * @param plateMini 盘口信息
     *                   
     */
    public void pushTradePlate4Socket(TradePlate plateMini) {
        ///log.info("/topic/market/trade-plate/{}" , plateMini.getSymbol());
        messagingTemplate.convertAndSend("/topic/market/trade-plate/" + plateMini.getSymbol(), plateMini);
    }

    /**
     *  socket方式推送盘口信息
     *  @author yangch
     *  @time 2018.07.03 15:01 
     *
     * @param plateFull 盘口信息
     *                   
     */
    public void pushTradePlateFull4Socket(TradePlate plateFull) {
        ///log.info("/topic/market/trade-plate-full/{}" , plateFull.getSymbol());
        messagingTemplate.convertAndSend("/topic/market/trade-plate-full/" + plateFull.getSymbol(), plateFull);
    }

    /**
     * netty方式推送盘口信息
     *
     * @param plateMini 盘口信息
     *                   
     * @author yangch
     * @time 2018.07.03 15:01 
     */
    public void pushTradePlate4Netty(TradePlate plateMini) {
        nettyHandler.handlePlate(plateMini.getSymbol(), plateMini);
    }


    /**
     * socket方式推送缩略行情
     *
     * @param thumb 缩略行情
     *               
     * @author yangch
     * @time 2018.07.03 15:01 
     */
    public void pushCoinThumb4Socket(CoinThumb thumb) {
        ///log.info("/topic/market/thumb");
        messagingTemplate.convertAndSend("/topic/market/thumb", thumb);
    }

    /**
     * Netty方式推送缩略行情
     *
     * @param thumb 缩略行情
     *               
     * @author yangch
     * @time 2018.07.03 15:01 
     */
    public void pushCoinThumb4Netty(CoinThumb thumb) {
        ///log.info("/topic/market/thumb");
        nettyHandler.handleThumb(thumb.getSymbol(), thumb);
    }

    /**
     * socket方式推送k线数据
     *
     * @param symbol 交易对名称
     * @param kLine  k线数据
     */
    public void pushKLine4Socket(String symbol, KLine kLine) {
        ///log.info("/topic/market/kline/{}",symbol);
        //推送K线数据
        messagingTemplate.convertAndSend("/topic/market/kline/" + symbol, kLine);
    }

    /**
     * Netty方式推送k线数据
     *
     * @param symbol 交易对名称
     * @param kLine  k线数据
     */
    public void pushKLine4Netty(String symbol, KLine kLine) {
        //推送K线数据
        nettyHandler.handleKLine(symbol, kLine);
    }

    /**
     *  socket方式异步推送钱包同步成功的消息
     *  @author yangch
     *  @time 2019-12-19 18:49:46
     *
     * @param coinSymbl 币种，如BTC、USDT
     * @param memberId  用户ID
     */
    @Async
    public void handlePushWalletSyncSucceed(String memberId, String coinSymbl) {
        ///log.info("/topic/wallet/change/{}/{}" ,order.getSymbol() , order.getMemberId());
        messagingTemplate.convertAndSend("/topic/wallet/change/" + memberId, coinSymbl);
    }


    /**
     * 推送k线
     *
     * @param handlers
     * @param kLine     
     * @author yangch
     * @time 2018.07.03 16:01 
     */
    public void pushKLine(List<MarketHandler> handlers, KLine kLine) {
        for (MarketHandler storage : handlers) {
            if (!(storage instanceof MongoMarketHandler)) {
                storage.handleKLine(kLine.getSymbol(), kLine);
            }
        }
    }

}
