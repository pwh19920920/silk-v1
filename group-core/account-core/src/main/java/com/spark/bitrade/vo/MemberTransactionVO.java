package com.spark.bitrade.vo;

import com.spark.bitrade.entity.MemberTransaction;
import lombok.Data;

@Data
public class MemberTransactionVO extends MemberTransaction{

    private String memberUsername ;

    private String memberRealName ;

    private String phone ;

    private String email ;

    /**
     * 用于存储人工充值时，操作人员的信息
     */
    private String comment;
}
