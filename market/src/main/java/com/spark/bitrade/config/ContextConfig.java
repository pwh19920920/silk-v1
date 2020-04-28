package com.spark.bitrade.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ContextConfig extends WebMvcConfigurerAdapter {
    //edit by yangch 时间： 2018.07.06 原因：api网关层处理，重复处理会报跨域问题

    /**
     * 解决 - Spring Boot enable <async-supported> like in web.xml
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean dispatcherServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(
                new DispatcherServlet(), "/");

        Map<String, String> params = new HashMap<String, String>();
        //params.put("org.atmosphere.servlet","org.springframework.web.servlet.DispatcherServlet");
        params.put("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
        params.put("contextConfigLocation", "net.org.selector.animals.config.ComponentConfiguration");
        registration.setInitParameters(params);
        registration.setAsyncSupported(true);
        return registration;
    }

    //edit by yangch 时间： 2018.07.06 原因：api网关层处理，重复处理会报跨域问题

	/*@Bean
	public FilterRegistrationBean corsFilter() {
	     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	     CorsConfiguration config = new CorsConfiguration();
	     config.addAllowedOrigin("*");
	     config.setAllowCredentials(true);
	     config.addAllowedHeader("*");
	     config.addAllowedMethod("*");
	     source.registerCorsConfiguration("/**", config);
	     FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
	     bean.setOrder(0);
	     return bean;
	}*/

}
