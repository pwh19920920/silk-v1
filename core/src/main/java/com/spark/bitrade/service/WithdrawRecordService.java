package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.constant.WithdrawStatus;
import com.spark.bitrade.dao.WithdrawRecordDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.mapper.dao.WithdrawRecordMapper;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.PageListMapResult;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.ThirdAuthQueryVo;
import com.spark.bitrade.vo.WithdrawRecordVO;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.constant.BooleanEnum.IS_FALSE;
import static com.spark.bitrade.constant.WithdrawStatus.*;
import static com.spark.bitrade.entity.QWithdrawRecord.withdrawRecord;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author Zhang Jinwei
 * @date 2018年01月29日
 */
@Service
public class WithdrawRecordService extends BaseService {
    @Autowired
    private WithdrawRecordDao withdrawApplyDao;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private InterfaceLogService interfaceLogService;
    @Autowired
    private WithdrawRecordMapper withdrawRecordMapper;

    public WithdrawRecord save(WithdrawRecord withdrawApply) {
        return withdrawApplyDao.save(withdrawApply);
    }


    /**
     * 保存并提交
     * @author shenzucai
     * @time 2018.08.01 14:38
     * @param withdrawApply
     * @return true
     */
    public WithdrawRecord saveAndFlush(WithdrawRecord withdrawApply) {
        return withdrawApplyDao.saveAndFlush(withdrawApply);
    }


    public List<WithdrawRecord> findAll() {
        return withdrawApplyDao.findAll();
    }

    public WithdrawRecord findOne(Long id) {
        return withdrawApplyDao.findOne(id);
    }

