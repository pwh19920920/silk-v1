package com.spark.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 场外交易订单
 *
 * @author Zhang Jinwei
 * @date 2017年12月11日
 */
@Entity
@Data
@Table(name = "otc_order")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    @Excel(name = "订单编号", orderNum = "1", width = 20)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 订单号
     */
    @Excel(name = "订单号", orderNum = "1", width = 20)
    @Column(unique = true, nullable = false)
    private String orderSn;

    /**
     * 广告类型 0:买入 1:卖出
     */
    @Excel(name = "订单类型", orderNum = "1", width = 20)
    @Enumerated(EnumType.ORDINAL)
    @NotNull(message = "广告类型不能为空")
    private AdvertiseType advertiseType;

    /**
     * 创建时间
     */
    @Excel(name = "创建时间", orderNum = "1", width = 20)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 广告拥有者id
     */
    @NotNull
    private Long memberId;
    /**
     * 广告拥有者id
     */
    private Long currencyId;
    /**
     * 广告拥有者姓名
     */
    private String memberName;
    private String memberRealName;

    /**
     * 交易对象id
     */
    @NotNull
    private Long customerId;

    /**
     * 交易对象用户名
     */
    private String customerName;
    private String customerRealName;

    /**
     * 支付码
     */
    @NotNull
    private String payCode;

    /**
     * 币种
     */
    @JoinColumn(name = "coin_id", nullable = false)
    @ManyToOne
    private OtcCoin coin;

    /**
     * 价格
     */
    //edit by tansitao 时间： 2018/10/26 原因：修改价格精度
    @DecimalMin(value = "0", message = "价格必须大于等于0")
    @Column(columnDefinition = "decimal(18,8) comment '价格'")
    private BigDecimal price;

    /**
     * 最高单笔交易额
     */
    @DecimalMin(value = "0", message = "最高交易额必须大于等于0")
    @Column(columnDefinition = "decimal(18,2) comment '最高交易额'")
    private BigDecimal maxLimit;

    /**
     * 国家
     */
    private String country;

    //add by tansitao 时间： 2018/4/25 原因：添加是否为手动取消订单，（1：表示手动取消订单，0：表示不是手动取消订单）
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isManualCancel;

    /**
     * 最低单笔交易额
     */
    @DecimalMin(value = "0", message = "最低交易额必须大于等于0")
    @Column(columnDefinition = "decimal(18,2) comment '最低交易额'")
    private BigDecimal minLimit;

    /**
     * 顾客需求
     */
    private String remark;

    /**
     * 付款期限，单位分钟
     */
    @DecimalMin(value = "1", message = "付款期限必须大于等于1")
    private Integer timeLimit;

    /**
     * 交易金额
     */
    @DecimalMin(value = "0.01", message = "交易金额必须大于等于0.01")
    @Column(columnDefinition = "decimal(18,2) comment '交易金额'")
    private BigDecimal money;

    /**
     * 交易数量
     */
    @Column(columnDefinition = "decimal(18,8) comment '交易数量'")
    @DecimalMin(value = "0.00000001", message = "交易数量有误")
    private BigDecimal number;

    /**
     * 订单金额
     */
    @DecimalMin(value = "0.01", message = "订单金额必须大于等于0.01")
    @Column(columnDefinition = "decimal(18,2) default 0 comment '订单金额'")
    private BigDecimal orderMoney;

    /**
     * 服务费率
     */
    @DecimalMin(value = "0.00", message = "服务费率必须大于等于0.00")
    @Column(columnDefinition = "decimal(18,4) default 0 comment '服务费率'")
    private BigDecimal serviceRate;

    /**
     * 服务费
     */
    @DecimalMin(value = "0.00", message = "服务费必须大于等于0.00")
    @Column(columnDefinition = "decimal(18,2) default 0 comment '服务费'")
    private BigDecimal serviceMoney;

    /**
     * 手续费
     */
    @Column(columnDefinition = "decimal(18,8) comment '手续费'")
    private BigDecimal commission;

    /**
     * 状态
     */
    @Enumerated(EnumType.ORDINAL)
    private OrderStatus status;

    /**
     * 付款时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    /**
     * 付费方式(用英文逗号隔开)
     */
    @NotBlank(message = "付费方式不能为空")
    private String payMode;

    @NotNull
    private Long advertiseId;

    /**
     * 订单取消时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelTime;

    /**
     * 取消订单的人 add by yangch 2018-04-28
     */
    private Long cancelMemberId;
    /**
     * 放行时间
     */

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date releaseTime;
    @Embedded
    private Alipay alipay;
    @Embedded
    private BankInfo bankInfo;
    @Embedded
    private WechatPay wechatPay;
    @Embedded
    private Epay epay; //addby tansitao 时间： 2018/8/16 原因：增加Epay支付方式
    private Long payMethod;//add by tansitao 时间： 2018/9/4 原因：支付方式
    @Column(columnDefinition = "varchar(255) comment '付款账号信息'")
    private String payMethodInfo; // 支付方式对应的账号信息(JSON格式数据)
    private String attr1;

    @JsonIgnore
    @Version
    private Long version;

    /**
     *  关闭时间
     * @author Zhang Yanjun
     * @time 2018.10.31 15:04
     */
    @Excel(name = "关闭时间", orderNum = "1", width = 20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date closeTime;

    //add by tansitao 时间： 2019/1/8 原因：新增订单来源类型
    @Column(columnDefinition = "varchar(32) default '-1' comment '订单来源类型'")
    private String orderSourceType="-1";

    @Column(columnDefinition = "int default 0 comment '是否为一键交易'")
    private BooleanEnum isOneKey = BooleanEnum.IS_FALSE;

    @Column(columnDefinition = "int default 0 comment '是否为商家购币'")
    private BooleanEnum isMerchantsBuy=BooleanEnum.IS_FALSE;

}
