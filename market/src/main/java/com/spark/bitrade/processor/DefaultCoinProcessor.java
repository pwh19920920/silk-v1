package com.spark.bitrade.processor;


import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.handler.MarketHandler;
import com.spark.bitrade.service.KLineService;
import com.spark.bitrade.service.MarketService;
import com.spark.bitrade.service.PushTradeMessage;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 默认交易处理器，产生1mK线信息
 *
 * @author yangch
 */
@ToString
public class DefaultCoinProcessor implements CoinProcessor {
    private Logger logger = LoggerFactory.getLogger(DefaultCoinProcessor.class);

    private String symbol;
    private String baseCoin;
    /**
     * 缓存上一条K线数据
     */
    private Map<String, KLine> privPeriodKlineMap;
    /**
     * 实时k线集合
     */
    private Map<String, KLine> realtimePeriodKlineMap;
    private List<MarketHandler> handlers;
    /**
     * 当前凌晨到当前的缩略行情
     */
    private CoinThumb coinThumb;

    /**
     * 卖盘盘口信息
     */
    private TradePlate sellTradePlate;
    /**
     * 买盘盘口信息
     */
    private TradePlate buyTradePlate;
    /**
     * 是否暂时处理
     */
    private Boolean isHalt = true;
    /**
     * 标记盘口信息是否已初始化（未初始化则从exchange获取初始化的数据，已初始化则不再初始化，避免重复调用exchange应用）
     */
    private boolean isTradePlateinitialize = false;

    /// 服务层接口
    private MarketService marketService;
    private KLineService kLineService;
    private CoinExchangeRate coinExchangeRate;


    /**
     * 初始化
     *
     * @param coin 交易对配置信息
     *              
     * @author yangch
     * @time 2018.07.16 13:54 
     */
    public DefaultCoinProcessor(ExchangeCoin coin) {
        handlers = new ArrayList<>();
        //createNewKLine(); //创建下1分钟k线(废弃，由initializeRealtimeKline()方法完成K线初始化 by 2018-07-21)
        this.baseCoin = coin.getBaseSymbol();
        this.symbol = coin.getSymbol();
    }

    @Override
    public void initialize() {
        //初始化K线数据
        this.initializeRealtimeKline();
        //从日K线中获取数据
        this.initializeThumb();
        //初始化 usd的汇率
        this.initializeUsdRate();

        // 完成初始化，可以对外提供服务
        this.setIsHalt(false);
    }

    @Override
    public void update24HVolume(final long time) {
        if (!isHalt) {
            // 异步更新24H成交量和成交额
            kLineService.asyncUpdate24HVolumeAndTurnover(coinThumb, this.symbol, time, false);
        }

        //synchronized (coinThumb){
        //BigDecimal volume = marketService.findTradeVolume(this.symbol,timeStart,time); //该方式效率低
        //coinThumb.setVolume(volume.setScale(4,RoundingMode.DOWN));
        //}
    }

    /**
     * 00:00:00 时重置CoinThumb
     */
    @Override
    public void resetThumb() {
        logger.info("reset coinThumb");
        synchronized (coinThumb) {
            coinThumb.setOpen(BigDecimal.ZERO);
            coinThumb.setHigh(BigDecimal.ZERO);
            coinThumb.setLow(BigDecimal.ZERO);
            //设置昨收价格
            coinThumb.setLastDayClose(coinThumb.getClose());
            coinThumb.setChg(BigDecimal.ZERO);
            coinThumb.setChange(BigDecimal.ZERO);

            //coinThumb.setClose(BigDecimal.ZERO); //del by yangch 时间： 2018.07.31 原因：用于表示最新的实时价，不清零
            //coinThumb.setVolume(BigDecimal.ZERO);   //成交量
            //coinThumb.setTurnover(BigDecimal.ZERO); //成交额
        }
    }

