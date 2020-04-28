package com.spark.shiro.filter;

import io.buji.pac4j.filter.CallbackFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class DefaultCallbackFilter extends CallbackFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        super.doFilter(servletRequest,servletResponse,filterChain);
        filterChain.doFilter(servletRequest,servletResponse);
    }
}
