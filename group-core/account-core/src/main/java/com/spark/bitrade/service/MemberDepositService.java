package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.MemberDepositDao;
import com.spark.bitrade.entity.MemberDeposit;
import com.spark.bitrade.entity.QMember;
import com.spark.bitrade.entity.QMemberDeposit;
import com.spark.bitrade.mapper.dao.MemberDepositMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.service.Base.TopBaseService;
import com.spark.bitrade.util.PredicateUtils;
import com.spark.bitrade.vo.MemberDepositVO;
import com.spark.bitrade.vo.ThirdAuthQueryVo;
import com.spark.bitrade.vo.WithdrawRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MemberDepositService extends BaseService<MemberDeposit> {

    @Autowired
    private MemberDepositDao memberDepositDao ;
    @Autowired
    private MemberDepositMapper memberDepositMapper;


    public Page<MemberDeposit> pageQuery(List<BooleanExpression> predicates, PageModel pageModel){
        Predicate predicate = PredicateUtils.getPredicate(predicates);
        return memberDepositDao.findAll(predicate,pageModel.getPageable());
    }

    //add by yangch 时间： 2018.04.29 原因：合并
    public Page<MemberDepositVO> page(List<BooleanExpression> predicates,PageModel pageModel){
        JPAQuery<MemberDepositVO> query = queryFactory.select(Projections.fields(MemberDepositVO.class,
                QMemberDeposit.memberDeposit.id.as("id"),
                QMember.member.username,
                QMemberDeposit.memberDeposit.createTime,//add by tansitao 时间： 2018/5/14 原因：增加时间
                QMember.member.id.as("memberId"),
                QMemberDeposit.memberDeposit.address,
                QMemberDeposit.memberDeposit.amount,
                QMemberDeposit.memberDeposit.unit)).from(QMember.member,QMemberDeposit.memberDeposit)
                .where(predicates.toArray(new BooleanExpression[predicates.size()]));
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()])) ;
        long total = query.fetchCount() ;
        query.offset(pageModel.getPageSize()*(pageModel.getPageNo()-1)).limit(pageModel.getPageSize());
        List<MemberDepositVO> list = query.fetch() ;
        return new PageImpl<MemberDepositVO>(list,pageModel.getPageable(),total);
    }

    /**
     * 第三方项目方授权的充币数据分页查询
     * @author fumy
     * @time 2018.09.19 17:57
     * @param symbol
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @ReadDataSource
    public PageInfo<ThirdAuthQueryVo> findMemberDeposit(String symbol, int pageNo, int pageSize){
        com.github.pagehelper.Page<ThirdAuthQueryVo> page= PageHelper.startPage(pageNo,pageSize);
        memberDepositMapper.queryDepositByUnit(symbol);
        return page.toPageInfo();
    }


    /**
     * 分页查询充币记录
     * @author fumy
     * @time 2018.09.23 19:15
     * @param params
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @ReadDataSource
    public PageInfo<MemberDepositVO> page(Map<String,Object> params, int pageNo, int pageSize){
        com.github.pagehelper.Page<MemberDepositVO> page= PageHelper.startPage(pageNo,pageSize);
        memberDepositMapper.queryFoPage(params);
        return page.toPageInfo();
    }

    /**
     * 获取指定账号的外购币进入数、平台提出数
     * @author fumy
     * @time 2018.10.08 15:36
     * @param date
     * @param unit
     * @return true
     */
    @ReadDataSource
    public Map<String,Object> getFixMemberStat(String date,String unit){
        if("total".equals(date)){
            date = "";
        }
        return memberDepositMapper.getFixMemberStat(date,unit);
    }

    /**
     * 根据交易哈希，获取交易详情
     *
     * @param txid 交易哈希
     * @return 交易详情
     */
    @ReadDataSource
    public MemberDeposit getMemberDepositByTxid(String txid) {
        return memberDepositMapper.getMemberDepositByTxid(txid);
    }
}
