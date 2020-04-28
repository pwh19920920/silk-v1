package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;

import java.util.Date;

@Data
public class PartnerAreaScreen extends AccountScreen{



    /**
     * 商家区域
     */
    private String areaId ;
}
