package com.spark.bitrade.entity;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysHelpClassification;
import lombok.Builder;
import lombok.Data;

import java.util.Date;


/**
  * 系统帮助返回类
  * @author tansitao
  * @time 2018/6/4 16:39 
  */
@Data
@Builder
public class SystemHelp {
    private Long id;

    private String title;

    private String imgUrl = "";

    private Date createTime;

    private CommonStatus status = CommonStatus.NORMAL;

    private SysHelpClassification sysHelpClassification;

    /**
     * 类型不为二维码时有效，为新手入门，充值指南，交易指南等的具体内容
     */

    private String author = "admin";

    private int sort = 0 ;
}
