package com.spark.bitrade.entity;

import com.spark.bitrade.constant.AdvertiseRankType;
import com.spark.bitrade.constant.AdvertiseType;

import java.util.Comparator;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.23 16:35  
 */
public class OrderComparator implements Comparator<MemberOrderCount> {

    private AdvertiseRankType advertiseRankType;

    private AdvertiseType advertiseType;

    public OrderComparator(AdvertiseRankType advertiseRankType,AdvertiseType advertiseType) {
        this.advertiseRankType = advertiseRankType;
        this.advertiseType=advertiseType;
    }

    @Override
    public int compare(MemberOrderCount o1, MemberOrderCount o2) {
        int price = o1.getPrice().compareTo(o2.getPrice());
        if(advertiseType==AdvertiseType.BUY){
            price = o2.getPrice().compareTo(o1.getPrice());
        }
        int hasT = o2.getHasTrade() - o1.getHasTrade();
        Long tra = o1.getTradingCounts() - o2.getTradingCounts();
        Long tra48 = o1.getCount48() - o2.getCount48();
        int money = o1.getMoney48().compareTo(o2.getMoney48());
        int sort=o2.getSort()-o1.getSort();
        int c;
        if (advertiseRankType == AdvertiseRankType.PRICE) {
            c=price;
        }else {
            c=hasT;
        }
        if(sort==0){
            if (c == 0) {
                if (tra == 0) {
                    if (tra48 == 0) {
                        return money;
                    } else {
                        return tra48.intValue();
                    }
                } else {
                    return tra.intValue();
                }
            } else {
                return c;
            }
        }else {
            return sort;
        }
    }
}
