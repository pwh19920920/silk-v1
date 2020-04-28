package com.spark.bitrade.model.screen;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.ability.ScreenAbility;
import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.entity.QAppRevision;
import com.spark.bitrade.util.PredicateUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2417:20
 */
@Data
public class AppRevisionScreen implements ScreenAbility {

    private String version;

    private Platform platform;

    //分页能力
    //edit by yangch 时间： 2018.04.29 原因：合并
    @Override
    public ArrayList<BooleanExpression> getBooleanExpressions() {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(version))
            booleanExpressions.add(QAppRevision.appRevision.version.eq(version));
        if (platform != null)
            booleanExpressions.add(QAppRevision.appRevision.platform.eq(platform));
        return booleanExpressions;
    }
    /*public Predicate getPredicate() {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(version))
            booleanExpressions.add(QAppRevision.AppRevisionController.version.eq(version));
        if (platform != null)
            booleanExpressions.add(QAppRevision.AppRevisionController.platform.eq(platform));
        return PredicateUtils.getPredicate(booleanExpressions);
    }*/
}
