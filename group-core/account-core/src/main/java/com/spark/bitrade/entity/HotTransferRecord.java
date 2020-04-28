package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name="hot_transfer_record")
@ExcelSheet
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotTransferRecord {

    public HotTransferRecord(){

    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Excel(name = "ID ")
    private Long id ;

    @Excel(name = "转入币种")
    private String unit ;
    @JsonIgnore
    private Long adminId ;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "转入时间")
    private String transferTime ;

    @Column(columnDefinition = "decimal(18,8) default 0 comment '转账金额'")
    @Excel(name = "转入数量")
    private BigDecimal amount ;

    @Column(columnDefinition = "decimal(18,8) default 0 comment '热钱包余额'")
    @Excel(name = "钱包余额")
    private BigDecimal balance ;

    @Column(columnDefinition = "decimal(18,8) default 0 comment '矿工费'")
    @Excel(name = "最小手续费")
    private BigDecimal minerFee ;

    @Transient
    @Excel(name = "操作人")
    private String adminName ;

    @Excel(name = "冷钱包地址")
    private String coldAddress ;

    @Excel(name = "转入单号")
    private String transactionNumber ;
}
