package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.config.ThirdConfig;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.event.MemberEvent;
import com.spark.bitrade.ext.HttpJwtToken;
import com.spark.bitrade.ext.MemberClaim;
import com.spark.bitrade.service.*;
import com.spark.bitrade.system.GeetestLib;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.AppStartImgVo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

// import com.spark.bitrade.util.JpaSessionUtil;

/**
 * @author Zhang Jinwei
 * @date 2018年01月10日
 */
@RestController
@Slf4j
public class LoginController extends BaseController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberEvent memberEvent;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private GeetestLib gtSdk;
    @Autowired
    private MemberSecuritySetService memberSecuritySetService;
    @Autowired
    private MemberLoginHistoryService memberLoginHistoryService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThirdConfig thirdConfig;
    @Autowired
    private IdWorkByTwitter idWorkByTwitter;
    @Autowired
    private CountryService countryService;
    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private StartupImgConfigService startupImgConfigService;
    @Autowired
    private ILoginEvent iLoginEvent;

    // @Value("${geetest.enabled:true}")
    private boolean geetestEnabled = false; // 极验证开关

    @Value("#{'${login.no.vertify.id}'.split(',')}")
    private List<String> noLoginVertifyIds;

    private String thirdIdCardImg = "2018/10/16/";

    @ApiOperation(value = "获取当前登录用户信息", tags = "获取当前登录用户信息")
    @RequestMapping("/memberInfo")
    public MessageResult loginedMemberInfo(HttpServletRequest request, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        return MessageResult.getSuccessInstance("memberInfo", user);
    }

    @ApiOperation(value = "PC登录接口", tags = "登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名", name = "username", dataType = "String"),
            @ApiImplicitParam(value = "加密密码", name = "password", dataType = "String"),
            @ApiImplicitParam(
                    value = "登录类型,0:web登录 1：Android登录 2：IOS登录 3：API接入",
                    name = "type",
                    dataType = "Enum"),
            @ApiImplicitParam(value = "安全验证类型，0：手机验证，1：安全验证，不传无需验证", name = "verifyType", dataType = "int")
    })
    @RequestMapping(
            value = "/login",
            method = {RequestMethod.POST, RequestMethod.OPTIONS})
    @Transactional(rollbackFor = Exception.class)
    public MessageResult login(
            HttpServletRequest request,
            String username,
            String password,
            LoginType type,
            Integer verifyType)
            throws Exception {
        Assert.hasText(username, msService.getMessage("MISSING_USERNAME"));
        Assert.hasText(password, msService.getMessage("MISSING_PASSWORD"));

        // 1、关闭极验证
        if (!geetestEnabled) {
            try {
                return loginCheck(request, username, password, type, BooleanEnum.IS_TRUE);
            } catch (Exception e) {
                return error(msService.getMessage(e.getMessage()));
            }
        }

        String challenge = request.getParameter(GeetestLib.fn_geetest_challenge);
        String validate = request.getParameter(GeetestLib.fn_geetest_validate);
        String seccode = request.getParameter(GeetestLib.fn_geetest_seccode);

        // 2、没有极验证的登录
        if (challenge == null && validate == null && seccode == null) {
            try {
                return loginCheck(request, username, password, type, BooleanEnum.IS_TRUE);

            } catch (Exception e) {
                return error(msService.getMessage(e.getMessage()));
            }
        } else {
            // 3、通过极验证登录
            int gt_server_status_code = 1;
            if (request.getSession() != null
                    && request.getSession().getAttribute(gtSdk.gtServerStatusSessionKey) != null) {
                gt_server_status_code =
                        (Integer)
                                request
                                        .getSession()
                                        .getAttribute(gtSdk.gtServerStatusSessionKey); // 从session中获取gt-server状态
            }
            String userid = (String) request.getSession().getAttribute("userid"); // 从session中获取userid
            // 自定义参数,可选择添加
            String ip = getRemoteIp(request);
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("user_id", userid); // 网站用户id
            param.put(
                    "client_type",
                    "web"); // web:电脑上的浏览器；h5:手机上的浏览器，包括移动应用内完全内置的web_view；native：通过原生SDK植入APP应用的方式
            param.put(
                    "ip_address",
                    ip.indexOf(",") != -1
                            ? ip.substring(0, ip.indexOf(","))
                            : ip); // 传输用户请求验证时所携带的IP。备注：多IP请求会报错

            int gtResult = 0;
            if (gt_server_status_code == 1) {
                // gt-server正常，向gt-server进行二次验证
                gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, param);
            } else {
                // gt-server非正常情况下，进行failback模式验证
                gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
            }

            if (gtResult == 1) {
                // 验证成功
                try {
                    return loginCheck(request, username, password, type, BooleanEnum.IS_TRUE);
                } catch (Exception e) {
                    return error(msService.getMessage(e.getMessage()));
                }
            } else {
                // 验证失败
                return error(msService.getMessage("GEETEST_FAIL"));
            }
        }
    }

    /**
     *  * app新的登录接口  * @author tansitao  * @time 2018/10/12 18:28   
     */
    @ApiOperation(value = "APP登录接口", tags = "登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名", name = "username", dataType = "String"),
            @ApiImplicitParam(value = "加密密码", name = "password", dataType = "String"),
            @ApiImplicitParam(
                    value = "登录类型,0:web登录 1：Android登录 2：IOS登录 3：API接入",
                    name = "type",
                    dataType = "Enum"),
            @ApiImplicitParam(value = "安全验证类型，0：手机验证，1：安全验证，不传无需验证", name = "verifyType", dataType = "int")
    })
    @RequestMapping(
            value = "/appLogin",
            method = {RequestMethod.POST, RequestMethod.OPTIONS})
    @Transactional(rollbackFor = Exception.class)
    public MessageResult appLogin(
            HttpServletRequest request,
            String username,
            String password,
            LoginType type,
            Integer verifyType) {
        Assert.hasText(username, msService.getMessage("MISSING_USERNAME"));
        Assert.hasText(password, msService.getMessage("MISSING_PASSWORD"));
        try {
            return loginCheck(request, username, password, type, BooleanEnum.IS_FALSE);
        } catch (Exception e) {
            return error(msService.getMessage(e.getMessage()));
        }
    }

    /**
     *  * api授权登录接口  * @author yangch  * @time 2018/10/12 18:28   
     */
    @ApiOperation(value = "API登录接口", tags = "登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名", name = "username", dataType = "String"),
            @ApiImplicitParam(value = "加密密码", name = "password", dataType = "String")
    })
    @RequestMapping(
            value = "/apiLogin",
            method = {RequestMethod.POST, RequestMethod.OPTIONS, RequestMethod.GET})
    @Transactional(rollbackFor = Exception.class)
    public MessageResult apiLogin(HttpServletRequest request, String username, String password) {
        Assert.hasText(username, msService.getMessage("MISSING_USERNAME"));
        Assert.hasText(password, msService.getMessage("MISSING_PASSWORD"));
        // 授权校验
        // 接口签名信息
        String signMsg = request.getHeader("access-auth-sign");
        Assert.isTrue(!StringUtils.isEmpty(signMsg), msService.getMessage("AUTH_PARAM_NULL"));

        // 验证鉴权签名
        try {
            if (!checkSign(username, signMsg)) {
                return error(msService.getMessage("SIGN_FAILED3"));
            }
        } catch (Exception e) {
            log.error("============api登录接口校验码校验失败==========", e);
            return error(msService.getMessage("SIGN_FAILED3"));
        }

        try {
            return loginCheck(request, username, password, LoginType.API, BooleanEnum.IS_FALSE);
        } catch (Exception e) {
            return error(msService.getMessage(e.getMessage()));
        }
    }

    /**
     * *  * 登录验证逻辑处理  * @author yangch  * @time 2018.07.13 10:00
     *
     * @param request
     * @param username
     * @param password
     * @param type
     */
    public MessageResult loginCheck(
            HttpServletRequest request,
            String username,
            String password,
            LoginType type,
            BooleanEnum isOldApi)
            throws Exception {
        // Assert.hasText(username, messageSourceService.getMessage("MISSING_USERNAME"));
        // Assert.hasText(password, messageSourceService.getMessage("MISSING_PASSWORD"));

        Member member = memberService.login(username, password);

        // add by zyj 2018.12.26: 接入风控
        MessageResult result = iLoginEvent.login(request, type, member);
        if (result.getCode() != 0) {
            return error(result.getMessage());
        }

        MemberSecuritySet memberSecuritySet = null;

        AuthMember authMember = AuthMember.toAuthMember(member);

        // add by tansitao 时间： 2019/1/2 原因：获取登录的会员来源信息
        String thirdMark = request.getHeader("thirdMark");
        // 没有配置的渠道列表id，需要进行安全验证
        if (!noLoginVertifyIds.contains(thirdMark) || StringUtils.isEmpty(thirdMark)) {
            // 需要安全验证的才查询安全权限设置
            memberSecuritySet = memberSecuritySetService.findOneBymemberId(member.getId());

            // edit by fumy. date:2018.10.26 reason:登录验证可二选一
            if (memberSecuritySet != null) {
                // 手机登录
                if (memberSecuritySet.getIsOpenPhoneLogin() == BooleanEnum.IS_TRUE) {
                    authMember.addLoginVerifyRequire("isOpenPhoneLogin", false);
                }
                // 谷歌登录
                if (memberSecuritySet.getIsOpenGoogleLogin() == BooleanEnum.IS_TRUE) {
                    authMember.addLoginVerifyRequire("isOpenGoogleLogin", false);
                }
            }
        }
        authMember.setLoginType(type);
        // add by yangch 时间： 2019.02.25 原因：授权实体中存放渠道来源
        authMember.setPlatform(thirdMark);
        //红包过滤
        if("redPack".equals(request.getParameter("redPack"))){
            authMember.setLoginVerifyMap(null);
        }
        // add by tansitao 时间： 2018/10/10 原因：增加token逻辑
    /*String token = request.getHeader("access-auth-token");
    if(isOldApi == BooleanEnum.IS_FALSE){
        //处理默认的token
        if(token == null) {
            token = Md5.md5Digest(username+System.currentTimeMillis());
        }
        token = token + member.getId() + System.currentTimeMillis();
    }*/
        // add by yangch 时间： 2019.02.25 原因：token中附件上渠道来源
    /*if (thirdMark!=null){
        token = token.concat("$$").concat(thirdMark);
    }*/

        // add by wsy date: 2019-4-2 14:28:28 , 修改token方式为jwt
        MemberClaim claim =
                MemberClaim.builder()
                        .userId(member.getId())
                        .username(member.getUsername())
                        .audience(thirdMark)
                        .build();
        String token = HttpJwtToken.getInstance().createTokenWithClaim(claim);
        memberEvent.onLoginSuccess(
                member, request, type, DigestUtils.md5Hex(token), thirdMark); // 异步调用登录成功

        request.getSession().setAttribute(SysConstant.SESSION_MEMBER, authMember); // 保存session

        // 设置登录成功后的返回信息
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setLocation(member.getLocation());
        loginInfo.setMemberLevel(member.getMemberLevel());
        loginInfo.setUsername(member.getUsername());
        // add by  shenzucai 时间： 2019.04.23  原因：该字段放入email,phone
        loginInfo.setEmail(member.getEmail());
        String token1 = request.getSession().getId();
        loginInfo.setToken(token1);
        loginInfo.setAccessToken(token);
        loginInfo.setRealName(member.getRealName());
        loginInfo.setCountry(member.getCountry());
        loginInfo.setAvatar(member.getAvatar());
        loginInfo.setPromotionCode(member.getPromotionCode());
        loginInfo.setId(member.getId());
        loginInfo.setPhone(member.getMobilePhone());
        // 没有设置安全校验的默认为不校验
        if (org.springframework.util.StringUtils.isEmpty(memberSecuritySet)) {
            loginInfo.setIsOpenPhoneLogin(BooleanEnum.IS_FALSE);
            loginInfo.setIsOpenGoogleLogin(BooleanEnum.IS_FALSE);
            loginInfo.setIsOpenPhoneUpCoin(BooleanEnum.IS_FALSE);
            loginInfo.setIsOpenGoogleUpCoin(BooleanEnum.IS_FALSE);
        } else {
            loginInfo.setIsOpenPhoneLogin(memberSecuritySet.getIsOpenPhoneLogin());
            loginInfo.setIsOpenGoogleLogin(memberSecuritySet.getIsOpenGoogleLogin());
            loginInfo.setIsOpenPhoneUpCoin(memberSecuritySet.getIsOpenPhoneUpCoin());
            loginInfo.setIsOpenGoogleUpCoin(memberSecuritySet.getIsOpenGoogleUpCoin());
            if("redPack".equals(request.getParameter("redPack"))){
                loginInfo.setIsOpenPhoneLogin(BooleanEnum.IS_FALSE);
                loginInfo.setIsOpenGoogleLogin(BooleanEnum.IS_FALSE);
                loginInfo.setIsOpenPhoneUpCoin(BooleanEnum.IS_FALSE);
                loginInfo.setIsOpenGoogleUpCoin(BooleanEnum.IS_FALSE);
            }
        }
        // 设置用户登录信息为登录状态
        ValueOperations valueOperations = redisTemplate.opsForValue();
        if (thirdMark != null) {
            valueOperations.set(
                    SysConstant.MEMBER_LOGOUT + loginInfo.getId() + ":" + thirdMark,
                    "0",
                    30,
                    TimeUnit.MINUTES);
        } else {
            valueOperations.set(SysConstant.MEMBER_LOGOUT + loginInfo.getId(), "0", 30, TimeUnit.MINUTES);
        }
        return success(loginInfo);
    }

    /**
     * 登出
     *
     * @return
     */
    @RequestMapping(value = "/logout")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginOut(
            HttpServletRequest request, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        request.getSession().removeAttribute(SysConstant.SESSION_MEMBER);
        // 向redis中存入退出信息
        // add by zyj 2019.1.24 : 加入来源
        String thirdMark = request.getHeader("thirdMark");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        if (thirdMark != null) {
            valueOperations.set(
                    SysConstant.MEMBER_LOGOUT + user.getId() + ":" + thirdMark, "1", 30, TimeUnit.MINUTES);
        } else {
            valueOperations.set(SysConstant.MEMBER_LOGOUT + user.getId(), "1", 30, TimeUnit.MINUTES);
        }
        if (request.getSession().getAttribute(SysConstant.SESSION_MEMBER) != null) {
            return error(msService.getMessage("LOGOUT_FAILED"));
        } else {
            memberEvent.onLoginOutSuccess(request, user); // 异步调用登出方法
            return success(msService.getMessage("LOGOUT_SUCCESS"));
        }
        // return request.getSession().getAttribute(SysConstant.SESSION_MEMBER) != null ?
        // error(messageSourceService.getMessage("LOGOUT_FAILED")) :
        // success(messageSourceService.getMessage("LOGOUT_SUCCESS"));
    }

    /**
     * 检查是否登录
     *
     * @param request
     * @return
     */
    @RequestMapping("/check/login")
    public MessageResult checkLogin(HttpServletRequest request) {
        // edit by tansitao 时间： 2018/12/12 原因：修改核查登录接口，将用户信息返回到前端
        AuthMember authMember = (AuthMember) request.getSession().getAttribute(SESSION_MEMBER);
        MessageResult result = MessageResult.success();
        if (authMember != null) {
            Member member = memberService.findOne(authMember.getId());
            // 设置登录成功后的返回信息
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setLocation(member.getLocation());
            loginInfo.setMemberLevel(member.getMemberLevel());
            loginInfo.setUsername(member.getUsername());
            String token1 = request.getSession().getId();
            loginInfo.setToken(token1);
            loginInfo.setRealName(member.getRealName());
            loginInfo.setCountry(member.getCountry());
            loginInfo.setAvatar(member.getAvatar());
            loginInfo.setPromotionCode(member.getPromotionCode());
            loginInfo.setId(member.getId());
            loginInfo.setPhone(member.getMobilePhone());
            result.setData(loginInfo);
        } else {
            String token = request.getHeader("access-auth-token");
            Member member = memberService.loginWithToken(token, request.getRemoteAddr(), "");
            if (member != null) {
                // 设置登录成功后的返回信息
                LoginInfo loginInfo = new LoginInfo();
                loginInfo.setLocation(member.getLocation());
                loginInfo.setMemberLevel(member.getMemberLevel());
                loginInfo.setUsername(member.getUsername());
                String token1 = request.getSession().getId();
                loginInfo.setToken(token1);
                loginInfo.setRealName(member.getRealName());
                loginInfo.setCountry(member.getCountry());
                loginInfo.setAvatar(member.getAvatar());
                loginInfo.setPromotionCode(member.getPromotionCode());
                loginInfo.setId(member.getId());
                loginInfo.setPhone(member.getMobilePhone());
                result.setData(loginInfo);
            } else {
                result.setData(false);
            }
        }

        return result;
    }

    /**
     * 查看登录历史
     *
     * @return
     */
    @RequestMapping(value = "/loginHistory")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginHistory(
            HttpServletRequest request,
            @SessionAttribute(SESSION_MEMBER) AuthMember user,
            PageModel pageModel) {
        PageInfo<MemberLoginHistory> page =
                memberLoginHistoryService.queryPageByMemberAndActId(
                        user.getId(), pageModel.getPageNo(), pageModel.getPageSize());
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
     *  * 第三方登录  * @author tansitao  * @time 2018/10/11 15:15   
     */
    @RequestMapping(value = "/thirdPreLogin")
    public MessageResult thirdLogin(HttpServletRequest request, ThirdLogin thirdLogin)
            throws Exception {
        // 接口签名信息
        String signMsg = request.getHeader("access-auth-sign");
        String thirdMark = request.getHeader("access-third-mark");
        Assert.isTrue(
                !StringUtils.isEmpty(signMsg) && !StringUtils.isEmpty(thirdMark),
                msService.getMessage("AUTH_PARAM_NULL"));

        // 验证鉴权签名
        try {
            if (!checkSign(thirdMark, signMsg)) {
                return error(msService.getMessage("SIGN_FAILED3"));
            }
        } catch (Exception e) {
            log.error("============校验失败==========", e);
            return error(msService.getMessage("SIGN_FAILED3"));
        }

        return thirdLoginProxy(request, thirdLogin);
    }

    /**
     * 第三方登录(无需授权，存在风险，后续会移到账户服务的相关接口中)
     *
     * @param request
     * @param thirdLogin
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/thirdLoginProxy")
    public MessageResult thirdLoginProxy(HttpServletRequest request, ThirdLogin thirdLogin)
            throws Exception {
        String thirdMark = request.getHeader("access-third-mark");
        //        Assert.isTrue(!StringUtils.isEmpty(thirdMark),
        // msService.getMessage("AUTH_PARAM_NULL"));
        //        Assert.isTrue(!StringUtils.isEmpty(thirdLogin.getPhone()),
        // msService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
        if (StringUtils.isEmpty(thirdMark)) {
            return MessageResult.error(MessageCode.MISSING_PLATFORM_MARK);
        }
        if (StringUtils.isEmpty(thirdLogin.getPhone())) {
            return MessageResult.error(MessageCode.MISSING_PHONE);
        } else {
            if ("中国".equals(thirdLogin.getCountry())) {
                if (!ValidateUtil.isChinaPhoneLegal(thirdLogin.getPhone())) {
                    return MessageResult.error(MessageCode.BAD_FORMAT_PHONE);
                }
            } else {
                if (!ValidateUtil.isGeneralPhoneLegal(thirdLogin.getPhone())) {
                    return MessageResult.error(MessageCode.BAD_FORMAT_PHONE);
                }
            }
        }

        // 判断用户是否存在，如果不存在则注册
        Member member = memberService.findByPhone(thirdLogin.getPhone());
        if (member == null) {
            log.info("================用户不存在，开始注册{}=============", thirdLogin.getPhone());
            // 用户不存在，进行注册
            member = thirdRegister(thirdLogin, request);
            if (member == null) {
                return error(msService.getMessage("REGISTRATION_FAILED"));
            }
        } else {
            if (thirdLogin.getMemberLevel() == MemberLevelEnum.REALNAME
                    && member.getMemberLevel() == MemberLevelEnum.GENERAL) {
                // 会员身份证号码
                List<Member> members = memberService.findAllByIdNumber(thirdLogin.getIdNumber());
                if (members == null || members.size() == 0) {
                    log.info("================用户已存在，未实名改为已实名{}=============", thirdLogin.getPhone());
                    Assert.isTrue(
                            !StringUtils.isEmpty(thirdLogin.getRealName()),
                            msService.getMessage("REAL_NAME_NOT_EMPTY"));
                    Assert.isTrue(
                            !StringUtils.isEmpty(thirdLogin.getIdNumber()),
                            msService.getMessage("REAL_NAME_USERID_NOT_EMPTY"));
                    member.setRealName(thirdLogin.getRealName());
                    member.setIdNumber(thirdLogin.getIdNumber());
                    member.setRealNameStatus(RealNameStatus.VERIFIED);
                    member.setMemberLevel(thirdLogin.getMemberLevel());
                    memberService.save(member);

                    MemberApplication memberApplication = new MemberApplication();
                    memberApplication.setIdCard(member.getIdNumber());
                    memberApplication.setRealName(thirdLogin.getRealName());
                    memberApplication.setOpType(3);
                    memberApplication.setAuditStatus(AuditStatus.AUDIT_SUCCESS);
                    memberApplication.setMember(member);
                    memberApplication.setCountry(member.getCountry().getZhName());
                    memberApplication.setIdentityCardImgReverse(
                            thirdIdCardImg + request.getHeader("access-third-mark") + "_shiming.png");
                    memberApplication.setIdentityCardImgInHand(
                            thirdIdCardImg + request.getHeader("access-third-mark") + "_shiming.png");
                    memberApplication.setIdentityCardImgFront(
                            thirdIdCardImg + request.getHeader("access-third-mark") + "_shiming.png");
                    memberApplicationService.save(memberApplication);
                }
            }
        }

        // 进行预登陆，向redis中存入第三方登录token
        String token = UUIDUtil.getUUID();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(
                SysConstant.THIRD_TOKEN + token, thirdMark + ":" + member.getId(), 5, TimeUnit.MINUTES);

        MessageResult result = MessageResult.success();
        result.setData(token);
        return result;
    }

    /**
     *  * 授权token校验API接口  * @author tansitao  * @time 2018/9/25 22:06   
     */
    @RequestMapping("/check/token")
    public MessageResult checkToken(HttpServletRequest request, String thirdToken, LoginType type) {
        Assert.isTrue(!StringUtils.isEmpty(thirdToken), msService.getMessage("AUTH_PARAM_NULL"));
        String token = request.getHeader("access-auth-token");
        Assert.isTrue(!StringUtils.isEmpty(token), msService.getMessage("AUTH_TOKEN_ERROR"));
        MessageResult result = MessageResult.success();
        // 从redis中获取用户的第三方登录信息
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String memberPreInfo = (String) valueOperations.get(SysConstant.THIRD_TOKEN + thirdToken);
        if (StringUtils.isEmpty(memberPreInfo)) {
            log.info("================redis中无第三方token信息==================" + thirdToken);
            return error(msService.getMessage("SIGN_FAILED3"));
        }
        int index = memberPreInfo.indexOf(":");
        String thirdMark = memberPreInfo.substring(0, index);
        String uid = memberPreInfo.substring(index + 1);
        Member member = memberService.findOne(Long.parseLong(uid));
        if (member != null) {
            // edit by yangch 时间： 2019.02.25 原因：添加渠道来源
            // String accessToken = token + member.getId() + System.currentTimeMillis() ;
            //            String accessToken = new StringBuilder()
            //                    .append(token).append(member.getId())
            //                    .append(System.currentTimeMillis())
            //                    .append("$$").append(thirdMark).toString() ;

            // add by tansitao 时间： 2018/12/27 原因：增加第三方平台接入，信息的转换
            MemberSourceType memberSourceType = null;
            try {
                memberSourceType = MemberSourceType.valueOf(thirdMark.toUpperCase());
            } catch (Exception e) {
                log.error("==========没有该数据的会员来源类型Type:" + thirdMark + "==========", e);
            }
            String thirdMarkOrdinal = "";
            if (memberSourceType != null) {
                thirdMarkOrdinal = String.valueOf(memberSourceType.getOrdinal());
            }
            try {
                memberEvent.onLoginSuccess(member, request, type, token, thirdMarkOrdinal); // 异步调用登录成功
            } catch (Exception e) {
                return error(msService.getMessage("SIGN_FAILED3"));
            }

            // 设置登录成功后的返回信息
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setLocation(member.getLocation());
            loginInfo.setMemberLevel(member.getMemberLevel());
            loginInfo.setUsername(member.getUsername());
            loginInfo.setAccessToken(token);
            loginInfo.setToken(request.getSession().getId());
            loginInfo.setRealName(member.getRealName());
            loginInfo.setCountry(member.getCountry());
            loginInfo.setAvatar(member.getAvatar());
            loginInfo.setPromotionCode(member.getPromotionCode());
            loginInfo.setId(member.getId());
            loginInfo.setPhone(member.getMobilePhone());
            result.setData(loginInfo);
        } else {
            return error(msService.getMessage("THIRD_LOGIN_FAIL"));
        }

        return result;
    }

    /**
     * 第三方用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public Member thirdRegister(ThirdLogin thirdLogin, HttpServletRequest request) throws Exception {
        // 如果手机号不为空进行手机号验证
        Member member = new Member();
        String thirdMark = request.getHeader("access-third-mark");
        Country country = countryService.findOne(thirdLogin.getCountry());
        Assert.isTrue(country != null, msService.getMessage("COUNTRY_NOT_EXIST"));
        if (thirdLogin.getCountry().equals("中国")) {
            Assert.isTrue(
                    ValidateUtil.isMobilePhone(thirdLogin.getPhone().trim()),
                    msService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
        }
        if (thirdLogin.getMemberLevel() == MemberLevelEnum.REALNAME) {
            // 会员身份证号码
            List<Member> members = memberService.findAllByIdNumber(thirdLogin.getIdNumber());
            Assert.isTrue(
                    members == null || members.size() == 0, msService.getMessage("HAS_BEEN_REGISTERED"));
            Assert.isTrue(
                    !StringUtils.isEmpty(thirdLogin.getRealName()),
                    msService.getMessage("REAL_NAME_NOT_EMPTY"));
            Assert.isTrue(
                    !StringUtils.isEmpty(thirdLogin.getIdNumber()),
                    msService.getMessage("REAL_NAME_USERID_NOT_EMPTY"));
            member.setRealName(thirdLogin.getRealName());
            member.setIdNumber(thirdLogin.getIdNumber());
            member.setRealNameStatus(RealNameStatus.VERIFIED);
        }

        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        // 盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex().toLowerCase();
        // 生成密码规则
        String password =
                new SimpleHash(
                        "md5",
                        Md5.md5Digest(thirdConfig.getPassword() + "hello, moto").toLowerCase(),
                        credentialsSalt,
                        2)
                        .toHex()
                        .toLowerCase();

        member.setMobilePhone(thirdLogin.getPhone());
        member.setMemberLevel(thirdLogin.getMemberLevel());
        member.setUsername(thirdLogin.getPhone());
        Location location = new Location();
        location.setCountry(thirdLogin.getCountry());
        member.setCountry(country);
        member.setLocation(location);
        member.setPassword(password);
        member.setSalt(credentialsSalt);
        member.setIp(IpUtils.getIp(request));
        if (thirdLogin.getMemberLevel() == MemberLevelEnum.REALNAME) {
            StringUtils.isEmpty(thirdLogin.getRealName());
            StringUtils.isEmpty(thirdLogin.getIdNumber());
        }
        Member member1 = memberService.save(member);
        if (thirdLogin.getMemberLevel() == MemberLevelEnum.REALNAME) {
            MemberApplication memberApplication = new MemberApplication();
            memberApplication.setIdCard(member.getIdNumber());
            memberApplication.setRealName(thirdLogin.getRealName());
            memberApplication.setOpType(3);
            memberApplication.setAuditStatus(AuditStatus.AUDIT_SUCCESS);
            memberApplication.setMember(member1);
            memberApplication.setCountry(member1.getCountry().getZhName());
            memberApplication.setIdentityCardImgReverse(thirdMark);
            memberApplication.setIdentityCardImgInHand(thirdMark);
            memberApplication.setIdentityCardImgFront(thirdMark);
            memberApplicationService.save(memberApplication);
        }
        //        memberEvent.onRegisterSuccess(member1, null);
        // add by tansitao 时间： 2019/1/2 原因：获取登录的会员来源信息
        //        String thirdMark = "";
        // add by tansitao 时间： 2018/12/27 原因：增加第三方接入的信息转换
        String memberSourceTypeStr = "";
        try {
            memberSourceTypeStr =
                    String.valueOf(MemberSourceType.valueOf(thirdMark.toUpperCase()).getOrdinal());
        } catch (Exception e) {
            log.error("==========没有该数据的会员来源类型Type:" + thirdMark + "==========", e);
        }

        memberEvent.onRegisterSuccess(member1, null, null, request, memberSourceTypeStr);
        return member1;
    }

    @PostMapping(value = "/inner/service/register")
    @SneakyThrows
    public Long internalRegister(@RequestBody ThirdLogin thirdLogin, HttpServletRequest request) {
        Member member = this.getService().thirdRegister(thirdLogin, request);
        return member.getId();
    }

    public LoginController getService() {
        return SpringContextUtil.getBean(LoginController.class);
    }

    /**
     *  * 权限校验接口  * @author tansitao  * @time 2018/10/10 11:42   
     */
    private boolean checkSign(String thirdMark, String signMsg) throws Exception {
        // String serkey = uid; //加密密钥（为用户ID）
        // 准入授权码
        String authMsg = thirdConfig.getAccessAuthKey(thirdMark);
        int index = authMsg.indexOf(":");
        String serkey = authMsg.substring(0, index); // 秘钥
        String authKey = authMsg.substring(index + 1); // 授权码
        // 获取 uid对应的 准入授权码
        // 接口签名校验
        int flag = SignUtil.checkSign(authKey, serkey, signMsg);
        if (flag == 0) {
            return true;
        } else {
            if (flag == 1) {
                log.info(
                        "=======================" + thirdMark + "," + msService.getMessage("SIGN_FAILED1"));
            } else if (flag == 2) {
                log.info(
                        "=======================" + thirdMark + "," + msService.getMessage("SIGN_FAILED2"));
            } else if (flag == 3) {
                log.info(
                        "=======================" + thirdMark + "," + msService.getMessage("SIGN_FAILED3"));
            }
            return false;
        }
    }

    /**
     * APP启动页面图片
     *
     * @param type
     * @author Zhang Yanjun
     * @time 2018.12.12 13:44
     */
    @ApiOperation(value = "APP启动页面图片")
    @ApiImplicitParam(value = "类型 0安卓 1苹果", name = "type", required = true)
    @RequestMapping(value = "/appStartImg", method = RequestMethod.POST)
    public MessageRespResult<List<AppStartImgVo>> appStartImg(int type) {
        List<StartupImgConfig> list = startupImgConfigService.findAll();
        List<AppStartImgVo> listVo = new ArrayList<>();
        for (StartupImgConfig startupImgConfig : list) {
            AppStartImgVo appStartImgVo = new AppStartImgVo();
            appStartImgVo.setName(startupImgConfig.getImageName());
            appStartImgVo.setIsShow(startupImgConfig.getIsShow());
            appStartImgVo.setIsFirst(startupImgConfig.getIsFirst());
            appStartImgVo.setShowTimeStart(startupImgConfig.getImageShowRangeStart());
            appStartImgVo.setShowTimeEnd(startupImgConfig.getImageShowRangeEnd());
            appStartImgVo.setUpdateTime(startupImgConfig.getUpdateTime());
            appStartImgVo.setUrl(startupImgConfig.getToUrl());
            appStartImgVo.setDuration(startupImgConfig.getDuration());
            // 安卓
            if (CommonUtils.equals(type, 0)) {
                appStartImgVo.setImgUrl(startupImgConfig.getAndroidImageUrl());
            } else {
                // 苹果
                appStartImgVo.setImgUrl(startupImgConfig.getIphoneImageUrl());
            }
            listVo.add(appStartImgVo);
        }
        return MessageRespResult.success("查询成功", listVo);
    }

    @ApiOperation(value = "验证登录密码")
    @RequestMapping(value = "/validateLoginPassword", method = {RequestMethod.POST,RequestMethod.GET})
    public MessageRespResult validateLoginPassword(@SessionAttribute(SESSION_MEMBER) AuthMember member,
                                                   String password){
        ValueOperations operations = redisTemplate.opsForValue();
        String key=String.format("entity:member:validatePassword:%s",member.getId());
        Assert.hasText(password, msService.getMessage("MISSING_PASSWORD"));
        int i = CommonUtils.toInt(operations.get(key), 0);
        if (i>5){
            return  MessageRespResult.error(msService.getMessage("ERROR_TIMES_THAN_FIVE"));
        }

        Member member2 = memberService.findOne(member.getId());
        String userPassWord = new SimpleHash("md5", password, member2.getSalt(), 2).toHex().toLowerCase();
        boolean equals = userPassWord.equals(member2.getPassword());
        if(!equals){
            operations.increment(key,1);
            redisTemplate.expire(key,1,TimeUnit.HOURS);
            return MessageRespResult.error(msService.getMessage("PASSWORD_ERROR"));
        }
        return MessageRespResult.success();
    }
}
