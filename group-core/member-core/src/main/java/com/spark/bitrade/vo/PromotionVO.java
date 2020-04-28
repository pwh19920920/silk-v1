package com.spark.bitrade.vo;


import com.github.pagehelper.PageInfo;
import com.spark.bitrade.dto.PromotionMemberDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 推荐信息
 * @author tansitao
 * @time 2018/12/17 11:49 
 */
@Data
public class PromotionVO implements Serializable{

    //一级推荐人数量
    private int oneInviteeNum ;

    //被推荐人信息
    private PageInfo<PromotionMemberDTO> pageInfo;

}
