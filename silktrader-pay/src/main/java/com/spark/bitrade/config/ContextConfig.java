package com.spark.bitrade.config;

import com.spark.bitrade.ext.OrdinalToEnumConverterFactory;
import com.spark.bitrade.interceptor.MemberInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class ContextConfig extends WebMvcConfigurerAdapter {

	/**
	 * 国际化
	 *
	 * @return
	 */
	@Bean(name = "messageSource")
	public ResourceBundleMessageSource getMessageSource() {
		ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
		resourceBundleMessageSource.setDefaultEncoding("UTF-8");
		resourceBundleMessageSource.setBasenames("i18n/messages", "i18n/ValidationMessages");
		resourceBundleMessageSource.setCacheSeconds(3600);
		return resourceBundleMessageSource;
	}

	@Override
	public Validator getValidator() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.setValidationMessageSource(getMessageSource());
		return validator;
	}


	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverterFactory(new OrdinalToEnumConverterFactory());
		super.addFormatters(registry);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new MemberInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/account/toMemberInfo",//收款方信息
						"/account/platformTransferByMemberId",//扫码枪平台互转
						"/account/supportCoinNoCondition",
						"/account/cloudRecord",//云端流水
						"/account/platformTransferByPhone","/account/wallet/getCoinAddrByUsername",
						"/account/supportCoinByAppId","/account/supportCoinByReq","/otc/api/flowRecord",
						"/api/fastRecord","/paymentCode/api/barCodeScannerPay",
						"/notify/business/api/get","/notify/business/api/etgscGateway");
		super.addInterceptors(registry);
	}

	// @Bean
	// public FilterRegistrationBean corsFilter() {
	//      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	//      CorsConfiguration config = new CorsConfiguration();
	//      config.addAllowedOrigin("*");
	//      config.setAllowCredentials(true);
	//      config.addAllowedHeader("*");
	//      config.addAllowedMethod("*");
	//      source.registerCorsConfiguration("/**", config);
	//      FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
	//      bean.setOrder(0);
	//      return bean;
	// }


	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html")
				.addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/");

	}

}