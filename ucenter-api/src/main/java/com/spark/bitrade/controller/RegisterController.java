package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.MultiMailConfiguration;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dto.ExchangeReleaseLockRequestDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.event.MemberEvent;
import com.spark.bitrade.feign.IExchangeReleaseLockApiService;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.spark.bitrade.constant.SysConstant.*;
import static com.spark.bitrade.util.MessageResult.error;
import static com.spark.bitrade.util.MessageResult.success;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * 会员注册
 *
 * @author Zhang Jinwei
 * @date 2017年12月29日
 */
@Controller
@Slf4j
public class RegisterController {

    //@Autowired
    //private JavaMailSender javaMailSender;

    @Autowired
    MultiMailConfiguration multiMailConfiguration;
    @Autowired
    IRegisterEvent iRegisterEvent;

    @Value("${spring.mail.username}")
    private String from;
    @Value("${spark.system.host}")
    private String host;
    @Value("${spark.system.name}")
    private String company;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private IdWorkByTwitter idWorkByTwitter;

    @Autowired
    private MemberEvent memberEvent;

    @Autowired
    private CountryService countryService;

    @Autowired
    MemberSecuritySetService securitySetService;

    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private IExchangeReleaseLockApiService iExchangeReleaseLockApiServicel;

    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private CoinService coinService ;

    private String userNameFormat = "U%06d";

