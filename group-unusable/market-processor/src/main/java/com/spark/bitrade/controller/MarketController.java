package com.spark.bitrade.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
public class MarketController {
    @Autowired
    private MarketService marketService;
    @Autowired
    private ExchangeCoinService coinService;
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private ExchangeTradeService exchangeTradeService;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;

    @Autowired
    private ExchangeOrderService exchangeOrderService;

    @Autowired
    private LatestTradeCacheService latestTradeCacheService;

    @Autowired
    private ExchangeMemberDiscountRuleService exchangeMemberDiscountRuleService;

    /**
     * 获取支持的交易币种
     * @return
     */
    @RequestMapping("symbol")
    public List<ExchangeCoin> findAllSymbol(){
        List<ExchangeCoin> coins = coinService.findAllEnabled();
        return coins;
    }

    @RequestMapping("overview")
    public Map<String,List<CoinThumb>> overview(){
        Map<String,List<CoinThumb>> result = new HashMap<>();
        List<ExchangeCoin> recommendCoin = coinService.findAllByFlag(1);
        List<CoinThumb> recommendThumbs = new ArrayList<>();
        for(ExchangeCoin coin:recommendCoin){
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            if(processor!= null) {
                CoinThumb thumb = processor.getThumb();
                recommendThumbs.add(thumb);
            }
        }
        result.put("recommend",recommendThumbs);
        List<CoinThumb> allThumbs = findSymbolThumb(null, null);
        Collections.sort(allThumbs, (o1, o2) -> o2.getChg().compareTo(o1.getChg()));
        int limit = allThumbs.size() > 10 ? 10 : allThumbs.size();
        result.put("changeRank",allThumbs.subList(0,limit));
        return result;
    }


    /**
     * 获取某交易对详情
     * @param symbol
     * @return
     */
    @RequestMapping("symbol-info")
    public ExchangeCoin findSymbol(String symbol){
        if(null !=symbol) symbol = symbol.toUpperCase();
        ExchangeCoin coin = coinService.findBySymbol(symbol);
        return coin;
    }

    /***
     * 获取币种缩略行情
      *
     * @author yangch
     * @time 2018.07.12 11:39 
      * @param displayArea 可选，0/MASTER=主区，1/INNOVATIVE=创新区
      * @param keyWord 可选，查询关键字
     */
    @RequestMapping("symbol-thumb")
    public List<CoinThumb> findSymbolThumb(@RequestParam(value = "displayArea",required = false) String displayArea,
                                           @RequestParam(value = "keyWord",required = false) String keyWord){
        List<ExchangeCoin> coins ;
        if("0".equalsIgnoreCase(displayArea) || ExchangeCoinDisplayArea.MASTER.name().equalsIgnoreCase(displayArea)) {
            coins = coinService.findAllByDisplayArea(ExchangeCoinDisplayArea.MASTER);   //查询主区
        } else if("1".equalsIgnoreCase(displayArea) || ExchangeCoinDisplayArea.INNOVATIVE.name().equalsIgnoreCase(displayArea)) {
            coins = coinService.findAllByDisplayArea(ExchangeCoinDisplayArea.INNOVATIVE); //查询创新区
        } else {
            coins = coinService.findAllEnabled(); //查询所有
        }

        List<CoinThumb> thumbs = new ArrayList<>();
        //edit by yangch 时间： 2018.07.12 原因：新增过滤的关键字
        coins.stream().filter( coin -> keyWord==null
                || coin.getSymbol().toUpperCase().indexOf(keyWord.toUpperCase()) != -1
                || coin.getSymbol().equalsIgnoreCase(keyWord.toUpperCase())).forEach(coin -> {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            if(null != processor) {
                CoinThumb thumb = processor.getThumb();
                if(null != thumb) {
                    thumbs.add(thumb);
                }
            }
        });
        /*
        //同上面的效果一样
        for(ExchangeCoin coin:coins){
            //edit by yangch 时间： 2018.07.12 原因：新增过滤的关键字
            if(keyWord==null || coin.getCoinSymbol().toUpperCase().indexOf(keyWord.toUpperCase()) != -1) {
                CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
                CoinThumb thumb = processor.getThumb();
                thumbs.add(thumb);
            }
        }*/
        return thumbs;
    }

