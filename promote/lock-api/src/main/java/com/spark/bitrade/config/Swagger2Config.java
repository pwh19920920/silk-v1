package com.spark.bitrade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author rongyu
 * @description
 * @date 2018/3/1 14:11
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Value("${swagger.is.enable}")
    private boolean swagger_is_enable;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swagger_is_enable)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.spark.bitrade.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("lock-Api")
                .description("servlet-context:/lock-api")
//                .termsOfServiceUrl("http://blog.didispace.com/")
                .contact("星客")
                .version("1.0")
                .build();
    }
}
