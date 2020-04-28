package com.spark.bitrade.exception;

/**
 * Created by Administrator on 2019/3/11.
 */
public class IllegalArgException extends RuntimeException {

    protected String   errCode = "40002";
    protected String   errMsg = "非法的参数";

    public IllegalArgException() {
        super();
    }

    public IllegalArgException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalArgException(String message) {
        super(message);
    }

    public IllegalArgException(Throwable cause) {
        super(cause);
    }

    public IllegalArgException(String errCode, String errMsg) {
        super(errCode + ":" + errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public String getErrCode() {
        return this.errCode;
    }

    public String getErrMsg() {
        return this.errMsg;
    }

}
