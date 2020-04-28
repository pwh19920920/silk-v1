package com.spark.bitrade.controller;


import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.config.GlobalConfig;
import com.spark.bitrade.config.MultiMailConfiguration;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.MemberPaymentAccountDao;
import com.spark.bitrade.dto.RealNameDetailDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import com.spark.bitrade.vo.MemberSecurityInfoVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpStatus;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.spark.bitrade.constant.BooleanEnum.IS_FALSE;
import static com.spark.bitrade.constant.BooleanEnum.IS_TRUE;
import static com.spark.bitrade.constant.CertifiedBusinessStatus.*;
import static com.spark.bitrade.constant.SysConstant.*;
import static com.spark.bitrade.util.MessageResult.error;
import static com.spark.bitrade.util.MessageResult.success;
import static org.springframework.util.Assert.*;


/**
 * 用户中心认证
 *
 * @author Zhang Jinwei
 * @date 2018年01月09日
 */
@Api(description = "用户中心认证控制类")
@RestController
@RequestMapping("/approve")
@Slf4j
public class ApproveController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private PartnerAreaService partnerAreaService;
    @Autowired
    private BusinessAuthDepositService businessAuthDepositService;
    @Autowired
    private BusinessAuthApplyService businessAuthApplyService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private UnlockCoinApplyService unlockCoinApplyService;
    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private MemberSecuritySetService memberSecuritySetService;
    @Autowired
    MultiMailConfiguration multiMailConfiguration;
    @Autowired
    private CountryService countryService;
    @Autowired
    private SMSProviderProxy smsProvider;
    @Autowired
    private MemberPaymentAccountService memberPaymentAccountService;
    @Resource
    private LocaleMessageSourceService localeMessageSourceService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private MemberSecuritySetService securitySetService;
    @Autowired
    private FincMemberAccountService fincMemberAccountService;
    @Autowired
    private GyDmcodeService gyDmcodeService;
    @Autowired
    private AliyunConfig aliyunConfig;
    //add by tansitao 时间： 2018/5/20 原因：添加亚马逊s3
//    @Autowired
//    private AwsConfig awsConfig;

    @Autowired
    private GlobalConfig globalConfig;  //add by tansitao 时间： 2018/5/11 原因：添加全局配置参数

    // add by archx 时间： 2019/4/4 原因：认证资料冻结验证
    @Autowired
    private MemberFrozenVoucherService memberFrozenVoucherService;

    @Autowired
    private OtcOrderService otcOrderService;
