package com.spark.bitrade.h5game;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * H5游戏平台响应
 *
 * @author archx
 * @time 2019/4/24 17:11
 */
@Data
@Builder
public class H5Resp {

    private boolean success = false;
    private String msg = "error";
    private int status = -1;
    private JSONObject dataWrapper;

    public H5Resp() {
    }

    public H5Resp(boolean success, String msg, int status, JSONObject dataWrapper) {
        this.success = success;
        this.msg = msg;
        this.status = status;
        this.dataWrapper = dataWrapper;
    }

    /**
     * 解包参数
     *
     * @param key 参数Key
     * @param clz 参数类型
     * @return optional
     */
    public <T> Optional<T> unwrap(String key, Class<T> clz) {
        if (dataWrapper != null && dataWrapper.containsKey(key)) {
            return Optional.ofNullable(dataWrapper.getObject(key, clz));
        }
        return Optional.empty();
    }
}
