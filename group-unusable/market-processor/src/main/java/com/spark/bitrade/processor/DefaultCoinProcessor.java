package com.spark.bitrade.processor;


import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.entity.TradePlate;
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
 */
@ToString
public class DefaultCoinProcessor implements CoinProcessor {
    private Logger logger = LoggerFactory.getLogger(DefaultCoinProcessor.class);
    /**
     * 当前未完成的K线
     */
    private String symbol;
    private String baseCoin;
    private Map<String,KLine> realtimePeriodKlineMap; //实时k线集合
    private List<MarketHandler> handlers;
    private CoinThumb coinThumb; //yangch 当前凌晨到当前的缩略行情
    //private KLine currentKLine; //yangch 最近一分钟k线

    //edit by yangch 时间： 2018.06.29 原因：添加盘口相关信息
    //标记盘口信息是否已初始化（未初始化则从exchange获取初始化的数据，已初始化则不再初始化，避免重复调用exchange应用）
    private boolean isTradePlateinitialize = false;
    //卖盘盘口信息
    private TradePlate sellTradePlate;
    //买盘盘口信息
    private TradePlate buyTradePlate;
    //是否暂时处理
    private Boolean isHalt = true;

    //服务层接口
    private MarketService marketService;
    private KLineService kLineService;
    private CoinExchangeRate coinExchangeRate;


    /***
     * 初始化
     * @author yangch
     * @time 2018.07.16 13:54 
     * @param symbol 交易对名称
     * @param baseCoin 交易基本符号名称
     */
    public DefaultCoinProcessor(String symbol,String baseCoin){
        handlers = new ArrayList<>();
        //createNewKLine(); //创建下1分钟k线(废弃，由initializeRealtimeKline()方法完成K线初始化 by 2018-07-21)
        this.baseCoin = baseCoin;
        this.symbol = symbol;
    }

    //初始化实时K线（按周期初始化）
    public void initializeRealtimeKline(){
        logger.info("-------初始化实时K线----------begin,"+symbol);
        //按周期初始化k线数据
        realtimePeriodKlineMap = new HashMap<>();
        kLineService.getListPeriod().forEach(period->{
            long endTime = System.currentTimeMillis();
            long startTime = KLineService.getCurrentPeriodStartTime(endTime, period);
            KLine kLine = kLineService.getKlineFromTrade(this.symbol, period, startTime, endTime);

            //处理初始化的K线数据为0的情况
            if(kLine.getHighestPrice().compareTo(BigDecimal.ZERO)==0
                    && kLine.getLowestPrice().compareTo(BigDecimal.ZERO)==0
                    && kLine.getOpenPrice().compareTo(BigDecimal.ZERO)==0
                    && kLine.getClosePrice().compareTo(BigDecimal.ZERO)==0) {
                KLine kLineTmp = kLineService.findLastKLine(this.symbol, period, endTime);
                if(null != kLineTmp) {
                    kLine.setOpenPrice(kLineTmp.getClosePrice());
                    kLine.setHighestPrice(kLineTmp.getClosePrice());
                    kLine.setLowestPrice(kLineTmp.getClosePrice());
                    kLine.setClosePrice(kLineTmp.getClosePrice());
                }
            }

            realtimePeriodKlineMap.put(period, kLine);
            logger.info("-------完成"+period+"K线的初始化----------"+symbol);
        });

        //校正k线的收盘价为1分钟的的收盘价（解决不同周期的收盘价不一致的情况，长时间的停应用可能会导致该问题，因为查询的是最近一段时间的交易数据）
        kLineService.getListPeriod().forEach(period->{
            if(!"1min".equals(period)) {
                KLine kLine = realtimePeriodKlineMap.get(period);

                KLine kline1min = realtimePeriodKlineMap.get("1min");
                if (kLine.getClosePrice().compareTo(kline1min.getClosePrice()) != 0
                        && kline1min.getClosePrice().compareTo(BigDecimal.ZERO) !=0) {
                    kLine.setClosePrice(kline1min.getClosePrice());
                    logger.warn("该周期的收盘价与1分钟k线收盘价不一致，校正该周期的收盘价，symbol="+symbol+",period=" + period );
                }
            }
        });

        logger.info("-------初始化实时K线----------end,"+symbol);
        System.out.println("\r\n\n");
    }

