package com.spark.bitrade.exception;

/**
 * Created by Administrator on 2019/3/11.
 */
public class BusinessSystemServiceUnavailableException extends ServiceUnavailableException {

    protected String   subCode;
    protected String   subMsg;

    public BusinessSystemServiceUnavailableException() {
        super();
    }

    public BusinessSystemServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessSystemServiceUnavailableException(String message) {
        super(message);
    }

    public BusinessSystemServiceUnavailableException(Throwable cause) {
        super(cause);
    }

    public BusinessSystemServiceUnavailableException(String errCode, String errMsg) {
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
