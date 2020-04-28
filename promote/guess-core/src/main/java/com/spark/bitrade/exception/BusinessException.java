package com.spark.bitrade.exception;

/**
 * 业务异常
 * 一般为业务级已知异常
 *  编码以B-[模块]-001
 */
public abstract class BusinessException extends Exception{
    /**
     * 错误编码
     */
    public String code;
    private BusinessException(){
    }
    public BusinessException(String message, String code) {
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
