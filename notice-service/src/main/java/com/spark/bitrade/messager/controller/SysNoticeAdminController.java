package com.spark.bitrade.messager.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.constant.Language;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.entity.messager.SysNoticeEntity;
import com.spark.bitrade.messager.entity.BaseNoticeEntity;
import com.spark.bitrade.messager.service.INoticeService;
import com.spark.bitrade.messager.service.ISysNoticeService;
import com.spark.bitrade.util.MessageResult;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.*;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author ww
 * @time 2019.09.10 10:40
 */

@RequestMapping("/sys/admin")
@RestController
@Component

@Api(value = "系统通知 管理员接口" ,tags = "系统通知 管理员接口")

public class SysNoticeAdminController extends NoticeController {


    @Autowired
    @Qualifier("sysNoticeServiceImpl")
    ISysNoticeService sysNoticeService;

    @Autowired
    INoticeService noticeService;


    @ApiOperation(value="取得用户的系统通知", notes="取得用户的系统通知 ",httpMethod = "POST")


    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId",value = "查询的用户ID"),
            @ApiImplicitParam(name = "start",value = "查询的起始ID 或 上一条ID 默认 从最新一条通知开始"),
            @ApiImplicitParam(name = "status",value = "查询的通知状态  0 ：未读/ 1 ：已读 / 默认为所有" ),
            @ApiImplicitParam(name = "size",value = "查询的条数   默认为20条"),
    })

    //按用户ID 取得相应 通知
    @RequestMapping("/list")
    public MessageResult getSysNoticeByUserId(@RequestParam(name = "memberId",defaultValue = "-1") Long memberId,
                                              @RequestParam(name = "start", defaultValue = "-1") Long start,
                                              @RequestParam(name = "status", defaultValue = "-1") int status,
                                              @RequestParam(name = "size", defaultValue = "20") int size){

        List<NoticeEntity>  noticeEntities = sysNoticeService.getLastSysNotice(memberId,status,size,start);

        List<BaseNoticeEntity> baseNoticeEntities = new ArrayList<>();
        for(NoticeEntity ne: noticeEntities){
            baseNoticeEntities.add(new BaseNoticeEntity(ne));
        }
       return  MessageResult.getSuccessInstance(NoticeType.SYS_NOTICE.getLable(),baseNoticeEntities);

    }


    //@ApiOperation(value = "发送消息体NoticeEntity" ,notes = "发送消息体NoticeEntity",httpMethod = "POST")
    //send -----begin----------------
    @ApiOperation(value = "发送消息体NoticeEntity" ,notes = "发送消息体NoticeEntity",httpMethod = "POST")

    //@RequestMapping("send")
    @RequestMapping("/send/noticeEntity")
    public MessageResult sysNoticeSend(@RequestBody  NoticeEntity noticeEntity) {
        send(noticeEntity);

        return  MessageResult.success();
    }






    @ApiOperation(value = "发送消息体SysNoticeEntity",notes = "发送消息体SysNoticeEntity",httpMethod = "POST")

    @RequestMapping("/send/sysNoticeEntity")
    public MessageResult sysNoticeEntitySend(@RequestBody SysNoticeEntity sysNoticeEntity,  @RequestParam(defaultValue = "") String deviceType,
                                             @RequestParam(defaultValue = "" ,required = false) String language,
                                             int isAlert, int isOfline
                                             ) {

        NoticeEntity noticeEntity = new NoticeEntity();
//        if(StringUtil.isNullOrEmpty(sysNoticeEntity.getUrl())){
//            sysNoticeEntity.setSubNoticeType(NoticeType.SYS_NOTICE_BASE);
//        }else{
//            sysNoticeEntity.setSubNoticeType(NoticeType.SYS_NOTICE_FORWARD);
//        }

        try{
            if(!StringUtil.isNullOrEmpty(deviceType)){
                for (String s: deviceType.trim().split(" ")
                     ) {
                    noticeEntity.getDeviceType().add(JPushDeviceType.valueOf(s.toUpperCase()));
                }
            }
        }catch (Exception e){
            return  MessageResult.error("device type error");
        }

        try{
            if(!StringUtil.isNullOrEmpty(language)) {
                for(String s :language.split("\\s+")){
                    noticeEntity.getLanguage().add(Language.valueOf(s));
                }
            }
        }catch (Exception e){
            return  MessageResult.error("Language type error");
        }


        noticeEntity.setIsAlert(isAlert);
        noticeEntity.setIsOffline(isOfline);
        //noticeEntity.setMemberId( sysNoticeEntity.getMemberId());
        noticeEntity.setNoticeType(NoticeType.SYS_NOTICE);
        noticeEntity.setData(sysNoticeEntity);
        return send(noticeEntity);

    }





    @ApiOperation(value="给的用户发送系统通知", notes="deviceType: ANDROID(\"android\"),IOS(\"ios\"), WINPHONE(\"winphone\"), WEB(\"web\"),  ALL(\"all\");   \n" +
            "memberId : 用户 ID　　　（0:所有人）  \n" +
            "language : zh_CN(\"zh_CN\"), zh_TW(\"zh_TW\"), en_US(\"en_US\"), ko_KR(\"ko_KR\");"
            ,httpMethod = "POST")


    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId",value = "接收通知的用户ID",required = true,defaultValue = "0"),
            @ApiImplicitParam(name = "title",value = "通知的标题",required = true),
            @ApiImplicitParam(name = "subTitle",value = "通知的副标题", defaultValue = ""),
            @ApiImplicitParam(name = "content",value = "通知内容",required = true),
            @ApiImplicitParam(name = "url",value = "跳转的URL", defaultValue = ""),
            @ApiImplicitParam(name = "deviceType",value = "接收的平台类型  all:所有设备 / android:安卓 / ios: IOS ",defaultValue = ""),
            @ApiImplicitParam(name = "language",value = "接收的前端的语言类型限制  / en_US / ko_KR / zh_TW / zh_CN /默认：所有语言的终端", defaultValue = ""),
            @ApiImplicitParam(name = "isAlert",value = "通知是否弹出  0: 不弹出， 1:弹出  默认为1", defaultValue = "1"),
            @ApiImplicitParam(name = "isOffline",value = "通知是否为离线通知  0: 在线通知 ， 1:离线通知  ,默认为 1", defaultValue = "1"),
