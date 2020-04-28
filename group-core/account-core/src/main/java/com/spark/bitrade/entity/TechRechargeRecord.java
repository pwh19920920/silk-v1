package com.spark.bitrade.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;


import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 技术充（减）币记录
 * </p>
 *
 * @author fumy
 * @since 2018-06-20
 */
@Entity
@Data
@ExcelSheet
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechRechargeRecord{
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    /**
     * 操作日期
     */
    @Excel(name = "统计周期")
    @Column(columnDefinition = "varchar(255) comment '统计周期'")
    private String opDate;
    /**
     * 交易流水号
     */
    @Excel(name = "交易流水号")
    @Column(columnDefinition = "varchar(255) comment '交易流水号'")
    private String tradeNo;
    /**
     * 币种
     */
    @Excel(name = "币种")
    @Column(columnDefinition = "varchar(255) comment '币种'")
    private String coinUnit;
    /**
     * 用户账号
     */
    @Excel(name = "用户账号")
    @Column(columnDefinition = "varchar(255) comment '用户账号'")
    private String memberAccount;
    /**
     * 姓名
     */
    @Excel(name = "姓名")
    @Column(columnDefinition = "varchar(255) comment '姓名'")
    private String memberRealName;
    /**
     * 充值类型,0：充，1：减
     */
    @Excel(name = "交易类型")
    @Transient
    private String rechargeTypeOut;
    @Column(columnDefinition = "int(2) comment '交易类型'")
    private Integer rechargeType;//交易类型
    /**
     * 充币数量
     */
    @Excel(name = "充币数量")
    @Column(columnDefinition = "decimal(19,8) comment '充币数量'")
    private BigDecimal rechargeNumber;
    /**
     * 操作类型
     */
    @Excel(name = "操作类型")
    @Column(columnDefinition = "int(2) comment '操作类型'")
    private Integer opType;
    /**
     * 备注
     */
    @Excel(name = "备注")
    @Column(columnDefinition = "varchar(255) comment '备注'")
    private String remark;

}
