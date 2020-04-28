package com.spark.bitrade.system;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.02 09:56  
 */
public class HttpServletUtil {

    private HttpServletUtil(){

    }

    public static HttpServletRequest getRequest() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request;
    }

    public static HttpServletResponse getResponse(){
        HttpServletResponse response =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        return response;
    }


    public static HttpSession getSession(){
        return getRequest().getSession();
    }



}
