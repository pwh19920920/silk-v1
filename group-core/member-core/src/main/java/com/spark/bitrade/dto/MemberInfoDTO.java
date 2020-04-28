package com.spark.bitrade.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.Date;

/**
 * @author lingxing
 * @time 2018.08.07 11:02
 */
@Data
public class MemberInfoDTO {
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 谷歌key
     */
    private String googleKey;

    /**
     * 是否绑定
     */
    private int googleState;
    /**
     * 谷歌绑定时间
     */
    private Date googleDate;
    /**
     * 姓名
     */
    private String realName;
    /**
     * 身份证号码
     */
    private String idNumber;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号码
     */
    private String mobilePhone;
    /**
         * 地址
     */
    @Embedded
    private Location location;
    /**
     * 用户等级
     */
    @Enumerated(EnumType.ORDINAL)
    private MemberLevelEnum memberLevel;
    /**
     * 所有只有两种状态的都可使用,账号状态.<br>
     */
    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * 银行卡信息
     */
    @Embedded
    private BankInfo bankInfo;


    /**
     * 推广码
     */
    private String promotionCode;
    /**
     * 实名认证状态
     */
    @Enumerated(EnumType.ORDINAL)
    private RealNameStatus realNameStatus = RealNameStatus.NOT_CERTIFIED;

    /**
     * 认证商家状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CertifiedBusinessStatus certifiedBusinessStatus = CertifiedBusinessStatus.NOT_CERTIFIED;

    /**
     * 头像
     */
    private String avatar;

}
