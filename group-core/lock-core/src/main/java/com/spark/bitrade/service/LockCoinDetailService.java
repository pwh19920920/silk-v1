package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.LockBttcOfflineWalletDao;
import com.spark.bitrade.dao.LockBttcRestitutionIncomePlanDao;
import com.spark.bitrade.dao.LockCoinDetailDao;
import com.spark.bitrade.dao.MemberTransactionDao;
import com.spark.bitrade.dto.LockCoinActivitieProjectDto;
import com.spark.bitrade.dto.LockCoinDetailVo;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.LockCoinDetailMapper;
import com.spark.bitrade.mapper.dao.QuantifyLockReWardMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.vo.UnlockedGoldKeyAmountVo;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.entity.QLockBttcRestitutionIncomePlan.lockBttcRestitutionIncomePlan;

/**
 * @author zhang yingxin
 * @date 2018/5/7
 */
@Service
public class LockCoinDetailService extends BaseService {
    @Autowired
    private LockCoinDetailDao lockCoinDetailDao;

    @Autowired
    private LockCoinDetailMapper mapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    QuantifyLockReWardMapper quantifyLockReWardMapper;
    @Autowired
    private LockRewardLevelConfigService levelConfigService;
    @Autowired
    private ILockBttcOfflineWalletService iLockBttcOfflineWalletService;
    @Resource
    private LockBttcOfflineWalletDao lockBttcOfflineWalletDao;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LockBttcRestitutionIncomePlanService lockBttcRestitutionIncomePlanService;
    @Autowired
    private LockBttcRestitutionIncomePlanDao lockBttcRestitutionIncomePlanDao;
    @Autowired
    private MemberTransactionDao memberTransactionDao;

    /**
     * 初始节点（使用的是lock_reward_level_config.id的值）
     * （最小的有效等级，用于判定达成奖励的最低等级条件：本人锁仓5万360天 或者业绩10万以上 ）
     */
    @Value("${lock.valid.min.levelId:1}")
    private Integer lockValidMinLevelId;
    /**
     * 本人锁仓5万360天
     */
    @Value("${lock.need.amount:50000}")
    private BigDecimal needLockAmount;

    /**
     * 本人锁仓5万360天
     */
    @Value("${lock.need.period:360}")
    private Integer needLockPeriod;


    @PersistenceContext
    protected EntityManager em;

    public List<LockCoinDetail> getAll() {
        return lockCoinDetailDao.findAll();
    }

    public Page<LockCoinDetail> list(Predicate predicate, PageModel pageModel) {
        return lockCoinDetailDao.findAll(predicate, pageModel.getPageable());
    }

    public List<LockCoinDetail> findAll(Predicate predicate) {
        return (List<LockCoinDetail>) lockCoinDetailDao.findAll(predicate);
    }

    public Page<LockCoinDetail> findAll(Predicate predicate, Pageable pageable) {
        return lockCoinDetailDao.findAll(predicate, pageable);
    }

    public LockCoinDetail findOne(long id) {
        return lockCoinDetailDao.findById(id);
    }

    /**
     *  * 通过id和type查询锁仓记录
     *  * @author tansitao
     *  * @time 2018/7/2 10:19 
     *  
     */
    @ReadDataSource
    public LockCoinDetail findOneByIdAndType(long id, LockType lockType) {
        return mapper.findOneByIdAndType(id, lockType.getOrdinal());
    }

    /**
     *  * 通过id和类型和用户id查询锁仓详情
     *  * @author tansitao
     *  * @time 2018/7/31 14:15 
     *  
     */
    @ReadDataSource
    public LockCoinDetail findOneByIdAndTypeAndMemberId(long id, LockType lockType, long memberId) {
        return mapper.findOneByIdAndTypeAndMemberId(id, lockType.getOrdinal(), memberId);
    }

    /**
     *  * 更新锁仓记录状态
     *  * @author tansitao
     *  * @time 2018/8/3 11:05 
     *  
     */
    @Transactional
    public int updateLockStatus(LockStatus lockStatus, LockStatus lodLockStatus, long id) {
        return lockCoinDetailDao.updateLockCoinStatus(id, lockStatus, lodLockStatus);
    }

