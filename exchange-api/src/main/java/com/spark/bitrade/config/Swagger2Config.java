package com.spark.bitrade.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;


/**
 * @author rongyu
 * @description
 * @date 2018/3/1 14:11
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Value("${swagger.is.enable:false}")
    private boolean swagger_is_enable;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swagger_is_enable)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.spark.bitrade.controller"))
                .paths(PathSelectors.any())
                //不显示错误的接口地址,错误路径不监控
//                .paths(Predicates.not(PathSelectors.regex("/error.*")))
//                // 对根下所有路径进行监控
//                .paths(PathSelectors.regex("/.*"))
                .build();
//                .securitySchemes(securitySchemes())
//                .securityContexts(securityContexts());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Exchange-Api")
                .description("servlet-context:/exchange")
//                .termsOfServiceUrl("http://blog.didispace.com/")
                .contact("星客")
                .version("1.0")
                .build();
    }

//    private List<ApiKey> securitySchemes() {
//        List<ApiKey> apiKeyList= new ArrayList();
//        ///apiKeyList.add(new ApiKey("x-auth-token", "x-auth-token", "header"));
//        apiKeyList.add(new ApiKey("access-auth-token", "access-auth-token", "header"));
//        return apiKeyList;
//    }
//
//    private List<SecurityContext> securityContexts() {
//        List<SecurityContext> securityContexts=new ArrayList<>();
//        securityContexts.add(
//                SecurityContext.builder()
//                        .securityReferences(defaultAuth())
//                        //.forPaths(PathSelectors.regex("^(?!auth).*$"))
//                        .build());
//        return securityContexts;
//    }
//
//    List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        List<SecurityReference> securityReferences=new ArrayList<>();
//        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
//        return securityReferences;
//    }
}
