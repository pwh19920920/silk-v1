package com.spark.bitrade.controller.v3;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.annotation.definition.ApiRequestLimit;
import com.spark.bitrade.controller.vo.VirtualTraderVo;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Random;

/**
 * 虚拟成交接口
 *
 * @author Archx[archx@foxmail.com]
 * @since 2019/6/28 15:19
 */
@Slf4j
@RestController
@RequestMapping("/v3/virtual")
public class VirtualTraderControllerV3 {

    private final KafkaTemplate kafkaTemplate;
    private String[] directions = new String[]{"BUY", "SELL"};
    private Random random = new Random();

    public VirtualTraderControllerV3(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @ApiRequestLimit(count = 10000)
    @PostMapping("/trader")
    public MessageResult trader(@RequestBody VirtualTraderVo trade) {
        log.info("虚拟成交订单: symbol = {}, amount = {}, price = {} ", trade.getSymbol(), trade.getAmount(), trade.getPrice());
        try {
            kafkaTemplate.send("exchange-trade-mocker", getKey(trade.getSymbol()), buildData(trade.getPrice(), trade.getAmount()));
            return MessageResult.success();
        } catch (Exception ex) {
            return MessageResult.error(ex.getMessage());
        }
    }

    private String getKey(String symbol) {
        if (symbol.contains("_")) {
            return symbol.replace("_", "/");
        }
        return symbol;
    }

    private String buildData(String price, String amount) {
        JSONObject json = new JSONObject();

        json.put("price", price);
        json.put("amount", amount);
        json.put("direction", directions[random.nextInt(2)]);
        json.put("time", Calendar.getInstance().getTimeInMillis());

        JSONArray array = new JSONArray();
        array.add(json);
        return array.toJSONString();
    }
}
