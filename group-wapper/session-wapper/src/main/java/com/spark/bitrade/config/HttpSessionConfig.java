package com.spark.bitrade.config;

import com.spark.bitrade.ext.SmartHttpSessionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;


/**
 * 12小时过期
 */
//@EnableRedisHttpSession(maxInactiveIntervalInSeconds =  3600 *12)
@EnableRedisHttpSession(maxInactiveIntervalInSeconds =  600)
public class HttpSessionConfig {
	 
	 @Bean
	 public HttpSessionStrategy httpSessionStrategy(){
		 HeaderHttpSessionStrategy headerSession = new HeaderHttpSessionStrategy();
		 CookieHttpSessionStrategy cookieSession = new CookieHttpSessionStrategy();
		 headerSession.setHeaderName("x-auth-token");

		 HeaderHttpSessionStrategy accessSession = new HeaderHttpSessionStrategy();
		 accessSession.setHeaderName("access-auth-token");
		 return new SmartHttpSessionStrategy(cookieSession, headerSession, accessSession);
	 }
}
