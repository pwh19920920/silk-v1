package com.spark.bitrade.dto;

import com.spark.bitrade.constant.DeviceType;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 极光推送DTO
 *
 * @author zhongxj
 * @time 2019.8.26
 */
@Data
public class PushJGDto {
    /**
     * 用户标签
     */
    private List<String> tags;
    /**
     * 用户别名
     */
    private List<String> alias;
    /**
     * 额外参数（可用作业务方面）
     */
    private Map<String, String> extras;
    /**
     * 推送设备类型
     */
    private DeviceType deviceType;
    /**
     * 通知消息内用（通知栏）
     */
    private String alert;
    /**
     * 应用内消息内容
     */
    private String message;
    /**
     * 角标数字为 5（不清楚可以不修改）
     */
    private Integer badge = 5;
}
