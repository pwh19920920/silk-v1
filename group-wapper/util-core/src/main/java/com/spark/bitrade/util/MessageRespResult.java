package com.spark.bitrade.util;


import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.enums.MessageCode;

public class MessageRespResult<T> {
    private int code;
    private String message;
    private T data;

    public MessageRespResult(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public MessageRespResult(int code, String msg, T object) {
        this.code = code;
        this.message = msg;
        this.data = object;
    }

    public MessageRespResult() {
    }

    public static MessageRespResult success() {
        return new MessageRespResult(0, "SUCCESS");
    }

    public static MessageRespResult success(String msg) {
        return new MessageRespResult(0, msg);
    }

    public static MessageRespResult success(String msg, Object data) {
        return new MessageRespResult(0, msg, data);
    }

    /**
     * 成功并返回数据类型
     *
     * @param data 返回的数据
     * @param <T>  返回的类型
     * @return
     */
    public static <T> MessageRespResult<T> success4Data(T data) {
        return new MessageRespResult(0, "SUCCESS", data);
    }

    public static MessageRespResult error(int code, String msg) {
        return new MessageRespResult(code, msg);
    }

    public static MessageRespResult error(String msg) {
        return new MessageRespResult(500, msg);
    }

    public static MessageRespResult error(MessageCode messageCode, String msg) {
        return new MessageRespResult(messageCode.getCode(), msg);
    }

    public static MessageRespResult error(MessageCode messageCode) {
        return new MessageRespResult(messageCode.getCode(), messageCode.name());
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


    public MessageResult to() {
        return new MessageResult(this.code, this.message, this.data);
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
        //return "{\"code\":"+code+",\"message\":\""+message+"\"}";
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static MessageRespResult getSuccessInstance(String message, Object data) {
        MessageRespResult result = success(message);
        result.setData(data);
        return result;
    }
}
