package com.spark.bitrade.util;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Setter;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author shenzucai
 * @time 2019.03.21 10:43
 */
public class AppIdUtil {

    /**
     * 处理appid问题，兼容open-api和以前的应用
     * @author shenzucai
     * @time 2019.03.21 10:48
     * @param paltFrom
     * @return true
     */
    public static String getRealAppIdBySessionPaltform(String paltFrom){

       /* // 0：表示星客交易所会员;1：表示升腾会员;2：表示链人会员;3：表示星客CNYT理财会员;4：表示星客钱包会员
        SILK("星客交易所", "0"),
                FINANCE("CNYT理财", "3"),
                SILK_PAY("星客钱包", "4"),
                ST_PAY("升腾钱包", "55434535"),
                LR_PAY("链人", "2");*/
        String appId;
        switch (paltFrom){
            //SILK("星客交易所", "0"),
            case "0":
                appId = "0";
                break;
            case "2":
                //LR_PAY("链人", "2");
                appId = "4";
                break;
                //FINANCE("CNYT理财", "3"),
            case "3":
                appId = "1";
                break;
                //SILK_PAY("星客钱包", "4"),
            case "4":
                appId = "2";
                break;
            default:
                appId = paltFrom;
        }

        return appId;
    }
}
