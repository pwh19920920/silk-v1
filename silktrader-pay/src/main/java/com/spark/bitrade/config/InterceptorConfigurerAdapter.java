package com.spark.bitrade.config;

import com.spark.bitrade.interceptor.WalletPayHttpRequestHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * <p>InterceptorConfigurerAdapter</p>
 * @author tian.bo
 * @date 2018-12-6
 */
@Configuration
public class InterceptorConfigurerAdapter extends WebMvcConfigurerAdapter {

    //将拦截器作为bean写入配置中
    @Bean
    public WalletPayHttpRequestHandlerInterceptor httpRequestHandlerInterceptor(){
        return new WalletPayHttpRequestHandlerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns 添加路径
        // excludePathPatterns 排除路径
        registry.addInterceptor(httpRequestHandlerInterceptor()).addPathPatterns("/pay/QRCode/api/**","");
        super.addInterceptors(registry);
    }

}
