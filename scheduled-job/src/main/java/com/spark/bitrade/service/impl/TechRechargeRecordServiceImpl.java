package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.TechRechargeRecord;
import com.spark.bitrade.mapper.dao.MemberMapper;
import com.spark.bitrade.mapper.dao.TechRechargeRecordMapper;
import com.spark.bitrade.service.ITechRechargeRecordService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 技术充（减）币记录 服务实现类
 * </p>
 *
 * @author fumy
 * @since 2018-06-20
 */
@Service
public class TechRechargeRecordServiceImpl extends ServiceImpl<TechRechargeRecordMapper, TechRechargeRecord> implements ITechRechargeRecordService {

    @Autowired
    MemberMapper memberMapper;

    @Override
    public List<TechRechargeRecord> techRechargeList() {
        //1--内部商户，2--操盘手，3--员工商户,4--外部商户除外账号
        String types="1,2,3,4";
        List<String> memberList = memberMapper.getMemberIdsByTypes(types);
        String innerId = memberList.get(0);
        String traderId = memberList.get(1);
        String employeeId = memberList.get(2);
        String outerId = memberList.get(3);
        return baseMapper.techRechargeList(traderId,innerId,employeeId,outerId);
    }

    @Override
    public int insertRecord(List<TechRechargeRecord> list) {
        return baseMapper.insertRecord(list);
    }
}