    //依赖 初始化的日K线数据
    public void initializeThumb(){
        //logger.info("initializeThumb from {} to {}",firstTimeOfToday,nowTime);
        logger.info("---- initializeThumb --- begin");
        String period = "1day";
        coinThumb = new CoinThumb();

        KLine kline =  realtimePeriodKlineMap.get(period);
        if(null == kline) {
            long endTime = System.currentTimeMillis();
            long startTime = KLineService.getCurrentPeriodStartTime(endTime, period);
            kline = kLineService.getKlineFromTrade(this.symbol,period, startTime, endTime); //实时获取当日K线数据
        }

        synchronized (coinThumb) {
            coinThumb.setSymbol(symbol);
            coinThumb.setOpen(kline.getOpenPrice());
            coinThumb.setHigh(kline.getHighestPrice());
            coinThumb.setLow(kline.getLowestPrice());
            coinThumb.setClose(kline.getClosePrice());
            //coinThumb.setVolume(coinThumb.getVolume().add(kline.getVolume()));          //24H成交量
            //coinThumb.setTurnover(coinThumb.getTurnover().add(kline.getTurnover()));    //24H成交额
            coinThumb.setChange(coinThumb.getClose().subtract(coinThumb.getOpen()));   //日涨幅量
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
                coinThumb.setChg(coinThumb.getChange().divide(coinThumb.getOpen(),4, RoundingMode.UP)); //日涨幅比例
            }
        }

        //异步更新24H成交量和成交额
        kLineService.asyncUpdate24HVolumeAndTurnover(coinThumb, this.symbol, System.currentTimeMillis(), true);