//    @Value("${phone.check.api:http://fephone.market.alicloudapi.com/phoneCheck}")
//    private String phoneCheckApi;
//    @Value("${phone.check.app.code:597a465014ec4ec4aa4beb7e76e52f5e}")
//    private String phoneCheckAppCode;

    /**
     * 181106原实名认证接口切换至另一商家.
     */
    @Value("${phone.check.api:http://mobile3elements.shumaidata.com/mobile/verify_real_name}")
    private String phoneCheckApi;
    @Value("${phone.check.app.code:a9734ebaf4a74a9a830ca5fe67fd6656}")
    private String phoneCheckAppCode;

    /**
     * 设置或更改用户头像
     *
     * @param user
     * @param url
     * @return
     */
    @RequestMapping("/change/avatar")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult update(@SessionAttribute(SESSION_MEMBER) AuthMember user, String url) {
        Member member = memberService.findOne(user.getId());
        member.setAvatar(url);
        memberService.save(member);
        return MessageResult.success();
    }

    //add by tansitao 时间： 2018/4/24 原因：添加修改用户名接口
    @RequestMapping("/change/username")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateUserName(@SessionAttribute(SESSION_MEMBER) AuthMember user, String newUserName) {
        isTrue(!memberService.usernameIsExist(newUserName), msService.getMessage("USERNAME_HAS_EXITED"));
        Member member = memberService.findOne(user.getId());
        //add by tansitao 时间： 2018/7/10 原因：已修改的昵称不允许修改
        isTrue(member.getUsername().equals(member.getMobilePhone()) || member.getUsername().equals(member.getEmail()), msService.getMessage("USERNAME_UPDATE_USED"));
        member.setUsername(newUserName);
        memberService.save(member);
        return MessageResult.success();
    }

    /**
     * 开启安全设置
     *
     * @param user
     * @return
     * @creater tansitao
     * @editer fumy 修改验证逻辑,开启和关闭可同时控制登录与提币开关
     */
    @ApiOperation(value = "开启安全设置", tags = "安全设置", notes = "开启手机登录、手机提币、谷歌登录、谷歌提币验证")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "开启（关闭）手机验证,0:关，1：开", name = "isOpenPhone", dataType = "int"),
            @ApiImplicitParam(value = "开启（关闭）谷歌验证,0:关，1：开", name = "isOpenGoogle", dataType = "int"),
            @ApiImplicitParam(value = "手机号，开启（关闭)谷歌验证时为不传", name = "phone", dataType = "String"),
            @ApiImplicitParam(value = "验证码，开启(关闭)手机、谷歌时必传", name = "code", dataType = "String")
    })
    @RequestMapping(value = "/security/open", method = {RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageResult saveSecuritySetting(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, BooleanEnum isOpenPhone, BooleanEnum isOpenGoogle, String phone, String code) {
        //进行参数校验，谷歌和手机验证不能同时关闭
        if (isOpenPhone != null && isOpenGoogle != null && isOpenPhone == BooleanEnum.IS_FALSE && isOpenGoogle == BooleanEnum.IS_FALSE) {
            return error("不能同时关闭谷歌和手机验证！");
        }

        MemberSecuritySet memberSecuritySet = memberSecuritySetService.findOneBymemberId(user.getId());
        if (memberSecuritySet == null) {
            memberSecuritySet = new MemberSecuritySet();
            memberSecuritySet.setMemberId(user.getId());
        }
        boolean isCheck;
        if (isOpenPhone != null) {
//            if(!isOpenPhone.isIs()){//关闭手机验证，需要验证手机验证码
            isCheck = smsService.checkCode(phone, code);
            isTrue(isCheck, localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
//            }

            //开启（关闭）手机登录、手机提币验证
            memberSecuritySet.setIsOpenPhoneLogin(isOpenPhone);
            memberSecuritySet.setIsOpenPhoneUpCoin(isOpenPhone);
        }
        if (isOpenGoogle != null) {
//            if(!isOpenGoogle.isIs()){//关闭谷歌验证，需要验证谷歌验证码
            isCheck = smsService.checkGoogleCode(user.getId(), code);
            isTrue(isCheck, localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
//            }

            //开启（关闭）谷歌登录、谷歌提币验证
            memberSecuritySet.setIsOpenGoogleLogin(isOpenGoogle);
            memberSecuritySet.setIsOpenGoogleUpCoin(isOpenGoogle);
        }
        memberSecuritySetService.save(memberSecuritySet);
        return MessageResult.success();
    }

    /**
     * 安全设置
     *
     * @param user
     * @return
     */
    @RequestMapping("/security/setting")
    public MessageResult securitySetting(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        PartnerArea partnerArea = partnerAreaService.findByMemberAndStatus(member);
        MemberSecuritySet memberSecuritySet = memberSecuritySetService.findOneBymemberId(member.getId());
        MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(user.getId());
        boolean isDeakingMember = memberService.isDeakingMember(member.getId());
        String idNumber = member.getIdNumber();
        //add by tansitao 时间： 2018/5/11 原因：获取全局配置
        Global global = new Global();
        global.setIdCardSwitch(globalConfig.getIdCardSwitch());
        MemberSecurity memberSecurity = MemberSecurity.builder().username(member.getUsername())
                .createTime(member.getRegistrationTime())
                .id(member.getId())
                .emailVerified(StringUtils.isEmpty(member.getEmail()) ? IS_FALSE : IS_TRUE)
                .email(member.getEmail())
                .country(member.getCountry())
                .mobilePhone(member.getMobilePhone())
                .fundsVerified(StringUtils.isEmpty(member.getJyPassword()) ? IS_FALSE : IS_TRUE)
                .loginVerified(IS_TRUE)
                .phoneVerified(StringUtils.isEmpty(member.getMobilePhone()) ? IS_FALSE : IS_TRUE)
                .realName(member.getRealName())
                .idCard(StringUtils.isEmpty(idNumber) ? null : idNumber.substring(0, 2) + "**********" + idNumber.substring(idNumber.length() - 2))
                .realVerified(member.getRealNameStatus() == RealNameStatus.VERIFIED ? IS_TRUE : IS_FALSE)
                .realAuditing(member.getRealNameStatus().equals(RealNameStatus.AUDITING) ? IS_TRUE : IS_FALSE)
                .certBusiness(member.getMemberLevel() == MemberLevelEnum.IDENTIFICATION ? IS_TRUE : IS_FALSE)
                .avatar(member.getAvatar())
                .googleKey(member.getGoogleKey())
                .googleState(member.getGoogleState())
                .businessVerified(member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.VERIFIED) ? IS_TRUE : IS_FALSE)
                .accountVerified((member.getBankInfo() == null && member.getAlipay() == null && member.getWechatPay() == null) ? IS_FALSE : IS_TRUE) //add by yangch 时间： 2018.05.03 原因：代码合并
                .global(global)//edit by tansitao 时间： 2018/5/11 原因：新增全局配置
                .partnerVerified(partnerArea == null ? IS_FALSE : IS_TRUE)//edit by tansitao 时间： 2018/5/11 原因：新增是否为合伙人
                .isDeakingMember(isDeakingMember == true ? IS_TRUE : IS_FALSE)
                .isOpenPhoneLogin(memberSecuritySet == null ? BooleanEnum.IS_FALSE : memberSecuritySet.getIsOpenPhoneLogin())//add by tansitao 时间： 2018/7/13 原因：增加手机验证、google验证
                .isOpenGoogleLogin(memberSecuritySet == null ? BooleanEnum.IS_FALSE : memberSecuritySet.getIsOpenGoogleLogin())
                .isOpenPhoneUpCoin(memberSecuritySet == null ? BooleanEnum.IS_FALSE : memberSecuritySet.getIsOpenPhoneUpCoin())
                .isOpenGoogleUpCoin(memberSecuritySet == null ? BooleanEnum.IS_FALSE : memberSecuritySet.getIsOpenGoogleUpCoin())
                .isOpenPropertyShow(memberSecuritySet == null ? BooleanEnum.IS_FALSE : memberSecuritySet.getIsOpenPropertyShow())
                .transactions(member.getTransactions())
                .isBindBank(member.getBankInfo() == null ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE) //add by tansitao 时间： 2018/11/9 原因：是否绑定银行卡
                .isBindAliPay(member.getAlipay() == null ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE) //add by tansitao 时间： 2018/11/9 原因：是否绑定支付宝
                .isBindWechatPay(member.getWechatPay() == null ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE)//add by tansitao 时间： 2018/11/9 原因：是否绑定微信
                .isBindEpay(memberPaymentAccount == null || StringUtils.isEmpty(memberPaymentAccount.getEpayNo()) ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE)//add by tansitao 时间： 2018/11/9 原因：是否绑定epay
                .build();
        if (memberSecurity.getRealAuditing().equals(IS_FALSE) && memberSecurity.getRealVerified().equals(IS_FALSE)) {
            MemberApplication memberApplication = memberApplicationService.findLatelyReject(member);
            memberSecurity.setRealNameRejectReason(memberApplication == null ? null : memberApplication.getRejectReason());
        }
        MessageResult result = MessageResult.success("success");
        result.setData(memberSecurity);
        return result;
    }

    /**
     * 商家认证材料检查
     *
     * @param user
     * @return
     */
//    @RequestMapping("/security/setting")
    @RequestMapping("/business/material")
    public MessageResult checkBusiness(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        String idNumber = member.getIdNumber();
        BusinessInfo businessInfo = BusinessInfo.builder().username(member.getUsername())
                .createTime(member.getRegistrationTime())
                .id(member.getId())
                .emailVerified(StringUtils.isEmpty(member.getEmail()) ? IS_FALSE : IS_TRUE)
                .email(member.getEmail())
                .mobilePhone(member.getMobilePhone())
                .fundsVerified(StringUtils.isEmpty(member.getJyPassword()) ? IS_FALSE : IS_TRUE)
                .loginVerified(IS_TRUE)
                .phoneVerified(StringUtils.isEmpty(member.getMobilePhone()) ? IS_FALSE : IS_TRUE)
                .realName(member.getRealName())
                .idCard(StringUtils.isEmpty(idNumber) ? null : idNumber.substring(0, 2) + "**********" + idNumber.substring(idNumber.length() - 2))
                .realVerified(StringUtils.isEmpty(member.getRealName()) ? IS_FALSE : IS_TRUE)
                .realAuditing(member.getRealNameStatus().equals(RealNameStatus.AUDITING) ? IS_TRUE : IS_FALSE)
                .avatar(member.getAvatar())
                .alipay(member.getAlipay())
                .aliVerified(member.getAlipay() == null ? IS_FALSE : IS_TRUE)
                .bankInfo(member.getBankInfo())
                .bankVerified(member.getBankInfo() == null ? IS_FALSE : IS_TRUE)
                .wechatPay(member.getWechatPay())
                .wechatVerified(member.getWechatPay() == null ? IS_FALSE : IS_TRUE)
                .marginVerified("0".equals(member.getMargin()) ? IS_FALSE : IS_TRUE)
                .build();

        MessageResult result = MessageResult.success("success");
        result.setData(businessInfo);
        return result;
    }

    /**
     * 设置资金密码
     *
     * @param jyPassword
     * @param user
     * @return
     */
    @RequestMapping("/transaction/password")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult approveTransaction(String jyPassword, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        hasText(jyPassword, msService.getMessage("MISSING_JY_PASSWORD"));
        //edit by yangch 2018-04-12 前端做了规则验证及md5的密文密传输，此处会错误误验证
        //isTrue(jyPassword.length() >= 6 && jyPassword.length() <= 20, msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
        Member member = memberService.findOne(user.getId());
        Assert.isNull(member.getJyPassword(), msService.getMessage("REPEAT_SETTING"));
        //生成密码
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        isTrue(!jyPass.equals(member.getPassword()), msService.getMessage("JYPASSWORD_LOGPASSWORD"));

//        String jyPass = Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase();
        member.setJyPassword(jyPass);
        memberService.save(member);
        return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
    }

    /**
     * 修改资金密码
     *
     * @param oldPassword
     * @param newPassword
     * @param user
     * @return
     */
    @RequestMapping("/update/transaction/password")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateTransaction(String oldPassword, String newPassword, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        hasText(oldPassword, msService.getMessage("MISSING_OLD_JY_PASSWORD"));
        hasText(newPassword, msService.getMessage("MISSING_NEW_JY_PASSWORD"));
        //edit by yangch 2018-04-12 前端做了规则验证及md5的密文密传输，此处会错误误验证
        //isTrue(newPassword.length() >= 6 && newPassword.length() <= 20, msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
        Member member = memberService.findOne(user.getId());
        String oldJyPass = new SimpleHash("md5", oldPassword, member.getSalt(), 2).toHex().toLowerCase();
        isTrue(oldJyPass.equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        String jyPass = new SimpleHash("md5", newPassword, member.getSalt(), 2).toHex().toLowerCase();
        isTrue(!jyPass.equals(member.getPassword()), msService.getMessage("JYPASSWORD_LOGPASSWORD"));
        member.setJyPassword(jyPass);
        memberService.save(member);
        return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
    }

    /**
     * 重置资金密码 目前设置、修改资金密码都是使用此方法
     *
     * @param newPassword
     * @param code
     * @param user
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "设置、修改资金密码", tags = "个人信息设置")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "新密码", name = "newPassword", dataType = "String"),
            @ApiImplicitParam(value = "验证码类型 0：手机 1：邮箱", name = "codeType", dataType = "int"),
            @ApiImplicitParam(value = "验证码", name = "code", dataType = "String")
    })
    @RequestMapping(value = "/reset/transaction/password", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public MessageResult resetTransaction(String newPassword, int codeType, String code, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        //edit by tansitao 时间： 2018/5/16 原因：修改重置资金密码验证逻辑，先判断用户资金密码是否与登录密码重复，并修改获取用户手机号的对象为查出来的手机号，不修改的话初次绑定手机号用户会有问题
        hasText(newPassword, msService.getMessage("MISSING_NEW_JY_PASSWORD"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Member member = memberService.findOne(user.getId());
        //手机验证类型
        if (codeType == 0) {
            Object cache = valueOperations.get(SysConstant.PHONE_RESET_TRANS_CODE_PREFIX + member.getMobilePhone());
            notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
            if (!code.equals(cache.toString())) {
                return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
            }
        }
        //邮箱验证类型
        if (codeType == 1) {
            Object cache = valueOperations.get(SysConstant.EMAIL_COMMON_CODE_PREFIX + member.getEmail());
            notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
            if (!code.equals(cache.toString())) {
                return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
            }
        }
        String jyPass = new SimpleHash("md5", newPassword, member.getSalt(), 2).toHex().toLowerCase();
        isTrue(!jyPass.equals(member.getPassword()), msService.getMessage("JYPASSWORD_LOGPASSWORD"));
        member.setJyPassword(jyPass);
        memberService.save(member);
        //modify by qhliao 保存密码之后在清空redis
        valueOperations.getOperations().delete(SysConstant.EMAIL_COMMON_CODE_PREFIX + member.getEmail());
        valueOperations.getOperations().delete(SysConstant.PHONE_RESET_TRANS_CODE_PREFIX + member.getMobilePhone());
        return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
    }

    /**
     * 绑定手机号
     *
     * @param password
     * @param phone
     * @param code
     * @param user
     * @return
     */
    @RequestMapping("/bind/phone")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindPhone(HttpServletRequest request, String countryName, String password, String phone, String code, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
        hasText(phone, msService.getMessage("MISSING_PHONE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        //add by tansitao 时间： 2018/5/12 原因：增加对国家名不为空的判断
        notNull(countryName, msService.getMessage("COUNTRY_NOT_NULL"));
        //add|edit|del by tansitao 时间： 2018/5/12 原因：修改判断对象为国家名参数
        if ("中国".equals(countryName)) {
            if (!ValidateUtil.isMobilePhone(phone.trim())) {
                return MessageResult.error(msService.getMessage("PHONE_FORMAT_ERROR"));
            }
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object cache = valueOperations.get(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        Member member1 = memberService.findByPhone(phone);
        isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
        if (!code.equals(cache.toString())) {
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }
        //del by tansitao 时间： 2018/11/11 原因：取消对redis中短信验证码的删除
//        else {
//            valueOperations.getOperations().delete(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
//        }
        ///add tansitao 时间： 2018/5/12 原因：增加对国家的绑定
        Member member = memberService.findOne(user.getId());
        // edit by wsy, date: 2019-1-25 16:39:27 reason: 绑定手机号时，只修改country
//        Location location = new Location();
//        location.setCountry(countryName);
//        member.setLocation(location);
        Country country = new Country();
        country.setZhName(countryName);
        member.setCountry(country);
        isTrue(member.getMobilePhone() == null, msService.getMessage("REPEAT_PHONE_REQUEST"));
        String userPassWord = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();
        if (member.getPassword().equals(userPassWord)) {
            member.setMobilePhone(phone);
            memberService.save(member);

            //add by fumy .date:2019.11.09 reason:如果用户未绑定谷歌验证，则默认开启手机登录、提币验证
            if (member.getGoogleState() == 0) {
                MemberSecuritySet securitySet = securitySetService.findOneBymemberId(member.getId());
                if (securitySet == null) {
                    securitySet = new MemberSecuritySet();
                }
                //默认开启手机登录、提币验证
                securitySet.setMemberId(member.getId());
                securitySet.setIsOpenPhoneLogin(BooleanEnum.IS_TRUE);
                securitySet.setIsOpenPhoneUpCoin(BooleanEnum.IS_TRUE);
                securitySetService.save(securitySet);
            }
            //add by tansitao 时间： 2018/11/11 原因：删除验证码
            valueOperations.getOperations().delete(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
            return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
        } else {
            request.removeAttribute(SysConstant.SESSION_MEMBER);
            return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
        }
    }


    /**
     * 更改登录密码
     *
     * @param request
     * @param newPassword
     * @param code
     * @param user
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "修改登录密码", tags = "个人信息设置")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "新密码", name = "newPassword", dataType = "String"),
            @ApiImplicitParam(value = "验证码类型 0：手机 1：邮箱", name = "codeType", dataType = "int"),
            @ApiImplicitParam(value = "验证码", name = "code", dataType = "String")
    })
    @RequestMapping(value = "/update/password", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateLoginPassword(HttpServletRequest request, String newPassword, int codeType, String code,
                                             @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {

        //add|edit|del by tansitao 时间： 2018/5/16 原因：修改重置登录验证逻辑，并修改获取用户手机号的对象为查出来的手机号，不修改的话初次绑定手机号用户会有问题
        hasText(newPassword, msService.getMessage("MISSING_NEW_PASSWORD"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));

        //edit by fumy date:2018.10.26 reason:新增邮箱验证
        Member member = memberService.findOne(user.getId());
        if (codeType == 0) {//手机验证类型
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object cache = valueOperations.get(SysConstant.PHONE_UPDATE_PASSWORD_PREFIX + member.getMobilePhone());
            notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
            if (!code.equals(cache.toString())) {
                return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.PHONE_UPDATE_PASSWORD_PREFIX + member.getMobilePhone());
            }
        }
        if (codeType == 1) {//邮箱验证类型
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object cache = valueOperations.get(SysConstant.EMAIL_COMMON_CODE_PREFIX + member.getEmail());
            notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
            if (!code.equals(cache.toString())) {
                return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.EMAIL_COMMON_CODE_PREFIX + member.getEmail());
            }
        }
        request.removeAttribute(SysConstant.SESSION_MEMBER);
        //用户新密码加密
        String userNewPassWord = new SimpleHash("md5", newPassword, member.getSalt(), 2).toHex().toLowerCase();
        member.setPassword(userNewPassWord);
        int i = memberService.updateMemberPassword(member.getId(), member.getPassword());
        isTrue(i > 0, msService.getMessage("UPDATE_FAILED"));
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 绑定邮箱
     *
     * @param request
     * @param password
     * @param code
     * @param email
     * @param user
     * @return
     */
    @RequestMapping("/bind/email")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindEmail(HttpServletRequest request, String password, String code, String email, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        hasText(email, msService.getMessage("MISSING_EMAIL"));
        isTrue(ValidateUtil.isEmail(email), msService.getMessage("EMAIL_FORMAT_ERROR"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object cache = valueOperations.get(EMAIL_BIND_CODE_PREFIX + email);
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        isTrue(code.equals(cache.toString()), msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        Member member = memberService.findOne(user.getId());
        isTrue(member.getEmail() == null, msService.getMessage("REPEAT_EMAIL_REQUEST"));
        String userPassWord = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();
        if (!userPassWord.equals(member.getPassword())) {
            request.removeAttribute(SysConstant.SESSION_MEMBER);
            return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
        } else {
            member.setEmail(email);
            memberService.save(member);
            return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
        }
    }

    /**
     * 实名认证
     *
     * @param realName
     * @param idCard
     *
     *
     */
    /**
     * 实名认证
     *
     * @param realName
     * @param idCard
     * @param user
     * @return
     */
//    @RequestMapping("/real/name")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realApprove(String realName, String idCard, String idCardFront,
                                     String idCardBack, String handHeldIdCard,
                                     @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        hasText(realName, msService.getMessage("MISSING_REAL_NAME"));
        hasText(idCard, msService.getMessage("MISSING_ID_CARD"));
        hasText(idCardFront, msService.getMessage("MISSING_ID_CARD_FRONT"));
        hasText(idCardBack, msService.getMessage("MISSING_ID_CARD_BACK"));
        hasText(handHeldIdCard, msService.getMessage("MISSING_ID_CARD_HAND"));

        //add by tansitao 时间： 2019/1/15 原因：放开身份证唯一的校验
       /* List<Member> members = memberService.findAllByIdNumber(idCard);
        if(members != null && members.size() > 0)
        {
            return  MessageResult.error(msService.getMessage("IDCARD_USED"));
        }*/
        Member member = memberService.findOne(user.getId());
        if ("China".equals(member.getCountry().getEnName())) {
            isTrue(ValidateUtil.isChineseName(realName), msService.getMessage("REAL_NAME_ILLEGAL"));
            isTrue(IdcardValidator.isValidate18Idcard(idCard), msService.getMessage("ID_CARD_ILLEGAL"));
        }
        isTrue(member.getRealNameStatus() == RealNameStatus.NOT_CERTIFIED, msService.getMessage("REPEAT_REAL_NAME_REQUEST"));
        MemberApplication memberApplication = new MemberApplication();
        memberApplication.setAuditStatus(AuditStatus.AUDIT_ING);
        memberApplication.setRealName(realName);
        memberApplication.setIdCard(idCard);
        memberApplication.setMember(member);
        //截取URL，只保存key字段
        memberApplication.setIdentityCardImgFront(idCardFront.split("[?]")[0].split("[|/]", 4)[3]);
        memberApplication.setIdentityCardImgInHand(handHeldIdCard.split("[?]")[0].split("[|/]", 4)[3]);
        memberApplication.setIdentityCardImgReverse(idCardBack.split("[?]")[0].split("[|/]", 4)[3]);
//        memberApplication.setIdentityCardImgFront(idCardFront);
//        memberApplication.setIdentityCardImgInHand(handHeldIdCard);
//        memberApplication.setIdentityCardImgReverse(idCardBack);
        memberApplication.setCreateTime(new Date());
        memberApplicationService.save(memberApplication);
        member.setRealNameStatus(RealNameStatus.AUDITING);
        memberService.save(member);
        return MessageResult.success(msService.getMessage("REAL_APPLY_SUCCESS"));
    }

    /**
     * 实名认证 - 身份证号检测
     *
     * @param idCard 身份证号
     * @author zhongxj
     * @date 2019.08.02
     * @desc 一个身份证，最多实名认证2个账号
     */
    @ApiOperation(value = "实名认证 - 身份证号检测")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "居民身份证号", name = "idCard", dataType = "String")
    })
    @RequestMapping(value = "/real/name/check", method = {RequestMethod.GET, RequestMethod.POST})
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realApproveCheck(String idCard) {
        int count = memberService.countMemberByIdNumberAndRealNameStatus(idCard);
        isTrue(count < 2, msService.getMessage("CERTIFICATION_NUMBER"));
        return MessageResult.success("", count > 0);
    }

    /**
     * 实名认证 - 自动认证
     *
     * @param realName 真实姓名
     * @param idCard   身份证号
     * @author zhongxj
     * @date 2019.08.02
     * @desc 一个身份证，最多实名认证2个账号
     */
    @ApiOperation(value = "自动认证")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "身份证姓名", name = "realName", dataType = "String"),
            @ApiImplicitParam(value = "居民身份证号", name = "idCard", dataType = "String")
    })
    @RequestMapping(value = "/real/name/auto", method = {RequestMethod.GET, RequestMethod.POST})
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realApproveForPhone(String realName, String idCard, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        int count = memberService.countMemberByIdNumberAndRealNameStatus(idCard);
        isTrue(count < 2, msService.getMessage("CERTIFICATION_NUMBER"));
        isTrue(ValidateUtil.isChineseName(realName), msService.getMessage("REAL_NAME_ILLEGAL"));
        isTrue(IdcardValidator.isValidate18Idcard(idCard), msService.getMessage("ID_CARD_ILLEGAL"));
        Member member = memberService.findOne(user.getId());
        isTrue(member.getRealNameStatus() == RealNameStatus.NOT_CERTIFIED, msService.getMessage("REPEAT_REAL_NAME_REQUEST"));
        isTrue(IdcardValidator.is18Idcard(idCard), msService.getMessage("REPEAT_REAL_NAME_REQUEST"));

        hasText(member.getMobilePhone(), msService.getMessage("MISSING_PHONE"));

        // add by archx 2019.04.04 : 认证信息冻结检测
        isTrue(!memberFrozenVoucherService.isFrozenCertificate(idCard, CertificateType.IDENTITY_CARD), msService.getMessage("REAL_NAME_FROZEN"));

        List<Member> members = memberService.findAllByIdNumber(idCard);

        //会员审核信息
        MemberApplication memberApplication = new MemberApplication();
        memberApplication.setAuditStatus(AuditStatus.AUDIT_DEFEATED);
        memberApplication.setIdCard(idCard);
        memberApplication.setRealName(realName);
        memberApplication.setMember(member);
        memberApplication.setCountry("中国");
        memberApplication.setIdentityCardImgFront("0");
        memberApplication.setIdentityCardImgInHand("0");
        memberApplication.setIdentityCardImgReverse("0");
        memberApplication.setCreateTime(new Date());
        memberApplication.setRepeatAudit(members != null && members.size() > 0 ? 1 : 0);
        memberApplication.setOpType(4); // 标记为自动认证

        // 调用三方接口实现认证过程
        try {
            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(phoneCheckApi);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new NameValuePair("name", realName));
            params.add(new NameValuePair("idcard", idCard));
            params.add(new NameValuePair("mobile", member.getMobilePhone()));
            log.info("mobile：{}", member.getMobilePhone());
            method.setQueryString(params.toArray(new NameValuePair[0]));
            method.addRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            method.addRequestHeader("Authorization", "APPCODE " + phoneCheckAppCode);
            method.getParams().setContentCharset("UTF-8");
            client.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                String result = method.getResponseBodyAsString();
                log.info("第三方认证接口返回结果：{}", result);
                JSONObject json = JSONObject.parseObject(result);
                JSONObject data = JSONObject.parseObject(json.getString("result"));
                if ("0".equals(json.getString("code")) && "1".equals(data.getString("res"))) {
                    // 实名认证通过
                    memberApplication.setAuditStatus(AuditStatus.AUDIT_SUCCESS);
                    // 实名会员
                    member.setMemberLevel(MemberLevelEnum.REALNAME);
                    // 添加会员真实姓名
                    member.setRealName(memberApplication.getRealName());
                    // 会员身份证号码
                    member.setIdNumber(memberApplication.getIdCard());
                    // 会员状态修改已认证
                    member.setRealNameStatus(RealNameStatus.VERIFIED);
                    // 实名认证时间
                    member.setApplicationTime(new Date());
                    // 用户国籍
                    Location location = new Location();
                    location.setCountry("中国"+json.getString("address"));
//                    location.setProvince(json.getString("province"));
//                    location.setCity(json.getString("city"));
//                    location.setDistrict(json.getString("prefecture"));
                    // location.setAddress(json.getString("address"));
                    member.setLocation(location);
                    // 如果该用户没有定位则获取定位信息
                    if (StringUtils.isEmpty(member.getAreaId())) {
                        DimArea dimArea = gyDmcodeService.getPostionInfo(member.getIdNumber(), member.getMobilePhone(), member.getIp());
                        if (dimArea != null) {
                            member.setAreaId(dimArea.getAreaId());
                        }
                    }
                }
            } else {
                log.error("调用第三方认证接口失败：{}", method.getStatusCode());
                return MessageResult.error(msService.getMessage("REAL_AUTO_API_FAIL"));
            }
        } catch (Exception e) {
            return MessageResult.error(msService.getMessage("REAL_AUTO_API_FAIL"));
        }

        memberApplicationService.save(memberApplication);
        memberService.save(member);

        if (memberApplication.getAuditStatus() == AuditStatus.AUDIT_SUCCESS) {
            // 发放奖励
            if (members == null || members.size() <= 0) {
                // 发放实名认证用户奖励
                memberApplicationService.handleActivityForRealName(member);
                // 发放实名认证推荐用户返佣
                Member memberPromotion = null;
                if (member.getInviterId() != null) {
                    memberPromotion = memberService.findOne(member.getInviterId());
                }
                memberApplicationService.PromotionForRealName(member, memberPromotion);
            }
            return MessageResult.success(msService.getMessage("REAL_AUTO_SUCCESS"));
        } else {
            return MessageResult.error(msService.getMessage("REAL_AUTO_FAIL"));
        }
    }

    /**
     * 实名认证（提交资料人工审核）
     *
     * @param realName 真实姓名
     * @param idCard   身份证号码
     * @author zhongxj
     * @date 2019.08.02
     * @desc 一个身份证，最多实名认证2个账号
     */
    @RequestMapping("/real/name")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realApproveForCountry(String realName, String idCard, String idCardFront,
                                               String idCardBack, String handHeldIdCard, String country, //add:国籍  zyj
                                               @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                               CertificateType certificateType
    ) {
        int count = memberService.countMemberByIdNumberAndRealNameStatus(idCard);
        isTrue(count < 2, msService.getMessage("CERTIFICATION_NUMBER"));
        hasText(realName, msService.getMessage("MISSING_REAL_NAME"));
        hasText(idCard, msService.getMessage("MISSING_ID_CARD"));
        hasText(idCardFront, msService.getMessage("MISSING_ID_CARD_FRONT"));
        // edit wsy data:2019-3-1 11:00:45
        if ("中国".equals(country)) {
            hasText(idCardBack, msService.getMessage("MISSING_ID_CARD_BACK"));//身份证背面图片
        }
        hasText(handHeldIdCard, msService.getMessage("MISSING_ID_CARD_HAND"));//身份证手持图片
        //add: 国籍
        hasText(country, msService.getMessage("MISSING_COUNTRY"));
        //根据身份证号查所有用户，放开身份证唯一校验
        /*List<Member> members = memberService.findAllByIdNumber(idCard);
        if(members != null && members.size() > 0){
            //身份证号已被用
            return  MessageResult.error(msService.getMessage("IDCARD_USED"));
        }*/
        Member member = memberService.findOne(user.getId());
        if ("中国".equals(country)) {
            //国家为中国时
            //姓名不合法
            isTrue(ValidateUtil.isChineseName(realName), msService.getMessage("REAL_NAME_ILLEGAL"));
            //身份证不合法
            isTrue(IdcardValidator.isValidate18Idcard(idCard), msService.getMessage("ID_CARD_ILLEGAL"));
            // add by archx 2019.04.04 : 认证信息冻结检测
            isTrue(!memberFrozenVoucherService.isFrozenCertificate(idCard, CertificateType.IDENTITY_CARD), msService.getMessage("REAL_NAME_FROZEN"));
        } else {
            //add by zyj 2019.03.19: 增加证件类型
            notNull(certificateType, msService.getMessage("MISSING_CERTIFICATE_TYPE"));
            if (certificateType == CertificateType.PASSPORT) {
                //验证护照号长度
                isTrue(idCard.length() >= 7, "护照编号长度至少7位");
            }
            // add by archx 2019.04.04 : 认证信息冻结检测
            isTrue(!memberFrozenVoucherService.isFrozenCertificate(idCard, certificateType), msService.getMessage("REAL_NAME_FROZEN"));
        }
        //设置国籍相关信息
//        Country country1 = countryService.findOne(country);
//        member.setCountry(country1);
        // edit by wsy, date: 2019-1-25 17:22:33 reason: 实名认证时，修改手机号归宿的，导致手机号无法收到短信验证码
        Location location = new Location();
        location.setCountry(country);
        member.setLocation(location);

        //已在审核或在审核中
        isTrue(member.getRealNameStatus() == RealNameStatus.NOT_CERTIFIED, msService.getMessage("REPEAT_REAL_NAME_REQUEST"));
        //会员审核信息
        MemberApplication memberApplication = new MemberApplication();
        memberApplication.setAuditStatus(AuditStatus.AUDIT_ING);
        memberApplication.setRealName(realName);
        memberApplication.setIdCard(idCard);
        memberApplication.setMember(member);
        //add:  会员审核信息中加入国籍审核
        memberApplication.setCountry(country);
        //add by zyj 2019.03.19: 增加证件类型
        memberApplication.setCertificateType(certificateType == null ? null : certificateType);
        //截取URL，只保存key字段
        memberApplication.setIdentityCardImgFront(idCardFront.split("[?]")[0].split("[|/]", 4)[3]);
        memberApplication.setIdentityCardImgInHand(handHeldIdCard.split("[?]")[0].split("[|/]", 4)[3]);
        // edit wsy data:2019-3-1 11:00:45
        if ("中国".equals(country) || org.apache.commons.lang3.StringUtils.isNotBlank(idCardBack)) {
            memberApplication.setIdentityCardImgReverse(idCardBack.split("[?]")[0].split("[|/]", 4)[3]);
        } else {
            memberApplication.setIdentityCardImgReverse("0");
        }
//        memberApplication.setIdentityCardImgFront(idCardFront);
//        memberApplication.setIdentityCardImgInHand(handHeldIdCard);
//        memberApplication.setIdentityCardImgReverse(idCardBack);
        memberApplication.setCreateTime(new Date());
        // edit by wsy date: 2019-1-23 10:17:32 reason: 实名认证重复认证标记
        List<Member> members = memberService.findAllByIdNumber(idCard);
        memberApplication.setRepeatAudit(members != null && members.size() > 0 ? 1 : 0);
        //会员审核单
        memberApplicationService.save(memberApplication);
        member.setRealNameStatus(RealNameStatus.AUDITING);
        //add by zyj 2019.03.19: 增加证件类型
        member.setCertificateType(certificateType);
        memberService.save(member);
        return MessageResult.success(msService.getMessage("REAL_APPLY_SUCCESS"));
    }

    /**
     * 证件信息冻结验证
     *
     * @param idCard          证件号码
     * @param certificateType 证件类型
     * @return result
     */
    @RequestMapping(value = "/real/name/frozen", method = {RequestMethod.GET, RequestMethod.POST})
    public MessageResult realNameApproveFrozenCheck(@RequestParam("idCard") String idCard, @RequestParam("cType") CertificateType certificateType) {
        boolean frozenCertificate = memberFrozenVoucherService.isFrozenCertificate(idCard, certificateType);
        return MessageResult.success("", frozenCertificate);
    }

    /**
     * 查询实名认证情况（废弃）
     *
     * @param user
     * @return
     */
    @PostMapping("/real/detail")
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public MessageResult realNameApproveDetailAbandoned(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        List<Predicate> predicateList = new ArrayList<>();
        predicateList.add(QMemberApplication.memberApplication.member.eq(member));
        PageResult<MemberApplication> memberApplicationPageResult = memberApplicationService.query(predicateList, null, null);
        MemberApplication memberApplication = new MemberApplication();
        if (memberApplicationPageResult != null && memberApplicationPageResult.getContent() != null
                && memberApplicationPageResult.getContent().size() > 0) {
            memberApplication = memberApplicationPageResult.getContent().get(0);
        }
        MessageResult result = MessageResult.success();
        result.setData(memberApplication);
        return result;
    }

    /**
     * 获取实名认证情况
     *
     * @param user
     * @return
     */
    @ApiOperation(value = "获取实名认证情况", tags = "获取实名认证情况", notes = "PC、APP端获取实名认证情况信息")
    @PostMapping("/real/detail/news")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realNameApproveDetail(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Long memberId = user.getId();
        Member member = memberService.findOne(memberId);
        RealNameStatus realNameStatus = member.getRealNameStatus();
        RealNameDetailDTO realNameDetailDTO = new RealNameDetailDTO();
        if (realNameStatus != null && realNameStatus.getOrdinal() > 0) {
            String country = member.getCountry() == null ? "--" : member.getCountry().getZhName();
            String certificateType = member.getCertificateType() == null ? "--" : member.getCertificateType().getCnName();
            realNameDetailDTO.setCertificateType(certificateType);
            realNameDetailDTO.setRealName(member.getRealName());
            realNameDetailDTO.setIdNumber(member.getIdNumber());
            realNameDetailDTO.setCountry(country);
        } else {
            //update by shushiping 2019-12-20 需要获取驳回理由
//            Integer auditStatus = memberApplicationService.getMemberApplicationByAuditStatus(memberId);
            Map<String,Object> auditMap = memberApplicationService.getMemberApplicationByAuditStatus(memberId);
            if ("3".equals(auditMap.get("auditStatus"))) {
                realNameStatus = RealNameStatus.AUDIT_DEFEATED;
                realNameDetailDTO.setRejectReason(auditMap.get("rejectReason") == null ? "" : auditMap.get("rejectReason").toString());
            }
        }
        realNameDetailDTO.setRealNameStatus(realNameStatus);
        MessageResult result = MessageResult.success();
        result.setData(realNameDetailDTO);
        return result;
    }

    /**
     * 检查身份证是否被使用
     *
     * @param user
     * @return
     */
    @PostMapping("/checkIdcard")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult checkIdcard(@SessionAttribute(SESSION_MEMBER) AuthMember user, String idCard) {
        MessageResult result = new MessageResult();
        //根据身份证号查所有用户
        List<Member> members = memberService.findAllByIdNumber(idCard);
        if (members != null && members.size() > 0) {
            //身份证号已被用
            result.setData("1");
        } else {
            //身份证没有被使用
            result.setData("0");
        }
        return result;
    }

    /**
     * 账户设置
     *
     * @param user
     * @return
     */
    @RequestMapping("/account/setting")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult accountSetting(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        hasText(member.getJyPassword(), msService.getMessage("NO_JY_PASSWORD"));

        MessageResult result = MessageResult.success();
        WechatPay wechatPay = new WechatPay();
        Alipay alipay = new Alipay();
        Epay epay = new Epay();
        String accountName = "";
        try {
            //从阿里云获取地址
            if (member.getAlipay() != null) {
                String uri = AliyunUtil.getPrivateUrl(aliyunConfig, member.getAlipay().getQrCodeUrl());
                alipay.setQrCodeUrl(uri);
                alipay.setAliNo(member.getAlipay().getAliNo());
            }
            if (member.getWechatPay() != null) {
                String uri = AliyunUtil.getPrivateUrl(aliyunConfig, member.getWechatPay().getQrWeCodeUrl());
                wechatPay.setQrWeCodeUrl(uri);
                wechatPay.setWechat(member.getWechatPay().getWechat());
                wechatPay.setWechatNick(member.getWechatPay().getWechatNick());
            }
            //add by tansitao 时间： 2018/8/14 原因：增加epay支付方式
            MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(user.getId());
            if (memberPaymentAccount != null) {
                epay.setEpayNo(memberPaymentAccount.getEpayNo());
                accountName = memberPaymentAccount.getAccountName(); //add by tansitao 时间： 2018/11/9 原因：获取账号姓名
            }
            MemberAccount memberAccount = MemberAccount.builder().alipay(alipay)
                    .aliVerified(member.getAlipay() == null ? IS_FALSE : IS_TRUE)
                    .bankInfo(member.getBankInfo())
                    .bankVerified(member.getBankInfo() == null ? IS_FALSE : IS_TRUE)
                    .wechatPay(wechatPay)
                    .wechatVerified(member.getWechatPay() == null ? IS_FALSE : IS_TRUE)
                    .realName(StringUtils.isEmpty(accountName) ? member.getRealName() : accountName) //edit by tansitao 时间： 2018/11/9 原因：如果账号姓名不存在用实名姓名
                    .epayVerified(memberPaymentAccount == null || StringUtils.isEmpty(memberPaymentAccount.getEpayNo()) ? IS_FALSE : IS_TRUE)//edit by tansitao 时间： 2018/11/11 原因：epay账号为空也显示为未绑定
                    .epay(epay)
                    //edit by fumy . date:2018.11.01 reason:交易次数查询
                    .transactions(member.getTransactions())
                    .build();
            result.setData(memberAccount);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(500);
            //edit by tansitao 时间： 2018/5/22 原因：修改为从亚马逊获取地址
            result.setMessage(msService.getMessage("ACHIEVE_PERSONAL_FAIL"));
        }

        return result;
    }


    /**
     * 设置银行卡
     *
     * @param bindBank
     * @param bindingResult
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping("/bind/bank")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindBank(@Valid BindBank bindBank, BindingResult bindingResult, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(member.getBankInfo() == null, msService.getMessage("REPEAT_SETTING"));
        return doBank(bindBank, bindingResult, user);
    }

    private MessageResult doBank(BindBank bindBank, BindingResult bindingResult, AuthMember user) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Member member = memberService.findOne(user.getId());

        String jyPassword = new SimpleHash("md5", bindBank.getJyPassword(), member.getSalt(), 2).toHex().toLowerCase();

        isTrue(jyPassword.equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBank(bindBank.getBank());
        bankInfo.setBranch(bindBank.getBranch());
        bankInfo.setCardNo(bindBank.getCardNo());
        member.setBankInfo(bankInfo);
        memberService.save(member);

        //add by tansitao 时间： 2018/11/9 原因：修改或增加账户姓名
        MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
        if (memberPaymentAccount == null) {
            memberPaymentAccount = new MemberPaymentAccount();
        }
        memberPaymentAccount.setMemberId(member.getId());
        memberPaymentAccount.setAccountName(bindBank.getRealName());
        memberPaymentAccountService.save(memberPaymentAccount);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 更改银行卡
     *
     * @param bindBank
     * @param bindingResult
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping("/update/bank")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateBank(@Valid BindBank bindBank, BindingResult bindingResult, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        boolean isInnerFinc = this.isInnerFinc(user.getId());
        if (isInnerFinc) {
            return error(msService.getMessage("INNER_CANNOT_UPDATE"));
        }
        return doBank(bindBank, bindingResult, user);
    }

    /**
     * 绑定阿里支付宝
     *
     * @param bindAli
     * @param bindingResult
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping("/bind/ali")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindAli(@Valid BindAli bindAli, BindingResult bindingResult, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(member.getAlipay() == null, msService.getMessage("REPEAT_SETTING"));
        return doAli(bindAli, bindingResult, user);
    }

    private MessageResult doAli(BindAli bindAli, BindingResult bindingResult, AuthMember user) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Member member = memberService.findOne(user.getId());
        String jyPassword = new SimpleHash("md5", bindAli.getJyPassword(), member.getSalt(), 2).toHex().toLowerCase();
        isTrue(jyPassword.equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        Alipay alipay = new Alipay();
        alipay.setAliNo(bindAli.getAli());

        //edity by yangch 解决前端传输url地址为空
        if (!StringUtils.isEmpty(bindAli.getQrCodeUrl())) {
            //截取支付宝二维码URL，只保存key字段
            alipay.setQrCodeUrl(bindAli.getQrCodeUrl().split("[?]")[0].split("[|/]", 4)[3]);
        }
        member.setAlipay(alipay);
        memberService.save(member);

        //add by tansitao 时间： 2018/11/9 原因：修改或增加账户姓名
        MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
        if (memberPaymentAccount == null) {
            memberPaymentAccount = new MemberPaymentAccount();
        }
        memberPaymentAccount.setMemberId(member.getId());
        memberPaymentAccount.setAccountName(bindAli.getRealName());
        memberPaymentAccountService.save(memberPaymentAccount);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     *  * 绑定Epay
     *  * @author tansitao
     *  * @time 2018/8/13 17:32 
     *
     * @param bindEpay  
     */
    @RequestMapping("/bind/epay")
//    @Transactional(rollbackFor = Exception.class)
    public MessageResult binEpay(@Valid BindEpay bindEpay, BindingResult bindingResult, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Member member = memberService.findOne(user.getId());
        String jyPassword = new SimpleHash("md5", bindEpay.getJyPassword(), member.getSalt(), 2).toHex().toLowerCase();
        isTrue(jyPassword.equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
        if (memberPaymentAccount == null) {
            memberPaymentAccount = new MemberPaymentAccount();
        } else {
            boolean isInnerFinc = this.isInnerFinc(user.getId());
            if (isInnerFinc) {
                return error(msService.getMessage("INNER_CANNOT_UPDATE"));
            }
        }
        memberPaymentAccount.setEpayNo(bindEpay.getEpayNo());
        memberPaymentAccount.setMemberId(member.getId());
        memberPaymentAccount.setAccountName(bindEpay.getRealName());
        memberPaymentAccountService.save(memberPaymentAccount);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 修改支付宝
     *
     * @param bindAli
     * @param bindingResult
     * @param user
     * @return
     */
    @RequestMapping("/update/ali")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateAli(@Valid BindAli bindAli, BindingResult bindingResult, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        boolean isInnerFinc = this.isInnerFinc(user.getId());
        if (isInnerFinc) {
            return error(msService.getMessage("INNER_CANNOT_UPDATE"));
        }
        return doAli(bindAli, bindingResult, user);
    }

    /**
     * 绑定微信
     *
     * @param bindWechat
     * @param bindingResult
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping("/bind/wechat")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindWechat(@Valid BindWechat bindWechat, BindingResult bindingResult, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(member.getWechatPay() == null, msService.getMessage("REPEAT_SETTING"));
        return doWechat(bindWechat, bindingResult, user);
    }

    private MessageResult doWechat(BindWechat bindWechat, BindingResult bindingResult, AuthMember user) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Member member = memberService.findOne(user.getId());
        String jyPassword = new SimpleHash("md5", bindWechat.getJyPassword(), member.getSalt(), 2).toHex().toLowerCase();
        isTrue(jyPassword.equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        WechatPay wechatPay = new WechatPay();
        wechatPay.setWechat(bindWechat.getWechat());
        wechatPay.setWechatNick(bindWechat.getWechatNick());
        //截取微信二维码URL，只保存key字段
        //edity by yangch 解决前端传输url地址为空
        if (!StringUtils.isEmpty(bindWechat.getQrCodeUrl())) {
            wechatPay.setQrWeCodeUrl(bindWechat.getQrCodeUrl().split("[?]")[0].split("[|/]", 4)[3]);
        }
        member.setWechatPay(wechatPay);
        memberService.save(member);

        //add by tansitao 时间： 2018/11/9 原因：修改或增加账户姓名
        MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
        if (memberPaymentAccount == null) {
            memberPaymentAccount = new MemberPaymentAccount();
        }
        memberPaymentAccount.setMemberId(member.getId());
        memberPaymentAccount.setAccountName(bindWechat.getRealName());
        memberPaymentAccountService.save(memberPaymentAccount);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 修改微信
     *
     * @param bindWechat
     * @param bindingResult
     * @param user
     * @return
     */
    @RequestMapping("/update/wechat")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateWechat(@Valid BindWechat bindWechat, BindingResult bindingResult, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        boolean isInnerFinc = this.isInnerFinc(user.getId());
        if (isInnerFinc) {
            return error(msService.getMessage("INNER_CANNOT_UPDATE"));
        }
        return doWechat(bindWechat, bindingResult, user);
    }

    /**
     * 解绑微信 支付宝  epay  1:支付宝2:微信3:epay
     * @return
     */
    @RequestMapping(value = "/pay/unBing",method = {RequestMethod.POST,RequestMethod.GET})
    public MessageRespResult unBind(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                     @RequestParam Integer unBingType, @RequestParam String jyPassword){
        Member member = memberService.findOne(user.getId());
        String pa = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        isTrue(pa.equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));

        memberService.unBindPay(member.getId(),unBingType);

        return MessageRespResult.success(msService.getMessage("SETTING_SUCCESS"));
    }


    /**
     * 是否为内部商户
     *
     * @param memberId
     * @author Zhang Yanjun
     * @time 2018.12.18 13:41
     */
    private boolean isInnerFinc(long memberId) {
        //edit by zyj 20190710: 现要求内部商户也可以修改支付信息
//        List<FincMemberAccount> list = fincMemberAccountService.findByType(1);
//        for (FincMemberAccount fincMemberAccount : list) {
//            if (CommonUtils.equals(fincMemberAccount.getMemberId(), memberId)) {
//                return true;
//            }
//        }
        return false;
    }

    /**
     * 认证商家申请状态
     *
     * @param user
     * @return
     */
    @RequestMapping("/certified/business/status")
    public MessageResult certifiedBusinessStatus(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findMemberId(user.getId());
        CertifiedBusinessInfo certifiedBusinessInfo = new CertifiedBusinessInfo();
        certifiedBusinessInfo.setCertifiedBusinessStatus(member.getCertifiedBusinessStatus());

        certifiedBusinessInfo.setMemberLevel(member.getMemberLevel());
        log.info("会员状态信息:{}", certifiedBusinessInfo);
        if (member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.FAILED)) {
            List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, member.getCertifiedBusinessStatus());
            log.info("会员申请商家认证信息:{}", businessAuthApplyList);
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                certifiedBusinessInfo.setCertifiedBusinessStatus(businessAuthApplyList.get(0).getCertifiedBusinessStatus());
                log.info("会员申请商家认证最新信息:{}", businessAuthApplyList.get(0));
                certifiedBusinessInfo.setCheckFailReason(businessAuthApplyList.get(0).getDetail());
            }
        }
        if (certifiedBusinessInfo.getCertifiedBusinessStatus().getOrdinal() == 0 || certifiedBusinessInfo.getCertifiedBusinessStatus().getOrdinal() == 2
                || certifiedBusinessInfo.getCertifiedBusinessStatus().getOrdinal() > 4) {
            List<UnlockCoinApply> unlockCoinApplyList = unlockCoinApplyService.findByMember(member);
            if (unlockCoinApplyList != null && unlockCoinApplyList.size() > 0) {
                if (unlockCoinApplyList.get(0).getStatus() == BusinessApplyStatus.PASS) {
                    if (member.getCertifiedBusinessStatus() != VERIFIED) {
                        if (member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.CANCEL_FORCE)) {
                            certifiedBusinessInfo.setCertifiedBusinessStatus(CANCEL_FORCE);
                        } else {
                            certifiedBusinessInfo.setCertifiedBusinessStatus(RETURN_SUCCESS);
                        }
                        //提示
                        certifiedBusinessInfo.setPassRemark(unlockCoinApplyList.get(0).getReason());
                    }
                } else if (unlockCoinApplyList.get(0).getStatus() == BusinessApplyStatus.NOPASS) {
                    //退保审核失败
                    certifiedBusinessInfo.setCertifiedBusinessStatus(RETURN_FAILED);
                    certifiedBusinessInfo.setApplyFailReason(unlockCoinApplyList.get(0).getRefusalReason());
                } else {
                    certifiedBusinessInfo.setCertifiedBusinessStatus(CANCEL_AUTH);
                }
            }
        }


        MessageResult result = MessageResult.success();
        result.setData(certifiedBusinessInfo);
        return result;
    }

    @RequestMapping("/business-auth-deposit/list")
    public MessageResult listBusinessAuthDepositList() {
        List<BusinessAuthDeposit> depositList = businessAuthDepositService.findAllByStatus(CommonStatus.NORMAL);
        depositList.forEach(deposit -> {
            deposit.setAdmin(null);
        });
        MessageResult result = MessageResult.success();
        result.setData(depositList);
        return result;
    }

    /**
     * 认证商家申请
     *
     * @param user
     * @return
     */
    @RequestMapping("/certified/business/apply")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult certifiedBusiness(@SessionAttribute(SESSION_MEMBER) AuthMember user, String json,
                                           @RequestParam Long businessAuthDepositId, @RequestParam String jyPassword) throws Exception {
        Member member = memberService.findOne(user.getId());
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));

        //只有未认证和认证失败、被强制取消的用户，可以发起认证申请
        isTrue(member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.NOT_CERTIFIED)
                || member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.FAILED)
                || member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.CANCEL_FORCE), msService.getMessage("REPEAT_APPLICATION"));
        isTrue(member.getMemberLevel().equals(MemberLevelEnum.REALNAME), msService.getMessage("NO_REAL_NAME"));
        //获取保证金币信息
        BusinessAuthDeposit businessAuthDeposit = businessAuthDepositService.findById(businessAuthDepositId);
        //保证金状态异常
        isTrue(businessAuthDeposit != null, msService.getMessage("INVALID_COIN"));
        isTrue(CommonStatus.NORMAL == businessAuthDeposit.getStatus(), msService.getMessage("INVALID_COIN"));

        //获取钱包信息
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(businessAuthDeposit.getCoin().getUnit(), member.getId());
        isTrue(memberWallet != null, msService.getMessage("WALLET_GET_FAIL"));
        //冻结保证金需要的金额
        MessageResult result = memberWalletService.freezeBalanceToLockBalance(memberWallet, businessAuthDeposit.getAmount());
        if (result.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        //申请记录
        BusinessAuthApply businessAuthApply = new BusinessAuthApply();
        businessAuthApply.setCreateTime(new Date());
        businessAuthApply.setAuthInfo(json);
        businessAuthApply.setCertifiedBusinessStatus(CertifiedBusinessStatus.AUDITING);
        businessAuthApply.setMember(member);
        //不一定会有保证金策略
        businessAuthApply.setBusinessAuthDeposit(businessAuthDeposit);
        businessAuthApply.setAmount(businessAuthDeposit.getAmount());

        businessAuthApplyService.create(businessAuthApply);

        member.setCertifiedBusinessApplyTime(new Date());
        member.setCertifiedBusinessStatus(CertifiedBusinessStatus.AUDITING);
        memberService.save(member);
        CertifiedBusinessInfo certifiedBusinessInfo = new CertifiedBusinessInfo();
        certifiedBusinessInfo.setCertifiedBusinessStatus(member.getCertifiedBusinessStatus());
        certifiedBusinessInfo.setMemberLevel(member.getMemberLevel());
        result = MessageResult.success();
        result.setData(certifiedBusinessInfo);
        return result;
    }

    /**
     * 申请取消认证商家,
     *
     * @return
     */
    @PostMapping("/cancel/business")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelBusiness(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                        @RequestParam(value = "reason", defaultValue = "") String reason, @RequestParam String jyPassword) {
        Member member = memberService.findOne(user.getId());
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));

        //判断是否有未完成的订单
        List<Order> list = otcOrderService.getAllOrdering(user.getId());
        if (list.size() > 0) {
            throw new IllegalArgumentException(msService.getMessage("HAVE_ORDER_ING"));
        }
        //商家认证状态是否为认证成功状态
        boolean bol = CommonUtils.equals(member.getCertifiedBusinessStatus(), CertifiedBusinessStatus.VERIFIED);
        if (bol == false) {
            throw new IllegalArgumentException(msService.getMessage("BUSINESS_STATE_ERROR"));
        }
        List<Advertise> advertiseList = advertiseService.getAllOnAdvertiseByMemberId(member.getId());
        isTrue(advertiseList == null || advertiseList.size() == 0, msService.getMessage("PLEASE_SOLD_OUT_ADV"));

        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, CertifiedBusinessStatus.VERIFIED);
        if (businessAuthApplyList == null || businessAuthApplyList.size() < 1) {
            return MessageResult.error("you are not verified business,business auth apply not exist......");
        }

        if (businessAuthApplyList.get(0).getCertifiedBusinessStatus() != CertifiedBusinessStatus.VERIFIED) {
            return MessageResult.error("data exception, state inconsistency(CertifiedBusinessStatus in BusinessAuthApply and Member)");
        }

        member.setCertifiedBusinessStatus(CANCEL_AUTH);
        log.info("会员状态:{}", member.getCertifiedBusinessStatus());
        memberService.save(member);
        log.info("会员状态:{}", member.getCertifiedBusinessStatus());

        UnlockCoinApply unlockCoinApply = new UnlockCoinApply();
        unlockCoinApply.setLockCoinDetailId(businessAuthApplyList.get(0).getLockCoinDetailId());
        unlockCoinApply.setMember(businessAuthApplyList.get(0).getMember());
        unlockCoinApply.setStatus(BusinessApplyStatus.APPLYING);
        unlockCoinApply.setApplyReason(reason);
        log.info("退保申请状态:{}", unlockCoinApply.getStatus());
        unlockCoinApplyService.save(unlockCoinApply);
        log.info("退保申请状态:{}", unlockCoinApply.getStatus());

        return MessageResult.success();
    }

    @ApiOperation(value = "验证原手机")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "原手机号", name = "phone", dataType = "String"),
            @ApiImplicitParam(value = "验证码", name = "code", dataType = "String")
    })
    @RequestMapping(value = "/get/phone", method = {RequestMethod.POST})
    public MessageResult getPhone(HttpServletRequest request, String phone, String code,
                                  @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user
    ){
        hasText(phone, msService.getMessage("MISSING_PHONE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        //获取缓存对象
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //获取缓存的验证码
        Object cache = valueOperations.get(SysConstant.PHONE_LOGIN_CODE + phone);
        //是否传入手机号
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        if (!code.equals(cache.toString())) {
            //验证码错误
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(SysConstant.PHONE_LOGIN_CODE + phone);
        }
        return MessageResult.success();
    }
    /**
     * 更换手机
     *
     * @param request
     * @param password //登录密码
     * @param phone    //新手机号
     * @param code     //手机验证码
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "更换手机号")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "登录密码", name = "password", dataType = "String"),
            @ApiImplicitParam(value = "新手机号", name = "phone", dataType = "String"),
            @ApiImplicitParam(value = "验证码", name = "code", dataType = "String")
    })
    @RequestMapping(value = "/change/phone", method = {RequestMethod.POST})
    @Transactional(rollbackFor = Exception.class)
    @CollectActionEvent(collectType = CollectActionEventType.CHANGE_PHONE_EMAIL, memberId = "#user.getId()",refId = "#collect")
    public MessageResult changePhone(HttpServletRequest request, String password, String phone, String code,
                                     @RequestParam(defaultValue = "mobile") String collect,
                                     @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user
    ) throws Exception {
        Member member = memberService.findOne(user.getId());
        hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
        hasText(phone, msService.getMessage("MISSING_PHONE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        Member member1 = memberService.findByPhone(phone);
        //手机号已绑定
        isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
        //获取缓存对象
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //获取缓存的验证码
        Object cache = valueOperations.get(SysConstant.PHONE_CHANGE_CODE_PREFIX + phone);
        //是否传入手机号
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        //加密
        String userPwd = CommonUtils.getMd5Password(password, member.getSalt());
//      edit by fumy.2018.10.31 ,reason: 因修改手机号发送验证码传入了国籍，可能会冲突，取消验证提交的手机号码格式
//        if (member.getCountry().getAreaCode().equals("86")) {  //如果手机号为中国
//            if (!ValidateUtil.isMobilePhone(phone.trim())) {  //判断手机号格式是否正确
//                return MessageResult.error(msService.getMessage("PHONE_FORMAT_ERROR"));
//            }
//        }
        //验证登录密码
        if (member.getPassword().equals(userPwd)) {
            if (!code.equals(cache.toString())) {
                //验证码错误
                return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.PHONE_CHANGE_CODE_PREFIX + phone);
            }
            String oldPhone = member.getMobilePhone();
            member.setMobilePhone(phone);
            //修改手机 禁止任何交易
            member.setTransactionStatus(BooleanEnum.IS_FALSE);
            memberService.save(member);
            // 换绑手机时如果同时绑定了谷歌则重置手机登录、提币验证开启状态
            /**
             * lc 11月6日修改,无需关闭
             */
//            if (member.getGoogleState() == 1) {
//                MemberSecuritySet securitySet = securitySetService.findOneBymemberId(member.getId());
//                if (securitySet != null && securitySet.getIsOpenGoogleLogin().isIs()) {
//                    securitySet.setIsOpenPhoneLogin(BooleanEnum.IS_FALSE);
//                    securitySet.setIsOpenPhoneUpCoin(BooleanEnum.IS_FALSE);
//                    securitySetService.save(securitySet);
//                }
//            }
            //给老号码发一条修改成功的短信
            sendMessage(oldPhone, member.getCountry().getZhName(), phone);
            return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
        } else {
            request.removeAttribute(SysConstant.SESSION_MEMBER);
            //密码错误
            return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
        }
    }


    // @Async
    public MessageResult sendMessage(String phone, String country, String newPhone) throws Exception {
        Assert.notNull(phone, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));
        Assert.notNull(country, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));
        Country country1 = countryService.findOne(country);
        Assert.notNull(country1, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));
        MessageResult result;
        //获取短信的内容
        String content = smsProvider.formatVerifyPhone(newPhone);
        if (country1.getAreaCode().equals("86")) {
            Assert.isTrue(ValidateUtil.isMobilePhone(phone.trim()), localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
            result = smsProvider.sendSingleMessage(phone, content);
        } else {
            result = smsProvider.sendInternationalMessage(content, country1.getAreaCode() + phone);
        }
        return result;
    }

    /**
     * 更换邮箱
     *
     * @param request
     * @param password
     * @param email
     * @param code
     * @param //user
     * @return true
     * @author fumy
     * @time 2018.07.11 15:33
     */
    @ApiOperation(value = "更换邮箱")
    @RequestMapping(value = "/change/email", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiImplicitParams({
            @ApiImplicitParam(value = "登录密码", name = "password", dataType = "String"),
            @ApiImplicitParam(value = "邮箱地址", name = "email", dataType = "String"),
            @ApiImplicitParam(value = "验证码", name = "code", dataType = "String")
    })
    @Transactional(rollbackFor = Exception.class)
    @CollectActionEvent(collectType = CollectActionEventType.CHANGE_PHONE_EMAIL, memberId = "#user.getId()",refId = "#collect")
    public MessageResult changeEmail(HttpServletRequest request, String password, String email, String code
            ,@RequestParam(defaultValue = "email") String collect
            , @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        //获取用户信息
        Member member = memberService.findOne(user.getId());
        hasText(password, msService.getMessage("MISSING_JY_PASSWORD"));
        hasText(email, msService.getMessage("MISSING_EMAIL"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        isTrue(ValidateUtil.isEmail(email), msService.getMessage("EMAIL_FORMAT_ERROR"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object cache = valueOperations.get(EMAIL_BIND_CODE_PREFIX + email);
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        isTrue(code.equals(cache.toString()), msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        isTrue(member.getEmail() != null, msService.getMessage("REPEAT_EMAIL_REQUEST"));
        String userPassword = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();
        if (!userPassword.equals(member.getPassword())) {
            request.removeAttribute(SysConstant.SESSION_MEMBER);
            return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
        } else {
            String oldEmail = member.getEmail();
            member.setEmail(email);
            //修改邮箱 禁止任何交易
            member.setTransactionStatus(BooleanEnum.IS_FALSE);
            memberService.save(member);
            getService().sentEmailCode(oldEmail, email);
            return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
        }
    }

    @Async
    public void sentEmailCode(String oldEmail, String newEmail) throws MessagingException, IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        model.put("datetime", DateUtil.getNewDate());
        model.put("email", newEmail);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("changeMessageEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        multiMailConfiguration.sentEmailHtml(oldEmail, "REGISTRATION_EMAIL_TITLE", html);

        log.info("send email for {},content:{}", oldEmail, html);
    }

    /**
     * 更换邮箱发送验证码
     *
     * @param email
     * @param user
     * @return true
     * @author fumy
     * @time 2018.08.01 16:15
     */
    @RequestMapping("/change/email/code")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendChangeEmail(String email, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));
        Assert.isTrue(!memberService.emailIsExist(email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
        String code = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object o = valueOperations.get(EMAIL_BIND_CODE_PREFIX + email);
        if (o != null) {
            code = String.valueOf(o);
        }
        try {
            getService().sentChangeEmailCode(valueOperations, email, code);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @Async
    public void sentChangeEmailCode(ValueOperations valueOperations, String email, String code) throws MessagingException, IOException, TemplateException {
        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("bindCodeEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        multiMailConfiguration.sentEmailHtml(email, localeMessageSourceService.getMessage("REGISTRATION_EMAIL_TITLE"), html);

        log.info("send email for {},content:{}", email, html);
        valueOperations.set(EMAIL_BIND_CODE_PREFIX + email, code, 10, TimeUnit.MINUTES);
    }

    /**
     * 通用发送邮箱验证码
     *
     * @param email
     * @param user
     * @return true
     * @author fumy
     * @time 2018.10.26 16:15
     */
    @ApiOperation(value = "通用发送邮箱验证码", tags = "发送邮箱验证码")
    @ApiImplicitParam(value = "邮箱地址,如：xxx@163.com，不传从session中获取", name = "email", dataType = "String")
    @RequestMapping(value = "/validation/email_code", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendUpdatePwdEmailCode(String email, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        //当邮箱地址不传时，默认从session获取邮箱
        if (StringUtils.isEmpty(email) && user != null) {
            email = user.getEmail();
            if (StringUtils.isEmpty(email)) {
                Member one = memberService.findOne(user.getId());
                email = one.getEmail();
                Assert.isTrue(StringUtils.hasText(email), localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
            }
        }
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.getOperations().delete(SysConstant.EMAIL_COMMON_CODE_PREFIX + email);
        String code = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        if (valueOperations.get(EMAIL_COMMON_CODE_PREFIX + email) != null) {
            return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_SEND"));
        }
        try {
            sentCommonEmailCode(valueOperations, email, code);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @Async
    public void sentCommonEmailCode(ValueOperations valueOperations, String email, String code) throws MessagingException, IOException, TemplateException {
        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("commonCodeEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        multiMailConfiguration.sentEmailHtml(email, localeMessageSourceService.getMessage("REGISTRATION_EMAIL_TITLE"), html);

        log.info("send email for {},content:{}", email, html);
        valueOperations.set(EMAIL_COMMON_CODE_PREFIX + email, code, 10, TimeUnit.MINUTES);
    }

    /**
     * 查询商家锁仓记录
     *
     * @param user
     * @return true
     * @author fumy
     * @time 2018.06.21 17:05
     */
    @ApiOperation(value = "查询商家锁仓记录")
    @PostMapping("/lock-coin-detail/customer")
    public MessageResult customerLockRecord(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        List<CustomerLockCoinDetail> list = lockCoinDetailService.findByMemberIdAndType(user.getId(), LockType.DEPOSIT);
        return success("查询成功", list);
    }

    /**
     * 查询用户安全设置信息
     *
     * @param memberId
     * @param user
     * @return true
     * @author fumy
     * @time 2018.11.01 14:42
     */
    @ApiOperation(value = "获取用户安全设置信息", tags = "安全设置")
    @ApiImplicitParam(value = "用户id,Pc端可不传，App端需要传递", name = "memberId", dataType = "Long", allowEmptyValue = true)
    @PostMapping("/security/info")
    public MessageRespResult<MemberSecurityInfoVo> getSecuritySetByMemberId(Long memberId, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        if (memberId == null && user != null) {
            memberId = user.getId();
        }
        PayStatusInfo payment = new PayStatusInfo();
        MemberSecurityInfoVo infoVo = new MemberSecurityInfoVo();
//        MemberSecurityInfoDto infoDto = memberService.findSecurityInfoByMemberId(memberId);
        Member member = memberService.findOne(memberId);
        MemberSecuritySet securitySet = memberSecuritySetService.findOneBymemberId(memberId);
        MemberPaymentAccount epay = memberPaymentAccountService.findPaymentAccountByMemberId(memberId);
        if (epay == null || StringUtils.isEmpty(epay.getEpayNo())) {
            payment.setEpay(BooleanEnum.IS_FALSE);
        } else {
            payment.setEpay(BooleanEnum.IS_TRUE);
        }

        if (member.getAlipay() != null) {
            payment.setAliPay(BooleanEnum.IS_TRUE);
        }
        if (member.getBankInfo() != null) {
            payment.setBank(BooleanEnum.IS_TRUE);
        }
        if (member.getWechatPay() != null) {
            payment.setWeChat(BooleanEnum.IS_TRUE);
        }

        if (member.getJyPassword() != null) {
            infoVo.setJyPassword(BooleanEnum.IS_TRUE);
        }
        if (member.getMobilePhone() != null) {
            infoVo.setPhone(BooleanEnum.IS_TRUE);
        }
        if (member.getEmail() != null) {
            infoVo.setEmail(BooleanEnum.IS_TRUE);
        }
        if (member.getCertifiedBusinessStatus() != null && member.getCertifiedBusinessStatus() == CertifiedBusinessStatus.VERIFIED) {
            infoVo.setCertifiedBusinessStatus(BooleanEnum.IS_TRUE);
        }
        if (member.getRealNameStatus() != null && member.getRealNameStatus() == RealNameStatus.VERIFIED) {
            infoVo.setRealNameStatus(BooleanEnum.IS_TRUE);
        }
        infoVo.setPayment(payment);
        infoVo.setMemberId(member.getId());
        if (securitySet == null) {
            infoVo.setIsOpenPropertyShow(BooleanEnum.IS_FALSE);
            infoVo.setIsOpenGoogleLogin(BooleanEnum.IS_FALSE);
            infoVo.setIsOpenGoogleUpCoin(BooleanEnum.IS_FALSE);
            infoVo.setIsOpenPhoneLogin(BooleanEnum.IS_FALSE);
            infoVo.setIsOpenPhoneUpCoin(BooleanEnum.IS_FALSE);
        } else {
            infoVo.setIsOpenPropertyShow(securitySet.getIsOpenPropertyShow());
            infoVo.setIsOpenGoogleLogin(securitySet.getIsOpenGoogleLogin());
            infoVo.setIsOpenGoogleUpCoin(securitySet.getIsOpenGoogleUpCoin());
            infoVo.setIsOpenPhoneLogin(securitySet.getIsOpenPhoneLogin());
            infoVo.setIsOpenPhoneUpCoin(securitySet.getIsOpenPhoneUpCoin());
        }


        return MessageRespResult.success("查询成功", infoVo);
    }

    /**
     * 验证资金密码是否正确
     *
     * @param
     * @author Zhang Yanjun
     * @time 2018.11.11 11:29
     */
    @ApiOperation(value = "验证资金密码是否正确", tags = "安全验证")
    @PostMapping("/isJyPassword")
    public MessageRespResult isJyPassword(String jyPassword, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        hasText(jyPassword, msService.getMessage("MISSING_JY_PASSWORD"));
        Member member = memberService.findOne(user.getId());
        String memberJyPass = member.getJyPassword();
        hasText(memberJyPass, msService.getMessage("ERROR_JYPASSWORD"));
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        if (jyPass.equals(memberJyPass)) {
            return MessageRespResult.success();
        } else {
            return MessageRespResult.error(msService.getMessage("ERROR_JYPASSWORD"));
        }
    }

    private ApproveController getService(){
        return SpringContextUtil.getBean(ApproveController.class);
    }

}
