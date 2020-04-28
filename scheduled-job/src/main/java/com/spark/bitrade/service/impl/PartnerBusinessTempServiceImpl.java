package com.spark.bitrade.service.impl;

import com.spark.bitrade.dto.PartnerBusinessDto;
import com.spark.bitrade.dto.TempTable;
import com.spark.bitrade.entity.PartnerBusinessTemp;
import com.spark.bitrade.mapper.dao.PartnerBusinessTempMapper;
import com.spark.bitrade.service.IPartnerBusinessTempService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-31
 */
@Service
public class PartnerBusinessTempServiceImpl extends ServiceImpl<PartnerBusinessTempMapper, PartnerBusinessTemp> implements IPartnerBusinessTempService {

    @Autowired
    private PartnerBusinessTempMapper partnerBusinessTempMapper;


    /**
     * 统计合伙人业务量（日表）
     *
     * @param partnerBusinessDto
     * @return true
     * @author shenzucai
     * @time 2018.05.31 14:43
     */
    @Override
    public Integer saveListPartnerBusinessTemp(PartnerBusinessDto partnerBusinessDto) {
        return partnerBusinessTempMapper.saveListPartnerBusinessTemp(partnerBusinessDto);
    }

    /**
     * @param tempTable
     * @return true
     * @author shenzucai
     * @time 2018.05.31 16:51
     */
    @Override
    public Boolean createTable(TempTable tempTable) {
        return partnerBusinessTempMapper.createTable(tempTable);
    }

    /**
     * @param baseTable
     * @return true
     * @author shenzucai
     * @time 2018.05.31 16:51
     */
    @Override
    public Integer isExistTable(String baseTable) {
        return partnerBusinessTempMapper.isExistTable(baseTable);
    }

    /**
     * @param newTable
     * @return true
     * @author shenzucai
     * @time 2018.05.31 16:51
     */
    @Override
    public Integer deleteData(String newTable) {
        return partnerBusinessTempMapper.deleteData(newTable);
    }

    /**
     * @param tempTable
     * @return true
     * @author shenzucai
     * @time 2018.05.31 16:51
     */
    @Override
    public Boolean initTable(TempTable tempTable,PartnerBusinessDto partnerBusinessDto) {
        // 存在则清理数据，不存在则创建
        Boolean aBoolean = partnerBusinessTempMapper.isExistTable(tempTable.getNewTable())>0?true:false;
        if(aBoolean){
            aBoolean = partnerBusinessTempMapper.deleteData(tempTable.getNewTable())>=0?true:false;

        }else{
            aBoolean = partnerBusinessTempMapper.createTable(tempTable);
        }
        if(aBoolean != true) {
            return aBoolean;
        }

        // 填充数据
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("tempTable",tempTable);
        map.put("partnerBusinessDto",partnerBusinessDto);
        aBoolean = partnerBusinessTempMapper.saveMemberofStatisticalCycle(map)>=0;
        return aBoolean;
    }



    /**
     * 根据条件获取表的数据，并将其保存至temp形式的表
     *
     * @param map
     * @return true
     * @author shenzucai
     * @time 2018.05.31 17:36
     */
    @Override
    public Integer saveMemberofStatisticalCycle(Map<String, Object> map) {
        return partnerBusinessTempMapper.saveMemberofStatisticalCycle(map);
    }

    /**
     * 清理合伙人业务统计明细表（当前统计周期和区域等级）
     *
     * @param partnerBusinessDto
     * @return true
     * @author shenzucai
     * @time 2018.06.01 14:13
     */
    @Override
    public Integer deleteDataByStatisticalCycleAndLevel(PartnerBusinessDto partnerBusinessDto) {
        return partnerBusinessTempMapper.deleteDataByStatisticalCycleAndLevel(partnerBusinessDto);
    }

    /**
     * 根据条件获取相应表的数据，并将其保存至temp形式的表，仅适用partner_business
     *
     * @param map
     * @return true
     * @author shenzucai
     * @time 2018.06.01 14:43
     */
    @Override
    public Integer saveListPartenerLevelData(Map<String, Object> map) {
        return partnerBusinessTempMapper.saveListPartenerLevelData(map);
    }

    /**
     * @param tempTable
     * @param partnerBusinessDto
     * @return true
     * @author shenzucai
     * @time 2018.05.31 16:51
     */
    @Override
    public Boolean initPartnerLevelTmpeTable(TempTable tempTable, PartnerBusinessDto partnerBusinessDto) {
        // 存在则清理数据，不存在则创建
        Boolean aBoolean = partnerBusinessTempMapper.isExistTable(tempTable.getNewTable())>0?true:false;
        if(aBoolean){
            aBoolean = partnerBusinessTempMapper.deleteData(tempTable.getNewTable())>=0?true:false;

        }else{
            aBoolean = partnerBusinessTempMapper.createTable(tempTable);
        }
        if(aBoolean != true) {
            return aBoolean;
        }

        // 填充数据
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("tempTable",tempTable);
        map.put("partnerBusinessDto",partnerBusinessDto);
        aBoolean = partnerBusinessTempMapper.saveListPartenerLevelData(map)>=0;
        return aBoolean;
    }

    /**
     * 根据条件将数据插入到partner_business
     *
     * @param map
     * @return true
     * @author shenzucai
     * @time 2018.06.01 18:34
     */
    @Override
    public Integer savePartnerBusiness(Map<String, Object> map) {
        return partnerBusinessTempMapper.savePartnerBusiness(map);
    }


    /**
     * @param tempTable
     * @param partnerBusinessDto
     * @return true
     * @author shenzucai
     * @time 2018.05.31 16:51
     */
    @Override
    public Boolean initPartnerBusinessTable(TempTable tempTable, PartnerBusinessDto partnerBusinessDto) {
        Boolean aBoolean = partnerBusinessTempMapper.deleteDataByStatisticalCycleAndLevel(partnerBusinessDto)>=0?true:false;
        if(aBoolean != true) {
            return aBoolean;
        }
        // 填充数据
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("tempTable",tempTable);
        map.put("partnerBusinessDto",partnerBusinessDto);
        aBoolean = partnerBusinessTempMapper.savePartnerBusiness(map)>=0;
        return aBoolean;
    }

    /**
     * 获取最新的时间周期
     *
     * @return true
     * @author shenzucai
     * @time 2018.06.11 12:05
     */
    @Override
    public PartnerBusinessTemp getLastOfTable() {
        return partnerBusinessTempMapper.getLastOfTable();
    }

}
