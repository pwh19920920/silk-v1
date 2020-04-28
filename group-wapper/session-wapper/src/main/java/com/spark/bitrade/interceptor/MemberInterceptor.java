package com.spark.bitrade.interceptor;


import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LocaleMessageSourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Zhang Jinwei
 * @date 2018年01月11日
 */
@Slf4j
public class MemberInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        log.info(request.getRequestURL().toString());
        BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        LocaleMessageSourceService msService = (LocaleMessageSourceService) factory.getBean("localeMessageSourceService");
        AuthMember user = (AuthMember) session.getAttribute(SysConstant.SESSION_MEMBER);
        //清空已退出的session
        if(user != null){
            //改为用redisService来处理缓存数据
            RedisTemplate redisTemplate = (RedisTemplate) factory.getBean("redisTemplate");
            //获取用户登录状态
            //add by zyj 2019.1.24 : 加入来源
            String thirdMark = request.getHeader("thirdMark");
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String loginState;
            if (thirdMark!=null){
                loginState = (String)valueOperations.get(SysConstant.MEMBER_LOGOUT + user.getId() +":"+ thirdMark );
            }else {
                loginState = (String)valueOperations.get(SysConstant.MEMBER_LOGOUT + user.getId());
            }
            if("1".equals(loginState)){
                user = null;
                request.getSession().removeAttribute(SysConstant.SESSION_MEMBER);
            }
        }
        if (user != null) {
            if (user.getStatus() != CommonStatus.NORMAL) {
                ajaxReturn(response, 4000, msService.getMessage("UNAUTHORIZED_LOCKED"));
            }
            if(!user.verifyLoginRequire()) {
                ajaxReturn(response, 4000, msService.getMessage("UNAUTHORIZED_VERIFY"));
            }
            return user.verifyLoginRequire();
        } else {
            String token = request.getHeader("access-auth-token");
            //判断access-auth-token是否为空
            if(com.mysql.jdbc.StringUtils.isNullOrEmpty(token)) {
                //如果access-auth-token为空，判断wallet-auth-token是否为空
                token = request.getHeader("wallet-auth-token");
                log.info("wallet-auth-token:{}",token);
                if(com.mysql.jdbc.StringUtils.isNullOrEmpty(token)){
                    //如果wallet-auth-token为空，则返回权限错误
                    ajaxReturn(response, 4000, msService.getMessage("UNAUTHORIZED"));
                    return false;
                }else{
                    //判断绑定信息是否为空
                    if (MemberJwtCheck.handleJwt(request)) {
                        return true;
                    } else {
                        //绑定信息为空，返回权限异常
                        ajaxReturn(response, 4000, msService.getMessage("UNAUTHORIZED"));
                        return false;
                    }
                }
            } else {
                //如果access-auth-token不为空
                log.info("token:{}", token);
                if (MemberJwtCheck.handleJwt(request)) {
                    return true;
                } else {
                    ajaxReturn(response, 4000, msService.getMessage("UNAUTHORIZED"));
                    return false;
                }
            }
        }
    }


    public void ajaxReturn(HttpServletResponse response, int code, String msg) throws IOException, JSONException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("message", msg);
        out.print(json.toString());
        out.flush();
        out.close();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }
}
