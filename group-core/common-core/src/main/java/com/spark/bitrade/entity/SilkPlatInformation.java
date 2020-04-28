package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * 平台消息内容
 *
 * @author zhongxj
 * @date 2019.09.11
 */
@TableName("silk_plat_information")
@Data
public class SilkPlatInformation {
    /**
     * 自增长ID
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    /**
     * 事件类型{0:充值到账提醒,1:新订单创建提醒,2:交易即将过期,3:已付款提醒,4:已释放提醒,5:申诉处理结果(成功)提醒,6:申诉处理结果(失败用户权限被冻结)提醒,7:申诉处理结果(失败被冻结前警告)提醒,8:收到了C2C用户聊天消息,9:商家认证审核通过}
     */
    private Integer infoType;
    /**
     * 是否开启短信{0:无此通道,1:开启,2:关闭}
     */
    private Integer useSms;
    /**
     * 是否开启邮件{0:无此通道,1:开启,2:关闭}
     */
    private Integer useEmail;
    /**
     * 是否开启站内信{0:无此通道,1:开启,2:关闭}
     */
    private Integer useInstation;
    /**
     * 是否开启C2C聊天消息{0:无此通道,1:开启,2:关闭}
     */
    private Integer useChat;
    /**
     * 是否开启离线消息通知{0:无此通道,1:开启,2:关闭}
     */
    private Integer useOffline;
    /**
     * 繁体中文短信内容
     */
    private String smsContentTraditional;
    /**
     * 简体中文短信内容
     */
    private String smsContentCn;
    /**
     * 英文短信内容
     */
    private String smsContentEn;
    /**
     * 中文站内信内容
     */
    private String instationContentCn;
    /**
     * 英文站内信内容
     */
    private String instationContentEn;
    /**
     * 中文离线内容
     */
    private String offlineContentCn;
    /**
     * 英文离线内容
     */
    private String offlineContentEn;
    /**
     * 中文邮件内容
     */
    private String emailContentCn;
    /**
     * 英文邮件内容
     */
    private String emailContentEn;
    /**
     * 中文C2C内容
     */
    private String chatContentCn;
    /**
     * 英文C2C内容
     */
    private String chatContentEn;
    /**
     * 中文站内信标题
     */
    private String instationTitleCn;
    /**
     * 中文邮件标题
     */
    private String emailTitleCn;
    /**
     * 中文离线标题
     */
    private String offlineTitleCn;
    /**
     * 接收方{0:不限,1:买方,2:卖方}
     */
    private Integer receiveType;
    /**
     * 英文站内信标题
     */
    private String instationTitleEn;
    /**
     * 英文邮件标题
     */
    private String emailTitleEn;
    /**
     * 英文离线标题
     */
    private String offlineTitleEn;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 编辑时间
     */
    private Date updateTime;
}
