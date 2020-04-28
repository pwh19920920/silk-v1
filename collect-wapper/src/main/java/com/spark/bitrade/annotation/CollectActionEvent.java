package com.spark.bitrade.annotation;

import com.spark.bitrade.constant.CollectActionEventType;

import java.lang.annotation.*;

/***
  * 事件采集
  * @author yangch
  * @time 2018.10.31 16:12
  */

@Target({ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CollectActionEvent {
    /**
     * 事件类型
     *
     * @return
     */
    CollectActionEventType collectType();

    /**
     * 用户ID，不填写则不处理，支持spring EL表达式
     *
     * @return
     */
    String memberId() default "";

    /**
     * 关联的订单号，不填写则不处理，支持spring EL表达式
     *
     * @return
     */
    String refId() default "";

    /**
     * 是否在方法执行前调用，默认为方法执行后调用
     *
     * @return
     */
    boolean beforeInvocation() default false;
}
