package com.spark.bitrade.trader;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderType;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.MergeOrder;
import com.spark.bitrade.entity.TradePlate;
import com.spark.bitrade.mq.PlateMessageWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.*;

public class CoinTrader {
    private String symbol;
    private KafkaTemplate<String, String> kafkaTemplate;
    private PlateMessageWrapper plateMessageWrapper;

    /**
     * 交易币种的精度
     */
    private int coinScale = 4;
    /**
     * 基币的精度
     */
    private int baseCoinScale = 4;
    private Logger logger = LoggerFactory.getLogger(CoinTrader.class);
    /**
     * 买入限价订单链表，价格从高到低排列
     */
    private TreeMap<BigDecimal, MergeOrder> buyLimitPriceQueue;
    /**
     * 卖出限价订单链表，价格从低到高排列
     */
    private TreeMap<BigDecimal, MergeOrder> sellLimitPriceQueue;
    /**
     * 买入市价订单链表，按时间从小到大排序
     */
    private LinkedList<ExchangeOrder> buyMarketQueue;
    /**
     * 卖出市价订单链表，按时间从小到大排序
     */
    private LinkedList<ExchangeOrder> sellMarketQueue;

    /**
     * 卖盘盘口信息
     */
    private TradePlate sellTradePlate;
    /**
     * 买盘盘口信息
     */
    private TradePlate buyTradePlate;

    /**
     * 是否下线该交易撮合器
     */
    private boolean tradingStop = false;
    /**
     * 是否暂停交易
     */
    private boolean tradingHalt = false;
    /**
     * 撮合器是否准备就绪
     */
    private boolean ready = false;

    public CoinTrader(String symbol) {
        this.symbol = symbol;
        initialize();
    }

    /**
     * 初始化交易线程
     */
    public void initialize() {
        logger.info("init CoinTrader for symbol {}", symbol);
        //买单队列价格降序排列
        buyLimitPriceQueue = new TreeMap<>(Comparator.reverseOrder());
        //卖单队列价格升序排列
        sellLimitPriceQueue = new TreeMap<>(Comparator.naturalOrder());
        buyMarketQueue = new LinkedList<>();
        sellMarketQueue = new LinkedList<>();

        sellTradePlate = new TradePlate(symbol, ExchangeOrderDirection.SELL);
        buyTradePlate = new TradePlate(symbol, ExchangeOrderDirection.BUY);
    }

