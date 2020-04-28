package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.entity.PartnerArea;

import java.util.List;

/**
  * 解锁service
  * @author tansitao
  * @time 2018/7/20 17:57 
  */
public interface ILockDetailService extends IService<LockCoinDetail> {

    /**
     * 批量解锁锁仓活动
     * @author tansitao
     * @time 2018/7/31 10:58 
     */
    void unlockActivityLock(int unlockNum);

    /**
      * 批量解锁投资活动
      * @author tansitao
      * @time 2018/7/31 10:58 
      */
    void unlockFinanActivity(int unlockNum);

    /**
     * 批量解锁内部锁仓活动
     * @author tansitao
     * @time 2018/8/7 10:38 
     */
    void unlockInternalActivity(int unlockNum);

    /**
     * 批量解锁STO锁仓
     * @author tansitao
     * @time 2018/11/7 18:05 
     */
    void unlockSTOActivityLock(int unlockNum);

    /**
      * 批量解锁节点产品活动
      * @author tansitao
      * @time 2018/7/31 10:58 
      */
    void unlockQuantifyActivity(int unlockNum);

    /**
      * 批量解锁CNYT推荐人分期收益
      * @author tansitao
      * @time 2018/7/31 10:58 
      */
    void unlockCnytRewardIncome(int unlockNum);

    /**
      * 批量解锁BTTC锁仓分期
      * @author dengdy
      * @time 2019/4/17 10:58 
      */
    void unlockBTTCRestitutionIncome(int unlockNum);

    /**
      * 批量解锁IEO锁仓分期
      * @author fatKarin
      * @time 2019/6/6 15:58 
      */
    void unlockIeoRestitutionIncome(int unlockNum);

    /**
      * 批量解锁BCC 赋能锁仓分期
      * @author fatKarin
      * @time 2019/7/1 15:58 
      */
    void unlockBccEnergize(int unlockNum);

    /**
      * 批量解锁金钥匙活动锁仓
      * @author dengdy
      * @time 2019/4/17 10:58 
      */
    void unlockGoldenKeyPrincipal(int unlockNum);

    /** 解锁CNYT锁仓收益（自己锁仓的）
     * @author Zhang Yanjun
     * @time 2018.12.11 15:37
     */
    void unlockCNYTActivityLock();

    /**
     * 解锁CNYT锁仓本金（自己锁仓的）
     * @author Zhang Yanjun
     * @time 2018.12.11 15:37
     */
    void unlockCNYTActivityCorLock();

    /**
     * 根据锁仓记录ID查询
     * @param lockCoinDetailId
     * @return
     */
    LockCoinDetail getMissRewardLock(Long lockCoinDetailId);

    /**
     * 根据锁仓Id查询是否存在锁仓记录
     * @param lockCoinDetailId
     * @return
     */
    boolean isExistRewardRecord(Long lockCoinDetailId);

}
