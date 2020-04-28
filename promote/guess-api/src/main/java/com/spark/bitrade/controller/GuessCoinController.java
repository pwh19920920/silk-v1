package com.spark.bitrade.controller;

import com.spark.bitrade.entity.GuessCoin;
import com.spark.bitrade.processor.CoinExchangeRateService;
import com.spark.bitrade.service.GuessCoinService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 竞猜币种控制类
 * @author Zhang Yanjun
 * @time 2018.09.12 17:33
 */
@RestController
@RequestMapping("guessCoin")
public class GuessCoinController extends BaseController {
    @Autowired
    private GuessCoinService guessCoinService;
    @Autowired
    private CoinExchangeRateService coinExchangeRateService;

    /**
     * 获取游戏币种信息
     * @author Zhang Yanjun
     * @time 2018.09.13 9:37
     * @param id  非小号币种id（如：bitcoin）
     */
    @GetMapping("getGuessCoin")
    public MessageResult dataOfGuessCoin(String id){
        if(id!=null){
            if("BTC".equalsIgnoreCase(id)){
                id = "Bitcoin";
            } else if("BTMC".equalsIgnoreCase(id)){
                id = "BTMC";
            }
        }

        GuessCoin guessCoin=guessCoinService.getGuessCoin(id);
//        Date date=DateUtil.stringToDate(guessCoin.getLastRedisTime());//更新时间为45s
//        if (DateUtil.diffMinute(date).compareTo(new BigDecimal(1)) == 1){//最后缓存时间与当前时间差大于1分钟
//            guessCoinService.flushGuessCoin(id);//删除缓存
//            guessCoin=guessCoinService.getGuessCoin(id);//重新存入缓存
//        }
        return success(guessCoin);
    }

    /**
     * 币种汇率转化
     * @param sourceAmount 转化前的数目
     * @param sourceCoinUnit 转换前的币种简写名称
     * @param targetCoinUnit 转换后的币种简写名称
     * @return 转换后的数目
     */
    @GetMapping("toRate")
    public MessageResult toRate(@RequestParam(value = "amount",required = false) BigDecimal sourceAmount,
                                @RequestParam("sourceCoin") String sourceCoinUnit,
                                @RequestParam("targetCoin")String targetCoinUnit){
        if(null == sourceAmount) {
            return success(coinExchangeRateService.toRate(sourceCoinUnit, targetCoinUnit));
        } else {
            return success(coinExchangeRateService.toRate(sourceAmount, sourceCoinUnit, targetCoinUnit));
        }
    }

}
