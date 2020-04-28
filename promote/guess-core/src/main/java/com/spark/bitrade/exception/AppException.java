package com.spark.bitrade.exception;

/**
 * 应用异常
 * 一般为系统编码级已知异常
 *  编码以A-[模块]-001
 */
public abstract class AppException extends RuntimeException {
    /**
     * 错误编码
     */
    public String code;

    private AppException(){

    }

    public AppException(String message,String code) {
        super(message);
        this.code=code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
