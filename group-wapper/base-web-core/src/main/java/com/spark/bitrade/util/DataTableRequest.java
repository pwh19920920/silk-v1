package com.spark.bitrade.util;

import com.sparkframework.lang.Convert;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


public class DataTableRequest {
	private int draw = 1;
	private int start;
	private int length;
	public Map<String,DataColumn> columns;
	
	public DataTableRequest(){
		start = 0;
		length = 10;
	}
	
	public DataTableRequest(int start, int length){
		this.start = start;
		this.length = length;
	}
	
	public int getDraw() {
		return draw;
	}
	public int getStart() {
		return start;
	}
	public int getLength() {
		return length;
	}
	public void setDraw(int draw) {
		this.draw = draw;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public void setLength(int length) {
		this.length = length;
	}
	
	public DataColumn addColumn(String name,String searchValue){
		DataColumn column = new DataColumn();
		column.setData(name);
		column.setName(name);
		column.setSearchable(true);
		column.setOrderable(true);
		column.setSearchValue(searchValue);
		column.setSearchRegex(searchValue);
		columns.put(name, column);
		return column;
	}
	
	public Map<String,DataColumn> parseRequest(HttpServletRequest request){
		//解析列
		Map<String,DataColumn> columns = new HashMap<String,DataColumn>();
		int index = 0;
		while(true){
			String data = request.getParameter("columns["+index+"][data]");
			if(StringUtils.isNotBlank(data)){
				DataColumn column = new DataColumn();
				column.setData(data);
				column.setName(request.getParameter("columns["+index+"][name]"));
				column.setSearchable(Convert.strToBoolean(request.getParameter("columns["+index+"][searchable]"), true));
				column.setOrderable(Convert.strToBoolean(request.getParameter("columns["+index+"][orderable]"), true));
				column.setSearchValue(request.getParameter("columns["+index+"][search][value]"));
				column.setSearchRegex(request.getParameter("columns["+index+"][search][regex]"));
				columns.put(data, column);
			}
			else break;
			index ++;
		}
		this.columns = columns;
		return columns;
	}
	
	/**
	 * 获取某列的搜索值
	 * @param field
	 * @return
	 */
	public String getSearchValue(String field){
		DataColumn column = this.columns.get(field);
		if(column != null)return column.getSearchValue();
		else return null;
	}

	/**
	 * 设置搜索值
	 * @param field
	 * @param searchValue
	 */
	public void setSearchValue(String field,String searchValue){
		DataColumn column = this.columns.get(field);
		if(column != null){
			column.setSearchValue(searchValue);
		}
	}
	
	/**
	 * 判断某列的搜索值是否为空
	 * @param field
	 * @return
	 */
	public boolean isSearchNotEmpty(String field){
		return StringUtils.isNotEmpty(getSearchValue(field));
	}
	
	public class DataColumn{
		private String data;
		private String name;
		private boolean searchable;
		private boolean orderable;
		private String searchValue;
		private String searchRegex;
		
		public String getData() {
			return data;
		}
		public String getName() {
			return name;
		}
		public boolean isSearchable() {
			return searchable;
		}
		public boolean isOrderable() {
			return orderable;
		}
		public String getSearchValue() {
			return searchValue;
		}
		public String getSearchRegex() {
			return searchRegex;
		}
		public void setData(String data) {
			this.data = data;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setSearchable(boolean searchable) {
			this.searchable = searchable;
		}
		public void setOrderable(boolean orderable) {
			this.orderable = orderable;
		}
		public void setSearchValue(String searchValue) {
			this.searchValue = searchValue;
		}
		public void setSearchRegex(String searchRegex) {
			this.searchRegex = searchRegex;
		}
	}
}
