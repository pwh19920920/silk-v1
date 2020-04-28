package com.spark.bitrade;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.service.MemberTransactionService;
import com.spark.bitrade.util.Md5;
import com.spark.bitrade.util.PriceUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class FinanceTest  {

    @Test
    public void testBigdecimal(){
//        BigDecimal a  = new BigDecimal("12.5");
//        System.out.println(a);
//        a.subtract(BigDecimal.ONE);
//        System.out.println(a);
//
//        System.out.println(Md5.MD5("password"));
//        ;
//        System.out.println(PriceUtil.toRate(BigDecimal.valueOf(10l), 8, BigDecimal.valueOf(1l), BigDecimal.valueOf(9l)));
        System.out.println(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(9), 9, BigDecimal.ROUND_UP).multiply(BigDecimal.valueOf(10)).setScale(8, BigDecimal.ROUND_UP).multiply(BigDecimal.valueOf(0.8)).setScale(8, BigDecimal.ROUND_DOWN));
    }
}
