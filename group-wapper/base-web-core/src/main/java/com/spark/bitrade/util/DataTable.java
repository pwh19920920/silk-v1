package com.spark.bitrade.util;

import java.util.List;
import java.util.Map;

/**
 * 前端datatable插件所需要的信息
 * @author yanqizheng
 *
 */
public class DataTable {
	private int draw;
	//记录总数
	private int recordsTotal;
	private int recordsFiltered;
	private List<?> data;
	private List<String> titles;
	private Map<String,String> addition;
	
	public List<String> getTitles() {
		return titles;
	}
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	private String error;
	public int getDraw() {
		return draw;
	}
	public int getRecordsTotal() {
		return recordsTotal;
	}
	public int getRecordsFiltered() {
		return recordsFiltered;
	}
	public List<?> getData() {
		return data;
	}
	public String getError() {
		return error;
	}
	public void setDraw(int draw) {
		this.draw = draw;
	}
	public void setRecordsTotal(int recordsTotal) {
		this.recordsTotal = recordsTotal;
	}
	public void setRecordsFiltered(int recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}
	public void setData(List<?> data) {
		this.data = data;
	}
	public void setError(String error) {
		this.error = error;
	}
	public Map<String, String> getAddition() {
		return addition;
	}
	public void setAddition(Map<String, String> addition) {
		this.addition = addition;
	}
	
}
