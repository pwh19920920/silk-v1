package com.spark.shiro.service;

import java.util.Map;

/**
 * shiro过滤器添加服务
 */
public interface ShiroFilterService {
    public void loadExtendShiroFileter(Map<String, String> filterChainDefinitionMap);
}
