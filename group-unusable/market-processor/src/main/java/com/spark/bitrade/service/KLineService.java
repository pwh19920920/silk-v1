package com.spark.bitrade.service;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.KLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/***
 * K线服务接口
 * @author yangch
 * @time 2018.07.16 17:28
 */

@Slf4j
@Service
public class KLineService {
    @Autowired
    private MongoTemplate mongoTemplate;

    private static List<String> listPeriod = new ArrayList<>();
    static {
        listPeriod.add("1min"); //1分钟K线
        listPeriod.add("5min"); //5分钟K线
        //listPeriod.add("10min"); //10分钟K线
        listPeriod.add("15min"); //15分钟K线
        listPeriod.add("30min"); //30分钟K线
        listPeriod.add("1hour"); //1小时K线
        listPeriod.add("1day"); //日K线
        listPeriod.add("1week"); //周k线
        listPeriod.add("1month"); //月k线
    }

    /***
     * 获取K线周期列表
     * @author yangch
     * @time 2018.07.16 17:33 
     * @param
     */
    public List<String> getListPeriod(){
        return listPeriod;
    }

    /**
     * 返回指定周期在指定时间戳中时间标志（指定时间在指定周期的开始时间）
     * @param time 处理的时间错
     * @param period 周期  [1,5,10,15,30]min、[1]hour、[1]week、[1]day、[1]month
     * @return 指定时间的开始时间戳
     */
    public static Long getCurrentPeriodStartTime(long time, String period){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        //分：将秒、微秒字段置为0
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);

        //分钟
        if(period.endsWith("min")) {
            int resolution = Integer.parseInt(period.replace("min",""));
            if(resolution==1){
                return calendar.getTimeInMillis(); //1分钟时间不用处理
            } else if (resolution > 1) {
                int currMinute = new Date(calendar.getTimeInMillis()).getMinutes() + 1; //获取分钟
                calendar.set(Calendar.MINUTE, currMinute / resolution * resolution); //获取指定周期的开始时间
                return calendar.getTimeInMillis();
            }
        } else if(period.endsWith("hour")){
            calendar.set(Calendar.MINUTE,0); //设置分钟为0
            return calendar.getTimeInMillis();
        } else {
            calendar.set(Calendar.MINUTE,0); //设置分钟为0
            calendar.set(Calendar.HOUR_OF_DAY,0); //设置小时为0

            if(period.endsWith("day")){ //天，凌晨
                return calendar.getTimeInMillis();
            } else if(period.endsWith("week")){ //周
                calendar.set(Calendar.DAY_OF_WEEK,1); //周，周第一天
                return calendar.getTimeInMillis();
            } else if(period.endsWith("month")){ //月，月第一天
                calendar.set(Calendar.DAY_OF_MONTH,1);
                return calendar.getTimeInMillis();
            }
        }

