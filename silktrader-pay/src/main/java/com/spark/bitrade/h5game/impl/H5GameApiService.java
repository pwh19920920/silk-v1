package com.spark.bitrade.h5game.impl;

import com.spark.bitrade.h5game.H5Config;
import com.spark.bitrade.h5game.H5Resp;
import com.spark.bitrade.h5game.IH5GameApiService;
import com.spark.bitrade.h5game.utils.H5GameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * H5GameApiService H5游戏平台接口实现
 *
 * @author archx
 * @time 2019/4/24 17:08
 */
@Service
public class H5GameApiService implements IH5GameApiService {

    private H5Config config = new H5Config(); // default

    @Autowired
    public void setConfig(H5Config config) {
        this.config = config;
    }

    @Override
    public String getServer() {
        return config.getServer();
    }

    @Override
    public H5Resp union(String mobile, String amount, String inviter) {
        Map<String, String> params = config.getUnionParams(mobile, amount, inviter);
        return H5GameUtils.post(getUrl("/v1/union/init"), params);
    }

    @Override
    public H5Resp transferIn(String mobile, String amount) {

        Map<String, String> params = config.getTransferParams(mobile, amount);

        return H5GameUtils.post(getUrl("/v1/transfer/valueIn"), params);
    }

    @Override
    public H5Resp transferOut(String mobile, String amount) {
        Map<String, String> params = config.getTransferParams(mobile, amount);

        return H5GameUtils.post(getUrl("/v1/transfer/valueOut"), params);
    }

    @Override
    public H5Resp balance(String mobile) {

        Map<String, String> params = config.getNormalParams(mobile);

        return H5GameUtils.post(getUrl("/v1/asset/getOneUserUsable"), params);
    }
}
