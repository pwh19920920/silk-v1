package com.spark.bitrade.controller.pay;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.LoginType;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.event.MemberEvent;
import com.spark.bitrade.ext.HttpJwtToken;
import com.spark.bitrade.ext.MemberClaim;
import com.spark.bitrade.service.*;
import com.spark.bitrade.service.impl.PayWalletMemberBindServiceImpl;
import com.spark.bitrade.system.GeetestLib;
import com.spark.bitrade.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * 用户信息操作控制器
 * @author tansitao
 * @time 2019.01.09 15:29
 */
@Api(description = "用户信息操作控制器")
@RestController
@RequestMapping("/member")
@Slf4j
public class MemberController extends BaseController {

    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private PayWalletMemberBindServiceImpl payWalletMemberBindService;
    @Autowired
    private GeetestLib gtSdk;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private IRegisterEvent iRegisterEvent;
    @Autowired
    private MemberEvent memberEvent;
    @Autowired
    private IdWorkByTwitter idWorkByTwitter;
    @Autowired
    private MemberSecuritySetService memberSecuritySetService;

    @Value("#{'${login.no.vertify.id}'.split(',')}")
    private List<String> noLoginVertifyIds;

    /**
      * 将线上钱包和交易平台账号进行绑定
      * @author tansitao
      * @time 2019/1/10 15:13 
      */
    @ApiOperation(value = "将线上钱包和交易平台账号进行绑定")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "钱包标识ID",name = "walletMarkId"),
            @ApiImplicitParam(value = "用户名",name = "username"),
            @ApiImplicitParam(value = "密码",name = "password")
    })
    @RequestMapping("/bindWallet")
    public MessageResult bindWallet(String walletMarkId, String username, String password, HttpServletRequest request) throws Exception{
        Assert.isTrue(!StringUtils.isEmpty(walletMarkId), msService.getMessage("WALLETMARK_IS_BULL"));
        Assert.isTrue(!StringUtils.isEmpty(username), msService.getMessage("MISSING_USERNAME"));
        Assert.isTrue(!StringUtils.isEmpty(password), msService.getMessage("MISSING_PASSWORD"));
        //modify by qhliao 删除之前的极验证 改用新的验证方式
//        String challenge = request.getParameter(GeetestLib.fn_geetest_challenge);
//        String validate = request.getParameter(GeetestLib.fn_geetest_validate);
//        String seccode = request.getParameter(GeetestLib.fn_geetest_seccode);
        Member member;
        try {
            //不进行及验证
            member = payWalletMemberBindService.check(username,password);
        } catch (Exception e) {
            log.error("=====绑定钱包用户到交易所,账户验证异常username：{}, walletMarkId:{}=====", username, walletMarkId, e);
            return error(msService.getMessage(e.getMessage()));
        }
//        //判断是否进行及验证
//        if (challenge == null && validate == null && seccode == null){
//
//        } else {
//            //及验证
//            int gtServerStatusCode = 1;
//            if(request.getSession()!=null && request.getSession().getAttribute(gtSdk.gtServerStatusSessionKey)!=null) {
//                gtServerStatusCode = (Integer) request.getSession().getAttribute(gtSdk.gtServerStatusSessionKey); //从session中获取gt-server状态
//            }
//            String userid = (String) request.getSession().getAttribute("userid"); //从session中获取userid
//            //自定义参数,可选择添加
//            String ip = getRemoteIp(request);
//            HashMap<String, String> param = new HashMap<String, String>();
//            param.put("user_id", userid);  //网站用户id
//            param.put("client_type", "web"); //web:电脑上的浏览器；h5:手机上的浏览器，包括移动应用内完全内置的web_view；native：通过原生SDK植入APP应用的方式
//            param.put("ip_address", ip.indexOf(",") != -1 ? ip.substring(0, ip.indexOf(",")) : ip); //传输用户请求验证时所携带的IP。备注：多IP请求会报错
//
//            int gtResult = 0;
//            if (gtServerStatusCode == 1) {
//                //gt-server正常，向gt-server进行二次验证
//                gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, param);
//            } else {
//                // gt-server非正常情况下，进行failback模式验证
//                gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
//            }
//
//            //判断是否验证成功
//            if (gtResult == 1) {
//                // 验证成功
//                try {
//                    member = payWalletMemberBindService.check(username,password);
//                } catch (Exception e) {
//                    return error(msService.getMessage(e.getMessage()));
//                }
//            } else {
//                // 验证失败
//                return error(msService.getMessage("GEETEST_FAIL"));
//            }
//        }

        String thirdMark = request.getHeader("thirdMark");

        //判断该钱包标识id是否已绑定其他会员
        this.loginOut(request, walletMarkId, member.getId(), thirdMark);

        PayWalletMemberBind payWalletMemberBind = payWalletMemberBindService.findByMemberIdAndAppId(member.getId(),thirdMark);

        //生成钱包的token
        String token = Md5.md5Digest(username+System.currentTimeMillis()) + System.currentTimeMillis() + member.getId();
        //处理渠道来源
        token = payWalletMemberBindService.getToken(token,thirdMark);
//        MemberClaim claim = MemberClaim.builder().userId(member.getId()).username(member.getUsername()).audience(thirdMark).build();
//        String token = HttpJwtToken.getInstance().createTokenWithClaim(claim);

        if(payWalletMemberBind == null){
            //没有绑定过的就新增
            payWalletMemberBind = new PayWalletMemberBind();
            payWalletMemberBind.setWalletMarkId(walletMarkId);
            payWalletMemberBind.setMemberId(member.getId());
            payWalletMemberBind.setToken(token);
            payWalletMemberBind.setAppId(thirdMark);
            payWalletMemberBindService.save(payWalletMemberBind);
        }else {
            //绑定过的就进行更新
            payWalletMemberBind.setWalletMarkId(walletMarkId);
            payWalletMemberBind.setToken(token);
            payWalletMemberBind.setUpdateTime(new Date());
            payWalletMemberBind.setUsable(BooleanEnum.IS_TRUE);
            payWalletMemberBindService.update(payWalletMemberBind);
        }

        MemberSecuritySet memberSecuritySet = null;

        AuthMember authMember = AuthMember.toAuthMember(member);
        //edit by fumy. date:2018.10.26 reason:登录验证可二选一
        if( !noLoginVertifyIds.contains(thirdMark) ){
            //需要安全验证的才查询安全权限设置
            memberSecuritySet = memberSecuritySetService.findOneBymemberId(member.getId());
            //edit by fumy. date:2018.10.26 reason:登录验证可二选一
            if(memberSecuritySet != null) {
                if(memberSecuritySet.getIsOpenPhoneLogin() == BooleanEnum.IS_TRUE){//手机登录
                    authMember.addLoginVerifyRequire("isOpenPhoneLogin", false);
                }
                if(memberSecuritySet.getIsOpenGoogleLogin() == BooleanEnum.IS_TRUE){//谷歌登录
                    authMember.addLoginVerifyRequire("isOpenGoogleLogin", false);
                }
            }
        }


        //add by yangch 时间： 2019.02.25 原因：授权实体中添加渠道来源

        if(!StringUtils.isEmpty(thirdMark)){
            authMember.setPlatform(thirdMark);
        }
        MemberClaim claim =
                MemberClaim.builder()
                        .userId(member.getId())
                        .username(member.getUsername())
                        .audience(thirdMark)
                        .build();
        String accessToken = HttpJwtToken.getInstance().createTokenWithClaim(claim);
        memberEvent.onLoginSuccess(
                member, request, LoginType.API, DigestUtils.md5Hex(accessToken), thirdMark); // 异步调用登录成功

        request.getSession().setAttribute(SysConstant.SESSION_MEMBER, authMember); //保存session

        //设置登录成功后的返回信息
        LoginInfo loginInfo = getLoginInfo(member);
        loginInfo.setToken(payWalletMemberBind.getToken());

        loginInfo.setAccessToken(accessToken);
        //没有设置安全校验的默认为不校验
        if(org.springframework.util.StringUtils.isEmpty(memberSecuritySet)){
            loginInfo.setIsOpenPhoneLogin(BooleanEnum.IS_FALSE );
            loginInfo.setIsOpenGoogleLogin( BooleanEnum.IS_FALSE);
            loginInfo.setIsOpenPhoneUpCoin(BooleanEnum.IS_FALSE );
            loginInfo.setIsOpenGoogleUpCoin(BooleanEnum.IS_FALSE);
        } else {
            loginInfo.setIsOpenPhoneLogin(memberSecuritySet.getIsOpenPhoneLogin());
            loginInfo.setIsOpenGoogleLogin(memberSecuritySet.getIsOpenGoogleLogin());
            loginInfo.setIsOpenPhoneUpCoin(memberSecuritySet.getIsOpenPhoneUpCoin());
            loginInfo.setIsOpenGoogleUpCoin(memberSecuritySet.getIsOpenGoogleUpCoin());
        }


        //设置用户登录信息为登录状态
        if (thirdMark!=null){
//            redisService.remove(SysConstant.MEMBER_LOGOUT + member.getId() +":"+ thirdMark);
            redisService.expireSet(SysConstant.MEMBER_LOGOUT + member.getId()  +":"+ thirdMark, "0", 1800000);
        }else {
//            redisService.remove(SysConstant.MEMBER_LOGOUT + member.getId());
            redisService.expireSet(SysConstant.MEMBER_LOGOUT + member.getId() , "0", 1800000);

        }
        return success(loginInfo);
    }

    /**
      * 邮件的方式注册并绑定
      * @author tansitao
      * @time 2019/1/10 16:41 
      */
    @RequestMapping("/register/email")
    @ResponseBody
    public MessageResult registerByEmailNoActive(@Valid LoginByEmail loginByEmail, BindingResult bindingResult, HttpServletRequest request)  throws Exception {
        //验证注册信息
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        notNull(loginByEmail.getCode(), msService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        Assert.isTrue(!StringUtils.isEmpty(loginByEmail.getWalletMarkId()), msService.getMessage("WALLETMARK_IS_BULL"));

        //验证码不能为空
        String email = loginByEmail.getEmail();
        String key = SysConstant.EMAIL_REG_CODE_PREFIX + email;
        Object code = redisService.get(key);
        notNull(code, msService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        //edit by lingxing 时间： 2018/7/16 原因：改用mybatis查询
        isTrue(!memberService.emailAndUsernameIsExist(loginByEmail.getUsername(),null,email), msService.getMessage("EMAIL_ALREADY_BOUND"));
        if (!code.toString().equals(loginByEmail.getCode())) {
            return error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            redisService.remove(key);
            redisService.remove(key + "Time");
        }

        //判断邀请码是否正确
        if (StringUtils.hasText(loginByEmail.getPromotion())){
            if (!memberService.checkPromotion(loginByEmail.getPromotion())){
                return error(msService.getMessage("PROMOTION_CODE_ERRO"));
            }
        }

        //add by zyj 2018.12.27 : 接入风控
        Member m = new Member();
        m.setEmail(loginByEmail.getEmail());
        MessageResult result1 = iRegisterEvent.register(request,null,m,loginByEmail.getPromotion());
        if (result1.getCode() != 0 ){
            return error(result1.getMessage());
        }

        //add by tansitao 时间： 2019/1/2 原因：获取登录的会员来源信息
        String thirdMark = request.getHeader("thirdMark");

        //判断该钱包是否绑定了其他用户
        this.loginOut(request,loginByEmail.getWalletMarkId(),null,thirdMark);

        //不可重复随机数
        String loginNo = String.valueOf(idWorkByTwitter.nextId());

        //注册邮箱用户,并绑定钱包信息
        Member member1 = payWalletMemberBindService.registerByEmail(loginNo, loginByEmail.getEmail(),
                loginByEmail.getPassword(), loginByEmail.getWalletMarkId(), request);

        //edit by tansitao 时间： 2018/12/27 原因：增加设备信息，和会员来源
        memberEvent.onRegisterSuccess(member1, loginByEmail.getPromotion(), loginByEmail.getLoginType(), request, thirdMark);

        //设置登录成功后的返回信息
        LoginInfo loginInfo = getLoginInfo(member1);
        return success(loginInfo);
    }



    /**
      * 通过手机注册，并绑定钱包
      * @author tansitao
      * @time 2019/1/11 9:53 
      */
    @RequestMapping(value = "/register/phone",method = {RequestMethod.POST,RequestMethod.OPTIONS})
    @ResponseBody
    public MessageResult loginByPhone(@Valid LoginByPhone loginByPhone, BindingResult bindingResult, HttpServletRequest request) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        if (SysConstant.CHINA.equals(loginByPhone.getCountry())) {
            Assert.isTrue(ValidateUtil.isMobilePhone(loginByPhone.getPhone().trim()), msService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
        }
        notNull(loginByPhone.getCode(), msService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        Assert.isTrue(!StringUtils.isEmpty(loginByPhone.getWalletMarkId()), msService.getMessage("WALLETMARK_IS_BULL"));

        //edit by tansitao 时间： 2018/5/16 原因：修改用户手机注册验证逻辑
        String phone = loginByPhone.getPhone();
        Object code = redisService.get(SysConstant.PHONE_REG_CODE_PREFIX + phone);
        notNull(code, msService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        //edit by lingxing 时间： 2018/7/16 原因：改用mybatis查询
        isTrue(!memberService.phoneAndUsernameIsExist(loginByPhone.getUsername(),phone,null), msService.getMessage("USERNAME_ALREADY_EXISTS"));
        if (!code.toString().equals(loginByPhone.getCode())) {
            return error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            redisService.remove(SysConstant.PHONE_REG_CODE_PREFIX + phone);
        }

        //判断邀请码是否正确
        if (StringUtils.hasText(loginByPhone.getPromotion())){
            if (!memberService.checkPromotion(loginByPhone.getPromotion())){
                return error(msService.getMessage("PROMOTION_CODE_ERRO"));
            }
        }

        //add by zyj 2018.12.27 : 接入风控
        Member m = new Member();
        m.setMobilePhone(loginByPhone.getPhone());
        MessageResult res = iRegisterEvent.register(request, null, m, loginByPhone.getPromotion());
        if (res.getCode() != 0){
            return error(res.getMessage());
        }

        //add by tansitao 时间： 2019/1/2 原因：获取登录的会员来源信息
        String thirdMark = request.getHeader("thirdMark");

        //判断该钱包是否绑定了其他用户
        this.loginOut(request,loginByPhone.getWalletMarkId(),null,thirdMark);

        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        Member member1 = payWalletMemberBindService.registerByPhone(loginNo, loginByPhone.getPhone(), loginByPhone.getPassword(),
                loginByPhone.getWalletMarkId(), loginByPhone.getCountry(), request);

        //edit by tansitao 时间： 2018/12/27 原因：增加设备信息，和会员来源
        memberEvent.onRegisterSuccess(member1, loginByPhone.getPromotion(), loginByPhone.getLoginType(), request, thirdMark);

        //设置登录成功后的返回信息
        LoginInfo loginInfo = getLoginInfo(member1);
        return success(loginInfo);
    }

    /**
      * 解绑钱包
      * @author tansitao
      * @time 2019/1/14 9:39 
      */
    @RequestMapping(value = "/unbind",method = {RequestMethod.POST})
    public MessageResult unbind(@SessionAttribute(SESSION_MEMBER) AuthMember member, String walletMarkId){
        Assert.isTrue(!StringUtils.isEmpty(walletMarkId), msService.getMessage("WALLETMARK_IS_BULL"));
//        PayWalletMemberBind payWalletMemberBind = new PayWalletMemberBind();
//        payWalletMemberBind.setWalletMarkId(walletMarkId);
//        payWalletMemberBind.setMemberId(member.getId());

        PayWalletMemberBind payWalletMemberBind = payWalletMemberBindService.findByMemberIdAndAppIdAndWalletMarkId(member.getId(),
                member.getPlatform(),walletMarkId);
        if(payWalletMemberBind != null){
            //生成钱包的token
            payWalletMemberBind.setUsable(BooleanEnum.IS_FALSE);
            payWalletMemberBindService.update(payWalletMemberBind);
        }else {
            return error(msService.getMessage("UNBIND_FAIL"));
        }
        return success("success");
    }

    /**
      * 获取登录信息
      * @author tansitao
      * @time 2019/1/11 10:33 
      */
    private LoginInfo getLoginInfo(Member member){
        //设置登录成功后的返回信息
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setLocation(member.getLocation());
        loginInfo.setMemberLevel(member.getMemberLevel());
        loginInfo.setUsername(member.getUsername());
        loginInfo.setToken(member.getToken());
        loginInfo.setAccessToken(member.getToken());
        loginInfo.setRealName(member.getRealName());
        loginInfo.setCountry(member.getCountry());
        loginInfo.setAvatar(member.getAvatar());
        loginInfo.setPromotionCode(member.getPromotionCode());
        loginInfo.setId(member.getId());
        loginInfo.setPhone(member.getMobilePhone());
        //add by  shenzucai 时间： 2019.04.23  原因：添加邮箱
        loginInfo.setEmail(member.getEmail());
        return loginInfo;
    }

    /**
     * 判断该钱包标识id是否已绑定其他会员 如果有则登出原有账户
     */
    @Async
    void loginOut(HttpServletRequest request,String walletMarkId,Long nowMemberId, String appId){
        List<PayWalletMemberBind> walletMarkIdList = payWalletMemberBindService.findMembers(walletMarkId,BooleanEnum.IS_TRUE,appId);
        if (walletMarkIdList.size()!=0){
            for (PayWalletMemberBind bind : walletMarkIdList){
                if (!bind.getMemberId().equals(nowMemberId)){
                    log.info("登出原绑定的用户-{}===========",bind.getMemberId());
                    bind.setUsable(BooleanEnum.IS_FALSE);
                    bind.setUpdateTime(new Date());
                    bind.setToken(null);
                    bind.setWalletMarkId(null);
                    payWalletMemberBindService.update(bind);
                    //登出
                    String thirdMark = request.getHeader("thirdMark");
                    if (thirdMark!=null){
                        redisService.expireSet(SysConstant.MEMBER_LOGOUT + bind.getMemberId() +":"+ thirdMark , "1", 1800000);
                    }else {
                        redisService.expireSet(SysConstant.MEMBER_LOGOUT + bind.getMemberId() , "1", 1800000);
                    }
                    log.info("登出原绑定的用户-{}成功===========",bind.getMemberId());
                }
            }
        }

    }
}
