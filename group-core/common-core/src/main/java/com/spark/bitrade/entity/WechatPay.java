package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 微信信息
 *
 * @author Zhang Jinwei
 * @date 2018年01月16日
 */
@Data
@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatPay implements Serializable {
    private static final long serialVersionUID = 1511509989072675896L;
    /**
     * 微信号
     */
    private String wechat;

    /**
     * 微信昵称
     */
    private String wechatNick;


    /**
     * 微信收款二维码
     */
    private String qrWeCodeUrl;
}