    /**
     * k线处理入口
     *
     * @param trades
     */
    @Override
    public void process(final List<ExchangeTrade> trades) {
        if (!isHalt) {
            if (trades == null || trades.size() == 0) {
                return;
            }

            //synchronized (currentKLine) {
            for (ExchangeTrade exchangeTrade : trades) {
                //处理K线
                ///processKline(currentKLine, exchangeTrade);

                //更新所有周期的k线
                kLineService.getListPeriod().forEach(period -> {
                    /// logger.info("-------更新"+period+"K线的数据----------"+symbol);
                    KLine kLine = realtimePeriodKlineMap.get(period);
                    /// logger.info("更新前：" + kLine);
                    processKline(kLine, exchangeTrade);
                    // 推送新的K线
                    pushHandleKLine(kLine);
                    // logger.info("更新后："+realtimePeriodKlineMap.get(period));
                });

                /// 处理今日概况信息，处理CoinThumb
                logger.debug("处理今日概况信息");
                handleThumb(exchangeTrade);

                // 存储成交信息、推送CoinThumb（异步保存）
                handleTradeStorage(exchangeTrade);
            }
            //}

            //add by yangch 时间： 2018.06.07 原因：添加 推送1min实时k线
            ///pushHandleKLine(currentKLine);

            /////pushAllPeriodHandleKLine(); //推送所有k线的实时数据 一起推送可能存在粘包
        } else {
            logger.warn("{}交易对已停止", this.symbol);
        }
    }


    /**
     * 生成k线数据
     *
     * @param range k线周期范围 [1,5,10,15,30]min、[1]hour、[1]week、[1]day、[1]month
     * @param field k线周期类型 已知枚举选项：min=Calendar.MINUTE、hour=Calendar.HOUR_OF_DAY、week=Calendar.DAY_OF_WEEK、day=Calendar.DAY_OF_YEAR、month=Calendar.DAY_OF_MONTH
     * @param time  k线时间
     *               
     * @author yangch
     * @time 2018.07.16 17:07 
     */
    @Override
    public void generateKLine(final int range, final int field, final long time) {
        if (isHalt) {
            return;
        }

        String rangeUnit = "";
        if (field == Calendar.MINUTE) {
            rangeUnit = "min";
        } else if (field == Calendar.HOUR_OF_DAY) {
            //传入 Calendar.HOUR_OF_DAY
            rangeUnit = "hour";
        } else if (field == Calendar.DAY_OF_WEEK) {
            //传入 Calendar.DAY_OF_WEEK
            rangeUnit = "week";
        } else if (field == Calendar.DAY_OF_YEAR) {
            //传入 Calendar.DAY_OF_YEAR
            rangeUnit = "day";
        } else if (field == Calendar.DAY_OF_MONTH) {
            //} else if(field == Calendar.MONTH){ //同调用时传入的参数不一致，导致没有生成月k线
            //传入 Calendar.DAY_OF_MONTH
            rangeUnit = "month";
        }

        String period = range + rangeUnit;
        KLine pervKLine, newKLine;
        synchronized (realtimePeriodKlineMap) {
            //前一周期的k线
            pervKLine = realtimePeriodKlineMap.get(period);
            //切换K线
            newKLine = createNewKLine(period, time, pervKLine);
            //缓存新周期的K线数据
            realtimePeriodKlineMap.put(period, newKLine);

            // 缓存前一周期的k线数据
            privPeriodKlineMap.put(period, pervKLine);
        }

        // 推送新的K线
        pushHandleKLine(newKLine);
        // 保存前一周期的k线数据
        kLineService.saveKLine(symbol, pervKLine);
    }


    /**
     * 存储交易信息
     *
     * @param exchangeTrade
     */
    public void handleTradeStorage(final ExchangeTrade exchangeTrade) {
        for (MarketHandler storage : handlers) {
            storage.handleTrade(symbol, exchangeTrade, coinThumb);
        }
    }


    //setter getter
    @Override
    public void setExchangeRate(CoinExchangeRate coinExchangeRate) {
        this.coinExchangeRate = coinExchangeRate;
    }

    @Override
    public void addHandler(MarketHandler storage) {
        handlers.add(storage);
    }

    @Override
    public void setIsHalt(boolean status) {
        this.isHalt = status;
    }

    @Override
    public boolean isHalt() {
        return this.isHalt;
    }

    @Override
    public void setMarketService(MarketService service) {
        this.marketService = service;
    }

    @Override
    public void setKLineService(KLineService kLineService) {
        this.kLineService = kLineService;
    }

