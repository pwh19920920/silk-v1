package com.spark.bitrade.entity;

import sun.net.www.protocol.http.AuthenticationHeader;

/**
 * Created by Administrator on 2019/3/8.
 */
public class AuthCode {

    private String authCode;
    private String barCode;

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public AuthCode(){

    }

    public AuthCode(String authCode, String barCode) {
        this.authCode = authCode;
        this.barCode = barCode;
    }
}
