package com.spark.bitrade.controller;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>抽象支付响应接口</p>
 * @author octopus
 * @since 2019/1/25.
 */
public class SilkTraderPayResponse implements Serializable {

    private String code;

    private String  msg;

    private String  subCode;

    private String  subMsg;

    private String  body;

    private Map<String, String> params;

    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSubCode() {
        return this.subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getSubMsg() {
        return this.subMsg;
    }

    public void setSubMsg(String subMsg) {
        this.subMsg = subMsg;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public boolean isSuccess() {
        return "10000".equals(code);
    }

}
