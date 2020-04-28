package com.spark.bitrade.annotation.handler;

import com.spark.bitrade.annotation.definition.ApiRequestLimit;
import com.spark.bitrade.exception.ApiRequestLimitException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * <p>api请求限制实现</p>
 * @author tian.bo
 * @date 2018/12/24.
 */
@Aspect
@Slf4j
@Component
public class ApiRequestLimitAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Pointcut("execution(* com.spark.bitrade.controller.*.*(..)) && @annotation(com.spark.bitrade.annotation.definition.ApiRequestLimit)")
    public void before(){
    }

    //@Before("within(@org.springframework.stereotype.Controller *) && @annotation(apiRequestLimit)")
    @Before("before()")
    public void apiRequestLimitHandler(JoinPoint joinPoint) throws ApiRequestLimitException {
        try {
            Object[] args = joinPoint.getArgs();
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String url = request.getRequestURL().toString();
            String ip = request.getRemoteAddr();
            String key = "req_limit_".concat(url).concat("_").concat(ip);
            ApiRequestLimit apiRequestLimit = this.getAnnotation(joinPoint);
            boolean bool = checkRequestCountWithRedis(apiRequestLimit, key);
            if(!bool){
                log.warn("requestLimited,【用户IP:{}】，【访问地址:{}】超出限定次数【{}】次",ip,url,apiRequestLimit.count());
                throw new ApiRequestLimitException("errorMsg","请求过于频繁,超出限制!");
            }
        } catch (ApiRequestLimitException e){
            throw e;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取注解
     * @param joinPoint
     * @return
     */
    private ApiRequestLimit getAnnotation(JoinPoint joinPoint){
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(ApiRequestLimit.class);
        }
        return null;
    }

    /**
     * 请求次数检查
     * @param apiRequestLimit
     * @param key
     * @return
     */
    private boolean checkRequestCountWithRedis(ApiRequestLimit apiRequestLimit, String key){
        long count = redisTemplate.opsForValue().increment(key, 1);
        if(count == 1){
            redisTemplate.expire(key, apiRequestLimit.time(), TimeUnit.MILLISECONDS);
        }
        if(count > apiRequestLimit.count()){
            return false;
        }
        return true;
    }
}
