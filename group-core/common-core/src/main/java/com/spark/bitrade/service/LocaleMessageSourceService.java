package com.spark.bitrade.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * @author Zhang Jinwei
 * @date 2018年01月30日
 */
@Component
@Slf4j
public class LocaleMessageSourceService {
    @Resource
    private MessageSource messageSource;

    /**
     * @param code ：对应messages配置的key.
     * @return
     */
    public String getMessage(String code) {
        return getMessage(code, null, code);
    }

    /**
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, "");
    }


    /**
     * @param code           ：对应messages配置的key.
     * @param args           : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        Locale locale = getLocale();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

    private Locale getLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            Class holderClass = Class.forName("org.springframework.web.context.request.RequestContextHolder");
            Method currentRequestAttributes = ReflectionUtils.findMethod(holderClass, "currentRequestAttributes");
            Object requestAttributes = ReflectionUtils.invokeMethod(currentRequestAttributes, null);
            Method request = ReflectionUtils.findMethod(requestAttributes.getClass(), "getRequest");
            HttpServletRequest httpServletRequest = (HttpServletRequest) ReflectionUtils.invokeMethod(request, requestAttributes);
            if (httpServletRequest != null) {
                String accept = httpServletRequest.getHeader("language");
//            String language = accept != null && accept.matches("en[-_]US") ? "en_US" : "zh_CN";
                if (accept != null) {
                    locale = StringUtils.parseLocaleString(accept);
                } else {
                    // 默认语言
                    locale = StringUtils.parseLocaleString("en_US");
                }
            } else {
                // 默认语言
                locale = StringUtils.parseLocaleString("en_US");
            }
        } catch (Exception e) {
            log.error("获取请求头的异常", e);
        }
        return locale;
    }


}
