package com.spark.bitrade.h5game;

/**
 * H5GameApiService
 *
 * @author archx
 * @time 2019/4/24 17:07
 */
public interface IH5GameApiService {

    /**
     * 获取接口服务地址
     *
     * @return server
     */
    String getServer();

    /**
     * 获取接口地址
     *
     * @param uri uri
     * @return url
     */
    default String getUrl(String uri) {
        return getServer() + uri;
    }

    /**
     * 联合登录
     *
     * @param mobile  标识符
     * @param amount  数额
     * @param inviter 邀请人标识符
     * @return resp
     */
    H5Resp union(String mobile, String amount, String inviter);

    /**
     * 转入
     *
     * @param mobile 标识符
     * @param amount 数额
     * @return resp
     */
    H5Resp transferIn(String mobile, String amount);

    /**
     * 转出
     *
     * @param mobile 标识符
     * @param amount 数额
     * @return resp
     */
    H5Resp transferOut(String mobile, String amount);


    /**
     * 余额
     *
     * @param mobile 标识符
     * @return resp
     */
    H5Resp balance(String mobile);

}
