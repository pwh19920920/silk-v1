package com.spark.bitrade.model;

import lombok.Data;

@Data
public class OtcOrderExcelScreen extends OtcOrderTopScreen{

    private Long memberId ;

    private Long customerId ;
}