    @Override
    public CoinThumb getThumb() {
        return coinThumb;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public KLine getKLine(String period) {
        return realtimePeriodKlineMap.get(period);
    }

    @Override
    public KLine getPrevKLine(String period) {
        return privPeriodKlineMap.get(period);
    }

    /**
     * 设置盘口信息
     *
     * @param tradePlate 盘口信息
     *                    
     * @author yangch
     * @time 2018.06.29 14:35 
     */
    @Override
    public void setTradePlate(TradePlate tradePlate) {
        if (tradePlate.getDirection() == ExchangeOrderDirection.BUY) {
            this.buyTradePlate = tradePlate;
        } else if (tradePlate.getDirection() == ExchangeOrderDirection.SELL) {
            this.sellTradePlate = tradePlate;
        }
    }

    /**
     * 获取盘口信息
     *
     * @param direction 买方方向
     *                   
     * @author yangch
     * @time 2018.06.29 14:36 
     */
    @Override
    public TradePlate getTradePlate(ExchangeOrderDirection direction) {
        if (direction == ExchangeOrderDirection.BUY) {
            return this.buyTradePlate;
        } else if (direction == ExchangeOrderDirection.SELL) {
            return this.sellTradePlate;
        }
        return null;
    }

    /**
     * 设置盘口信息初始化状态
     *
     * @param flag
     */
    @Override
    public void isTradePlateinitialize(boolean flag) {
        this.isTradePlateinitialize = flag;
    }

    @Override
    public boolean isTradePlateinitialize() {
        return this.isTradePlateinitialize;
    }


    /**
     * 初始化实时K线
     */
    private void initializeRealtimeKline() {
        //初始化实时K线（按周期初始化）
        logger.info("-------初始化实时K线----------begin," + symbol);

        //按周期初始化k线数据
        realtimePeriodKlineMap = new HashMap<>(kLineService.getListPeriod().size());
        privPeriodKlineMap = new HashMap<>(kLineService.getListPeriod().size());

        kLineService.getListPeriod().forEach(period -> {
            long endTime = System.currentTimeMillis();
            long startTime = KLineService.getCurrentPeriodStartTime(endTime, period);
            KLine kLine = kLineService.getKlineFromTrade(this.symbol, period, startTime, endTime);

            // 处理初始化的K线数据为0的情况
            if (kLine.getHighestPrice().compareTo(BigDecimal.ZERO) == 0
                    && kLine.getLowestPrice().compareTo(BigDecimal.ZERO) == 0
                    && kLine.getOpenPrice().compareTo(BigDecimal.ZERO) == 0
                    && kLine.getClosePrice().compareTo(BigDecimal.ZERO) == 0) {
                KLine kLineTmp = kLineService.findLastKLine(this.symbol, period, endTime);
                if (null != kLineTmp) {
                    kLine.setOpenPrice(kLineTmp.getClosePrice());
                    kLine.setHighestPrice(kLineTmp.getClosePrice());
                    kLine.setLowestPrice(kLineTmp.getClosePrice());
                    kLine.setClosePrice(kLineTmp.getClosePrice());
                }
            }

            realtimePeriodKlineMap.put(period, kLine);
            logger.info("-------完成" + period + "K线的初始化----------" + symbol);
        });

        // 校正k线的收盘价为1分钟的的收盘价（解决不同周期的收盘价不一致的情况，长时间的停应用可能会导致该问题，因为查询的是最近一段时间的交易数据）
        String period1min = "1min";
        kLineService.getListPeriod().forEach(period -> {
            if (!period1min.equals(period)) {
                KLine kLine = realtimePeriodKlineMap.get(period);

                KLine kline1min = realtimePeriodKlineMap.get(period1min);
                if (kLine.getClosePrice().compareTo(kline1min.getClosePrice()) != 0
                        && kline1min.getClosePrice().compareTo(BigDecimal.ZERO) != 0) {
                    kLine.setClosePrice(kline1min.getClosePrice());
                    logger.warn("该周期的收盘价与1分钟k线收盘价不一致，校正该周期的收盘价，symbol=" + symbol + ",period=" + period);
                }
            }
        });

        logger.info("-------初始化实时K线----------end," + symbol + "\\r\\n\\n");
    }

    /**
     * 初始化缩略行情(依赖 初始化的日K线数据)
     */
    private void initializeThumb() {
        // 依赖 初始化的日K线数据
        logger.info("---- initializeThumb --- begin");
        String period = "1day";
        coinThumb = new CoinThumb();

        KLine kline = realtimePeriodKlineMap.get(period);
        if (null == kline) {
            long endTime = System.currentTimeMillis();
            long startTime = KLineService.getCurrentPeriodStartTime(endTime, period);
            // 实时获取当日K线数据
            kline = kLineService.getKlineFromTrade(this.symbol, period, startTime, endTime);
        }

        synchronized (coinThumb) {
            coinThumb.setSymbol(symbol);
            coinThumb.setOpen(kline.getOpenPrice());
            coinThumb.setHigh(kline.getHighestPrice());
            coinThumb.setLow(kline.getLowestPrice());
            coinThumb.setClose(kline.getClosePrice());
            //coinThumb.setVolume(coinThumb.getVolume().add(kline.getVolume()));          //24H成交量
            //coinThumb.setTurnover(coinThumb.getTurnover().add(kline.getTurnover()));    //24H成交额
            // 日涨幅量
            coinThumb.setChange(coinThumb.getClose().subtract(coinThumb.getOpen()));
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
                // 日涨幅比例
                coinThumb.setChg(coinThumb.getChange().divide(coinThumb.getOpen(), 4, RoundingMode.UP));
            }
        }

        // 异步更新24H成交量和成交额
        kLineService.asyncUpdate24HVolumeAndTurnover(coinThumb, this.symbol, System.currentTimeMillis(), true);

        logger.info("---- initializeThumb --- end");
    }

