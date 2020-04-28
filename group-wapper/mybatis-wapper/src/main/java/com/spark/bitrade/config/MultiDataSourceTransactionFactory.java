//package com.spark.bitrade.config;
//
//import org.apache.ibatis.session.TransactionIsolationLevel;
//import org.apache.ibatis.transaction.Transaction;
//import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
//
//import javax.sql.DataSource;
//
///**
// * @author shenzucai
// * @time 2018.06.24 00:43
// */
//public class MultiDataSourceTransactionFactory extends SpringManagedTransactionFactory {
//    @Override
//    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
//        return new MultiDataSourceTransaction(dataSource);
//    }
//}