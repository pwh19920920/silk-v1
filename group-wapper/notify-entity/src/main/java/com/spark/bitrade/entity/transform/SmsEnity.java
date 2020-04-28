package com.spark.bitrade.entity.transform;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.Date;

/**
 * 短信实体
 * @author tansitao
 * @time 2018/12/17 16:01 
 */
@Data
public class SmsEnity {
    public static final String MSG_SMS_HANDLER = "msg-sms-handler";
    private String toPhone; //接收地址
    private String content; //短信内容
    private String areaCode;//国家区号
    private String thirdMark; // 会员登录来源
    private Date validDate;    //短信的有效时间，如因为故障导致消息没有发送出去，则超过有效时间则不再发送消息
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
