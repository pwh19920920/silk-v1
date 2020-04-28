package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.ActivitieType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.LockSettingStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/***
 * 锁仓活动配置表
  *
 * @author yangch
 * @time 2018.06.12 14:36
 */

@Data
@ApiModel(description = "理财宝返回大活动模型")
public class LockCoinActivitieProjectDto {
    @ApiModelProperty(value = "id",name = "id")
    private Long id;
    @ApiModelProperty(value = "活动名称",name = "name")
    private String name; //活动方案名称
    @ApiModelProperty(value = "活动类型(0:锁仓活动,1:理财锁仓,2:其它,3:SLB节点产品,4:STO锁仓" +
            ",5:STO增值计划,6:IEO活动,7:金钥匙活动,8:赋能计划)",name = "type")
    private Integer type; //活动类型

    //活动币种符号
    @ApiModelProperty(value = "活动币种",name = "coinSymbol")
    private String coinSymbol;

    //富文本的活动类容
    @ApiModelProperty(value = "活动内容",name = "description")
    private String description;
    @ApiModelProperty(value = "link",name = "link")
    private String link;
    @ApiModelProperty(value = "活动每份数量",name = "unitPerAmount")
    //活动每份数量（1表示1个币，大于1表示每份多少币）
    private BigDecimal unitPerAmount;

    //活动计划数量（币数、份数）
    @ApiModelProperty(value = "活动计划数量",name = "planAmount")
    private BigDecimal planAmount;

    //最低购买数量（币数、份数）
    @ApiModelProperty(value = "最低购买数量",name = "minBuyAmount")
    private BigDecimal minBuyAmount;

    //最大购买数量（币数、份数）
    @ApiModelProperty(value = "最大购买数量",name = "maxBuyAmount")
    private BigDecimal maxBuyAmount;
    @ApiModelProperty(value = "活动开始时间",name = "startTime")
    private Date startTime; //活动开始时间
    @ApiModelProperty(value = "活动截止时间",name = "endTime")
    private Date endTime; // 活动截止时间
    @ApiModelProperty(value = "活动状态",name = "status")
    private LockSettingStatus status; //活动状态
    @ApiModelProperty(value = "创建时间",name = "createTime")
    private Date createTime;//创建时间
    @ApiModelProperty(value = "更新时间",name = "updateTime")
    private Date updateTime; //更新时间
    @ApiModelProperty(value = "adminId",name = "adminId")
    private Long adminId; //操作人员ID
    @ApiModelProperty(value = "boughtAmount",name = "boughtAmount")
    private BigDecimal boughtAmount = BigDecimal.ZERO;
    @ApiModelProperty(value = "isOverdue",name = "isOverdue")
    private Integer isOverdue;//是否过期,0未过期,1已过期,2未开始

    //add by zyj : pc1.3.1 新增
    @ApiModelProperty(value = "imgUrl",name = "imgUrl")
    private String imgUrl;
    @ApiModelProperty(value = "titleImg",name = "titleImg")
    //add by zyj 2018.11.27 STO锁仓增值新增 start
    private String titleImg;
    @ApiModelProperty(value = "incomeImg",name = "incomeImg")
    private String incomeImg;
    @ApiModelProperty(value = "reward",name = "reward")
    private String reward;
    //add by zyj 2018.11.27 STO锁仓增值新增 end
    @ApiModelProperty(value = "锁仓天数",name = "lockDays")
    private String lockDays;
    @ApiModelProperty(value = "月利率",name = "monthRate")
    private BigDecimal monthRate;
    @ApiModelProperty(value = "活动描述",name = "briefDescription")
    private String briefDescription;

    //add by zhf 2019.10.24 活动理财通接口修改
    @ApiModelProperty(value = "锁仓天数(数组返回)",name = "lockDaysList")
    private List<Integer> lockDaysList;
    @ApiModelProperty(value = "月利率(数组返回)",name = "monthRateList")
    private List<BigDecimal> monthRateList;
    @ApiModelProperty(value = "参加活动总人数",name = "memberTotal")
    private Integer memberTotal;

    @ApiModelProperty(value = "当前时间",name = "nowTime")
    private Date nowTime;
}
