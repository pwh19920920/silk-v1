package com.spark.bitrade.dto;

import com.spark.bitrade.entity.Agreement;
import lombok.Getter;
import lombok.Setter;

/**
 * 上币中心 DTO
 *
 * @author zhongxj
 * @time 2019.08.27
 */
@Setter
@Getter
public class AgreementDto {
    /**
     * 展示顺序
     */
    private Integer displayOrder;
    /**
     * 协议内容
     */
    private Agreement agreement;
}
