package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.util.DateUtil;
import lombok.Data;

import java.util.Date;

/**
 * 用于数据库查询参数类
 * @author shenzucai
 * @time 2018.05.31 15:23
 */
@Data
public class PartnerBusinessDto {

    public PartnerBusinessDto(String date) {
        this.date = date;
        Integer integer = date.lastIndexOf("-");
        // 如果是月初
        if("01".equalsIgnoreCase(date.substring(integer+1,integer+3))){
            this.isFirstDayofMonth = true;
        }else{
            this.isFirstDayofMonth = false;
        }
        this.startDate =  DateUtil.strToYYMMDDDate(this.date);
        this.endDate = DateUtil.dateAddDay(this.startDate,1);
        this.preDate = DateUtil.dateAddDay(this.date,-1);
        this.startTime = this.startDate.getTime();
        this.endTime = this.endDate.getTime();
        this.month = date.substring(0,date.lastIndexOf("-"));
    }

    /**
     * 统计周期 yyyy-MM-dd
     */
    private String date;

    /**
     * 统计周期开始时间 yyy-MM-dd 00:00:00
     */
    private Date startDate;

    /**
     *统计周期结束时间 yyy-MM-dd 00:00:00
     */
    private Date endDate;

    /**
     *统计周期前一天
     */
    private String preDate;

    /**
     *统计周期开始时间戳ms
     */
    private Long startTime;

    /**
     *统计时间结束时间戳ms
     */
    private Long endTime;

    /**
     * 地区等级 1 2 3
     */
    private Integer level;

    private Boolean isFirstDayofMonth;

    private String month;
}
