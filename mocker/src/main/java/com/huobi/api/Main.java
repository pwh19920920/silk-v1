package com.huobi.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static final String API_KEY = "c1181d08-5341ea01-bae11b53-08a08";
    static final String API_SECRET = "d29c3144-41225bbf-8a31622b-9e8b0";

    public static void main(String[] args) {
        try {
            apiSample();
        } catch (ApiException e) {
            System.err.println("API Error! err-code: " + e.getErrCode() + ", err-msg: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void apiSample() {
        // create ApiClient using your api key and api secret:
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        // get symbol list:
        print(client.getSymbols());
        Map<String, String> params = new HashMap<>();
        params.put("symbol", "btcusdt");
        String resp = client.get("/market/trade", params);
        JSONObject json = JSON.parseObject(resp);
        if (json.getString("status").equals("ok")) {
            System.out.println(json.getJSONObject("tick"));
        }
    }

    static void print(Object obj) {
        try {
            System.out.println(JsonUtil.writeValue(obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
