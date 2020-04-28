package com.spark.bitrade;

import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.MergeOrder;

import java.util.ArrayList;
import java.util.List;

/**
 *  * 
 *  * @author yangch
 *  * @time 2018.10.26 11:14
 *  
 */
public class TestMergeOrder {

    public static void main(String[] args) {
        List<ExchangeOrder> orders = new ArrayList<>();

        MergeOrder mergeOrder = new MergeOrder();

        ExchangeOrder order1 = new ExchangeOrder();
        order1.setOrderId("order1");
        order1.setMemberId(100L);
        mergeOrder.add(order1);
        orders.add(order1);

        ExchangeOrder order2 = new ExchangeOrder();
        order2.setOrderId("order1");
        mergeOrder.add(order2);
        orders.add(order2);

        System.out.println(mergeOrder.getOrders());
        System.out.println(orders);

    }
}
