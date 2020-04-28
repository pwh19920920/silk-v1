package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberSecuritySet;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberSecuritySetService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.GoogleAuthenticatorUtil;
import com.spark.bitrade.util.Md5;
import com.spark.bitrade.util.MessageResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.manager.util.SessionUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @author shenzucai
 * @time 2018.04.09 11:07
 */
@RestController
@Slf4j
@RequestMapping("/google")
public class GoogleAuthenticationController extends BaseController{

    @Autowired
    private MemberService memberService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private MemberSecuritySetService securitySetService;

    /**
     * 验证google
     * @author shenzucai
     * @time 2018.04.09 11:36
     * @param user
     * @param codes
     * @return true
     */

    @RequestMapping(value = "/yzgoogle",method = RequestMethod.POST)
    public MessageResult yzgoogle(@SessionAttribute(SESSION_MEMBER) AuthMember user,String codes) {
        // enter the code shown on device. Edit this and run it fast before the
        // code expires!
        long code = Long.parseLong(codes);
        Member member = memberService.findOne(user.getId());
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        //  ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        boolean r = ga.check_code(member.getGoogleKey(), code, t);
        System.out.println("rrrr="+r);
        if(!r){
            //edit by tansitao 时间： 2018/5/21 原因：增加国际化
            return MessageResult.error(msService.getMessage("AUTH_FAIL"));
        }
        else{
            user.addLoginVerifyRequire("isOpenGoogleLogin", true);
            //edit by tansitao 时间： 2018/5/21 原因：增加国际化
            return MessageResult.success(msService.getMessage("AUTH_SUCCESS"));
        }
    }


    /**
     * 生成谷歌认证码
     * @return
     */
    @RequestMapping(value = "/sendgoogle",method = RequestMethod.GET)
    public MessageResult  sendgoogle(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        log.info("开始进入用户id={}",user.getId());
        long current = System.currentTimeMillis();
        Member member = memberService.findOne(user.getId());
        log.info("查询完毕 耗时={}",System.currentTimeMillis()-current);
        if (member == null){
            //edit by tansitao 时间： 2018/5/21 原因：增加国际化
            return  MessageResult.error(msService.getMessage("RE_LOGIN"));
        }   
        String secret = GoogleAuthenticatorUtil.generateSecretKey();
        log.info("secret完毕 耗时={}",System.currentTimeMillis()-current);
        String qrBarcodeURL = "";
        if(StringUtils.isEmpty(member.getMobilePhone())){
            qrBarcodeURL = GoogleAuthenticatorUtil.getQRBarcodeURL(member.getEmail(),
                    "SilkTrader", secret);
        }else {
            qrBarcodeURL = GoogleAuthenticatorUtil.getQRBarcodeURL(member.getMobilePhone(),
                    "SilkTrader", secret);
        }
        log.info("qrBarcodeURL完毕 耗时={}",System.currentTimeMillis()-current);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("link",qrBarcodeURL);
        jsonObject.put("secret",secret);
        log.info("jsonObject完毕 耗时={}",System.currentTimeMillis()-current);
        MessageResult messageResult = new MessageResult();
        messageResult.setData(jsonObject);
        messageResult.setMessage("ACHIEVE_SUCCESS");
        log.info("执行完毕 耗时={}",System.currentTimeMillis()-current);
        return  messageResult;

    }


    /**
     * google解绑
     * @author shenzucai
     * @time 2018.04.09 12:47
     * @param codes
     * @param user
     * @return true
     */
    @ApiOperation(value = "解绑谷歌",tags = "安全设置")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "谷歌验证码", name = "codes", dataType = "String"),
            @ApiImplicitParam(value = "登录密码", name = "password", dataType = "String")
    })
    @RequestMapping(value = "/jcgoogle" ,method = RequestMethod.POST)
    public MessageResult jcgoogle(String codes, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String password) {
        Member member = memberService.findOne(user.getId());
        String GoogleKey = member.getGoogleKey();
        if(StringUtils.isEmpty(password)){
            //edit by tansitao 时间： 2018/5/21 原因：增加国际化
            return MessageResult.error(msService.getMessage("MISSING_PASSWORD"));
        }
        try {
            if(!(new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase()).equals(member.getPassword().toLowerCase())){
                //edit by tansitao 时间： 2018/5/21 原因：增加国际化
                return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long code = Long.parseLong(codes);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        // ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        boolean r = ga.check_code(GoogleKey, code, t);
        if(!r){
            //edit by tansitao 时间： 2018/5/21 原因：增加国际化
            return MessageResult.error(msService.getMessage("AUTH_FAIL"));

        }else{
            member.setGoogleDate(new Date());
            member.setGoogleState(0);
            Member result = memberService.save(member);
            if(result != null){
                //add by fumy .date:2018.11.06 reason:解绑谷歌成功后重置安全设置登录、提币谷歌验证
                MemberSecuritySet securitySet = securitySetService.findOneBymemberId(member.getId());
                if(securitySet!=null){
                    securitySet.setIsOpenGoogleLogin(BooleanEnum.IS_FALSE);
                    securitySet.setIsOpenGoogleUpCoin(BooleanEnum.IS_FALSE);
                    securitySetService.save(securitySet);
                }
                //edit by tansitao 时间： 2018/5/21 原因：增加国际化
                return MessageResult.success(msService.getMessage("UNBUNDLE_SUCCESS"));
            }else{
                //edit by tansitao 时间： 2018/5/21 原因：增加国际化
                return MessageResult.error(msService.getMessage("UNBUNDLE_FAIL"));
            }
        }
    }




        //ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        /**
         * 绑定google
         * @author shenzucai
         * @time 2018.04.09 15:19
         * @param codes
         * @param user
         * @return true
         */
        @RequestMapping(value = "/googleAuth" ,method = RequestMethod.POST)
        @Transactional(rollbackFor = Exception.class)
        public MessageResult googleAuth(String codes, @SessionAttribute(SESSION_MEMBER) AuthMember user,String secret) {

            Member member = memberService.findOne(user.getId());
            long code = Long.parseLong(codes);
            long t = System.currentTimeMillis();
            GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
            boolean r = ga.check_code(secret, code, t);
            if(!r){
                //edit by tansitao 时间： 2018/5/21 原因：增加国际化
                return MessageResult.error(msService.getMessage("AUTH_FAIL"));
            }else{
                member.setGoogleState(1);
                member.setGoogleKey(secret);
                member.setGoogleDate(new Date());
                Member result = memberService.save(member);
                //add by fumy . date:2019.11.09 reason:如果用户未绑定手机号，则默认开启谷歌登录、提币验证
                if(StringUtils.isEmpty(member.getMobilePhone())){
                    MemberSecuritySet securitySet = securitySetService.findOneBymemberId(member.getId());
                    if(securitySet == null){
                        securitySet = new MemberSecuritySet();
                    }
                    //默认开启谷歌登录、提币验证
                    securitySet.setMemberId(member.getId());
                    securitySet.setIsOpenGoogleLogin(BooleanEnum.IS_TRUE);
                    securitySet.setIsOpenGoogleUpCoin(BooleanEnum.IS_TRUE);
                    securitySetService.save(securitySet);
                }

                if(result != null){
                    //edit by tansitao 时间： 2018/5/21 原因：增加国际化
                    return MessageResult.success(msService.getMessage("BUNDLE_SUCCESS"));
                }else{
                    //edit by tansitao 时间： 2018/5/21 原因：增加国际化
                    return MessageResult.error(msService.getMessage("BUNDLE_FAIL"));
                }
            }
        }

}
