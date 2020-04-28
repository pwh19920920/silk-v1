package com.spark.bitrade.controller;

import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.IUserConfigurationCenterService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.UserConfigurationCenterService;
import com.spark.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.isTrue;

/**
 * 个人配置中心
 *
 * @author Zhongxj
 * @date 2019年09月11日
 */
@Slf4j
@RestController
@RequestMapping("/userConfigurationCenter")
@Api(description = "消息提醒-个人中心配置")
public class UserConfigurationCenterController extends BaseController {
    @Autowired
    private IUserConfigurationCenterService iUserConfigurationCenterService;
    @Autowired
    private UserConfigurationCenterService userConfigurationCenterService;
    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * 获取会员当前配置列表
     *
     * @param user 会员ID
     * @return 会员当前配置列表
     */
    @ApiOperation(value = "获取会员当前配置列表", notes = "获取会员当前配置列表")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public MessageResult list(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        return userConfigurationCenterService.listUserConfigurationCenter(user.getId());
    }

    /**
     * 保存会员当前配置列表
     *
     * @param triggeringEvent 事件
     * @param channel         渠道，isSms-短信，isEmail-邮件，isApns-APP
     * @param user            会员信息
     * @param status          状态 1开启，2关闭
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public MessageResult save(Integer triggeringEvent, String channel, @SessionAttribute(SESSION_MEMBER) AuthMember user, Integer status) {
        Integer count = iUserConfigurationCenterService.saveUserConfigurationCenter(triggeringEvent, channel, user.getId(), status);
//        isTrue(count > 0, msService.getMessage("EDIT_ERROR"));
        return MessageResult.success(msService.getMessage("INTERNATION_SUCCESS"));
    }

    /**
     * 获取当前会员，需要发送的消息通道
     *
     * @param memberId        会员ID
     * @param triggeringEvent 事件
     * @return 当前会员，需要发送的消息通道
     */
    @RequestMapping(value = "/getUserConfigurationCenterByMemberAndEvent", method = RequestMethod.POST)
    public MessageResult getUserConfigurationCenterByMemberAndEvent(Integer triggeringEvent, Long memberId) {
        MessageResult result = success();
        result.setData(userConfigurationCenterService.getUserConfigurationCenterByMemberAndEvent(memberId, triggeringEvent));
        return result;
    }
}
