package com.spark.bitrade.model;

import com.spark.bitrade.constant.AdvertiseControlStatus;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.entity.Advertise;
import lombok.Data;

import java.util.Date;

@Data
public class AdvertiseScreen extends AccountScreen{

    AdvertiseType advertiseType;

    String payModel ;

    /**
     * 广告状态 (012  上架/下架/关闭)
     */
    AdvertiseControlStatus status ;

}
