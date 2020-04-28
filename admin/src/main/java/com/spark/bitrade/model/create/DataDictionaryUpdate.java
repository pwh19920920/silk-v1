package com.spark.bitrade.model.create;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:46
 */
@Data
public class DataDictionaryUpdate {
    @NotBlank
    private String value;
    private String comment;
}