    /**
     * 修改锁仓剩余金额
     *
     * @param id     锁仓id
     * @param amount 新的剩余金额
     * @return 修改结果
     * @author dengdy
     * @time 2019/5/20 11:08
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRemainAmount(Long id, BigDecimal amount) {
        int result = lockCoinDetailDao.updateRemainAmount(id, amount);
        if (result > 0) {
            return true;
        }
        return false;
    }

//    public List<LockCoinDetail> findByMemberAndStatus(Member member, DepositStatusEnum status){
//        return depositRecordDao.findByMemberAndStatus(member,status);
//    }

    //add by tansitao 时间： 2018/11/21 原因：清空缓存
    @CacheEvict(cacheNames = "LockCoinDetail", key = "'entity:LockCoinDetail:' + #lockCoinDetail.getCoinUnit() + ':' + #lockCoinDetail.getMemberId()")
    public LockCoinDetail save(LockCoinDetail lockCoinDetail) {
        return lockCoinDetailDao.saveAndFlush(lockCoinDetail);
    }

    /**
     * 修改返佣状态
     * * @param id 活动记录ID
     *
     * @param lockRewardSatusOld 修改前的状态
     * @param lockRewardSatusNew 修改后的状态
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateLockRewardSatus(Long id, LockRewardSatus lockRewardSatusOld, LockRewardSatus lockRewardSatusNew) {
        return lockCoinDetailDao.updateLockRewardSatus(id, lockRewardSatusOld, lockRewardSatusNew);
    }

    /**
     * 查询商家锁仓记录
     *
     * @param memberId
     * @param type
     * @return true
     * @author fumy
     * @time 2018.06.21 17:03
     */
    public List<CustomerLockCoinDetail> findByMemberIdAndType(long memberId, LockType type) {
        return lockCoinDetailDao.findByMemberIdAndType(memberId, type);
    }

    /**
     * 分页查询金钥匙活动用户锁仓记录
     *
     * @param memberId
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageInfo<CustomerLockCoinDetail> findGoldenKeyLockRecords(long memberId, LockType type,
                                                                     int pageNo, int pageSize) {
        com.github.pagehelper.Page<CustomerLockCoinDetail> page = PageHelper.startPage(pageNo, pageSize);
        mapper.findGoldenKeyLockRecords(memberId, type);
        return page.toPageInfo();
    }

    /**
     * 获取用户SLB节点产品的购买总金额
     *
     * @param memberId
     * @return true
     * @author fumy
     * @time 2018.07.25 10:29
     */
    public BigDecimal getUserQuantifyTotalCny(Long memberId) {
        return mapper.queryUserQuantifyTotalCny(memberId);
    }


    public Member getRewardMember(Long memberId) {
        if (memberId == null) {
            return null;
        }
        Member member = memberService.findOne(memberId);
        QuantifyLockReWard quantifyLockReWard = quantifyLockReWardMapper.getByMemberId(member.getId());
        if (quantifyLockReWard != null) {
            return member;
        }
        if (member == null) {
            return null;
        }
        //查询上级用户是否存在返佣资格表
        if (member.getInviterId() == null) {//如果无上级邀请人则直接返回null
            return null;
        }
        return getRewardMember(member.getInviterId());
    }

    /**
     * 按条件分页查询SLB节点产品VIP
     *
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return true
     * @author fumy
     * @time 2018.08.28 14:42
     */
    @ReadDataSource
    @Cacheable(cacheNames = "quantifyLockReWard", key = "'entity:quantifyLockReWard:All'")
    public PageInfo<QuantifyLockReWard> findByPage(Long memberId, int pageNo, int pageSize) {
        com.github.pagehelper.Page<QuantifyLockReWard> page = PageHelper.startPage(pageNo, pageSize);
        quantifyLockReWardMapper.getByPage(memberId);
        return page.toPageInfo();
    }

    /**
     * 添加SLB节点产品vip
     *
     * @param memberId
     * @return true
     * @author fumy
     * @time 2018.08.28 17:07
     */
    @CacheEvict(cacheNames = "quantifyLockReWard", key = "'entity:quantifyLockReWard:All'")
    public QuantifyLockReWard addVip(Long memberId) {
        int status = 0;//默认值
        Date date = new Date();
        String createTime = DateUtil.dateToString(date);
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("status", status);
        params.put("createTime", createTime);
        Long id = quantifyLockReWardMapper.insertVip(params);

        QuantifyLockReWard quantifyLockReWard = new QuantifyLockReWard();
        quantifyLockReWard.setId(id);
        quantifyLockReWard.setMemberId(memberId);
        quantifyLockReWard.setStatus(status);
        quantifyLockReWard.setCreateTime(date);
        return quantifyLockReWard;
    }

