package com.spark.bitrade.controller;

import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.MarketRedoService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  
 *
 * @author young
 * @time 2019.11.14 11:02
 */

@Slf4j
@RestController
public class ManageController {
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private MarketRedoService redoService;


    /**
     * 业务重做接口，访问地址：/market/redo?id=
     *
     * @param id 异常业务ID
     *            
     * @author yangch
     * @time 2018.06.09 14:16 
     */
    @RequestMapping("redo")
    public MessageResult redo(Long id) {
        return redoService.redo(id);
    }

    /**
     * 获取交易队处理器的状态，访问地址：/market/processorStatus?symbol=SLB/USDT
     *
     * @param symbol
     * @return
     */
    @RequestMapping("processorStatus")
    public Map<String, String> traderStatus(@RequestParam(value = "symbol", required = false) String symbol) {
        Map<String, String> map = new HashMap<>(32);
        if (null != symbol) {
            CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol.toUpperCase());
            if (null != coinProcessor) {
                map.put(symbol, coinProcessor.isHalt() ? "suspend" : "running");
            } else {
                map.put(symbol, "--");
            }
        } else {
            Set<Map.Entry<String, CoinProcessor>> entrySet = coinProcessorFactory.getProcessorMap().entrySet();
            for (Map.Entry<String, CoinProcessor> entry : entrySet) {
                map.put(entry.getKey(), entry.getValue().isHalt() ? "suspend" : "running");
            }
        }
        return map;
    }
}
