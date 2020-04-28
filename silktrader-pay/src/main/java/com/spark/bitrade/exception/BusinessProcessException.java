package com.spark.bitrade.exception;

/**
 * Created by Administrator on 2019/3/11.
 */
public class BusinessProcessException extends BusinessException {
    protected String   subCode;
    protected String   subMsg;

    public BusinessProcessException() {
        super();
    }

    public BusinessProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessProcessException(String message) {
        super(message);
    }

    public BusinessProcessException(Throwable cause) {
        super(cause);
    }

    public BusinessProcessException(String errCode, String errMsg) {
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
