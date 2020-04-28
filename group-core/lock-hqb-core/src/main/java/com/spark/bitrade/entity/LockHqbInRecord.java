package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.HqbInRecordStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 活期宝划转入记录表(LockHqbInRecord)实体类
 *
 * @author dengdy
 * @since 2019-04-23 15:41:23
 */
@Data
@TableName("lock_hqb_in_record")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockHqbInRecord implements Serializable {

    private static final long serialVersionUID = 536923268638452740L;

    /**
     * 转入记录表id
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
     * 申请数量
     */
    private BigDecimal applyAmount;

    /**
     * 申请时间
     */
    private Long applyTime;

    /**
     * 确认时间/收益生效时间
     */
    private Long planEffectiveDate;

    /**
     * 状态(0:未确认,1:已确认,2:确认失败)
     */
    private HqbInRecordStatusEnum status;

    /**
     * 活期宝账户id
     */
    private Long walletId;

}