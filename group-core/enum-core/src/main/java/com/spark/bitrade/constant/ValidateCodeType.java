package com.spark.bitrade.constant;

import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.06 09:45  
 */
@Getter
@AllArgsConstructor
public enum ValidateCodeType implements BaseEnum {

    /**
     * 无验证 0
     */
    NO_VALIDATE("无验证"),
    /**
     * 图形验证码 自己开发的  1
     */
    IMAGE_CODE("图形验证码"),
    /**
     * 老的极验证，即现在用的极验证  2
     */
    OLD_GEETEST("老的极验证，即现在用的极验证"),
    /**
     * 网易极验证 3
     */
    WANGYI_GEETEST("网易极验证"),
    ;


    private String cnName;

    @Override
    public int getOrdinal() {
        return this.ordinal();
    }


    public static ValidateCodeType getTypeByOrdinal(int order) {

        ValidateCodeType[] values = ValidateCodeType.values();
        for (ValidateCodeType type : values) {
            int ordinal = type.getOrdinal();
            if (order == ordinal) {
                return type;
            }
        }
        return null;
    }


}