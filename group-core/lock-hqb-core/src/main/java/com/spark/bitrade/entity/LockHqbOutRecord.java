package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.HqbOutRecordStatusEnum;
import com.spark.bitrade.constant.HqbOutRecordTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 活期宝转出记录表
 *
 * @author Zhang Yanjun
 * @time 2019.04.23 11:48
 */
@Data
@TableName("lock_hqb_out_record")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockHqbOutRecord {

    /**
     * id
     */
    @TableId(type = IdType.NONE)
    private Long id;

    /**
     * 用户ID
     */
    private Long memberId;

    /**
     * 应用或渠道ID
     */
    private String appId;

    /**
     * 币种
     */
    private String coinSymbol;

    /**
     * 类型：0=立即到账、1=延迟到账（当日有收益，暂不支持）
     */
    private HqbOutRecordTypeEnum type;

    /**
     * 申请数量
     */
    private BigDecimal applyAmount;

    /**
     * 状态 0=未完成、1=已完成
     */
    private HqbOutRecordStatusEnum status;

    /**
     * 申请时间
     */
    private Long createTime;

    /**
     * 活期宝账户id
     */
    private Long walletId;
}














