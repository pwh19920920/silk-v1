package com.spark.bitrade.filter;

//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;

/***
 * 
 * @author yangch
 * @time 2018.07.07 10:15
 */
//@Slf4j
//@Component
//public class OptionRequestFilter extends ZuulFilter {
//    @Override
//    public Object run() {
//        RequestContext ctx = RequestContext.getCurrentContext();
//        HttpServletRequest request = ctx.getRequest();
//        HttpSession session = request.getSession();
//        String url = request.getRequestURL().toString();
//        log.info("{} OptionRequestFilter request to {}", request.getMethod(), url);
//        System.out.println("OptionRequestFilter:session"+request.getRequestedSessionId()+",sessionId="+request.getSession().getId()+",token="+ctx.getResponse().getHeader("x-auth-token"));
//
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            return null;
//        }
////        String username = (String) session.getAttribute(UserSessionMap.USERNAME_KEY);
////        if (StringUtils.isBlank(username)) {
////            ctx.setSendZuulResponse(false);
////            ctx.setResponseStatusCode(401);
////            ctx.setResponseBody("{\"status\":401,\"msg\":\"user is not login !\"}");
////            ctx.getResponse().setContentType("text/html;charset=UTF-8");
////        }
////        ctx.addZuulRequestHeader("username", username);
//        return null;
//    }
//
//    @Override
//    public boolean shouldFilter() {
//        return true;// 是否执行该过滤器，此处为true，说明需要过滤
//    }
//
//    @Override
//    public int filterOrder() {
//        return -3;// 优先级为0，数字越大，优先级越低
//    }
//
//    @Override
//    public String filterType() {
//        return "pre";// 前置过滤器
//    }
//}
