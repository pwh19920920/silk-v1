package com.spark.bitrade.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * 盘口信息
 *
 * @author young
 */
@Data
@Slf4j
public class TradePlate {
    private LinkedList<TradePlateItem> items;
    /**
     * 最大深度
     */
    private int maxDepth = 100;
    /**
     * 盘口方向
     */
    private ExchangeOrderDirection direction;

    /**
     * 交易对
     */
    private String symbol;

    public TradePlate() {
    }

    public TradePlate(ExchangeOrderDirection direction) {
        this.direction = direction;
        items = new LinkedList<>();
    }

    public TradePlate(String symbol, ExchangeOrderDirection direction) {
        this.direction = direction;
        this.symbol = symbol;
        items = new LinkedList<>();
    }

    /**
     * 添加盘口
     *
     * @param exchangeOrder
     * @return
     */
    public boolean add(ExchangeOrder exchangeOrder) {
        log.debug("add TradePlate order={}", exchangeOrder);
        if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE) {
            return false;
        }
        if (exchangeOrder.getDirection() != direction) {
            return false;
        }
        synchronized (items) {
            int index = 0;
            if (items.size() > 0) {
                for (index = 0; index < items.size(); index++) {
                    TradePlateItem item = items.get(index);
                    if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY
                            && item.getPrice().compareTo(exchangeOrder.getPrice()) > 0) {
                        continue;
                    }
                    if (exchangeOrder.getDirection() == ExchangeOrderDirection.SELL
                            && item.getPrice().compareTo(exchangeOrder.getPrice()) < 0) {
                        continue;
                    }

                    if (item.getPrice().compareTo(exchangeOrder.getPrice()) == 0) {
                        BigDecimal deltaAmount = exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount());
                        item.setAmount(item.getAmount().add(deltaAmount));
                        //添加到已有队列中，并返回
                        return true;
                    } else {
                        break;
                    }
                }
            }

            //edit by yangch 时间： 2018.10.29 原因： 新增，此处的深度限制会带来交易对买卖队列和盘口数据的不一致问题
            ///if(index < maxDepth) {
            TradePlateItem newItem = new TradePlateItem();
            newItem.setAmount(exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount()));
            newItem.setPrice(exchangeOrder.getPrice());
            items.add(index, newItem);
            ///}
        }
        return true;
    }

    public void remove(ExchangeOrder order, BigDecimal amount, boolean removeItemFlag) {
        log.debug("remove TradePlate order={}", order);
        synchronized (items) {
            for (int index = 0; index < items.size(); index++) {
                TradePlateItem item = items.get(index);
                if (item.getPrice().compareTo(order.getPrice()) == 0) {
                    item.setAmount(item.getAmount().subtract(amount));
                    if (removeItemFlag
                            || item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        items.remove(index);
                    }
                    break;
                    //return;
                }
            }
        }
    }

    /**
     * 按价格档位移除盘口
     *
     * @param price
     * @return
     */
    public boolean removeItem(BigDecimal price) {
        log.debug("remove TradePlate price={}", price);
        synchronized (items) {
            for (int index = 0; index < items.size(); index++) {
                TradePlateItem item = items.get(index);
                if (item.getPrice().compareTo(price) == 0) {
                    items.remove(index);
                    log.info("remove TradePlateItem ,{}", item);
                    return true;
                }
            }
        }
        return false;
    }

    public void remove(ExchangeOrder order, boolean removeItemFlag) {
        remove(order, order.getAmount().subtract(order.getTradedAmount()), removeItemFlag);
    }

    public void setItems(LinkedList<TradePlateItem> items) {
        this.items = items;
    }

    /**
     * 获取盘口中高价价格
     *
     * @return
     */
    public BigDecimal highestPrice() {
        if (items.size() == 0) {
            return BigDecimal.ZERO;
        }
        if (direction == ExchangeOrderDirection.BUY) {
            return items.getFirst().getPrice();
        } else {
            return items.getLast().getPrice();
        }
    }

    /**
     * 获取盘口中最低价格
     *
     * @return
     */
    public BigDecimal lowestPrice() {
        if (items.size() == 0) {
            return BigDecimal.ZERO;
        }
        if (direction == ExchangeOrderDirection.BUY) {
            return items.getLast().getPrice();
        } else {
            return items.getFirst().getPrice();
        }
    }

    /**
     * 获取盘口中最大档位数量
     *
     * @return
     */
    public BigDecimal maxAmount() {
        if (items.size() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = BigDecimal.ZERO;
        for (TradePlateItem item : items) {
            if (item.getAmount().compareTo(amount) > 0) {
                amount = item.getAmount();
            }
        }
        return amount;
    }

    /**
     * 获取委托量最小档位数量
     *
     * @return
     */
    public BigDecimal minAmount() {
        if (items.size() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = items.getFirst().getAmount();
        for (TradePlateItem item : items) {
            if (item.getAmount().compareTo(amount) < 0) {
                amount = item.getAmount();
            }
        }
        return amount;
    }

    /**
     * 当前深度
     *
     * @return
     */
    public int currentDepth() {
        return items.size();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    public JSONObject toJSON() {
        return toJSON(0);
    }

    public JSONObject toJSON(int limit) {
        synchronized (items) {
            JSONObject json = new JSONObject();
            json.put("direction", direction);
            json.put("maxAmount", maxAmount());
            json.put("minAmount", minAmount());
            json.put("highestPrice", highestPrice());
            json.put("lowestPrice", lowestPrice());
            json.put("symbol", getSymbol());

            if (limit > 0) {
                json.put("items", items.size() > limit ? items.subList(0, limit) : items);
            } else {
                json.put("items", items);
            }

            return json;
        }
    }

    /**
     * 将盘口数据转换为json字符串
     *
     * @return
     */
    public String toJSONString() {
        synchronized (items) {
            return JSON.toJSONString(this);
        }
    }

    /**
     * 获取盘口数据
     *
     * @param limit 盘口大小
     * @return
     */
    public LinkedList<TradePlateItem> getItems(int limit) {
        synchronized (items) {
            return items.size() > limit ? new LinkedList<>(items.subList(0, limit)) : items;
        }
    }
}
