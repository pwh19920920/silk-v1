package com.spark.bitrade.entity.transform;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.Date;

/***
 * 邮件实体
 * @author yangch
 * @time 2018.10.08 11:32
 */
@Data
public class EmailEnity {
    public static final String MSG_EMAIL_HANDLER = "msg-email-handler";
    private String toEmail; //接收地址
    private String subject; //邮件主题
    private String htmlConent; //邮件内容
    private Date validDate;    //邮件的有效时间，如因为故障导致消息没有发送出去，则超过有效时间则不再发送消息

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
