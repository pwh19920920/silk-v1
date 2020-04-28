package com.spark.bitrade.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author rongyu
 * @description
 * @date 2017/12/29 14:14
 */
@Data
public class CoinAbstractDto {
    @Id
    @NotBlank(message = "币名称不得为空")
    @Excel(name = "货币", orderNum = "1", width = 20)
    private String name;
    /**
     * 中文
     */
    @Excel(name = "中文名称", orderNum = "1", width = 20)
    @NotBlank(message = "中文名称不得为空")
    private String nameCn;
    /**
     * 缩写
     */
    @Excel(name = "单位", orderNum = "1", width = 20)
    @NotBlank(message = "单位不得为空")
    private String unit;
    /**
     * 排序
     */
    private int sort;
    
}
