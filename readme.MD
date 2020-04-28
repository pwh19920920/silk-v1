## [接口文档]

## 应用端口
```
eureka-server   8761/7000
config-server   7001
ucenter-api     6001  https://silktraderdk.net/uc/healthy
otc-api         6002  https://silktraderdk.net/otc/healthy
exchange-api    6003  https://silktraderdk.net/exchange/healthy 
market          6004  https://silktraderdk.net/market/healthy
market-push     6004  https://silktraderdk.net/market/healthy   (替换market)
exchange        6005  https://silktraderdk.net/ex/healthy
wallet          6006  https://silktraderdk.net/wallet/healthy
chat            6007  https://silktraderdk.net/chat/healthy
admin           6010  https://apidkadmin.silktrader.top/admin/healthy

open-api        6009  https://silktraderdk.net/api/healthy
updating        6011  https://silktraderdk.net/updating/healthy
silktrader-pay  6014  pay

gateway-api     6600  https://silktraderdk.net/healthy
exchange-order-processor    6020  https://silktraderdk.net/exOrder/healthy
exchange-trade-processor    6021  https://silktraderdk.net/exTradeProcesser/healthy
market-processor            6022  https://silktraderdk.net/exOrder/healthy

scheduled-job   8081

exchange-vip-api            6015  https://silktraderdk.net/exchange-vip/healthy 
mocker          6025
service-lock    6026    https://silktraderdk.net/lock/healthy 
support-coin-server    6030  
ticker 6304(v2)
```


## 应用启动顺序及说明

```
0.AppEurekaServer       com.spark.bitrade.AppEurekaServer       必须启动，提供Eureka服务
1.ConfigServerApplication com.spark.bitrade.ConfigServerApplication 提供分布式配置中心服务
2.GatewayApiApplication com.spark.bitrade.GatewayApiApplication 必须启动，提供网关服务

3.ExchangeApplication   com.spark.bitrade.ExchangeApplication   币币交易模块，提供撮合服务
4.ExchangeApiApplication    com.spark.bitrade.ExchangeApiApplication    币币交易模块，提供下单、订单服务
5.MarketApplication     com.spark.bitrade.MarketApplication     币币交易模块，提供行情数据（有需要时启动，目前UC模块会依赖此模块）

6.UcenterApplication    com.spark.bitrade.UcenterApplication    必须启动，UC模块，提供登录、账户等信息（依赖market模块）

7.Otc-ApiApplication    com.spark.bitrade.ApiApplication        场外交易模块，提供c2c买卖功能
8.ChatApplication       com.spark.bitrade.ChatApplication       场外交易模块，提供聊天功能

9.WebApplication        com.spark.bitrade.WebApplication        后端管理模块，提供后端管理功能
10.JobApplication       com.spark.bitrade.JobApplication        定时任务模块，提供定时任务
11.OpenApiApplication   com.spark.bitrade.OpenApiApplication    
12.WalletApplication    com.spark.bitrade.WalletApplication     钱包模块，提供交易所和区块链的交互接口
13.UpdatingApplication  com.deaking.UpdatingApplication         系统升级模块，提供升级状态等接口
14.ockerApplication     com.spark.bitrade.mocker.MockerApplication  币币交易模块，提供从火币获取k线数据功能
15.GuessApiApplication  com.spark.bitrade.GuessApiApplication   竞猜活动
16.EmailApplication     com.spark.bitrade.EmailApplication      邮件模块，提供邮件发送功能
17.ExchangeVipApiApplication    com.spark.bitrade.ExchangeVipApiApplication 币币交易模块，提供机器人等第三方接入的接口
```


## maven私服地址 ##
公网地址（不可上传jar包）：http://211.83.206.252:9644/nexus/content/groups/public/
    admin/admin123
开发环境地址(内网使用)：http://172.16.0.88:8081/repository/maven-public/
    admin/de@kingP@ssw0rd

# 环境 #
## 联调环境 ##
### 域名说明 ###
```
172.16.0.100 www.dev.pro    前端界面地址
172.16.0.100 api.dev.pro    前端api地址
172.16.0.100 dkadmin.dev.pro        管理端界面地址
172.16.0.100 api.dkadmin.dev.pro    管理端api地址
```

