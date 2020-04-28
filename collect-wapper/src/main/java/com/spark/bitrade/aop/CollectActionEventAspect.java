package com.spark.bitrade.aop;

import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.service.IPushCollectCarrier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/***
  * 事件采集拦截器
  * @author yangch
  * @time 2018.10.31 17:35
  */

@Component
@Aspect
public class CollectActionEventAspect {
    ExpressionParser parser = new SpelExpressionParser();
    LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    @Autowired
    private IPushCollectCarrier pushCollectCarrier;

    @Around("@annotation(action)")
    public Object invoked(ProceedingJoinPoint pjp, CollectActionEvent action) throws Throwable {
        if (action.beforeInvocation()) {
            pushCollectCarrier(pjp, action);

            return pjp.proceed();
        } else {
            Object proceed = pjp.proceed();

            pushCollectCarrier(pjp, action);

            return proceed;
        }
    }

    /**
     * 推送消息
     *
     * @param pjp
     * @param action
     */
    private void pushCollectCarrier(ProceedingJoinPoint pjp, CollectActionEvent action) {
        try {
            if (StringUtils.isEmpty(action.memberId()) == false
                    || StringUtils.isEmpty(action.refId()) == false) {

                Object[] args = pjp.getArgs();
                Method method = ((MethodSignature) pjp.getSignature()).getMethod();
                String[] params = discoverer.getParameterNames(method);
                EvaluationContext context = new StandardEvaluationContext();
                for (int len = 0; len < params.length; len++) {
                    context.setVariable(params[len], args[len]);
                }

                CollectCarrier carrier = new CollectCarrier();
                carrier.setCollectType(action.collectType());
                carrier.setCreateTime(new Date());
                carrier.setLocale(SysConstant.ZH_LANGUAGE);

                if (!StringUtils.isEmpty(action.memberId())) {
                    Expression expMemberId = parser.parseExpression(action.memberId());
                    carrier.setMemberId(expMemberId.getValue(context, String.class));
                }

                if (!StringUtils.isEmpty(action.refId())) {
                    Expression expRefId = parser.parseExpression(action.refId());
                    carrier.setRefId(expRefId.getValue(context, String.class));
                }

                //语言环境
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (null != requestAttributes) {
                    //HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                    if (null != request) {
                        carrier.setLocale(request.getLocale().toString());
                    }
                }


                //if (expRefId.getValue(context, Integer.class) >0) {
//            System.out.println(action.refId() + ",在"
//                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
//                    + "执行方法，" + pjp.getTarget().getClass() + "." + method.getName()
//                    + "(" + convertArgs(args) + ")");
                //}

                pushCollectCarrier.push(carrier);
            } else {
                //System.out.println("不推送消息");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String convertArgs(Object[] args) {
        StringBuilder builder = new StringBuilder();
        for (Object arg : args) {
            if (null == arg) {
                builder.append("null");
            } else {
                builder.append(arg.toString());
            }
            builder.append(',');
        }
        builder.setCharAt(builder.length() - 1, ' ');
        return builder.toString();
    }
}
