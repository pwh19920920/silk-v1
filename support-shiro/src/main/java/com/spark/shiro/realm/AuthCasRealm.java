package com.spark.shiro.realm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.shiro.service.UserPermissionService;

import javax.annotation.Resource;
import java.util.Set;

public class AuthCasRealm extends SsoCasRealm {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private UserPermissionService userPermissionService;


    /**
     * 查询用户拥有的角色
     */
    protected Set<String> getRoleNames(String loginNo) {
        return userPermissionService.getRoleNames(loginNo);
    }
    /**
     * 查询用户可进行的操作
     */
    protected Set<String> getPermissionNames(String loginNo) {
        return userPermissionService.getPermissionNames(loginNo);
    }
}
