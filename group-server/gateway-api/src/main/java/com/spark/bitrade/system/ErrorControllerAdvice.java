package com.spark.bitrade.system;

import com.netflix.zuul.exception.ZuulException;
import com.spark.bitrade.util.MessageResult;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/***
 * 
 * @author yangch
 * @time 2018.07.04 16:38
 */

//@ControllerAdvice
//public class ErrorControllerAdvice {
//
//    @ResponseBody
//    @ExceptionHandler(value = ZuulRuntimeException.class)
//    public MessageResult myErrorHandler(ZuulRuntimeException ex) {
//        ex.printStackTrace();
//        MessageResult result = MessageResult.error(429, "Too Many Requests");
//        return result;
//    }
//
//    @ResponseBody
//    @ExceptionHandler(value = ZuulException.class)
//    public MessageResult myErrorHandler(ZuulException ex) {
//        ex.printStackTrace();
//        MessageResult result = MessageResult.error(430, "Too Many Requests2");
//        return result;
//    }
//
//    @ResponseBody
//    @ExceptionHandler(value = Exception.class)
//    public MessageResult myErrorHandler(Exception ex) {
//        ex.printStackTrace();
//        MessageResult result = MessageResult.error(500, "未知错误2");
//        return result;
//    }
//}
