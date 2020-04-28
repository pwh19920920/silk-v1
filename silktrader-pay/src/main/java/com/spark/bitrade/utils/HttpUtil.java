package com.spark.bitrade.utils;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {

	private static Logger logger = Logger.getLogger(HttpUtil.class);

	private static final Pattern p = Pattern.compile("\\s*|\t|\r|\n");

	public static String httpURLConnectionPOST(String postUrl, String base64) {
		try {
			URL url = new URL(postUrl);
			System.out.println(url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			connection.connect();
			DataOutputStream dataout = new DataOutputStream(connection.getOutputStream());

			Matcher m = p.matcher(base64);
			String after = m.replaceAll("");
			System.out.println(after);
			dataout.writeBytes(after);
			dataout.flush();
			dataout.close(); 
			BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder sb = new StringBuilder(); 

			while ((line = bf.readLine()) != null) {
				line = new String(line.getBytes(), "utf-8");
				sb.append(line);
			}
			bf.close(); 
			connection.disconnect(); 
			return sb.toString();

		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	public static String httpURLConectionGET(String postUrl) {  
        try {  
            URL url = new URL(postUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
            connection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));  
            String line;  
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);  
            }  
            br.close();
            connection.disconnect();
            return sb.toString();
        } catch (Exception e) {  
        	logger.error(e); 
            return "失败";
        }  
    } 
	
	public static String httpURLConectionPOST(String postUrl) {
		try {
			URL url = new URL(postUrl);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.connect();
			DataOutputStream dataout = new DataOutputStream(connection.getOutputStream());
			dataout.flush();
			dataout.close();
			BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder sb = new StringBuilder();

			while ((line = bf.readLine()) != null) {
				line = new String(line.getBytes(), "utf-8");
				sb.append(line);
			}
			bf.close(); 
			connection.disconnect();
			return sb.toString();

		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
}
