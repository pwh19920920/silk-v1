package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 游戏支付记录
 *
 * @author archx
 * @time 2019/4/25 10:44
 */
@Data
@Builder
@TableName("h5game_pay_record")
@NoArgsConstructor
@AllArgsConstructor
public class H5GamePayRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;      // 会员标识
    private String mobile;      // H5平台身份标识，默认为会员推荐码
    private BigDecimal amount;  // 支付数额
    private int direction;      // H5GameDirection
    private int state;          // H5GameRecordState
    private int errCode;        // 错误码
    private String errMsg;      // 错误消息
    private String remark;      // 备注
    private Long fpId;          // FastPayRecord ID
    private Long refId;         // 引用标识，只有退换处理后才具有

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
