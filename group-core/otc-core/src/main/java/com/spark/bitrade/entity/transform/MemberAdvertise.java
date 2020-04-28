package com.spark.bitrade.entity.transform;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.AdvertiseControlStatus;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.PriceType;
import com.spark.bitrade.entity.Advertise;
import com.spark.bitrade.entity.Country;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年01月10日
 */
@Data
@Builder
public class MemberAdvertise {
    private long id;
    private AdvertiseType advertiseType;
    /**
     * 最低单笔交易额
     */
    private BigDecimal minLimit;

    /**
     * 最高单笔交易额
     */
    private BigDecimal maxLimit;
    private AdvertiseControlStatus status;
    private BigDecimal remainAmount;

    private String coinUnit;

    /**
     * 单价
     */
    private BigDecimal price;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

//    private Country country;

    private PriceType priceType; //add by tansitao 时间： 2018/5/20 原因：增加价格类型

    private BigDecimal premiseRate; //add by tansitao 时间： 2018/5/20 原因：增加溢价百分比

    public static MemberAdvertise toMemberAdvertise(Advertise x) {
        return MemberAdvertise.builder().id(x.getId())
                .advertiseType(x.getAdvertiseType())
                .coinUnit(x.getCoin().getUnit())
                .createTime(x.getCreateTime())
                .minLimit(x.getMinLimit())
                .maxLimit(x.getMaxLimit())
                .remainAmount(x.getRemainAmount())
                .status(x.getStatus())
//                .country(x.getCountry())
                .priceType(x.getPriceType())
                .price(x.getPrice())
                .premiseRate(x.getPremiseRate())
                .build();
    }

}
