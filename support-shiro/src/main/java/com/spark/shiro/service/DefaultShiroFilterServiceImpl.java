package com.spark.shiro.service;

import java.util.Map;

public class DefaultShiroFilterServiceImpl implements  ShiroFilterService{
    @Override
    public void loadExtendShiroFileter(Map<String, String> filterChainDefinitionMap) {
        //默认什么都不执行
    }
}
