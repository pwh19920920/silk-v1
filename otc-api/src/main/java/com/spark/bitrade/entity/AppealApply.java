package com.spark.bitrade.entity;

import com.spark.bitrade.constant.AppealType;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @author Zhang Jinwei
 * @date 2018年01月22日
 */
@Data
public class AppealApply {
    @NotNull(message = "缺少参数")
    private String orderSn;
    @NotBlank(message = "申诉原因不能为空")
    private String remark;
//    @NotBlank(message = "必须上传申诉材料")
    private String materialUrls;//add by tansitao 时间： 2018/9/7 原因：申诉材料
    @NotNull
    private AppealType appealType;//add by tansitao 时间： 2018/9/7 原因：申诉类型
}
