package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingConfigStatus;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 竞猜活动
  * @author tansitao
  * @time 2018/9/13 11:02 
  */
@Data
@Builder
public class PriceRangeVo {

    private Long id;

    //id,期数id
    private Long periodId;

    //序号
    private Integer seqId;

    //组名
    private String groupName;

    //开始范围
    private BigDecimal beginRange;

    //结束范围
    private BigDecimal endRange;

    //排序
    private Integer orderId;

    //数量
    private Integer number;
}
