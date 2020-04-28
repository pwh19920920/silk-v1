package com.spark.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * OTC币种
 *
 * @author Zhang Jinwei
 * @date 2018年01月09日
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtcCoin {
    @Excel(name = "otc货币编号", orderNum = "1", width = 20)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Excel(name = "otc货币名称", orderNum = "1", width = 20)
    @NotBlank(message = "币名称不得为空")
    private String name;

    /**
     * 中文
     */
    @Excel(name = "otc货币单位中文名称", orderNum = "1", width = 20)
    @NotBlank(message = "中文名称不得为空")
    private String nameCn;

    /**
     * 缩写
     */
    @Excel(name = "otc货币单位", orderNum = "1", width = 20)
    @NotBlank(message = "单位不得为空")
    private String unit;

    /**
     * 状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * 交易手续费率（卖币）
     */
    @Column(columnDefinition = "decimal(18,6) comment '交易手续费率（卖币）'")
    private BigDecimal jyRate;
    /**
     * 交易手续费率（买币）
     */
    @Column(columnDefinition = "decimal(18,6) comment '交易手续费率（买币）'")
    private BigDecimal buyJyRate;

    @Column(columnDefinition = "decimal(18,8) comment '卖出广告最低发布数量'")
    private BigDecimal sellMinAmount;

    @Column(columnDefinition = "decimal(18,8) comment '买入广告最低发布数量'")
    private BigDecimal buyMinAmount;

    /**
     * 最低单笔交易额
     */
    @Column(columnDefinition = "decimal(18,2) comment '最低单笔交易额'") //add by tansitao 时间： 2018/8/21 原因：增加交易最小额
    private BigDecimal tradeMinLimit = BigDecimal.ZERO;

    /**
     * 最高单笔交易额
     */
    @Column(columnDefinition = "decimal(18,2) comment '最高单笔交易额'")//add by tansitao 时间： 2018/8/21 原因：增加交易最大额
    private BigDecimal tradeMaxLimit = BigDecimal.ZERO;

    //add by tansitao 时间： 2018.09.03 原因：折扣=discount
    //买币手续费的折扣率，用小数表示百分比（表示减免的手续费）。eg：20%=0.2；1为手续费全免，0为不优惠手续费
    @Column(columnDefinition = "decimal(8,4) default 0 comment '买币手续费的折扣率'")
    private BigDecimal feeBuyDiscount;

    //卖币手续费的折扣率，用小数表示百分比（表示减免的手续费）。eg：20%=0.2；1为手续费全免，0为不优惠手续费
    @Column(columnDefinition = "decimal(8,4) default 0 comment '卖币手续费的折扣率'")
    private BigDecimal feeSellDiscount;

    @Excel(name = "otc货币单位", orderNum = "1", width = 20)
    private int sort;

    /**
     * 是否是平台币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isPlatformCoin = BooleanEnum.IS_FALSE;

    //add by yangch 时间： 2018.10.24 原因： 1.3优化需求的扩展字段 --begin
    /**
     * 货币小数位精度（默认为8位）
     */
    @Column(columnDefinition = "int(11) default 8 comment '货币小数位精度（默认为8位）'")
    private int coinScale;

    /**
     * 普通用户上架广告费用的币种（使用币种的简写名称，默认为USDT）
     */
    @Column(columnDefinition = "varchar(32) default 'USDT' comment '普通用户上架广告费用的币种（使用币种的简写名称，默认为USDT）'")
    private String generalFeeCoinUnit;

    /**
     * 普通用户上架广告费用
     */
    @Column(columnDefinition = "decimal(18,8) default 0 comment '普通用户上架广告费用'")
    private BigDecimal generalFee;

    /**
     * 可用的支付优惠币种（针对普通用户上架广告费用；使用币种的简写名称，默认为SLU）
     */
    @Column(columnDefinition = "varchar(32) default 'SLU' comment '可用的支付优惠币种（针对普通用户上架广告费用；使用币种的简写名称，默认为SLU）'")
    private String generalDiscountCoinUnit;

    /**
     * 支付优惠折扣率(针对普通用户上架广告费用，用小数来表示百分比，默认为0)
     */
    @Column(columnDefinition = "decimal(8,4) default 0 comment '支付优惠折扣率(针对普通用户上架广告费用，用小数来表示百分比)'")
    private BigDecimal generalDiscountRate;

    /**
     * 使用优惠币种结算的精度（针对普通用户上架广告费用，默认为8位）
     */
    @Column(columnDefinition = "int(11) default 8 comment '使用优惠币种结算的精度（针对普通用户上架广告费用，默认为8位）'")
    private int generalDiscountCoinScale;

    /**
     * 发布购买广告时账户最低可用余额要求（针对普通用户上架，默认为0）
     */
    @Column(columnDefinition = "decimal(18,8) default 0 comment '发布购买广告时账户最低可用余额要求（针对普通用户上架）'")
    private BigDecimal generalBuyMinBalance;
    //add by yangch 时间： 2018.10.24 原因： 1.3优化需求的扩展字段 --end
}