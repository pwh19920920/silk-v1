package com.spark.bitrade.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.spark.bitrade.config.TesterPropertis;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/***
 * 升级过滤器
 * @author yangch
 * @time 2018.07.05 17:00
 */

@Component
@Slf4j
public class UpdatingFilter extends ZuulFilter {
    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private TesterPropertis testerPropertis;

    //系统是否正在升级
    private static Boolean isUpdating = false;

    //@Value("'${user.tester.whitelist}'.split(',')")
    //@Value("${user.tester.whitelist}")
    //private List<String> whitelist;

    //判断用户是否在白名单中
    private boolean inWhitelist(Long uid){
        List<Integer> whitelist = testerPropertis.getWhitelist();
        if(null == whitelist) {
            return false;
        }

        //return whitelist.contains(uid);
        for (Integer u:whitelist ) {
            if(u.longValue() == uid.longValue()){
                return true;
            }
        }

        return false;
    }

    public void setIsUpdating(boolean flag){
        isUpdating = flag;
    }


    @Override
    public String filterType() {
        //前置过滤器
        return "pre";
    }

    @Override
    public int filterOrder() {
        //优先级，数字越大，优先级越低
        return -5;
    }

    @Override
    public boolean shouldFilter() {
        //是否执行该过滤器，true代表需要过滤
        return isUpdating;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        HttpSession session = request.getSession();
        AuthMember user = (AuthMember) session.getAttribute(SysConstant.SESSION_MEMBER);
        if(user == null){
            String token = request.getHeader("x-auth-token"); //pc端token
            if(StringUtils.isBlank(token)) {
                token = request.getHeader("access-auth-token"); //app端token
                log.info("app端登录，token={}", token);

                if(!StringUtils.isBlank(token)) {
                    //获取用户信息
                    BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
                    MemberService memberService = (MemberService) factory.getBean("memberService");
                    Member member = memberService.loginWithToken(token, request.getRemoteAddr(), "");
                    if (member != null) {
                        user = AuthMember.toAuthMember(member);
                        session.setAttribute(SysConstant.SESSION_MEMBER, user);
                    }
                }
            }
        }

        if (user != null) {
            if (user.getId() > 80000 && user.getId() < 100000) {
                log.info("预留用户：uid={}", user.getId());
            } else if( inWhitelist(user.getId()) ){
                log.info("白名单用户：uid={}", user.getId());
            } else {
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(HttpStatus.FORBIDDEN.value()); //请求被禁止
                ctx.setResponseBody("{\"data\":\"\",\"code\":999,\"message\":\""+msService.getMessage("SYSTEM_IS_UPDATING")+"\"}");
                ctx.getResponse().setContentType("application/json; charset=utf-8");
                return null;
            }
        }

        return null;
    }

}
