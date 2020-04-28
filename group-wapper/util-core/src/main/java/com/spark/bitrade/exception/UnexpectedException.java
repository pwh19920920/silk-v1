package com.spark.bitrade.exception;

/**
 * 非预期的结果
 * @author yangch
 * @date 2018-05-18
 */
public class UnexpectedException extends Exception {
    public UnexpectedException(String msg) {
        super(msg);
    }
}
