package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.AppealDTO;
import com.spark.bitrade.entity.Appeal;
import com.spark.bitrade.entity.OrderAppealAccessory;
import com.spark.bitrade.vo.AppealDetailVO;
import com.spark.bitrade.vo.AppealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 后台申诉mapper
 * @author Zhang Yanjun
 * @time 2018.08.30 16:32
 */
@Mapper
public interface AppealMapper {
    //查询后台申诉信息
    List<AppealDTO> findAllBy(Map<String,Object> map);
    //查询申诉详情
    AppealDetailVO findOne(Long id);

    @Select("SELECT * FROM appeal a WHERE a.order_id = #{orderId} limit 1")
    Appeal findByorderId(@Param("orderId") String orderId );

    @Select("SELECT * FROM appeal a WHERE a.order_id = #{orderId} ORDER BY a.create_time desc LIMIT 1")
    Appeal findNewByorderId(@Param("orderId") String orderId );

    List<OrderAppealAccessory> getAppealImg(@Param("appealId") Long id);

    //查询历史申诉
    List<AppealDetailVO> findAppealHistoty(@Param("orderSn")String orderSn);
}
