# [maven] #
## 【1、私服地址】 ##
```
- 开发环境地址(内网使用)：
    http://172.16.0.88:8081/repository/maven-public/
    admin/de@kingP@ssw0rd

- 公网地址（不可上传jar包）：
    http://211.83.206.252:9644/nexus/content/groups/public/
    admin/admin123
```

## 【2、settings.xml】 ##
maven settings文件配置参考：
```
<?xml version="1.0" encoding="UTF-8"?>

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <!-- 配置本地仓库路径 -->
  <localRepository>E:\maven_workspace</localRepository>
  <servers>
    <!-- 发布私服的用户和密码 -->
    <server>
      <id>releases</id>
      <username>admin</username>
      <password>de@kingP@ssw0rd</password>
    </server>
    <server>
      <id>snapshots</id>
      <username>admin</username>
      <password>de@kingP@ssw0rd</password>
    </server>
  </servers>

  <mirrors>
    <mirror>
        <id>deaking</id>
        <mirrorOf>*</mirrorOf>
        <name>deaking</name>
        <url>http://172.16.0.88:8081/repository/maven-public/</url>
    </mirror> 
  </mirrors>
  
  <profiles>

    <profile>
      <id>deaking</id>
      <!--所有请求均通过镜像 -->
      <repositories>
        <repository>
          <id>central</id>
          <url>http://central.maven.org/maven2/</url>
          <releases>
            <updatePolicy>always</updatePolicy>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <updatePolicy>always</updatePolicy>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>central</id>
          <url>http://central.maven.org/maven2/</url>
          <releases>
            <updatePolicy>always</updatePolicy>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <updatePolicy>always</updatePolicy>
            <enabled>true</enabled>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>

<activeProfiles>
    <activeProfile>deaking</activeProfile>
</activeProfiles>

</settings>
```

## 【3、pom.xml】 ##
pom文件中添加如下发布地址：
```
<distributionManagement>
    <repository>
        <id>releases</id>
        <name>maven-releases</name>
        <url>http://172.16.0.88:8081/repository/maven-releases</url>
    </repository>
    <snapshotRepository>
        <id>snapshots</id>
        <name>maven-snapshots</name>
        <url>http://172.16.0.88:8081/repository/maven-snapshots</url>
    </snapshotRepository>
</distributionManagement>
```


 

