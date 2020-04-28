package com.spark.bitrade.messager.dto;

import com.spark.bitrade.constant.messager.JPushDeviceType;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ww
 * @time 2019.09.11 17:15
 */
@Data
public class JPushEntity {

    /**
     * 用户标签  用户 md5(member_id)
     */
    private List<String> tags = new ArrayList<>();
    /**
     * 用户别名
     */
    private List<String> alias = new ArrayList<>();
    /**
     * 额外参数（可用作业务方面）
     */
    private Map<String, String> extras = new HashMap<>();
    /**
     * 推送设备类型
     */
    private List<JPushDeviceType> deviceType = new ArrayList<>();
    /**
     * 通知消息内用（通知栏）
     */
    private String title;
    /**
     * 应用内消息内容
     */

    private String subTitle;
    /**
     * 应用内消息内容
     */

    private String jsonData;
    /**
     * 角标数字为 5（不清楚可以不修改）
     */
    private Integer badge = 5;
}
