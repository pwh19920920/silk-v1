package com.spark.bitrade.aop;

import com.spark.bitrade.constant.DataSourceContextHolder;
import com.spark.bitrade.constant.DataSourceType;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;


/***
 * 在service层切换数据源
 *
 * 必须在事务AOP之前执行，所以实现Ordered,order的值越小，越先执行
 * 如果一旦开始切换到写库，则之后的读都会走写库
 * @author yangch
 * @time 2018.08.20 9:09
 */

@Aspect
@EnableAspectJAutoProxy(exposeProxy=true,proxyTargetClass=true)
@Component
@ConditionalOnProperty(value = "shardingSphere.config", havingValue = "false", matchIfMissing = true)
public class DataSourceAopInService implements PriorityOrdered{

private static Logger log = LoggerFactory.getLogger(DataSourceAopInService.class);

	@Before("execution(* com.spark.bitrade.service..*.*(..)) "
			+ " && @annotation(com.spark.bitrade.annotation.ReadDataSource) ")
	public void setReadDataSourceType() {
		//如果已经开启写事务了，那之后的所有读都从写库读
		if(!DataSourceType.write.getType().equals(DataSourceContextHolder.getReadOrWrite())){
			DataSourceContextHolder.setRead();
		}
	    
	}
	
	@Before("execution(* com.spark.bitrade.service..*.*(..)) "
			+ " && @annotation(com.spark.bitrade.annotation.WriteDataSource) ")
	public void setWriteDataSourceType() {
	    DataSourceContextHolder.setWrite();
	}
    
	@Override
	public int getOrder() {
		/**
		 * 值越小，越优先执行
		 * 要优于事务的执行
		 * 在启动类中加上了@EnableTransactionManagement(order = 10) 
		 */
		return 1;
	}

}
