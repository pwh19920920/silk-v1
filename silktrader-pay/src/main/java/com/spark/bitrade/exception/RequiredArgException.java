package com.spark.bitrade.exception;

/**
 * Created by Administrator on 2019/3/11.
 */
public class RequiredArgException extends MissingArgException {

    protected String   subCode;
    protected String   subMsg;

    public RequiredArgException() {
        super();
    }

    public RequiredArgException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequiredArgException(String message) {
        super(message);
    }

    public RequiredArgException(Throwable cause) {
        super(cause);
    }

    public RequiredArgException(String errCode, String errMsg) {
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
