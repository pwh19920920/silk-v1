package com.spark.bitrade.model.screen;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.ability.ScreenAbility;
import com.spark.bitrade.constant.AdvertiseControlStatus;
import com.spark.bitrade.entity.QAdvertise;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2711:45
 */
@Data
public class AdvertiseScreen implements ScreenAbility {

    AdvertiseControlStatus status;

    /**
     * 处理内部断言
     *
     * @return
     */
    @Override
    public ArrayList<BooleanExpression> getBooleanExpressions() {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (status != null){ //add by tansitao 时间： 2018/11/8 原因：按照阿里巴巴规范修改代码
            booleanExpressions.add(QAdvertise.advertise.status.eq(status));
        }
        return booleanExpressions;
    }


}
