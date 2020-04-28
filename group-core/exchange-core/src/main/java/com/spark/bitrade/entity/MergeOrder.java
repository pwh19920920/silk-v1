package com.spark.bitrade.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MergeOrder {
    private List<ExchangeOrder> orders = new ArrayList<>();

    //最后位置添加一个
    public void add(ExchangeOrder order){
        orders.add(order);
    }


    public ExchangeOrder get(){
        return orders.get(0);
    }

    public int size(){
        return orders.size();
    }

    public BigDecimal getPrice(){
        return orders.get(0).getPrice();
    }

    //add by yangch 时间： 2018.10.26 原因：获取当前价位的所有订单信息
    public List<ExchangeOrder> getOrders(){
        return orders;
    }

    public Iterator<ExchangeOrder> iterator(){
        return orders.iterator();
    }
}
