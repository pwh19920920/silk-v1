package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.dao.HotTransferRecordDao;
import com.spark.bitrade.entity.HotTransferRecord;
import com.spark.bitrade.mapper.dao.HotTransferRecordMapper;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class HotTransferRecordService extends TopBaseService<HotTransferRecord,HotTransferRecordDao> {

    @Autowired
    public void setDao(HotTransferRecordDao dao) {
        super.setDao(dao);
    }
    @Autowired
    private HotTransferRecordMapper hotTransferRecordMapper;

    /**
     * 热钱包转账至冷钱包记录分页查询
     * @author Zhang Yanjun
     * @time 2018.09.06 17:19
     * @param adminName
     * @param coldAddress
     * @param unit
     * @param pageNo
     * @param pageSize
     */
    @ReadDataSource
    public PageInfo<HotTransferRecord> findAllBy(String adminName, String coldAddress, String unit, int pageNo, int pageSize){
        Page<HotTransferRecord> page= PageHelper.startPage(pageNo,pageSize);
        hotTransferRecordMapper.findAllBy(adminName,coldAddress,unit);
        return page.toPageInfo();
    }

    /**
     * 热钱包转账至冷钱包记录导出
     * @author Zhang Yanjun
     * @time 2018.09.06 17:19
     * @param adminName
     * @param coldAddress
     * @param unit
     */
    @ReadDataSource
    public List<HotTransferRecord> findAllForOut(String adminName, String coldAddress, String unit){
        return hotTransferRecordMapper.findAllBy(adminName,coldAddress,unit);
    }


}
