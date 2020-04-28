package com.spark.bitrade.exception;

/**
 * <p>ApiException/p>
 *  @author tian.bo
 *  @date 2018-12-5
 */
public class ApiException extends RuntimeException {

    final String errCode;

    public ApiException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }

    public ApiException(Exception e) {
        super(e);
        this.errCode = e.getClass().getName();
    }

    public String getErrCode() {
        return this.errCode;
    }

}
