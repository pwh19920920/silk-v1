package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.constant.CertificateType;
import com.spark.bitrade.entity.MemberFrozenVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MemberFrozenVoucherMapper extends BaseMapper<MemberFrozenVoucher> {

    @Select("select count(1) from member_frozen_voucher where state = 1 and id_card=#{idCard} and certificate_type = #{type,typeHandler=org.apache.ibatis.type.EnumOrdinalTypeHandler}")
    long countByIdCardAndCertificateType(@Param("idCard") String idCard, @Param("type") CertificateType type);
}
