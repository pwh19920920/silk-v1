package com.spark.bitrade.util;


import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.enums.MessageCode;

public class MessageResult {
    private int code;
    private String message;
    private Object data;

    public MessageResult(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public MessageResult(int code, String msg, Object object) {
        this.code = code;
        this.message = msg;
        this.data = object;
    }

    public MessageResult() {
    }

    public static MessageResult success() {
        return new MessageResult(0, "SUCCESS");
    }

    public static MessageResult success(String msg) {
        return new MessageResult(0, msg);
    }

    public static MessageResult success(String msg, Object data) {
        return new MessageResult(0, msg, data);
    }

    public static MessageResult error(int code, String msg) {
        return new MessageResult(code, msg);
    }

    public static MessageResult error(String msg) {
        return new MessageResult(500, msg);
    }

    public static MessageResult error(MessageCode messageCode, String msg) {
        return new MessageResult(messageCode.getCode(), msg);
    }

    public static MessageResult error(MessageCode messageCode) {
        return new MessageResult(messageCode.getCode(), messageCode.name());
    }

    public boolean isSuccess() {
        if (code == 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageRespResult to() {
        return new MessageRespResult(this.code, this.message, this.data);
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
        //return "{\"code\":"+code+",\"message\":\""+message+"\"}";
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static MessageResult getSuccessInstance(String message, Object data) {
        MessageResult result = success(message);
        result.setData(data);
        return result;
    }
}
