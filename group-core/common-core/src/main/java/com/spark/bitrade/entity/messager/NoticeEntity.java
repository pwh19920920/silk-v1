package com.spark.bitrade.entity.messager;

import com.spark.bitrade.constant.Language;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.constant.messager.NoticeTag;
import com.spark.bitrade.constant.messager.NoticeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知entity
 *
 * @author wangw
 * @date 2019.09.26
 */
@Data
@Document(NoticeTag.NOTICE)
@ApiModel(description = "通知实体类")
public class NoticeEntity {
    @Id
    Long id;

    //@ApiModelProperty("接收者的ID   0为所有人")
    //Long memberId = 0L;
    //使用列表同时支持多种设备
    @ApiModelProperty("接收者的设备类型")
    List<JPushDeviceType> deviceType = new ArrayList<>();

    @ApiModelProperty("接收者的设备语言种类")
    List<Language> language = new ArrayList<>();

    @ApiModelProperty("通知类型  已支持：SYS_NOTICE")
    NoticeType noticeType;

    long createTime = System.currentTimeMillis();
    //int status = 0;
    @ApiModelProperty("的具体数据  SysNoticeEntity")
    Object data;
    @ApiModelProperty("的具体数据  SysNoticeEntity ID")
    long noticeId;


    @ApiModelProperty("通知是否弹出  0: 不弹出， 1:弹出  默认为1")
    int isAlert = 1;
    @ApiModelProperty("通知是否为离线通知  0: 在线通知 ， 1:离线通知  ,默认为 1")
    int isOffline = 1;
    @ApiModelProperty("通知的扩展参数")

    Map<String, Object> extras = new HashMap<>();
}
