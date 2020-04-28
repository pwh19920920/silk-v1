package com.spark.bitrade.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
  * 
  * @author tansitao
  * @time 2018/5/15 11:37 
  */
public class JsonUtil {

	/**
	 * 将对象转换成JSON
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static String ObjectToJson(Object obj) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(obj);
		return !"".equals(json) ? json : "{}";
	}
	
	public static  Object getObject(String jsonString, Class clazz, Map map)throws Exception {
		JSONObject	jsonObject = JSONObject.fromObject(jsonString);
		return JSONObject.toBean(jsonObject, clazz, map);
	}
	
	/**
	 * 将JSON转换成对象
	 * @param json
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static  Object getObjectFromJson(String json, Class<?> clazz) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		Object obj = null;
		try {
			obj = mapper.readValue(json, clazz);
		} catch (Exception e) {
			throw new Exception("转换异常");
		}
		return obj;
	}
	
	
	/**
	 * 将JSON转换成对象
	 * @param io
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static <T> T getObjectFromJsonReader(Reader io, Class<T> clazz) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		T obj = null;
		try {
			obj = (T) mapper.readValue(io, clazz);
		} catch (Exception e) {
			throw new Exception("转换异常");
		}
		return obj;
	}

	/**
	 * <p>描述：从一个JSON数组得到一个java对象集合 </p>
	 * <p>日期：2014-12-2 下午2:34:52 </p>
	 * @param jsonString 要转换的json字符串
	 * @param clazz 转换的目标对象
	 * @return 转换成功的对象
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getObjectList(String jsonString, Class clazz) {
		JSONArray array = JSONArray.fromObject(jsonString);
		List list = new ArrayList();
		for (Iterator iter = array.iterator(); iter.hasNext();) {
			JSONObject jsonObject = (JSONObject) iter.next();
			list.add(JSONObject.toBean(jsonObject, clazz));
		}
		return list;
	}
}
