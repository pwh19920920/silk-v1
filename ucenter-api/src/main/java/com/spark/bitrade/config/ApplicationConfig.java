package com.spark.bitrade.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.spark.bitrade.ext.OrdinalToEnumConverterFactory;
import com.spark.bitrade.interceptor.MemberInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年02月06日
 */
@Configuration
public class ApplicationConfig extends WebMvcConfigurerAdapter {
  @Resource private ObjectMapper objectMapper;

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
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/asset/**").addResourceLocations("classpath:/asset/");
    super.addResourceHandlers(registry);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverterFactory(new OrdinalToEnumConverterFactory());
    super.addFormatters(registry);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(new MemberInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/account/platformTransferByMemberId", // 扫码枪平台互转
            "/account/supportCoinNoCondition",
            "/account/cloudRecord", // 云端流水
            "/account/platformTransferByPhone",
            "/account/wallet/getCoinAddrByUsername",
            "/asset/walletByUsername",
            "/account/supportCoinByAppId",
            "/account/supportCoinByReq",
            "/otc/api/flowRecord",
            "/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/coin/content",
            "/activity/reward-list",
            "/activity/reward-content",
            "/upload/oss/stream",
            "/thirdAuth/**",
            "/mobile/register/voice/code",
            "/register/**",
            "/mobile/validation/code",
            "/google/yzgoogle",
            "/mobile/code",
            "/mobile/codeCheck",
            "/login",
            "/check/login",
            "/start/captcha",
            "/support/country",
            "/ancillary/**",
            "/activity/lockActivityProject/**",
            "/activity/lockActivitySetting/**",
            "/activity/financialLockActivityProject/**",
            "/announcement/**",
            "/mobile/reset/code",
            "/reset/email/code",
            "/reset/login/password",
            "/vote/info",
            "/coin/supported",
            "/healthy",
            "/thirdPreLogin",
            "/thirdLoginProxy",
            "/check/token",
            "/appLogin",
            "/apiLogin",
            "/appStartImg",
            "/member/bindWallet",
            "/member/register/email",
            "/member/register/phone",
            "/promotion/checkPromotion",
            "/paymentCode/api/find",
            "/paymentCode/api/open",
            "/information/**",
            "/random/image/**",
            "/inner/service/**");
    super.addInterceptors(registry);
  }

  // edit by yangch 时间： 2018.07.06 原因：api网关层处理，重复处理会报跨域问题
  /*@Bean
  public FilterRegistrationBean corsFilter() {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      CorsConfiguration config = new CorsConfiguration();
      config.addAllowedOrigin("*");
      config.setAllowCredentials(true);
      config.addAllowedHeader("*");
      config.addAllowedMethod("*");
      config.addExposedHeader("x-auth-token");
      source.registerCorsConfiguration("/**", config);
      FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
      bean.setOrder(0);
      return bean;
  }*/

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    // https://blog.csdn.net/L_Sail/article/details/70217393

    // 将输出的long转换为string
    /*MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
    ObjectMapper objectMapper = jackson2HttpMessageConverter.getObjectMapper();*/
    // 不显示为null的字段
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    SimpleModule simpleModule = new SimpleModule();
    // edit by young 时间： 2019.07.08 原因：兼容新增的long长度为19位时，导致的前端不能正常使用的问题
    /*simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
    simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);*/
    simpleModule.addSerializer(Long.class, MyToStringSerializer.instance);
    simpleModule.addSerializer(Long.TYPE, MyToStringSerializer.instance);
    objectMapper.registerModule(simpleModule);

    /*jackson2HttpMessageConverter.setObjectMapper(objectMapper);
    //放到第一个
    converters.add(0, jackson2HttpMessageConverter);*/
  }
}