    /**
     * 注册支持的国家
     *
     * @return
     */
    @RequestMapping(value = "/support/country", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult allCountry() {
        MessageResult result = success();
        List<Country> list = countryService.getAllCountry();
        result.setData(list);
        return result;
    }

    /**
     * 检查用户名是否重复
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "/register/check/username")
    @ResponseBody
    public MessageResult checkUsername(String username) {
        MessageResult result = success();
        if (memberService.usernameIsExist(username)) {
            result.setCode(500);
            result.setMessage(localeMessageSourceService.getMessage("ACTIVATION_FAILS_USERNAME"));
        }
        return result;
    }


    /**
     * 激活邮件
     *
     * @param key
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/register/active")
    @Transactional(rollbackFor = Exception.class)
    @Deprecated // 废弃
    public String activate(String key, HttpServletRequest request) throws Exception {
        if (StringUtils.isEmpty(key)) {
            request.setAttribute("result", localeMessageSourceService.getMessage("INVALID_LINK"));
            return "registeredResult";
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object info = valueOperations.get(key);
        LoginByEmail loginByEmail = null;
        if (info instanceof LoginByEmail) {
            loginByEmail = (LoginByEmail) info;
        }
        if (loginByEmail == null) {
            request.setAttribute("result", localeMessageSourceService.getMessage("INVALID_LINK"));
            return "registeredResult";
        }
        if (memberService.emailIsExist(loginByEmail.getEmail())) {
            request.setAttribute("result", localeMessageSourceService.getMessage("ACTIVATION_FAILS_EMAIL"));
            return "registeredResult";
        } else if (memberService.usernameIsExist(loginByEmail.getUsername())) {
            request.setAttribute("result", localeMessageSourceService.getMessage("ACTIVATION_FAILS_USERNAME"));
            return "registeredResult";
        }
        //删除redis里存的键值
        valueOperations.getOperations().delete(key);
        valueOperations.getOperations().delete(loginByEmail.getEmail());
        //不可重复随机数
        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex().toLowerCase();
        //生成密码
//        String password = Md5.md5Digest(loginByEmail.getPassword() + credentialsSalt).toLowerCase();
        String password = new SimpleHash("md5", loginByEmail.getPassword(), credentialsSalt, 2).toHex().toLowerCase();

        Member member = new Member();
        member.setMemberLevel(MemberLevelEnum.GENERAL);
        Location location = new Location();
        location.setCountry(loginByEmail.getCountry());
        member.setLocation(location);
        Country country = new Country();
        country.setZhName(loginByEmail.getCountry());
        member.setCountry(country);
        member.setUsername(loginByEmail.getUsername());
        member.setPassword(password);
        member.setEmail(loginByEmail.getEmail());
        member.setSalt(credentialsSalt);
        Member member1 = memberService.save(member);

        if (member1 != null) {
            //member1.setPromotionCode(String.format(userNameFormat, member1.getId()) + GeneratorUtil.getNonceString(2));
            //add by tansitao 时间： 2019/1/2 原因：获取登录的会员来源信息
            String thirdMark = request.getHeader("thirdMark");
            //edit by tansitao 时间： 2018/12/27 原因：增加设备信息，和会员来源
            memberEvent.onRegisterSuccess(member1, loginByEmail.getPromotion(), LoginType.WEB, request, thirdMark);
            request.setAttribute("result", localeMessageSourceService.getMessage("ACTIVATION_SUCCESSFUL"));
        } else {
            request.setAttribute("result", localeMessageSourceService.getMessage("ACTIVATION_FAILS"));
        }
        return "registeredResult";
    }

    /**
     * 邮箱注册
     *
     * @param loginByEmail
     * @param bindingResult
     * @return
     */
    @RequestMapping("/register/email")
    @ResponseBody
    public MessageResult registerByEmail(@Valid LoginByEmail loginByEmail, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        String email = loginByEmail.getEmail();
        isTrue(!memberService.emailIsExist(email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
        isTrue(!memberService.usernameIsExist(loginByEmail.getUsername()), localeMessageSourceService.getMessage("USERNAME_ALREADY_EXISTS"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        if (valueOperations.get(email) != null) {
            return error(localeMessageSourceService.getMessage("LOGIN_EMAIL_ALREADY_SEND"));
        }
        try {
            log.info("send==================================");
            getController().sentEmail(valueOperations, loginByEmail, email);
            log.info("success===============================");
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SEND_LOGIN_EMAIL_SUCCESS"));
    }

    /**
      * 邮件验证码的方式注册
      * @author yangch
      * @time 2018.04.13 17:03 
     * @param loginByEmail
     * @param bindingResult
     * @param request 用于请求分析
     */
    @RequestMapping("/register/email4Code")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)//edit by tansitao 时间： 2018/5/17 原因：添加事务控制
    public MessageResult registerByEmailNoActive(@Valid LoginByEmail loginByEmail, BindingResult bindingResult,HttpServletRequest request)  throws Exception {
        //验证注册信息
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        notNull(loginByEmail.getCode(), localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));

        //验证码不能为空
        String email = loginByEmail.getEmail();
        String key = SysConstant.EMAIL_REG_CODE_PREFIX + email;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object code = valueOperations.get(key);
        //add|edit|del by tansitao 时间： 2018/5/16 原因：增加对验证码的判断，以及修改过滤用户不合法的顺序
        notNull(code, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        //edit by lingxing 时间： 2018/7/16 原因：改用mybatis查询
        isTrue(!memberService.emailAndUsernameIsExist(loginByEmail.getUsername(),null,email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
        //isTrue(!memberService.usernameIsExist(loginByEmail.getUsername()), localeMessageSourceService.getMessage("USERNAME_ALREADY_EXISTS"));
        if (!code.toString().equals(loginByEmail.getCode())) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(key);
            valueOperations.getOperations().delete(key + "Time");
        }


        //判断邀请码是否正确
        if (StringUtils.hasText(loginByEmail.getPromotion())){
            if (!memberService.checkPromotion(loginByEmail.getPromotion())){
                return error(localeMessageSourceService.getMessage("PROMOTION_CODE_ERRO"));
            }
        }

        //add by zyj 2018.12.27 : 接入风控
        Member m = new Member();
        m.setEmail(loginByEmail.getEmail());
        MessageResult result1 = iRegisterEvent.register(request,null,m,loginByEmail.getPromotion());
        if (result1.getCode() != 0 ){
            return error(result1.getMessage());
        }

        //注册邮箱用户

        //不可重复随机数
        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex().toLowerCase();
        //老的生成密码方式
//        String password = Md5.md5Digest(loginByEmail.getPassword() + credentialsSalt).toLowerCase();
        //新的生成密码方式
        String password = new SimpleHash("md5", loginByEmail.getPassword(), credentialsSalt, 2).toHex().toLowerCase();

        Member member = new Member();
        member.setMemberLevel(MemberLevelEnum.GENERAL);
        //edit by tansitao 时间： 2018/5/13 原因：邮件注册取消对国家的判断
//        Location location = new Location();
//        location.setCountry(loginByEmail.getCountry());
//        member.setLocation(location);
//        Country country = new Country();
//        country.setZhName(loginByEmail.getCountry());
//        member.setCountry(country);
        member.setUsername(loginByEmail.getUsername());
        member.setPassword(password);
        member.setEmail(loginByEmail.getEmail());
        member.setSalt(credentialsSalt);
        //member.setPromotionCode(String.format(userNameFormat, member.getId()) + GeneratorUtil.getNonceString(2));
        //add|edit|del by  shenzucai 时间： 2018.05.29  原因：获取请求ip
        member.setIp(IpUtils.getIp(request));
        Member member1 = memberService.save(member);

//        memberEvent.onRegisterSuccess(member1, loginByEmail.getPromotion());
        //add by tansitao 时间： 2019/1/2 原因：获取登录的会员来源信息
        String thirdMark = request.getHeader("thirdMark");
        //edit by tansitao 时间： 2018/12/27 原因：增加设备信息，和会员来源
        memberEvent.onRegisterSuccess(member1, loginByEmail.getPromotion(), loginByEmail.getLoginType(), request, thirdMark);
        //add 注册赠送100锁仓ESP
        //getController().exchangeReleaseLock(member.getId(),member.getEmail());
        return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
    }

    /**
      * 发送邮件注册验证码
      * @author yangch
      * @time 2018.04.13 15:57
      */
    @ApiOperation(value = "注册邮箱验证码",tags = "发送邮箱验证码")
    @ApiImplicitParam(value = "邮箱地址",name = "email",dataType = "String")
    //@PostMapping("/register/email/code")
    @RequestMapping(value = "/register/email/code",method = {RequestMethod.POST,RequestMethod.GET,RequestMethod.OPTIONS})
    @ResponseBody
    public MessageResult sendRegEmailCheckCode(String email) throws Exception {
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));

        String key = SysConstant.EMAIL_REG_CODE_PREFIX + email;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object codeOld = valueOperations.get(key);
        if (codeOld != null) {
            //判断如果请求间隔小于一分钟则请求失败
            if (!BigDecimalUtils.compare(DateUtil.diffMinute((Date) (valueOperations.get(key + "Time"))), BigDecimal.ONE)) {
                return error(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
            }
        }
        /*if (valueOperations.get(key) != null) {
            return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_SEND")); //已发送邮箱验证码
        }*/
        isTrue(!memberService.emailIsExist(email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND")); //验证邮箱是否已存在

        String codeNew = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        try {
            getController().sendRegEmailCode(valueOperations, email, codeNew);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    /**
      * 发送注册验证码邮件
      * @author yangch
      * @time 2018.04.13 16:54 
     * @param valueOperations
     * @param email
     * @param code
     */
    @Async
    public void sendRegEmailCode(ValueOperations valueOperations, String email, String code) throws MessagingException, IOException, TemplateException {
        String key = SysConstant.EMAIL_REG_CODE_PREFIX + email;
        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("regCodeEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        //multiMailConfiguration.sentEmailHtml(email, company, html);
        multiMailConfiguration.sentEmailHtml(email, localeMessageSourceService.getMessage("REGISTRATION_EMAIL_TITLE"), html);

        log.info("send email for {},content:{}", email, html);
        valueOperations.getOperations().delete(key);
        valueOperations.getOperations().delete(key + "Time");

        valueOperations.set(key, code, 10, TimeUnit.MINUTES);
        valueOperations.set(key + "Time", new Date(), 10, TimeUnit.MINUTES);
    }


    @Async
    public void sentEmail(ValueOperations valueOperations, LoginByEmail loginByEmail, String email) throws MessagingException, IOException, TemplateException {
        //缓存邮箱和注册信息
        String token = UUID.randomUUID().toString().replace("-", "");
        /*
        eidty by yangch 2018-4-6
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(company);*/

        Map<String, Object> model = new HashMap<>(16);
        model.put("username", loginByEmail.getUsername());
        model.put("token", token);
        model.put("host", host);
        model.put("name", company);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("activateEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        //eidty by yangch 2018-4-6
        /*helper.setText(html, true);
        //发送邮件
        javaMailSender.send(mimeMessage);*/
        multiMailConfiguration.sentEmailHtml(email, company, html);

        valueOperations.set(token, loginByEmail, 12, TimeUnit.HOURS);
        valueOperations.set(email, "", 12, TimeUnit.HOURS);
    }

    /**
     * 手机注册
     *
     * @param loginByPhone
     * @param bindingResult
     * @param request 用于请求分析
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/register/phone",method = {RequestMethod.POST,RequestMethod.OPTIONS})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginByPhone(
            @Valid LoginByPhone loginByPhone,
            BindingResult bindingResult,
            HttpServletRequest request) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        if (loginByPhone.getCountry().equals("中国")) {
            Assert.isTrue(ValidateUtil.isMobilePhone(loginByPhone.getPhone().trim()), localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
        }
        //edit by tansitao 时间： 2018/5/16 原因：修改用户手机注册验证逻辑
        String phone = loginByPhone.getPhone();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object code = valueOperations.get(SysConstant.PHONE_REG_CODE_PREFIX + phone);
        notNull(code, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        //edit by lingxing 时间： 2018/7/16 原因：改用mybatis查询
        isTrue(!memberService.phoneAndUsernameIsExist(loginByPhone.getUsername(),phone,null), localeMessageSourceService.getMessage("USERNAME_ALREADY_EXISTS"));
        if (!code.toString().equals(loginByPhone.getCode())) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(SysConstant.PHONE_REG_CODE_PREFIX + phone);
        }

        //判断邀请码是否正确
        if (StringUtils.hasText(loginByPhone.getPromotion())){
            if (!memberService.checkPromotion(loginByPhone.getPromotion())){
                return error(localeMessageSourceService.getMessage("PROMOTION_CODE_ERRO"));
            }
        }

        //add by zyj 2018.12.27 : 接入风控
        Member m = new Member();
        m.setMobilePhone(loginByPhone.getPhone());
        MessageResult res = iRegisterEvent.register(request, null, m, loginByPhone.getPromotion());
        if (res.getCode() != 0){
            return error(res.getMessage());
        }


        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex().toLowerCase();
        //老的生成密码规则
//        String password = Md5.md5Digest(loginByPhone.getPassword() + credentialsSalt).toLowerCase();
        //新的生成密码规则
        String password = new SimpleHash("md5", loginByPhone.getPassword(), credentialsSalt, 2).toHex().toLowerCase();
        Member member=new Member();
        member.setMemberLevel(MemberLevelEnum.GENERAL);
        Location location = new Location();
        location.setCountry(loginByPhone.getCountry());
        Country country = new Country();
        country.setZhName(loginByPhone.getCountry());
        member.setCountry(country);
        member.setLocation(location);
        member.setUsername(loginByPhone.getUsername());
        member.setPassword(password);
        member.setMobilePhone(phone);
        member.setSalt(credentialsSalt);
        //add|edit|del by  shenzucai 时间： 2018.05.29  原因：获取请求ip
        member.setIp(IpUtils.getIp(request));

        Member member1 = memberService.save(member);
        member.setPromotionCode(String.format(userNameFormat, member.getId()) + GeneratorUtil.getNonceString(2));
        memberService.save(member);
        //add by fumy. date:2018.10.29 reason:手机注册默认开启手机登录、提币验证
        MemberSecuritySet memberSecuritySet = new MemberSecuritySet();
        memberSecuritySet.setMemberId(member1.getId());
        memberSecuritySet.setIsOpenPhoneLogin(BooleanEnum.IS_TRUE);
        memberSecuritySet.setIsOpenPhoneUpCoin(BooleanEnum.IS_TRUE);
        securitySetService.save(memberSecuritySet);

        //add by tansitao 时间： 2019/1/2 原因：获取登录的会员来源信息
        String thirdMark = request.getHeader("thirdMark");
        //edit by tansitao 时间： 2018/12/27 原因：增加设备信息，和会员来源
        memberEvent.onRegisterSuccess(member1, loginByPhone.getPromotion(), loginByPhone.getLoginType(), request, thirdMark);

        // 加入红包领取注册标识.. 存入redis
        if ("REDENVELOPEACTIVITIES".equals(loginByPhone.getNewRegiest())) {
            String key = "NEWREGIEST:MEMBERID:" + member.getId();
            redisTemplate.opsForValue().set(key, loginByPhone.getNewRegiest(), 1, TimeUnit.HOURS);
        }
        //edit lc 注册赠送锁仓100ESP
        //getController().exchangeReleaseLock(member.getId(),member.getMobilePhone());
        return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
    }

    /**
     * 发送绑定邮箱验证码
     *
     * @param email
     * @param user
     * @return
     */
    @ApiOperation(value = "发送绑定邮箱验证码",tags = "发送邮箱验证码")
    @ApiImplicitParam(value = "邮箱地址",name = "email",dataType = "String")
    @RequestMapping(value = "/bind/email/code",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendBindEmail(String email,@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));
        Member member = memberService.findOne(user.getId());
        Assert.isNull(member.getEmail(), localeMessageSourceService.getMessage("BIND_EMAIL_REPEAT"));
        Assert.isTrue(!memberService.emailIsExist(email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
        String code = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object o = valueOperations.get(EMAIL_BIND_CODE_PREFIX + email);
        if (o != null) {
            code=String.valueOf(o);
        }
        try {
            getController().sentEmailCode(valueOperations, email, code);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @Async
    public void sentEmailCode(ValueOperations valueOperations, String email, String code) throws MessagingException, IOException, TemplateException {
        /*
        //eidty by yangch 2018-4-6
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(company);*/

        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("bindCodeEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        //eidty by yangch 2018-4-6
        /*helper.setText(html, true);
        //发送邮件
        javaMailSender.send(mimeMessage);*/
        //multiMailConfiguration.sentEmailHtml(email, company, html);
        multiMailConfiguration.sentEmailHtml(email,  localeMessageSourceService.getMessage("REGISTRATION_EMAIL_TITLE"), html);

        log.info("send email for {},content:{}", email, html);
        valueOperations.set(EMAIL_BIND_CODE_PREFIX + email, code, 10, TimeUnit.MINUTES);
    }

    /**
     * 增加提币地址验证码
     *
     * @param user
     * @return
     */
    @ApiOperation(value = "增加提币地址验证码",tags = "发送邮箱验证码")
    @RequestMapping(value = "/add/address/code",method = {RequestMethod.POST})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendAddAddress(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        String code = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Member member = memberService.findOne(user.getId());
        String email = member.getEmail();
        if (email == null) {
            return error(localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
        }
        if (valueOperations.get(ADD_ADDRESS_CODE_PREFIX + email) != null) {
            return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_SEND"));
        }
        try {
            getController().sentEmailAddCode(valueOperations, email, code);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @Async
    public void sentEmailAddCode(ValueOperations valueOperations, String email, String code) throws MessagingException, IOException, TemplateException {
        /*
        //eidty by yangch 2018-4-6
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(company);*/

        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("addAddressCodeEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        //eidty by yangch 2018-4-6
        /*helper.setText(html, true);
        //发送邮件
        javaMailSender.send(mimeMessage);*/
        //multiMailConfiguration.sentEmailHtml(email, company, html);
        multiMailConfiguration.sentEmailHtml(email,  localeMessageSourceService.getMessage("REGISTRATION_EMAIL_TITLE"), html);
        valueOperations.set(ADD_ADDRESS_CODE_PREFIX + email, code, 10, TimeUnit.MINUTES);
    }

    @ApiOperation(value = "重置密码发送验证码",tags = "发送邮箱验证码")
    @ApiImplicitParam(value = "邮箱地址",name = "account",dataType = "String")
    @RequestMapping(value = "/reset/email/code",method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult sendResetPasswordCode(String account) {
        Member member = memberService.findByEmail(account);
        Assert.notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String code = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        Object o = valueOperations.get(RESET_PASSWORD_CODE_PREFIX + account);
        if (o != null) {
            code= String.valueOf(o);
        }
        try {
            getController().sentResetPassword(valueOperations, account, code);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @Async
    public void sentResetPassword(ValueOperations valueOperations, String email, String code) throws MessagingException, IOException, TemplateException {
        //eidty by yangch 2018-4-6
        /*MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(company);*/

        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("resetPasswordCodeEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        //eidty by yangch 2018-4-6
        /*helper.setText(html, true);
        //发送邮件
        javaMailSender.send(mimeMessage);*/
        //multiMailConfiguration.sentEmailHtml(email, company, html);
        multiMailConfiguration.sentEmailHtml(email,  localeMessageSourceService.getMessage("REGISTRATION_EMAIL_TITLE"), html);
        valueOperations.set(RESET_PASSWORD_CODE_PREFIX + email, code, 10, TimeUnit.MINUTES);
    }

    /**
     * 忘记密码后重置密码
     *
     * @param mode     0为手机验证，1为邮箱验证
     * @param account  手机或邮箱
     * @param code     验证码
     * @param password 新密码
     * @return
     */
    @ApiOperation(value = "忘记密码后重置密码",tags = "个人信息设置")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "0为手机验证，1为邮箱验证",name = "mode",dataType = "String"),
            @ApiImplicitParam(value = "手机或邮箱",name = "account",dataType = "String"),
            @ApiImplicitParam(value = "验证码",name = "code",dataType = "String"),
            @ApiImplicitParam(value = "新密码",name = "password",dataType = "String")
    })
    @RequestMapping(value = "/reset/login/password", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult forgetPassword(int mode, String account, String code, String password) throws Exception {
        Member member = null;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.RESET_PASSWORD_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        if (mode == 0) {
            member = memberService.findByPhone(account);
        } else if (mode == 1) {
            member = memberService.findByEmail(account);
        }
        //edit by yangch 2018-04-12 前端做了规则验证及md5的密文密传输，此处会错误误验证
        //isTrue(password.length() >= 6 && password.length() <= 20, localeMessageSourceService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
        isTrue(password.length() >0, localeMessageSourceService.getMessage("MISSING_PASSWORD"));
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        if (!code.equals(redisCode.toString())) {
            return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            valueOperations.getOperations().delete(SysConstant.RESET_PASSWORD_CODE_PREFIX + account);
        }
        //生成密码
//        String newPassword = Md5.md5Digest(password + member.getSalt()).toLowerCase();

        String newPassword = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();

        member.setPassword(newPassword);
        return success(localeMessageSourceService.getMessage("PASSWORD_RESET_SUCCEEDED"));
    }

    public RegisterController getController(){
        return SpringContextUtil.getBean(RegisterController.class);
    }


    /**
     * 币币交易释放锁仓规则
     * @parm
     * @return
     */
    @Async
    public void exchangeReleaseLock( Long memberId, String refId){
        String unit = "ESP";
        BigDecimal amount = new BigDecimal("100");
            MemberWallet promoteMemberWalletCeche = memberWalletService.findCacheByCoinUnitAndMemberId(unit,memberId);
            log.info("用户钱包={}",promoteMemberWalletCeche);
            if(promoteMemberWalletCeche == null){
                //对应币种的账户不存在，则创建对应的账户（解决买币账户不存在的问题）
                Coin coin = coinService.findByUnit(unit);
                if(null == coin){
                    log.warn("币种不存在。 币种名称={}", unit);
                    return;
                }
                promoteMemberWalletCeche = memberWalletService.createMemberWallet(memberId, coin);
                if(null == promoteMemberWalletCeche){
                    log.warn("用户账户不存在。用户id={},币种名称={}",memberId,  unit);
                    return;
                }
            }
            //钱包锁仓余额 增加到可用余额
            // MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(unit, memberId);
            //   MessageResult rl = memberWalletService.increaseBalance(promoteMemberWalletCeche.getId(),amout);
                ExchangeReleaseLockRequestDTO requestDTO = new ExchangeReleaseLockRequestDTO();
                requestDTO.setCoinSymbol(unit);
                requestDTO.setLockAmount(amount.toPlainString());
                requestDTO.setMemberId(Integer.valueOf(memberId.toString()));
                 //充值记录id
                requestDTO.setRefId(refId);
                requestDTO.setType("1");
                MessageRespResult lockRl  = iExchangeReleaseLockApiServicel.exchangeReleaseLock(JSON.toJSONString(requestDTO));
                if(!lockRl.isSuccess()){
                        log.info("新用户注册锁仓ESP失败,执行手动锁仓");
                        memberWalletService.freezeBalanceToLockBalance(promoteMemberWalletCeche,amount);
                }else{
                    MemberTransaction transaction = new MemberTransaction();
                    transaction.setAmount(amount);
                    transaction.setSymbol(unit);
                    transaction.setMemberId(memberId);
                    transaction.setType(TransactionType.LOCK_ESP);
                    transaction.setComment(TransactionType.LOCK_ESP.getCnName());
                    transaction.setCreateTime(new Date());
                    transactionService.save(transaction);

                }
    }
}
