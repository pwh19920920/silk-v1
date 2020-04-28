package com.spark.bitrade.task;

import com.spark.bitrade.entity.BettingState;

/***
 * 
 * @author yangch
 * @time 2018.09.14 14:29
 */
public interface IBettingTaskHandler {
    public void run(BettingState bettingState);
}
