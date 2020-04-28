//package com.spark.bitrade.service;
//
//
//import com.spark.bitrade.entity.KLine;
//import com.spark.bitrade.entity.ExchangeTrade;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.Calendar;
//import java.util.List;
//
//@Service
//public class MarketService {
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    public List<KLine> findAllKLine(String symbol,String peroid){
//        //Criteria criteria = Criteria.where("period").is("1min"); del by yangch 2018-04-26 代码合并
//        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC,"time"));
//        Query query = new Query().with(sort).limit(1000);
//
//        return mongoTemplate.find(query,KLine.class,"exchange_kline_"+symbol+"_"+peroid);
//    }
//
//    public List<KLine> findAllKLine(String symbol,long fromTime,long toTime,String period){
//        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
//        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC,"time"));
//        Query query = new Query(criteria).with(sort);
//        //edit by yangch 时间： 2018.05.03 原因：代码合并
//        //return mongoTemplate.find(query,KLine.class,"exchange_kline_"+symbol+"_"+ period);
//        List<KLine> kLines = mongoTemplate.find(query,KLine.class,"exchange_kline_"+symbol+"_"+ period);
//        return kLines;
//    }
//    //add by yangch 时间： 2018.05.03 原因：代码合并
//    public ExchangeTrade findFirstTrade(String symbol,long fromTime,long toTime){
//        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
//        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC,"time"));
//        Query query = new Query(criteria).with(sort);
//        return mongoTemplate.findOne(query,ExchangeTrade.class,"exchange_trade_"+symbol);
//    }
//
//    //add by yangch 时间： 2018.05.03 原因：代码合并
//    public ExchangeTrade findLastTrade(String symbol,long fromTime,long toTime){
//        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
//        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC,"time"));
//        Query query = new Query(criteria).with(sort);
//        return mongoTemplate.findOne(query,ExchangeTrade.class,"exchange_trade_"+symbol);
//    }
//    //add by yangch 时间： 2018.05.03 原因：代码合并
//    public ExchangeTrade findTrade(String symbol, long fromTime, long toTime, Sort.Order order){
//        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
//        Sort sort = new Sort(order);
//        Query query = new Query(criteria).with(sort);
//        return mongoTemplate.findOne(query,ExchangeTrade.class,"exchange_trade_"+symbol);
//    }
//
//    public List<ExchangeTrade> findTradeByTimeRange(String symbol, long timeStart, long timeEnd){
//        Criteria criteria = Criteria.where("time").gte(timeStart).andOperator(Criteria.where("time").lt(timeEnd));
//        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC,"time"));
//        Query query = new Query(criteria).with(sort);
//
//        return mongoTemplate.find(query,ExchangeTrade.class,"exchange_trade_"+symbol);
//    }
//
//    public void saveKLine(String symbol,KLine kLine){
//        mongoTemplate.insert(kLine,"exchange_kline_"+symbol+"_"+kLine.getPeriod());
//    }
//
//    //add by yangch 时间： 2018.04.26 原因：合并新增
//    /**
//     * 查找某时间段内的交易量
//     * @param symbol
//     * @param timeStart
//     * @param timeEnd
//     * @return
//     */
//    public BigDecimal findTradeVolume(String symbol, long timeStart, long timeEnd){
//        Criteria criteria = Criteria.where("time").gt(timeStart)
//                .andOperator(Criteria.where("time").lte(timeEnd));
//                //.andOperator(Criteria.where("volume").gt(0));
//        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC,"time"));
//        Query query = new Query(criteria).with(sort);
//        List<KLine> kLines =  mongoTemplate.find(query,KLine.class,"exchange_kline_"+symbol+"_1min");
//        BigDecimal totalVolume = BigDecimal.ZERO;
//        for(KLine kLine:kLines){
//            totalVolume = totalVolume.add(kLine.getVolume());
//        }
//        return totalVolume;
//    }
//}
