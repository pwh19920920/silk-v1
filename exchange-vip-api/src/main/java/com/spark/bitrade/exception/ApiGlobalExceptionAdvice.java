package com.spark.bitrade.exception;

import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>接口异常拦截处理</p>
 * @author tian.bo
 * @date 2018-12-6
 */
@Slf4j
@ControllerAdvice
public class ApiGlobalExceptionAdvice {
    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public MessageResult apiException(ApiException ae){
        log.error(ae.getMessage());
        return MessageResult.error(ae.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ApiRequestLimitException.class)
    public MessageResult apiRequestLimitException(ApiRequestLimitException ae){
        log.error(ae.getMessage());
        return MessageResult.error(ae.getMessage());
    }



}