    /**
     * 增加限价订单到队列，买入单按从价格高到低排，卖出单按价格从低到高排
     *
     * @param exchangeOrder
     */
    public void addLimitPriceOrder(ExchangeOrder exchangeOrder) {
        if (exchangeOrder.getType() != ExchangeOrderType.LIMIT_PRICE) {
            logger.warn("不是限价订单，已丢弃该订单，订单={}", exchangeOrder);
            return;
        }
        if (!exchangeOrder.getSymbol().equalsIgnoreCase(symbol)) {
            logger.warn("交易对不匹配，已丢弃该订单，订单={}", exchangeOrder);
            return;
        }

        logger.info("增加到限价订单队列,orderId = {}", exchangeOrder.getOrderId());
        TreeMap<BigDecimal, MergeOrder> list;
        if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
            list = buyLimitPriceQueue;
            buyTradePlate.add(exchangeOrder);
            if (ready) {
                sendTradePlateMessage(buyTradePlate);
            }
        } else {
            list = sellLimitPriceQueue;
            sellTradePlate.add(exchangeOrder);
            if (ready) {
                sendTradePlateMessage(sellTradePlate);
            }
        }
        synchronized (list) {
            MergeOrder mergeOrder = list.get(exchangeOrder.getPrice());
            if (mergeOrder == null) {
                mergeOrder = new MergeOrder();
                mergeOrder.add(exchangeOrder);
                list.put(exchangeOrder.getPrice(), mergeOrder);
            } else {
                mergeOrder.add(exchangeOrder);
            }
        }
    }

    public void addMarketPriceOrder(ExchangeOrder exchangeOrder) {
        if (exchangeOrder.getType() != ExchangeOrderType.MARKET_PRICE) {
            logger.warn("不是市价订单，已丢弃该订单，订单={}", exchangeOrder);
            return;
        }
        if (!exchangeOrder.getSymbol().equalsIgnoreCase(symbol)) {
            logger.warn("交易对不匹配，已丢弃该订单，订单={}", exchangeOrder);
            return;
        }

        logger.info("增加到市价订单队列,orderId = {}", exchangeOrder.getOrderId());
        LinkedList<ExchangeOrder> list = exchangeOrder.getDirection() == ExchangeOrderDirection.BUY ? buyMarketQueue : sellMarketQueue;
        synchronized (list) {
            list.addLast(exchangeOrder);
        }
    }

    public void trade(List<ExchangeOrder> orders) {
        if (tradingHalt) {
            return;
        }
        for (ExchangeOrder order : orders) {
            trade(order);
        }
    }


    /**
     * 主动交易输入的订单，交易不完成的会输入到队列
     *
     * @param exchangeOrder
     */
    public void trade(ExchangeOrder exchangeOrder) {
        if (tradingHalt) {
            logger.warn("交易暂停，已丢弃该订单，订单={}", exchangeOrder);
            return;
        }
        if (!symbol.equalsIgnoreCase(exchangeOrder.getSymbol())) {
            logger.warn("交易对不匹配，已丢弃该订单，订单={}", exchangeOrder);
            return;
        }

        logger.info("trade symbol={}, orderId={}", symbol, exchangeOrder.getOrderId());
        //logger.debug("trade order={}",exchangeOrder);
        /*if(exchangeOrder.getAmount().compareTo(BigDecimal.ZERO) <=0
        || exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount()).compareTo(BigDecimal.ZERO)<=0){
            return ;
        }*/
        //edit by yangch 时间： 2018.05.12 原因：解决市价部分交易后重启exchange模块导致无法继续交易和撤单的问题
//        if(exchangeOrder.getType()== ExchangeOrderType.MARKET_PRICE
//                && exchangeOrder.getDirection()==ExchangeOrderDirection.BUY){
//            //需要换算为USDT后再判断（exchangeOrder.getTurnover()即为市价买入的成交额）
//            if (exchangeOrder.getAmount().compareTo(BigDecimal.ZERO) <= 0 ||
//                    exchangeOrder.getAmount().subtract(exchangeOrder.getTurnover()).compareTo(BigDecimal.ZERO) <= 0) {
//                return;
//            }
//        } else {
//            if (exchangeOrder.getAmount().compareTo(BigDecimal.ZERO) <= 0
//                    || exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount()).compareTo(BigDecimal.ZERO) <= 0) {
//                return;
//            }
//        }
        //判断订单是否已完成
        if (exchangeOrder.isCompleted()
                || exchangeOrder.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("已完成的订单或者交易数量为小于等于0，订单={}", exchangeOrder);
            orderCompleted(exchangeOrder);
        }

        TreeMap<BigDecimal, MergeOrder> limitPriceOrderList;
        LinkedList<ExchangeOrder> marketPriceOrderList;
        if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
            limitPriceOrderList = sellLimitPriceQueue;
            marketPriceOrderList = sellMarketQueue;
        } else {
            limitPriceOrderList = buyLimitPriceQueue;
            marketPriceOrderList = buyMarketQueue;
        }

        if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE) {
            // 与限价单交易
            matchMarketPriceWithLPList(limitPriceOrderList, exchangeOrder);
        } else if (exchangeOrder.getType() == ExchangeOrderType.LIMIT_PRICE) {
            //限价单价格必须大于0
            if (exchangeOrder.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("限价单价格必须大于0，已丢弃该订单，订单={}", exchangeOrder);
                return;
            }
            // 先与限价单交易
            matchLimitPriceWithLPList(limitPriceOrderList, exchangeOrder, false);
            if (exchangeOrder.getAmount().compareTo(exchangeOrder.getTradedAmount()) > 0) {
                //后与市价单交易
                matchLimitPriceWithMPList(marketPriceOrderList, exchangeOrder);
            }
        }
    }

    /**
     * 限价委托单与限价队列匹配
     *
     * @param lpList       限价对手单队列
     * @param focusedOrder 委托订单
     */
    public void matchLimitPriceWithLPList(TreeMap<BigDecimal, MergeOrder> lpList, ExchangeOrder focusedOrder, boolean canEnterList) {
        List<ExchangeTrade> exchangeTrades = new ArrayList<>();
        List<ExchangeOrder> completedOrders = new ArrayList<>();
        synchronized (lpList) {
            Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = lpList.entrySet().iterator();
            boolean exitLoop = false;
            while (!exitLoop && mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
                //买入单需要匹配的价格不大于委托价，否则退出
                if (focusedOrder.getDirection() == ExchangeOrderDirection.BUY
                        && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) > 0) {
                    break;
                }
                //卖出单需要匹配的价格不小于委托价，否则退出
                if (focusedOrder.getDirection() == ExchangeOrderDirection.SELL
                        && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) < 0) {
                    break;
                }

                while (orderIterator.hasNext()) {
                    ExchangeOrder matchOrder = orderIterator.next();
                    //订单匹配处理
                    ExchangeTrade trade = processMatch(focusedOrder, matchOrder);
                    if (null != trade) {
                        exchangeTrades.add(trade);
                    }

                    //判断匹配单是否完成
                    if (matchOrder.isCompleted()) {
                        //当前匹配的订单完成交易，删除该订单
                        orderIterator.remove();
                        completedOrders.add(matchOrder);
                        withUnfinishedTraded(trade, focusedOrder);
                    }

                    //判断交易单是否完成
                    if (focusedOrder.isCompleted()) {
                        //交易完成
                        completedOrders.add(focusedOrder);
                        withUnfinishedTraded(trade, matchOrder);

                        //退出循环
                        exitLoop = true;
                        break;
                    }
                }

                if (mergeOrder.size() == 0) {
                    //移除队列中的该价格
                    mergeOrderIterator.remove();

                    //备注：队列移除后可能导致订单队列和盘口的数据不一致,需移除此价格档位的盘口
                    if (focusedOrder.getDirection() == ExchangeOrderDirection.SELL) {
                        buyTradePlate.removeItem(entry.getKey());
                    } else {
                        sellTradePlate.removeItem(entry.getKey());
                    }
                }
            }
        }

        //如果还没有交易完，订单压入列表中
        if (focusedOrder.getTradedAmount().compareTo(focusedOrder.getAmount()) < 0 && canEnterList) {
            addLimitPriceOrder(focusedOrder);
        }

        //每个订单的匹配批量推送
        handleExchangeTrade(exchangeTrades);
        if (completedOrders.size() > 0) {
            orderCompleted(completedOrders);
            TradePlate plate = focusedOrder.getDirection() == ExchangeOrderDirection.BUY ? sellTradePlate : buyTradePlate;
            sendTradePlateMessage(plate);
        }
    }

    /**
     * 限价委托单与市价队列匹配
     *
     * @param mpList       市价对手单队列
     * @param focusedOrder 交易订单
     */
    public void matchLimitPriceWithMPList(LinkedList<ExchangeOrder> mpList, ExchangeOrder focusedOrder) {
        List<ExchangeTrade> exchangeTrades = new ArrayList<>();
        List<ExchangeOrder> completedOrders = new ArrayList<>();
        synchronized (mpList) {
            Iterator<ExchangeOrder> iterator = mpList.iterator();
            while (iterator.hasNext()) {
                ExchangeOrder matchOrder = iterator.next();
                ExchangeTrade trade = processMatch(focusedOrder, matchOrder);
                if (trade != null) {
                    exchangeTrades.add(trade);
                }
                //判断匹配单是否完成，市价单amount为成交量
                if (matchOrder.isCompleted()) {
                    iterator.remove();
                    completedOrders.add(matchOrder);
                    withUnfinishedTraded(trade, focusedOrder);
                }
                //判断吃单是否完成，判断成交量是否完成
                if (focusedOrder.isCompleted()) {
                    //交易完成
                    completedOrders.add(focusedOrder);
                    withUnfinishedTraded(trade, matchOrder);

                    //退出循环
                    break;
                }
            }
        }

        //如果还没有交易完，订单压入列表中
        if (focusedOrder.getTradedAmount().compareTo(focusedOrder.getAmount()) < 0) {
            addLimitPriceOrder(focusedOrder);
        }
        //每个订单的匹配批量推送
        handleExchangeTrade(exchangeTrades);
        orderCompleted(completedOrders);
    }


    /**
     * 市价委托单与限价对手单列表交易
     *
     * @param lpList       限价对手单列表
     * @param focusedOrder 待交易订单
     */
    public void matchMarketPriceWithLPList(TreeMap<BigDecimal, MergeOrder> lpList, ExchangeOrder focusedOrder) {
        List<ExchangeTrade> exchangeTrades = new ArrayList<>();
        List<ExchangeOrder> completedOrders = new ArrayList<>();
        synchronized (lpList) {
            Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = lpList.entrySet().iterator();
            boolean exitLoop = false;
            while (!exitLoop && mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
                while (orderIterator.hasNext()) {
                    ExchangeOrder matchOrder = orderIterator.next();
                    //处理匹配
                    ExchangeTrade trade = processMatch(focusedOrder, matchOrder);
                    if (trade != null) {
                        exchangeTrades.add(trade);
                    }
                    //判断匹配单是否完成
                    if (matchOrder.isCompleted()) {
                        //当前匹配的订单完成交易，删除该订单
                        orderIterator.remove();
                        completedOrders.add(matchOrder);
                        withUnfinishedTraded(trade, focusedOrder);
                    }
                    //判断焦点订单是否完成
                    if (focusedOrder.isCompleted()) {
                        completedOrders.add(focusedOrder);
                        withUnfinishedTraded(trade, matchOrder);

                        //退出循环
                        exitLoop = true;
                        break;
                    }
                }
                if (mergeOrder.size() == 0) {
                    //移除订单，同时异常盘口数据(注意，此处可能导致订单队列和盘口的数据不一致)
                    mergeOrderIterator.remove();

                    //备注：队列移除后可能导致订单队列和盘口的数据不一致,需移除此价格档位的盘口
                    if (focusedOrder.getDirection() == ExchangeOrderDirection.SELL) {
                        buyTradePlate.removeItem(entry.getKey());
                    } else {
                        sellTradePlate.removeItem(entry.getKey());
                    }
                }
            }
        }

        //如果还没有交易完，订单压入列表中,市价买单按成交量算
        if ((focusedOrder.getDirection() == ExchangeOrderDirection.SELL
                && focusedOrder.getTradedAmount().compareTo(focusedOrder.getAmount()) < 0)
                || (focusedOrder.getDirection() == ExchangeOrderDirection.BUY
                && focusedOrder.getTurnover().compareTo(focusedOrder.getAmount()) < 0)) {
            addMarketPriceOrder(focusedOrder);
        }

        //每个订单的匹配批量推送
        handleExchangeTrade(exchangeTrades);
        if (completedOrders.size() > 0) {
            orderCompleted(completedOrders);
            TradePlate plate = focusedOrder.getDirection() == ExchangeOrderDirection.BUY ? sellTradePlate : buyTradePlate;
            sendTradePlateMessage(plate);
        }
    }

    //为成交单中设置未成交完的订单信息
    private void withUnfinishedTraded(ExchangeTrade trade, ExchangeOrder order) {
        if (order.isCompleted() == false && null != trade) {
            trade.setUnfinishedOrderId(order.getOrderId());
            trade.setUnfinishedTradedAmount(order.getTradedAmount());
            trade.setUnfinishedTradedTurnover(order.getTurnover());
        }
    }

    /**
     * 计算委托单剩余可成交的数量
     *
     * @param order     委托单
     * @param dealPrice 成交价
     * @return
     */
    private BigDecimal calculateTradedAmount(ExchangeOrder order, BigDecimal dealPrice) {
        if (order.getDirection() == ExchangeOrderDirection.BUY
                && order.getType() == ExchangeOrderType.MARKET_PRICE) {
            //计算剩余成交额
            BigDecimal leftTurnover = order.getAmount().subtract(order.getTurnover());
            //计算剩余成交量
            return leftTurnover.divide(dealPrice, coinScale, BigDecimal.ROUND_DOWN);
        } else {
            return order.getAmount().subtract(order.getTradedAmount());
        }
    }

    /**
     * 调整市价单剩余成交额，当剩余成交额不足时设置订单完成
     *
     * @param order
     * @param dealPrice
     * @return 返回校正无法成交的成交额数量
     */
    private BigDecimal adjustMarketOrderTurnover(ExchangeOrder order, BigDecimal dealPrice) {
        if (order.getDirection() == ExchangeOrderDirection.BUY
                && order.getType() == ExchangeOrderType.MARKET_PRICE) {
            BigDecimal leftTurnover = order.getAmount().subtract(order.getTurnover());
            if (leftTurnover.divide(dealPrice, coinScale, BigDecimal.ROUND_DOWN)
                    .compareTo(BigDecimal.ZERO) == 0) {

                //2019-01-31 yangch : 最终成交额包含了无法成交的部分（注：实际为平台“吃掉”了无法成交的部分，且没有记录该账，在处理数据的平衡关系时需要注意）
                order.setTurnover(order.getAmount());
                return leftTurnover;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 处理两个匹配的委托订单
     *
     * @param focusedOrder 焦点单
     * @param matchOrder   匹配单
     * @return
     */
    private ExchangeTrade processMatch(ExchangeOrder focusedOrder, ExchangeOrder matchOrder) {
        if (!focusedOrder.getSymbol().equalsIgnoreCase(matchOrder.getSymbol())) {
            logger.warn("订单交易对不匹配，无法撮合，focusedOrder={}, matchOrder={}", focusedOrder, matchOrder);
            return null;
        }

        //需要交易的数量，成交量,成交价，可用数量
        BigDecimal needAmount, dealPrice, availAmount;
        //如果匹配单是限价单，则以其价格为成交价
        if (matchOrder.getType() == ExchangeOrderType.LIMIT_PRICE) {
            dealPrice = matchOrder.getPrice();
        } else {
            dealPrice = focusedOrder.getPrice();
        }
        //成交价必须大于0
        if (dealPrice == null || dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("订单处理价格小于等于0，无法撮合, focusedOrder={}, matchOrder={}", focusedOrder, matchOrder);
            return null;
        }

        needAmount = calculateTradedAmount(focusedOrder, dealPrice);
        availAmount = calculateTradedAmount(matchOrder, dealPrice);
        //计算成交量
        BigDecimal tradedAmount = (availAmount.compareTo(needAmount) >= 0 ? needAmount : availAmount);
        //logger.info("dealPrice={},amount={}",dealPrice,tradedAmount);
        //如果成交量为0说明剩余额度无法成交，退出
        if (tradedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            //校正市价买入单的成交额=数量，防止订单一直循环匹单
            //edit by yangch 时间： 2019.01.31 原因：去重重复的判断
            /*if(ExchangeOrderType.MARKET_PRICE == focusedOrder.getType()
                    && focusedOrder.getDirection() == ExchangeOrderDirection.BUY){
                adjustMarketOrderTurnover(focusedOrder, dealPrice);
            } else if(ExchangeOrderType.MARKET_PRICE == matchOrder.getType()
                    && matchOrder.getDirection() == ExchangeOrderDirection.BUY){
                adjustMarketOrderTurnover(matchOrder, dealPrice);
            }*/
            adjustMarketOrderTurnover(focusedOrder, dealPrice);
            adjustMarketOrderTurnover(matchOrder, dealPrice);

            return null;
        }

        //计算成交额,成交额应该和数量保持相同的精度
        //BigDecimal turnover = tradedAmount.multiply(dealPrice).setScale(this.baseCoinScale,BigDecimal.ROUND_UP);
        //edit by yangch 时间： 2018.05.21 原因：成交额应该和数量保持相同的精度
        //BigDecimal turnover = tradedAmount.multiply(dealPrice);
        ///BigDecimal turnover = tradedAmount.multiply(dealPrice).setScale(this.coinScale,BigDecimal.ROUND_UP); //向上取在多笔单撮合的结果中可能超过了冻结余额
        ///BigDecimal turnover = tradedAmount.multiply(dealPrice).setScale(this.coinScale, BigDecimal.ROUND_DOWN);
        BigDecimal turnover = tradedAmount.multiply(dealPrice).setScale(this.baseCoinScale, BigDecimal.ROUND_DOWN);
        /**
         * edit by yangch 时间： 2019.01.28 : 校正市价买单的成交额
         * 解决市价买入时，因最后一笔舍掉后导致的“精度尾巴”问题
         * 如：买入的交易额为10，成交价为1.88600000，实际成交为9.999949200000，
         *    舍掉后为9.999，剩下0.001，导致还需要成交一笔才能完成该单的问题（该问题会导致一个极小的撮单明细）
         * 解决：市价买入时，判断是否等于“最小的精度”，如存在则重新计算并向上入
         */
        if (ExchangeOrderType.MARKET_PRICE == focusedOrder.getType()
                && focusedOrder.getDirection() == ExchangeOrderDirection.BUY &&
                focusedOrder.getAmount().subtract(turnover).multiply(BigDecimal.TEN.pow(this.baseCoinScale))
                        .compareTo(BigDecimal.ONE) == 0) {
            turnover = tradedAmount.multiply(dealPrice).setScale(this.baseCoinScale, BigDecimal.ROUND_UP);
        }

        //edit by yangch 时间： 2019.01.28 原因： 舍掉后的交易额为0
        //如果成交额为0说明剩余额度无法成交，按最大位数的精度处理，如仍为0则退出
        if (turnover.compareTo(BigDecimal.ZERO) <= 0) {
            turnover = tradedAmount.multiply(dealPrice).setScale(8, BigDecimal.ROUND_DOWN);
            //放宽条件后仍然为0的情况
            if (turnover.compareTo(BigDecimal.ZERO) <= 0) {
                adjustMarketOrderTurnover(focusedOrder, dealPrice);
                adjustMarketOrderTurnover(matchOrder, dealPrice);
                return null;
            }
        }

        matchOrder.setTradedAmount(matchOrder.getTradedAmount().add(tradedAmount));
        matchOrder.setTurnover(matchOrder.getTurnover().add(turnover));
        focusedOrder.setTradedAmount(focusedOrder.getTradedAmount().add(tradedAmount));
        focusedOrder.setTurnover(focusedOrder.getTurnover().add(turnover));

        //创建成交记录
        ExchangeTrade exchangeTrade = new ExchangeTrade();
        exchangeTrade.setSymbol(symbol);
        exchangeTrade.setAmount(tradedAmount);
        exchangeTrade.setDirection(focusedOrder.getDirection());
        exchangeTrade.setPrice(dealPrice);
        exchangeTrade.setBuyTurnover(turnover);
        exchangeTrade.setSellTurnover(turnover);

        //校正市价单剩余成交额
        if (ExchangeOrderType.MARKET_PRICE == focusedOrder.getType()
                && focusedOrder.getDirection() == ExchangeOrderDirection.BUY) {
            BigDecimal adjustTurnover = adjustMarketOrderTurnover(focusedOrder, dealPrice);
            exchangeTrade.setBuyTurnover(turnover.add(adjustTurnover));
        } else if (ExchangeOrderType.MARKET_PRICE == matchOrder.getType()
                && matchOrder.getDirection() == ExchangeOrderDirection.BUY) {
            BigDecimal adjustTurnover = adjustMarketOrderTurnover(matchOrder, dealPrice);
            exchangeTrade.setBuyTurnover(turnover.add(adjustTurnover));
        }

        if (focusedOrder.getDirection() == ExchangeOrderDirection.BUY) {
            exchangeTrade.setBuyMemberId(focusedOrder.getMemberId());
            exchangeTrade.setBuyOrderId(focusedOrder.getOrderId());
            exchangeTrade.setSellMemberId(matchOrder.getMemberId());
            exchangeTrade.setSellOrderId(matchOrder.getOrderId());
        } else {
            exchangeTrade.setBuyMemberId(matchOrder.getMemberId());
            exchangeTrade.setBuyOrderId(matchOrder.getOrderId());
            exchangeTrade.setSellMemberId(focusedOrder.getMemberId());
            exchangeTrade.setSellOrderId(focusedOrder.getOrderId());
        }

        exchangeTrade.setTime(Calendar.getInstance().getTimeInMillis());
        if (matchOrder.getType() == ExchangeOrderType.LIMIT_PRICE) {
            if (matchOrder.getDirection() == ExchangeOrderDirection.BUY) {
                buyTradePlate.remove(matchOrder, tradedAmount, false);
                //sendTradePlateMessage(buyTradePlate); //订单处理完后再发送盘口数据
            } else {
                sellTradePlate.remove(matchOrder, tradedAmount, false);
                //sendTradePlateMessage(sellTradePlate); //订单处理完后再发送盘口数据
            }
        }
        return exchangeTrade;
    }

    public void handleExchangeTrade(List<ExchangeTrade> trades) {
        //logger.debug("handleExchangeTrade:{}", trades);
        if (trades.size() > 0) {
            int maxSize = 1000;
            //发送消息，key为交易对符号
            if (trades.size() > maxSize) {
                int size = trades.size();
                for (int index = 0; index < size; index += maxSize) {
                    int length = (size - index) > maxSize ? maxSize : size - index;
                    List<ExchangeTrade> subTrades = trades.subList(index, index + length);
                    logger.info("exchangeTrade。symbol={}, trades={}", symbol, subTrades);
                    //成交明细
                    kafkaTemplate.send("exchange-trade", symbol, JSON.toJSONString(subTrades));
                    ///kafkaTemplate.send("exchange-trade-market", symbol, JSON.toJSONString(subTrades));  //行情消息
                }
            } else {
                logger.info("exchangeTrade。symbol={}, trades={}", symbol, trades);
                //成交明细
                kafkaTemplate.send("exchange-trade", symbol, JSON.toJSONString(trades));
                ///kafkaTemplate.send("exchange-trade-market", symbol, JSON.toJSONString(trades)); //行情消息
            }
        }
    }

    /**
     * 订单完成，执行消息通知,订单数超1000个要拆分发送
     *
     * @param orders
     */
    public void orderCompleted(List<ExchangeOrder> orders) {
        if (orders.size() > 0) {
            int maxSize = 1000;
            if (orders.size() > maxSize) {
                int size = orders.size();
                for (int index = 0; index < size; index += maxSize) {
                    int length = (size - index) > maxSize ? maxSize : size - index;
                    List<ExchangeOrder> subOrders = orders.subList(index, index + length);
                    //logger.info("orderCompleted。symbol={}, orders={}", symbol, orders);
                    kafkaTemplate.send("exchange-order-completed", symbol, JSON.toJSONString(subOrders));
                }
            } else {
                //logger.info("orderCompleted。symbol={}, orders={}", symbol, orders);
                kafkaTemplate.send("exchange-order-completed", symbol, JSON.toJSONString(orders));
            }
        }
    }

    public void orderCompleted(ExchangeOrder order) {
        if (null != order) {
            List<ExchangeOrder> orders = new ArrayList<>(1);
            orders.add(order);
            //logger.info("orderCompleted。symbol={}, orders={}", symbol, orders);
            kafkaTemplate.send("exchange-order-completed", symbol, JSON.toJSONString(orders));
        }
    }

    /**
     * 发送盘口变化消息
     *
     * @param plate
     */
    public void sendTradePlateMessage(TradePlate plate) {
//        TradePlate newPlate = plate;
        //控制推送的最大盘口深度
        if (plate.currentDepth() > plate.getMaxDepth()) {
            TradePlate newPlate = new TradePlate(symbol, plate.getDirection());
            newPlate.setItems(plate.getItems(plate.getMaxDepth()));
            plateMessageWrapper.push(newPlate);
        } else {
            plateMessageWrapper.push(plate);
        }

//        //kafkaTemplate.send("exchange-trade-plate", symbol, JSON.toJSONString(plate));
//        kafkaTemplate.send("exchange-trade-plate", symbol, JSON.toJSONString(newPlate));
    }

    /**
     * 取消委托订单
     *
     * @param exchangeOrder
     * @return
     */
    public ExchangeOrder cancelOrder(ExchangeOrder exchangeOrder) {
        logger.debug("orderCanceled,orderId={}", exchangeOrder.getOrderId());
        if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE) {
            // 处理市价单
            List<ExchangeOrder> list;
            if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
                list = this.buyMarketQueue;
            } else {
                list = this.sellMarketQueue;
            }
            synchronized (list) {
                Iterator<ExchangeOrder> orderIterator = list.iterator();
                while ((orderIterator.hasNext())) {
                    ExchangeOrder order = orderIterator.next();
                    if (order.getOrderId().equalsIgnoreCase(exchangeOrder.getOrderId())) {
                        orderIterator.remove();
                        onRemoveOrder(order, false);
                        return order;
                    }
                }
            }
        } else {
            // 处理限价单
            TreeMap<BigDecimal, MergeOrder> list;
            if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
                list = this.buyLimitPriceQueue;
            } else {
                list = this.sellLimitPriceQueue;
            }
            synchronized (list) {
                MergeOrder mergeOrder = list.get(exchangeOrder.getPrice());
                if (null == mergeOrder) {
                    return null;
                }
                Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
                while (orderIterator.hasNext()) {
                    ExchangeOrder order = orderIterator.next();
                    if (order.getOrderId().equalsIgnoreCase(exchangeOrder.getOrderId())) {
                        orderIterator.remove();
                        if (mergeOrder.size() == 0) {
                            list.remove(exchangeOrder.getPrice());
                            onRemoveOrder(order, true);
                        } else {
                            onRemoveOrder(order, false);
                        }
                        return order;
                    }
                }
            }
        }
        return null;
    }

    public void onRemoveOrder(ExchangeOrder order, boolean removeItemFlag) {
        logger.debug("removeOrder,orderId={}", order.getOrderId());
        if (order.getType() == ExchangeOrderType.LIMIT_PRICE) {
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                buyTradePlate.remove(order, removeItemFlag);
                sendTradePlateMessage(buyTradePlate);
            } else {
                sellTradePlate.remove(order, removeItemFlag);
                sendTradePlateMessage(sellTradePlate);
            }
        }
    }

    public TradePlate getTradePlate(ExchangeOrderDirection direction) {
        if (direction == ExchangeOrderDirection.BUY) {
            return buyTradePlate;
        } else {
            return sellTradePlate;
        }
    }


    /**
     * 查询交易器里的订单
     *
     * @param orderId
     * @param type
     * @param direction
     * @return
     */
    public ExchangeOrder findOrder(String orderId, ExchangeOrderType type, ExchangeOrderDirection direction) {
        if (type == ExchangeOrderType.MARKET_PRICE) {
            LinkedList<ExchangeOrder> list;
            if (direction == ExchangeOrderDirection.BUY) {
                list = this.buyMarketQueue;
            } else {
                list = this.sellMarketQueue;
            }
            synchronized (list) {
                Iterator<ExchangeOrder> orderIterator = list.iterator();
                while ((orderIterator.hasNext())) {
                    ExchangeOrder order = orderIterator.next();
                    if (order.getOrderId().equalsIgnoreCase(orderId)) {
                        return order;
                    }
                }
            }
        } else {
            TreeMap<BigDecimal, MergeOrder> list;
            if (direction == ExchangeOrderDirection.BUY) {
                list = this.buyLimitPriceQueue;
            } else {
                list = this.sellLimitPriceQueue;
            }
            synchronized (list) {
                Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = list.entrySet().iterator();
                while (mergeOrderIterator.hasNext()) {
                    Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                    MergeOrder mergeOrder = entry.getValue();
                    Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
                    while ((orderIterator.hasNext())) {
                        ExchangeOrder order = orderIterator.next();
                        if (order.getOrderId().equalsIgnoreCase(orderId)) {
                            return order;
                        }
                    }
                }
            }
        }
        return null;
    }

    public TreeMap<BigDecimal, MergeOrder> getBuyLimitPriceQueue() {
        return buyLimitPriceQueue;
    }

    public LinkedList<ExchangeOrder> getBuyMarketQueue() {
        return buyMarketQueue;
    }

    public TreeMap<BigDecimal, MergeOrder> getSellLimitPriceQueue() {
        return sellLimitPriceQueue;
    }

    public LinkedList<ExchangeOrder> getSellMarketQueue() {
        return sellMarketQueue;
    }

    public void setKafkaTemplate(KafkaTemplate<String, String> template) {
        this.kafkaTemplate = template;
    }

    public void setPlateMessageWrapper(PlateMessageWrapper plateMessageWrapper) {
        this.plateMessageWrapper = plateMessageWrapper;
    }

    public void setCoinScale(int scale) {
        this.coinScale = scale;
    }

    //返回当前币种精度
    public int getCoinScale() {
        return this.coinScale;
    }

    public void setBaseCoinScale(int scale) {
        this.baseCoinScale = scale;
    }

    //返回基币精度
    public int getBaseCoinScale() {
        return this.baseCoinScale;
    }

    //add by yangch 时间： 2018.10.29 原因：通过撤销当前撮合器的所有订单方法
    public void cancelAllOrder() {
        logger.info("cancelAllOrder：开始撤销订单，交易对={}", symbol);
        List<ExchangeOrder> listOrder = new ArrayList<>();
        //买单队列
        this.buyLimitPriceQueue.forEach((_price, _mergeOrder) -> {
            _mergeOrder.getOrders().forEach(order -> {
                listOrder.add(order);
            });
        });
        this.buyMarketQueue.forEach(order -> {
            listOrder.add(order);
        });

        //卖单队列
        this.sellLimitPriceQueue.forEach((_price, _mergeOrder) -> {
            _mergeOrder.getOrders().forEach(order -> {
                listOrder.add(order);
            });
        });
        this.sellMarketQueue.forEach(order -> {
            listOrder.add(order);
        });

        //撤销
        logger.info("cancelAllOrder：撤销的订单数量={}", listOrder.size());
        listOrder.iterator().forEachRemaining(order -> {
            this.cancelOrder(order);
            //正常的从交易内存队列中撤单
            kafkaTemplate.send("exchange-order-cancel-success", symbol, JSON.toJSONString(order));
        });

        listOrder.clear();
        logger.info("cancelAllOrder：完成所有订单的撤销，交易对={}", symbol);
    }

    //add by yangch 时间： 2018.10.29 原因：重置盘口数据
    public void resetTradePlate(ExchangeOrderDirection direction) {
        logger.info("resetTradePlate：开始重置盘口数据，交易对={}", symbol);
        //临时存放tradingHalt的状态
        boolean _tradingHalt = this.tradingHalt;
        this.haltTrading();

        //重置买盘盘口
        if (null == direction || direction == ExchangeOrderDirection.BUY) {
            this.buyTradePlate = new TradePlate(symbol, ExchangeOrderDirection.BUY);

            //恢复买盘盘口数据
            this.buyLimitPriceQueue.forEach((_price, _mergeOrder) -> {
                _mergeOrder.getOrders().forEach(order -> {
                    this.buyTradePlate.add(order);
                });
            });

            //重新发送盘口数据
            this.sendTradePlateMessage(this.buyTradePlate);
        }

        //重置卖盘盘口
        if (null == direction || direction == ExchangeOrderDirection.SELL) {
            this.sellTradePlate = new TradePlate(symbol, ExchangeOrderDirection.SELL);
            //恢复卖盘盘口数据
            this.sellLimitPriceQueue.forEach((_price, _mergeOrder) -> {
                _mergeOrder.getOrders().forEach(order -> {
                    this.sellTradePlate.add(order);
                });
            });
            //重新发送盘口数据
            this.sendTradePlateMessage(this.sellTradePlate);
        }

        //恢复状态
        this.tradingHalt = _tradingHalt;

        logger.info("resetTradePlate：完成盘口数据重置，交易对={}", symbol);
    }

    public boolean isTradingHalt() {
        return this.tradingHalt;
    }

    /**
     * 暂停交易,不接收新的订单
     */
    public void haltTrading() {
        this.tradingHalt = true;
    }

    /**
     * 恢复交易
     */
    public void resumeTrading() {
        this.tradingHalt = false;
    }

    /**
     * 停止并下线交易撮合器
     */
    public void stopTrading() {
        //停止交易，取消当前所有订单
        haltTrading();
        this.tradingStop = true;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * 交易撮合器的状态
     *
     * @return
     */
    public boolean isTradingStop() {
        return this.tradingStop;
    }

    public boolean getReady() {
        return this.ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getLimitPriceOrderCount(ExchangeOrderDirection direction) {
        int count = 0;
        TreeMap<BigDecimal, MergeOrder> queue = direction == ExchangeOrderDirection.BUY ? buyLimitPriceQueue : sellLimitPriceQueue;
        Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = queue.entrySet().iterator();
        while (mergeOrderIterator.hasNext()) {
            Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
            MergeOrder mergeOrder = entry.getValue();
            count += mergeOrder.size();
        }
        return count;
    }

}