    //edit tansitao 时间： 2018/5/31 原因：增加对币种的查询功能
    @RequestMapping("symbol-thumb-trend")
    public JSONArray findSymbolThumbWithTrend(String symbol){
        List<ExchangeCoin> coins = coinService.findAllEnabled();
        //List<CoinThumb> thumbs = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.set(Calendar.MINUTE,0);
        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY,-24);
        JSONArray array = new JSONArray();
        long firstTimeOfToday = calendar.getTimeInMillis();
        for(ExchangeCoin coin:coins){
            if(symbol == null || coin.getCoinSymbol().toLowerCase().indexOf(symbol.toLowerCase()) != -1) {
                CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
                if(null == processor) {
                    continue;
                }
                CoinThumb thumb = processor.getThumb();
                if(null == thumb){
                    continue;
                }
                JSONObject json = (JSONObject) JSON.toJSON(thumb);

                //edit by yangch 时间： 2018.06.28 原因：缓存优化
                /*List<KLine> lines = marketService.findAllKLine(thumb.getSymbol(),firstTimeOfToday,nowTime,"1hour");
                log.debug("time from {} to {} result = {}",firstTimeOfToday,nowTime,lines);
                JSONArray trend = new JSONArray();
                for(KLine line:lines){
                    trend.add(line.getClosePrice());
                }*/
                long endCacheTime =  toHourTime(nowTime);
                long periodCacheTime =endCacheTime-toHourTime(firstTimeOfToday);
                JSONArray trend =marketService.findSymbolThumbWithTrendCache(thumb.getSymbol(), firstTimeOfToday, nowTime, "1hour", endCacheTime, periodCacheTime); //24小时趋势
                json.put("trend",trend);
                array.add(json);
            }

        }
        return array;
    }

    /**
     * 获取币种历史K线
     * @param symbol
     * @param from
     * @param to
     * @param resolution
     * @return
     */
    @RequestMapping("history")
    public JSONArray findKHistory(String symbol,long from,long to,String resolution){
        if(null !=symbol) symbol = symbol.toUpperCase();
        long startTime = System.currentTimeMillis(); //add by yangch 时间： 2018.06.02 原因：耗时统计
        //根据动态的起止时间戳，计算相同周期内的缓存截至时间戳。相同周期内，相同的缓存截止时间戳内不重复查询mongodb库
        ///long endCacheTime =  toHourTime(to);                     //缓存截止时间:默认缓存时长为1小时
        ///long periodCacheTime =endCacheTime-toHourTime(from);     //查询周期，根据查询的起止时间计算查询周期

        String period = "";
        if(resolution.endsWith("H") || resolution.endsWith("h")){
            period = resolution.substring(0,resolution.length()-1) + "hour";
        } else if(resolution.endsWith("D") || resolution.endsWith("d")){
            period = resolution.substring(0,resolution.length()-1) + "day";
        } else if(resolution.endsWith("W") || resolution.endsWith("w")){
            period = resolution.substring(0,resolution.length()-1) + "week";
        } else if(resolution.endsWith("M") || resolution.endsWith("m")){
            period = resolution.substring(0,resolution.length()-1) + "month";
        } else{
            Integer val = Integer.parseInt(resolution);
            if(val < 60) {
                period = resolution + "min";

                ///endCacheTime = toMinuteTime(to, val); //缓存截止时间为1分钟
               /// periodCacheTime =endCacheTime-toMinuteTime(from, val);
            } else {
                period = (val/60) + "hour";
            }
        }

//        ///List<KLine> list = marketService.findAllKLine(symbol,from,to,period);
//        List<KLine> list = marketService.findAllKLineCache(symbol,from,to,period, endMinuteTime, periodTime);
//        //edit by yangch 时间： 2018.06.07 原因：如果是1min则添加当前实时的K线
//        //2018-06-08 添加以下代码会触发pc端k线频繁的访问该接口
//        /*//*if(period.equalsIgnoreCase("1min")){
//            list.add(coinProcessorFactory.getProcessor(symbol).getKLine());
//        }*/
//
//        JSONArray array = new JSONArray();
//        for(KLine item:list){
//            JSONArray group = new JSONArray();
//            group.add(0,item.getTime());
//            group.add(1,item.getOpenPrice());
//            group.add(2,item.getHighestPrice());
//            group.add(3,item.getLowestPrice());
//            group.add(4,item.getClosePrice());
//            group.add(5,item.getVolume());
//            array.add(group);
//        }

        long currentTime =  KLineService.getCurrentPeriodStartTime(new Date().getTime(), period);
        long endCacheTime =  KLineService.getCurrentPeriodStartTime(to, period);                  //缓存截止时间
        //long toTime = KLineService.getCurrentPeriodStartTime(to, period);
        long periodCacheTime =endCacheTime-KLineService.getCurrentPeriodStartTime(from, period);  //查询周期，根据查询的起止时间计算查询周期

        //获取历史K线数据
        JSONArray array = marketService.findKHistoryCache(symbol, from, to, period, endCacheTime, periodCacheTime);
        //System.out.println("---------时间"+ DateUtil.dateToString(new Date(from)) +","+ DateUtil.dateToString(new Date(to)));

        //处理最后一条K线数据为实时数据（但要考虑 查询的是不是最近时间段的K线，如PC端tradingview插件会 访问其他历史时间段的数据）
        //查询的提供的结束时候和系统当前时间一致的情况下，考虑添加最新的K线实时数据
        if(currentTime == endCacheTime) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(symbol);
            KLine currentKLine = processor.getKLine(period);
            if(currentKLine!=null) {
                //判断最后一条K线数据是否重复了，如果和实时的重复了 则删除
                if(array.size()>0) {
                    int lastInex = array.size() - 1;
                    long lastTime = array.getJSONArray(lastInex).getLong(0);
                    if (lastTime == currentKLine.getTime()) {
                        array.remove(lastInex);
                        log.warn("交易对={}，周期={}，时间={}， 历史K线数据和实时K线数据重复", symbol, period, String.valueOf(currentKLine.getTime()));
                    }
                }

                array.add(marketService.klineToJSONArray(currentKLine)); //添加最新的实时K线数据
            } else {
                log.warn("{}交易对中{}周期的实时K线数据不存在", symbol ,period);
            }
        }

        log.info("market history user times:{} ms",(System.currentTimeMillis()-startTime));
        return array;
    }

    //时间错精度保留到分钟，resolution为1小时内的1分、5分、15分、30分K线的标识
    private Long toMinuteTime(long time, int resolution){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        calendar.set(Calendar.MILLISECOND,0); //去掉毫秒
        calendar.set(Calendar.SECOND,0); //去掉秒
        if(resolution>0) {
            int currMinute = new Date(calendar.getTimeInMillis()).getMinutes()+1; //获取分钟
            calendar.set(Calendar.MINUTE, currMinute/resolution * resolution); //获取指定周期的开始时间
        }

        return calendar.getTimeInMillis();
    }
    //时间错精度保留到小时
    private Long toHourTime(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        calendar.set(Calendar.MILLISECOND,0); //去掉毫秒
        calendar.set(Calendar.SECOND,0); //去掉秒
        calendar.set(Calendar.MINUTE,0); //去掉分钟
        return calendar.getTimeInMillis();
    }

    /**
     * 查询最近成交记录
     * @param symbol 交易对符号
     * @param size 返回记录最大数量
     * @return
     */
    @RequestMapping("latest-trade")
    public List<ExchangeTrade> latestTrade(String symbol, int size){
        if(null !=symbol) symbol = symbol.toUpperCase();
        //数据本地缓存优化
        if(latestTradeCacheService.getCacheSize()>=size){
            List<ExchangeTrade> list = latestTradeCacheService.poll(symbol, size);
            if(null == list){
                list = exchangeTradeService.findLatest(symbol, size);
            }
            return list;
        } else {
            return exchangeTradeService.findLatest(symbol, size);
        }
    }

    /***
     * 从exchange实时获取盘口信息并初始化到market模块的缓存中
     * @author yangch
     * @time 2018.06.29 15:25 
       * @param symbol
     */
    private CoinProcessor getCoinProcessor(String symbol){
        if(null !=symbol) symbol = symbol.toUpperCase();
        CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
        if (coinProcessor != null && !coinProcessor.isTradePlateinitialize()) {
            //远程RPC服务URL,后缀为币种单位
            String serviceName = "SERVICE-EXCHANGE-TRADE";
            /*String url = "http://" + serviceName + "/monitor/realTimePlate?symbol="+symbol;
            ResponseEntity<HashMap> responseResult = restTemplate.getForEntity(url, HashMap.class);
            Map<String,TradePlate> result = (Map<String,TradePlate>) responseResult.getBody();

            //Map<String,TradePlate> result = exchangePlateService.realTimePlate(symbol);

            //初始化盘口信息
            coinProcessor.setTradePlate(result.get("bid"));
            coinProcessor.setTradePlate(result.get("ask"));*/

            String urlBuy = "http://" + serviceName + "/extrade/monitor/realTimePlate?symbol="+symbol+"&direction="+ExchangeOrderDirection.BUY;
            ResponseEntity<TradePlate> responseBuy = restTemplate.getForEntity(urlBuy, TradePlate.class);
            coinProcessor.setTradePlate(responseBuy.getBody());

            String urlSell = "http://" + serviceName + "/extrade/monitor/realTimePlate?symbol="+symbol+"&direction="+ExchangeOrderDirection.SELL;
            ResponseEntity<TradePlate> responseSell = restTemplate.getForEntity(urlSell, TradePlate.class);
            coinProcessor.setTradePlate(responseSell.getBody());

            coinProcessor.isTradePlateinitialize(true); //完成初始化
        }
        return coinProcessor;
    }

    @RequestMapping("exchange-plate")
    public Map<String,List<TradePlateItem>> findTradePlate(String symbol, Integer pageSize){
        if(null !=symbol) symbol = symbol.toUpperCase();
        CoinProcessor coinProcessor = getCoinProcessor(symbol);
        if (coinProcessor != null) {
            Map<String,List<TradePlateItem>> result = new HashMap<>();
            TradePlate buyTradePlate =coinProcessor.getTradePlate(ExchangeOrderDirection.BUY);
            TradePlate sellTradePlate =coinProcessor.getTradePlate(ExchangeOrderDirection.SELL);

            if(buyTradePlate!=null){
                LinkedList<TradePlateItem> buyItems;

                //获取指定数量的盘口信息
                if(pageSize!=null && pageSize>0){
                    buyItems = buyTradePlate.getItems(pageSize);
                }else{
                    buyItems = buyTradePlate.getItems();
                }

                if(!StringUtils.isEmpty(buyItems)) {
                    BigDecimal buyTotalAmountBuy= BigDecimal.ZERO;
                    for (int i =0, length= buyItems.size(); i <length ; i++) {
                        TradePlateItem item = buyItems.get(i);
                        buyTotalAmountBuy =buyTotalAmountBuy.add(item.getAmount()) ;
                        item.setTotalAmount(buyTotalAmountBuy);
                    }
                }
                result.put("bid",buyItems);
            }
            if(sellTradePlate!=null){
                LinkedList<TradePlateItem> sellItems = sellTradePlate.getItems();
                //获取指定数量的盘口信息
                if(pageSize!=null && pageSize>0){
                    sellItems = sellTradePlate.getItems(pageSize);
                }else{
                    sellItems = sellTradePlate.getItems();
                }
                if(!StringUtils.isEmpty(sellItems)) {
                    BigDecimal sellTotalAmount = BigDecimal.ZERO;
                    for (int i = 0, length = sellItems.size(); i < length; i++) {
                        TradePlateItem item = sellItems.get(i);
                        sellTotalAmount = sellTotalAmount.add(item.getAmount());
                        item.setTotalAmount(sellTotalAmount);
                    }
                }
                result.put("ask",sellItems);
            }
            return result;
        } else {
            return null;
        }

        //edit by yangch 时间： 2018.06.29 原因：修改为不从exchange获取数据，防止请求量大了应用exchange服务
        /*//远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-EXCHANGE-TRADE";
        String url = "http://" + serviceName + "/monitor/plate?symbol="+symbol;
        ResponseEntity<HashMap> result = restTemplate.getForEntity(url, HashMap.class);
        return (Map<String, List<TradePlateItem>>) result.getBody();*/
    }

   //add by yangch 时间： 2018.04.24 原因：合并新增
    @RequestMapping("exchange-plate-mini")
    public Map<String,JSONObject> findTradePlateMini(String symbol, Integer pageSize){
        if(null !=symbol) symbol = symbol.toUpperCase();
        CoinProcessor coinProcessor = getCoinProcessor(symbol);
        if (coinProcessor != null) {
            Map<String,JSONObject> result = new HashMap<>();
            TradePlate buyTradePlate =coinProcessor.getTradePlate(ExchangeOrderDirection.BUY);
            TradePlate sellTradePlate =coinProcessor.getTradePlate(ExchangeOrderDirection.SELL);

            if(buyTradePlate!=null){
                if(null != pageSize) {
                    result.put("bid", buyTradePlate.toJSON(pageSize));
                } else {
                    result.put("bid", buyTradePlate.toJSON(7));
                }
            }
            if(sellTradePlate!=null){
                if(null != pageSize) {
                    result.put("ask", sellTradePlate.toJSON(pageSize));
                } else {
                    result.put("ask", sellTradePlate.toJSON(7));
                }
            }
            return result;
        } else {
            return null;
        }

        //edit by yangch 时间： 2018.06.29 原因：修改为不从exchange获取数据，防止请求量大了应用exchange服务
        /*//远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-EXCHANGE-TRADE";
        String url = "http://" + serviceName + "/monitor/plate-mini?symbol="+symbol;
        ResponseEntity<HashMap> result = restTemplate.getForEntity(url, HashMap.class);
        return (Map<String, JSONObject>) result.getBody();*/
    }

    //add by yangch 时间： 2018.04.24 原因：合并新增
    @RequestMapping("exchange-plate-full")
    public Map<String,JSONObject> findTradePlateFull(String symbol, Integer pageSize){
        if(null !=symbol) symbol = symbol.toUpperCase();
        CoinProcessor coinProcessor = getCoinProcessor(symbol);
        if (coinProcessor != null) {
            Map<String,JSONObject> result = new HashMap<>();
            TradePlate buyTradePlate =coinProcessor.getTradePlate(ExchangeOrderDirection.BUY);
            TradePlate sellTradePlate =coinProcessor.getTradePlate(ExchangeOrderDirection.SELL);

            if(buyTradePlate!=null){
                if(null != pageSize) {
                    result.put("bid",buyTradePlate.toJSON(pageSize));
                } else {
                    result.put("bid",buyTradePlate.toJSON());
                }
            }
            if(sellTradePlate!=null){
                if(null != pageSize) {
                    result.put("ask", sellTradePlate.toJSON(pageSize));
                } else {
                    result.put("ask", sellTradePlate.toJSON());
                }
            }
            return result;
        } else {
            return null;
        }

        //edit by yangch 时间： 2018.06.29 原因：修改为不从exchange获取数据，防止请求量大了应用exchange服务
        /*
        //远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-EXCHANGE-TRADE";
        String url = "http://" + serviceName + "/monitor/plate-full?symbol="+symbol;
        ResponseEntity<HashMap> result = restTemplate.getForEntity(url, HashMap.class);
        return (Map<String,JSONObject>) result.getBody();*/
    }

    /***
     * 业务重做接口
     * 访问地址：/market/redo?id=
     * @author yangch
     * @time 2018.06.09 14:16 
       * @param id 异常业务ID
     */
    @RequestMapping("redo")
    //@Transactional(rollbackFor = Exception.class)  不能在此提供事务，方法中有try catch 会报 org.springframework.transaction.TransactionSystemException: Could not commit JPA transaction; nested exception is javax.persistence.RollbackException: Transaction marked as rollbackOnly
    public MessageResult redo(Long id){
        BusinessErrorMonitor businessErrorMonitor = businessErrorMonitorService.findOne(id);
        if(null == businessErrorMonitor){
            return MessageResult.error("指定的业务重做记录不存在");
        }

        //已处理则返回
        if(businessErrorMonitor.getMaintenanceStatus() == BooleanEnum.IS_TRUE){
            return MessageResult.success();
        }

        try {
            MessageResult result;
            if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE
                    || businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_BUY
                    || businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_SELL ) {
                //币币交易--撮单成功后成交明细处理错误
                //恢复实体
                ExchangeTrade trade = JSON.parseObject(businessErrorMonitor.getInData(), ExchangeTrade.class);
                if(null == trade){
                    return MessageResult.error("撮单成交明细参数为空");
                }
                if(businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE) {
                    result = exchangeOrderService.redoProcessExchangeTrade(trade);
                } else if(businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_BUY) {
                    result = exchangeOrderService.processExchangeTrade(trade, ExchangeOrderDirection.BUY);
                } else if(businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_SELL){
                    result = exchangeOrderService.processExchangeTrade(trade, ExchangeOrderDirection.SELL);
                } else{
                    result = MessageResult.error("重做类型错误");
                }
            } else if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__TRADE_COMPLETED) {
                //币币交易--撮单成功后订单完成处理出错
                //恢复实体
                ExchangeOrder order = JSON.parseObject(businessErrorMonitor.getInData(), ExchangeOrder.class);
                if(null == order) {
                    return MessageResult.error("订单参数为空");
                }

                //result = exchangeOrderService.tradeCompleted(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
                result = exchangeOrderService.tradeCompleted(order, BooleanEnum.IS_TRUE);
                if(result.getCode()!=0){
                    //判断订单状态为成功或撤单，则认为处理成功
                    ExchangeOrder orderNow = exchangeOrderService.findOne(order.getOrderId());
                    if( orderNow.getStatus() == ExchangeOrderStatus.CANCELED || orderNow.getStatus() == ExchangeOrderStatus.COMPLETED) {
                        result = MessageResult.success("订单状态发生变化，当前状态为"+orderNow.getStatus().name());
                    }
                }
            } else if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__ORDER_RETURN_BALANCE_FAIL) {
                //币币交易--归还订单余额失败
                //恢复实体
                ExchangeOrder order = JSON.parseObject(businessErrorMonitor.getInData(), ExchangeOrder.class);
                if(null == order) {
                    return MessageResult.error("订单参数为空");
                }
                result = exchangeOrderService.returnOrderBalance(order, BooleanEnum.IS_TRUE);

            } else {
                return MessageResult.error("该接口不支持该业务重做");
            }

            if(result.getCode() == 0 ) {
                //更改重做记录状态
                businessErrorMonitor.setMaintenanceStatus(BooleanEnum.IS_TRUE);
                businessErrorMonitor.setMaintenanceResult("业务重做成功");
                businessErrorMonitorService.save(businessErrorMonitor);

                return  result;
            } else {
                throw new Exception(result.getMessage());
            }
        }catch (Exception e){
            try {
                //保存重做记录错误信息
                businessErrorMonitor.setMaintenanceResult(e.getMessage());
                businessErrorMonitorService.update4NoRollback(businessErrorMonitor);
            }catch (Exception ex) { }
            return MessageResult.error("业务重做出错，错误信息："+e.getMessage());
        }
    }

    //获取交易队处理器的状态，访问地址：/market/processorStatus?symbol=SLB/USDT
    @RequestMapping("processorStatus")
    public Map<String, String> traderStatus(@RequestParam(value = "symbol",required = false) String symbol){
        Map<String, String> map = new HashMap<>();
        if(null != symbol) {
            CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol.toUpperCase());
            if(null != coinProcessor) {
                map.put(symbol, coinProcessor.isHalt()?"suspend":"running");
            } else {
                map.put(symbol, "--");
            }
        } else {
            Set<Map.Entry<String, CoinProcessor>> entrySet = coinProcessorFactory.getProcessorMap().entrySet();
            for (Map.Entry<String, CoinProcessor> entry: entrySet) {
                map.put(entry.getKey(), entry.getValue().isHalt()?"suspend":"running");
            }
        }
        return map;
    }

    //刷新会员优惠规则缓存，访问地址： /market/flushMemberDiscountRuleCache?memberId=0
    @RequestMapping("flushMemberDiscountRuleCache")
    public MessageResult flushMemberDiscountRuleCache(@RequestParam(value = "memberId",required = false) Long memberId){
        if(null == memberId) {
            exchangeMemberDiscountRuleService.flushCache();
            //return MessageResult.success("缓存数据", exchangeMemberDiscountRuleService.getMapDiscountRuleCache());
        } else{
            exchangeMemberDiscountRuleService.flushCache(memberId);
        }

        return MessageResult.success("更新成功");
    }

    //获取会员优惠规则缓存，访问地址： /market/getMemberDiscountRuleCache?memberId=0
    @RequestMapping("getMemberDiscountRuleCache")
    public MessageResult getMemberDiscountRuleCache(@RequestParam(value = "memberId",required = false) Long memberId){
        if(null == memberId) {
            return MessageResult.success("缓存数据", exchangeMemberDiscountRuleService.getMapDiscountRuleCache());
        } else {
            return MessageResult.success("缓存数据", exchangeMemberDiscountRuleService.getMapDiscountRuleCache(memberId));
        }
    }
}
