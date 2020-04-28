package com.spark.bitrade.service;

import com.spark.bitrade.entity.ThirdPlatformApply;
import com.spark.bitrade.vo.PayApplyVo;

/**
 * @author fumy
 * @time 2018.10.23 09:33
 */
public interface IPayApplyService {

    boolean isExistPlatByApplyKey(String applyKey);

    void save(ThirdPlatformApply thirdPlatformApply);

    PayApplyVo getApplyByAccount(String busiAccount);

    boolean isExistApply(String busiAccount,String applyKey);
}
