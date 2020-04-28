package com.spark.bitrade.config;


import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageHelper;
import com.spark.bitrade.interceptor.SqlPrintInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;


/**
 *  
 *
 * @author yangch
 * @time 2019.03.19 13:39
 */
@Configuration
@MapperScan(basePackages="com.spark.bitrade.mapper.dao")
@Slf4j
@ConditionalOnProperty(value = "shardingSphere.config", havingValue = "true")
public class MybatisOnShardingShareConfiguration {
    //XxxMapper.xml文件所在路径
    @Value("${mybatis.mapperLocations}")
    private String mapperLocations;

    //  加载全局的配置文件
    @Value("${mybatis.configLocation}")
    private String configLocation;

    @Value("${mybatis.typeAliasesPackage}")
    private String typeAliasesPackage;

    //枚举包路径
    @Value("${mybatis.typeEnumsPackage:com.spark.bitrade.constant}")
    private String typeEnumsPackage;
    @Value("${mybatis.typeHandlersPackage:com.spark.bitrade.config.handler}")
    private String typeHandlersPackage;

    //引入shardingsphere后，此处的数据源为shardingsphere代理后的数据源
    @Autowired
    private DataSource dataSource;

    @Bean(name="sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        log.info("-------------------- init mybatis-plus sqlSessionFactory  on MybatisOnShardingShareConfiguration---------------------");
        try {
            //SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
            MybatisSqlSessionFactoryBean sessionFactoryBean = new MybatisSqlSessionFactoryBean();
            sessionFactoryBean.setDataSource(dataSource);

            // 读取配置
            sessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);

            //设置mapper.xml文件所在位置
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(mapperLocations);
            sessionFactoryBean.setMapperLocations(resources);
            //设置mybatis-config.xml配置文件位置
            sessionFactoryBean.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));

            //添加分页插件、打印sql插件
            Interceptor[] plugins = new Interceptor[]{pageHelper(),new SqlPrintInterceptor()};
            sessionFactoryBean.setPlugins(plugins);

            sessionFactoryBean.setTypeHandlersPackage(typeHandlersPackage);

            //设置枚举路径
            sessionFactoryBean.setTypeEnumsPackage(typeEnumsPackage);

            SqlSessionFactory sqlSessionFactory = sessionFactoryBean.getObject();

            return sqlSessionFactory;
        } catch (IOException e) {
            log.error("mybatis resolver mapper*xml is error",e);
            return null;
        } catch (Exception e) {
            log.error("mybatis sqlSessionFactoryBean create error",e);
            return null;
        }
    }

    /**
     * 分页插件
     * @return
     */
    @Bean
    public PageHelper pageHelper() {
        PageHelper pageHelper = new PageHelper();
        Properties p = new Properties();
        p.setProperty("offsetAsPageNum", "true");
        p.setProperty("rowBoundsWithCount", "true");
        p.setProperty("reasonable", "true");
        p.setProperty("returnPageInfo", "check");
        p.setProperty("params", "count=countSql");
        pageHelper.setProperties(p);
        return pageHelper;
    }


    //事务管理
    @Bean(name={"transactionManager"})
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}
