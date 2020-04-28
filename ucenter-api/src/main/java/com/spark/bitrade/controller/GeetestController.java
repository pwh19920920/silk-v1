package com.spark.bitrade.controller;

import com.spark.bitrade.config.MultiMailConfiguration;
import com.spark.bitrade.system.GeetestLib;
import com.spark.bitrade.util.GeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Zhang Jinwei
 * @date 2018年02月23日
 */
@RestController
@Slf4j
public class GeetestController extends BaseController {
    @Autowired
    MultiMailConfiguration multiMailConfiguration;

    @Autowired
    private GeetestLib gtSdk;

    @Value("${geetest.enabled:true}")
    private boolean geetestEnabled; //极验证开关

    @RequestMapping(value = "/start/captcha")
    public String startCaptcha(HttpServletRequest request) {
        try {
            String resStr = "{}";
            String userid = "spark";
            //自定义参数,可选择添加
            HashMap<String, String> param = new HashMap<String, String>();
            String ip = getRemoteIp(request);
            //String ip = "172.31.35.48, 172.31.34.203";
            //edit by yangch 时间： 2018.07.12 原因：多ip请求会报错，此处的ip貌似没有实际的意义
            if (ip.indexOf(",") != -1) {
                ip = ip.substring(0, ip.indexOf(","));
            }
            if (StringUtils.isEmpty(ip)) {
                //随便给定一个ip（貌似没有实际意义）
                ip = "127.0.0.1";
            }

            //网站用户id
            param.put("user_id", userid);
            //web:电脑上的浏览器；h5:手机上的浏览器，包括移动应用内完全内置的web_view；native：通过原生SDK植入APP应用的方式
            param.put("client_type", "web");
            //传输用户请求验证时所携带的IP
            param.put("ip_address", ip);
            //进行验证预处理
            int gtServerStatus = gtSdk.preProcess(param, geetestEnabled);
            //将服务器状态设置到session中
            request.getSession().setAttribute(gtSdk.gtServerStatusSessionKey, gtServerStatus);
            //System.out.println("----------session1:"+request.getSession());
            //System.out.println("----------sessionId1:"+request.getSession().getId());
            //将userid设置到session中
            request.getSession().setAttribute("userid", userid);
            resStr = gtSdk.getResponseStr();
            return resStr;
        } catch (Exception ex) {
            log.error("获取极验证信息出错！", ex);
        }

        //关闭极验证
        //if (!geetestEnabled) {
        String random = GeneratorUtil.getNonceString(10).toLowerCase();
        return "{\"success\":1,\"enabled\":false,\"challenge\":\"86da28d7471d84d0e4f5da" + random + "\",\"gt\":\"5d6c528b8fb327ebc72a15" + random + "\"}";
        //}
    }
}