    /**
     * 初始化汇率
     */
    private void initializeUsdRate() {
        // 初始化汇率
        coinThumb.setBaseUsdRate(coinExchangeRate.getUsdRate(baseCoin));
        coinThumb.setUsdRate(coinThumb.getClose().multiply(coinExchangeRate.getUsdRate(baseCoin))
                .setScale(coinThumb.getClose().scale(), BigDecimal.ROUND_DOWN));
    }

    /**
     * 根据指定的周期创建k线
     *
     * @param period    k线周期
     * @param time      k线的开始时间
     * @param prevKLine 前一周期K线数据，可以为null
     *                   
     * @author yangch
     * @time 2018.07.20 11:46 
     */
    private KLine createNewKLine(final String period, final long time, final KLine prevKLine) {
        KLine newKLine = new KLine();
        //synchronized (newKLine) {
        newKLine.setSymbol(this.symbol);
        newKLine.setPeriod(period);
        newKLine.setCount(0);
        //设置k线的时间为当前周期
        newKLine.setTime(time);

        if (prevKLine != null) {
            // 前一周期的k线存在，则初始化当前k线的开盘价、收盘价、最高价、最低价为前一周去的收盘价
            newKLine.setOpenPrice(prevKLine.getClosePrice());
            newKLine.setClosePrice(prevKLine.getClosePrice());
            newKLine.setHighestPrice(prevKLine.getClosePrice());
            newKLine.setLowestPrice(prevKLine.getClosePrice());
        }
        //}
        return newKLine;
    }

