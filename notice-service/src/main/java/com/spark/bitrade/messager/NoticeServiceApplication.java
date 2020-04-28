package com.spark.bitrade.messager;

import com.spark.bitrade.messager.config.ApplicationProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.annotation.WebFilter;


@EnableScheduling
@EnableDiscoveryClient
@EnableAsync
//@EnableAutoConfiguration
@EnableTransactionManagement(order = 10) //开启事务，并设置order值，默认是Integer的最大值
//@ComponentScan(basePackages={"com.spark.bitrade"})
@EnableCaching


@EnableEurekaClient
@SpringBootApplication()
@EnableSwagger2
@EnableFeignClients
//@EnableRedisHttpSession
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
//@EnableTransactionManagement
//@MapperScan(basePackages ={"com.spark.bitrade.messager.dao"})
public class NoticeServiceApplication implements ApplicationContextAware {



    public static void main(String[] args) {

        SpringApplication noticeServiceApplication = new SpringApplication(NoticeServiceApplication.class);
        // 第四种方式：注册监听器
        noticeServiceApplication.addListeners(new ApplicationProperties("application.properties"));

        noticeServiceApplication.run(args);



        new NettyServer().run();
    }


    // NettyServerHandler  Bean 支持
    private static ApplicationContext applicationContext;
    private static DefaultListableBeanFactory defaultListableBeanFactory;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        defaultListableBeanFactory = (DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
    }

    public static <T> T getBean(Class<T> clazz) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        String className = clazz.getName();
        defaultListableBeanFactory.registerBeanDefinition(className, beanDefinitionBuilder.getBeanDefinition());
        return (T) applicationContext.getBean(className);
    }

    public static void destroy(String className){
        defaultListableBeanFactory.removeBeanDefinition(className);
        System.out.println("destroy " + className);
    }

}
