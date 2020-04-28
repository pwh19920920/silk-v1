package com.spark.bitrade.controller;

import com.spark.bitrade.services.ApiCacheService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>缓存清除</p>
 * @author tian.bo
 * @date 2018-12-7
 */
@Slf4j
@RestController
@RequestMapping("/openapi/cache")
public class ApiRefreshCacheController {

    @Autowired
    private ApiCacheService apiCacheService;

    @GetMapping("/refresh")
    public MessageResult refresh(){
        apiCacheService.clearCache();
        return MessageResult.success("success");
    }

}
