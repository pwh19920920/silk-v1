package com.spark.bitrade.aspect;

import com.spark.bitrade.chain.ManMacValidateService;
import com.spark.bitrade.system.HttpServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.02 13:38  
 */
@Aspect
@Component
@Slf4j
public class ValidateAspect {

    @Autowired
    private ManMacValidateService manMacValidateService;

    @Around("execution(* com.spark.bitrade.controller.SmsController.*(..))" +
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
    public Object validateCode(ProceedingJoinPoint pjp) throws Throwable {
        log.info("methodName:{}",pjp.getSignature().getName());
        //过滤silkpay
        if("55580235".equals(HttpServletUtil.getRequest().getHeader("thirdMark"))){
            return pjp.proceed();
        }
        manMacValidateService.validate();

        return pjp.proceed();

    }


}
