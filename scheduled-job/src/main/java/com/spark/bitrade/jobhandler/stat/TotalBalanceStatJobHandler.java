package com.spark.bitrade.jobhandler.stat;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.CoinBase;
import com.spark.bitrade.entity.TotalBalanceStat;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.ITotalBalanceStatService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fumy
 * @time 2018.09.27 18:27
 */
@JobHandler(value = "totalBalanceStatJobHandler")
@Component
@Slf4j
public class TotalBalanceStatJobHandler extends IJobHandler {

    private Logger logger = LoggerFactory.getLogger(TotalBalanceStatJobHandler.class);

    private static volatile Map<String, Coin> coinConcurrentHashMap = new ConcurrentHashMap<String, Coin>();


    @Autowired
    private MemberWalletService memberWalletService;//add by tansitao 时间： 2018/5/4 原因：添加memberWalletService

    @Autowired
    private CoinService coinService;  //add tansitao 时间： 2018/5/4 原因：添加coinservice

    @Autowired
    private RestTemplate restTemplate; //add tansitao 时间： 2018/5/4 原因：添加restTemplate

    @Autowired
    private ITotalBalanceStatService totalBalanceStatService;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        //核心处理逻辑描述：
        //1.查询出实时总额数据
        //2.将查询的总额数据按日期录入数据库


