package com.spark.bitrade.controller.vo;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.List;

/**
 * <p>订单号</p>
 * @author tian.bo
 * @date 2018-12-7
 */
@Data
public class OrderCancelVo {

    //订单号集合
    private List<String> orderIds;
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
