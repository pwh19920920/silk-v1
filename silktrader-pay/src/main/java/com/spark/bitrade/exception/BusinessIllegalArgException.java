package com.spark.bitrade.exception;

/**
 * Created by Administrator on 2019/3/11.
 */
public class BusinessIllegalArgException extends IllegalArgException {

    protected String   subCode;
    protected String   subMsg;

    public BusinessIllegalArgException() {
        super();
    }

    public BusinessIllegalArgException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessIllegalArgException(String message) {
        super(message);
    }

    public BusinessIllegalArgException(Throwable cause) {
        super(cause);
    }

    public BusinessIllegalArgException(String errCode, String errMsg) {
        super(errCode + ":" + errMsg);
        this.subCode = errCode;
        this.subMsg = errMsg;
    }

    public String getSubCode() {
        return this.subCode;
    }

    public String getSubMsg() {
        return this.subMsg;
    }
}
