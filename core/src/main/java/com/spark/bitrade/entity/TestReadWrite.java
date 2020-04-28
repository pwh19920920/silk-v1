package com.spark.bitrade.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/***
 * 读写分离测试实体类
  *
 * @author yangch
 * @time 2018.06.20 11:51
 */

@Data     //生成getter和setter
@Entity  //jpa需要，会自动生成表，没有启动会报错
//@Table   //可以不需要
public class TestReadWrite implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	private String userName;
}
