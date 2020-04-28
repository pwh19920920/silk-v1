package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BusinessErrorMonitorType;
import com.spark.bitrade.constant.BusinessModule;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/***
 * 业务异常告警表
  *  备注：仅记录必须要重新处理的业务异常
  *
 * @author yangch
 * @time 2018.06.05 13:29
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessErrorMonitor {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 业务模块（如：币币交易）
     */
    /*@Enumerated(EnumType.ORDINAL)
    private BusinessModule module;*/

    //业务类型描述（如：枚举，成交明细）
    @Enumerated(EnumType.ORDINAL)
    private BusinessErrorMonitorType type;

    //原始异常描述
    @Column(columnDefinition = "varchar(4096) comment '异常描述'")
    private String errorMsg;

    //输入参数数据，如果参数是对象为json序列化的结果
    @Column(columnDefinition = "varchar(4096) comment '输入参数数据'")
    private String inData;

    //异常时间
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //是否处理(0=未处理，1=已处理)
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum maintenanceStatus;

    //处理结果描述
    @Column(columnDefinition = "varchar(4096) comment '处理结果描述'")
    private String maintenanceResult;

    //处理人员ID，管理后端admin表
    private Long maintenanceId;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date  maintenanceTime;

    //处理内容描述
    @Column(columnDefinition = "varchar(4096) comment '处理内容描述'")
    private String maintenanceDescription;
}
