package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.constant.TransferDirection;
import com.spark.bitrade.dao.MemberTransactionDao;
import com.spark.bitrade.dto.MemberDepositDTO;
import com.spark.bitrade.dto.MemberTransactionDTO;
import com.spark.bitrade.dto.MemberTransactionDetailDTO;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.entity.QMember;
import com.spark.bitrade.entity.QMemberTransaction;
import com.spark.bitrade.mapper.dao.MemberTransactionCoreMapper;
import com.spark.bitrade.pagination.*;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.vo.MemberTransactionVO;
import com.spark.bitrade.vo.UnlockedGoldKeyAmountVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Service
public class MemberTransactionService extends BaseService {
    @Autowired
    private MemberTransactionDao transactionDao;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private MemberTransactionCoreMapper mapper;


    public PageListMapResult queryDslForPageListResult(QueryDslContext qdc, Integer pageNo, Integer pageSize) {
        JPAQuery<Tuple> jpaQuery = queryFactory.select(qdc.expressionToArray())
                .from(qdc.entityPathToArray())
                .where(qdc.predicatesToArray());
        List<Tuple> tuples = jpaQuery.orderBy(qdc.orderSpecifiersToArray())
                .offset((pageNo - 1) * pageSize).limit(pageSize)
                .fetch();
        List<Map<String, Object>> list = new LinkedList<>();//返回结果
        //封装结果
        for (int i = 0; i < tuples.size(); i++) {
            //遍历tuples
            Map<String, Object> map = new LinkedHashMap<>();//一条信息
            for (Expression expression : qdc.getExpressions()) {
                map.put(expression.toString().split(" as ")[1],//别名作为Key
                        tuples.get(i).get(expression));//获取结果
            }
            list.add(map);
        }
        PageListMapResult pageListMapResult = new PageListMapResult(list, pageNo, pageSize, jpaQuery.fetchCount());//分页封装
        return pageListMapResult;
    }

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<MemberTransaction> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        List<MemberTransaction> list;
        JPAQuery<MemberTransaction> jpaQuery = queryFactory.selectFrom(QMemberTransaction.memberTransaction);
        if (booleanExpressionList != null)
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    /**
     * 保存交易记录
     *
     * @param transaction
     * @return
     */
    public MemberTransaction save(MemberTransaction transaction) {
        return transactionDao.saveAndFlush(transaction);
    }

    public List<MemberTransaction> findAll() {
        return transactionDao.findAll();
    }


    public MemberTransaction findOne(Long id) {
        return transactionDao.findOne(id);
    }


