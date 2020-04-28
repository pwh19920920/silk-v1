package com.spark.bitrade.exception;

/**
 * 信息不一致异常
 * @author yangch
 * @date 2018-05-18
 */
public class InconsistencyException extends Exception {
    public InconsistencyException(String msg) {
        super(msg);
    }
}