    /**
     * 处理k线数据（通用）
     *
     * @param kLine
     * @param exchangeTrade
     */
    private void processKline(KLine kLine, final ExchangeTrade exchangeTrade) {
        if (kLine == null || exchangeTrade == null) {
            return;
        }

        synchronized (kLine) {
            if (kLine.getClosePrice().compareTo(BigDecimal.ZERO) == 0) {
                //第一次设置K线值
                kLine.setOpenPrice(exchangeTrade.getPrice());
                kLine.setHighestPrice(exchangeTrade.getPrice());
                kLine.setLowestPrice(exchangeTrade.getPrice());
                kLine.setClosePrice(exchangeTrade.getPrice());
            } else {
                //edit by yangch 时间： 2018.06.07 原因：K线切换后第一次成交 则修改k线数据
                if (kLine.getCount() == 0) {
                    kLine.setOpenPrice(exchangeTrade.getPrice());
                    kLine.setHighestPrice(exchangeTrade.getPrice());
                    kLine.setLowestPrice(exchangeTrade.getPrice());
                    kLine.setClosePrice(exchangeTrade.getPrice());
                } else {
                    kLine.setHighestPrice(exchangeTrade.getPrice().max(kLine.getHighestPrice()));
                    kLine.setLowestPrice(exchangeTrade.getPrice().min(kLine.getLowestPrice()));
                    kLine.setClosePrice(exchangeTrade.getPrice());
                }
            }

            //成交笔数
            kLine.setCount(kLine.getCount() + 1);
            //成交量
            kLine.setVolume(kLine.getVolume().add(exchangeTrade.getAmount()));
            BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount());
            //成交额
            kLine.setTurnover(kLine.getTurnover().add(turnover));
        }
    }

    /**
     * 处理CoinThumb
     *
     * @param exchangeTrade
     */
    private void handleThumb(final ExchangeTrade exchangeTrade) {
        logger.debug("handleThumb symbol = {}", this.symbol);
        synchronized (coinThumb) {
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) == 0) {
                //第一笔交易记为开盘价
                coinThumb.setOpen(exchangeTrade.getPrice());
            }
            coinThumb.setHigh(exchangeTrade.getPrice().max(coinThumb.getHigh()));

            //最低价
            if (coinThumb.getLow().compareTo(BigDecimal.ZERO) == 0) {
                coinThumb.setLow(exchangeTrade.getPrice());
            } else {
                coinThumb.setLow(exchangeTrade.getPrice().min(coinThumb.getLow()));
            }

            coinThumb.setClose(exchangeTrade.getPrice());
            coinThumb.setVolume(coinThumb.getVolume().add(exchangeTrade.getAmount()).setScale(8, RoundingMode.DOWN));
            //coinThumb.setVolume(coinThumb.getVolume().add(exchangeTrade.getAmount()).setScale(4, RoundingMode.UP));

            //BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount()).setScale(4, RoundingMode.UP);
            BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount()).setScale(8, RoundingMode.DOWN);
            coinThumb.setTurnover(coinThumb.getTurnover().add(turnover));

            //收盘价-开盘价
            BigDecimal change = coinThumb.getClose().subtract(coinThumb.getOpen());
            coinThumb.setChange(change);

            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
                coinThumb.setChg(change.divide(coinThumb.getOpen(), 4, BigDecimal.ROUND_UP));
            }

            //edit by yangch 时间： 2018.05.05 原因：合并代码
            if (baseCoin.equalsIgnoreCase("USDT")) {
                logger.info("setUsdRate", exchangeTrade.getPrice());
                coinThumb.setUsdRate(exchangeTrade.getPrice());
            } else {

            }
            coinThumb.setBaseUsdRate(coinExchangeRate.getUsdRate(baseCoin));
            coinThumb.setUsdRate(exchangeTrade.getPrice().multiply(coinExchangeRate.getUsdRate(baseCoin)
                    .setScale(coinThumb.getHigh().scale(), BigDecimal.ROUND_DOWN)));
            //logger.info("setUsdRate", exchangeTrade.getPrice().multiply(coinExchangeRate.getUsdRate(baseCoin)));
            //coinThumb.setUsdRate(coinExchangeRate.getUsdRate(baseCoin));

            logger.debug("thumb = {}", coinThumb);
        }
    }

    /**
     * 推送实时K线
     *
     * @param kLine  
     * @author yangch
     * @time 2018.06.07 13:45 
     */
    private void pushHandleKLine(final KLine kLine) {
        PushTradeMessage pushTradeMessage = SpringContextUtil.getBean(PushTradeMessage.class);
        pushTradeMessage.pushKLine(handlers, kLine);
    }

    /**
     * 推送所有周期的k线数据
     *
     * @param  
     * @author yangch
     * @time 2018.07.20 10:53 
     */
    private void pushAllPeriodHandleKLine() {
        PushTradeMessage pushTradeMessage = SpringContextUtil.getBean(PushTradeMessage.class);
        //推送所有周期的k线
        kLineService.getListPeriod().forEach(period -> {
            /// logger.info("-------推送"+period+"K线的数据----------"+symbol);
            pushTradeMessage.pushKLine(handlers, realtimePeriodKlineMap.get(period));
        });
    }
}
