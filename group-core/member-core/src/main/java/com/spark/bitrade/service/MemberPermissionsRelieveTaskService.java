package com.spark.bitrade.service;

import com.spark.bitrade.constant.MonitorExecuteEnvent;
import com.spark.bitrade.constant.RelievePermissionsStaus;
import com.spark.bitrade.dao.MemberPermissionsRelieveTaskDao;
import com.spark.bitrade.entity.MemberPermissionsRelieveTask;
import com.spark.bitrade.mapper.dao.MemberPermissionRelieveTaskMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户权限自动解禁任务表Service
 * @author tansitao
 * @time 2018/11/27 15:27 
 */
@Service
public class MemberPermissionsRelieveTaskService extends BaseService {
    @Autowired
    private MemberPermissionsRelieveTaskDao mprTaskDao;

    @Autowired
    private MemberPermissionRelieveTaskMapper mprTaskMapper;
    /**
     * 保存自动解冻权限任务
     * @author tansitao
     * @time 2018/11/27 15:32 
     */
    @CacheEvict(cacheNames = "memberPermissionsRelieveTask", allEntries = true)
    public MemberPermissionsRelieveTask save(MemberPermissionsRelieveTask mprTask){
        return mprTaskDao.save(mprTask);
    }

    /**
     * 通过权限类型和会员id查询自动解锁任务记录
     * @author tansitao
     * @time 2018/11/28 11:57 
     */
    public MemberPermissionsRelieveTask findByMemberIdAndPermissionsType(Long memberId, MonitorExecuteEnvent type){
        return mprTaskMapper.queryByMemberAndType(memberId, type.getOrdinal());
    }

    /**
      * 通过权限类型和会员id查询自动解锁任务记录
      * @author tansitao
      * @time 2018/11/28 11:57 
      */
    public List<MemberPermissionsRelieveTask> findListByMemberIdAndPermissionsType(Long memberId, MonitorExecuteEnvent one,MonitorExecuteEnvent two){
        return mprTaskMapper.queryListByMemberAndType(memberId, one.getOrdinal(),two.getOrdinal());
    }

    /**
      * 查询达到解锁条件的自动解锁任务记录
      * @author tansitao
      * @time 2018/11/28 11:57 
      */
    public List<MemberPermissionsRelieveTask> findAllPermissionsRelieveTask(){
        return mprTaskMapper.queryAllTask();
    }

    /**
     * 处理自动解锁权限任务
     * @author tansitao
     * @time 2018/11/29 14:27 
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, RelievePermissionsStaus status){
        mprTaskDao.updateRelievePermissionsStatus(status, id);
    }
}
