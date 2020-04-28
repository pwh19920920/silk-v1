package com.spark.bitrade.h5game;

import com.spark.bitrade.h5game.utils.H5GameUtils;
import lombok.Data;

import java.util.Map;

/**
 * H5Config
 *
 * @author archx
 * @time 2019/4/24 18:08
 */
@Data
public class H5Config {

    private String server = "http://111.90.145.192:8099";
    private String appId = "411ce36bee364e8f907e268fd148d1b3";
    private String appSecret = "344a29215ed04934aa389f76";

    private Long payRoleId;
    private Long payAppId;

    /**
     * 获取转账参数
     *
     * @param mobile 标识符
     * @param amount 数额
     * @return map
     */
    public Map<String, String> getTransferParams(String mobile, String amount) {
        return H5GameUtils.buildTransferParams(mobile, amount, appId, appSecret);
    }

    /**
     * 获取常规参数
     *
     * @param mobile 标识符
     * @return map
     */
    public Map<String, String> getNormalParams(String mobile) {
        return H5GameUtils.buildNormalParams(mobile, appId, appSecret);
    }

    /**
     * 获取联合登录参数
     *
     * @param mobile  标识符
     * @param amount  数额
     * @param inviter 邀请人
     * @return map
     */
    public Map<String, String> getUnionParams(String mobile, String amount, String inviter) {
        return H5GameUtils.buildUnionParams(mobile, amount, inviter, appId, appSecret);
    }
}
