//package com.spark.bitrade.filter;
//
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//import com.spark.bitrade.constant.SysConstant;
//import com.spark.bitrade.entity.Member;
//import com.spark.bitrade.entity.transform.AuthMember;
//import com.spark.bitrade.service.MemberService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.BeanFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.support.WebApplicationContextUtils;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
///***
// * 
// * @author yangch
// * @time 2018.07.05 17:00
// */
//
//@Component
//@Slf4j
//public class TokenFilter extends ZuulFilter {
//
//    @Override
//    public String filterType() {
//        //前置过滤器
//        return "pre";
//    }
//
//    @Override
//    public int filterOrder() {
//        //优先级，数字越大，优先级越低
//        return -5;
//    }
//
//    @Override
//    public boolean shouldFilter() {
//        //是否执行该过滤器，true代表需要过滤
//        return true;
//    }
//
//    @Override
//    public Object run() {
//        RequestContext ctx = RequestContext.getCurrentContext();
//        HttpServletRequest request = ctx.getRequest();
//
//        //*log.info("send {} request to {}", request.getMethod(), request.getRequestURL().toString());
//
//        //获取传来的参数accessToken
////        Object accessToken = request.getParameter("accessToken");
////        if(accessToken == null) {
////            log.warn("access token is empty");
////            //过滤该请求，不往下级服务去转发请求，到此结束
////            ctx.setSendZuulResponse(false);
////            ctx.setResponseStatusCode(401);
////            ctx.setResponseBody("{\"result\":\"accessToken is empty!\"}");
////            return null;
////        }
//        //如果有token，则进行路由转发
//        log.info("access token ok");
//        //这里return的值没有意义，zuul框架没有使用该返回值*//*
//
//        log.info(request.getRequestURL().toString());
//        log.info(request.getSession().getId());
//
//        HttpSession session = request.getSession();
//        AuthMember user = (AuthMember) session.getAttribute(SysConstant.SESSION_MEMBER);
//        if(user!=null){
//            if(user.getId()==71639 || (user.getId()>80000 && user.getId()<100000) ) {
//
//            } else {
//                ctx.setSendZuulResponse(false);
//                ctx.setResponseStatusCode(503);
//                HttpStatus.TOO_MANY_REQUESTS.value()
//                ctx.setResponseBody("{\"data\":\"\",\"code\":999,\"message\":\"系统正在升级...\"}");
//                ctx.getResponse().setContentType("application/json; charset=utf-8");
//                return null;
//            }
//        }
//        /*if (user == null) {
//            String token = request.getHeader("access-auth-token");
//            if(!StringUtils.isBlank(token)){
//                //解决service为null无法注入问题
//                BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
//                MemberService memberService = (MemberService) factory.getBean("memberService");
//                Member member = memberService.loginWithToken(token, request.getRemoteAddr(), "");
//                if (member != null) {
//                    session.setAttribute(SysConstant.SESSION_MEMBER, AuthMember.toAuthMember(member));
//                    return null;
//                }
//            }
//            log.info("token:{}",token);
//        }*/
//
//        return null;
//    }
//}
