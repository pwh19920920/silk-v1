//package com.spark.bitrade.jobhandler;
//
//import com.spark.bitrade.entity.OtcCoin;
//import com.spark.bitrade.exception.UnexpectedException;
//import com.spark.bitrade.service.AdvertiseService;
//import com.spark.bitrade.service.IOtcServerV2Service;
//import com.spark.bitrade.service.OtcCoinService;
//import com.spark.bitrade.util.MessageResult;
//import com.sparkframework.sql.DataException;
//import com.xxl.job.core.biz.model.ReturnT;
//import com.xxl.job.core.handler.IJobHandler;
//import com.xxl.job.core.handler.annotation.JobHandler;
//import com.xxl.job.core.log.XxlJobLogger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//
//del by ss 时间： 2020/04/05 原因：迁移到v2定时任务模块 schedule-job，类名为 autoPutOffShelvesAdvertiseHandler

/***
 * 自动下架C2C的广告 任务（从otc-api模块迁移过来）
  *  任务周期：每半个小时执行一次（0 30 * * * ? *）
 * @author yangch
 * @time 2018.07.26 14:45
 */
//
//@JobHandler(value="autoPutOffShelvesAdvertiseHandler")
//@Component
//public class AutoPutOffShelvesAdvertiseHandler extends IJobHandler {
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Autowired
//    private OtcCoinService otcCoinService;
//    @Autowired
//    private AdvertiseService advertiseService;
//    @Autowired
//    private IOtcServerV2Service iOtcServerV2Service;
//
//    @Override
//    public ReturnT<String> execute(String param) throws Exception {
//        //核心处理逻辑描述：
//
//        XxlJobLogger.log("=========开始检查自动下架的广告===========");
//        //支持的币种
//        List<OtcCoin> list = otcCoinService.getNormalCoin();
//        list.stream().forEach(
//                x -> {
//                    //BigDecimal marketPrice = map.get(x.getUnit());
//                    BigDecimal marketPrice = getMarketPrice( x.getUnit() ); //获取市场价
//                    try {
//                        List<Map<String, String>> list1 = advertiseService.selectSellAutoOffShelves(x.getId(), marketPrice, x.getJyRate());
//                        List<Map<String, String>> list2 = advertiseService.selectBuyAutoOffShelves(x.getId(), marketPrice);
//                        list1.addAll(list2);
//                        list1.stream().forEach(
//                                y -> {
//                                    try {
//                                        advertiseService.autoPutOffShelves(y, x);
//                                    } catch (UnexpectedException e) {
//                                        e.printStackTrace();
//                                        XxlJobLogger.log("{0}号广告:自动下架失败", y.get("id"));
//                                    }
//                                }
//                        );
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    } catch (DataException e) {
//                        e.printStackTrace();
//                    }
//                }
//        );
//        XxlJobLogger.log("=========结束检查自动下架的广告===========");
//
//        return SUCCESS;
//    }
//
//    //获取市场价
//    public BigDecimal getMarketPrice(String symbol){
//        String serviceName = "bitrade-market";
//        String url = "http://" + serviceName + "/market/exchange-rate/cny/{coin}";
//        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, symbol);
//        XxlJobLogger.log("remote call:service={0},result={1}", serviceName, result);
//
//        if(result.getStatusCode().value() == 200 && result.getBody().getCode() == 0){
//            BigDecimal rate =  new BigDecimal((String)result.getBody().getData());
//            return rate;
//        }
//
//        return BigDecimal.ZERO;
//    }
//}
