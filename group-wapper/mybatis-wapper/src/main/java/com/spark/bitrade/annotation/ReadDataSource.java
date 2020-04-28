package com.spark.bitrade.annotation;

import java.lang.annotation.*;

/***
 * mybatis读写分离：只读数据源注解
 * @author yangch
 * @time 2018.08.20 9:12
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ReadDataSource {

}
