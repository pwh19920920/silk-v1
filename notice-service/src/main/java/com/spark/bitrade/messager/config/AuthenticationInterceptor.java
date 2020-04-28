package com.spark.bitrade.messager.config;

import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.messager.annotation.UserLoginToken;
import com.spark.bitrade.messager.service.IMemberInfoService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

import static com.spark.bitrade.messager.constant.HttpConstant.LOGINED_MEMEBER;

/**
 * @author ww
 * @time 2019.10.06 10:46
 */
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    IMemberInfoService memberInfoService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        //String token = httpServletRequest.getHeader("token");// 从 http 请求头中取出 token
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();
        //检查是否有passtoken注释，有则跳过认证
//        if (method.isAnnotationPresent(PassToken.class)) {
//            PassToken passToken = method.getAnnotation(PassToken.class);
//            if (passToken.required()) {
//                return true;
//            }
//        }
        //检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {

                AuthMember authMember = memberInfoService.getLoginMemberByToken(httpServletRequest.getHeader("x-auth-token"), httpServletRequest.getHeader("access-auth-token"));
                if (authMember == null) {
                    httpServletRequest.setCharacterEncoding("UTF-8");
                    httpServletResponse.setCharacterEncoding("UTF-8");
                    httpServletResponse.setContentType("text/html;charset=utf-8");
                    //httpServletResponse.setContentType("text/html;charset=utf-8");
                    httpServletResponse.getWriter().write(MessageResult.error("请登录").toString());
                    return false;
                }
                httpServletRequest.setAttribute(LOGINED_MEMEBER,authMember);
                return true;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}