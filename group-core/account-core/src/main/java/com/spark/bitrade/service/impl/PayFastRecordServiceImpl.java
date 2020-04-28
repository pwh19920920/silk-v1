package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.dto.PayRecordDto;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.PayFastRecord;
import com.spark.bitrade.entity.PayWalletPlatMemberBind;
import com.spark.bitrade.mapper.dao.PayFastRecordMapper;
import com.spark.bitrade.service.IPayFastRecordService;
import com.spark.bitrade.service.IPayWalletPlatMemberBindService;
import com.spark.bitrade.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayFastRecordServiceImpl extends ServiceImpl<PayFastRecordMapper, PayFastRecord> implements IPayFastRecordService {

    @Autowired
    private PayFastRecordMapper payFastRecordMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private IPayWalletPlatMemberBindService platMemberBindService;


    //    public int insert(PayFastRecord pojo){
//        return payFastRecordMapper.insert(pojo);
//    }
//
//    public int insertList(List<PayFastRecord> pojos){
//        return payFastRecordMapper.insertList(pojos);
//    }
//
//    public List<PayFastRecord> select(PayFastRecord pojo){
//        return payFastRecordMapper.select(pojo);
//    }
//
//    public int update(PayFastRecord pojo){
//        return payFastRecordMapper.update(pojo);
//    }
    @Override
    @WriteDataSource
    public int save(PayFastRecord payFastRecord) {
        return payFastRecordMapper.insert(payFastRecord);
    }

    @Override
    public PayFastRecord findOneByTradeSn(String tradeSn) {
        return payFastRecordMapper.findOneByTradeSn(tradeSn);
    }

    /**
     * 根据条件查询快速转账记录
     *
     * @param unit
     * @param tradeType    转账类型（0转入 1转出）
     * @param transferType 交易类型
     * @param platform     转账方应用ID
     * @param platformTo   收款方应用ID
     */
    @Override
    @ReadDataSource
    public PageInfo<PayFastRecord> findByMember(String unit, Long memberId, int pageNo, int pageSize, int tradeType,
                                                PayTransferType transferType, String platform, String platformTo, String startTime, String endTime) {
        Page<PayFastRecord> page = PageHelper.startPage(pageNo, pageSize);
        this.payFastRecordMapper.findByMember(unit, memberId, tradeType, transferType.getOrdinal(), platform, platformTo, startTime, endTime);
        return page.toPageInfo();
    }

    @Override
    @ReadDataSource
    public PageInfo<PayFastRecord> findlist(int pageNo, int pageSize, PayTransferType transferType, String startTime, String endTime,
                                            Long fromId, Long toId, String fromPhone, String toPhone, String fromAppid, String toAppid) {
        Page<PayFastRecord> page = PageHelper.startPage(pageNo, pageSize);
        this.payFastRecordMapper.findlist((transferType == null || "".equals(transferType) ? null : transferType.getOrdinal()),
                startTime, endTime, fromId, toId, fromPhone, toPhone, fromAppid, toAppid);
        return page.toPageInfo();
    }

    @Override
    @ReadDataSource
    public PageInfo<PayFastRecord> findFastRecord(String unit,int pageNo, int pageSize, Long memberId, String appId, String startTime, String endTime) {
        Page<PayFastRecord> page = PageHelper.startPage(pageNo, pageSize);
        this.payFastRecordMapper.findFastRecord(unit,memberId, appId, appId, startTime, endTime);
        return page.toPageInfo();
    }

    @Override
    @ReadDataSource
    public List<PayRecordDto> getFastRecord(Long memberId, String appId,List<PayFastRecord> list) {

        List<PayRecordDto> payRecordList = new ArrayList<>();

        //用户
        Member member = memberService.findOne(memberId);
        //用户管理表
        PayWalletPlatMemberBind platMemberBind = platMemberBindService.findByMemberIdAndAppId(memberId, appId);



        list.forEach(fastRecord -> {
            PayRecordDto payRecordDto = new PayRecordDto();
            payRecordDto.setMemberId(memberId);
            payRecordDto.setUsername(member.getUsername());
            payRecordDto.setBusinessname(platMemberBind == null ? null : platMemberBind.getBusinessName());
            payRecordDto.setType(0); //类型默认为0


            //交易方向
            if (fastRecord.getPayId().equals(memberId)) {
                //用户为支付方，类型type为转出1
                payRecordDto.setType(1);
                //交易对象为收款方
                Member tradeMember = memberService.findOne(fastRecord.getReceiptId());
                PayWalletPlatMemberBind tradePlatMemberBind = platMemberBindService.findByMemberIdAndAppId(fastRecord.getReceiptId(), fastRecord.getPlatformTo());
                payRecordDto.setTradeMemberId(tradeMember.getId());
                payRecordDto.setTradeUsername(tradeMember.getUsername());
                payRecordDto.setTradeBusinessName(tradePlatMemberBind == null ? null : tradePlatMemberBind.getBusinessName());
            } else {
                //用户为收款方,类型type为转入0
                //交易对象为支付方
                Member tradeMember = memberService.findOne(fastRecord.getPayId());
                PayWalletPlatMemberBind tradePlatMemberBind = platMemberBindService.findByMemberIdAndAppId(fastRecord.getPayId(), fastRecord.getPlatform());
                payRecordDto.setTradeMemberId(tradeMember.getId());
                payRecordDto.setTradeUsername(tradeMember.getUsername());
                payRecordDto.setTradeBusinessName(tradePlatMemberBind == null ? null : tradePlatMemberBind.getBusinessName());
            }

            //流水号、地址、时间、状态、总数、实到数、手续费、币种、交易类型
            payRecordDto.setId(fastRecord.getTradeSn());
            payRecordDto.setToAddress(fastRecord.getReceiptAddress());
            payRecordDto.setFromAddress(fastRecord.getPayAddress());
            payRecordDto.setCreateTime(fastRecord.getCreateTime());
            payRecordDto.setStatus(fastRecord.getStatus().getOrdinal());
            payRecordDto.setTotalAmount(fastRecord.getAmount());
            payRecordDto.setArriveAmount(fastRecord.getArrivedAmount());
            payRecordDto.setFee(fastRecord.getFee());
            payRecordDto.setUnit(fastRecord.getUnit());
            payRecordDto.setFeeUnit(fastRecord.getFeeUnit());
            payRecordDto.setTradeType(fastRecord.getTradeType().getOrdinal());

            payRecordList.add(payRecordDto);
        });

//    }

        return payRecordList;
    }


}
