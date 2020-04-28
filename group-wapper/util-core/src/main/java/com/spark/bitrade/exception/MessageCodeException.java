package com.spark.bitrade.exception;

import com.spark.bitrade.enums.MessageCode;

/**
 *  
 *
 * @author yangch
 * @time 2019.01.23 17:54
 */
public class MessageCodeException extends RuntimeException {
    public MessageCodeException(MessageCode code){
        super(String.valueOf(code.name()));
    }
}
