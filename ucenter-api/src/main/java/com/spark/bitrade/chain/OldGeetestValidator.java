package com.spark.bitrade.chain;

import com.spark.bitrade.constant.ValidateCodeType;
import com.spark.bitrade.system.GeetestLib;
import com.spark.bitrade.system.HttpServletUtil;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.06 10:14  
 */
@Slf4j
public class OldGeetestValidator extends AbstractValidateCode {

    /**
     * 老的极验证SDK
     */
    protected GeetestLib gtSdk;

    public OldGeetestValidator(ValidateCodeType type) {
        this.validateCodeType = type;
    }



    @Override
    protected void validate() {
        this.gtSdk = SpringContextUtil.getBean(GeetestLib.class);
        log.info("=============================极验证开始=======================================");
        HttpServletRequest request = HttpServletUtil.getRequest();
        String ip = request.getRemoteAddr();
        String challenge = request.getParameter(GeetestLib.fn_geetest_challenge);
        String validate = request.getParameter(GeetestLib.fn_geetest_validate);
        String seccode = request.getParameter(GeetestLib.fn_geetest_seccode);
        log.info("challenge:{},validate:{},seccode:{}",challenge,validate,seccode);
        Integer gt_server_status_code = (Integer) request.getSession().getAttribute(gtSdk.gtServerStatusSessionKey);
        //从session中获取userid
        String userid = (String) request.getSession().getAttribute("userid");
        //自定义参数,可选择添加
        HashMap<String, String> param = new HashMap<String, String>(8);
        //网站用户id
        param.put("user_id", userid);
        if (ip.indexOf(",") != -1) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        if (StringUtils.isEmpty(ip)) {
            //随便给定一个ip（貌似没有实际意义）
            ip = "127.0.0.1";
        }
        //传输用户请求验证时所携带的IP
        param.put("ip_address", ip);

        int gtResult = 0;

        if (null != gt_server_status_code && gt_server_status_code == 1) {
            //gt-server正常，向gt-server进行二次验证
            gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, param);
        } else {
            // gt-server非正常情况下，进行failback模式验证
            log.info("failback:use your own server captcha validate");
            gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
        }
        Assert.isTrue(gtResult == 1, localeMessageSourceService.getMessage("GEETEST_FAIL"));
        // 记录下请求内容
        log.info("请求路径 : " + request.getRequestURL().toString());
        log.info("请求方式 : " + request.getMethod());
        log.info("请求IP  : " + request.getRemoteAddr());
        log.info("=============================极验证验证成功=======================================");
    }
}
