package com.spark.bitrade.dto;

import lombok.Data;

/**
 * <p>SecretKeyDTO传输对象</p>
 * @author tian.bo
 * @date 2019/1/9.
 */
@Data
public class SecretKeyDTO {
    /**
     * secretKey
     */
    private String sk;
    /**
     * 交易编号
     */
    private String txn;

}
