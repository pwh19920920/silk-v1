package com.spark.bitrade.messager.entity;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.messager.NoticeEntity;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ww
 * @time 2019.09.16 14:17
 */





@Data
public class BaseNoticeEntity {
    public BaseNoticeEntity(){

    }

    public BaseNoticeEntity(NoticeEntity noticeEntity){
        this.action  = noticeEntity.getNoticeType().getLable();
        //this.subAction  = noticeEntity.getSubNoticeType().getLable();
        this.data = noticeEntity.getData();
        //this.id = noticeEntity.id;
        this.extras = noticeEntity.getExtras();
        //this.status = noticeEntity.status;
    }

    //int status;
    //long id ;
    String action;
    //String subAction;
    Object data;
    Map<String,Object> extras = new HashMap<>();

    @Override
    public String toString(){
        return JSON.toJSONString(this);
    }
}
