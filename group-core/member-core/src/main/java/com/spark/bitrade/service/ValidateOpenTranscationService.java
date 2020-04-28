package com.spark.bitrade.service;

import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.MemberSecuritySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 *  
 *   验证是否禁止交易 
 *  @author liaoqinghui  
 *  @time 2019.08.13 16:16  
 */
@Service
public class ValidateOpenTranscationService {

    @Autowired
    private MemberSecuritySetService memberSecuritySetService;

    /**
     * 是否开平台内转账
     *
     * @param memberId
     * @return
     */
    public void validateOpenPlatformTransaction(Long memberId, String message) {
        MemberSecuritySet set = getMemberSecuritySet(memberId);
        if (set != null) {
            BooleanEnum bool = set.getIsOpenPlatformTransaction();
            Assert.isTrue(bool.isIs(), message);
        }
    }

    /**
     * 是否开启提币
     *
     * @param memberId
     * @return
     */
    public void validateOpenUpCoinTransaction(Long memberId, String message) {
        MemberSecuritySet set = getMemberSecuritySet(memberId);
        if (set != null) {
            BooleanEnum bool = set.getIsOpenUpCoinTransaction();
            Assert.isTrue(bool.isIs(), message);
        }
    }

    /**
     * 是否开启BB交易
     *
     * @param memberId
     * @return
     */
    public void validateOpenBBTransaction(Long memberId, String message) {
        MemberSecuritySet set = getMemberSecuritySet(memberId);
        if (set != null) {
            BooleanEnum bool = set.getIsOpenBbTransaction();
            Assert.isTrue(bool.isIs(), message);
        }
    }



    /**
     * 是否开法币交易
     *
     * @param memberId
     * @return
     */
    public void validateOpenExPitTransaction(Long memberId, String message, AdvertiseType type) {
        MemberSecuritySet set = getMemberSecuritySet(memberId);
        if (set != null) {
            //买
            if(type==AdvertiseType.BUY){
                BooleanEnum bool = set.getIsOpenExPitTransaction();
                Assert.isTrue(bool.isIs(), message);
            }
            if(type==AdvertiseType.SELL){
                BooleanEnum bool = set.getIsOpenExPitSellTransaction();
                Assert.isTrue(bool.isIs(), message);
            }
            //卖

        }
    }


    private MemberSecuritySet getMemberSecuritySet(Long memberId) {
        MemberSecuritySet oneBymemberId = memberSecuritySetService.findOneBymemberId(memberId);
        return oneBymemberId;
    }
}
