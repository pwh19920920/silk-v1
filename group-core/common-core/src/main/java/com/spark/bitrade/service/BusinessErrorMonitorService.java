package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BusinessErrorMonitorType;
import com.spark.bitrade.dao.BusinessErrorMonitorDao;
import com.spark.bitrade.entity.BusinessErrorMonitor;
import com.spark.bitrade.mapper.dao.BusinessErrorMonitorMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/***
 * 
 * @author yangch
 * @time 2018.06.05 14:22
 */
@Service
public class BusinessErrorMonitorService extends BaseService<BusinessErrorMonitor> {
    @Autowired
    private BusinessErrorMonitorDao businessErrorMonitorDao;
    @Autowired
    private BusinessErrorMonitorMapper businessErrorMonitorMapper;

    public BusinessErrorMonitor save(BusinessErrorMonitor entity) {
        return businessErrorMonitorDao.save(entity);
    }

    @Transactional(noRollbackFor = Exception.class)
    public BusinessErrorMonitor update4NoRollback(BusinessErrorMonitor entity) {
        return businessErrorMonitorDao.save(entity);
    }

    /***
      * 记录业务错误记录
     *
      * @author yangch
      * @time 2018.06.05 14:56 
      * @param type 错误类型
      * @param inData 输入参数
      * @param errorMsg 错误描述
      */
    @Transactional(noRollbackFor = Exception.class)
    public void add(BusinessErrorMonitorType type, String inData, String errorMsg) {
        BusinessErrorMonitor entity = new BusinessErrorMonitor();
        //entity.setModule(module);
        entity.setType(type);
        entity.setInData(inData);
        entity.setErrorMsg(errorMsg);
        entity.setMaintenanceStatus(BooleanEnum.IS_FALSE);
        businessErrorMonitorDao.save(entity);
    }

    public BusinessErrorMonitor findOne(Long id) {
        return businessErrorMonitorDao.findOne(id);
    }


    public Page<BusinessErrorMonitor> findAll(Pageable pageable) {
        return businessErrorMonitorDao.findAll(pageable);
    }

    //根据条件分页
    @ReadDataSource
    public PageInfo<BusinessErrorMonitor> findBy(Integer type, Integer maintenanceStatus,int timeSort, int pageNo, int pageSize){
        com.github.pagehelper.Page<BusinessErrorMonitor> page= PageHelper.startPage(pageNo,pageSize);
        this.businessErrorMonitorMapper.findBy(type,maintenanceStatus,timeSort);
        return page.toPageInfo();
    }

    //查询所有未处理的异常数量
    @ReadDataSource
    public int findUnMaintenanceStatus(){
        return businessErrorMonitorMapper.findUnMaintenanceStatus();
    }
}
