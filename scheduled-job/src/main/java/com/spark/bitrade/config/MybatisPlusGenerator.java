package com.spark.bitrade.config;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DbType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.junit.Test;

/**
 * @author shenzucai
 * @time 2018.05.11 17:38
 */
public class MybatisPlusGenerator {

    @Test
    public void generateCode() {
        String packageName = "com.spark.bitrade";
        boolean serviceNameStartWithI = false;//user -> UserService, 设置成true: user -> IUserService
        generateByTables(!serviceNameStartWithI, packageName, "dim_area"
                ,"partner_business_month_temp"
                ,"partner_business_temp");
    }

    public static void generateByTables(boolean serviceNameStartWithI, String packageName, String... tableNames) {
        GlobalConfig config = new GlobalConfig();
        String dbUrl = "jdbc:mysql://172.16.0.66:3306/otc_test?characterEncoding=utf-8";

        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL)
                .setUrl(dbUrl)
                .setUsername("bjxy_db")
                .setPassword("Credit2016Admin")
                .setDriverName("com.mysql.jdbc.Driver");

        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig
                .setCapitalMode(false)
                .setEntityLombokModel(true)
                .setDbColumnUnderline(true)
                .setNaming(NamingStrategy.underline_to_camel)
                .setInclude(tableNames);//修改替换成你需要的表名，多个表名传数组


        config.setActiveRecord(false)
                .setAuthor("shenzucai")
                .setOutputDir("D:\\program_code\\silktrade\\trade-platform\\scheduled-job\\src\\main\\java")
                .setFileOverride(true)
                .setEnableCache(false);


        if (!serviceNameStartWithI) {
            config.setServiceName("%sService");
        }
        new AutoGenerator().setGlobalConfig(config)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(
                        new PackageConfig()
                                .setParent(packageName)
                                .setController("controller")
                                .setEntity("mybatisentity")
                ).execute();
    }
}
