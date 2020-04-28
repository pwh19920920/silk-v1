package com.spark.bitrade.messager.controller;

import com.spark.bitrade.messager.annotation.UserLoginToken;
import com.spark.bitrade.messager.model.MemberMailEntity;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.messager.service.IMemberInfoService;
import com.spark.bitrade.messager.service.IMemberMailService;
import com.spark.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.spark.bitrade.messager.constant.HttpConstant.LOGINED_MEMEBER;

/**
 * @author ww
 * @time 2019.09.10 10:40
 */

@RequestMapping("/mail")
@RestController
@Component

@Api(value = "站内信 用户接口" ,tags = "站内信 用户接口")

public class MemberMailController extends NoticeController {

    @Autowired
    IMemberMailService memberMailService;

    @Autowired
    IMemberInfoService memberInfoService;



    @ApiOperation(value = "取得指定用户的站内信", notes = "取得指定用户的站内信 ", httpMethod = "POST")



//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "start",value = "指定查询的起始ID 或 上一条ID 默认从最近一条通知开始", defaultValue = "-1", paramType = "path",dataType = "Long"),
//            @ApiImplicitParam(name = "status",value = "指定查询的通知状态  0 ：未读/ 1 ：已读 / 默认为所有", defaultValue = "-1",paramType = "path",dataType = "int"),
//            @ApiImplicitParam(name = "size",value = "指定查询的条数   默认为20条", defaultValue = "-1",paramType = "path", dataType = "int"),
//    })




    @UserLoginToken
    //按用户ID 取得相应 通知
    @RequestMapping("/list")
    public MessageResult getMailsByUserId(
            @RequestParam(required = false, defaultValue = "-1") Long start,
                                              @RequestParam(required = false, defaultValue = "-1") int status,
                                              @RequestParam(required = false, defaultValue = "20") int size,

                                              HttpServletRequest request) {


        AuthMember authMember =  (AuthMember) request.getAttribute(LOGINED_MEMEBER);
        List<MemberMailEntity> memberMailEntities = memberMailService.getMailsByMemberId(authMember.getId(),size,status,start);

        return MessageResult.getSuccessInstance("", memberMailEntities);

    }

    @ApiOperation(value = "通知设置为已读", notes = "通知设置为已读 ", httpMethod = "POST")

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "通知Id  , 0 则同时把所有通知设置为已读",paramType = "path"),
    })


    @UserLoginToken
    //按用户ID 取得相应 通知
    @RequestMapping(value = "/readAct",method = RequestMethod.POST)
    public MessageResult setMemberMailReadedByMemberId(@RequestParam(required = true,defaultValue = "-1") Long id, HttpServletRequest request) {


        AuthMember authMember =  (AuthMember) request.getAttribute(LOGINED_MEMEBER);
        memberMailService.setStatusWithIdAndMemberId(id, authMember.getId(), 1);
        return MessageResult.success();

    }


   //@ApiOperation(value = "获取用户信息未读条数", notes = "获取用户信息未读条数 ", httpMethod = "POST")
    //按用户ID 取得相应 通知
   /* @RequestMapping("/getUnreadCount")
    public MessageResult getUnreadCount(HttpServletRequest request) {

        AuthMember authMember = memberInfoService.getLoginMemberByToken(request.getHeader("x-auth-token"),request.getHeader("access-auth-token"));
        if(authMember==null){
            return  MessageResult.error("请登录");
        }

        int unreadCount = sysNoticeService.getLastUnReadNoticeCountByMemberId(authMember.getId());

        BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity();
        baseNoticeEntity.setAction(NoticeType.SYS_NOTICE.getLable());
        //baseNoticeEntity.setSubAction(NoticeType.SYS_NOTICE_UNREAD_COUNT.getLable());
        baseNoticeEntity.getExtras().put("unreadCount",unreadCount);

        return MessageResult.getSuccessInstance(baseNoticeEntity.getAction(),baseNoticeEntity);

    }*/


    //按用户ID 取得相应 通知


    @ApiOperation(value = "获取用户通知", notes = "获取用户通知 ", httpMethod = "POST")

    @UserLoginToken
    @RequestMapping(value = "/get/{id}",method = RequestMethod.POST)
    public MessageResult setMemberMailByMemberId(@PathVariable(name = "id") Long id, HttpServletRequest request) {


        AuthMember authMember =  (AuthMember) request.getAttribute(LOGINED_MEMEBER);
        MemberMailEntity memberMailEntity = memberMailService.getMailWithContentByIdWithUserId(id,authMember.getId());
        if(memberMailEntity!=null) {
            //1 设置为已读
            memberMailService.setStatusWithIdAndMemberId(id,authMember.getId(),1);
        }
        return MessageResult.getSuccessInstance("",memberMailEntity);

    }



}