    /**
     * 根据增加提现的时候外部提币自动提现的限制，规则为一天之内每个用户的每个币种只能自动提现一次，两次自动审核提币时间间隔不能小于24小时 true则允许
     * @author shenzucai
     * @time 2018.11.22 10:53
     * @param memberId
     * @param coinName
     * @return true
     */
    public Boolean allowAutoWithDraw(long memberId,String coinName){
        Integer dayDiff = withdrawApplyDao.getLastWithDrawDayDiff(memberId,coinName);
        // 没有提币记录，或者提币间隔时间大于0 则允许提币
        if(dayDiff == null || dayDiff.compareTo(new Integer(0))  == 1){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 条件查询对象
     *
     * @param predicateList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<WithdrawRecord> query(List<Predicate> predicateList, Integer pageNo, Integer pageSize) {
        List<WithdrawRecord> list;
        JPAQuery<WithdrawRecord> jpaQuery = queryFactory.selectFrom(QWithdrawRecord.withdrawRecord);
        if (predicateList != null) {
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset(CommonUtils.toLong((pageNo - 1) * pageSize)).limit(pageSize).fetch();
        }
        else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    @Transactional(readOnly = true)
    public void test() {
        //查询字段
        List<Expression> expressions = new ArrayList<>();
        expressions.add(QWithdrawRecord.withdrawRecord.memberId.as("memberId"));
        //查询表
        List<EntityPath> entityPaths = new ArrayList<>();
        entityPaths.add(QWithdrawRecord.withdrawRecord);
        entityPaths.add(QMember.member);
        //查询条件
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(QWithdrawRecord.withdrawRecord.memberId.eq(QMember.member.id));
        //排序
        List<OrderSpecifier> orderSpecifierList = new ArrayList<>();
        orderSpecifierList.add(QWithdrawRecord.withdrawRecord.id.desc());
        PageListMapResult pageListMapResult = super.queryDslForPageListResult(expressions, entityPaths, predicates, orderSpecifierList, 1, 10);
        System.out.println(pageListMapResult);

    }

    /**
     * 修改 根据withdrawApply.id[]修改withdrawApply的WithdrawStatus
     *
     * @param ids    withdrawApply.id[]
     * @param status WithdrawStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long[] ids, WithdrawStatus status, RestTemplate restTemplate) throws Exception{
        WithdrawRecord withdrawRecord;
        for (Long id : ids) {
            //20	4.70000000	0	2018-02-27 17:47:37		0.30000000	0	28	0	5.00000000			GalaxyChain
            withdrawRecord = withdrawApplyDao.findOne(id);
            //确认提现申请存在
            notNull(withdrawRecord, "不存在");
            //确认订单状态是审核中
            isTrue(withdrawRecord.getStatus() == PROCESSING, "id为" + id + "不是审核状态的提现");
            //确认提现类型不是自动提现
            isTrue(withdrawRecord.getIsAuto() == IS_FALSE, "id为" + id + "不是人工审核提现");
            //审核
            if (status == WAITING)
            {
                MemberWallet wallet = walletService.findByCoinAndMemberId(withdrawRecord.getCoin(),withdrawRecord.getMemberId());
                notNull(wallet, "wallet null!");
                // 仅在余额不小于0
                boolean verifyBlance = BigDecimal.ZERO.compareTo(wallet.getBalance()) < 1;
                // 且冻结余额大于提币数量
                boolean verifyFrozenBalance = withdrawRecord.getTotalAmount().compareTo(wallet.getFrozenBalance()) < 1;
                if(!verifyBlance || !verifyFrozenBalance)
                {
                    throw new IllegalArgumentException("用户钱包余额、冻结余额不足，审核失败");
                }
                //add by tansitao 时间： 2018/5/1 原因：添加对主币、带币是否足够的判断
                //审核通过
                RPCUtil rpcUtil = new RPCUtil();
                Coin baseCoin = null;
                Coin coin = withdrawRecord.getCoin();
                isTrue(coin != null, "COIN_ILLEGAL");
                //add by tansitao 时间： 2018/7/31 原因：判断是否为内部转账
                if(!walletService.hasExistByAddr(withdrawRecord.getAddress())){
                    //判断平台的该币余额是否足够
                    if(!rpcUtil.balanceIsEnough(interfaceLogService, restTemplate, coin, withdrawRecord.getTotalAmount()))
                    {
                        //余额不足归集钱包
                        rpcUtil.collectCoin(interfaceLogService, restTemplate,  coin);
                        throw new IllegalArgumentException(coin.getUnit() + "余额不足请充值");
                    }
                    //判断是否为带币
                    if (!StringUtils.isEmpty(coin.getBaseCoinUnit()))
                    {
                        //判断主币余额是否足够
                        baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                        if(!rpcUtil.balanceIsEnough(interfaceLogService, restTemplate, baseCoin, withdrawRecord.getBaseCoinFree()))
                        {
                            //余额不足归集钱包
                            rpcUtil.collectCoin(interfaceLogService, restTemplate, baseCoin);
                            throw new IllegalArgumentException(baseCoin.getUnit() + "余额不足请充值");
                        }
                    }
                }

            }
            else if (status == FAIL)
            {
                //审核不通过
                MemberWallet wallet = walletService.findByCoinAndMemberId(withdrawRecord.getCoin(), withdrawRecord.getMemberId());
                notNull(wallet, "wallet null!");
                //edit by tansitao 时间： 2018/5/18 原因：修改操作钱包为sql方式
//                wallet.setBalance(wallet.getBalance().add(withdrawRecord.getTotalAmount()));
//                wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(withdrawRecord.getTotalAmount()));
//                walletService.save(wallet);
                MessageResult result = walletService.thawBalance(wallet, withdrawRecord.getTotalAmount());
                if (result.getCode() != 0)
                {
                    throw new UnexpectedException("not the desired result");
                }
                //add|edit|del by  shenzucai 时间： 2018.10.31  原因：解冻抵扣账户币种账户
                if(!StringUtils.isEmpty(withdrawRecord.getFeeDiscountCoinUnit())) {
                    Coin disCountCoin = coinService.findByUnit(withdrawRecord.getFeeDiscountCoinUnit());
                    MemberWallet wallet1 = walletService.findByCoinAndMemberId(disCountCoin, withdrawRecord.getMemberId());
                    MessageResult result1 = walletService.thawBalance(wallet1, withdrawRecord.getFeeDiscountAmount());
                    if (result1.getCode() != 0) {
                        throw new UnexpectedException("not the desired result");
                    }
                }
            }
            withdrawRecord.setStatus(status);
            withdrawApplyDao.save(withdrawRecord);
        }
    }

    //add by tansitao 时间： 2018/5/18 原因：增加带事务的操作钱包方法
//    @Transactional(rollbackFor = Exception.class)
//    public MessageResult thawBalance(MemberWallet wallet, BigDecimal amount)
//    {
//        MessageResult result = walletService.thawBalance(wallet, amount);
//        return  result;
//    }

    /**
     * 提现成功处理
     *
     * @param withdrawId
     * @param txid
     */
    @Transactional
    public void withdrawSuccess(Long withdrawId, String txid) throws Exception{
        WithdrawRecord record = findOne(withdrawId);
        if (record != null) {
            record.setTransactionNumber(txid);
            record.setStatus(WithdrawStatus.SUCCESS);
            MemberWallet wallet = walletService.findByCoinUnitAndMemberId(record.getCoin().getUnit(), record.getMemberId());
            if (wallet != null) {
                //edit by tansitao 时间： 2018/5/18 原因：修改操作钱包为sql方式
//                wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(record.getTotalAmount()));
                MessageResult result = walletService.decreaseFrozen(wallet.getId(), record.getTotalAmount());
                if (result.getCode() != 0)
                {
                    throw new UnexpectedException("not the desired result");
                }

                //add|edit|del by  shenzucai 时间： 2018.10.31  原因：解冻抵扣账户币种账户
                if(!StringUtils.isEmpty(record.getFeeDiscountCoinUnit())) {
                    Coin disCountCoin = coinService.findByUnit(record.getFeeDiscountCoinUnit());
                    MemberWallet wallet1 = walletService.findByCoinAndMemberId(disCountCoin, record.getMemberId());
                    MessageResult result1 = walletService.decreaseFrozen(wallet1.getId(), record.getFeeDiscountAmount());
                    if (result1.getCode() != 0) {
                        throw new UnexpectedException("not the desired result");
                    }
                }

                MemberTransaction transaction = new MemberTransaction();
                transaction.setAmount(record.getTotalAmount());
                transaction.setSymbol(wallet.getCoin().getUnit());
                transaction.setAddress(wallet.getAddress());
                transaction.setMemberId(wallet.getMemberId());
                transaction.setType(TransactionType.WITHDRAW);
                transaction.setFee(record.getFee());
                transaction.setFeeDiscountCoinUnit(record.getFeeDiscountCoinUnit());
                transaction.setFeeDiscountAmount(record.getFeeDiscountAmount());
                transaction.setComment(record.getComment());
                transactionService.save(transaction);
            }
            //判断是否为带币
            String baseCoin = record.getCoin().getBaseCoinUnit();
            if (!StringUtils.isEmpty(baseCoin))
            {
                //根据主币单位获取币种设置
                Coin coin = coinService.findByUnit(baseCoin);
                if(coin != null){
                    MemberWallet basewallet = walletService.findByCoinUnitAndMemberId(coin.getUnit(), record.getMemberId());
                    if (basewallet != null) {
                        //edit by tansitao 时间： 2018/5/18 原因：修改操作钱包为sql方式
//                        basewallet.setFrozenBalance(basewallet.getFrozenBalance().subtract(record.getBaseCoinFree()));
                        MessageResult result = walletService.decreaseFrozen(basewallet.getId(), record.getBaseCoinFree());
                        if (result.getCode() != 0)
                        {
                            throw new UnexpectedException("not the desired result");
                        }
                        MemberTransaction transaction = new MemberTransaction();
                        transaction.setAmount(record.getBaseCoinFree());
                        transaction.setSymbol(coin.getUnit());
                        transaction.setAddress(basewallet.getAddress());
                        transaction.setMemberId(basewallet.getMemberId());
                        transaction.setType(TransactionType.WITHDRAW);
                        transaction.setFee(new BigDecimal("0"));
                        transactionService.save(transaction);
                    }
                }
            }
        }
    }

    /**
     * 提现失败处理
     *
     * @param withdrawId
     */
    @Transactional
    public void withdrawFail(Long withdrawId) throws Exception{
        //edit by shenzucai 时间： 2018.04.25 原因：修改提币失败判断逻辑 start
        WithdrawRecord record = findOne(withdrawId);
        //  if (record == null || (record.getStatus() != WithdrawStatus.PROCESSING)) 判断状态错误，到失败是已经是wattiing
        if (record != null && (record.getStatus() == WithdrawStatus.WAITING || record.getStatus() == WithdrawStatus.SUCCESS)) {
            //  MemberWallet wallet = walletService.findByCoinAndAddress(record.getCoin(), record.getAddress()); 地址取错了
            MemberWallet wallet = walletService.findByCoinAndMemberId(record.getCoin(), record.getMemberId());
            //edit by shenzucai 时间： 2018.04.25 原因：修改提币失败判断逻辑 end
            if (wallet != null) {
                //edit by tansitao 时间： 2018/5/18 原因：修改操作钱包为sql方式
//                wallet.setBalance(wallet.getBalance().add(record.getTotalAmount()));
//                wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(record.getTotalAmount()));
                MessageResult result = walletService.thawBalance(wallet, record.getTotalAmount());
                if (result.getCode() != 0)
                {
                    throw new UnexpectedException("not the desired result");
                }
                record.setStatus(WithdrawStatus.FAIL);
        }

            //判断是否为带币
            String baseCoin = record.getCoin().getBaseCoinUnit();
            if (!StringUtils.isEmpty(baseCoin))
            {
                //根据主币单位获取币种设置
                Coin coin = coinService.findByUnit(baseCoin);
                if(coin != null){
                    MemberWallet basewallet = walletService.findByCoinUnitAndMemberId(coin.getUnit(), record.getMemberId());
                    if (basewallet != null) {
                        //edit by tansitao 时间： 2018/5/18 原因：修改操作钱包为sql方式
//                        basewallet.setBalance(basewallet.getBalance().add(record.getBaseCoinFree()));
//                        basewallet.setFrozenBalance(basewallet.getFrozenBalance().subtract(record.getBaseCoinFree()));
                        MessageResult result = walletService.thawBalance(basewallet, record.getBaseCoinFree());
                        if (result.getCode() != 0)
                        {
                            throw new UnexpectedException("not the desired result");
                        }
                    }
                }
            }
        }
    }


    /**
     * 提现失败处理
     * @author shenzucai
     * @time 2018.04.25 15:26
     * @param withdrawId
     * @param reason
     * @return true
     */
    @Transactional
    public void withdrawFailMark(Long withdrawId,String reason) throws Exception{
        //edit by shenzucai 时间： 2018.04.25 原因：修改提币失败判断逻辑 start
        WithdrawRecord record = findOne(withdrawId);
        //  if (record == null || (record.getStatus() != WithdrawStatus.PROCESSING)) 判断状态错误，到失败是已经是wattiing
        if (record != null && (record.getStatus() == WithdrawStatus.WAITING || record.getStatus() == WithdrawStatus.SUCCESS)) {
            //  MemberWallet wallet = walletService.findByCoinAndAddress(record.getCoin(), record.getAddress()); 地址取错了
            MemberWallet wallet = walletService.findByCoinAndMemberId(record.getCoin(), record.getMemberId());
            //edit by shenzucai 时间： 2018.04.25 原因：修改提币失败判断逻辑 end
            if (wallet != null) {
                //edit by tansitao 时间： 2018/5/18 原因：修改操作钱包为sql方式
//                wallet.setBalance(wallet.getBalance().add(record.getTotalAmount()));
//                wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(record.getTotalAmount()));
                MessageResult result = walletService.thawBalance(wallet, record.getTotalAmount());
                if (result.getCode() != 0)
                {
                    throw new UnexpectedException("not the desired result");
                }
                record.setStatus(WithdrawStatus.FAIL);
                record.setErrorRemark(reason);
            }

            //判断是否为带币
            String baseCoin = record.getCoin().getBaseCoinUnit();
            if (!StringUtils.isEmpty(baseCoin))
            {
                //根据主币单位获取币种设置
                Coin coin = coinService.findByUnit(baseCoin);
                if(coin != null){
                    MemberWallet basewallet = walletService.findByCoinUnitAndMemberId(coin.getUnit(), record.getMemberId());
                    if (basewallet != null) {
                        //edit by tansitao 时间： 2018/5/18 原因：修改操作钱包为sql方式
//                        basewallet.setBalance(basewallet.getBalance().add(record.getBaseCoinFree()));
//                        basewallet.setFrozenBalance(basewallet.getFrozenBalance().subtract(record.getBaseCoinFree()));
                        MessageResult result = walletService.thawBalance(basewallet, record.getBaseCoinFree());
                        if (result.getCode() != 0)
                        {
                            throw new UnexpectedException("not the desired result");
                        }
                    }
                }
            }
        }

    }

    /**
     * 自动转币失败，转为人工处理
     * @param withdrawId
     */
    @Transactional
    public void autoWithdrawFail(Long withdrawId) {
        WithdrawRecord record = findOne(withdrawId);
        if (record == null || record.getStatus() != WithdrawStatus.PROCESSING) {
            return;
        }
        record.setIsAuto(BooleanEnum.IS_FALSE);
        record.setStatus(WithdrawStatus.PROCESSING);
    }

    @Transactional(readOnly = true)
    public Page<WithdrawRecord> findAllByMemberId(Long memberId, int page, int pageSize, String unit) {
        Sort orders = Criteria.sortStatic("id.desc");
        PageRequest pageRequest = new PageRequest(page, pageSize, orders);
        Criteria<WithdrawRecord> specification = new Criteria<WithdrawRecord>();
        specification.add(Restrictions.eq("memberId", memberId, false));
        specification.add(Restrictions.eq("coin.unit", unit, false));
        return withdrawApplyDao.findAll(specification, pageRequest);
    }

    public Page<WithdrawRecordVO> joinFind(List<Predicate> predicates,PageModel pageModel){
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers() ;
        JPAQuery<WithdrawRecordVO> query = queryFactory.select(
                Projections.fields(WithdrawRecordVO.class,
                        QWithdrawRecord.withdrawRecord.id.as("id"),
                        QWithdrawRecord.withdrawRecord.memberId.as("memberId"),
                        QWithdrawRecord.withdrawRecord.coin.unit,
                        QMember.member.username.as("memberUsername"),
                        QMember.member.realName.as("memberRealName"),
                        QWithdrawRecord.withdrawRecord.totalAmount.as("totalAmount"),
                        QWithdrawRecord.withdrawRecord.arrivedAmount.as("arrivedAmount"),
                        QWithdrawRecord.withdrawRecord.status,
                        QWithdrawRecord.withdrawRecord.isAuto.as("isAuto"),
                        QWithdrawRecord.withdrawRecord.address,
                        QWithdrawRecord.withdrawRecord.createTime.as("createTime"),
                        QWithdrawRecord.withdrawRecord.fee,
                        QWithdrawRecord.withdrawRecord.transactionNumber.as("transactionNumber"),
                        QWithdrawRecord.withdrawRecord.remark,
                        QWithdrawRecord.withdrawRecord.feeDiscountCoinUnit,
                        QWithdrawRecord.withdrawRecord.feeDiscountAmount,
                        QWithdrawRecord.withdrawRecord.comment,
                        //add by shenzucai 时间： 2018.05.25 原因：添加提币失败原因
                        QWithdrawRecord.withdrawRecord.errorRemark)
        ).from(QWithdrawRecord.withdrawRecord,QMember.member).where(predicates.toArray(new BooleanExpression[predicates.size()]));
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        List<WithdrawRecordVO> list = query.offset((pageModel.getPageNo()-1)*pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        long total  = query.fetchCount() ;
        return new PageImpl<>(list,pageModel.getPageable(),total);

    }

    /**
     * 添加范围导出功能
     * @author shenzucai
     * @time 2018.06.13 10:24
     * @param predicates
     * @param pageModel
     * @return true
     */
    public List<WithdrawRecordVO> outExcel(List<Predicate> predicates,PageModel pageModel){
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers() ;
        JPAQuery<WithdrawRecordVO> query = queryFactory.select(
                Projections.fields(WithdrawRecordVO.class,
                        QWithdrawRecord.withdrawRecord.id.as("id"),
                        QWithdrawRecord.withdrawRecord.memberId.as("memberId"),
                        QWithdrawRecord.withdrawRecord.coin.unit,
                        QMember.member.username.as("memberUsername"),
                        QMember.member.realName.as("memberRealName"),
                        QWithdrawRecord.withdrawRecord.totalAmount.as("totalAmount"),
                        QWithdrawRecord.withdrawRecord.arrivedAmount.as("arrivedAmount"),
                        QWithdrawRecord.withdrawRecord.status,
                        QWithdrawRecord.withdrawRecord.isAuto.as("isAuto"),
                        QWithdrawRecord.withdrawRecord.address,
                        QWithdrawRecord.withdrawRecord.createTime.as("createTime"),
                        QWithdrawRecord.withdrawRecord.fee,
                        QWithdrawRecord.withdrawRecord.transactionNumber.as("transactionNumber"),
                        QWithdrawRecord.withdrawRecord.remark,
                        //add by shenzucai 时间： 2018.05.25 原因：添加提币失败原因
                        QWithdrawRecord.withdrawRecord.errorRemark)
        ).from(QWithdrawRecord.withdrawRecord,QMember.member).where(predicates.toArray(new BooleanExpression[predicates.size()]));

        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        // 单词最多导出60000条记录 todo 后续支持更多
        query.limit(60000);
        List<WithdrawRecordVO> list = query.fetch();
        //edit by zyj
        for (int i=0;i<list.size();i++){
            list.get(i).setCreateTimeOut(DateUtil.dateToString(list.get(i).getCreateTime()));//操作时间
            //状态
            list.get(i).setStatusOut(list.get(i).getStatus().getCnName());
            //是否自动提币
            list.get(i).setIsAutoOut(list.get(i).getIsAuto().getNameCn()=="是"?"是":"否");
        }
        return list;

    }

    /**
     * 第三方项目方授权的提币数据分页查询
     * @author fumy
     * @time 2018.09.19 17:25
     * @param symbol
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @ReadDataSource
    public PageInfo<ThirdAuthQueryVo> findWithdrawRecordForThirdAuth(String symbol, int pageNo, int pageSize){
        com.github.pagehelper.Page<ThirdAuthQueryVo> page= PageHelper.startPage(pageNo,pageSize);
        withdrawRecordMapper.queryWithdrawRecord(symbol);
        return page.toPageInfo();
    }
    @ReadDataSource
    public Integer countMemberWithdraw(Long id, String name) {
        return withdrawRecordMapper.countMemberWithdraw(id,name);
    }
}
