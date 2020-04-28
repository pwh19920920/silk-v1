package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.02 09:35  
 */
@Data
@Builder
public class ImageCodeVo {

    /**
     * 图片base64
     */
    @ApiModelProperty("图形验证码base64")
    private String imageUrl;

    /**
     * 时间戳key  key+时间戳
     */
    @ApiModelProperty("时间戳key，发送短信或邮箱时，传次参数和图形验证码code")
    private String timeKey;

}
