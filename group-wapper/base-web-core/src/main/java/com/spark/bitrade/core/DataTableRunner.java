package com.spark.bitrade.core;


import com.spark.bitrade.util.DataTable;
import com.spark.bitrade.util.DataTableRequest;

public interface DataTableRunner {
	public DataTable run(DataTableRequest params) throws Exception;
}
