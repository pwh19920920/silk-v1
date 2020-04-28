package com.spark.bitrade.entity;

import com.spark.bitrade.constant.PartnerLevle;
import com.spark.bitrade.constant.PartnerStaus;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
public class PartnerAreaScreen {



    /**
     * 合伙人区域
     */
    private String areaId ;

    /**
     * 等级
     */
    @Enumerated(EnumType.ORDINAL)
    private PartnerLevle level;

}
