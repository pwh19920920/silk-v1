package com.spark.bitrade.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MemberDepositDTO {



    private BigDecimal amount;

    private String unit;

    private Date createTime;

    private String txid;

    private String address;

    private String link;



}
