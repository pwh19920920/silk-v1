package com.spark.bitrade.mocker.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 货币Api客户端配置
 *
 * @author yangch
 * @time 2018.04.17 14:56
 *  
 */

@Configuration
public class ApiClientConfiguration {
    //设置代理
    @Getter
    private static boolean isProxy = false;
    @Getter
    private static String proxyHost ="127.0.0.1"; //默认测试环境配置
    @Getter
    private static int proxyPort    =7777; //默认测试环境

    //构造静态变量的初始化值
    @Value("${science.internet.proxy}")
    private boolean isProxyTmp;
    @Value("${science.internet.proxy.host}")
    private String proxyHostTmp;
    @Value("${science.internet.proxy.port}")
    private int proxyPortTmp;

    @PostConstruct
    public void init() {
        isProxy = isProxyTmp;
        proxyHost = proxyHostTmp;
        proxyPort = proxyPortTmp;
    }

}
