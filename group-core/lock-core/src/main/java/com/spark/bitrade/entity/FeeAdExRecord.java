package com.spark.bitrade.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author qiliao
 * @since 2019-09-10
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FeeAdExRecord implements Serializable {

    private static final long serialVersionUID=1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @ApiModelProperty(name = "id",value = "id")
    private Long id;

    /**
     * 币种
     */
    private String coin;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 创建时间统计时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private Long memberId;

    private Integer type;

    public static final String ID = "id";

    public static final String COIN = "coin";

    public static final String FEE = "fee";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

}
