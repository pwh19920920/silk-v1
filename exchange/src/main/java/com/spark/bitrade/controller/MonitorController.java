package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.trader.CoinTrader;
import com.spark.bitrade.trader.CoinTraderFactory;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderType;
import com.spark.bitrade.constant.KafkaTopicConstant;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/monitor")
public class MonitorController {
    @Autowired
    private CoinTraderFactory factory;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ExchangeCoinService exchangeCoinService;


    @RequestMapping("trader-overview")
    public JSONObject traderOverview(String symbol) {
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }
        JSONObject result = new JSONObject();
        //卖盘信息
        JSONObject ask = new JSONObject();
        //买盘信息
        JSONObject bid = new JSONObject();
        ask.put("limit_price_order_count", trader.getLimitPriceOrderCount(ExchangeOrderDirection.SELL));
        ask.put("market_price_order_count", trader.getSellMarketQueue().size());
        ask.put("depth", trader.getTradePlate(ExchangeOrderDirection.SELL).currentDepth());
        bid.put("limit_price_order_count", trader.getLimitPriceOrderCount(ExchangeOrderDirection.BUY));
        bid.put("market_price_order_count", trader.getBuyMarketQueue().size());
        bid.put("depth", trader.getTradePlate(ExchangeOrderDirection.BUY).currentDepth());
        result.put("ask", ask);
        result.put("bid", bid);
        return result;
    }

    /**
     * 盘口订单明细
     * http://127.0.0.1:6005/extrade/monitor/trader-detail?symbol=SLU/CNYT
     *
     * @param symbol
     * @return
     */
    @RequestMapping("trader-detail")
    public JSONObject traderDetail(String symbol) {
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }
        JSONObject result = new JSONObject();
        //卖盘信息
        JSONObject ask = new JSONObject();
        //买盘信息
        JSONObject bid = new JSONObject();
        ask.put("limit_price_queue", trader.getSellLimitPriceQueue());
        ask.put("market_price_queue", trader.getSellMarketQueue());
        bid.put("limit_price_queue", trader.getBuyLimitPriceQueue());
        bid.put("market_price_queue", trader.getBuyMarketQueue());
        result.put("ask", ask);
        result.put("bid", bid);
        return result;
    }

    //del by yangch 时间： 2018.06.29 原因：由market模块提供，此处不提供
    @RequestMapping("plate")
    @Deprecated
    public Map<String, List<TradePlateItem>> traderPlate(String symbol) {
        Map<String, List<TradePlateItem>> result = new HashMap<>();
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }
        //edit by yangch 时间： 2018.04.21 原因：添加累计
        LinkedList<TradePlateItem> buyItems = trader.getTradePlate(ExchangeOrderDirection.BUY).getItems();
        if (!StringUtils.isEmpty(buyItems)) {
            BigDecimal buyTotalAmountBuy = BigDecimal.ZERO;
            for (int i = 0, length = buyItems.size(); i < length; i++) {
                TradePlateItem item = buyItems.get(i);
                buyTotalAmountBuy = buyTotalAmountBuy.add(item.getAmount());
                item.setTotalAmount(buyTotalAmountBuy);
            }
        }
        LinkedList<TradePlateItem> sellItems = trader.getTradePlate(ExchangeOrderDirection.SELL).getItems();
        if (!StringUtils.isEmpty(sellItems)) {
            BigDecimal sellTotalAmount = BigDecimal.ZERO;
            for (int i = 0, length = sellItems.size(); i < length; i++) {
                TradePlateItem item = sellItems.get(i);
                sellTotalAmount = sellTotalAmount.add(item.getAmount());
                item.setTotalAmount(sellTotalAmount);
            }
        }
        result.put("bid", buyItems);
        result.put("ask", sellItems);
        //edit by yangch 时间： 2018.04.21 原因：修改前的代码
        //result.put("bid",trader.getTradePlate(ExchangeOrderDirection.BUY).getItems());
        //result.put("ask",trader.getTradePlate(ExchangeOrderDirection.SELL).getItems());

        return result;
    }

    /**
     * http://127.0.0.1:6005/extrade/monitor/plate-mini?symbol=SLU/CNYT
     *
     * @param symbol
     * @return
     */
    @RequestMapping("plate-mini")
    public Map<String, JSONObject> traderPlateMini(String symbol) {
        Map<String, JSONObject> result = new HashMap<>();
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }
        result.put("bid", trader.getTradePlate(ExchangeOrderDirection.BUY).toJSON(10));
        result.put("ask", trader.getTradePlate(ExchangeOrderDirection.SELL).toJSON(10));
        return result;
    }

    //add by yangch 时间： 2018.04.24 原因：合并新增
    @RequestMapping("plate-full")
    public Map<String, JSONObject> traderPlateFull(String symbol) {
        Map<String, JSONObject> result = new HashMap<>();
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }
        result.put("bid", trader.getTradePlate(ExchangeOrderDirection.BUY).toJSON());
        result.put("ask", trader.getTradePlate(ExchangeOrderDirection.SELL).toJSON());
        return result;
    }

    /**
     * 提供交易对的最新盘口数据
     * http://127.0.0.1:6005/extrade/monitor/realTimePlate?symbol=SLU/CNYT&direction=SELL|BUY
     *
     * @param symbol
     * @param direction
     * @return
     */
    @RequestMapping("realTimePlate")
    public TradePlate realTimePlate(String symbol, ExchangeOrderDirection direction) {
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }
        return trader.getTradePlate(direction);
    }

    /**
     * 盘口订单明细
     * http://127.0.0.1:6005/extrade/monitor/plate-detail?symbol=SLU/CNYT&direction=SELL
     *
     * @param symbol    必填
     * @param direction 可选
     * @return
     */
    @RequestMapping("plate-detail")
    public JSONObject plateDetail(String symbol, ExchangeOrderDirection direction) {
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        if (direction == null || direction == ExchangeOrderDirection.BUY) {
            // 买盘订单明细信息
            JSONObject bid = new JSONObject();
            TreeMap<BigDecimal, MergeOrder> treeMap = trader.getBuyLimitPriceQueue();

            bid.put("limit_price_queue", treeMap);
            bid.put("market_price_queue", trader.getBuyMarketQueue());

            result.put("bid", bid);
        }

        if (direction == null || direction == ExchangeOrderDirection.SELL) {
            // 卖盘订单明细信息
            JSONObject ask = new JSONObject();
            TreeMap<BigDecimal, MergeOrder> treeMap = trader.getSellLimitPriceQueue();

            ask.put("limit_price_queue", treeMap);
            ask.put("market_price_queue", trader.getSellMarketQueue());

            result.put("ask", ask);
        }

        return result;
    }

    /**
     * 维护类接口：重置盘口数据（注意，重置盘口数据的时候会暂停交易，即下单后会被取消订单）
     * http://127.0.0.1:6005/extrade/monitor/plate-reset?symbol=SLU/CNYT&direction=SELL/BUY
     *  @author yangch
     *  @time 2018.10.29 15:03 
     *
     * @param symbol    交易对，必填
     * @param direction 购买方向，可选。
     * @return
     */
    @RequestMapping("plate-reset")
    public String plateReset(String symbol, ExchangeOrderDirection direction) {
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }

        //重置盘口数据
        trader.resetTradePlate(direction);

        return "reset ok";
    }

    /**
     * 维护类接口：撤销指定撮合器中所有订单
     * http://127.0.0.1:6005/extrade/monitor/cancelAllOrder?symbol=SLU/CNYT
     * <p>
     *  @author yangch
     *  @time 2018.10.29 15:03 
     *
     * @param symbol 交易对，必填
     * @return
     */
    @RequestMapping("cancelAllOrder")
    public String cancelAllOrder(String symbol) {
        CoinTrader trader = factory.getTrader(symbol);
        if (trader == null) {
            return null;
        }

        trader.cancelAllOrder();    //撤销所有订单
        return "cancel all order ok";
    }

    /**
     * 维护类接口：停止撮合器
     * http://127.0.0.1:6005/extrade/monitor/stopTrader?symbol=SLU/CNYT
     *  @author yangch
     *  @time 2018.10.29 15:03 
     *
     * @param symbol 交易对，必填
     * @return
     */
    @RequestMapping("stopTrader")
    public String stopTrader(String symbol) {
        factory.offlineTrader(symbol);

        //发送一个空的订单触发退出任务
        ExchangeOrder exchangeOrder = new ExchangeOrder();
        kafkaTemplate.send("exchange-order", symbol, JSON.toJSONString(exchangeOrder));

        return "ok，已发送撮合器停止命令，关注“exit consumerOrder:”日志是否已退出（待任务执行完则退出）。";
    }

    @RequestMapping("symbols")
    public List<String> symbols() {
        HashMap<String, CoinTrader> traders = factory.getTraderMap();
        List<String> symbols = new ArrayList<>();
        traders.forEach((key, trader) -> {
            symbols.add(key);
        });
        return symbols;
    }

    /**
     * 查找订单
     *
     * @param symbol
     * @param orderId
     * @param direction
     * @param type
     * @return
     */
    @RequestMapping("order")
    public ExchangeOrder findOrder(String symbol, String orderId,
                                   ExchangeOrderDirection direction, ExchangeOrderType type) {
        CoinTrader trader = factory.getTrader(symbol);
        return trader.findOrder(orderId, type, direction);
    }

    /**
     * 获取交易队撮合器的状态
     */
    @RequestMapping("traderStatus")
    public Map<String, String> traderStatus(@RequestParam(value = "symbol", required = false) String symbol) {
        Map<String, String> map = new HashMap<>();
        if (null != symbol) {
            CoinTrader trader = factory.getTrader(symbol.toUpperCase());
            if (null != trader) {
                map.put(symbol, trader.isTradingHalt() ? "suspend" : "running");
            } else {
                map.put(symbol, "--");
            }
        } else {
            Set<Map.Entry<String, CoinTrader>> entrySet = factory.getTraderMap().entrySet();
            for (Map.Entry<String, CoinTrader> entry : entrySet) {
                map.put(entry.getKey(), entry.getValue().isTradingHalt() ? "suspend" : "running");
            }
        }
        return map;
    }

    /**
     * 重置交易撮合器，根据ExchangeCoin的启用状态进行相应的启动或者暂停交易对
     * /extrade/monitor/resetTrader?symbol=
     *
     * @param symbol
     * @return
     */
    @RequestMapping("resetTrader")
    public MessageResult resetTrader(String symbol) {
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol.toUpperCase());
        if (null != exchangeCoin) {
            kafkaTemplate.send(KafkaTopicConstant.exchangeTraderManager,
                    exchangeCoin.getSymbol(), JSON.toJSONString(exchangeCoin));
        }

        return MessageResult.success();
    }
}