//
    })


    @RequestMapping(value = "/send",method = RequestMethod.POST)
    public MessageResult sysNoticeSend(@RequestParam(value = "memberId" ,defaultValue = "0" ,required = true) Long memberId,
                                       @RequestParam(value = "title" ) String title,
                                       @RequestParam(value = "subTitle" ,defaultValue = "" )String subTitle,
                                       @RequestParam(value = "content" ,defaultValue = "" ,required = true) String content,
                                       @RequestParam(value = "url" ,defaultValue = "" )String url,
                                       @RequestParam(value = "deviceType" ,defaultValue = "" ,required = true)String deviceType,
                                       @RequestParam(value = "language" ,defaultValue = "")String language,
                                       @RequestParam(value = "isAlert" ,defaultValue = "1")int isAlert,
                                       @RequestParam(value = "isOffline" ,defaultValue = "1")int isOffline
    ) {


        //notice TYPE
        //


        if(StringUtil.isNullOrEmpty(title)){
            return  MessageResult.error("title is not null or empty");
        }
        if(StringUtil.isNullOrEmpty(content)){
            return  MessageResult.error("content is not null or empty");
        }

        SysNoticeEntity sysNoticeEntity = new SysNoticeEntity();
        sysNoticeEntity.setUrl(url);
        sysNoticeEntity.setTitle(title);
        sysNoticeEntity.setContent(content);
        sysNoticeEntity.setSubTitle(subTitle);
        sysNoticeEntity.setMemberId(memberId);


        //sysNoticeEntity.setDeviceType(JPushDeviceType.valueOf(deviceType.toUpperCase()));
        return sysNoticeEntitySend(sysNoticeEntity,deviceType,language,isAlert,isOffline);
    }



    //@ApiOperation(value="给的用户发送系统通知", notes="为了通信 方便  data 要求传入JSON {} 对象",httpMethod = "POST")

    @RequestMapping("/send/noticeEntityJson")
    public MessageResult sysNoticeSendNoticeEntity(Long memberId,String data,@RequestParam(defaultValue = "") String deviceType,@RequestParam(defaultValue = "" ,required = false) String language) {



        NoticeEntity noticeEntity = new NoticeEntity();

        try{
            if(!StringUtil.isNullOrEmpty(deviceType)){
                for (String s: deviceType.trim().split(" ")
                ) {
                    noticeEntity.getDeviceType().add(JPushDeviceType.valueOf(s.toUpperCase()));
                }
            }
        }catch (Exception e){
            return  MessageResult.error("device type error");
        }

        Object obj = null;
        try{
            obj = JSON.parseObject(data);
        }catch (Exception e){
            return MessageResult.error("data json parse error");
        }


        //noticeEntity.setMemberId(memberId);
        noticeEntity.setData(obj);
        noticeEntity.setNoticeType(NoticeType.SYS_NOTICE);

        return send(noticeEntity);
    }

    //@ApiOperation(value="给的用户发送系统通知", notes="为了通信 方便  data 要求传入JSON {} 对象",httpMethod = "POST")

    @RequestMapping("/send/sysNoticeEntityJson")
    public MessageResult sysNoticeSend(String jsonSysNoticeEntity,@RequestParam(defaultValue = "all") String deviceType,@RequestParam(defaultValue = "" ,required = false) String language,int isAlert,int isOffline) {

        try {
            SysNoticeEntity sysNoticeEntity = JSON.parseObject(jsonSysNoticeEntity, SysNoticeEntity.class);

            return sysNoticeEntitySend(sysNoticeEntity,deviceType,language,isAlert,isOffline);
        }catch (Exception e){
            return MessageResult.error("jsonSmsEntity 参数不正确");
        }
    }
    
    
    @RequestMapping("/test")
    public MessageResult test() {


        Random random  = new Random();
    //0 所有人
        sysNoticeSend(0L,"title","subtitle","content","http://www.qq.com/","",null,1,1);
        return  sysNoticeSend(0L,"title","subtitle","content","","",null ,1,1);
    }


}
