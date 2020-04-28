package com.spark.bitrade.interceptor;

import com.spark.bitrade.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>钱包支付接口拦截器</p>
 * @author tian.bo
 * @date 2019/1/8.
 */
@Slf4j
public class WalletPayHttpRequestHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        return this.preProcessor(request,response,o);
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {

    }

    public boolean preProcessor(HttpServletRequest request, HttpServletResponse response, Object o){
        //请求方法
        String method = request.getMethod();
        /**
         *  请求头校验
         */
        String userAgent = request.getHeader("User-Agent");
        String contentType = request.getHeader("Content-Type");
        String token = request.getHeader("wallet-auth-token");
        String cmd = request.getHeader("cmd");

        log.info("userAgent={}",userAgent);

        if(StringUtils.isEmpty(token)){
            log.error("bad-request, token is null");
            throw new ApiException("bad-request","bad-request");
        }
        if(StringUtils.isEmpty(cmd)){
            log.error("bad-request, cmd is null");
            throw new ApiException("bad-request","bad-request");
        }
        if("POST".equals(StringUtils.upperCase(method))){
            if(!StringUtils.equals("application/json;charset=utf-8",StringUtils.lowerCase(contentType))){
                log.error("bad-request, contentType={}",contentType);
                throw new ApiException("bad-request","bad-request");
            }
        }else{
            if(!StringUtils.equals("application/x-www-form-urlencoded",contentType)){
                log.error("bad-request, contentType={}",contentType);
                throw new ApiException("bad-request","bad-request");
            }
        }
        /*if(!StringUtils.equals("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36",userAgent)){
            log.error("bad-request, userAgent={}",userAgent);
            throw new ApiException("bad-request","bad-request");
        }*/
        return true;
    }
}