    public List findAllByWhere(Date startTime, Date endTime, TransactionType type, Long memberId) {
        QMemberTransaction qMemberTransaction = QMemberTransaction.memberTransaction;
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        if (startTime != null)
            booleanExpressionList.add(qMemberTransaction.createTime.gt(startTime));
        if (endTime != null)
            booleanExpressionList.add(qMemberTransaction.createTime.lt(endTime));
        if (type != null)
            booleanExpressionList.add(qMemberTransaction.type.eq(type));
        if (memberId != null)
            booleanExpressionList.add(qMemberTransaction.memberId.eq(memberId));
        return queryFactory.selectFrom(qMemberTransaction).
                where(booleanExpressionList.toArray(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()])))
                .fetch();
    }

    public Page<MemberTransaction> queryByMember(Long uid, Integer pageNo, Integer pageSize, TransactionType type, String unit) {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("createTime.desc");
        //分页参数
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        //查询条件
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("type", type, false));
        specification.add(Restrictions.eq("symbol", unit, false));
        return transactionDao.findAll(specification, pageRequest);
    }

    public Page<MemberTransaction> queryByMember(Long uid, Integer pageNo, Integer pageSize, TransactionType type, TransferDirection direction, String startDate, String endDate, String unit) throws ParseException {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("createTime.desc");
        //分页参数
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        //查询条件
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("symbol", unit, false));
        if(type != null){
            specification.add(Restrictions.eq("type",type,false));
        }
        // 转账方向
        if (direction != null && direction.isAvailable(type)) {
            // 转入 amount > 0
            if (direction == TransferDirection.IN) {
                specification.add(Restrictions.gt("amount", BigDecimal.ZERO, false));
            }
            // 转出 amount < 0
            else {
                specification.add(Restrictions.lt("amount", BigDecimal.ZERO, false));
            }
        }

        if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
            specification.add(Restrictions.gte("createTime",DateUtil.YYYY_MM_DD_MM_HH_SS.parse(startDate+" 00:00:00"),false));
            specification.add(Restrictions.lte("createTime",DateUtil.YYYY_MM_DD_MM_HH_SS.parse(endDate+" 23:59:59"),false));
        }
        //update by shushiping 日常需求12.12法币卖出和提现显示为负数
        Page<MemberTransaction> pageInfo = transactionDao.findAll(specification, pageRequest);
        List<MemberTransaction> memberTransactionList = pageInfo.getContent();
        if(memberTransactionList != null && memberTransactionList.size() > 0){
            //法币卖出和提现显示为负数
            memberTransactionList.stream().forEach(item -> {
                if(TransactionType.OTC_SELL.equals(item.getType()) || TransactionType.WITHDRAW.equals(item.getType())){
                    item.setAmount(item.getAmount().multiply(new BigDecimal(-1)));
                }
            });
        }
        return transactionDao.findAll(specification, pageRequest);
    }

    /**
     * 查询交易记录
     * @author tansitao
     * @time 2018/8/23 12:00 
     */
    @ReadDataSource
    public List<MemberTransactionDTO> findMemberTransaction(String startTime, String endTime, String type, long memberId){
        return  mapper.queryByTypeAndTime(startTime, endTime, type, memberId);
    }

    public List<MemberTransaction> findMatchTransaction(Long uid,String symbol){
        Sort orders = Criteria.sortStatic("createTime.asc");
        //查询条件
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("flag",0,false));
        specification.add(Restrictions.eq("symbol",symbol,false));
        specification.add(Restrictions.gt("amount",0,false));
        List<TransactionType> types = new ArrayList<>();
        types.add(TransactionType.RECHARGE);
        types.add(TransactionType.EXCHANGE);
        types.add(TransactionType.ADMIN_RECHARGE);
        specification.add(Restrictions.in("type",types,false));
        List<MemberTransaction> transactions = transactionDao.findAll(specification,orders);
        /*for(MemberTransaction transaction:transactions){
            if(transaction.getType() != TransactionType.RECHARGE && transaction.getType() == TransactionType.EXCHANGE){
                transactions.remove(transaction);
            }
        }*/
        return transactions;
    }

    @Transactional
    public void matchWallet(Long uid,String symbol,BigDecimal amount){
        List<MemberTransaction> transactions = findMatchTransaction(uid,symbol);
        BigDecimal deltaAmount = BigDecimal.ZERO;
        MemberWallet gccWallet = walletService.findByCoinUnitAndMemberId("GCC",uid);
        MemberWallet gcxWallet = walletService.findByCoinUnitAndMemberId("GCX",uid);

        for(MemberTransaction transaction:transactions){
            if(amount.compareTo(deltaAmount) > 0) {
                BigDecimal  amt = amount.subtract(deltaAmount).compareTo(transaction.getAmount()) > 0 ? transaction.getAmount() : amount.subtract(deltaAmount);
                deltaAmount = deltaAmount.add(amt);
                transaction.setFlag(1);
            }
            else break;
        }

        gccWallet.setBalance(gccWallet.getBalance().subtract(deltaAmount));
        gcxWallet.setBalance(gcxWallet.getBalance().add(deltaAmount));

        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(deltaAmount);
        transaction.setSymbol(gcxWallet.getCoin().getUnit());
        transaction.setAddress(gcxWallet.getAddress());
        transaction.setMemberId(gcxWallet.getMemberId());
        transaction.setType(TransactionType.MATCH);
        transaction.setFee(BigDecimal.ZERO);
        //保存配对记录
        save(transaction);
        if(gccWallet.getBalance().compareTo(BigDecimal.ZERO) < 0){
            gccWallet.setBalance(BigDecimal.ZERO);
        }
    }

    public boolean isOverMatchLimit(String day,double limit) throws Exception {
        double totalAmount = 0.00;
        System.out.println("day:"+day+",limit:"+limit);
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("type", TransactionType.MATCH, false));
        Date date1 = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(day+" 00:00:00");
        Date date2 = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(day+" 23:59:59");
        specification.add(Restrictions.gte("createTime", date1, false));
        specification.add(Restrictions.lte("createTime", date2, false));
        List<MemberTransaction> transactions = transactionDao.findAll(specification);
        for(MemberTransaction transaction:transactions){
            totalAmount += transaction.getAmount().doubleValue();
        }
        return totalAmount >= limit;
    }

    //add by yangch 时间： 2018.05.03 原因：合并新增
    public BigDecimal findMemberDailyMatch(Long uid,String day) throws Exception {
        BigDecimal totalAmount = BigDecimal.ZERO;
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("type", TransactionType.MATCH, false));
        Date date1 = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(day+" 00:00:00");
        Date date2 = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(day+" 23:59:59");
        specification.add(Restrictions.gte("createTime", date1, false));
        specification.add(Restrictions.lte("createTime", date2, false));
        specification.add(Restrictions.eq("memberId",uid,false));
        List<MemberTransaction> transactions = transactionDao.findAll(specification);
        for(MemberTransaction transaction:transactions){
            totalAmount = transaction.getAmount().add(totalAmount);
        }
        return totalAmount;
    }

    public Page<MemberTransactionVO> joinFind(List<Predicate> predicates, PageModel pageModel){
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers() ;
        JPAQuery<MemberTransactionVO> query = queryFactory.select(Projections.fields(MemberTransactionVO.class,
                QMemberTransaction.memberTransaction.address,
                QMemberTransaction.memberTransaction.amount,
                QMemberTransaction.memberTransaction.createTime.as("createTime"),
                QMemberTransaction.memberTransaction.fee,
                QMemberTransaction.memberTransaction.flag,
                QMemberTransaction.memberTransaction.id.as("id"),
                QMemberTransaction.memberTransaction.symbol,
                QMemberTransaction.memberTransaction.type,
                QMemberTransaction.memberTransaction.comment,
                QMember.member.username.as("memberUsername"),
                QMember.member.mobilePhone.as("phone"),
                QMember.member.email,
                QMember.member.realName.as("memberRealName"),
                QMember.member.id.as("memberId")))
                .from(QMemberTransaction.memberTransaction, QMember.member);
        predicates.add(QMemberTransaction.memberTransaction.memberId.eq(QMember.member.id));
        query.where(predicates.toArray(new BooleanExpression[predicates.size()]));
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        List<MemberTransactionVO> list = query.offset((pageModel.getPageNo()-1)*pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        long total = query.fetchCount();
        return new PageImpl<>(list, pageModel.getPageable(), total);
    }

    /**
     * 分页查找交易记录MemberTransaction
     * @author Zhang Yanjun
     * @time 2018.09.06 17:11
     * @param pageNo
     * @param pageSize
     * @param map
     */
    @ReadDataSource
    public PageInfo<MemberTransactionDetailDTO> findBy(int pageNo, int pageSize, Map<String,Object> map){
        com.github.pagehelper.Page<MemberTransactionDetailDTO> page= PageHelper.startPage(pageNo,pageSize);
//        this.mapper.findBy(map);
        this.findByUtil(map);
        return page.toPageInfo();
    }
    private List<MemberTransactionDetailDTO> findByUtil(Map<String,Object> map){
        List<MemberTransactionDetailDTO> list = this.mapper.findBy(map);
        for (int i=0;i<list.size();i++){
            //交易时间
            list.get(i).setCreateTime(list.get(i).getCreateTime().substring(0,list.get(i).getCreateTime().length()-2));
            //交易类型
            list.get(i).setTypeOut(list.get(i).getType().getCnName());
        }
        return list;
    }

    /**
     * 导出交易记录MemberTransaction Excel
     * @author Zhang Yanjun
     * @time 2018.09.06 17:13
     * @param map
     */
    @ReadDataSource
    public List<MemberTransactionDetailDTO> findAllByMemberTransactionForOut(Map<String,Object> map){
//        return  this.mapper.findBy(map);
        return this.findByUtil(map);
    }

    /**
     * 分页查询用户金钥匙活动解锁金钥匙记录
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ReadDataSource
    public PageInfo<UnlockedGoldKeyAmountVo> findReleaseGoldenKeyRecords(Long memberId, int pageNo, int pageSize) {
        com.github.pagehelper.Page<UnlockedGoldKeyAmountVo> page = PageHelper.startPage(pageNo, pageSize);
        //PageHelper会自动拦截到下面这查询sql
        mapper.findGoldenKeyTransactions(memberId);
        return page.toPageInfo();
    }

    public PageInfo<MemberDepositDTO> findMemberRechargeRecord(long memberId, int pageNo, int pageSize, String unit) {
        com.github.pagehelper.Page<MemberDepositDTO> page = PageHelper.startPage(pageNo, pageSize);
       mapper.findMemberRechargeRecord(memberId,unit);


        return page.toPageInfo();
    }
}
