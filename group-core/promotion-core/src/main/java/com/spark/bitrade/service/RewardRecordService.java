package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PromotionRewardCycle;
import com.spark.bitrade.constant.RewardRecordStatus;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dao.MemberTransactionDao;
import com.spark.bitrade.dao.MemberWalletDao;
import com.spark.bitrade.dao.RewardRecordDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Service
public class RewardRecordService extends BaseService {
    @Autowired
    private RewardRecordDao rewardRecordDao;

    @Autowired
    private MemberWalletDao memberWalletDao;

    @Autowired
    private MemberTransactionDao transactionDao;

    public RewardRecord save(RewardRecord rewardRecord){
        return rewardRecordDao.save(rewardRecord);
    }

    //edit by tansitao 时间： 2018/5/23 原因：修改为分页方式
    public Page<RewardRecord> queryRewardPromotionList(Long uid, Integer pageNo, Integer pageSize) throws ParseException {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("createTime.desc");
        //分页参数
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        //查询条件
        Criteria<RewardRecord> specification = new Criteria<RewardRecord>();
        specification.add(Restrictions.eq("member.id", uid, false));

        return rewardRecordDao.findAll(specification, pageRequest);
    }

    //add by yangch 时间： 2018.04.29 原因：合并
    public Map<String,BigDecimal> getAllPromotionReward(long memberId,RewardRecordType type){
        List<Object[]> list = rewardRecordDao.getAllPromotionReward(memberId,type.getOrdinal());
        Map<String,BigDecimal> map = new HashMap<>() ;
        for(Object[] array:list){
            map.put(array[0].toString(),(BigDecimal)array[1]);
        }
        return map ;
    }

    //add by tansitao 时间： 2018/5/29 原因：增加合伙人分页查询
    public Page<RewardRecord> queryRewardPartnerList(Long uid, Integer pageNo, Integer pageSize) throws ParseException {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("createTime.desc");
        //分页参数
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        //查询条件
        Criteria<RewardRecord> specification = new Criteria<RewardRecord>();
        specification.add(Restrictions.eq("member.id", uid, false));
        specification.add(Restrictions.eq("type", RewardRecordType.PARTNER, false));

        return rewardRecordDao.findAll(specification, pageRequest);
    }

    //add by tansitao 时间： 2018/5/29 原因：添加按照合伙人分页查询
    public Page<RewardRecord> findAll(Predicate predicate, Pageable pageable) {
        return rewardRecordDao.findAll(predicate, pageable);
    }

    /**
      * 准备发放指定条件的佣金，修改状态未 发放中
     *
      * @author yangch
      * @time 2018.06.01 9:50 
     * @param type 佣金发放类型
     * @param status 返佣状态
     * @param promotionRewardCycle 返佣周期
     * @param startTime 返佣开始范围
     * @param endTime   返佣结束范围
     */
    @Transactional(rollbackFor = Exception.class)
    public int preparePayReward(RewardRecordType type , RewardRecordStatus status,
                                 PromotionRewardCycle cycle,
                                 Date startTime, Date endTime) {
        return rewardRecordDao.preparePayReward(type.getOrdinal(), status.getOrdinal(), cycle.getOrdinal(), startTime, endTime);
    }

    /***
     * 根据指定的条件查询前1000条记录
      *
     * @author yangch
     * @time 2018.06.01 13:44 
     * @param status 返佣状态
     * @param type 佣金发放类型
     * @param cycle 返佣周期
     */
    public List<RewardRecord> findTopNByStatusAndTypeAndRewardCycle(RewardRecordStatus status, RewardRecordType type, PromotionRewardCycle cycle){
        return  rewardRecordDao.findFirst10000ByStatusAndTypeAndRewardCycle(status, type, cycle);
    }

    /**
     * 发放失败
     * yangch 2018-06-01 14:27:42
     * @param rewardRecord 返佣记录
     * @param expectStatus 当前返佣记录的状态（修改前的状态）
     * @return
     */
    @Transactional(noRollbackFor = Exception.class)
    public int payRewardFailed(RewardRecord rewardRecord, RewardRecordStatus expectStatus){
        return rewardRecordDao.updateStatusByIdAndStatus(rewardRecord.getId(), expectStatus.getOrdinal(), RewardRecordStatus.FAILED.getOrdinal());
    }
    /**
     * 发放成功
     * yangch 2018-06-01 14:27:42
     * @param rewardRecord 返佣记录
     * @param expectStatus 当前返佣记录的状态（修改前的状态）
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int payRewardSuccessed(RewardRecord rewardRecord, RewardRecordStatus expectStatus){
        return rewardRecordDao.updateStatusByIdAndStatus(rewardRecord.getId(), expectStatus.getOrdinal(), RewardRecordStatus.TREATED.getOrdinal());
    }

    /**
     * 发放佣金
     *
     * @param rewardRecord
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void payReward(RewardRecord rewardRecord) throws Exception {
        //添加账户的余额
        MemberWallet memberWallet = memberWalletDao.findByCoinAndMemberId(rewardRecord.getCoin(), rewardRecord.getMember().getId());
        int rstBalance = memberWalletDao.increaseBalance(memberWallet.getId(), rewardRecord.getAmount());
        if(rstBalance <=0){
            //payRewardFailed(rewardRecord, RewardRecordStatus.TREATING);
            throw new UnexpectedException(MessageFormat.format("返佣失败，账户余额增加失败。memberWallet.id={0},amount={1}", memberWallet.getId(), rewardRecord.getAmount()));
        }

        //添加交易记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(rewardRecord.getAmount());
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setFeeDiscount(BigDecimal.ZERO);
        memberTransaction.setMemberId(rewardRecord.getMember().getId());
        memberTransaction.setSymbol(rewardRecord.getCoin().getUnit());
        memberTransaction.setType(TransactionType.EXCHANGE_PARTNER_AWARD);    //交易记录为“13=EXCHANGE_PARTNER_AWARD("币币交易合伙人奖励")”
        memberTransaction.setRefId(String.valueOf(rewardRecord.getId()));         //关联“reward_record”表中对应的返佣记录
        transactionDao.save(memberTransaction);

        //修改返佣记录的状态
        payRewardSuccessed(rewardRecord, RewardRecordStatus.TREATING);
    }

}
