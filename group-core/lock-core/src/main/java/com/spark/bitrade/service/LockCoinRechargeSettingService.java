package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.LockSettingStatus;
import com.spark.bitrade.dao.LockCoinRechargeSettingDao;
import com.spark.bitrade.dto.LockCoinRechargeSettingDto;
import com.spark.bitrade.entity.LockCoinRechargeSetting;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 * 锁仓充值配置

 * @author yangch
 * @time 2018.06.12 15:55
 */

@Service
public class LockCoinRechargeSettingService extends BaseService {
    @Autowired
    private LockCoinRechargeSettingDao dao;

    public LockCoinRechargeSetting save(LockCoinRechargeSetting entity){
        return dao.save(entity);
    }

    public LockCoinRechargeSetting findOne(Long id){
        return dao.findOne(id);
    }

    /***
     * 查询所有已生效的锁仓充值配置
     *
     * @author yangch
     * @time 2018.06.12 16:04 
     * @param
     */
    public List<LockCoinRechargeSetting> findAllValid(){
        return dao.findAllByStatus(LockSettingStatus.VALID);
    }

    /**
      * 分页查询
      * @author tansitao
      * @time 2018/6/13 10:33 
      */
    public Page<LockCoinRechargeSetting> findAll(Predicate predicate, Pageable pageable) {
        return dao.findAll(predicate, pageable);
    }

//    /***
//     * 查询所有已生效的锁仓充值配置
//     *
//     * @author yangch
//     * @time 2018.06.12 16:04 
//     * @param
//     */
//    public List<LockCoinRechargeSettingDto> findAllValid4Dto(){
//        return dao.queryAllByStatus(LockSettingStatus.VALID);
//    }
}
