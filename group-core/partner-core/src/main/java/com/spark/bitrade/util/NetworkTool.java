package com.spark.bitrade.util;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * url网络连接util
 * @version  0.0.1
 * @author   谭思涛
 * @date     2017-2-7 上午10:29:25
 */
@SuppressWarnings("deprecation")
public class NetworkTool {
	
	private Logger logger=Logger.getLogger(this.getClass());
	/**
	 * 通过post的json形式访问接口
	 * @param strURL
	 * @param params
	 * @return
	 */
	public String postContent(String strURL, String params) {
		String result = "";
		BufferedReader in = null;
        try 
        {  
            URL url = new URL(strURL);// 创建连接  
            HttpURLConnection connection = (HttpURLConnection) url  
                    .openConnection();  
            connection.setDoOutput(true);  
            connection.setDoInput(true);  
            connection.setUseCaches(false);  
            connection.setInstanceFollowRedirects(true);  
            connection.setRequestMethod("POST"); // 设置请求方式  
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式  
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式  
            connection.connect();  
            OutputStreamWriter out = new OutputStreamWriter(  
            connection.getOutputStream(), "UTF-8"); // utf-8编码  
            out.append(params);  
            out.flush();
			out.close();
            // 读取响应  
			//判断接口返回状态
			 if (connection.getResponseCode() == 200) 
			 {  
				 	//通过BufferedReader读取返回值
			        in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));  
			        String inputLine = "";  
			        while ((inputLine = in.readLine()) != null) 
			        {  
			            result += inputLine;  
			        }  
			        logger.info("=====================接口访问成功==========================返回值：" + result);
			 }
			 else
			 {
				 logger.error("=====================接口访问异常==========================url:" + strURL + "====================params:" + params);
				 return "{\"state\":\"0\"},\"msg\":\"接口访问失败\"";
			 }
        } 
        catch (Exception e) 
        {  
        	logger.error("=====================接口访问出错==========================", e);
        	return "{\"state\":\"0\"},\"msg\":\"接口访问失败\"";
        }
        finally
        {
        	if(in != null)
        	{
        		try 
        		{
					in.close();
				} 
        		catch (IOException e) 
				{
					logger.error("=====================资源关闭出错==========================", e);
				} 
        	}
        	
        }
        return result; // 自定义错误信息  
    }  

	/**
	* 获取网址内容
	* @param url
	* @return
	* @throws Exception
	*/
	public String getContent(String url){
	    String res = "";
		HttpClient client = new DefaultHttpClient();
	    HttpParams httpParams = client.getParams();
	    //设置网络超时参数
	    HttpConnectionParams.setConnectionTimeout(httpParams, 15000);//连接超时时间
	    HttpConnectionParams.setSoTimeout(httpParams, 5000);//读取数据超时时间
	    HttpResponse response = null;
	    try 
	    {
//	    	url = URLEncoder.encode(url, "utf-8");
//	    	System.out.println(url);
	    	response = client.execute(new HttpGet(url));
		    StatusLine statusLine=response.getStatusLine();//获取请求对象中的响应行对象  
	        int responseCode=statusLine.getStatusCode();//从状态行中获取状态码  
	        if(responseCode==200)
	        {  
	        	HttpEntity entity = response.getEntity();
			    res = EntityUtils.toString(entity, "utf-8");
	//	        String res = new String(str.getBytes("ISO-8859-1"), "utf-8"); 
		        logger.info("=====================接口访问成功================url:" + url +"==========返回值：" + res);
				
	        }else
	        {
	        	logger.info("=====================接口访问失败==================url:" + url +"========返回responseCode：" + responseCode);
	        	return res;
	        }
        } 
	    catch (Exception e) 
		{
			logger.error("=====================接口访问异常==========================url:" + url, e);
			return res;
		}
	    return res;
	} 
	
	/**
	 * 判断邮件格式是否正确
	 * @param strEmail
	 * @return
	 */
	public static boolean isEmail(String strEmail) {
		String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}	
	
	/** 
     * 根据url下载文件，保存到filepath中 
     * @param url 
     * @param filepath 
     * @return 
     */  
    public static boolean download(String url, String filepath) {  
    	boolean isSuccess = true;
    	InputStream is = null;
    	 FileOutputStream fileout = null;
    	 File file = null;
        try {  
            HttpClient client = new DefaultHttpClient();  
            HttpGet httpget = new HttpGet(url);  
            HttpResponse response = client.execute(httpget);  
            
            StatusLine statusLine=response.getStatusLine();//获取请求对象中的响应行对象  
            int responseCode=statusLine.getStatusCode();//从状态行中获取状态码  
            //判断接口是否访问成功
            if(responseCode==200){
            	//获取接口返回类容
            	 HttpEntity entity = response.getEntity(); 
            	 //将返回的信息变成输入流
                 is = entity.getContent();  
                 file = new File(filepath);  
                 file.getParentFile().mkdirs();  
                 fileout = new FileOutputStream(file);  
                 /** 
                  * 根据实际运行效果 设置缓冲区大小 
                  */  
                 byte[] buffer=new byte[1024];  
                 int ch = 0;  
                 //将文件写入到指定地方
                 while ((ch = is.read(buffer)) != -1) {  
                     fileout.write(buffer,0,ch);  
                 }  
            }else{
            	isSuccess = false;
            	System.out.println("-------------------下载接口访问异常-------------------------------");
            }
           
        } catch (Exception e) {
        	isSuccess = false;
            e.printStackTrace();  
        }  finally{
        		try {
        			if(is != null){
						is.close();
        			}
        			if(fileout != null){
		        		 fileout.close(); 
		        	}
        		}
				 catch (IOException e) {
					// TODO Auto-generated catch block
					 isSuccess = false;
					e.printStackTrace();
					System.out.println("---------------------------io流关闭异常--------------------------------");
				}  
        	
        	
        }
        return isSuccess;  
    }
}

