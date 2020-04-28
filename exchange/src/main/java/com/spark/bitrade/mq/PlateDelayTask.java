package com.spark.bitrade.mq;

import com.spark.bitrade.constant.ExchangeOrderDirection;
import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *  盘口延迟推送任务
 *
 * @author young
 * @time 2019.09.18 14:30
 */
@Data
public class PlateDelayTask implements Delayed {
    /**
     * 交易对
     */
    private String symbol;

    /**
     * 交易方向
     */
    private ExchangeOrderDirection direction;

    /**
     * ms,执行时间
     */
    private Long executeTime;

    /**
     * @param delayTime 延迟时长，单位毫秒
     * @param symbol    交易对
     * @param direction 交易方向
     */
    public PlateDelayTask(long delayTime, String symbol, ExchangeOrderDirection direction) {
        this.executeTime = System.currentTimeMillis() + delayTime;
        this.symbol = symbol;
        this.direction = direction;
    }

    /**
     * 计算当前时间到执行时间之间还有多长时间
     *
     * @param unit
     * @return
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
            return 1;
        } else if (this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        //实例 或交易对和交易方向相同就可以认为相同
        if (obj == null) {
            return false;
        } else {
            if (this == obj) {
                //实例相同
                return true;
            } else if (this.symbol == null || this.direction == null) {
                return false;
            } else if (obj instanceof PlateDelayTask) {
                PlateDelayTask c = (PlateDelayTask) obj;
                if (this.symbol.equals(c.symbol) && this.direction == c.direction) {
                    //任务相同
                    return true;
                }
            }
        }
        return false;
    }
}
