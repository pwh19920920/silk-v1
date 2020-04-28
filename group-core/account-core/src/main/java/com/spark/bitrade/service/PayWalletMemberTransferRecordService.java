package com.spark.bitrade.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.spark.bitrade.entity.PayWalletMemberTransferRecord;
import com.spark.bitrade.mapper.dao.PayWalletMemberTransferRecordMapper;

@Service
public class PayWalletMemberTransferRecordService {

    @Resource
    private PayWalletMemberTransferRecordMapper payWalletMemberTransferRecordMapper;

    public int insert(PayWalletMemberTransferRecord pojo){
        return payWalletMemberTransferRecordMapper.insert(pojo);
    }

    public int insertList(List< PayWalletMemberTransferRecord> pojos){
        return payWalletMemberTransferRecordMapper.insertList(pojos);
    }

    public List<PayWalletMemberTransferRecord> select(PayWalletMemberTransferRecord pojo){
        return payWalletMemberTransferRecordMapper.select(pojo);
    }

    public int update(PayWalletMemberTransferRecord pojo){
        return payWalletMemberTransferRecordMapper.update(pojo);
    }

}
