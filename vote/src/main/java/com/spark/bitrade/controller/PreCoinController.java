package com.spark.bitrade.controller;

import com.spark.bitrade.entity.PreCoin;
import com.spark.bitrade.service.PreCoinService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("preCoin")
public class PreCoinController {

    @Autowired
    private PreCoinService preCoinService ;

    @PostMapping("merge")
    public MessageResult merge(@Valid PreCoin coin){
        coin = preCoinService.save(coin);
        return MessageResult.getSuccessInstance("保存成功",coin);
    }
}
