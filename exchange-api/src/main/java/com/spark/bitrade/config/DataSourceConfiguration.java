//package com.spark.bitrade.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import javax.sql.DataSource;
//
///***
// * 数据库源配置（core已有，此处冗余的目的是为了解决druid监控问题）
//
// * @author yangch
// * @time 2018.06.19 18:42
// */
//@Configuration
//@Slf4j
//public class DataSourceConfiguration {
//	@Value("${mybatis.datasource.type}")
//	private Class<? extends DataSource> dataSourceType;
//
//	/**
//	 * 写库 数据源配置
//	 * @return
//	 */
//	@Bean(name = {"writeDataSource","dataSource"})
//    @Primary
//    @ConfigurationProperties(prefix = "mysql.datasource.write")
//    //@ConfigurationProperties(prefix = "mysql.datasource")
//    public DataSource writeDataSource() {
//        log.info("--------------------exchange-api writeDataSource init ---------------------");
//        return DataSourceBuilder.create().type(dataSourceType).build();
//    }
//
//	/**
//     * 有多少个从库就要配置多少个
//     * @return
//     */
//    @Bean(name = "readDataSource01")
//    @ConfigurationProperties(prefix = "mysql.datasource.read01")
//    public DataSource readDataSourceOne() {
//        log.info("--------------------exchange-api  read01 DataSourceOne init ---------------------");
//        return DataSourceBuilder.create().type(dataSourceType).build();
//    }
//
//    @Bean(name = "readDataSource02")
//    @ConfigurationProperties(prefix = "mysql.datasource.read02")
//    public DataSource readDataSourceTwo() {
//        log.info("--------------------exchange-api  read02 DataSourceTwo init ---------------------");
//        return DataSourceBuilder.create().type(dataSourceType).build();
//    }
//
//
//}
