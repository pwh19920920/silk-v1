package com.spark.bitrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("location")
public class LocationConfig {
    private String ipUrl;
    private String ipToken;
    private String phoneUrl;
    private String phoneToken;
    private String ossSchema = "https";

    public String getIpUrl() {
        return ipUrl;
    }

    public void setIpUrl(String ipUrl) {
        this.ipUrl = ipUrl;
    }

    public String getPhoneUrl() {
        return phoneUrl;
    }

    public void setPhoneUrl(String phoneUrl) {
        this.phoneUrl = phoneUrl;
    }

    public String getIpToken() {
        return ipToken;
    }

    public void setIpToken(String ipToken) {
        this.ipToken = ipToken;
    }

    public String getPhoneToken() {
        return phoneToken;
    }

    public void setPhoneToken(String phoneToken) {
        this.phoneToken = phoneToken;
    }

    public String toIpUrl(String key){
        return String.format("%s://%s?token=%s&ip=%s", ossSchema,ipUrl,ipToken,key);
    }

    public String toPhoneUrl(String key){
        return String.format("%s://%s?token=%s&mobile=%s", ossSchema,phoneUrl,phoneToken,key);
    }


}
