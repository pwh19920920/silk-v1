package com.spark.bitrade.config;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import com.alibaba.druid.support.http.WebStatFilter;

/***
  * druid过滤器.
  * @author yangch
  * @time 2018.06.23 8:48
  */
@WebFilter(filterName = "druidWebStatFilter", urlPatterns = "/*",
        initParams = {
                @WebInitParam(name = "exclusions", value = "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*")//忽略资源
        }, asyncSupported = true
)
public class DruidStatFilter extends WebStatFilter {
}