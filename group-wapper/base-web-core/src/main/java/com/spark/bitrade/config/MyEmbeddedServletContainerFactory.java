package com.spark.bitrade.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.stereotype.Component;

/**
 * 内嵌tomcat优化配置
 * @author shenzucai
 * @time 2018.06.22 07:53
 */
@Component
public class MyEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory {


    private Logger logger = LoggerFactory.getLogger(MyEmbeddedServletContainerFactory.class);
    @Override
    protected void customizeConnector(Connector connector)
    {

        super.customizeConnector(connector);
        logger.info("进入内嵌tomcat优化设置");
        Http11NioProtocol protocol = (Http11NioProtocol)connector.getProtocolHandler();
        logger.info("进行优化前tomcat的默认配置 连接数配置{} 最大线程数{} 最大连接数配置{}"
                ,protocol.getConnectionCount()
                ,protocol.getMaxThreads()
                ,protocol.getMaxConnections());

        //设置最大连接数
        protocol.setMaxConnections(20000);
        //设置最大线程数
        protocol.setMaxThreads(2000);
        protocol.setConnectionTimeout(300000);

        logger.info("进行优化后tomcat的配置 连接数配置{} 最大线程数{} 最大连接数配置{}"
                ,protocol.getConnectionCount()
                ,protocol.getMaxThreads()
                ,protocol.getMaxConnections());
    }
}
