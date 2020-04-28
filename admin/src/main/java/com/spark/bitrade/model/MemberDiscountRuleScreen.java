package com.spark.bitrade.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author fumy
 * @time 2018.08.30 13:51
 */
@Data
public class MemberDiscountRuleScreen {

    private Long id;

    private Long memberId;

    private int enable;                 //是否启用配置：1：是，0：否

    private BigDecimal buyDiscount;     //买币手续费折扣

    private BigDecimal sellDiscount;    //卖币手续费折扣

    private String note;

    private String symbol;      //币种

}
