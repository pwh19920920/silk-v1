```
@author: yangch
@date  : 2018-11-06
```

### 模块说明 ###
```
模块名称：config-server（提供分布式配置中心服务）
基本功能：
端口号：7001
依赖服务：无

```
### 一：服务提供的配置说明 ###
#### native方式配置提供配置 ####
```
spring.profiles.active=native
#指定配置文件的位置
spring.cloud.config.server.native.searchLocations=file:F:/properties/
```
说明：properties目录下存放properties文件，格式{applicationName}-{profile}.properties。
   applicationName为spring.application.name，profile可以为dev/test/prod等。
   eg：service-exchange-vip-api-dev.properties
#### git方式配置提供配置 ####
```
# git管理配置
spring.cloud.config.server.git.uri=http://172.16.0.99/spring-cloud-config/trade-platform-config.git
spring.cloud.config.server.git.searchPaths=config-center/{application}
spring.cloud.config.server.git.username=username
spring.cloud.config.server.git.password=password
```
说明：在config-center目录下按‘spring.application.name’名称来创建目录，再改目录下放置配置文件。
     如application-dev.properties、application-prod.properties等
 
### 二：服务调用说明 ###
#### pom.xml配置文件 ####
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<!-- 提供配置刷新 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
#### 添加bootstrap.properties配置文件 ####
eg：
```
#应用服务名称
spring.application.name=service-exchange-vip-api
#应用端口
server.port=6015

#url方式调用spring cloud config
#spring.cloud.config.uri=http://localhost:7001/
#通过服务名称方式调用 spring cloud config（推荐）
spring.cloud.config.discovery.enabled=true
spring.cloud.config.discovery.service-id=config-server
#分支名称
spring.cloud.config.label=master
#多环境 dev/debug/test/prod
spring.cloud.config.profile=dev
```
注意：上面这些属性必须配置在bootstrap.properties中，config部分内容才能被正确加载。
因为config的相关配置会先于application.properties，而bootstrap.properties的加载也是先于application.properties。

#### 配置刷新 ####
使用@RefreshScope注解来刷新配置
eg：
```
@RefreshScope
@RestController
public class TestController {
    @Value("${from}")
    private String from;

    @RequestMapping("/from")
    public String from() {
        return this.from;
    }
}
```
使用spring-boot-starter-actuator提供的refresh接口来刷新配置，或是使用spring cloud bus来刷新配置
eg：http://localhost:6015/exchange-vip/refresh