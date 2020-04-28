package com.spark.bitrade.annotation.definition;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * <p>接口请求限制</p>
 * @author tian.bo
 * @date 2018/12/24.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Order(Ordered.HIGHEST_PRECEDENCE)
public @interface ApiRequestLimit {

    /**
     * 允许访问的次数,默认MAX_VALUE
     */
    int count() default Integer.MAX_VALUE;

    /**
     * 时间周期,单位毫秒,默认一分钟
     */
    long time() default 60000;
}
