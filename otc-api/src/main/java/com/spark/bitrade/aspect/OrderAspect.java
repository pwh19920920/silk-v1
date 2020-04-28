package com.spark.bitrade.aspect;

import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.AdvertiseService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.OrderService;
import com.spark.bitrade.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static org.springframework.util.Assert.isTrue;

/**
 * 处理订单的所有操作aop，防止用户重复操作
 * @author tansitao
 * @time 2018/10/31 11:17 
 */
@Aspect
@Component
@Slf4j
public class OrderAspect {
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private RedisService redisService;
    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.spark.bitrade.controller.OrderController.cancelOrder(..))"+
            "||execution(public * com.spark.bitrade.controller.OrderController.buy(..))"+
            "||execution(public * com.spark.bitrade.controller.OrderController.sell(..))"+
            "||execution(public * com.spark.bitrade.controller.OrderController.appeal(..))" +
            "||execution(public * com.spark.bitrade.controller.OrderController.confirmRelease(..))")
    public void dealOrder() {
    }

    @Before("dealOrder()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        log.info("=====================================订单监控======================================================");
        check(joinPoint);
    }

    public void check(JoinPoint joinPoint) throws Exception {
        startTime.set(System.currentTimeMillis());

        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        log.info("请求方法 : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("请求参数 : " + Arrays.toString(joinPoint.getArgs()));
        AuthMember authMember = (AuthMember) request.getSession().getAttribute(SysConstant.SESSION_MEMBER);
        //从redis中获取用户是否有处理中的订单，如果有，则不允许下单
        String havDealingOrder = (String) redisService.get(SysConstant.C2C_DEALING_ORDER + authMember.getId());
        isTrue(StringUtils.isEmpty(havDealingOrder) , msService.getMessage("NOT_REPEAT_ORDER"));
        redisService.expireSet(SysConstant.C2C_DEALING_ORDER + authMember.getId(), "1", 1);
    }

    @AfterReturning(pointcut = "dealOrder()")
    public void doAfterReturning() throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        AuthMember authMember = (AuthMember) request.getSession().getAttribute(SysConstant.SESSION_MEMBER);
        //订单处理结束，清除缓存
        redisService.remove(SysConstant.C2C_DEALING_ORDER + authMember.getId());
        log.info("=====================================订单处理耗时：" + (System.currentTimeMillis() - startTime.get()) + "ms=====================================");
        startTime.remove();
    }

    /**
     * 切面处理发生异常需要进行业务逻辑处理
     * @author tansitao
     * @time 2018/11/9 9:26 
     */
    @AfterThrowing(pointcut = "dealOrder()")
    public void afterThrowing(JoinPoint joinPoint) {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        AuthMember authMember = (AuthMember) request.getSession().getAttribute(SysConstant.SESSION_MEMBER);
        //订单异常，清除缓存
        redisService.remove(SysConstant.C2C_DEALING_ORDER + authMember.getId());
        log.info("=====================================订单异常，处理时间：" + (System.currentTimeMillis() - startTime.get()) + "ms=====================================");
        startTime.remove();
    }
}
