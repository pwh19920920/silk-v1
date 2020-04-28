package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.entity.GuessCoin;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.NetworkTool;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏币种service
 * @author Zhang Yanjun
 * @time 2018.09.12 17:16
 */
@Service
public class GuessCoinService {

    /**
     * 获取游戏币种信息
     * @author Zhang Yanjun
     * @time 2018.09.13 9:40
     * @param id  非小号币种id（如：bitcoin）
     */
    @Cacheable(cacheNames = "guessCoin" ,key = "'entity:guessCoin:'+#id")
    public GuessCoin getGuessCoin(String id){
        return getGuessCoinRealtime(id);
    }

    public GuessCoin getGuessCoinRealtime(String id){
        NetworkTool networkTool=new NetworkTool();
        String str=networkTool.getContent("https://public.bqi.com/public/v1/ticker?code="+id);//非小号外部获取币种信息接口
        JSONArray jsonArray= JSON.parseArray(str);
        JSONObject jsonObject=jsonArray.getJSONObject(0);
//        str=str.replace("\"","");
//        str=str.replace("[{","");
//        str=str.replace("}]","");
//        String[] str1=str.split(",");
//        Map<String,String> map=new HashMap<>();
        GuessCoin guessCoin=new GuessCoin();
//        for (int i=0;i<str1.length;i++){
//            String[] str2=str1[i].split(":");
//            map.put(str2[0],str2[1]);
//        }
        guessCoin.setId(jsonObject.getString("id"));
        guessCoin.setName(jsonObject.getString("name"));
        guessCoin.setSymbol(jsonObject.getString("symbol"));
        guessCoin.setPriceUsd(jsonObject.getString("price_usd"));
        String lastUpdated=DateUtil.stampToDate(jsonObject.getString("last_updated"));//时间戳转换
        guessCoin.setLastUpdated(lastUpdated);
        guessCoin.setLastRedisTime(DateUtil.getDateTime());
        return guessCoin;
    }

    /**
     * 清除缓存
     * @author Zhang Yanjun
     * @time 2018.09.13 9:40
     * @param id  非小号币种id（如：bitcoin）
    */
    @CacheEvict(cacheNames = "guessCoin" ,key = "'entity:guessCoin:'+#id")
    public void flushGuessCoin(String id){ }

}
