package com.spark.bitrade.messager.service;

import com.spark.bitrade.messager.entity.SysNoticeCountEntity;

/**
 * @author Administrator
 * @time 2019.10.05 23:24
 */
public interface ISysNoticeCountService {


    public SysNoticeCountEntity getSysNoticeCountEntityByMemberId(long memberId);

    public SysNoticeCountEntity saveSysNoticeCountEntity(SysNoticeCountEntity sysNoticeCountEntity);

}
