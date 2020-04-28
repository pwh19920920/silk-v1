package com.spark.bitrade.messager.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

/**
 * @author ww
 * @time 2019.09.29 21:20
 */
@Data
@Document("SYS_NOTICE_COUNT")
public class SysNoticeCountEntity {

    @Id
    Long memberId = 0L;
    int totalCount = 0;
    int unreadCount = 0;
}
