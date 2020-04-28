package com.spark.bitrade.dto;

import lombok.Data;

/**
 * @author shenzucai
 * @time 2018.05.31 16:24
 */
@Data
public class TempTable {

    /**
     * 依据的表结构
     */
    private String baseTable;

    /**
     * 生成的新的表
     */
    private String newTable;
}
