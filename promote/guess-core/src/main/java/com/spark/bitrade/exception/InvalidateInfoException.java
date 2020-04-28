package com.spark.bitrade.exception;

/**
 * 信息错误异常
 */
public class InvalidateInfoException extends AppException{

    private static String ERROR_CODE="A-01-001";

    public InvalidateInfoException(String message) {
        super(message,ERROR_CODE);
    }
}
