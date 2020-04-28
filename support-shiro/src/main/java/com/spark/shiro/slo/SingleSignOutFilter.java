package com.spark.shiro.slo;


import org.apache.shiro.web.servlet.AdviceFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class SingleSignOutFilter extends AdviceFilter {
    private static final SingleSignOutHandler HANDLER = new SingleSignOutHandler();
    public SingleSignOutFilter() {
        HANDLER.init();
    }
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest hrequest = (HttpServletRequest)request;
        HttpServletResponse hresponse = (HttpServletResponse)response;
        HANDLER.process(hrequest, hresponse);
        return  false;
    }
}

