package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.spark.bitrade.constant.IncomeType;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.SettlementType;
import com.spark.bitrade.entity.UnlockCoinDetail;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lingxing
 * @time 2018.07.19 12:01
 */
@Data
public class LockCoinDetailDto {
    private String userName;
    private String name;
    private String lockDays;
    private Long id ;
    List<UnlockCoinDetail>unlockCoinDetailList;
    private long memberId;
    private LockType type;
    private String coinUnit;
    private Long refActivitieId;
    private  BigDecimal totalAmount;
    private  BigDecimal lockPrice;
    private BigDecimal remainAmount;
    private Date lockTime;
    private Date planUnlockTime; //为null，表示解锁时间未知
    private BigDecimal planIncome;
    private LockStatus status;
    private Date unlockTime;
    private Date cancleTime;
    private BigDecimal usdtPriceCNY;
    private BigDecimal totalCNY;
    private String remark;
}
