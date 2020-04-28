package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import lombok.Data;

import java.util.Date;

/**
 * 消息推送渠道配置表
 * @author tansitao
 * @time 2018/12/18 10:55 
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysNotificationChannelConfig {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    //消息类型
    private MonitorTriggerEvent type;

    //是否短信通知
    private BooleanEnum isSms = BooleanEnum.IS_FALSE;

    //是否邮件通知
    private BooleanEnum isEmail = BooleanEnum.IS_FALSE;

    //是否进行应用内系统消息通知
    private BooleanEnum isSystem = BooleanEnum.IS_FALSE;

    //是否进行离线通知
    private BooleanEnum isApns = BooleanEnum.IS_FALSE;

    //是否进行C2C聊天消息发送
    private BooleanEnum isChat = BooleanEnum.IS_FALSE;

    //创建时间
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;

    //修改时间
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime ;

    //数据是否可用
    private BooleanEnum usable = BooleanEnum.IS_TRUE;

}
