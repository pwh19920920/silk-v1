package com.spark.bitrade.config;

import com.alibaba.druid.support.spring.stat.DruidStatInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageHelper;
import com.spark.bitrade.constant.DataSourceContextHolder;
import com.spark.bitrade.constant.DataSourceType;
import com.spark.bitrade.interceptor.SqlPrintInterceptor;
import com.spark.bitrade.util.MyResolverUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@AutoConfigureAfter(DataSourceConfiguration.class)
@MapperScan(basePackages="com.spark.bitrade.mapper.dao")
//@DependsOn("springContextUtil")
@Slf4j
@ConditionalOnProperty(value = "shardingSphere.config", havingValue = "false", matchIfMissing = true)
public class MybatisConfiguration {
	/*@Value("${mysql.datasource.readSize}")
    private String readDataSourceSize;*/

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



    @Autowired
    @Qualifier("writeDataSource")
    private DataSource writeDataSource;
    @Autowired
    @Qualifier("readDataSource01")
    private DataSource readDataSource01;
    @Autowired
    @Qualifier("readDataSource02")
    private DataSource readDataSource02;


    @Bean(name="sqlSessionFactory")
    @ConditionalOnProperty(value = "mybatisplus.config",havingValue = "false",matchIfMissing = true)
    public SqlSessionFactory sqlSessionFactorys() throws Exception {
        log.info("--------------------  mybatis sqlSessionFactory init ---------------------");
        try {
            SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
            sessionFactoryBean.setDataSource(roundRobinDataSouceProxy());

            // 读取配置
            //sessionFactoryBean.setTypeAliasesPackage("com.spark.bitrade.domain");
            sessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);

            //设置mapper.xml文件所在位置
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(mapperLocations);
            sessionFactoryBean.setMapperLocations(resources);
            //设置mybatis-config.xml配置文件位置
            sessionFactoryBean.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));

            //添加分页插件、打印sql插件
            Interceptor[] plugins = new Interceptor[]{pageHelper(),new SqlPrintInterceptor()};
            sessionFactoryBean.setPlugins(plugins);

            //edit by yangch 时间： 2019.01.18 原因：修改为配置
            //sessionFactoryBean.setTypeHandlersPackage("com.spark.bitrade.config.handler");
            sessionFactoryBean.setTypeHandlersPackage(typeHandlersPackage);

            //del by yangch 时间： 2019.01.18 原因：解决事务不生效问题
            //sessionFactoryBean.setTransactionFactory(new MultiDataSourceTransactionFactory());

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


    @Bean(name="sqlSessionFactory")
    @ConditionalOnProperty(value = "mybatisplus.config",havingValue = "true")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        log.info("-------------------- mybatis-plus sqlSessionFactory init ---------------------");
        try {
            //SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();

            MybatisSqlSessionFactoryBean sessionFactoryBean = new MybatisSqlSessionFactoryBean();
            sessionFactoryBean.setDataSource(roundRobinDataSouceProxy());

            // 读取配置
            //sessionFactoryBean.setTypeAliasesPackage("com.spark.bitrade.domain");
            sessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);

            //设置mapper.xml文件所在位置
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(mapperLocations);
            sessionFactoryBean.setMapperLocations(resources);
            //设置mybatis-config.xml配置文件位置
            sessionFactoryBean.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));

            //添加分页插件、打印sql插件
            Interceptor[] plugins = new Interceptor[]{pageHelper(),new SqlPrintInterceptor()};
            sessionFactoryBean.setPlugins(plugins);

            //edit by yangch 时间： 2019.01.18 原因：修改为配置
            //sessionFactoryBean.setTypeHandlersPackage("com.spark.bitrade.config.handler");
            sessionFactoryBean.setTypeHandlersPackage(typeHandlersPackage);

            //add by yangch 时间： 2019.01.18 原因：设置枚举路径
            sessionFactoryBean.setTypeEnumsPackage(typeEnumsPackage);

            //del by yangch 时间： 2019.01.18 原因：解决事务不生效问题
            //sessionFactoryBean.setTransactionFactory(new MultiDataSourceTransactionFactory());

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
    /**
     * 把所有数据库都放在路由中
     * @return
     */
    @Bean(name="roundRobinDataSouceProxy")
    public AbstractRoutingDataSource roundRobinDataSouceProxy() {
        Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
        //把所有数据库都放在targetDataSources中,注意key值要和determineCurrentLookupKey()中代码写的一至，
        //否则切换数据源时找不到正确的数据源
        targetDataSources.put(DataSourceType.write.getType(), writeDataSource);
        targetDataSources.put(DataSourceType.read.getType()+"1", readDataSource01);
        targetDataSources.put(DataSourceType.read.getType()+"2", readDataSource02);

        //final int readSize = Integer.parseInt(readDataSourceSize);
        final int readSize = targetDataSources.size()-1;

        //路由类，寻找对应的数据源
        AbstractRoutingDataSource proxy = new AbstractRoutingDataSource(){
            private AtomicInteger count = new AtomicInteger(Integer.MAX_VALUE);
            /**
             * 这是AbstractRoutingDataSource类中的一个抽象方法，
             * 而它的返回值是你所要用的数据源dataSource的key值，有了这个key值，
             * targetDataSources就从中取出对应的DataSource，如果找不到，就用配置默认的数据源。
             */
            @Override
            protected Object determineCurrentLookupKey() {
                String typeKey = DataSourceContextHolder.getReadOrWrite();
                if (typeKey == null || typeKey.equals(DataSourceType.write.getType())){
                    //System.err.println("使用数据库write.............");
                    log.info("datasource write");
                    return DataSourceType.write.getType();
                }

                //读库， 简单负载均衡
                if(count.get() == Integer.MAX_VALUE) {
                    count.set(0);
                }
                int number = count.getAndAdd(1);
                int lookupKey = number % readSize;
                //System.err.println("使用数据库read-"+(lookupKey+1));
                log.info("datasource read-{}", (lookupKey+1));
                return DataSourceType.read.getType()+(lookupKey+1);
            }
        };

        proxy.setDefaultTargetDataSource(writeDataSource);//默认库
        proxy.setTargetDataSources(targetDataSources);
        return proxy;
    }


    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        MyResolverUtil<Class<?>> resolverUtil = new MyResolverUtil<>();
        //edit by yangch 时间： 2019.01.18 原因：修改为配置
        //resolverUtil.find(new MyResolverUtil.IsA(Enum.class), "com.spark.bitrade.constant");
        resolverUtil.find(new MyResolverUtil.IsA(Enum.class), typeEnumsPackage);
        Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
        for(Class<?> type : typeSet){
            log.info("Scanned type: '{}' use enum ordinal type handler",type.getName());
            typeHandlerRegistry.register(type, new EnumOrdinalTypeHandler(type));
        }

        return new SqlSessionTemplate(sqlSessionFactory);
    }

    //事务管理
    @Bean(name={"transactionManager","annotationDrivenTransactionManager"})
    /*public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager((DataSource) SpringContextUtil.getBean("roundRobinDataSouceProxy"));
    }*/
    public PlatformTransactionManager annotationDrivenTransactionManager(@Qualifier("roundRobinDataSouceProxy") AbstractRoutingDataSource roundRobinDataSouceProxy) {
        return new DataSourceTransactionManager(roundRobinDataSouceProxy);
    }



    // 配置Druid关联Spring监控，以下druid配置移走后启动报错
    // 如 无法解析mysql.datasource.write.username=bjxy_db，提示Reason: Property 'username' threw exception; nested exception is java.lang.UnsupportedOperationExcep
    @Bean
    public DruidStatInterceptor druidStatInterceptor() {
        DruidStatInterceptor dsInterceptor = new DruidStatInterceptor();
        return dsInterceptor;
    }

    @Bean
    @Scope("prototype")
    public JdkRegexpMethodPointcut druidStatPointcut() {
        JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
        pointcut.setPattern("com.spark.bitrade.dao.*");
        pointcut.setPattern("com.spark.bitrade.service.*");
        //pointcut.setPattern("com.spark.bitrade.service.*");
        return pointcut;
    }

    @Bean
    public DefaultPointcutAdvisor druidStatAdvisor(DruidStatInterceptor druidStatInterceptor, JdkRegexpMethodPointcut druidStatPointcut) {
        DefaultPointcutAdvisor defaultPointAdvisor = new DefaultPointcutAdvisor();
        defaultPointAdvisor.setPointcut(druidStatPointcut);
        defaultPointAdvisor.setAdvice(druidStatInterceptor);
        return defaultPointAdvisor;
    }
}
