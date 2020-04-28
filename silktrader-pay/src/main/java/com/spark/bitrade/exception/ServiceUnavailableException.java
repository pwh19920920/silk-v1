package com.spark.bitrade.exception;

/**
 * <p>服务不可用</p>
 * @author tian.bo
 * @since 2019/3/11.
 */
public class ServiceUnavailableException extends RuntimeException {

    protected String   errCode = "20000";
    protected String   errMsg = "服务不可用";

    public ServiceUnavailableException() {
        super();
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(Throwable cause) {
        super(cause);
    }

    public ServiceUnavailableException(String errCode, String errMsg) {
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
