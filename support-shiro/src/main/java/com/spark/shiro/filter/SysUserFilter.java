package com.spark.shiro.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.shiro.config.ShiroConfig;
import com.spark.shiro.entity.ShiroMember;
import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.shiro.service.UserPermissionService;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class SysUserFilter extends PathMatchingFilter {

    private UserPermissionService userPermissionService;
    private ObjectMapper objectMapper;
    private Logger logger = LoggerFactory.getLogger(getClass());
    public SysUserFilter(UserPermissionService userPermissionService,
                         ObjectMapper objectMapper){
        this.userPermissionService = userPermissionService;
        this.objectMapper = objectMapper;
    }
    @Override
    protected boolean onPreHandle(ServletRequest request,
                                  ServletResponse response,
                                  Object mappedValue)
            throws Exception {
        Subject currentUser = SecurityUtils.getSubject();
        String name = ((Pac4jPrincipal) currentUser.getPrincipal()).getProfile().getId();
        if (name == null)
            return true;
        Session session = currentUser.getSession();
        ShiroMember user = (ShiroMember) session.getAttribute(ShiroConfig.SESSION_LOGIN_USER);
        if (user == null || user.getUsername() == null || !user.getUsername().equals(name)) {
            try {
                String userJson = userPermissionService.findByLoginNo(name);
                session.setAttribute(ShiroConfig.SESSION_LOGIN_USER,
                        objectMapper.readValue(userJson, ShiroMember.class));
            } catch (Exception e) {
                logger.error("权限设置失败", e);
                throw new RuntimeException("获取用户登陆信息失败");
            }
        }
        return true;
    }
}
