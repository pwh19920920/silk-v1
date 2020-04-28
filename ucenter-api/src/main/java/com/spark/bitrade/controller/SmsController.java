package com.spark.bitrade.controller;

import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Country;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.CountryService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.SmsSendService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.MessageResult.error;
import static com.spark.bitrade.util.MessageResult.success;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author Zhang Jinwei
 * @date 2018年01月08日
 */
@Slf4j
@Api(description = "短信发送控制类")
@RestController
@RequestMapping("/mobile")
public class SmsController {

    @Autowired
    private SMSProviderProxy smsProvider;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MemberService memberService;
    @Autowired
    private SmsSendService smsSendService;
    @Resource
    private LocaleMessageSourceService localeMessageSourceService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * 注册验证码发送
     *
     * @return
     */
    @ApiOperation(value = "注册验证码",tags = "发送短信验证码")
    @PostMapping("/code")
    public MessageResult sendCheckCode(String phone, String country) throws Exception {
        Assert.isTrue(!memberService.phoneIsExist(phone), localeMessageSourceService.getMessage("PHONE_ALREADY_EXISTS"));
        Assert.notNull(country, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));
        Country country1 = countryService.findOne(country);
        Assert.notNull(country1, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = SysConstant.PHONE_REG_CODE_PREFIX + phone;
        Object code = valueOperations.get(key);
        if (code != null) {
            //判断如果请求间隔小于一分钟则请求失败
            if (!BigDecimalUtils.compare(DateUtil.diffMinute((Date) (valueOperations.get(key + "Time"))), BigDecimal.ONE)) {
                return error(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
            }
        }
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        MessageResult result;
        if (country1.getAreaCode().equals("86")) {
            Assert.isTrue(ValidateUtil.isMobilePhone(phone.trim()), localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
            //edit by tansitao 时间： 2018/7/13 原因：改成异步发送短信
            smsSendService.sendVerifyMessage(phone, randomCode);
        }
        //add by tansitao 时间： 2018/9/12 原因：判断是否为港澳台，发送繁体短信
        else if ("+886".equals(country1.getAreaCode()) || "+853".equals(country1.getAreaCode()) || "+852".equals(country1.getAreaCode()))
        {
            smsSendService.sendComplexMessage(randomCode, country1.getAreaCode() + phone);
        }
        else {
            //edit by tansitao 时间： 2018/7/13 原因：改成异步发送短信
            smsSendService.sendInternationalMessage(randomCode, country1.getAreaCode() + phone);
        }
        //edit by tansitao 时间： 2018/7/13 原因：修改为不需要对短信发送结束的转台判断
        valueOperations.getOperations().delete(key);
        valueOperations.getOperations().delete(key + "Time");
        // 缓存验证码
        valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
        valueOperations.set(key + "Time", new Date(), 10, TimeUnit.MINUTES);
        return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
    }

    /**
     * 重置交易密码验证码
     *
     * @param user
     * @return
     */
    @ApiOperation(value = "重置交易密码验证码",tags = "发送短信验证码")
    @RequestMapping(value = "/transaction/code", method = RequestMethod.POST)
    public MessageResult sendResetTransactionCode(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        Assert.hasText(member.getMobilePhone(), localeMessageSourceService.getMessage("NOT_BIND_PHONE"));
        return sendSmsCode(member,SysConstant.PHONE_RESET_TRANS_CODE_PREFIX);
    }

    /**
     * 绑定手机号验证码
     *  edit by tansitao 时间： 2018/5/12 原因：按照弱化需求，增加传入参数国家区号
     * @param phone
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "绑定手机号验证码",tags = "发送短信验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "手机号码",name = "phone",dataType ="String"),
            @ApiImplicitParam(value = "国家区号",name = "countryNo",dataType ="String")
    })
    @RequestMapping(value = "/bind/code", method = RequestMethod.POST)
    public MessageResult setBindPhoneCode(String phone , String countryNo,@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        Member member1 = memberService.findByPhone(phone);
        //手机号已绑定
        isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
        Assert.isTrue(!StringUtils.hasText(member.getMobilePhone()), localeMessageSourceService.getMessage("REPEAT_PHONE_REQUEST"));
        //add by tansitao 时间： 2018/5/12 原因：增加国家不能为空的判断
        notNull(countryNo, localeMessageSourceService.getMessage("COUNTRY_NOT_NULL"));
        MessageResult result;
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        //add by tansitao 时间： 2018/5/12 原因：修改判断对象为国家区号
        if ("86".equals(countryNo)) {
            if (!ValidateUtil.isMobilePhone(phone.trim())) {
                return error(localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
            }
            //edit by tansitao 时间： 2018/7/13 原因：改成异步发送短信
            smsSendService.sendVerifyMessage(phone, randomCode);
        }
        //add by tansitao 时间： 2018/9/12 原因：判断是否为港澳台，发送繁体短信
        else if ("+886".equals(countryNo) || "+853".equals(countryNo) || "+852".equals(countryNo))
        {
            smsSendService.sendComplexMessage(randomCode, countryNo + member.getMobilePhone());
        }
        else {
            //edit by tansitao 时间： 2018/7/13 原因：改成异步发送短信
            smsSendService.sendInternationalMessage(randomCode, countryNo + phone);
        }
        //edit by tansitao 时间： 2018/7/13 原因：修改为不需要对短信发送结束的转台判断
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = SysConstant.PHONE_BIND_CODE_PREFIX + phone;
        valueOperations.getOperations().delete(key);
        // 缓存验证码
        valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
        return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
    }

    /**
     * 更改登录密码验证码
     *
     * @param user
     * @return
     */
    @ApiOperation(value = "更改登录密码验证码",tags = "发送短信验证码")
    @RequestMapping(value = "/update/password/code", method = RequestMethod.POST)
    public MessageResult updatePasswordCode(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        Assert.hasText(member.getMobilePhone(), localeMessageSourceService.getMessage("NOT_BIND_PHONE"));
        return sendSmsCode(member,SysConstant.PHONE_UPDATE_PASSWORD_PREFIX);
    }

    /**
     * 添加提币地址验证码
     * @param user
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "添加提币地址验证码",tags = "发送短信验证码")
    @RequestMapping(value = "/add/address/code", method = RequestMethod.POST)
    public MessageResult addAddressCode(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        Assert.hasText(member.getMobilePhone(), localeMessageSourceService.getMessage("NOT_BIND_PHONE"));
        return sendSmsCode(member,SysConstant.PHONE_ADD_ADDRESS_PREFIX);
    }

    /**
     * 忘记密码验证码
     */
    @ApiOperation(value = "忘记密码验证码",tags = "发送短信验证码")
    @ApiImplicitParam(value = "手机号",name = "account",dataType = "String")
    @RequestMapping(value = "/reset/code", method = RequestMethod.POST)
    public MessageResult resetPasswordCode(String account) throws Exception {
        Member member = memberService.findByPhone(account);
        Assert.notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        return sendSmsCode(member,SysConstant.RESET_PASSWORD_CODE_PREFIX);
    }

    /**
     * 更改手机验证码
     */
    @ApiOperation(value = "更换手机验证码")
    @RequestMapping(value = "/change/code", method = RequestMethod.POST)
    public MessageResult resetPhoneCode(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        Assert.hasText(member.getMobilePhone(), localeMessageSourceService.getMessage("NOT_BIND_PHONE"));
        return sendSmsCode(member,SysConstant.PHONE_CHANGE_CODE_PREFIX);
    }

    /**
     * 登录、提币手机验证码(通用发送短信验证码)
     * @author tansitao
     * @time 2018/7/5 14:17 
     */
    @ApiOperation(value = "通用发送短信验证码",tags = "发送短信验证码")
    @ApiImplicitParam(value = "手机号",name = "phone",dataType = "String")
    @RequestMapping(value = "/validation/code", method = RequestMethod.POST)
    public MessageResult loginCode(String phone,@ApiIgnore @SessionAttribute(value = SESSION_MEMBER, required = false) AuthMember user) throws Exception {
        //当手机号不传时，默认从session获取手机号
        if(StringUtils.isEmpty(phone) && user != null){
            phone = user.getMobilePhone();
        }
        Member member = memberService.findByPhone(phone);
        Assert.notNull(member , localeMessageSourceService.getMessage("NOT_BIND_PHONE"));
        return sendSmsCode(member,SysConstant.PHONE_LOGIN_CODE);
    }

    /**
     * 发送短信验证码
     * @author fumy
     * @time 2018.11.08 9:58
     * @param member
     * @param cacheKeyPrefix 缓存前缀
     * @return true
     */
    public MessageResult sendSmsCode(Member member,String cacheKeyPrefix){
        String redisKey = cacheKeyPrefix + member.getMobilePhone();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object code = valueOperations.get(redisKey);
        if (code != null) {
            //判断如果请求间隔小于一分钟则请求失败
            if (!BigDecimalUtils.compare(DateUtil.diffMinute((Date) (valueOperations.get(redisKey + "Time"))), BigDecimal.ONE)) {
                return error(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
            }
        }
        //获取验证码
        String randomCode = code != null ? String.valueOf(code) : CommonUtils.getVerifyCode(100000, 999999);
        log.info("发送短信验证码：{}, old: {}", redisKey, randomCode);
        if ("86".equals(member.getCountry().getAreaCode())) {
            Assert.isTrue(ValidateUtil.isMobilePhone(member.getMobilePhone().trim()), localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
            //edit by tansitao 时间： 2018/7/13 原因：改成异步发送短信
            smsSendService.sendVerifyMessage(member.getMobilePhone(), randomCode);
        }
        //add by tansitao 时间： 2018/9/12 原因：判断是否为港澳台，发送繁体短信
        else if ("+886".equals(member.getCountry().getAreaCode()) || "+853".equals(member.getCountry().getAreaCode()) || "+852".equals(member.getCountry().getAreaCode())) {
            smsSendService.sendComplexMessage(randomCode, member.getCountry().getAreaCode() + member.getMobilePhone());
        } else {
            //edit by tansitao 时间： 2018/7/13 原因：改成异步发送短信
            smsSendService.sendInternationalMessage(randomCode, member.getCountry().getAreaCode() + member.getMobilePhone());
        }
        valueOperations.getOperations().delete(redisKey);
        valueOperations.getOperations().delete(redisKey + "Time");
        // 缓存验证码
        valueOperations.set(redisKey, randomCode, 10, TimeUnit.MINUTES);
        valueOperations.set(redisKey + "Time", new Date(), 10, TimeUnit.MINUTES);
        return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));

    }


    /**
    * 更换手机号码时发送验证码
    * @author Zhang Yanjun
    * @time 2018.07.11 17:21
    * @param phone 新手机号
    * @return
    */
    @ApiOperation(value = "更换手机发送验证码",tags = "发送短信验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "国籍编号",name = "countryNo",dataType = "String"),
            @ApiImplicitParam(value = "手机号",name = "phone",dataType = "String")
    })
    @RequestMapping(value = "/change/phone/code", method = RequestMethod.POST)
    public MessageResult changePhoneCode(String countryNo,String phone, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user
    ) throws Exception {
        Member member = memberService.findOne(user.getId());
        Member member1 = memberService.findByPhone(phone);
        //手机号已绑定
        isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        MessageResult result;
        if ("86".equals(countryNo))
        {
            Assert.isTrue(ValidateUtil.isMobilePhone(phone.trim()), localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
            result = smsProvider.sendVerifyMessage(phone, randomCode);
        }//繁体港澳台
        else if ("+886".equals(countryNo) || "+853".equals(countryNo) || "+852".equals(countryNo))
        {
            result = smsProvider.sendComplexMessage(randomCode, countryNo + phone);
        }
        else {
            result = smsProvider.sendInternationalMessage(randomCode, countryNo + phone);
        }
        if (result.getCode() == 0) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String key = SysConstant.PHONE_CHANGE_CODE_PREFIX + phone;
            valueOperations.getOperations().delete(key);
            // 缓存验证码
            valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
            return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
        } else {
            return error(localeMessageSourceService.getMessage("SEND_SMS_FAILED"));
        }
    }

    @RequestMapping(value = "/dcc-codeCheck")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult dccLoginCodeCheck(@SessionAttribute(value = SESSION_MEMBER, required = false) AuthMember user, @RequestParam String phone, @RequestParam String code) {
        return loginCodeCheck(user, phone, code);
    }

    /**
      * 手机登录验证码核查
      * @author tansitao
      * @time 2018/7/5 14:32 
      */
    @RequestMapping(value = "/codeCheck")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginCodeCheck(@SessionAttribute(value = SESSION_MEMBER, required = false) AuthMember user, @RequestParam String phone, @RequestParam String code) {
        Assert.hasText(phone, localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
        Assert.hasText(code, localeMessageSourceService.getMessage("MISSING_VERIFICATION_CODE"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.PHONE_LOGIN_CODE + phone);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        if (!code.equals(redisCode.toString())) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(SysConstant.PHONE_LOGIN_CODE + phone);
        }
        log.info("登录用户:{}",user);
        if(user!=null){
            user.addLoginVerifyRequire("isOpenPhoneLogin", true);
        }
        return success();
    }

    /**
     *  发送语音验证码
     * @author fumy
     * @time 2018.08.10 11:41
     * @param phone
     * @param country
     * @return true
     */
    @RequestMapping(value = "/register/voice/code", method = RequestMethod.POST)
    public MessageResult sendVoiceCode(@RequestParam String phone,String country) throws Exception {
        Assert.isTrue(!memberService.phoneIsExist(phone), localeMessageSourceService.getMessage("PHONE_ALREADY_EXISTS"));
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(1000, 9999));
        MessageResult result;
        if ("中国".equals(country))
        {
            Assert.isTrue(ValidateUtil.isMobilePhone(phone.trim()), localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
            result = smsProvider.sendVoiceCode(phone, randomCode);
        } else {
           return error("不支持的国家和地区");
        }
        if (result.getCode() == 0) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String key = SysConstant.PHONE_REG_CODE_PREFIX + phone;
            valueOperations.getOperations().delete(key);
            // 缓存验证码
            valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
            valueOperations.set(key + "Time", new Date(), 10, TimeUnit.MINUTES);
            return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
        } else {
            return error(localeMessageSourceService.getMessage("SEND_SMS_FAILED"));
        }
    }

}