    /**
     * 根据会员id查询
     *
     * @param memberId
     * @return true
     * @author fumy
     * @time 2018.08.29 14:52
     */
    public QuantifyLockReWard findOneByMemberId(Long memberId) {
        return quantifyLockReWardMapper.getByMemberId(memberId);
    }


    /**
     * 查询用户解锁时间
     *
     * @param smsSendStatus
     * @return
     * @author lingxing
     */
    @ReadDataSource
    public PageInfo<LockCoinDetailVo> getUserPlanUnlockTimeAndSmsSendStatus(int smsSendStatus, int pageNum, int pageSize) {
        com.github.pagehelper.Page<LockCoinDetailVo> page = PageHelper.startPage(pageNum, pageSize);
        //add by tansitao 时间： 2018/11/27 原因：通过短信状态和锁仓类型查询锁仓记录
        mapper.findByPlanUnlockTimeAndSmsSendStatus(smsSendStatus, LockType.QUANTIFY.getOrdinal());
        return page.toPageInfo();
    }

    /**
     * 批量修改
     *
     * @return
     * @author lingxing
     */
    @Transactional
    public void batchUpdate(List<LockCoinDetailVo> list, int j) {
        if (list != null && list.size() != 0) {
            for (int i = 0; i < list.size(); i++) {
                LockCoinDetail lockCoinDetail = em.find(LockCoinDetail.class, list.get(i).getId());
                //获取传入的j假如不等于0说明云片链接不通
                if (j != 0) {
                    lockCoinDetail.setSmsSendStatus(SmsSendStatus.FAIL_SEND_SMS);
                }
                //发送短信成功，更改状态
                if (lockCoinDetail != null) {
                    lockCoinDetail.setSmsSendStatus(list.get(i).getSmsSendStatus());
                    //循环完的时候将写入数据库
                    if (list.size() - 1 == i) {
                        em.flush();
                        em.clear();
                    }
                }
            }
        }
    }

    /**
     *  * 获取某个币种的所有锁仓收益
     *  * @author tansitao
     *  * @time 2018/11/20 13:56 
     *  
     */
    @Cacheable(cacheNames = "LockCoinDetail", key = "'entity:LockCoinDetail:' + #unit + ':' + #memberId")
    public BigDecimal getTotalInCome(Long memberId, String unit) {
        return mapper.queryTotalInCome(memberId, unit, LockCoinActivitieType.COIN_REWARD.getOrdinal());
    }

    /**
     * 根据会员id和类型查询已锁定和解锁中的记录
     *
     * @param memberId
     * @param type
     * @author Zhang Yanjun
     * @time 2018.12.28 11:13
     */
    @ReadDataSource
    public List<LockCoinDetail> findByMemberIdAndType(Long memberId, LockType type) {
        return mapper.findByMemberIdAndType(memberId, type);
    }

    /**
     * 查询会员参加某一活动的锁仓记录
     *
     * @param memberId 会员ID
     * @param id       活动ID
     * @return 活动列表
     * @author wsy
     * @time 2019年4月17日11:12:48
     */
    public List<LockCoinDetail> findByMemberIdAndId(Long memberId, Long id) {
        return mapper.findByMemberIdAndId(memberId, id);
    }

    /**
     * 统计STO增值计划所有收益和未解锁锁仓数
     *
     * @return lockAmount=锁仓总数量，incomeAmount=计划总收益
     * @author Zhang Yanjun
     * @time 2019.01.18 9:51
     */
    public Map<String, Object> statTotalAndIncome(Long memberId, LockType type, String symbol) {
        return mapper.statTotalAndIncome(memberId, type.getOrdinal(), symbol);
    }

    /**
     * 检查用户的锁仓是否满足新等级条件
     *
     * @param lockMarketRewardDetail
     * @return
     */
    public boolean checkMemberRewardQualification(LockMarketRewardDetail lockMarketRewardDetail, String symbol) {
        //等级大于初始节点等级的都任务已满足奖励条件
        if (lockMarketRewardDetail.getCurrentLevelId() >= lockValidMinLevelId) {
            return true;
        }
        return matchMemberPerformanceAndLockCondition(lockMarketRewardDetail.getMemberId(), symbol);
    }

