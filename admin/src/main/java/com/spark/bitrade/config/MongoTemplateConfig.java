package com.spark.bitrade.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import lombok.Data;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据配置文件创建MongoDbFactory
 *
 */
@Data
public abstract class MongoTemplateConfig {


    // 变量名跟配置的参数对应
    private List<String> hosts;
    String database, username, password;
    private List<Integer> ports;
    private String authenticationDatabase;
    private Integer minConnectionsPerHost;
    private Integer connectionsPerHost;



    public MongoDbFactory mongoDbFactory(String databaseName) throws Exception {
        if(databaseName != null){
            database =databaseName;
        }
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.connectionsPerHost(minConnectionsPerHost);
        builder.connectionsPerHost(connectionsPerHost);

        MongoClientOptions mongoClientOptions = builder.build();

        // MongoDB地址列表
        List<ServerAddress> serverAddresses = new ArrayList<>();
        for (String host :hosts) {
            Integer index = hosts.indexOf(host);
            Integer port = ports.get(index);

            ServerAddress serverAddress = new ServerAddress(host, port);
            serverAddresses.add(serverAddress);
        }
        System.out.println("serverAddresses:" + serverAddresses.toString());

        // 连接认证
        List<MongoCredential> mongoCredentialList = new ArrayList<>();
        if (username != null) {
            mongoCredentialList.add(MongoCredential.createScramSha1Credential(
                    username,
                    authenticationDatabase != null ? authenticationDatabase : database, password.toCharArray()));

        }

        // 无认证的初始化方法
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return new SimpleMongoDbFactory(new MongoClient(serverAddresses), database);
        }

        MongoClient mongoClient = new MongoClient(serverAddresses, mongoCredentialList, mongoClientOptions);
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClient, database);
        return  mongoDbFactory;
    }


    /*
     * Factory method to create the MongoTemplate
     */
    abstract public MongoTemplate getMongoTemplate() throws Exception;
}
