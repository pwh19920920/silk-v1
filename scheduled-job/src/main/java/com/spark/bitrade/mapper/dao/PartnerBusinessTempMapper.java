package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.dto.PartnerBusinessDto;
import com.spark.bitrade.dto.TempTable;
import com.spark.bitrade.entity.PartnerBusinessTemp;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-31
 */
@Mapper
public interface PartnerBusinessTempMapper extends BaseMapper<PartnerBusinessTemp> {

    /**
     * 统计合伙人业务量（日表）
     * @author shenzucai
     * @time 2018.05.31 14:43
     * @param partnerBusinessDto
     * @return true
     */
    Integer saveListPartnerBusinessTemp(PartnerBusinessDto partnerBusinessDto);

    /**
     *
     * @author shenzucai
     * @time 2018.05.31 16:51
     * @param tempTable
     * @return true
     */
    Boolean createTable(TempTable tempTable);
    /**
     *
     * @author shenzucai
     * @time 2018.05.31 16:51
     * @param baseTable
     * @return true
     */
    Integer isExistTable(String baseTable);
    /**
     *
     * @author shenzucai
     * @time 2018.05.31 16:51
     * @param newTable
     * @return true
     */
    Integer deleteData(String newTable);

    /**
     * 根据条件获取表的数据，并将其保存至temp形式的表 (member,exchang_order,reward_record)
     * @author shenzucai
     * @time 2018.05.31 17:36
     * @param map
     * @return true
     */
    Integer saveMemberofStatisticalCycle(Map<String, Object> map);

    /**
     * 清理合伙人业务统计明细表（当前统计周期和区域等级）
     * @author shenzucai
     * @time 2018.06.01 14:13
     * @param partnerBusinessDto
     * @return true
     */
    Integer deleteDataByStatisticalCycleAndLevel(PartnerBusinessDto partnerBusinessDto);

    /**
     * 根据条件获取相应表的数据，并将其保存至temp形式的表，仅适用partner_business
     * @author shenzucai
     * @time 2018.06.01 14:43
     * @param map
     * @return true
     */
    Integer  saveListPartenerLevelData(Map<String, Object> map);

    /**
     * 根据条件将数据插入到partner_business
     * @author shenzucai
     * @time 2018.06.01 18:34
     * @param map
     * @return true
     */
    Integer savePartnerBusiness(Map<String, Object> map);


    /**
     * 获取最新的时间周期
     * @author shenzucai
     * @time 2018.06.11 12:05
     * @param
     * @return true
     */
    PartnerBusinessTemp getLastOfTable();

}