### 服务端 ###
```
IP:172.16.0.100
部署的服务：
  mysql：172.16.0.100:3306
  mongodb：172.16.0.100:27017
    配置文件：/etc/mongod.conf
    启停命令：service mongod [start|stop|restart]
  zookeeper：172.16.0.100:2181
    程序路径：/usr/local/zookeeper
    启动命令：/usr/local/zookeeper/bin/zkServer.sh start 
  kafka：172.16.0.100:9092
    程序路径：/usr/local/kafka
    启动命令：/usr/local/kafka/bin/kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties
  Redis：172.16.0.100:6379
    配置文件：/etc/redis/6379.conf
    启停命令：/etc/init.d/redis_6379 [start|stop|restart]
  Nginx：172.16.0.100:80
    程序路径：/home/deploy/bin/nginx
    配置文件：/home/deploy/bin/nginx/conf/nginx.conf
    前端：/home/deploy/app/silktrade-font-web
    管理端：/home/deploy/app/silktrade-admin-web
    启动命令：/home/deploy/bin/nginx/sbin/nginx
  eureka：http://172.16.0.100:8761/eureka/
    程序路径：/home/deploy/app/cloud.jar
  调度服务端：http://172.16.0.91:8080/xxl-job-admin
    程序路径：/home/deploy/tomcat-8.0.24/webapps/xxl-job-admin.war
  应用网关程序：
    程序路径：/home/deploy/app/gateway-api.jar
```
### 应用程序 ###
```
  IP:172.16.0.101
  部署路径：/home/deploy/app
  启停命令：sbm 应用程序名称  [start 1024|stop] 
  
  应用程序：admin-api、chat、exchange-api、exchange、guess-api、market、otc-api、
           scheduled-job、ucenter-api、wallet
```
  
## 测试环境 ##
### 域名说明 ###
```
172.16.0.78 www.400.pro    前端界面地址
172.16.0.78 api.400.pro    前端api地址
172.16.0.79 dkadmin.400.pro        管理端界面地址
172.16.0.79 api.dkadmin.400.pro    管理端api地址
```

### 服务端 ###
```
IP:172.16.0.66,172.16.0.67
部署的服务：
  mysql：172.16.0.66:3306(主库),172.16.0.67:3306（从库）

IP:172.16.0.86
部署的服务：
  mongodb：172.16.0.86:27017
    程序路径：/usr/local/mongodb
    配置文件： /usr/local/mongodb/mongod.conf
    启停命令：service mongod [start|stop|restart]

IP:172.16.0.82
部署的服务：
  zookeeper：172.16.0.82:2181
    程序路径：/usr/local/zookeeper
    启动命令：/usr/local/zookeeper/bin/zkServer.sh start 
  kafka：172.16.0.82:9092
    程序路径：/usr/local/kafka
    启动命令：/usr/local/kafka/bin/kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties

IP:172.16.0.91
 Redis：172.16.0.91:6379
    配置文件：/etc/redis/6379.conf
    启停命令：/etc/init.d/redis_6379 [start|stop|restart]


IP:172.16.0.78
部署的服务：
  Nginx：172.16.0.78:80
    程序路径：/home/deploy/bin/nginx
    配置文件：/home/deploy/bin/nginx/conf/nginx.conf
    前端：/home/deploy/app/silktrade-font-web
  应用网关程序：
      程序路径：/home/deploy/app/gateway-api.jar
  火币数据抓取程序：
      程序路径：
      /home/deploy/app/mocker.jar
  应用升级服务程序：
      程序路径：
      /home/deploy/app/updating.jar

IP:172.16.0.79
部署的服务：
  Nginx：172.16.0.79:80
    管理端：/home/deploy/app/silktrade-admin-web
    启动命令：/home/deploy/bin/nginx/sbin/nginx
    
eureka：http://172.16.0.85:8761/eureka/,http://172.16.0.78:8761/eureka/
  程序路径：/home/deploy/app/cloud.jar

调度服务端：http://172.16.0.91:8080/xxl-job-admin
  -- 程序路径：/home/deploy/tomcat-8.0.24/webapps/xxl-job-admin.war
  程序路径：/home/deploy/tomcat8_trade/webapps/xxl-job-admin.war
  
```
### 应用程序 ###
```
  部署路径：/home/deploy/app
  启停命令：sbml 应用程序名称  [start 1024|stop] 
  
IP:172.16.0.79  
  
  应用程序：exchange-api、exchange-vip-api、guess-api、otc-api、
           ucenter-api、silktrader-pay、cmarket-api
IP:172.16.0.80
  应用程序：chat、market、scheduled-job、open-api、collect-supervisor
IP:172.16.0.85
  应用程序：config-server、admin-api、exchange、lock-api、wallet、eureka-server
```
 