        boolean isSucc = insertStat();
        if(isSucc){
            XxlJobLogger.log("总额查询当日统计数据生成完毕!>......................");
        }else {
            XxlJobLogger.log("总额查询当日统计数据生成失败!>......................");
        }
        return SUCCESS;
    }


    public boolean insertStat(){
        List<Coin> list = totalBalancePageQuery();
        TotalBalanceStat stat = null;
        Date date = new Date();
        int row =0;
        for(Coin coin:list){
            stat = new TotalBalanceStat();
            stat.setCreateTime(date);
            stat.setOpDate(DateUtil.getDate());
            stat.setNameCn(coin.getNameCn());
            stat.setName(coin.getName());
            stat.setUnit(coin.getUnit());
            stat.setAllBalance(coin.getAllBalance());
            stat.setHotAllBalance(coin.getHotAllBalance());
            stat.setCoinBaseAddress(coin.getCoinBaseAddress());
            stat.setCoinBaseBalance(coin.getCoinBaseBalance());
            stat.setColdWalletAddress(coin.getColdWalletAddress());
            row = totalBalanceStatService.inertNew(stat);
        }
        return row > 0 ? true : false;
    }



    private List<Coin> totalBalancePageQuery() {
        //edit by  shenzucai 时间： 2018.09.07  原因：将结果放在内存中，查询是直接返回内存中的数据，并做异步更新
        List<Coin> pageResult = coinService.findAll();
        if (coinConcurrentHashMap.isEmpty()) {
            for (Coin coin : pageResult) {
                String url = null;
                String coinUrl = null;
                coin.setAllBalance(memberWalletService.getAllBalance(coin.getName()));
                if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                    url = "http://SERVICE-RPC-ETH/rpc/balance?coinUnit=" + coin.getUnit();
                }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                    url = "http://SERVICE-RPC-SLU/rpc/balance?coinUnit=" + coin.getUnit();
                } else {
                    url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/balance";
                }

                coin.setHotAllBalance(getRPCWalletBalance(url, coin.getUnit()));

                if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                    coinUrl = "http://SERVICE-RPC-ETH/rpc/coinBase?coinUnit=" + coin.getUnit();
                }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                    coinUrl = "http://SERVICE-RPC-SLU/rpc/coinBase?coinUnit=" + coin.getUnit();
                } else {
                    coinUrl = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/coinBase";
                }


                CoinBase coinBase = getRPCWalletCoinBalance(coinUrl, coin);
                if (coinBase != null) {
                    coin.setCoinBaseBalance(coinBase.getBalance());
                    coin.setCoinBaseAddress(coinBase.getCoinBase());
                }
                coinConcurrentHashMap.put(coin.getUnit(),coin);
            }
        } else {
            for (String str : coinConcurrentHashMap.keySet()) {
                for (Coin coin : pageResult) {
                    if (str.equalsIgnoreCase(coin.getUnit())) {
                        coin.setAllBalance(coinConcurrentHashMap.get(str).getAllBalance());
                        coin.setHotAllBalance(coinConcurrentHashMap.get(str).getHotAllBalance());
                        coin.setCoinBaseBalance(coinConcurrentHashMap.get(str).getCoinBaseBalance());
                        coin.setCoinBaseAddress(coinConcurrentHashMap.get(str).getCoinBaseAddress());
                    }

                }
            }
            getService().getTotalBalance(pageResult);
            List<Coin> coins = new ArrayList();
            for(Coin coin:pageResult){
                if(coinConcurrentHashMap.containsKey(coin.getUnit())){
                    coins.add(coinConcurrentHashMap.get(coin.getUnit()));
                }
            }

        }
        return pageResult;
    }

    @Async
    public void getTotalBalance(List<Coin> coins){
        logger.info("开始更新数据：{}",coins);
        for (Coin coin : coins) {
            String url = null;
            String coinUrl = null;
            coin.setAllBalance(memberWalletService.getAllBalance(coin.getName()));
            if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                url = "http://SERVICE-RPC-ETH/rpc/balance?coinUnit=" + coin.getUnit();
            }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                url = "http://SERVICE-RPC-SLU/rpc/balance?coinUnit=" + coin.getUnit();
            } else {
                url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/balance";
            }

            coin.setHotAllBalance(getRPCWalletBalance(url, coin.getUnit()));

            if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                coinUrl = "http://SERVICE-RPC-ETH/rpc/coinBase?coinUnit=" + coin.getUnit();
            }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                coinUrl = "http://SERVICE-RPC-SLU/rpc/coinBase?coinUnit=" + coin.getUnit();
            } else {
                coinUrl = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/coinBase";
            }


            CoinBase coinBase = getRPCWalletCoinBalance(coinUrl, coin);
            if (coinBase != null) {
                coin.setCoinBaseBalance(coinBase.getBalance());
                coin.setCoinBaseAddress(coinBase.getCoinBase());
            }

            if(coinConcurrentHashMap.containsKey(coin.getUnit())){
                coinConcurrentHashMap.replace(coin.getUnit(),coin);
            }else{
                coinConcurrentHashMap.put(coin.getUnit(),coin);
            }

        }
        logger.info("更新数据：{}",coins);
    }


    private BigDecimal getRPCWalletBalance(String url, String unit) {
        try {
            //String url = "http://" + serviceName + "/rpc/address/{account}";
            ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
            log.info("result={}", result);
            if (result.getStatusCode().value() == 200) {
                MessageResult mr = result.getBody();
                if (mr.getCode() == 0) {
                    String balance = mr.getData().toString();
                    BigDecimal bigDecimal = new BigDecimal(balance) ;
                    log.info(unit + "热钱包余额:", bigDecimal);
                    return bigDecimal;
                }
            }
        }  catch (Exception e) {
            log.error("error={}", e);
            return new BigDecimal("0");
        }
        return new BigDecimal("0");
    }

    private CoinBase getRPCWalletCoinBalance(String url, Coin coin){
        CoinBase coinBase = null;
        try{
            ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
            log.info("--------test------------result={}",  result);
            if (result.getStatusCode().value() == 200) {
                logger.info(result.getBody().toString());
                MessageResult mr = result.getBody();
                if (mr.getCode() == 0) {
                    try {
                        logger.info("数据{}",mr.getData());
                        String str = JSONObject.toJSONString(mr.getData());
                        log.info("这是个什么鬼 {}",str);
                        coinBase = JSONObject.parseObject(str, CoinBase.class);
                    }catch (Exception e){
                        log.info("{}",e);
                        log.info("该币种{}没有coinbase",coin.getName());

                    }
                }

            }
        }
        catch (Exception e){

        }
        return coinBase;
    }

    private TotalBalanceStatJobHandler getService() {
        return SpringContextUtil.getBean(this.getClass());
    }
}
