package com.spark.bitrade.controller;

import com.spark.bitrade.exception.MessageCodeException;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.system.MyControllerAdvice;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *  
 *
 * @author yangch
 * @time 2019.04.03 11:28
 */

@Slf4j
@ControllerAdvice
public class ExchangeControllerAdvice extends MyControllerAdvice {
    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * 统一的错误码处理
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = MessageCodeException.class)
    public MessageResult myErrorHandler(MessageCodeException ex) {
        log.error("错误码处理", ex);
        MessageResult result = MessageResult.error(msService.getMessage(ex.getMessage()));
        return result;
    }
}
