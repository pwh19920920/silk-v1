package com.spark.bitrade.exception;

/**
 * Created by Administrator on 2019/3/11.
 */
public class MissingArgException extends RuntimeException{

    protected String   errCode = "40001";
    protected String   errMsg = "缺少必选参数";

    public MissingArgException() {
        super();
    }

    public MissingArgException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingArgException(String message) {
        super(message);
    }

    public MissingArgException(Throwable cause) {
        super(cause);
    }

    public MissingArgException(String errCode, String errMsg) {
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
