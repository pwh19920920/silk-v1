package com.spark.bitrade.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * json解析util
 * @version  0.0.1
 * @author   谭思涛
 * @date     2017-2-7 上午10:45:55
 */
public class ResolveJson {
	private Logger logger = Logger.getLogger(ResolveJson.class);
	public static void main(String[] srgs){
		ResolveJson rj =new ResolveJson();
//		{"status":"1","data":{"account":"059106014751","bdCode":50,"city":"010086002004","cityName":"福州市","province":"010086002","provinceName":"湖南省","operator":null}}
		String res= "{\"status\":\"1\",\"data\":{\"account\":\"059106014751\",\"bdCode\":50,\"city\":\"010086002004\",\"cityName\":\"福州市\",\"province\":\"010086002\",\"provinceName\":\"湖南省\",\"operator\":null}}";
		String data = rj.getVal(res, "data");
		System.out.println(rj.getVal(data, "account"));
//		JSONObject json = JSONObject.parseObject(res);
//		JSONObject  data = json.getJSONObject("data");
//		System.out.println(data.getString("account"));
	}
	/**
	 * 
	 * @param res 传入jsonobject字符串
	 * @param key 通过key得到value
	 * @return
	 */
public String getVal(String res, String key)
	{
	String val = "";
		try{
			JSONObject json = JSONObject.fromObject(res);
			val = json.getString(key);
		}
		catch (Exception e) 
		{
			// TODO: handle exception
			logger.error("====================没有找到" + key + "属性==========================");
			return "";
		}
	//	System.out.println("val:" + val);
		 return val;	
	}
	
/**
 * <p>描述：从一个JSON数组得到一个java对象集合 </p>
 * <p>日期：2014-12-2 下午2:34:52 </p>
 * @param jsonString 要转换的json字符串
 * @param clazz 转换的目标对象
 * @return 转换成功的对象
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public List getObjectList(String jsonString, Class clazz) {
	try 
	{
		JSONArray array = JSONArray.fromObject(jsonString);
		List list = new ArrayList();
		for (Iterator iter = array.iterator(); iter.hasNext();) 
		{
			JSONObject jsonObject = (JSONObject) iter.next();
			list.add(JSONObject.toBean(jsonObject, clazz));
		}
		return list;
	} 
	catch (Exception e) 
	{
		// TODO: handle exception
		logger.error("=====================转换为json对象数组失败===========================", e);
		return null;
	}
	
	
}

/**
 * 将对象转换成JSON
 * @param obj
 * @return
 * @throws Exception
 */
public  String ObjectToJson(Object obj) throws Exception {
	ObjectMapper mapper = new ObjectMapper();
	String json = mapper.writeValueAsString(obj);
	return !"".equals(json) ? json : "{}";
}
}
