package com.spark.bitrade.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * @author shenzucai
 * @time 2018.09.04 16:31
 */
public class SingalGsonUtil {

    private static volatile Gson gson = null;

    private SingalGsonUtil() {
    }

    public static Gson getGson(){
        if(gson == null){
            synchronized (SingalGsonUtil.class){
                if(gson == null){
                    gson = new GsonBuilder().registerTypeAdapter(Double.class,
                            new JsonSerializer<Double>() {
                                @Override
                                public JsonElement serialize(Double value,
                                                             Type theType, JsonSerializationContext context) {

                                    // Keep 5 decimal digits only
                                    return new JsonPrimitive((new BigDecimal(value)).setScale(5, BigDecimal.ROUND_DOWN));
                                }
                            }).serializeNulls().create();
                }
            }
        }
        return gson;
    }
}
