package com.spark.shiro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.shiro.service.UserPermissionService;
import com.spark.shiro.filter.DefaultCallbackFilter;
import com.spark.shiro.filter.SysUserFilter;
import com.spark.shiro.filter.UserFilter;
import com.spark.shiro.realm.AuthCasRealm;
import com.spark.shiro.realm.SsoCasRealm;
import com.spark.shiro.service.DefaultShiroFilterServiceImpl;
import com.spark.shiro.service.ShiroFilterService;
import com.spark.shiro.slo.SingleSignOutFilter;
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.subject.Pac4jSubjectFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSubjectFactory;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SsoProperties.class)
public class ShiroConfig {
    public static final String SESSION_LOGIN_USER = "loginUser";

    @Autowired
    SsoProperties ssoProperties;
    @Bean
    public DefaultSecurityManager defaultSecurityManager(
            SsoCasRealm ssoCasRealm, DefaultSubjectFactory subjectFactory,
            DefaultSessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(ssoCasRealm);
        securityManager.setSessionManager(sessionManager);
        securityManager.setSubjectFactory(subjectFactory);
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean
    public SsoCasRealm ssoCasRealm() {
        return new AuthCasRealm();
    }

    @Bean
    public Pac4jSubjectFactory subjectFactory() {
        return new Pac4jSubjectFactory();
    }

    @Bean
    public CasConfiguration casClientConfiguration() {
        return new CasConfiguration(ssoProperties.getLoginUrl(),ssoProperties.getPrefixUrl());
    }

    @Bean
    public CasClient casClient(CasConfiguration casClientConfiguration) {
        CasClient casClient = new CasClient();
        casClient.setName("CasClient");
        casClient.setConfiguration(casClientConfiguration);
        casClient.setCallbackUrl(ssoProperties.getCallbackUrl());
        return casClient;
    }

    @Bean
    public Clients casClients(CasClient casClient) {
        Clients casClients = new Clients();
        List<Client> casClientList = new ArrayList<>();
        casClientList.add(casClient);
        casClients.setClients(casClientList);
        casClients.setDefaultClient(casClient);
        return casClients;
    }

    @Bean
    public Config casConfig(Clients casClients) {
        Config config = new Config();
        config.setClients(casClients);
        return config;
    }

    public CallbackFilter casCallbackFilter(Config config) {
        CallbackFilter filter = new DefaultCallbackFilter();//filterBuilder.getCallbackFilter();
        filter.setDefaultUrl(ssoProperties.getSuccessUrl());
        filter.setConfig(config);
        return filter;
    }

    /**
     * session相关配置
     */
    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }

    @Bean
    public SimpleCookie sessionIdCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("sid");
        simpleCookie.setHttpOnly(true);
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }

    @Bean
    public CachingSessionDAO sessionDAO(SessionIdGenerator sessionIdGenerator) {
        EnterpriseCacheSessionDAO enterpriseCacheSessionDAO = new EnterpriseCacheSessionDAO();
        enterpriseCacheSessionDAO.setSessionIdGenerator(sessionIdGenerator);
        return enterpriseCacheSessionDAO;
    }

    @Bean
    public DefaultSessionManager sessionManager(CachingSessionDAO sessionDAO,
                                                SimpleCookie sessionIdCookie) {
        DefaultWebSessionManager defaultSessionManager =
                new DefaultWebSessionManager();
        defaultSessionManager.setGlobalSessionTimeout(1800000);
        defaultSessionManager.setDeleteInvalidSessions(true);
        //defaultSessionManager.setSessionValidationSchedulerEnabled(true);
        //defaultSessionManager.setSessionValidationScheduler(quartzSessionValidationScheduler);
        defaultSessionManager.setSessionDAO(sessionDAO);
        defaultSessionManager.setSessionIdCookieEnabled(true);
        defaultSessionManager.setSessionIdCookie(sessionIdCookie);
        return defaultSessionManager;
    }

    /**
     * @param defaultSecurityManager
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilter(DefaultSecurityManager defaultSecurityManager,
                                              UserPermissionService userPermissionService,
                                              Config casConfig,
                                              ObjectMapper objectMapper,
                                              ShiroFilterService shiroFilterService) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(defaultSecurityManager);
        shiroFilterFactoryBean.getFilters().put("sysUserFilter", new SysUserFilter(userPermissionService, objectMapper));
        shiroFilterFactoryBean.getFilters().put("user", new UserFilter());
        shiroFilterFactoryBean.getFilters().put("cas", casCallbackFilter(casConfig));
        shiroFilterFactoryBean.getFilters().put("sloLogout", new SingleSignOutFilter());
        //拦截器.
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/**/*.js", "anon");
        filterChainDefinitionMap.put("/**/*.css", "anon");
        filterChainDefinitionMap.put("/**/*.png", "anon");
        filterChainDefinitionMap.put("/**/*.jpg", "anon");
        //具体业务实现方负责增加的过滤器
        shiroFilterService.loadExtendShiroFileter(filterChainDefinitionMap);
        filterChainDefinitionMap.put("/rpcUserPermissionService", "anon");
        filterChainDefinitionMap.put("/cas", "cas,sloLogout");
        filterChainDefinitionMap.put("/logout", "sloLogout");
        filterChainDefinitionMap.put("/loginout", "logout");
        filterChainDefinitionMap.put("/**", "user,sysUserFilter");
        //filterChainDefinitionMap.put("/**", "anon");

        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl(ssoProperties.getLoginUrl());
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl(ssoProperties.getSuccessUrl());

        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }

    @ConditionalOnMissingBean(name = "shiroFilterService")
    @Bean
    public ShiroFilterService shiroFilterService(){
        return new DefaultShiroFilterServiceImpl();
    }
    @Bean
    public ObjectMapper ObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
    }
    @ConditionalOnMissingBean(name = "userPermissionService")
    @Bean(name = "userPermissionService")
    public HessianProxyFactoryBean initRmiProxyFactoryBean() {
        HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
        factory.setServiceUrl(new StringBuffer(ssoProperties.getRemoteUrl())
                .append("/rpcUserPermissionService").toString());
        factory.setServiceInterface(UserPermissionService.class);
        return factory;

    }


}