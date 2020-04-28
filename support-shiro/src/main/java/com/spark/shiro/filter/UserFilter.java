package com.spark.shiro.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.pac4j.core.context.Pac4jConstants;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserFilter extends org.apache.shiro.web.filter.authc.UserFilter {

    @Override
    protected void saveRequest(ServletRequest request) {
        // 还是先执行着shiro自己的方法
        super.saveRequest(request);
        Session session = SecurityUtils.getSubject().getSession();
        session.setAttribute(Pac4jConstants.REQUESTED_URL, ((HttpServletRequest)request).getRequestURI());
    }
}
