package com.spark.bitrade.exception;

/**
 * 空对象 空数据 异常
 */
public class EmptyObjectException extends AppException {

    private static String ERROR_CODE="A-01-002";

    public EmptyObjectException(String message) {
        super(message, ERROR_CODE);
    }
}
