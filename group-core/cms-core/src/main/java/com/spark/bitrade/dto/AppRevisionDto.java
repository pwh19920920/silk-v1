package com.spark.bitrade.dto;

import com.spark.bitrade.constant.Platform;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author lingxing
 * @time 2018.07.14 15:26
 */
@Setter
@Getter
public class AppRevisionDto {
    private String platform;
    private String version;
}
