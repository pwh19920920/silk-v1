package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.controller.vo.TradePlateTotalVo;
import com.spark.bitrade.entity.TradePlate;
import com.spark.bitrade.entity.TradePlateItem;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.util.MessageRespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

/**
 *  盘口
 *
 * @author young
 * @time 2019.12.10 09:40
 */

@Slf4j
@RestController
public class PlateController extends CommonController {
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private RestTemplate restTemplate;

    final String URL_PREFIX = "api/v1/";


    /**
     * 盘口数据（计算累积数量）
     *
     * @param symbol   必填，交易对
     * @param pageSize 选填，盘口数量
     * @return
     */
    @RequestMapping("exchange-plate")
    public Map<String, List<TradePlateItem>> findTradePlate(String symbol, Integer pageSize) {
        if (null != symbol) {
            symbol = symbol.toUpperCase();
        }
        CoinProcessor coinProcessor = getCoinProcessor(symbol);
        if (coinProcessor != null) {
            Map<String, List<TradePlateItem>> result = new HashMap<>();
            TradePlate buyTradePlate = coinProcessor.getTradePlate(ExchangeOrderDirection.BUY);
            TradePlate sellTradePlate = coinProcessor.getTradePlate(ExchangeOrderDirection.SELL);

            if (buyTradePlate != null) {
                result.put("bid", getTradePlateItems(buyTradePlate, pageSize));
            }
            if (sellTradePlate != null) {
                result.put("ask", getTradePlateItems(sellTradePlate, pageSize));
            }
            return result;
        } else {
            return null;
        }
    }


    /**
     * mini盘口数据（不计算累积数量）
     *
     * @param symbol   必填，交易对
     * @param pageSize 选填，盘口数量(默认为10条)
     * @return
     */
    @RequestMapping("exchange-plate-mini")
    public Map<String, JSONObject> findTradePlateMini(String symbol, Integer pageSize) {
        if (pageSize == null) {
            pageSize = 10;
        }

        return this.findTradePlateFull(symbol, pageSize);
    }

    /**
     * 盘口数据（不计算累积数量）
     *
     * @param symbol   必填，交易对
     * @param pageSize 选填，盘口数量
     * @return
     */
    @RequestMapping("exchange-plate-full")
    public Map<String, JSONObject> findTradePlateFull(String symbol, Integer pageSize) {
        if (null != symbol) {
            symbol = symbol.toUpperCase();
        }

        CoinProcessor coinProcessor = getCoinProcessor(symbol);
        if (coinProcessor != null) {
            Map<String, JSONObject> result = new HashMap<>(16);
            TradePlate buyTradePlate = coinProcessor.getTradePlate(ExchangeOrderDirection.BUY);
            TradePlate sellTradePlate = coinProcessor.getTradePlate(ExchangeOrderDirection.SELL);

            if (buyTradePlate != null) {
                if (null != pageSize) {
                    result.put("bid", buyTradePlate.toJSON(pageSize));
                } else {
                    result.put("bid", buyTradePlate.toJSON());
                }
            }
            if (sellTradePlate != null) {
                if (null != pageSize) {
                    result.put("ask", sellTradePlate.toJSON(pageSize));
                } else {
                    result.put("ask", sellTradePlate.toJSON());
                }
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * 盘口累积数量
     *
     * @param symbol 必填，交易对
     * @return
     */
    @RequestMapping(URL_PREFIX + "exchange-plate-total")
    public MessageRespResult<TradePlateTotalVo> tradePlateTotal(@RequestParam("symbol") String symbol) {
        TradePlateTotalVo vo = new TradePlateTotalVo();

        CoinProcessor coinProcessor = getCoinProcessor(this.toUpperCase(symbol));
        if (Objects.nonNull(coinProcessor)) {
            TradePlate buyTradePlate = coinProcessor.getTradePlate(ExchangeOrderDirection.BUY);
            TradePlate sellTradePlate = coinProcessor.getTradePlate(ExchangeOrderDirection.SELL);

            if (Objects.nonNull(buyTradePlate)) {
                vo.setBidTotal(this.getTradePlateTotal(buyTradePlate));
            }
            if (Objects.nonNull(sellTradePlate)) {
                vo.setAskTotal(this.getTradePlateTotal(sellTradePlate));
            }
        }

        return MessageRespResult.success4Data(vo);
    }

    /**
     * 买1盘口数据
     *
     * @param symbol 交易对，可忽略大小写。eg：BTC/USDT
     * @return
     */
    @RequestMapping(URL_PREFIX + "exchange-plate/buy1")
    public MessageRespResult<TradePlateItem> tradePlateBuy1(@RequestParam("symbol") String symbol) {
        return this.getFirstTradePlateItem(ExchangeOrderDirection.BUY, symbol);
    }

    /**
     * 卖1盘口数据
     *
     * @param symbol 交易对，可忽略大小写。eg：BTC/USDT
     * @return
     */
    @RequestMapping(URL_PREFIX + "exchange-plate/sell1")
    public MessageRespResult<TradePlateItem> tradePlateSell1(@RequestParam("symbol") String symbol) {
        return this.getFirstTradePlateItem(ExchangeOrderDirection.SELL, symbol);
    }


    /**
     * 从exchange实时获取盘口信息并初始化到market模块的缓存中
     *
     * @param symbol  
     * @author yangch
     * @time 2018.06.29 15:25 
     */
    private CoinProcessor getCoinProcessor(String symbol) {
        Optional<CoinProcessor> optional = this.getCoinProcessor(coinProcessorFactory, restTemplate, symbol);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    private LinkedList<TradePlateItem> getTradePlateItems(TradePlate tradePlate, Integer pageSize) {
        LinkedList<TradePlateItem> items;

        //获取指定数量的盘口信息
        if (pageSize != null && pageSize > 0) {
            items = tradePlate.getItems(pageSize);
        } else {
            items = tradePlate.getItems();
        }

        // 计算累计数量
        if (!StringUtils.isEmpty(items)) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (int i = 0, length = items.size(); i < length; i++) {
                TradePlateItem item = items.get(i);
                totalAmount = totalAmount.add(item.getAmount());
                item.setTotalAmount(totalAmount);
            }
        }
        return items;
    }

    /**
     * 盘口累计数量
     *
     * @param buyTradePlate
     * @return
     */
    private BigDecimal getTradePlateTotal(TradePlate buyTradePlate) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0, length = buyTradePlate.getItems().size(); i < length; i++) {
            totalAmount = totalAmount.add(buyTradePlate.getItems().get(i).getAmount());
        }
        return totalAmount;
    }

    /**
     * 获取盘口的买卖1数据
     *
     * @param direction
     * @param symbol
     * @return
     */
    private MessageRespResult<TradePlateItem> getFirstTradePlateItem(ExchangeOrderDirection direction, String symbol) {
        CoinProcessor coinProcessor = getCoinProcessor(this.toUpperCase(symbol));
        if (Objects.nonNull(coinProcessor)) {
            TradePlate tradePlate = coinProcessor.getTradePlate(direction);
            if (tradePlate.getItems().size() > 0) {
                return MessageRespResult.success4Data(tradePlate.getItems().getFirst());
            }
        }

        return MessageRespResult.success4Data(null);
    }
}
