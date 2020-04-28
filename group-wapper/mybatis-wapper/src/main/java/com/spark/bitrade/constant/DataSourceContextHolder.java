package com.spark.bitrade.constant;

import lombok.extern.slf4j.Slf4j;

/***
 * 本地线程，数据源上下文
 * @author yangch
 * @time 2018.06.19 18:43
 */
@Slf4j
public class DataSourceContextHolder {
	//private static Logger log = LoggerFactory.getLogger(DataSourceContextHolder.class);
	
	//线程本地环境
	private static final ThreadLocal<String> local = new ThreadLocal<String>();

    public static ThreadLocal<String> getLocal() {
        return local;
    }

    /**
     * 读库
     */
    public static void setRead() {
        local.set(DataSourceType.read.getType());
        //log.info("数据库切换到读库...");
    }

    /**
     * 写库
     */
    public static void setWrite() {
        local.set(DataSourceType.write.getType());
        //log.info("数据库切换到写库...");
    }

    public static String getReadOrWrite() {
        return local.get();
    }
    
    public static void clear(){
    	local.remove();
    }
}