        logger.info("---- initializeThumb --- end");
    }


    @Override
    public void initializeUsdRate() {
        //edit by yangch 时间： 2018.05.05 原因：合并
        //coinThumb.setUsdRate(coinExchangeRate.getUsdRate(baseCoin));
        coinThumb.setBaseUsdRate(coinExchangeRate.getUsdRate(baseCoin));
        coinThumb.setUsdRate(coinThumb.getClose().multiply(coinExchangeRate.getUsdRate(baseCoin)));
    }


    /***
     * 根据指定的周期创建k线
     * @author yangch
     * @time 2018.07.20 11:46 
     * @param period k线周期
     * @param time k线的开始时间
     * @param prevKLine 前一周期K线数据，可以为null
     */
    public KLine createNewKLine(final String period, final long time, final KLine prevKLine){
        KLine newKLine = new KLine();
        //synchronized (newKLine) {
            newKLine.setSymbol(this.symbol);
            newKLine.setPeriod(period);
            newKLine.setCount(0);
            newKLine.setTime(time); //设置k线的时间为当前周期

            if(prevKLine!=null){
                //前一周期的k线存在，则初始化当前k线的开盘价、收盘价、最高价、最低价为前一周去的收盘价
                newKLine.setOpenPrice(prevKLine.getClosePrice());
                newKLine.setClosePrice(prevKLine.getClosePrice());
                newKLine.setHighestPrice(prevKLine.getClosePrice());
                newKLine.setLowestPrice(prevKLine.getClosePrice());
            }
        //}
        return newKLine;
    }


    //最近24小时的成交量
    @Override
    public void update24HVolume(final long time) {
        if(!isHalt) {
            //异步更新24H成交量和成交额
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
    public void resetThumb(){
        logger.info("reset coinThumb");
        synchronized (coinThumb){
            coinThumb.setOpen(BigDecimal.ZERO);
            coinThumb.setHigh(BigDecimal.ZERO);
            coinThumb.setLow(BigDecimal.ZERO);
            coinThumb.setLastDayClose(coinThumb.getClose()); //设置昨收价格
            //coinThumb.setClose(BigDecimal.ZERO); //del by yangch 时间： 2018.07.31 原因：用于表示最新的实时价，不清零
            coinThumb.setChg(BigDecimal.ZERO);
            coinThumb.setChange(BigDecimal.ZERO);

            //coinThumb.setVolume(BigDecimal.ZERO);   //成交量
            //coinThumb.setTurnover(BigDecimal.ZERO); //成交额
        }
    }

    //k线处理入口
    public void process(final List<ExchangeTrade> trades){
        if(!isHalt) {
            if (trades == null || trades.size() == 0) return ;

            //synchronized (currentKLine) {
            for (ExchangeTrade exchangeTrade : trades) {
                //处理K线
                ///processKline(currentKLine, exchangeTrade);

                //更新所有周期的k线
                kLineService.getListPeriod().forEach(period->{
                    //logger.info("-------更新"+period+"K线的数据----------"+symbol);
                    KLine kLine = realtimePeriodKlineMap.get(period);
                    //logger.info("更新前：" + kLine);
                    processKline(kLine, exchangeTrade);
                    pushHandleKLine(kLine);  //推送新的K线
                    //logger.info("更新后："+realtimePeriodKlineMap.get(period));
                });

                //处理今日概况信息，处理CoinThumb
                logger.debug("处理今日概况信息");
                handleThumb(exchangeTrade);

                //存储成交信息、推送CoinThumb（异步保存）
                handleTradeStorage(exchangeTrade);
            }
            //}

            //add by yangch 时间： 2018.06.07 原因：添加 推送1min实时k线
            ///pushHandleKLine(currentKLine);

            /////pushAllPeriodHandleKLine(); //推送所有k线的实时数据 一起推送可能存在粘包
        } else {
            logger.warn("{}交易对已停止", this.symbol);
        };
    }

    //yangch：处理k线数据（通用）
    public void processKline(KLine kLine,final ExchangeTrade exchangeTrade){
        if(kLine==null || exchangeTrade==null) return;

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

            kLine.setCount(kLine.getCount() + 1); //成交笔数
            kLine.setVolume(kLine.getVolume().add(exchangeTrade.getAmount())); //成交量
            BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount());
            kLine.setTurnover(kLine.getTurnover().add(turnover));   //成交额
        }
    }

    //处理CoinThumb
    public void handleThumb(final ExchangeTrade exchangeTrade){
        logger.debug("handleThumb symbol = {}", this.symbol);
        synchronized (coinThumb) {
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) == 0) {
                //第一笔交易记为开盘价
                coinThumb.setOpen(exchangeTrade.getPrice());
            }
            coinThumb.setHigh(exchangeTrade.getPrice().max(coinThumb.getHigh()));

            //最低价
            if(coinThumb.getLow().compareTo(BigDecimal.ZERO) == 0){
                coinThumb.setLow(exchangeTrade.getPrice());
            } else{
                coinThumb.setLow(exchangeTrade.getPrice().min(coinThumb.getLow()));
            }

            coinThumb.setClose(exchangeTrade.getPrice());
            coinThumb.setVolume(coinThumb.getVolume().add(exchangeTrade.getAmount()).setScale(8, RoundingMode.DOWN));
            //coinThumb.setVolume(coinThumb.getVolume().add(exchangeTrade.getAmount()).setScale(4, RoundingMode.UP));

            //BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount()).setScale(4, RoundingMode.UP);
            BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount()).setScale(8, RoundingMode.DOWN);
            coinThumb.setTurnover(coinThumb.getTurnover().add(turnover));

            BigDecimal change = coinThumb.getClose().subtract(coinThumb.getOpen()); //收盘价-开盘价
            coinThumb.setChange(change);

            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
                coinThumb.setChg(change.divide(coinThumb.getOpen(),4, BigDecimal.ROUND_UP));
            }

            //edit by yangch 时间： 2018.05.05 原因：合并代码
            if(baseCoin.equalsIgnoreCase("USDT")) {
                logger.info("setUsdRate", exchangeTrade.getPrice());
                coinThumb.setUsdRate(exchangeTrade.getPrice());
            } else {

            }
            coinThumb.setBaseUsdRate(coinExchangeRate.getUsdRate(baseCoin));
            coinThumb.setUsdRate(exchangeTrade.getPrice().multiply(coinExchangeRate.getUsdRate(baseCoin)));
            //logger.info("setUsdRate", exchangeTrade.getPrice().multiply(coinExchangeRate.getUsdRate(baseCoin)));
            //coinThumb.setUsdRate(coinExchangeRate.getUsdRate(baseCoin));

            logger.debug("thumb = {}",coinThumb);
        }
    }

    /***
     * 生成k线数据
     * @author yangch
     * @time 2018.07.16 17:07 
       * @param range k线周期范围 [1,5,10,15,30]min、[1]hour、[1]week、[1]day、[1]month
     * @param field k线周期类型 已知枚举选项：min=Calendar.MINUTE、hour=Calendar.HOUR_OF_DAY、week=Calendar.DAY_OF_WEEK、day=Calendar.DAY_OF_YEAR、month=Calendar.DAY_OF_MONTH
     * @param time k线时间
     */
    @Override
    public void generateKLine(final int range, final int field, final long time) {
        if(isHalt){
            return;
        }

        String rangeUnit = "";
        if(field == Calendar.MINUTE){
            rangeUnit = "min";
        } else if(field == Calendar.HOUR_OF_DAY){
            //传入 Calendar.HOUR_OF_DAY
            rangeUnit = "hour";
        } else if(field == Calendar.DAY_OF_WEEK){
            //传入 Calendar.DAY_OF_WEEK
            rangeUnit = "week";
        } else if(field == Calendar.DAY_OF_YEAR){
            //传入 Calendar.DAY_OF_YEAR
            rangeUnit = "day";
        } else if(field == Calendar.DAY_OF_MONTH){
            //} else if(field == Calendar.MONTH){ //同调用时传入的参数不一致，导致没有生成月k线
            //传入 Calendar.DAY_OF_MONTH
            rangeUnit = "month";
        }

        String period = range+rangeUnit;
        KLine pervKLine,newKLine;
        synchronized (realtimePeriodKlineMap) {
            pervKLine = realtimePeriodKlineMap.get(period);         //前一周期的k线
            newKLine = createNewKLine(period, time, pervKLine); //切换K线
            realtimePeriodKlineMap.put(period, newKLine);          //缓存新周期的K线数据
        }

        pushHandleKLine(newKLine);                             //推送新的K线
        kLineService.saveKLine(symbol,pervKLine);           //保存前一周期的k线数据
    }

    //生成1分钟k线（废弃 by 2018-07-21）
    public void autoGenerate(){
        /*DateFormat df = new SimpleDateFormat("HH:mm:ss");
        logger.info("auto generate 1min kline in {},data={}", df.format( new Date(currentKLine.getTime())),JSON.toJSONString(currentKLine));
        synchronized (currentKLine){
            //没有成交价时存储上一笔成交价
            if(currentKLine.getOpenPrice().compareTo(BigDecimal.ZERO) == 0){
                currentKLine.setOpenPrice(coinThumb.getClose());
                currentKLine.setLowestPrice(coinThumb.getClose());
                currentKLine.setHighestPrice(coinThumb.getClose());
                currentKLine.setClosePrice(coinThumb.getClose());
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            //currentKLine.setTime(calendar.getTimeInMillis()); //yangch 不能修改时间，否则存的数据就为t-1模式

            //edit by yangch 时间： 2018.06.07 原因：解决保存周期和推送周期一样了，导致前端出现“双线”问题
            *//*handleKLineStorage(currentKLine);
            createNewKLine();*//*
            //保存当前周期的K线（保存数据到mongodb库）
            for (MarketHandler storage : handlers) {
                if(storage instanceof MongoMarketHandler){
                    storage.handleKLine(symbol, currentKLine);
                }
            }

            createNewKLine();   //k线切换,创建下1分钟k线

            //推送下一周期的k线
            pushHandleKLine(currentKLine);
        }*/
    }


    //推送及存储接口（已分解该功能）
    /*public void handleKLineStorage(KLine kLine){
        for(MarketHandler storage: handlers){
            storage.handleKLine(symbol,kLine);
        }
    }*/
    //存储交易信息
    public void handleTradeStorage(final ExchangeTrade exchangeTrade){
        for(MarketHandler storage: handlers){
            storage.handleTrade(symbol, exchangeTrade,coinThumb);
        }
    }
    /***
      * 推送实时K线
      * @author yangch
      * @time 2018.06.07 13:45 
     * @param kLine
     */
    public void pushHandleKLine(final KLine kLine){
        /*for (MarketHandler storage : handlers) {
            if(!(storage instanceof MongoMarketHandler)){
                storage.handleKLine(symbol, currentKLine);
            }
        }*/

        //edit by yangch 时间： 2018.07.03 原因：修改为调用可监控性能的接口
        PushTradeMessage pushTradeMessage = SpringContextUtil.getBean(PushTradeMessage.class);
        pushTradeMessage.pushKLine(handlers, kLine);
    }
    /***
      * 推送所有周期的k线数据
      * @author yangch
      * @time 2018.07.20 10:53 
     * @param
     */
    public void pushAllPeriodHandleKLine(){
        //edit by yangch 时间： 2018.07.03 原因：修改为调用可监控性能的接口
        PushTradeMessage pushTradeMessage = SpringContextUtil.getBean(PushTradeMessage.class);
        //推送所有周期的k线
        kLineService.getListPeriod().forEach(period->{
            //logger.info("-------推送"+period+"K线的数据----------"+symbol);
            pushTradeMessage.pushKLine(handlers, realtimePeriodKlineMap.get(period));
        });
    }


    //setter getter
    @Override
    public void setExchangeRate(CoinExchangeRate coinExchangeRate) {
        this.coinExchangeRate = coinExchangeRate;
    }

    public void addHandler(MarketHandler storage) {
        handlers.add(storage);
    }

    @Override
    public void setIsHalt(boolean status) {
        this.isHalt = status;
    }

    @Override
    public boolean isHalt(){
        return this.isHalt;
    }

    @Override
    public void setMarketService(MarketService service) {
        this.marketService = service;
    }
    @Override
    public void setKLineService(KLineService kLineService){
        this.kLineService = kLineService;
    }

    public CoinThumb getThumb() {
        return coinThumb;
    }

    public String getSymbol(){
        return symbol;
    }

    /*@Override
    public KLine getKLine() {
        return currentKLine;
    }*/

    @Override
    public KLine getKLine(String period) {
        return  realtimePeriodKlineMap.get(period);
    }

    /***
     * 设置盘口信息
     * @author yangch
     * @time 2018.06.29 14:35 
     * @param tradePlate 盘口信息
     */
    public void setTradePlate(TradePlate tradePlate){
        if(tradePlate.getDirection() == ExchangeOrderDirection.BUY){
            this.buyTradePlate = tradePlate;
        } else if(tradePlate.getDirection() == ExchangeOrderDirection.SELL){
            this.sellTradePlate = tradePlate;
        }
    }
    /***
     * 获取盘口信息
     * @author yangch
     * @time 2018.06.29 14:36 
     * @param direction 买方方向
     */
    public TradePlate getTradePlate(ExchangeOrderDirection direction){
        if(direction == ExchangeOrderDirection.BUY){
            return this.buyTradePlate;
        } else if(direction == ExchangeOrderDirection.SELL){
            return this.sellTradePlate;
        }
        return null;
    }
    //设置盘口信息是否初始化
    public void isTradePlateinitialize(boolean flag){
        this.isTradePlateinitialize = flag;
    }
    public boolean isTradePlateinitialize(){
        return this.isTradePlateinitialize;
    }
}
