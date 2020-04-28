package com.spark.shiro.service;

import java.util.Set;

/**
 * 权限获取接口
 */
public interface UserPermissionService {
    /**
     *查询登陆用户信息
     * @param loginNo 登陆用户名
     * @return 登陆用户
     */
    String findByLoginNo(String loginNo);

    /**
     * 查询角色信息
     * @param loginNo 登陆用户名
     * @return 登陆用户
     */
    Set<String> getRoleNames(String loginNo);

    /**
     * 查询权限信息
     * @param loginNo 登陆用户名
     * @return 登陆用户
     */
    Set<String> getPermissionNames(String loginNo);
}
