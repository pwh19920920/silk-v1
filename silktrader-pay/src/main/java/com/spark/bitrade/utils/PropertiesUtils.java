/**
 * Copyright:   北京互融时代软件有限公司
 * @author:      Liu Shilei
 * @version:      V1.0 
 * @Date:        2015年11月4日 下午1:22:49
 */
package com.spark.bitrade.utils;

import java.io.FileReader;
import java.io.InputStream;
import java.util.*;

/**
 * 读取配置文件
 * @author shenzucai
 * @time 2018.07.01 15:02
 */
public class PropertiesUtils {

	public static Properties PAY = null;
	
	static {
		PAY = new Properties();
		try {
			PAY.load(new FileReader(PropertiesUtils.class
					.getClassLoader()
					.getResource("application.properties")
					.getPath()));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
