package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.PromotionLevel;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 推荐用户dto
 * @author tansitao
 * @time 2018/8/18 14:31 
 */
@Data
public class PromotionMemberDTO {
    private Date createTime;
    private String username;
    private PromotionLevel level;
    private Long memberId;
}