    /**
     * 检查用户的锁仓是否满足新等级条件
     *
     * @param memberId
     * @return
     */
    public boolean matchMemberPerformanceAndLockCondition(Long memberId, String symbol) {
        //等级配置
        LockRewardLevelConfig levelConfig =
                getValidFirstLevelConfig(symbol);
        return matchMemberPerformanceAndLockCondition(memberId, levelConfig);
    }

    /**
     * 获取“初级节点”
     *
     * @return
     */
    public LockRewardLevelConfig getValidFirstLevelConfig(String symbol) {
        return levelConfigService.getLevelConfigById(lockValidMinLevelId, symbol);
    }

    /**
     * 匹配“本人锁仓5万360天 或者业绩10万以上”条件
     *
     * @param memberId
     * @param levelConfig
     * @return
     */
    public boolean matchMemberPerformanceAndLockCondition(Long memberId, LockRewardLevelConfig levelConfig) {
        // 判断用户的子部门业绩是否满足条件（达到100,000数量）
        BigDecimal subAmount = mapper.queryMemberSubAmount(memberId, levelConfig.getSymbol());
        if (subAmount != null && subAmount.compareTo(levelConfig.getPerformanceTotal()) >= 0) {
            return true;
        }
        return matchMemberLockCondition(memberId, levelConfig.getSymbol());
    }

    /**
     * 匹配“本人锁仓5万360天”条件
     *
     * @param memberId
     * @return
     */
    public boolean matchMemberLockCondition(Long memberId, String symbol) {
        // 查询是否存在满足条件的数据（单条锁仓数量达到50000，周期360天）
        BigDecimal lockAmount = mapper.queryQualificationDetailByMemberId(memberId, needLockPeriod, symbol);
        //大于0 表示存在满足新等级条件的锁仓记录
        if (lockAmount != null && lockAmount.compareTo(needLockAmount) >= 0) {
            return true;
        }

        return false;
    }


