package com.spark.bitrade.controller;

import com.spark.bitrade.entity.DimArea;
import com.spark.bitrade.service.GyDmcodeService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.NetworkTool;
import com.spark.bitrade.util.ResolveJson;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.util.Assert.isNull;
import static org.springframework.util.Assert.isTrue;

/**
 * 归属地信息类
 * @author shenzucai
 * @time 2018.05.29 13:55
 */
@RestController
@RequestMapping("/location")
@Slf4j
public class LocationController {
    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private GyDmcodeService gyDmcodeService;
    /**
     *
     * @author shenzucai
     * @time 2018.05.29 13:59
     * @param idCard 身份证号
     * @param phone 电话号码
     * @param ip IP地址
     * @return true
     */
    @GetMapping("get")
    public MessageResult getPostionInfo(String idCard, String phone, String ip){
        DimArea dimArea = gyDmcodeService.getPostionInfo(idCard, phone, ip);
        MessageResult mr = MessageResult.success();
        mr.setData(dimArea);
        return mr;
    }

}
