package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.RedPackReceiveRecord;
import com.spark.bitrade.vo.RedPackVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * <p>
 * 红包领取记录(red_pack_receive_record) Mapper 接口
 * </p>
 *
 * @author qiliao
 * @since 2019-11-25
 */
public interface RedPackReceiveRecordMapper extends BaseMapper<RedPackReceiveRecord> {

    @Select("SELECT " +
            "  IFNULL(m.mobile_phone,m.email) as mobilePhone, " +
            "  rpc.receive_amount as amount, " +
            "  rpc.receive_time receiveTime, " +
            "  rpc.receive_unit as coinUnit, " +
            "  rpc.redpack_id as redPackRecordId " +
            " FROM " +
            " red_pack_receive_record rpc LEFT JOIN member m ON m.id=rpc.member_id " +
            " WHERE " +
            " rpc.redpack_id = #{packManageId}  " +
            " AND rpc.receive_time is not NULL " +
            " and rpc.member_id is not null")
    List<RedPackVo> findRedpackRecordByManageId(@Param("packManageId") Long packManageId,
                                                @Param("page") Integer page,
                                                @Param("size") Integer size);

    @Select("select coin_scale from exchange_coin where coin_symbol=#{coin} ORDER BY coin_scale DESC LIMIT 1 ")
    Integer findScaleByCoin(@Param("coin") String coin);
}
