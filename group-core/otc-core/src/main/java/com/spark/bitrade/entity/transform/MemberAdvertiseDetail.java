package com.spark.bitrade.entity.transform;

import com.spark.bitrade.constant.AdvertiseControlStatus;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PriceType;
import com.spark.bitrade.entity.Advertise;
import com.spark.bitrade.entity.Country;
import lombok.Builder;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @author Zhang Jinwei
 * @date 2018年01月09日
 */
@Builder
@Data
public class MemberAdvertiseDetail implements Serializable{

    private Long id;

    private Long coinId;

    private String coinName;

    private String coinNameCn;

    private String coinUnit;

    private Country country;

    private PriceType priceType;


    /**
     * 交易价格(及时变动)
     */
    private BigDecimal price;

    /**
     * 广告类型 0:买入 1:卖出
     */
    private AdvertiseType advertiseType;


    /**
     * 最低单笔交易额
     */
    private BigDecimal minLimit;

    /**
     * 最高单笔交易额
     */
    private BigDecimal maxLimit;

    /**
     * 备注
     */
    private String remark;

    /**
     * 付款期限，单位分钟
     */
    private Integer timeLimit;

    /**
     * 溢价百分比
     */
    private BigDecimal premiseRate;


    /**
     * 付费方式(用英文逗号隔开)
     */
    private String payMode;


    /**
     * 广告状态
     */
    private AdvertiseControlStatus status ;

    private BigDecimal number;

    /**
     * 市场价
     */
    private BigDecimal marketPrice;

    private BooleanEnum auto;

    private String autoword;

    //add by yangch 时间： 2018.10.24 原因： 1.3优化需求的扩展字段 --begin
    /**
     * 需要交易方已绑定手机号
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum needBindPhone = BooleanEnum.IS_FALSE;

    /**
     * 需要交易方已做实名认证
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum needRealname = BooleanEnum.IS_FALSE;

    /**
     * 需要交易方至少完成过N笔交易（默认为0）
     */
    //edit by tansitao 时间： 2018/10/25 原因：添加默认值
    private int needTradeTimes = 0;

    /**
     * 是否使用优惠币种支付（默认为0）
     */
    private BooleanEnum needPutonDiscount = BooleanEnum.IS_FALSE;
    //add by yangch 时间： 2018.10.24 原因： 1.3优化需求的扩展字段 --end

    //add by tansitao 时间： 2018/11/19 原因：同时最大处理订单数 (0 = 不限制)
    private int maxTradingOrders = 0;

    public static MemberAdvertiseDetail toMemberAdvertiseDetail(Advertise advertise){
        return MemberAdvertiseDetail.builder()
                .id(advertise.getId())
                .advertiseType(advertise.getAdvertiseType())
                .coinId(advertise.getCoin().getId())
                .coinName(advertise.getCoin().getName())
                .coinNameCn(advertise.getCoin().getNameCn())
                .coinUnit(advertise.getCoin().getUnit())
//                .country(advertise.getCountry())
                .auto(advertise.getAuto())
                .maxLimit(advertise.getMaxLimit())
                .minLimit(advertise.getMinLimit())
                .number(advertise.getNumber())
                .payMode(advertise.getPayMode())
                .premiseRate(advertise.getPremiseRate())
                .price(advertise.getPrice())
                .priceType(advertise.getPriceType())
                .remark(advertise.getRemark())
                .status(advertise.getStatus())
                .timeLimit(advertise.getTimeLimit())
                .autoword(advertise.getAutoword())
                .needBindPhone(advertise.getNeedBindPhone()) //add by tansitao 时间： 2018/11/1 原因：增加手机绑定
                .needRealname(advertise.getNeedRealname()) //add by tansitao 时间： 2018/11/1 原因：增加实名制
                .needTradeTimes(advertise.getNeedTradeTimes()) //add by tansitao 时间： 2018/11/1 原因：增加交易次数
                .needPutonDiscount(advertise.getNeedPutonDiscount()) //add by tansitao 时间： 2018/11/1 原因：增加是否折扣
                .maxTradingOrders(advertise.getMaxTradingOrders())//add by tansitao 时间： 2018/11/19 原因：同时最大处理订单数 (0 = 不限制)
                .build();
    }


}
