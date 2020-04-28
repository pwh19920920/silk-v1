package com.spark.bitrade.system;

import com.spark.bitrade.exception.GeeTestException;
import com.spark.bitrade.exception.InconsistencyException;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

/**
 * @author Zhang Jinwei
 * @date 2017年12月23日
 */
@Slf4j
@ControllerAdvice
public class MyControllerAdvice {
    /**
     * 拦截乐观锁失败异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = ObjectOptimisticLockingFailureException.class)
    public MessageResult myErrorHandler(ObjectOptimisticLockingFailureException ex) {
        log.error("乐观锁失败异常", ex);
        ///ex.printStackTrace();
        MessageResult result = MessageResult.error(6000, "数据过期，请刷新重试");
        return result;
    }

    /**
     * 拦截参数异常
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public MessageResult myErrorHandler(IllegalArgumentException e) {
        log.error("参数异常", e);
        ///e.printStackTrace();
        MessageResult result = MessageResult.error(e.getMessage());
        return result;
    }

    /**
     * 拦截绑定参数异常
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public MessageResult myErrorHandler(ServletRequestBindingException e) {
        log.error("绑定参数异常", e);
        ///e.printStackTrace();
        MessageResult result = MessageResult.error(3000, "参数绑定错误(如:必须参数没传递)!");
        return result;
    }


   /* @ResponseBody
    @ExceptionHandler(value = RedisConnectionFailureException.class)
    public MessageResult myErrorHandler(RedisConnectionFailureException e) {
        e.printStackTrace();
        MessageResult result = MessageResult.error(2000, "网络异常，请稍后重试");
        return result;
    }*/

    /**
     * 拦截数据过期异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = InformationExpiredException.class)
    public MessageResult myErrorHandler(InformationExpiredException ex) {
        log.error("数据过期异常", ex);
        ///ex.printStackTrace();
        MessageResult result = MessageResult.error("数据过期，请刷新重试");
        return result;
    }

    /**
     * 拦截数据不一致异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = InconsistencyException.class)
    public MessageResult myErrorHandler(InconsistencyException ex) {
        log.error("数据不一致异常", ex);
        ///ex.printStackTrace();
        MessageResult result = MessageResult.error(ex.getMessage());
        return result;
    }

    /**
     * 异常 不是想要的结果
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = UnexpectedException.class)
    public MessageResult myErrorHandler(UnexpectedException ex) {
        log.error("数据不一致异常", ex);
        ///ex.printStackTrace();
        MessageResult result = MessageResult.error(ex.getMessage());
        return result;
    }

    /**
     * 拦截极验证验证失败异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = GeeTestException.class)
    public MessageResult myErrorHandler(GeeTestException ex) {
        log.error("极验证验证失败异常", ex);
        ///ex.printStackTrace();
        MessageResult result = MessageResult.error(ex.getMessage());
        return result;
    }

    /**
     * 拦截异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public MessageResult myErrorHandler(Exception ex) {
        log.error("未知异常", ex);
        if (Objects.nonNull(ex.getCause())
                && ex.getCause().getClass().toString().equals("class com.netflix.client.ClientException")) {
            return MessageResult.error("the service is unavailable, Please try again later");
        } else {
            ///ex.printStackTrace();
            return MessageResult.error("未知错误");
        }
    }

    /**
     * @param
     * @return
     * @author rongyu
     * @description 错误请求方式异常  HttpRequestMethodNotSupportedException
     * @date 2018/2/28 17:32
     */
    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public MessageResult httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("错误请求方式异常", ex);
        ///ex.printStackTrace();
        String methods = "";
        //支持的请求方式
        String[] supportedMethods = ex.getSupportedMethods();
        for (String method : supportedMethods) {
            methods += method;
        }
        MessageResult result = MessageResult.error("Request method " + ex.getMethod() + "  not supported !" +
                " supported method : " + methods + "!");
        return result;
    }
}
