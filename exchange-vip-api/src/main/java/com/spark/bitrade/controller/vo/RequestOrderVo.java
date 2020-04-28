package com.spark.bitrade.controller.vo;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>订单下单实体类</p>
 * @author tian.bo
 * @date 2018-12-7
 */
@Data
public class RequestOrderVo implements Serializable {
    private String symbol;  //交易对
    private ExchangeOrderDirection direction;   //交易方向
    private ExchangeOrderType type; //交易类型
    private BigDecimal price;   //委托价格
    private BigDecimal amount;  //委托数量

    private String customContent; //自定义内容，非必须
    private Object resultData;  //下单处理结果

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
