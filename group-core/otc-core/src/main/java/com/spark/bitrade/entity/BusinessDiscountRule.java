package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 法币交易折扣规则
 * @author tansitao
 * @time 2018/9/3 14:28 
 */
@Data
@Entity
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessDiscountRule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    //会员ID
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    //交易对名称，格式：BTC/USDT
    @NotNull(message = "交易对不能为空，可以用*号代表所有交易对")
    private String symbol;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int(11) default 1 comment '是否启用配置'")
    private BooleanEnum enable = BooleanEnum.IS_TRUE;


    //买币手续费的折扣率（基于已有优惠的基础上），用小数表示百分比（表示减免的手续费）。eg：20%=0.2；1为手续费全免，0为不优惠手续费
    @Column(columnDefinition = "decimal(8,4) default 1 comment '买币广告手续费的折扣率'")
    private BigDecimal feeBuyDiscount;

    //卖币手续费的折扣率（基于已有优惠的基础上），用小数表示百分比（表示减免的手续费）。eg：20%=0.2；1为手续费全免，0为不优惠手续费
    @Column(columnDefinition = "decimal(8,4) default 1 comment '卖币广告手续费的折扣率'")
    private BigDecimal feeSellDiscount;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime comment '任务创建时间'")
    private Date createTime ;

    //备注
    private String note;

}
