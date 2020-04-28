package com.spark.shiro.realm;

import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Map;
import java.util.Set;

public abstract class SsoCasRealm<T> extends Pac4jRealm {
    /**
     * 授权逻辑
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        final Pac4jPrincipal principal = principals.oneByType(Pac4jPrincipal.class);
        String loginNo = principal.getProfile().getId();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setRoles(getRoleNames(loginNo));
        authorizationInfo.setStringPermissions(getPermissionNames(loginNo));
        return authorizationInfo;
    }


    /**
     *
     * <p>Title: clearAllCachedAuthorizationInfo</p>
     * <p>Description: </p>
     * 清除所有缓存授权
     */
    public void clearAllCachedAuthorizationInfo() {
        getAuthorizationCache().clear();
    }
    /**
     *
     * <p>Title: clearAllCachedAuthenticationInfo</p>
     * <p>Description: </p>
     * 清除所有缓存权限
     */
    public void clearAllCachedAuthenticationInfo() {
        getAuthenticationCache().clear();
    }
    /**
     *
     * <p>Title: clearAllCache</p>
     * <p>Description: </p>
     * 清除所有缓存
     */
    public void clearAllCache() {
        clearAllCachedAuthenticationInfo();
        clearAllCachedAuthorizationInfo();
    }

    /**
     *
     * <p>Title: getRoleNames</p>
     * <p>Description: </p>
     * 根据用户名查询角色
     * @param loginNo
     * @return
     */
    protected abstract Set<String> getRoleNames(String loginNo);
    /**
     *
     * <p>Title: getPermissionNames</p>
     * <p>Description: </p>
     * 根据用户名查询权限
     * @param loginNo
     * @return
     */
    protected abstract Set<String> getPermissionNames(String loginNo) ;
}
