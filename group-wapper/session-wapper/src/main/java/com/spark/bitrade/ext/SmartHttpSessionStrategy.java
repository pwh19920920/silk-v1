package com.spark.bitrade.ext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.session.Session;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class SmartHttpSessionStrategy implements HttpSessionStrategy {
    private CookieHttpSessionStrategy browser;
    private HeaderHttpSessionStrategy api;
    private HeaderHttpSessionStrategy access;
    private String tokenName = "x-auth-token";

    public SmartHttpSessionStrategy(CookieHttpSessionStrategy browser, HeaderHttpSessionStrategy api, HeaderHttpSessionStrategy access) {
        this.browser = browser;
        this.api = api;
        this.access = access;
    }

    @Override
    public String getRequestedSessionId(HttpServletRequest request) {
        String paramToken = request.getParameter(tokenName);
        if (StringUtils.isNotEmpty(paramToken)) {
            return paramToken;
        }
        String sessionId = request.getHeader("session-token");
        if (StringUtils.isEmpty(sessionId)) {
            return getStrategy(request).getRequestedSessionId(request);
        } else {
            return sessionId;
        }
    }

    @Override
    public void onNewSession(Session session, HttpServletRequest request, HttpServletResponse response) {
        String sessionToken = response.getHeader("access-auth-token");
        if (StringUtils.isBlank(sessionToken)) {
            getStrategy(request).onNewSession(session, request, response);
        }
    }

    @Override
    public void onInvalidateSession(HttpServletRequest request, HttpServletResponse response) {
        getStrategy(request).onInvalidateSession(request, response);
    }

    private HttpSessionStrategy getStrategy(HttpServletRequest request) {
        String authType = request.getHeader("x-auth-token");
        String accessType = request.getHeader("access-auth-token");
        String sessionToken = request.getHeader("session-token");
        if (accessType != null && sessionToken != null) {
            return this.access;
        } else if (authType != null) {
            return this.api;
        } else {
            return this.browser;
        }
    }
}