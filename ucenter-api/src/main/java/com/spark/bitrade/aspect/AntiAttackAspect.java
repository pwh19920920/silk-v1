package com.spark.bitrade.aspect;

import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.service.LocaleMessageSourceService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 登录之后发送邮件或者短信频率最快也只能一分钟一次
 *
 * @author Zhang Jinwei
 * @date 2018年04月03日
 */
@Aspect
@Component
@Slf4j
public class AntiAttackAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(* com.spark.bitrade.controller.SmsController.*(..))" +
            "&&!execution(* com.spark.bitrade.controller.SmsController.dccLoginCodeCheck(..))" +
            "&&!execution(* com.spark.bitrade.controller.SmsController.loginCodeCheck(..))" +
            "&&!execution(* com.spark.bitrade.controller.SmsController.sendSmsCode(..))" +
            "||execution(* com.spark.bitrade.controller.RegisterController.sendBindEmail(..))" +
            "||execution(* com.spark.bitrade.controller.RegisterController.sendAddAddress(..))" +
            "||execution(* com.spark.bitrade.controller.RegisterController.sendResetPasswordCode(..))" +
            "||execution(* com.spark.bitrade.controller.RegisterController.sendRegEmailCheckCode(..))" +
            "||execution(* com.spark.bitrade.controller.RegisterController.registerByEmail(..))" +
            "||execution(* com.spark.bitrade.controller.ApproveController.sendUpdatePwdEmailCode(..))" +
            "||execution(* com.spark.bitrade.controller.ApproveController.sendChangeEmail(..))")
    public void antiAttack() {
    }

    @Before("antiAttack()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        log.info("❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤");
        check(joinPoint);
    }

    public void check(JoinPoint joinPoint) throws Exception {
        startTime.set(System.currentTimeMillis());
        String methodName = joinPoint.getSignature().getName();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = SysConstant.ANTI_ATTACK_ + request.getSession().getId()+methodName;
        log.info("防攻击KEY:{}",key);
        Object code = valueOperations.get(key);
        if (code != null) {
            throw new IllegalArgumentException(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
        }
    }

    @AfterReturning(pointcut = "antiAttack()")
    public void doAfterReturning(JoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String key = SysConstant.ANTI_ATTACK_ + request.getSession().getId()+methodName;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, "send sms all too often", 1, TimeUnit.MINUTES);
        log.info("处理耗时：" + (System.currentTimeMillis() - startTime.get()) + "ms");
        log.info("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
        //add by tansitao 时间： 2018/11/22 原因：调用方法介绍remove掉线程
        startTime.remove();
    }

    /**
     * 接口异常的处理
     * @author tansitao
     * @time 2018/11/22 16:03 
     */
    @AfterThrowing(pointcut = "antiAttack()")
    public void afterThrowing() {
        log.info("=====================================处理耗时："  + (System.currentTimeMillis() - startTime.get()) + "ms");
        startTime.remove();
    }
}
