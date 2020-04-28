package com.spark.bitrade.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/***
 * 继承Transactional注解，并设置默认的事务管理器名称为transactionManager
 * @author yangch
 * @time 2019.01.18 15:27
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Transactional
public @interface MybatisTransactional {
    @AliasFor(
            annotation = Transactional.class,
            attribute = "transactionManager"
    )
    String value() default "transactionManager";

    @AliasFor(
            annotation = Transactional.class,
            attribute = "transactionManager"
    )
    String transactionManager() default "transactionManager";

    @AliasFor(
            annotation = Transactional.class,
            attribute = "propagation"
    )
    Propagation propagation() default Propagation.REQUIRED;

    @AliasFor(
            annotation = Transactional.class,
            attribute = "isolation"
    )
    Isolation isolation() default Isolation.DEFAULT;

    @AliasFor(
            annotation = Transactional.class,
            attribute = "timeout"
    )
    int timeout() default -1;

    @AliasFor(
            annotation = Transactional.class,
            attribute = "readOnly"
    )
    boolean readOnly() default false;

    @AliasFor(
            annotation = Transactional.class,
            attribute = "rollbackFor"
    )
    Class<? extends Throwable>[] rollbackFor() default {};

    @AliasFor(
            annotation = Transactional.class,
            attribute = "rollbackForClassName"
    )
    String[] rollbackForClassName() default {};

    @AliasFor(
            annotation = Transactional.class,
            attribute = "noRollbackFor"
    )
    Class<? extends Throwable>[] noRollbackFor() default {};

    @AliasFor(
            annotation = Transactional.class,
            attribute = "noRollbackForClassName"
    )
    String[] noRollbackForClassName() default {};
}