    @Transactional(rollbackFor = Exception.class)
    public void unlockIeoDetailById(Long id, BigDecimal waitingRate, BigDecimal lockRate, BigDecimal unlockRate, int times, long idd, int circleDays) throws Exception {
        try {
            LockCoinDetail lockCoinDetail = findOne(id);
            Assert.notNull(lockCoinDetail, "没查询到此锁仓活动");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, circleDays);
            Date circleDate = cal.getTime();
            /*
            1.找到某个人的IEO锁仓
            2.[锁仓数量]/5为（每一期解仓的数量）,每一期*0.9为(每一期增加待解锁金钥匙数量),每一期*0.1为(每一期锁仓数量)
            3.（累计锁仓数） = 原锁仓数 + 每一期锁仓数。累计锁仓数*N为 （每一期解锁金钥匙数量）
            4. lock_coin_detail------->[锁仓数量]
              (每一期增加待解锁金钥匙数量)------->lock_bttc_offline_wallet
              (每一期锁仓数量)----->（累计锁仓数）------>lock_coin_detail
             （每一期解锁金钥匙数量）------->member_wallet
             */

//            int times = 5;
//            BigDecimal waitingRate = new BigDecimal("0.9");
//            BigDecimal lockRate = BigDecimal.ONE.subtract(waitingRate);
//            BigDecimal unlockRate = new BigDecimal("0.2");
//            //验证活动配置和锁仓配置是否存在
//            long idd = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
            //锁仓剩余数量
            BigDecimal remainAmount = lockCoinDetail.getRemainAmount();
            //[锁仓数量]
            BigDecimal totalAmount = lockCoinDetail.getTotalAmount();
            //（每一期解仓的数量）
            BigDecimal oneTimeAmount = totalAmount.divide(new BigDecimal(times), 8, BigDecimal.ROUND_HALF_DOWN);

            //-------------解仓上一期----------
            int nowTime = 1;
            BigDecimal lastAmount = BigDecimal.ZERO;
            LockBttcRestitutionIncomePlan lbPlan = lockBttcRestitutionIncomePlanService.findLastPlanByMemberIdAndDetailId(lockCoinDetail.getMemberId(), id);
            BigDecimal leftAmount = BigDecimal.ZERO;
            BigDecimal lastPlanAmount = BigDecimal.ZERO;
            if (lbPlan != null) {
                LockCoinDetail lastDetail = lockCoinDetailDao.findById(lbPlan.getLockDetailId());
                if (lastDetail == null) {
                    throw new Exception("未找到锁仓活动或锁仓活动已经解仓");
                }
                lastAmount = lastDetail.getTotalAmount();
                leftAmount = lastDetail.getTotalCNY();
                nowTime = lbPlan.getPeriod();
                lastDetail.setStatus(LockStatus.UNLOCKED);
                lastPlanAmount = lbPlan.getRestitutionAmount();
//                lockCoinDetailDao.save(lastDetail);
                lockCoinDetailDao.updateLockCoinStatus(lastDetail.getId(), LockStatus.UNLOCKED, LockStatus.LOCKED);
//                lbPlan.setStatus(LockBackStatus.BACKED);
                //上期解仓完毕
                lockBttcRestitutionIncomePlanService.updateStatus(lbPlan.getId(), LockBackStatus.BACK, LockBackStatus.BACKED);
            }
            //-------------锁仓下一期----------
            Long nextDetailId = null;
            //(每一期锁仓数量)
            BigDecimal lockBttcAmount = BigDecimal.ZERO;
            BigDecimal nowAmount = BigDecimal.ZERO;
            boolean b = false;
            //剩余量充足，既前5期
            if (remainAmount.compareTo(oneTimeAmount) >= 0) {
                // 新增交易记录 以储存释放金钥匙金额，保持事务一致性，交易记录作为星客账户金钥匙增加记录
//                if(lastAmount.compareTo(BigDecimal.ZERO)>0){
//                    MemberTransaction memberTransaction = new MemberTransaction();
//                    memberTransaction.setMemberId(lockCoinDetail.getMemberId());
//                    memberTransaction.setAmount(lastAmount);
//                    memberTransaction.setType(TransactionType.GOLD_KEY_OWN);
//                    memberTransaction.setSymbol("BTTC");
//                    memberTransaction.setRefId(id+"");
//                    memberTransaction.setComment("IEO-BTTC金钥匙账户");
//                    memberTransactionService.save(memberTransaction);
//                    memberTransactionDao.updateCreateTime(new Date(), memberTransaction.getId());
//                }
                lockBttcAmount = oneTimeAmount.multiply(lockRate);
                nowAmount = lastAmount.add(lockBttcAmount);
                lockCoinDetailDao.updateRemainAmountAndPlanUnlockTime(lockCoinDetail.getId(), lockCoinDetail.getRemainAmount().subtract(oneTimeAmount), circleDate);
                nextDetailId = iLockBttcOfflineWalletService.lockReleaseGoldenKeyForIeo(lockCoinDetail.getMemberId(), nowAmount, id, oneTimeAmount.multiply(waitingRate), unlockRate, false, BigDecimal.ZERO, false, leftAmount.compareTo(BigDecimal.ZERO) == 0 ? lockCoinDetail.getTotalAmount().multiply(waitingRate) : leftAmount);
            } else if (remainAmount.compareTo(new BigDecimal("0.001")) > 0) {
                //有剩余IEO未解仓数。解仓完毕再进入计算解锁多少金钥匙
                nowAmount = lastAmount.add(remainAmount.multiply(lockRate));
                oneTimeAmount = remainAmount.multiply(lockRate);
                lockCoinDetailDao.updateRemainAmountAndPlanUnlockTime(lockCoinDetail.getId(), BigDecimal.ZERO, circleDate);
                nextDetailId = iLockBttcOfflineWalletService.lockReleaseGoldenKeyForIeo(lockCoinDetail.getMemberId(), nowAmount, id, oneTimeAmount.multiply(waitingRate), unlockRate, false, BigDecimal.ZERO, false, leftAmount);
            } else {
                //IEO前5期解仓完毕，判断是否进入循环锁仓
                if (leftAmount.compareTo(new BigDecimal("0.001")) > 0) {
                    if (nowTime <= times) {
                        throw new Exception("IEO锁仓返还期数未满");
                    }

                    if (leftAmount.compareTo(totalAmount.multiply(lockRate).multiply(unlockRate)) < 0) {
                        lastAmount = leftAmount.divide(unlockRate, 8, BigDecimal.ROUND_DOWN);
                        b = true;
                    }
                    oneTimeAmount = BigDecimal.ZERO;
                    nowAmount = lastAmount;
                    lockCoinDetailDao.updateRemainAmountAndPlanUnlockTime(lockCoinDetail.getId(), BigDecimal.ZERO, circleDate);
                    nextDetailId = iLockBttcOfflineWalletService.lockReleaseGoldenKeyForIeo(lockCoinDetail.getMemberId(), nowAmount,
                            id, oneTimeAmount.multiply(waitingRate), unlockRate, false, BigDecimal.ZERO, false, leftAmount);
                } else {
                    //开始最后5期返还
                    //计算本期解仓数
                    BigDecimal bAmount = oneTimeAmount.multiply(lockRate);
                    if (lastAmount.compareTo(BigDecimal.ZERO) > 0) {
                        MemberTransaction memberTransaction = new MemberTransaction();
                        memberTransaction.setMemberId(lockCoinDetail.getMemberId());
                        memberTransaction.setAmount(lastAmount);
                        memberTransaction.setType(TransactionType.GOLD_KEY_OWN);
                        memberTransaction.setSymbol("BTTC");
                        memberTransaction.setRefId(id == null ? "0" : id + "");
                        memberTransaction.setComment("IEO-BTTC金钥匙账户");
                        memberTransactionService.save(memberTransaction);
                        memberTransactionDao.updateCreateTime(new Date(), memberTransaction.getId());
                        nowAmount = lastAmount.subtract(bAmount);
                        if (totalAmount.multiply(lockRate).compareTo(lastPlanAmount.subtract(new BigDecimal("0.001"))) >= 0
                                && totalAmount.multiply(lockRate).compareTo(lastPlanAmount.add(new BigDecimal("0.001"))) <= 0) {
                            nowAmount = totalAmount.multiply(lockRate).subtract(bAmount);
                        }

                        lockCoinDetailDao.updateRemainAmountAndPlanUnlockTime(lockCoinDetail.getId(), BigDecimal.ZERO, circleDate);
                        nextDetailId = iLockBttcOfflineWalletService.lockReleaseGoldenKeyForIeo(lockCoinDetail.getMemberId(), nowAmount, id, BigDecimal.ZERO, unlockRate, true, bAmount, false, leftAmount);
                    }
//                    if(lastAmount.compareTo(BigDecimal.ZERO)==0){
//
//                    }
                }

            }
            //计算下一期
            if (nextDetailId != null) {
                LockBttcRestitutionIncomePlan plan = new LockBttcRestitutionIncomePlan();
                plan.setMemberId(lockCoinDetail.getMemberId());
                //无用项，暂时保留
                plan.setRestitutionAmount(b ? totalAmount.multiply(lockRate) : nowAmount);
                plan.setComment(String.format("第%d期", nowTime + 1));
                plan.setLockDetailId(nextDetailId);
                plan.setPeriod(id.intValue());
                plan.setStatus(LockBackStatus.BACK);
                plan.setSymbol("BTTC");
                plan.setRewardTime(circleDate);
                plan.setCreateTime(new Date());
                lockBttcRestitutionIncomePlanDao.save(plan);
            } else {
                lockCoinDetailDao.updateLockCoinStatus(lockCoinDetail.getId(), LockStatus.UNLOCKED, LockStatus.LOCKED);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 修改剩余锁仓金额和锁仓状态
     * @param amount
     * @param status
     * @param id
     * @return
     */
    public int updateRemainAmountAndLockStatus(BigDecimal amount, LockStatus status,Long id){
        return lockCoinDetailDao.updateRemainAmountAndLockStatus(amount,status,id);
    }

    /**
     * 查询当前用户参与的结束活动
     * @return
     */
    public List<LockCoinDetail> selectByMemberId(Long memberId){
        return lockCoinDetailDao.selectByMemberId(memberId);
    }

    /**
     * 查询当前语言的活动配置
     * @param projectId
     * @param type
     * @return
     */
    public LockCoinActivitieProjectInternational selectInternational(Long projectId,Integer type){
        return mapper.selectInternational(projectId,type);
    }

    public LockCoinActivitieSettingInternational selectInternationalSetting(Long settingId,Integer type){
        return mapper.selectInternationalSetting(settingId,type);
    }
}
