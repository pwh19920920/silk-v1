package com.spark.bitrade.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("springContextUtil")
//@Order
@Slf4j
public class SpringContextUtil implements ApplicationContextAware{

	private static ApplicationContext applicationContext = null;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if(SpringContextUtil.applicationContext == null){
			SpringContextUtil.applicationContext = applicationContext;
		}
		
	}

	public static ApplicationContext getApplicationContext() {
		/*boolean flag = false;
		int count = 0;
		do{
			if(applicationContext!=null){
				flag = true;
			} else {
				try {
					log.info("等待applicationContext={},count={}",applicationContext,count++);
					TimeUnit.MILLISECONDS.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}while (!flag);*/
		return applicationContext;
	}

	
	public static Object getBean(String name){
		return getApplicationContext().getBean(name);
	}
	
	public static <T> T getBean(Class<T> clazz){
		return getApplicationContext().getBean(clazz);
	}
}
