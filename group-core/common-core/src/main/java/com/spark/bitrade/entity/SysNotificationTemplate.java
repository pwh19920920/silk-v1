package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.constant.NotificationType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
  * 通知内容模版
  * @author tansitao
  * @time 2018/12/18 10:55 
  */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysNotificationTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private int id;

    //消息类型
    private MonitorTriggerEvent type;

    //通知渠道类型
    private NotificationType notificationType;

    //语言
    private String language;

    //模版内容
    private String template;

    //创建时间
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;

    //修改时间
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime ;

    //数据是否可用
    private BooleanEnum usable = BooleanEnum.IS_TRUE;
}
