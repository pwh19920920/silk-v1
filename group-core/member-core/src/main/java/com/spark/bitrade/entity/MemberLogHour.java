package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;


@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberLogHour {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    private Long memberId ;

    @JsonFormat(timezone = "GMX+8",pattern = "yyyy-MM-dd")
    private Date date ;

    @Max(value = 23)
    @Min(value = 0)
    private int hour ;

    /**
     * 1小时内的注册人数
     */
    private int registrationNum ;

    /**
     * 1小时内的实名认证人数
     */
    private int applicationNum ;

    /**
     * 1小时内的认证商家人数
     */
    private int bussinessNum ;
}
