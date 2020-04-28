package com.spark.bitrade.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bian.b on 2018/8/15.
 */
@Deprecated
public class MapFactory {
    private MapFactory() {
    }

    public static Map<String, Object> getInstanceConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }
}
