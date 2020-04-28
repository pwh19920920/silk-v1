package com.spark.bitrade.messager.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.messager.SysNoticeEntity;
import com.spark.bitrade.messager.model.MemberMailEntity;
import com.spark.bitrade.messager.service.IMemberMailService;
import com.spark.bitrade.messager.service.INoticeService;
import com.spark.bitrade.messager.service.ISysNoticeService;
import com.spark.bitrade.util.MessageResult;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * @author ww
 * @time 2019.09.10 10:40
 */

@RequestMapping("/member/mail/admin")
@RestController
@Component

@Api(value = "站内信 管理员接口" ,tags = "站内信 管理员接口")

public class MemberMailAdminController  {


    @Autowired
    @Qualifier("sysNoticeServiceImpl")
    ISysNoticeService sysNoticeService;

    @Autowired
    INoticeService noticeService;

    @Autowired
    IMemberMailService memberMailService;




    @ApiOperation(value="取得用户的站内信", notes="取得用户的站内信 ",httpMethod = "POST")


    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId",value = "查询的用户ID"),
            @ApiImplicitParam(name = "start",value = "查询的起始ID 或 上一条ID 默认 从最新一条站内信开始"),
            @ApiImplicitParam(name = "status",value = "查询的站内信状态  0 ：未读/ 1 ：已读 / 默认为所有" ),
            @ApiImplicitParam(name = "size",value = "查询的条数   默认为20条"),
    })

    //按用户ID 取得相应 站内信
    @RequestMapping("/list")
    public MessageResult getSysNoticeByUserId(@RequestParam(name = "memberId",defaultValue = "-1") Long memberId,
                                              @RequestParam(name = "start", defaultValue = "-1") Long start,
                                              @RequestParam(name = "status", defaultValue = "-1") int status,
                                              @RequestParam(name = "size", defaultValue = "20") int size){


        List<MemberMailEntity> memberMailEntities = memberMailService.getLastMails(memberId,status,size,start);

        /*List<NoticeEntity>  noticeEntities = sysNoticeService.getLastSysNotice(memberId,status,size,start);

        List<BaseNoticeEntity> baseNoticeEntities = new ArrayList<>();
        for(NoticeEntity ne: noticeEntities){
            baseNoticeEntities.add(new BaseNoticeEntity(ne));
        }*/

       return  MessageResult.getSuccessInstance(MemberMailEntity.class.getSimpleName(),memberMailEntities);

    }


    //@ApiOperation(value = "发送消息体NoticeEntity" ,notes = "发送消息体NoticeEntity",httpMethod = "POST")
    //send -----begin----------------
    @ApiOperation(value = "发送消息体NoticeEntity" ,notes = "发送消息体NoticeEntity",httpMethod = "POST")

    //@RequestMapping("send")
    @RequestMapping("/send/memberMailEntity")
    public MessageResult memberMailSend(@RequestBody  MemberMailEntity memberMailEntity) {

        if(memberMailService.insert(memberMailEntity)>0){

            return  MessageResult.getSuccessInstance("SUCCESS",memberMailEntity);
        }
        return  MessageResult.error("ERROR");
    }




    @ApiOperation(value="给的用户发送站内信", notes="deviceType: ANDROID(\"android\"),IOS(\"ios\"), WINPHONE(\"winphone\"), WEB(\"web\"),  ALL(\"all\");   \n" +
            "memberId : 用户 ID　　　（0:所有人）  \n" +
            "language : zh_CN(\"zh_CN\"), zh_TW(\"zh_TW\"), en_US(\"en_US\"), ko_KR(\"ko_KR\");"
            ,httpMethod = "POST")


    @ApiImplicitParams({
            @ApiImplicitParam(name = "toMemberId",value = "接收站内信的用户ID",required = true,defaultValue = "0"),
            @ApiImplicitParam(name = "subject",value = "站内信的标题",required = true),
            @ApiImplicitParam(name = "content",value = "站内信内容",required = true),
            @ApiImplicitParam(name = "url",value = "跳转的URL", defaultValue = ""),
            @ApiImplicitParam(name = "deviceType",value = "接收的平台类型  all:所有设备 / android:安卓 / ios: IOS ",defaultValue = ""),
            @ApiImplicitParam(name = "language",value = "接收的前端的语言类型限制  / en_US / ko_KR / zh_TW / zh_CN /默认：所有语言的终端", defaultValue = ""),
            @ApiImplicitParam(name = "isAlert",value = "站内信是否弹出  0: 不弹出， 1:弹出  默认为1", defaultValue = "1"),
            @ApiImplicitParam(name = "isOffline",value = "站内信是否为离线站内信  0: 在线站内信 ， 1:离线站内信  ,默认为 1", defaultValue = "1"),
//
    })


    @RequestMapping(value = "/send",method = RequestMethod.POST)
    public MessageResult memberMailSend(@RequestParam(value = "toMemberId" ,defaultValue = "0" ,required = true) Long toMemberId,
                                       @RequestParam(value = "subject" ) String subject,
                                       @RequestParam(value = "content" ,defaultValue = "" ,required = true) String content,
                                       @RequestParam(value = "url" ,defaultValue = "" )String url,
                                       @RequestParam(value = "deviceType" ,defaultValue = "" ,required = true)String deviceType,
                                       @RequestParam(value = "language" ,defaultValue = "")String language,
                                       @RequestParam(value = "isAlert" ,defaultValue = "1")int isAlert,
                                       @RequestParam(value = "isOffline" ,defaultValue = "1")int isOffline
    ) {


        //notice TYPE
        //


        if(StringUtil.isNullOrEmpty(subject)){
            return  MessageResult.error("subject is not null or empty");
        }
        if(StringUtil.isNullOrEmpty(content)){
            return  MessageResult.error("content is not null or empty");
        }

        MemberMailEntity memberMailEntity = new MemberMailEntity();

        SysNoticeEntity sysNoticeEntity = new SysNoticeEntity();
        memberMailEntity.setSubject(subject);
        memberMailEntity.setContent(content);
        memberMailEntity.setToMemberId(toMemberId);
        memberMailEntity.setFromMemberId(0L);
        //sysNoticeEntity.setDeviceType(JPushDeviceType.valueOf(deviceType.toUpperCase()));
        return memberMailSend(memberMailEntity);
    }


    //@ApiOperation(value="给的用户发送站内信", notes="为了通信 方便  data 要求传入JSON {} 对象",httpMethod = "POST")


    //@ApiOperation(value="给的用户发送站内信", notes="为了通信 方便  data 要求传入JSON {} 对象",httpMethod = "POST")

    @RequestMapping("/send/memberMailJson")
    public MessageResult memberMailSend(String jsonMemeberMailEntity,@RequestParam(defaultValue = "all") String deviceType,@RequestParam(defaultValue = "" ,required = false) String language,int isAlert,int isOffline) {

        try {
            MemberMailEntity memberMailEntity = JSON.parseObject(jsonMemeberMailEntity, MemberMailEntity.class);

            return memberMailSend(memberMailEntity);
        }catch (Exception e){
            return MessageResult.error("jsonSmsEntity 参数不正确");
        }
    }
    
    
    @RequestMapping("/test")
    public MessageResult test() {


        Random random  = new Random();
    //0 所有人
        //sysNoticeSend(0L,"title","subtitle","content","http://www.qq.com/","",null,1,1);
        //return  sysNoticeSend(0L,"title","subtitle","content","","",null ,1,1);
        return MessageResult.success();
    }


}
