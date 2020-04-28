package com.spark.bitrade.config;

import com.google.common.collect.Maps;
import com.spark.bitrade.entity.SysPermission;
import com.spark.bitrade.service.SysRoleService;
import com.spark.bitrade.core.AdminRealm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhang Jinwei
 * @date 2017年12月19日
 */
@Slf4j
@Configuration
public class ShiroConfig implements ApplicationContextAware {
    /**
     * ShiroFilterFactoryBean 处理拦截资源文件问题。
     *
     * @param securityManager
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        log.info("ShiroConfiguration.shirFilter()");
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //拦截器.
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        //配置退出过滤器,其中的具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        //过滤链定义，从上向下顺序执行，一般将 /**放在最为下边 ,这是一个坑呢，一不小心代码就不好使了;
        //filterChainDefinitionMap.put("/admin/**", "authc");

        filterChainDefinitionMap.put("/admin/**", "authc");
        filterChainDefinitionMap.put("/common/upload","anon");
        filterChainDefinitionMap.put("/admin/common/upload","anon");
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
        // 未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * shiro缓存管理器;
     * 需要注入对应的其它的实体类中：
     * 安全管理器：securityManager
     *
     * @return
     */
    @Bean
    public EhCacheManager ehCacheManager() {
        log.info("ShiroConfiguration.getEhCacheManager()");
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
        return cacheManager;
    }

    //add by yangch 时间： 2018.04.24 原因：合并新增
    @Bean(name="simpleCookie")
    public SimpleCookie getSimpleCookie(){
        SimpleCookie simpleCookie = new SimpleCookie();
        simpleCookie.setName("rememberMe");
        simpleCookie.setHttpOnly(true);
        //edit by yangch 时间： 2018.05.04 原因：临时解决后端session失效问题
        //simpleCookie.setMaxAge(7*24*60*60);
        simpleCookie.setMaxAge(30*60);
        return simpleCookie ;
    }

    //add by yangch 时间： 2018.04.24 原因：合并新增
    @Bean
    public CookieRememberMeManager getCookieRememberMeManager(){
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(getSimpleCookie());
        return cookieRememberMeManager ;
    }

    @Bean(name = "securityManager")
    public DefaultWebSecurityManager getDefaultWebSecurityManager() {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        //设置realm.
        defaultWebSecurityManager.setRealm(new AdminRealm());
        defaultWebSecurityManager.setCacheManager(ehCacheManager());
        defaultWebSecurityManager.setRememberMeManager(getCookieRememberMeManager()); //add by yangch 时间： 2018.04.24 原因：合并新增
        return defaultWebSecurityManager;
    }

    /**
     * 开启shiro aop注解支持.
     * 使用代理方式;所以需要开启代码支持;
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            //组装realm到securityManager中
            final Realm myRealm = (Realm) applicationContext.getBean("adminRealm");
            final DefaultWebSecurityManager sm = (DefaultWebSecurityManager) applicationContext
                    .getBean("securityManager");
            sm.setRealm(myRealm);
        } catch (Exception e) {
            throw new Error("Critical system error", e);
        }
    }

}
