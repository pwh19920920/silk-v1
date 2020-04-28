package com.spark.bitrade.aspect;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.chain.ManMacValidateService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.system.HttpServletUtil;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 *  
 *   登录拦截器 拦截登录时密码错误次数 
 *  @author liaoqinghui  
 *  @time 2019.08.12 14:14  
 */
@Aspect
@Slf4j
@Component
public class LoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private ManMacValidateService manMacValidateService;

    private static final String FORBIDDEN=":forbidden";

    @Around("execution(* com.spark.bitrade.controller.LoginController.login(..))" +
            "||execution(* com.spark.bitrade.controller.LoginController.appLogin(..))" +
            "||execution(* com.spark.bitrade.controller.pay.MemberController.bindWallet(..))")
    public Object validateCode(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = HttpServletUtil.getRequest();
        String thirdMark = request.getHeader("thirdMark");
        //过滤silkpay
        if("55580235".equals(thirdMark)){
            return pjp.proceed();
        }

        Assert.isTrue(StringUtils.hasText(thirdMark), msService.getMessage("THIRD_MARK_CANT_NULL"));
        ValueOperations<String, Integer> op = redisTemplate.opsForValue();
        String username = request.getParameter("username");
        String key = "errorAccount:" + username + ":count";
        //判断密码错误次数
        Integer errorCount = op.get(key);
        errorCount = errorCount == null ? 0 : errorCount;
        //密码错误五次
        //禁止账户操作2小时
        Integer integer = op.get(key + FORBIDDEN);
        if (integer != null) {
            return MessageResult.error(4009, msService.getMessage("ACCOUNT_IS_FORBIDDEN_TWO"));
        }
        //密码错误三次
        if (errorCount >= 3) {
            //错误三次进行人机验证
            manMacValidateService.validate();
        }

        //获取返回结果
        Object proceed = pjp.proceed();
        String resultStr = JSON.toJSONString(proceed);
        MessageResult respResult = JSON.parseObject(resultStr, MessageResult.class);
        String message = respResult.getMessage();
        //统计账号密码错误次数
        if ("账号或密码错误".equals(message) || "Account or password error".equals(message)) {
            op.set(key, errorCount + 1, 5L, TimeUnit.MINUTES);
            Integer lastCount = op.get(key);
            if (lastCount >= 5) {
                //设置禁用账户KEY
                op.set(key + FORBIDDEN, 1, 2L, TimeUnit.HOURS);
                return MessageResult.error(4009, msService.getMessage("ACCOUNT_IS_FORBIDDEN_TWO"));
            }
            if (lastCount >= 3) {
                return MessageResult.error(4008, msService.getMessage("PASSWORD_INCORRECT_THREE"));
            }
        }
        //清除错误次数
        if (respResult.getCode()==0){
            redisTemplate.delete(key);
        }
        log.info("loginResult:{}", JSON.toJSONString(proceed));
        return proceed;
    }
}
