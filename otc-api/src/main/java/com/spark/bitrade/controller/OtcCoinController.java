package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.coin.CoinExchangeFactory;
import com.spark.bitrade.entity.OtcCoin;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.OtcCoinService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.MessageResult.success;

/**
 * @author Zhang Jinwei
 * @date 2018年01月06日
 */
@RestController
@Slf4j
@RequestMapping(value = "/coin")
public class OtcCoinController {

    @Autowired
    private OtcCoinService coinService;
    @Autowired
    private CoinExchangeFactory coins;
    // TODO 临时过滤BT和DCC币种
    private List<String> exclude = Collections.singletonList("DCC");

    @RequestMapping(value = "config")
    public MessageResult serviceRate(@RequestParam(defaultValue = "") String coinUnit) {
        Map<String, Object> data = new HashMap<>();
        data.put("serviceRate", coinService.getServiceRate(coinUnit));
        MessageResult result = success();
        result.setData(data);
        return result;
    }

    /**
     * 取得正常的币种和余额
     *
     * @return
     */
    @RequestMapping(value = "pcall")
    public MessageResult allCoin( @SessionAttribute(SESSION_MEMBER) AuthMember member) throws Exception {
        ////edit by tansitao 时间： 2018/4/26 原因：修改获取币种和余额
        List<Map<String, String>> list = coinService.getAllNormalCoinAndBalance(member.getId());
        list.stream().forEachOrdered(x ->{
            if(coins.get(x.get("unit")) != null) {
                x.put("marketPrice", coins.get(x.get("unit")).toString());
            }
        });
        MessageResult result = success();
        result.setData(list);
        return result;
    }

    /**
     * app端取得正常的币种
     *
     * @return
     */
    @RequestMapping(value = "all")
    public MessageResult allCoin(@RequestParam(defaultValue = "0") int type) throws Exception {
        List<Map<String, String>> list = coinService.getAllNormalCoin();
        list.stream().forEachOrdered(x -> {
            if (coins.get(x.get("unit")) != null) {
                x.put("marketPrice", coins.get(x.get("unit")).toString());
            }
        });
        MessageResult result = success();
        if (type == 0) {
            result.setData(list.stream().filter(i -> !exclude.contains(i.get("unit"))).toArray());
        } else {
            result.setData(list);
        }
        return result;
    }


    @RequestMapping(value = "coinRank",method = RequestMethod.GET)
    public MessageResult coinRank(String coin){
        if(StringUtils.isEmpty(coin)){
            return MessageResult.success();
        }
        OtcCoin otcCoin = coinService.findByUnit(coin);
        JSONObject o=new JSONObject();
        if(otcCoin!=null){
            o.put("max",otcCoin.getTradeMaxLimit());
            o.put("min",otcCoin.getTradeMinLimit());
        }
        return MessageResult.success("success",o);
    }

}
