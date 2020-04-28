package com.spark.bitrade.interceptor;

import com.spark.bitrade.config.VipConfig;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.SignUtil;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/***
 * 授权接口接入授权校验
 * @author yangch
 * @time 2018.10.07 17:55
 */
@Slf4j
@Component
public class MemberVipInterceptor extends MemberInterceptor {
    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private VipConfig vipConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        //接口签名信息
        String signMsg = request.getHeader("access-auth-sign");
        if(null == signMsg) {
            if(null == msService){
                msService = SpringContextUtil.getBean(LocaleMessageSourceService.class);
            }

            ajaxReturn(response, 4000, msService.getMessage("SIGN_NULL"));
            return false;
        }

        HttpSession session = request.getSession();
        log.info(request.getRequestURL().toString());
        //System.out.println("core::rssionid="+request.getRequestedSessionId()+",sessionId="+request.getSession().getId()+",token="+response.getHeader("x-auth-token"));
        AuthMember user = (AuthMember) session.getAttribute(SysConstant.SESSION_MEMBER);
        if (user != null) {
            if (user.getStatus() != CommonStatus.NORMAL) {
                ajaxReturn(response, 4000, "unauthorized");
            }
            if(!user.verifyLoginRequire()) {
                ajaxReturn(response, 4000, "unauthorized");
            }

            //edit by yangch 时间： 2018.10.07 原因： 校验接口签名校验
            //return user.verifyLoginRequire();
            //校验接口签名校验
            if(user.verifyLoginRequire()) {
                return checkSign(String.valueOf(user.getId()), signMsg, response);
            } else {
                return false;
            }

        } else {
            String token = request.getHeader("access-auth-token");
            if(StringUtils.isEmpty(token)){
                ajaxReturn(response, 4000, "unauthorized");
                return false;
            }
            log.info("token:{}",token);

            //解决service为null无法注入问题
            BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            MemberService memberService = (MemberService) factory.getBean("memberService");
            //MemberEvent memberEvent = (MemberEvent) factory.getBean("memberEvent");
            Member member = memberService.loginWithToken(token, request.getRemoteAddr(), "");
            if (member != null) {
                //del by yangch 时间： 2018.06.24 原因：登录不更新token过期时间
                /*Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, 24 * 7);
                member.setTokenExpireTime(calendar.getTime());
                memberService.save(member);
                memberEvent.onLoginSuccess(member, request.getRemoteAddr());*/
                //session.setAttribute(token, AuthMember.toAuthMember(member));
                session.setAttribute(SysConstant.SESSION_MEMBER,AuthMember.toAuthMember(member));

                //edit by yangch 时间： 2018.10.07 原因： 校验接口签名校验
                //return true;
                //校验接口签名校验
                return checkSign(String.valueOf(member.getId()), signMsg, response);
            } else {
                ajaxReturn(response, 4000, "unauthorized");
                return false;
            }

        }
    }

    private boolean checkSign(String uid, String signMsg, HttpServletResponse response) throws Exception {
        //String serkey = uid; //加密密钥（为用户ID）
        if(null == vipConfig) {
            vipConfig = SpringContextUtil.getBean(VipConfig.class);
        }
        //准入授权码
        String authKey = vipConfig.getAccessAuthKey(uid);
        if(null == authKey) {
            if(null == msService){
                msService = SpringContextUtil.getBean(LocaleMessageSourceService.class);
            }

            ajaxReturn(response, 4000, msService.getMessage("SIGN_FAILED4"));
            return false;
        }

        //获取 uid对应的 准入授权码
        //接口签名校验
        int flag = SignUtil.checkSign(authKey, uid, signMsg);
        if(flag == 0 ) {
            return true;
        } else {
            if(null == msService){
                msService = SpringContextUtil.getBean(LocaleMessageSourceService.class);
            }

            if(flag ==1){
                ajaxReturn(response, 4000, msService.getMessage("SIGN_FAILED1"));
            } else if(flag ==2) {
                ajaxReturn(response, 4000, msService.getMessage("SIGN_FAILED2"));
            } else if(flag ==3) {
                ajaxReturn(response, 4000, msService.getMessage("SIGN_FAILED3"));
            } else if(flag ==4) {
                ajaxReturn(response, 4000, msService.getMessage("SIGN_FAILED5"));
            }

            return false;
        }
    }
}
