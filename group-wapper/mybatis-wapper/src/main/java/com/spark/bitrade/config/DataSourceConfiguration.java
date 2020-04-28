package com.spark.bitrade.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

/***
 * 数据库源配置

 * @author yangch
 * @time 2018.06.19 18:42
 */
@Configuration
@Slf4j
@ConditionalOnProperty(value = "shardingSphere.config", havingValue = "false", matchIfMissing = true)
public class DataSourceConfiguration {
	//@Value("${mysql.datasource.type}")
	@Value("${mybatis.datasource.type}")
	private Class<? extends DataSource> dataSourceType;
    
	/**
	 * 写库 数据源配置
	 * @return
	 */
	@Bean(name = {"writeDataSource","dataSource"})
    @Qualifier("writeDataSource")
    @Primary
    @ConfigurationProperties(prefix = "mysql.datasource.write")
    //@ConfigurationProperties(prefix = "mysql.datasource")
    public DataSource writeDataSource() {
        log.info("-------------------- writeDataSource init ---------------------");
        return DataSourceBuilder.create().type(dataSourceType).build();
    }

	/**
     * 有多少个从库就要配置多少个
     * @return
     */
    @Bean(name = "readDataSource01" )
    @ConfigurationProperties(prefix = "mysql.datasource.read01")
    public DataSource readDataSourceOne() {
        log.info("-------------------- read01 DataSourceOne init ---------------------");
        return DataSourceBuilder.create().type(dataSourceType).build();
    }

    @Bean(name = "readDataSource02")
    @ConfigurationProperties(prefix = "mysql.datasource.read02")
    public DataSource readDataSourceTwo() {
        log.info("-------------------- read02 DataSourceTwo init ---------------------");
        return DataSourceBuilder.create().type(dataSourceType).build();
    }
    
    
}
