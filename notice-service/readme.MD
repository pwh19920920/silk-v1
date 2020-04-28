
### 
    需要 ucenter memeberInfo 接口
    需要 更新网关服务
    
    修改极光配置信息
    
    jpush.APP_KEY=cc483aaedd8aa92f5d2b212a
    #8ddf929fdbc84b7574e2a312
    jpush.MASTER_SECRET=631266424e9550aee8bb3164
    #631266424e9550aee8bb3164
    
    /etc/rc.local
    ulimit -SHn 65535
    
    /etc/profile
    ulimit -SHn 65535
    
    /etc/security/limits.conf
    * soft nofile 65535
    * hard nofile 65535
    
    需要 设置服务器 ulimit -n 65535


###mysql 数据库
    member_mail.sql
    member_mail_content.sql

###mogodb创建index
    .createIndex({"memberId":1})


### nginx 设置
    location /notice/notice-ws {
  
            index index.html;
            client_max_body_size 5m;
            proxy_pass http://172.16.0.101:7010;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header X-Real-IP $remote_addr;
            
    }


