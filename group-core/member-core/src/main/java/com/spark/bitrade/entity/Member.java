package com.spark.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 会员用户
 *
 * @author Zhang Jinwei
 * @date 2018年01月02日
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member implements Serializable{

    @Excel(name = "会员编号", orderNum = "1", width = 20)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

//    @JsonIgnore
    private String salt;

    @Excel(name = "会员用户名", orderNum = "1", width = 20)
    @NotBlank(message = "用户名不得为空")
    @Column(unique = true)
    private String username;

    /**
     * 登录密码
     */
//    @JsonIgnore
    @NotBlank(message = "密码不得为空")
    private String password;

    @Excel(name = "是否缴纳保证金", orderNum = "1", width = 20)
    private String margin;

    @Excel(name = "googleKey", orderNum = "1", width = 20)
    private String googleKey;

    @Excel(name = "googleState", orderNum = "1", width = 20)
    /**
     * 绑定状态，1：绑定，0：未绑定
     */
    private int googleState;

    @Excel(name = "googleDate", orderNum = "1", width = 20)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date googleDate;
    /**
     * 交易密码
     */
//    @JsonIgnore
    private String jyPassword;

    @Excel(name = "会员真实姓名", orderNum = "1", width = 20)
    private String realName;

    /**
     * 身份证号码
     */
    @Excel(name = "身份证号码", orderNum = "1", width = 20)
    private String idNumber;

    @Excel(name = "邮箱", orderNum = "1", width = 20)
    @Column(unique = true)
    private String email;


    @Excel(name = "手机号", orderNum = "1", width = 25)
    @Column(unique = true)
    private String mobilePhone;

    @Embedded
    private Location location;

    @Enumerated(EnumType.ORDINAL)
    private MemberLevelEnum memberLevel;

    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;

    @Excel(name = "注册时间", orderNum = "1", width = 20)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registrationTime;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    private String token;


    /**
     * 交易次数
     */
    private int transactions = 0;

    @Embedded
    private BankInfo bankInfo;

    @Embedded
    private Alipay alipay;

    @Embedded
    private WechatPay wechatPay;

    /**
     * 申诉次数
     */
    private int appealTimes = 0;
    /**
     * 胜诉次数
     */
    private int appealSuccessTimes = 0;

    /**
     * 邀请者ID
     */
    private Long inviterId;

    /**
     * 推广码
     */
    private String promotionCode;

    private int firstLevel = 0;
    private int secondLevel = 0;
    private int thirdLevel = 0;

    @JoinColumn(name = "local")
    @ManyToOne
    private Country country;
    /**
     * 实名认证状态
     */
    @Excel(name = "实名认证状态", orderNum = "1", width = 20)
    @Enumerated(EnumType.ORDINAL)
    private RealNameStatus realNameStatus = RealNameStatus.NOT_CERTIFIED;

    /**
     * 登录次数
     */
    private int loginCount = 0;
    /**
     * 认证商家状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CertifiedBusinessStatus certifiedBusinessStatus = CertifiedBusinessStatus.NOT_CERTIFIED;

    /**
     * 认证商家申请时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date certifiedBusinessApplyTime;

    //add by yangch 时间： 2018.05.11 原因：代码合并
    /**
     * 实名认证通过时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applicationTime;

    //add by yangch 时间： 2018.05.11 原因：代码合并
    /**
     * 商家认证审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date certifiedBusinessCheckTime;



    /**
     * 头像
     */
    private String avatar;
    /**
     * token预计过期时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date tokenExpireTime;

    //add by yangch 时间： 2018.04.24 原因：合并新增
    /**
     * 发布广告  1表示可以发布
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum publishAdvertise = BooleanEnum.IS_TRUE;

    //add by yangch 时间： 2018.04.26 原因：合并新增
    /**
     * 0 表示禁止交易
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum transactionStatus = BooleanEnum.IS_TRUE;

    //add|edit|del by  shenzucai 时间： 2018.05.29  原因：1）	新增归属区域ID字段 2）新增用户注册时的IP，记录注册时的IP start

    /**
     * 区域id
     */
    private String areaId;

    /**
     * 注册ip
     */
    private String ip;


    //add|edit|del by  shenzucai 时间： 2018.05.29  原因：1）	新增归属区域ID字段 2）新增用户注册时的IP，记录注册时的IP end


    //add by yangch 时间： 2018.05.11 原因：代码合并
    /**
     * 签到能力
     */
    //@Column(name = "sign_in_ability", columnDefinition = "bit default 1", nullable = false)
    //private Boolean signInAbility = true;

    /**
     * 证件类型（0身份证1护照2驾照）
     */
    private CertificateType certificateType;
}