package com.spark.bitrade.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>随机数生成工具类</p>
 * @author tian.bo
 * @since 2019/3/8
 */
public class NumberUtils {
	
	/**
	 * 随机数生成
	 * @param num
	 * @return
	 */
	public static String generatorVercode(int num){
		StringBuffer sb = new StringBuffer();
		String str = "0123456789";
		for(int i=0; i<num; i++){
			int n = (int) (Math.random() * 10);
			String source = String.valueOf(str.charAt(n));
			sb.append(source);
		}
		return sb.toString();
	}
	
	/**
	 * 电话号码验证
	 * @param str
	 * @return
	 */
	public static boolean isMobile(String str) {   
        Pattern p = null;  
        Matcher m = null;  
        boolean b = false;   
        p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);  
        b = m.matches();   
        return b;  
    }  
	
	/**
	  * @Title: generatecheckCode 
	  * @Description: TODO(产生三位随机数) 
	  * @return String    返回类型
	 */
	public static String generatecheckCode3() {
	    String chars = "23456789abcdefghijkmnpqrstuvwxyzABCDEFGHIJKMNPQRSTUVWXYZ";
	    int length = 3;
		char[] rands = new char[length];
		for (int i = 0; i < rands.length; i++) {
			int rand = (int)(Math.random()*10);
			rands[i] = chars.charAt(rand);
		}
		return String.valueOf(rands);
	}

}
