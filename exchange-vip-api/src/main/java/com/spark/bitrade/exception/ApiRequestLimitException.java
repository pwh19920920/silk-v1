package com.spark.bitrade.exception;

/**
 * <p>ApiRequestLimitException</p>
 * @author tian.bo
 * @date 2018/12/25.
 */
public class ApiRequestLimitException extends RuntimeException {

    final String errCode;

    public ApiRequestLimitException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }

    public ApiRequestLimitException(Exception e) {
        super(e);
        this.errCode = e.getClass().getName();
    }

    public String getErrCode() {
        return this.errCode;
    }

}
