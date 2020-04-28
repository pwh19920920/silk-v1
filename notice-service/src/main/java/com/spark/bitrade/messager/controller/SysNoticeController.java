package com.spark.bitrade.messager.controller;

import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.entity.messager.SysNoticeEntity;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.messager.annotation.UserLoginToken;
import com.spark.bitrade.messager.entity.BaseNoticeEntity;
import com.spark.bitrade.messager.entity.SysNoticeCountEntity;
import com.spark.bitrade.messager.service.INoticeService;
import com.spark.bitrade.messager.service.ISysNoticeCountService;
import com.spark.bitrade.messager.service.ISysNoticeService;
import com.spark.bitrade.messager.service.IMemberInfoService;
import com.spark.bitrade.util.MessageResult;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.spark.bitrade.messager.constant.HttpConstant.LOGINED_MEMEBER;

/**
 * @author ww
 * @time 2019.09.10 10:40
 */

@RequestMapping("/sys")
@RestController
@Component

@Api(value = "系统通知 用户接口" ,tags = "系统通知 用户接口")

public class SysNoticeController extends NoticeController {


    @Autowired
    @Qualifier("sysNoticeServiceImpl")
    ISysNoticeService sysNoticeService;


    @Autowired
    ISysNoticeCountService sysNoticeCountService;

    @Autowired
    IMemberInfoService memberInfoService;

    @Autowired
    INoticeService noticeService;





    @ApiOperation(value = "取得指定用户的系统通知", notes = "取得指定用户的系统通知 ", httpMethod = "POST")



    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "指定查询页数 从第一页开始"),
            @ApiImplicitParam(name = "status",value = "指定查询的通知状态  0 ：未读/ 1 ：已读 / 默认为所有"),
            @ApiImplicitParam(name = "size",value = "指定查询的条数   默认为20条"),
    })
    //按用户ID 取得相应 通知

    @UserLoginToken
    @RequestMapping("/list/page")
    public MessageResult getSysNoticeByUserId(@RequestParam(required = false, defaultValue = "1") int page,
                                              @RequestParam(required = false, defaultValue = "-1") int status,
                                              @RequestParam(required = false, defaultValue = "20") int size,
                                              HttpServletRequest request) {


        AuthMember authMember =  (AuthMember) request.getAttribute(LOGINED_MEMEBER);

        List<SysNoticeEntity> sysNoticeEntities = sysNoticeService.getLastSysNoticePage(authMember.getId(), status, size, page);


        BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity();
        baseNoticeEntity.setAction(NoticeType.SYS_NOTICE.getLable());
        baseNoticeEntity.setData(sysNoticeEntities);

        SysNoticeCountEntity userSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(0);
        SysNoticeCountEntity allSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(authMember.getId());
        baseNoticeEntity.getExtras().put("totalCount", userSysNoticeCountEntity.getTotalCount()+allSysNoticeCountEntity.getTotalCount());
        baseNoticeEntity.getExtras().put("unreadCount", userSysNoticeCountEntity.getUnreadCount() + allSysNoticeCountEntity.getUnreadCount());

        return MessageResult.getSuccessInstance(NoticeType.SYS_NOTICE.getLable(), baseNoticeEntity);

    }





    @ApiOperation(value = "取得指定用户的系统通知", notes = "取得指定用户的系统通知 ", httpMethod = "POST")



    @ApiImplicitParams({
            @ApiImplicitParam(name = "start",value = "指定查询的起始ID 或 上一条ID 默认从最近一条通知开始"),
            @ApiImplicitParam(name = "status",value = "指定查询的通知状态  0 ：未读/ 1 ：已读 / 默认为所有"),
            @ApiImplicitParam(name = "size",value = "指定查询的条数   默认为20条"),
    })
    //按用户ID 取得相应 通知
    @UserLoginToken
    @RequestMapping("/list")
    public MessageResult getSysNoticeByUserId(@RequestParam(required = false, defaultValue = "-1") Long start,
                                              @RequestParam(required = false, defaultValue = "-1") int status,
                                              @RequestParam(required = false, defaultValue = "20") int size,
                                              HttpServletRequest request) {

        AuthMember authMember =  (AuthMember) request.getAttribute(LOGINED_MEMEBER);

        List<NoticeEntity> noticeEntities = sysNoticeService.getLastSysNotice(authMember.getId(), status, size, start);
        List<BaseNoticeEntity> baseNoticeEntities = new ArrayList<>();
        for (NoticeEntity ne : noticeEntities) {
            baseNoticeEntities.add(new BaseNoticeEntity(ne));
        }


        return MessageResult.getSuccessInstance(NoticeType.SYS_NOTICE.getLable(), baseNoticeEntities);

    }



    @ApiOperation(value = "通知设置为已读", notes = "通知设置为已读 ", httpMethod = "POST")

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "通知Id  , 0 则同时把所有通知设置为已读",paramType = "path"),
    })


    @UserLoginToken
    //按用户ID 取得相应 通知
    @RequestMapping(value = "/readAct",method = RequestMethod.POST)
    public MessageResult setSysNoticeReadedByMemberId(@RequestParam(required = true,defaultValue = "-1") Long id, HttpServletRequest request) {


        AuthMember authMember =  (AuthMember) request.getAttribute(LOGINED_MEMEBER);
        sysNoticeService.setStatusByIdWithMemberId(id, authMember.getId(), 1);
        return MessageResult.success();

    }


    @UserLoginToken

    @ApiOperation(value = "获取用户信息未读条数", notes = "获取用户信息未读条数 ", httpMethod = "POST")
    //按用户ID 取得相应 通知
    @RequestMapping("/getUnreadCount")
    public MessageResult getUnreadCount(HttpServletRequest request) {


        AuthMember authMember =  (AuthMember) request.getAttribute(LOGINED_MEMEBER);



        SysNoticeCountEntity userSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(0);
        SysNoticeCountEntity allSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(authMember.getId());
        //int unreadCount = sysNoticeService.getLastUnReadNoticeCountByMemberId(authMember.getId());

        BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity();
        baseNoticeEntity.setAction(NoticeType.SYS_NOTICE.getLable());
        //baseNoticeEntity.setSubAction(NoticeType.SYS_NOTICE_UNREAD_COUNT.getLable());
        baseNoticeEntity.getExtras().put("totalCount", userSysNoticeCountEntity.getTotalCount()+allSysNoticeCountEntity.getTotalCount());
        baseNoticeEntity.getExtras().put("unreadCount", userSysNoticeCountEntity.getUnreadCount() + allSysNoticeCountEntity.getUnreadCount());

        return MessageResult.getSuccessInstance(baseNoticeEntity.getAction(),baseNoticeEntity);

    }


    //按用户ID 取得相应 通知


    @ApiOperation(value = "获取用户通知", notes = "获取用户通知 ", httpMethod = "POST")

    @UserLoginToken
    @RequestMapping(value = "/get/{id}",method = RequestMethod.POST)
    public MessageResult setSysNoticeByMemberId(@PathVariable(name = "id") Long id, HttpServletRequest request) {


        AuthMember authMember =  (AuthMember) request.getAttribute(LOGINED_MEMEBER);
        SysNoticeEntity sysNoticeEntity = sysNoticeService.getSysNoticeByIdWithMemberId(id, authMember.getId());

        BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity();
        baseNoticeEntity.setAction(NoticeType.SYS_NOTICE.getLable());
        //baseNoticeEntity.setSubAction(sysNoticeEntity);
        baseNoticeEntity.setData(sysNoticeEntity);


        return MessageResult.getSuccessInstance(baseNoticeEntity.getAction(),baseNoticeEntity);

    }



}
