package com.spark.bitrade.interceptor;

import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.service.AdminService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;


@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {

        BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        AdminService adminService = (AdminService) factory.getBean("adminService");
        System.out.println(request.getContextPath());
        Subject currentUser = SecurityUtils.getSubject();
        //判断用户是通过记住我功能自动登录,此时session失效
        if(!currentUser.isAuthenticated() && currentUser.isRemembered()){
            try {
                Admin admin = adminService.findByUsername(currentUser.getPrincipals().toString());
                //对密码进行加密后验证
                UsernamePasswordToken token = new UsernamePasswordToken(admin.getUsername(), admin.getPassword(),currentUser.isRemembered());
                //把当前用户放入session
                currentUser.login(token);
                Session session = currentUser.getSession();
                session.setAttribute(SysConstant.SESSION_ADMIN,admin);
                //设置会话的过期时间--ms,默认是30分钟，设置负数表示永不过期
                session.setTimeout(60*1000L); //edit by yangch 时间： 2018.04.26 原因：合并最新代码
            }catch (Exception e){
                //自动登录失败,跳转到登录页面
                //response.sendRedirect(request.getContextPath()+"/system/employee/sign/in");
                ajaxReturn(response, 4000, "unauthorized");
                return false;
            }
            if(!currentUser.isAuthenticated()){
                //自动登录失败,跳转到登录页面
                ajaxReturn(response, 4000, "unauthorized");
                return false;
            }
        }
        return true;
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

    public String getRemoteIp(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
            return request.getHeader("X-Real-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
            return request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        }
        return request.getRemoteAddr();
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
       /* BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(httpServletRequest.getServletContext());
        SysRoleService sysRoleService = (SysRoleService) factory.getBean("sysRoleService");
        SysPermissionService sysPermissionService = (SysPermissionService) factory.getBean("sysPermissionService");
        HttpSession session = httpServletRequest.getSession() ;
        Admin admin = (Admin) session.getAttribute(SysConstant.SESSION_ADMIN);
        List<Menu> list;
        if (admin.getUsername().equals("root")) {
            list = sysRoleService.toMenus(sysPermissionService.findAll(), 0L);
        } else {
            list = sysRoleService.toMenus(sysRoleService.getPermissions(admin.getRoleId()), 0L);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("permissions", list);
        map.put("admin", admin);
        httpServletResponse.getWriter().print(com.alibaba.fastjson.JSONObject.toJSONString(map));*/
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
