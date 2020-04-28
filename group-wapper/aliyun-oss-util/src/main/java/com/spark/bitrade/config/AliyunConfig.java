package com.spark.bitrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云配置文件
 * @author tansitao
 * @time 2018/7/23 14:50 
 */
@Configuration
@ConfigurationProperties("aliyun")
public class AliyunConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String ossSchema = "https";
    private String ossEndpoint;
    private String ossBucketName;
    private Integer overTime;
    private String publicAccessKeyId;
    private String publicAccessKeySecret;
    private String publicOssEndpoint;
    private String publicOssBucketName;
    private  String publicDirectory;

    public String getPublicAccessKeyId() {
        return publicAccessKeyId;
    }

    public void setPublicAccessKeyId(String publicAccessKeyId) {
        this.publicAccessKeyId = publicAccessKeyId;
    }

    public String getPublicAccessKeySecret() {
        return publicAccessKeySecret;
    }

    public void setPublicAccessKeySecret(String publicAccessKeySecret) {
        this.publicAccessKeySecret = publicAccessKeySecret;
    }

    public String getPublicOssEndpoint() {
        return publicOssEndpoint;
    }

    public void setPublicOssEndpoint(String publicOssEndpoint) {
        this.publicOssEndpoint = publicOssEndpoint;
    }

    public String getPublicOssBucketName() {
        return publicOssBucketName;
    }

    public void setPublicOssBucketName(String publicOssBucketName) {
        this.publicOssBucketName = publicOssBucketName;
    }

    public Integer getOverTime() {
        return overTime;
    }

    public void setOverTime(Integer overTime) {
        this.overTime = overTime;
    }

    public String getOssSchema() {
        return ossSchema;
    }

    public void setOssSchema(String ossSchema) {
        this.ossSchema = ossSchema;
    }

    public String getOssEndpoint() {
        return ossEndpoint;
    }

    public void setOssEndpoint(String ossEndpoint) {
        this.ossEndpoint = ossEndpoint;
    }

    public String getOssBucketName() {
        return ossBucketName;
    }

    public void setOssBucketName(String ossBucketName) {
        this.ossBucketName = ossBucketName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getPublicDirectory() {
        return publicDirectory;
    }

    public void setPublicDirectory(String publicDirectory) {
        this.publicDirectory = publicDirectory;
    }
}
