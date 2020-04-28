package com.spark.bitrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 
 * @author yangch
 * @time 2018.10.07 19:21
 */
@RefreshScope
@Component
@ConfigurationProperties(prefix="member.accessAuthKey")
@Data
public class VipConfig {
    //vip用户ID及准入授权码，格式=用户ID:准入授权码
    private List<String> list = new ArrayList<>();
    private Map<String, String > map = null;

    private void initMap() {
        if(null == map){
            map = new HashMap<>();
            if(null !=list){
                list.forEach(s -> {
                    int index = s.indexOf(":");
                    if(index > 0) {
                        map.put(s.substring(0, index), s.substring(index + 1));
                    }
                });
            }
        }
    }

    public String getAccessAuthKey(String uid){
        initMap();
        return map.get(uid);
    }
}
