package com.spark.bitrade.model.update;

import com.spark.bitrade.ability.UpdateAbility;
import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.entity.AppRevision;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:48
 */
@Data
@Getter
@Setter
public class AppRevisionUpdate implements UpdateAbility<AppRevision> {

    private String remark;

    private String downloadUrl;

    private int platform;


    //转化
    @Override
    public AppRevision transformation(AppRevision appRevision) {
        if (StringUtils.isNotBlank(remark))
            appRevision.setRemark(remark);
        if (StringUtils.isNotBlank(downloadUrl))
            appRevision.setDownloadUrl(downloadUrl);
        return appRevision;
    }
}
