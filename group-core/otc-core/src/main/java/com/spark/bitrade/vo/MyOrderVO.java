package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.entity.Country;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Zhang Yanjun
 * @time 2019.02.21 13:58
 */
@Data
public class MyOrderVO {
    private String orderSn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private Long noticeTime;  //更新时间
    private String unit;
    private AdvertiseType type;
    private String name;
    private BigDecimal price;
    private BigDecimal money;
    private BigDecimal commission;
    private BigDecimal amount;
    private OrderStatus status;
    private Long memberId;
    //add by tansitao 时间： 2018/4/24 原因：添加付款码
    private String payCode;
//    private Country country;
    private Long currencyId;
    private AdvertiseType advertiseType;

    /**
     * 法币名称
     */
    private String currencyName;

    /**
     * 法币单位
     */
    private String currencyUnit;

    /**
     * 法币符号
     */
    private String currencySymbol;
    private String countryName;//add by tansitao 时间： 2018/8/15 原因：增加国家名字
    private BooleanEnum isManualCancel; //add by tansitao 时间： 2018/12/28 原因：增加是否为手动取消

    public long getNoticeTime() {
        if(noticeTime == null){
            return createTime.getTime();
        }
        return noticeTime;
    }
}
