package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.dto.LockAbstractDto;
import com.spark.bitrade.dto.LockCoinDetailDto;
import com.spark.bitrade.dto.LockInternalDetailDto;
import com.spark.bitrade.dto.LockTypeDto;
import com.spark.bitrade.mapper.dao.LockTypeMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by lingxing on 2018/7/12.
 */
@Service
public class LockTypeService extends BaseService {
    @Autowired
    private LockTypeMapper mapper;

    /**
      * 此处可以根据锁仓类型查内部锁仓和锁仓活动，理财锁仓
      * @author lingxing
      * @time 2018/7/13 14:33 
      */
    @ReadDataSource
    public PageInfo<LockTypeDto> queryPageByLockType(LockAbstractDto lockAbstract, int pageNum, int pageSize){
        com.github.pagehelper.Page<LockTypeDto> page = PageHelper.startPage(pageNum, pageSize);
//        this.mapper.findAllLock(lockAbstract);
        this.findAllLock(lockAbstract);
        return page.toPageInfo();
    }

    //锁仓查询
    private  List<LockTypeDto> findAllLock(LockAbstractDto lockAbstract){
        List<LockTypeDto> list=mapper.findAllLock(lockAbstract);
        for (int i=0;i<list.size();i++){
            //edit by zyj:解决时间显示毫秒的问题
            String cancleTime=list.get(i).getCancleTime()==null?"":list.get(i).getCancleTime();
            list.get(i).setCancleTime(cancleTime.substring(0,cancleTime.length()-2>0?cancleTime.length()-2:0));
            String lockTime=list.get(i).getLockTime()==null?"":list.get(i).getLockTime();
            list.get(i).setLockTime(lockTime.substring(0,lockTime.length()-2>0?lockTime.length()-2:0));
            String planUnlockTime=list.get(i).getPlanUnlockTime()==null?"":list.get(i).getPlanUnlockTime();
            list.get(i).setPlanUnlockTime(planUnlockTime.substring(0,planUnlockTime.length()-2>0?planUnlockTime.length()-2:0));
            //edit by zyj:修改类型、状态显示为中文
            list.get(i).setLockType(list.get(i).getType().getCnName());
            list.get(i).setLockStatus(list.get(i).getStatus().getCnName());
        }
        return list;
    }

    @ReadDataSource
    public LockCoinDetailDto findByLockDetail(Long lockCoinDetailId){
        Assert.notNull(lockCoinDetailId, "该锁仓配置不存在");
        LockCoinDetailDto lockDetailDto=this.mapper.findByLockDetail(lockCoinDetailId);
        if(lockDetailDto!=null){
            lockDetailDto.setUnlockCoinDetailList(this.mapper.findByLockCoinDetailId(lockDetailDto.getId()));
        }
        return  lockDetailDto;
    }
    @ReadDataSource
    public LockInternalDetailDto findByLockInternalDetail(Long lockCoinDetailId){
        Assert.notNull(lockCoinDetailId, "该锁仓配置不存在");
        LockInternalDetailDto internalDetailDto=this.mapper.findByLockInternalDetail(lockCoinDetailId);
        if(internalDetailDto!=null){
            internalDetailDto.setUnlockCoinDetailList(this.mapper.findByLockCoinDetailId(internalDetailDto.getId()));
        }
        return internalDetailDto ;
    }

    //导出
    public List<LockTypeDto> findByInternalLockAllForOut(LockAbstractDto lockAbstractDto){
        return  this.findAllLock(lockAbstractDto);
    }
}
