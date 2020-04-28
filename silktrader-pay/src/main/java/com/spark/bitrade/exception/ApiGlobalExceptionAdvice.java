package com.spark.bitrade.exception;

import com.spark.bitrade.controller.SilkTraderPayResponse;
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

    @ResponseBody
    @ExceptionHandler(value = BusinessSystemServiceUnavailableException.class)
    public SilkTraderPayResponse apiRequestLimitException(BusinessSystemServiceUnavailableException ae){
        SilkTraderPayResponse response = new SilkTraderPayResponse();
        response.setCode(ae.getErrCode());
        response.setMsg(ae.getErrMsg());
        response.setSubCode(ae.getSubCode());
        response.setSubMsg(ae.getSubMsg());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(value = RequiredArgException.class)
    public SilkTraderPayResponse apiRequestLimitException(RequiredArgException ae){
        log.error(ae.getMessage());
        SilkTraderPayResponse response = new SilkTraderPayResponse();
        response.setCode(ae.getErrCode());
        response.setMsg(ae.getErrMsg());
        response.setSubCode(ae.getSubCode());
        response.setSubMsg(ae.getSubMsg());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(value = BusinessIllegalArgException.class)
    public SilkTraderPayResponse apiRequestLimitException(BusinessIllegalArgException ae){
        log.error(ae.getMessage());
        SilkTraderPayResponse response = new SilkTraderPayResponse();
        response.setCode(ae.getErrCode());
        response.setMsg(ae.getErrMsg());
        response.setSubCode(ae.getSubCode());
        response.setSubMsg(ae.getSubMsg());
        return response;
    }

    @ResponseBody
    @ExceptionHandler(value = BusinessProcessException.class)
    public SilkTraderPayResponse apiRequestLimitException(BusinessProcessException ae){
        log.error(ae.getMessage());
        SilkTraderPayResponse response = new SilkTraderPayResponse();
        response.setCode(ae.getErrCode());
        response.setMsg(ae.getErrMsg());
        response.setSubCode(ae.getSubCode());
        response.setSubMsg(ae.getSubMsg());
        return response;
    }






}