        log.warn("未处理周期{}，时间：{}",period ,time);
        return time;
    }

    /***
     * K线数据统计（统计开盘价、收盘价、最高价、最低价、交易笔数）
     * @author yangch
     * @time 2018.07.19 9:54 
     * @param symbol 交易对
     * @param fromTime 开始时间（大于）
     * @param toTime 结束时间（小于等于）
     * @return Map<String, Double> key=openPrice(开盘价)/highestPrice(最高价)/lowestPrice(最低价)/closePrice(收盘价)/count(交易笔数)
     */
    public Map<String, BigDecimal> getKLineBaseData(String symbol,long fromTime,long toTime){
        //备注：因为“exchange_trade_交易对”中的都为字符类型，无法做sum统计
        //String commond ="db.runCommand( { " +
        String command ="\n{ \n" +
                        "   aggregate:'exchange_trade_"+symbol+"', \n" +
                        "   pipeline: [ \n" +
                        "      { $match : {'time' : { '$gt' : "+fromTime+", '$lte' : "+toTime+" } } }, \n" + //1529763101246  1529778088203
                        "      { $sort: { time: 1 }},\n"+
                        "      { $group : { '_id': 1 \n" +
                        "         ,'openPrice'  :{'$first':'$price'} \n" + //开盘价
                        "         ,'highestPrice': {'$max':'$price'} \n" + //最高价
                        "         ,'lowestPrice':{'$min':'$price'} \n" + //最低价
                        "         ,'closePrice':{'$last':'$price'} \n" + //收盘价
                        "         ,'count': {'$sum': 1 } \n" + //交易笔数
                        "       } } \n" +
                        "   ]\n" +
                        "   ,'cursor': { } \n" +
                        "} \n";
               // "} )";
        log.info("{} command : {} ", symbol, command);
        CommandResult result = mongoTemplate.executeCommand(command);
        //System.out.println("aggregate:"+result.toString()); //aggregate:{ "cursor" : { "firstBatch" : [ { "_id" :  null  , "openPrice" : "3.00000000" , "highestPrice" : "3.00000000" , "lowestPrice" : "2" , "closePrice" : "3" , "count" : 30}] , "id" : 0 , "ns" : "bitrade.exchange_trade_DOGE/USDT"} , "ok" : 1.0}
        log.info("{} aggregate结果：{}", symbol, result);

        //结果解析
        Map<String, String> mapTemp = new HashMap();
        if(result != null) {
            Map mapResult = result.toMap();
            if(mapResult!=null) {
                BasicDBObject mapCursor = (BasicDBObject) mapResult.get("cursor");
                if(mapCursor!=null) {
                    BasicDBList listFirstBatch = (BasicDBList) mapCursor.get("firstBatch");
                    if(listFirstBatch!=null) {
                        listFirstBatch.forEach(obj -> {
                            BasicDBObject objTmp = (BasicDBObject) obj;
                            mapTemp.putAll(objTmp.toMap());
                        });
                    } else {
                        log.warn("listFirstBatch is null");
                    }
                } else {
                    log.warn("mapCursor is null");
                }
            } else {
                log.warn("mapResult is null");
            }
        }else{
            log.warn("result is null");
        }

        //数据类型转换
        Map<String, BigDecimal> map = new HashMap();
        map.put("openPrice", new BigDecimal(mapTemp.getOrDefault("openPrice" ,"0")).setScale(8, BigDecimal.ROUND_DOWN) ); //mapTemp 返回的字符串类型
        map.put("highestPrice", new BigDecimal(mapTemp.getOrDefault("highestPrice" ,"0")).setScale(8, BigDecimal.ROUND_DOWN) );
        map.put("lowestPrice", new BigDecimal(mapTemp.getOrDefault("lowestPrice" ,"0")).setScale(8, BigDecimal.ROUND_DOWN) );
        map.put("closePrice", new BigDecimal(mapTemp.getOrDefault("closePrice" ,"0")).setScale(8, BigDecimal.ROUND_DOWN) );
        //System.out.println("------======="+mapTemp.get("count"));
        //map.put("count", new BigDecimal(mapTemp.get("count")==null?"0": String.valueOf(mapTemp.get("count"))) ); //返回的是integer类型

        log.info("{}解析结果：openPrice={},highestPrice={},lowestPrice={},closePrice={},count={}"
                , symbol, map.get("openPrice"), map.get("highestPrice")
                , map.get("lowestPrice"), map.get("closePrice") ,map.get("count"));
        /*System.out.println("openPrice="+map.get("openPrice")
                +",highestPrice="+map.get("highestPrice")
                +",lowestPrice="+map.get("lowestPrice")
                +",closePrice="+map.get("closePrice")
                +",count="+map.get("count"));*/
        return map;
    }

    /***
     * K线数据统计（统计交易量和交易额）
     * @author yangch
     * @time 2018.07.19 9:51 
     *
     * @param symbol 交易对
     * @param fromTime 开始时间（大于）
     * @param toTime 结束时间（小于等于）
     * @return   Map<String,Double> ,key=amount(总交易量)/turnover(总交易额)/count=(交易笔数)
     */
    public Map<String,BigDecimal> getKLinExtendData(String symbol,long fromTime,long toTime){
        //备注：因为“exchange_trade_交易对”中的都为字符类型，无法做sum统计
        String mapJsonStr = " function (){ \n" +
                            "      emit('sum',{'amount':parseFloat(this.amount), 'turnover':parseFloat(this.price)*parseFloat(this.amount), count:1}); \n" +
                            " }";
        String redureJsonStr = " function (key,values){ \n" +
                                "      var res={amount:0, turnover:0, count:1}; \n" +
                                "      values.forEach(function(val){ \n" +
                                "          res.amount += val.amount; \n" +
                                "          res.turnover += val.turnover; \n" +
                                "          res.count += val.count; \n" +
                                "      }); \n" +
                                "      return res; \n" +
                                " }";
        String command = "\n{ \n" +
                        "   mapReduce: 'exchange_trade_"+symbol+"', \n" +
                        "   map:\""+mapJsonStr+"\", \n" +
                        "   reduce: \""+redureJsonStr+"\", \n" +
                        "   out: {inline: 1}, \n" +
                        "   query: { time : { $gt : "+fromTime+", $lte : "+toTime+" } }  \n" +
                        "} \n";
        log.info("{} command : {} ", symbol, command);

        //方案1
        CommandResult result = mongoTemplate.executeCommand(command);
        //System.out.println("mapredure::"+result.toString()); //mapredure::{ "results" : [ { "_id" : "sum" , "value" : { "amount" : 148.0 , "turnover" : 318.0 , "count" : 9.0}}] , "timeMillis" : 20 , "counts" : { "input" : 9 , "emit" : 9 , "reduce" : 1 , "output" : 1} , "ok" : 1.0}
        log.info("{} mapredure结果：{}",symbol, result);

        Map<String,BigDecimal> map = new HashMap();
        if(result != null) {
            BasicDBList resultsList = (BasicDBList) result.get("results");
            if(resultsList != null && resultsList.size() >0 ) {
                BasicDBObject basicDBObject = (BasicDBObject) resultsList.get(0);
                if(basicDBObject != null) {
                    BasicDBObject basicDBObjectValue = (BasicDBObject) basicDBObject.get("value");
                    if(basicDBObjectValue != null) {
                        map.put("amount", new BigDecimal(basicDBObjectValue.getDouble("amount", 0)).setScale(8, BigDecimal.ROUND_DOWN) );
                        map.put("turnover", new BigDecimal(basicDBObjectValue.getDouble("turnover", 0)).setScale(8, BigDecimal.ROUND_DOWN));
                        map.put("count", new BigDecimal(basicDBObjectValue.getDouble("count", 0)).setScale(0, BigDecimal.ROUND_DOWN));
                        //map.putAll(basicDBObjectValue.toMap());
                    } else {
                        log.warn("basicDBObjectValue is null");
                    }
                } else {
                    log.warn("basicDBObject is null");
                }
            } else {
                log.warn("resultsList is null");
            }
        } else {
            log.warn("result is null");
        }
        log.info("{}解析结果：amount={},turnover={},count={}", symbol,
                map.getOrDefault("amount", BigDecimal.ZERO), map.getOrDefault("turnover", BigDecimal.ZERO), map.getOrDefault("count", BigDecimal.ZERO));
        //System.out.println("amount="+map.get("amount")  +",turnover="+map.get("turnover"));

        //方案2
//        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
//        Query query = new Query(criteria);
//        MapReduceResults<Map> results = mongoTemplate.mapReduce(query,"exchange_trade_DOGE/USDT", map, redure, Map.class);
//        System.out.println("mapredure::"+results.iterator());
//        BasicDBList list = (BasicDBList)results.getRawResults().get("results");
//        for (int i = 0; i < list.size(); i ++) {
//            BasicDBObject obj = (BasicDBObject)list.get(i);
//            System.out.println(obj.toString());
//        }

        return map;
    }

    /***
      * 统计交易量和成交额
      * @author yangch
      * @time 2018.07.19 9:51 
     *
     * @param symbol 交易对
     * @param period 周期
     * @param fromTime 开始时间（大于）
     * @param toTime 结束时间（小于等于）
     * @param isDebug 是否打印调试日志
     * @return   Map<String,Double> ,key=volume(总交易量)/turnover（总成交额）
     */
    public Map<String,BigDecimal> getStatsFromKLine(final String symbol , final String period, final long fromTime, final long toTime, final boolean isDebug){
        //备注：因为“exchange_kline_交易对_1min”中的都为字符类型，无法做sum统计
        String mapJsonStr = " function (){ \n" +
                "      emit('sum',{'volume':parseFloat(this.volume), 'turnover':parseFloat(this.turnover)}); \n" +
                " }";
        String redureJsonStr = " function (key,values){ \n" +
                "      var res={volume:0, turnover:0}; \n" +
                "      values.forEach(function(val){ \n" +
                "          res.volume += val.volume; \n" +
                "          res.turnover += val.turnover; \n" +
                "      }); \n" +
                "      return res; \n" +
                " }";
        String command = "\n{ \n" +
                "   mapReduce: 'exchange_kline_"+symbol+"_"+period+"', \n" +
                "   map:\""+mapJsonStr+"\", \n" +
                "   reduce: \""+redureJsonStr+"\", \n" +
                "   out: {inline: 1}, \n" +
                "   query: { time : { $gt : "+fromTime+", $lte : "+toTime+" } }  \n" +
                "} \n";
        if(isDebug) {
            log.info("{} command : {} ", symbol, command);
        }

        //方案1
        CommandResult result = mongoTemplate.executeCommand(command);
        //System.out.println("mapredure::"+result.toString()); //mapredure::{ "results" : [ { "_id" : "sum" , "value" : { "volume" : 148.0 , "turnover" : 318.0 }}] , "timeMillis" : 20 , "counts" : { "input" : 9 , "emit" : 9 , "reduce" : 1 , "output" : 1} , "ok" : 1.0}
        if(isDebug) {
            log.info("{} mapredure结果：{}", symbol, result);
        }

        Map<String,BigDecimal> map = new HashMap();
        if(result != null) {
            BasicDBList resultsList = (BasicDBList) result.get("results");
            if(resultsList != null && resultsList.size() >0 ) {
                BasicDBObject basicDBObject = (BasicDBObject) resultsList.get(0);
                if(basicDBObject != null) {
                    BasicDBObject basicDBObjectValue = (BasicDBObject) basicDBObject.get("value");
                    if(basicDBObjectValue != null) {
                        map.put("volume", new BigDecimal(basicDBObjectValue.getDouble("volume", 0)).setScale(8, BigDecimal.ROUND_DOWN) );
                        map.put("turnover", new BigDecimal(basicDBObjectValue.getDouble("turnover", 0)).setScale(8, BigDecimal.ROUND_DOWN));
                        //map.put("count", new BigDecimal(basicDBObjectValue.getDouble("count", 0)).setScale(0, BigDecimal.ROUND_DOWN));
                        //map.putAll(basicDBObjectValue.toMap());
                    } else {
                        log.warn("basicDBObjectValue is null");
                    }
                } else {
                    log.warn("basicDBObject is null");
                }
            } else {
                log.warn("resultsList is null");
            }
        } else {
            log.warn("result is null");
        }
        if(isDebug) {
            log.info("{}解析结果：amount={},turnover={}", symbol,
                    map.getOrDefault("volume", BigDecimal.ZERO), map.getOrDefault("turnover", BigDecimal.ZERO));
            //System.out.println("amount="+map.get("amount")  +",turnover="+map.get("turnover"));
        }

        return map;
    }

    /***
      * 指定时间段内的最后一条K线
      * @author yangch
      * @time 2018.07.17 11:43 
     * @param symbol 交易对
     * @param fromTime 开始时间
     * @param toTime 截止时间戳
     */
    public KLine findLastKLine(String symbol, final String period, long fromTime, long toTime){
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC,"time"));
        Query query = new Query(criteria).with(sort);
        return mongoTemplate.findOne(query,KLine.class,"exchange_kline_"+symbol+"_"+period);
    }
    /***
     * 指定时间段内的最后一条K线（尽可能的查询到最后一条K线）
     * @author yangch
     * @time 2018.07.26 17:33 
     * @param symbol
     * @param period
     * @param toTime
     */
    public KLine findLastKLine(String symbol , final String period, long toTime) {
        //注：一般为应用升级或者重启服务时，存在业务的中段，用于恢复k线数据
        Long fromTime =0L;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(toTime);
        if(period.endsWith("min") || period.endsWith("hour")){
            calendar.add(Calendar.HOUR_OF_DAY, - 6); //往前推5个小时
        } else{
            calendar.add(Calendar.DAY_OF_YEAR, - 5); //往前推5天
        }

        fromTime = calendar.getTimeInMillis();
        return findLastKLine(symbol, period, fromTime, toTime);
    }

    /**
     * 异步更新24消息交易量和成交额
     * @param coinThumb
     * @param symbol
     * @param time
     * @param isDebug 是否打印调试日志
     */
    @Async
    public void asyncUpdate24HVolumeAndTurnover(CoinThumb coinThumb, final String symbol, final long time, final boolean isDebug){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.HOUR_OF_DAY,-24);
        long fromTime = calendar.getTimeInMillis();

        Map<String,BigDecimal>  mapData =getStatsFromKLine( symbol, "1min", fromTime, time, isDebug);

        //更新成交量和成交额
        synchronized (coinThumb){
            coinThumb.setVolume(mapData.getOrDefault("volume",BigDecimal.ZERO));
            coinThumb.setTurnover(mapData.getOrDefault("turnover",BigDecimal.ZERO));
        }
    }

    /***
     * 获取k线数据，根据交易数据生成指定时间段的k线数据
     * @author yangch
     * @time 2018.07.19 11:10 
       * @param symbol 交易对
     * @param period k线周期
     * @param fromTime 开始时间（大于）
     * @param toTime 结束时间（小于等于）
     */
    public KLine getKlineFromTrade(final String symbol, final String period, final long fromTime, final long toTime){
        Map<String, BigDecimal> baseData = getKLineBaseData( symbol, fromTime, toTime);
        Map<String,BigDecimal>  extendData =getKLinExtendData( symbol, fromTime, toTime);

        KLine kLine = new KLine();
        kLine.setSymbol(symbol);
        kLine.setOpenPrice(baseData.getOrDefault("openPrice",BigDecimal.ZERO));
        kLine.setHighestPrice(baseData.getOrDefault("highestPrice",BigDecimal.ZERO));
        kLine.setLowestPrice(baseData.getOrDefault("lowestPrice",BigDecimal.ZERO));
        kLine.setClosePrice(baseData.getOrDefault("closePrice",BigDecimal.ZERO));
        //kLine.setCount(baseData.getOrDefault("count",BigDecimal.ZERO).toBigInteger().intValue());
        kLine.setCount(extendData.getOrDefault("count",BigDecimal.ZERO).toBigInteger().intValue());
        kLine.setVolume(extendData.getOrDefault("amount",BigDecimal.ZERO));
        kLine.setTurnover(extendData.getOrDefault("turnover",BigDecimal.ZERO));
        kLine.setPeriod(period);
        kLine.setTime(fromTime);

        return kLine;
    }

    /**
     * 查询k线是否存在
     *
     * @param symbol 交易对
     * @param period k线周期
     * @param time k线时间
     * @return 是否存在
     */
    public boolean existsKlineByTime(String symbol,String period, long time) {
        Criteria criteria = Criteria.where("time").is(time);
        Query query = new Query(criteria);
        return mongoTemplate.exists(query,"exchange_kline_"+symbol+"_"+period );
        //return mongoTemplate.findOne(query,KLine.class,"exchange_kline_"+symbol+"_"+period );
    }

    /***
      * 保存k线(异步保存K线数据)
      * @author yangch
      * @time 2018.07.16 14:17 
     * @param symbol 交易对名称
     * @param kLine k线数据
     */
    @Async
    public void saveKLine(final String symbol,final KLine kLine){
        if(kLine==null || StringUtils.isEmpty(symbol)) {
            return;
        }
        //一般为重启该模块后k线数据为空（抽查判断即可，不一定要判断完所有数据都为0）
        if(kLine.getHighestPrice().compareTo(BigDecimal.ZERO)==0
                && kLine.getLowestPrice().compareTo(BigDecimal.ZERO)==0
                && kLine.getCount()==0) {
            log.warn("未保存该k线数据，kline={}", kLine);
            return;
        }

        //MongoMarketHandler.handleKLine()方法功能一样
        //K线不存在则生产K线（排除1min中，因为太频繁了会影响性能）
        if( kLine.getPeriod().equalsIgnoreCase("1min") || !existsKlineByTime(symbol, kLine.getPeriod(), kLine.getTime()) ) {
            mongoTemplate.insert(kLine, "exchange_kline_" + symbol + "_" + kLine.getPeriod());
        }
    }
}
