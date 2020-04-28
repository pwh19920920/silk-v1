package com.spark.bitrade.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 *  锁仓DTO
 * @author lc
 * @since 2019/12/17
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeReleaseLockRequestDTO {

    private static final long serialVersionUID = -7151685362718045801L;

    /**
     * @parm 用户id
     */
    @NotNull(message = "memberId不能为空")
    @Min(value = 1,message = "memberId异常")
    private Integer memberId;

    /**
     * @parm 代币code
     */
    @NotBlank(message = "币种不能为空")
    @ApiModelProperty(value = "币种")
    private String coinSymbol;


    /**
     * @parm  锁仓数量
     */
    @NotBlank(message = "锁仓数量不能为空")
    @ApiModelProperty(value = "锁仓数量")
    private String  lockAmount;

    /**
     * @parm  充值流水记录id
     */
    @NotBlank(message = "充值流水记录id不能为空")
    @ApiModelProperty(value = "记录关联的充值流水记录id")
    private String refId;

    /**
     *
     * @parm 锁仓类型 1 新用户注册赠送锁仓
     * @return
     */
    private String type;


    @Override
    public String toString() {
        return "ReleaseLockRequestDTO{" +
                "memberId=" + memberId +
                ", coinSymbol='" + coinSymbol + '\'' +
                ", lockAmount='" + lockAmount + '\'' +
                ", refId='" + refId + '\'' +
                '}';
    }

}
