package com.spark.bitrade.controller;

import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dto.CoinDescriptionDto;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rongyu
 * @Description: coin
 * @date 2018/4/214:20
 */

@RestController
@RequestMapping("coin")
@Api(description = "币种管理",value = "币种管理")
public class CoinController extends BaseController {
    @Autowired
    private CoinService coinService;

    @Autowired
    MongoTemplate mongoTemplate;

    @GetMapping("legal")
    public MessageResult legal() {
        List<Coin> legalAll = coinService.findLegalAll();
        return success(legalAll);
    }

    @GetMapping("legal/page")
    public MessageResult findLegalCoinPage(PageModel pageModel) {
        Page all = coinService.findLegalCoinPage(pageModel);
        return success(all);
    }

    //add by yangch 时间： 2018.05.02 原因：合并新增
    @RequestMapping("supported")
    public List<Map<String,String>>  findCoins(){
        List<Coin> coins = coinService.findAll();
        List<Map<String,String>> result = new ArrayList<>();
        coins.forEach(coin->{
            if(coin.getHasLegal() == Boolean.FALSE) {
                Map<String, String> map = new HashMap<>();
                map.put("name",coin.getName());
                map.put("nameCn",coin.getNameCn());
                map.put("withdrawFee",String.valueOf(coin.getMinTxFee()));
                map.put("enableRecharge",String.valueOf(coin.getCanRecharge().getOrdinal()));
                map.put("minWithdrawAmount",String.valueOf(coin.getMinWithdrawAmount()));
                map.put("enableWithdraw",String.valueOf(coin.getCanWithdraw().getOrdinal()));
                result.add(map);
            }
        });
        return result;
    }

    /**
     * 获取币种介绍（暂时） TODO 此处使用mongodb不合理，对性能有影响，后续需要修改，加入缓存
     * @author Zhang Yanjun
     * @time 2018.10.25 16:37
     * @param unit
    */
    @PostMapping("content")
    @ApiOperation(value = "获取币种介绍",notes = "获取币种介绍")
    @ApiImplicitParam(value = "币种缩写  如：ETH",name = "unit")
    public MessageResult findContentByName(String unit){
        Coin coin = coinService.findOne(unit);
        Criteria criteria = Criteria.where("unit").is(unit.trim());
        CoinDescriptionDto descriptionDto = mongoTemplate.findOne(new Query(criteria), CoinDescriptionDto.class,"coin_description");
        if (descriptionDto != null) {
            descriptionDto.setLink(coin != null ? coin.getMoreLink() : "");
        }
        return success("获取成功",descriptionDto);
    }
}
