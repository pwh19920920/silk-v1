package com.spark.bitrade.cache;

import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.entity.TradePlate;

import java.util.HashMap;
import java.util.Map;

/***
 * 
 * @author yangch
 * @time 2018.09.04 16:56
 */
public class CoinCacheProcessor {
    private String symbol;
    private CoinThumb coinThumb;
    private Map<String,KLine> realtimePeriodKlineMap; //实时k线集合

    //卖盘盘口信息
    private TradePlate sellTradePlate;
    //买盘盘口信息
    private TradePlate buyTradePlate;

    //是否暂时处理
    private Boolean isHalt = true;

    public CoinCacheProcessor(String symbol){
        this.symbol = symbol;
        this.realtimePeriodKlineMap = new HashMap<>();
    }

    public void setCoinThumb(CoinThumb coinThumb) {
        this.coinThumb = coinThumb;
    }

    public CoinThumb getThumb() {
        return coinThumb;
    }

    public String getSymbol(){
        return symbol;
    }

    /***
      * 设置盘口信息
      * @author yangch
      * @time 2018.06.29 14:35 
     * @param tradePlate 盘口信息
     */
    public void setTradePlate(TradePlate tradePlate){
        if(tradePlate.getDirection() == ExchangeOrderDirection.BUY){
            this.buyTradePlate = tradePlate;
        } else if(tradePlate.getDirection() == ExchangeOrderDirection.SELL){
            this.sellTradePlate = tradePlate;
        }
    }
    /***
      * 获取盘口信息
      * @author yangch
      * @time 2018.06.29 14:36 
     * @param direction 买方方向
     */
    public TradePlate getTradePlate(ExchangeOrderDirection direction){
        if(direction == ExchangeOrderDirection.BUY){
            return this.buyTradePlate;
        } else if(direction == ExchangeOrderDirection.SELL){
            return this.sellTradePlate;
        }
        return null;
    }

    public void setKline(KLine kline){
        realtimePeriodKlineMap.put(kline.getPeriod(), kline);
    }

    public KLine getKLine(String period) {
        return  realtimePeriodKlineMap.get(period);
    }


    public void setIsHalt(boolean status) {
        this.isHalt = status;
    }
    public boolean isHalt(){
        return this.isHalt;
    }
}
