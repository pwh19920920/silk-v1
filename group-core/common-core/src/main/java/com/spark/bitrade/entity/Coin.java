package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CoinFeeType;
import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author rongyu
 * @description
 * @date 2017/12/29 14:14
 */
@Entity
@Data
@Table(name = "coin")
@ExcelSheet
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coin implements Serializable {
    private static final long serialVersionUID = 7898194272883238672L;

    /**
     * 中文
     */
    @Excel(name = "中文名")
    @NotBlank(message = "中文名称不得为空")
    private String nameCn;

    @Id
    @NotBlank(message = "币名称不得为空")
    @Excel(name = "名称（英文）")
    private String name;
    /**
     * 缩写
     */
    @Excel(name = "币种缩写")
    @NotBlank(message = "单位不得为空")
    private String unit;
    /**
     * 状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * 最小提币手续费
     */
//    @Excel(name = "最小提币手续费")
    private double minTxFee;
    /**
     * 对人民币汇率
     */
//    @Excel(name = "对人民币汇率")
    @Column(columnDefinition = "decimal(18,2) default 0.00 comment '人民币汇率'")
    private double cnyRate;
    /**
     * 最大提币手续费
     */
//    @Excel(name = "最大提币手续费")
    private double maxTxFee;
    /**
     * 对美元汇率
     */
//    @Excel(name = "对美元汇率")
    @Column(columnDefinition = "decimal(18,2) default 0.00 comment '美元汇率'")
    private double usdRate;
    /**
     * 是否支持rpc接口
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum enableRpc = BooleanEnum.IS_FALSE;

    ////add by tansitao 时间： 2018/4/22 原因：添加主币
    private  String baseCoinUnit;

    /**
     * 排序
     */
    private int sort;

    /**
     * 是否能提币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canWithdraw;

    /**
     * 是否能充币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canRecharge;


    /**
     * 是否能转账
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canTransfer = BooleanEnum.IS_TRUE;

    /**
     * 是否能自动提币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canAutoWithdraw;

    /**
     * 提币阈值
     */
    @Column(columnDefinition = "decimal(18,8) comment '提现阈值'")
    private BigDecimal withdrawThreshold;
    @Column(columnDefinition = "decimal(18,8) comment '最小提币数量'")
    private BigDecimal minWithdrawAmount;
    @Column(columnDefinition = "decimal(18,8) comment '最大提币数量'")
    private BigDecimal maxWithdrawAmount;

    /**
     * 是否是平台币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isPlatformCoin = BooleanEnum.IS_FALSE;

    /**
     * 是否是合法币种
     */
    @Column(name = "has_legal", columnDefinition = "bit default 0", nullable = false)
    private Boolean hasLegal = false;

    @Transient
    @Excel(name = "会员总余额")
    private BigDecimal allBalance ;



    @Transient
    @Excel(name = "钱包余额")
    private BigDecimal hotAllBalance ;

    //add by shenzucai 时间： 2018.04.25 原因：添加coinbase地址 start
    @Transient
    @Excel(name = "提币地址")
    private String coinBaseAddress ;

    @Transient
    @Excel(name = "提币地址余额")
    private BigDecimal coinBaseBalance ;
    //add by shenzucai 时间： 2018.04.25 原因：添加coinbase余额

    @Excel(name = "冷钱包地址")
    private String coldWalletAddress ;

    /**
     * 转账时付给矿工的手续费
     */
    @Column(columnDefinition = "decimal(18,8) default 0 comment '矿工费'")
    private BigDecimal minerFee;

    @Column(columnDefinition = "int default 4 comment '提币精度'")
    private int withdrawScale;

    //add by shenzucai 时间： 2018.05.25 原因：添加区块链浏览器地址
    /**
     * 对应区块链浏览器tx前缀
     */
    private String exploreUrl;

    //add by  shenzucai 时间： 2018.07.05  原因：添加手续费比例收集 start
    /**
     * 手续费率设置
     */
    @Column(columnDefinition = "decimal(18,8) default 0.01 comment '手续费率'")
    private BigDecimal feeRate;

    /**
     * 手续费模式0，固定，1比例
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 0 comment '手续费模式0，固定，1比例'")
    private CoinFeeType feeType;

    //add by  shenzucai 时间： 2018.07.05  原因：添加手续费比例收集 end

    //add by  shenzucai 时间： 2018.10.10  原因：添加提币地址标签判断 start
    /**
     * 是否具有标签
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 1 comment '是否具有标签 0 有，1无，默认1'")
    private CommonStatus hasLabel = CommonStatus.ILLEGAL;
    //add by  shenzucai 时间： 2018.10.10  原因：添加提币地址标签判断 end

    /**
     * 最小到账金额
     */
    @Column(columnDefinition = "decimal(18,8) default 0.002 comment '最小到账金额'")
    private double minDepositAmount;

    //add by zyj: 2018.10.13
    private String content;//币种介绍

    //add by  shenzucai 时间： 2018.10.29  原因： 匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 start
    @Column(columnDefinition = "varchar(255) comment '手续费抵扣币种单位（不包括当前币种）'")
    private String feeDiscountCoinUnit;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 0 comment '手续费抵扣模式'")
    private CoinFeeType feeDiscountType;

    @Column(columnDefinition = "decimal(18,8) comment '抵扣数值，手续费抵扣模式为0时，代表具体数量，抵扣模式为1时，代表折扣0.8代表8折'")
    private BigDecimal feeDiscountAmount;
    //add by  shenzucai 时间： 2018.10.29  原因：匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 end

    @Column(columnDefinition = "varchar(255) comment '币种介绍链接'")
    private String moreLink;

    @Column(columnDefinition = "int(11) comment '自动次数'")
    private Integer autoCount;
}
