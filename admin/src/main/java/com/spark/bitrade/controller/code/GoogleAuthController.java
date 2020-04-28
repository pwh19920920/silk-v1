package com.spark.bitrade.controller.code;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.core.Menu;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.AdminService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.SysPermissionService;
import com.spark.bitrade.service.SysRoleService;
import com.spark.bitrade.util.GoogleAuthenticatorUtil;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.constant.SysConstant.SESSION_ADMIN;


/**
 * @author lingxing
 * @time 2018.07.30 10:26
 */
@RestController
@Slf4j
@RequestMapping("/google")
public class GoogleAuthController extends BaseController{
    @Autowired
    AdminService adminService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private SysRoleService sysRoleService;
    @Resource
    private SysPermissionService sysPermissionService;
    /**
     * 生成谷歌认证码
     * @return
     */
    @RequestMapping(value = "/sendgoogle",method = RequestMethod.GET)
    public MessageResult sendgoogle(@SessionAttribute(SESSION_ADMIN) Admin adminInfo) {
        log.info("开始进入用户id={}",adminInfo.getId());
        long current = System.currentTimeMillis();
        Admin admin = adminService.findOne(adminInfo.getId());
        log.info("查询完毕 耗时={}",System.currentTimeMillis()-current);
        if (admin == null){
            return  MessageResult.error(msService.getMessage("RE_LOGIN"));
        }
        String secret = GoogleAuthenticatorUtil.generateSecretKey();
        log.info("secret完毕 耗时={}",System.currentTimeMillis()-current);
        String qrBarcodeURL = GoogleAuthenticatorUtil.getQRBarcodeURL(adminInfo.getId().toString(),
                "siltrder", secret);
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

    @RequestMapping(value = "/yzgoogle",method = RequestMethod.POST)
    public MessageResult yzgoogle(@SessionAttribute("username")String username,
                                  @SessionAttribute("password")String password,String code,HttpServletRequest request) {
        Assert.isTrue(org.apache.commons.lang.StringUtils.isNotEmpty(username)&& org.apache.commons.lang.StringUtils.isNotEmpty(password),"会话已过期");
        long codes = Long.parseLong(code);
        Admin admin = adminService.findByUsername(username);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        boolean r = ga.check_code(admin.getGoogleKey(), codes, t);
        if(!r){
            return MessageResult.error("验证码错误");
        }
        else{
            UsernamePasswordToken token = new UsernamePasswordToken(username, password,true);
            token.setHost(getRemoteIp(request));
            //token.setRememberMe(true); //del by yangch 时间： 2018.04.26 原因：代码合并
            SecurityUtils.getSubject().login(token);
            List<Menu> list;
            //edit by yangch 时间： 2018.04.29 原因：合并
            if (admin.getRoleId()==1) {
                //if (admin.getUsername().equals("root")) {
                list = sysRoleService.toMenus(sysPermissionService.findAll(), 0L);
            } else {
                list = sysRoleService.toMenus(sysRoleService.getPermissions(admin.getRoleId()), 0L);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("permissions", list);
            map.put("admin", admin);
            return MessageResult.success("验证码成功",map);
        }
    }
    /**
     * 绑定google
     * @author lingxing
     * @time 2018.04.09 15:19
     * @param codes
     * @param adminInfo
     * @return true
     */
    @RequestMapping(value = "/googleAuth" ,method = RequestMethod.POST)
    public MessageResult googleAuth(String codes, @SessionAttribute(SESSION_ADMIN) Admin adminInfo,String secret) {
        Assert.isTrue(org.apache.commons.lang.StringUtils.isNotEmpty(codes)&& org.apache.commons.lang.StringUtils.isNotEmpty(secret),"动态验证码不能为空");
        Admin admin = adminService.findOne(adminInfo.getId());
        long code = Long.parseLong(codes);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        boolean r = ga.check_code(secret, code, t);
        if(!r){
            return MessageResult.error(msService.getMessage("AUTH_FAIL"));//
        }else{
            admin.setGoogleState(1);
            admin.setGoogleKey(secret);
            admin.setGoogleDate(new Date());
            Admin result = adminService.save(admin);
            if(result != null){
                return MessageResult.success("绑定成功");
            }else{
                return MessageResult.error("绑定失败");
            }
        }
    }
    @RequestMapping(value = "/jcgoogle" ,method = RequestMethod.POST)
    public MessageResult jcgoogle(String codes, @SessionAttribute(SESSION_ADMIN) Admin adminInfo) {
        Admin admin = adminService.findOne(adminInfo.getId());
        String GoogleKey = admin.getGoogleKey();
        long code = Long.parseLong(codes);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        // ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        boolean r = ga.check_code(GoogleKey, code, t);
        if(!r){
            return MessageResult.error(msService.getMessage("AUTH_FAIL"));
        }else{
            admin.setGoogleDate(new Date());
            admin.setVerificationType(0);
            admin.setGoogleState(0);
            admin.setGoogleKey("");
            Admin result = adminService.save(admin);
            if(result != null){
                return MessageResult.success("解绑成功");
            }else{
                return MessageResult.error("谷歌解绑失败");
            }
        }
    }
}
