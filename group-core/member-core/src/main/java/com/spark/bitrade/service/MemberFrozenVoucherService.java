package com.spark.bitrade.service;

import com.spark.bitrade.constant.CertificateType;
import com.spark.bitrade.mapper.dao.MemberFrozenVoucherMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MemberFrozenVoucherService {

    @Resource
    private MemberFrozenVoucherMapper mapper;

    public boolean isFrozenCertificate(String idCard, CertificateType type) {
        return mapper.countByIdCardAndCertificateType(idCard, type) > 0;
    }
}
