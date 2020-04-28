package com.spark.bitrade.config;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.alibaba.druid.support.http.StatViewServlet;

/***
  * druid数据源状态监控.
  * @author yangch
  * @time 2018.06.23 8:39
  */
//启动类添加注解：@ServletComponentScan("com.spark.bitrade.config")
//http://127.0.0.1:8080/druid/
@WebServlet(urlPatterns = "/druid/*",
        initParams = {
                //@WebInitParam(name="allow",value="192.168.1.72,127.0.0.1"),// IP白名单(没有配置或者为空，则允许所有访问)
                //@WebInitParam(name="deny",value="192.168.1.73"),// IP黑名单 (存在共同时，deny优先于allow)
                @WebInitParam(name = "loginUsername", value = "deaking"),// 用户名
                @WebInitParam(name = "loginPassword", value = "deakingPassw0rd"),// 密码
                //@WebInitParam(name="resetEnable",value="false")// 禁用HTML页面上的“Reset All”功能
                @WebInitParam(name = "resetEnable", value = "true")
        }, asyncSupported = true
)
public class DruidStatViewServlet extends StatViewServlet {
    private static final long serialVersionUID = 1L;
}
