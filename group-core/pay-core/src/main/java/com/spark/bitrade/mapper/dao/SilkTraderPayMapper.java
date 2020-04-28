package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.SilkTraderContract;
import com.spark.bitrade.entity.SilkTraderContractDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第三方支付
 */
@Mapper
public interface SilkTraderPayMapper {

    //商家签约信息分页
    List<SilkTraderContract> findAll();

    //添加商家签约信息配置
    int create(SilkTraderContract silkTraderContract);

    //商家签约信息详情
    SilkTraderContract findOne(@Param("id") Long id);

    //商家签约信息删除
    int deletes(Long[] ids);

    //商家签约信息修改
    void update(SilkTraderContract silkTraderContract);

    //查询商家签约状态
    int findStatusById(@Param("id")long id);

    //商家签约信息修改启用状态
    void updateStatus(@Param("status") long status, @Param("id")long id);

    //商家签约币种详情分页
    List<SilkTraderContractDetail> findDetailAll(@Param("contractNo")String contractNo);

    //某商家签约币种详情
    SilkTraderContractDetail findDetailOne(@Param("id") Long id);

    //添加商家签约币种详情
    void createDetail(SilkTraderContractDetail silkTraderContractDetail);

    //商家签约币种详情删除
    int deletesDetail(Long[] ids);

    //商家签约币种详情修改
    int updateDetail(SilkTraderContractDetail silkTraderContractDetail);
}
