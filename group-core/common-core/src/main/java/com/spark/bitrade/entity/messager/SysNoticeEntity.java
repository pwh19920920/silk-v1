package com.spark.bitrade.entity.messager;

import com.spark.bitrade.constant.messager.NoticeTag;
import com.spark.bitrade.constant.messager.NoticeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ww
 *  2019.09.16 10:33
 */
@Data
@Document(NoticeTag.SYS_NOTICE)
@ApiModel( description = "系统消息实体类")
public class SysNoticeEntity{
    @Id
    Long id =0L;


    @ApiModelProperty("通知子类型  已支持：SYS_NOTICE_BASE SYS_NOTICE_FORWARD")
    NoticeType subNoticeType;

    //String tag  = NoticeType.SYS_NOTICE.getLable();
    String content;
    @ApiModelProperty("通知内容")
    String title;
    @ApiModelProperty("需要跳的URL地址")
    String url;

    @ApiModelProperty("状态   0：未读、1：已读")
    int status=0;
    @ApiModelProperty("用户ID")
    Long memberId=0L;

    @ApiModelProperty("APP通知子标题")
    String subTitle;

    @ApiModelProperty("通知的扩展参数")
    Map<String,Object> extras = new HashMap<>();

    @ApiModelProperty("通知创建时间")
    long createTime = System.currentTimeMillis();

}
