package com.spark.bitrade.entity.transform;

import com.spark.bitrade.constant.CollectActionEventType;
import lombok.Data;

import java.util.Date;

/***
  *kafka事件类 
  *@author yangch
  *@time 2018.11.01 19:12
  */
@Data
public class CollectCarrier {
    /**
     * 事件类型
     */
    private CollectActionEventType collectType;
    /**
     * 用户ID，不填写则不处理
     */
    private String memberId;
    /**
     * 关联的订单号，不填写则不处理
     */
    private String refId;
    /**
     * 扩展信息
     */
    private String extend;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 本地化信息，如zh_CN,en_US
     */
    private String locale;
}
