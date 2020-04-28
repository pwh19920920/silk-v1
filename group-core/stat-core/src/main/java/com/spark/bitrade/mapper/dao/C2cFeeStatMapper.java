package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.vo.C2cFeeStatSynVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * c2c交易手续费统计查询
 * @author Zhang Yanjun
 * @time 2018.10.10 17:14
*/
@Mapper
public interface C2cFeeStatMapper extends BaseMapper<C2cFeeStatSynVO> {

    //c2c交易手续费总的、内部、外部查询
    List<C2cFeeStatSynVO> findC2cFeeStatAllAndInnerAndOuter(@Param("type") Integer type,@Param("unit") String unit,@Param("startTime") String startTime,@Param("endTime") String endTime);


}
