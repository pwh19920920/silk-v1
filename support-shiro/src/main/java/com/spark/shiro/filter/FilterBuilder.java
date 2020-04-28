package com.spark.shiro.filter;

import io.buji.pac4j.filter.CallbackFilter;

/**
 * 过滤器生成
 *
 * @author yansheng
 * @version v1.0.0
 * @date 2017/11/15
 */
public interface FilterBuilder {

    public CallbackFilter getCallbackFilter();
}
