package com.spark.bitrade.config;

import com.spark.bitrade.util.IdWorkByTwitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/***
  * 
  * @author yangch
  * @time 2018.11.21 13:40
  */
@Configuration
@Component
@Slf4j
public class IdWorkByTwitterConfig {
    //应用名称
    @Value("${spring.application.name}")
    private String appName;
    //路径
    @Value("${server.context-path}")
    private String contextPath;

    private static long SNOWFLAKE_WORKID = -1;
    private static long SNOWFLAKE_DATACENTERID = 0;

    @Autowired
    private DiscoveryClient discovery;


    @Bean("idWorkByTwitterSnowflake")
    public IdWorkByTwitter idWorkByTwitter(){
        //获取已经使用的workId和dataCenterId列表
        List<String> lstRegisterInfo = new ArrayList<>();
        if(StringUtils.hasText(this.appName)) {
            log.info("spring.application.name={},context-path={}", this.appName, this.contextPath);
            //根据当前应用的服务名称从Eureka获取服务的列表
            List<ServiceInstance> list = discovery.getInstances(this.appName);
            if (list != null) {
                log.info("已启动的实例节点数量={}", list.size());
                RestTemplate restTemplate = new RestTemplate();
                list.forEach(si -> {
                    try {
                        String url = si.getUri().toString();
                        if(StringUtils.hasText(contextPath) && contextPath.length()>1){
                            url+= contextPath+"/idWorkByTwitter/registerInfo";
                        }

                        String registerInfo = restTemplate.getForObject(url, String.class);
                        lstRegisterInfo.add(registerInfo);
                        log.info("idWorkByTwitter已使用信息，url={},registerInfo={}", url, registerInfo);
                    } catch (Exception ex) {
                        log.error("获取实例信息失败，详细信息{}", ex);
                    }
                });
            }
        } else {
            log.warn("spring.application.name 为空");
        }

        //数据中心ID从0开始
        for (long dataCenterId = 0; dataCenterId <32 ; dataCenterId++) {
            for (long workId = 0; workId <32 ; workId++) {
                String registerInfo = getRegisterInfo(workId, dataCenterId);
                if(!lstRegisterInfo.contains(registerInfo)) {
                    log.info("idWorkByTwitter使用信息，workId={},dataCenterId={}", workId, dataCenterId);
                    SNOWFLAKE_WORKID = workId;
                    SNOWFLAKE_DATACENTERID = dataCenterId;
                    return new IdWorkByTwitter(workId, dataCenterId);
                }
            }
        }

        log.error("idWorkByTwitter初始化失败");
        return null;
    }


    private static String getRegisterInfo(long workId , long dataCenterId){
        return new StringBuilder().append(workId).append("-").append(dataCenterId).toString();
    }

    /***
      * 获取登记使用的workId和dataCenterId
      * @author yangch
      * @time 2018.11.21 10:26 
     * @return  格式：workId-dataCenterId
     */
    public static String getRegisterInfo(){
        return getRegisterInfo(SNOWFLAKE_WORKID , SNOWFLAKE_DATACENTERID);
    }

}
