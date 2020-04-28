package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.dao.LockIeoRestitutionIncomePlanDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.LockDetailMapper;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
  * 解锁锁仓活动服务实现类
  * @author tansitao
  * @time 2018/7/19 11:16 
  */
@Service
@Slf4j
public class LockDetailServiceImpl extends ServiceImpl<LockDetailMapper, LockCoinDetail> implements ILockDetailService {

    @Autowired
    private LockDetailMapper lockDetailMapper;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UnLockCoinDetailService unLockCoinDetailService;

    @Autowired
    private UnlockCoinTaskService unlockCoinTaskService;

    @Autowired
    private LockRewardReturnService lockRewardReturnService;

    @Autowired
    private LockMarketRewardIncomePlanService lockMarketRewardIncomePlanService;

    @Autowired
    private LockMemberIncomePlanService lockMemberIncomePlanService;

    @Autowired
    private UnlockIncomeService unlockIncomeService;

    @Autowired
    private LockBttcRestitutionIncomePlanService lockBttcRestitutionIncomePlanService;

    @Autowired
    private LockIeoRestitutionIncomePlanDao lockIeoRestitutionIncomePlanDao;

    @Autowired
    private ISilkDataDistService silkDataDistService;

    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;

    /**
      * 解锁锁仓活动
      * @author tansitao
      * @time 2018/7/19 16:39 
      */
    @Override
    public void unlockActivityLock(int unlockNum)
    {

        List<LockCoinDetail> lockCoinDetailList = lockDetailMapper.findLockDetailList(LockStatus.LOCKED.getOrdinal(), LockType.LOCK_ACTIVITY.getOrdinal(), unlockNum, new Date());
        if(lockCoinDetailList != null){
            XxlJobLogger.log("======锁仓活动, 可解锁数量"+ lockCoinDetailList.size() + "======");
            //批量处理解锁数据
            dealUnlockList(lockCoinDetailList,LockType.LOCK_ACTIVITY);
            //如果数据库还有未解锁数据，继续处理
            if(lockCoinDetailList.size() == unlockNum){
                unlockActivityLock(unlockNum);
            }
        }

    }

    /**
      * 批量解锁投资活动
      * @author tansitao
      * @time 2018/7/31 10:58 
      */
    @Override
    public void unlockFinanActivity(int unlockNum) {
        Date unlockTime = DateUtil.dateAddDay(new Date(),-1);
        List<LockCoinDetail> lockCoinDetailList = lockDetailMapper.findLockDetailList(LockStatus.LOCKED.getOrdinal(), LockType.FINANCIAL_LOCK.getOrdinal(), unlockNum, unlockTime);
        if(lockCoinDetailList != null){
            XxlJobLogger.log("======投资活动, 可解锁数量"+ lockCoinDetailList.size() + "======");
            //批量处理解锁数据
            dealUnlockList(lockCoinDetailList,LockType.FINANCIAL_LOCK);
            //如果数据库还有未解锁数据，继续处理
            if(lockCoinDetailList.size() == unlockNum){
                unlockActivityLock(unlockNum);
            }
        }
    }

    /**
     * 批量解锁内部锁仓活动
     * @author tansitao
     * @time 2018/8/7 10:39 
     */
    @Override
    public void unlockInternalActivity(int unlockNum) {
        List<UnlockCoinTask> unlockCoinTasks = lockDetailMapper.findUnLockTaskList(ProcessStatus.NOT_PROCESSED.getOrdinal());
        if(unlockCoinTasks != null){
            XxlJobLogger.log("======内部锁仓活动, 可解锁数量"+ unlockCoinTasks.size() + "======");
            //处理解锁数据
            dealInternalUnlockList(unlockCoinTasks);
            //如果数据库还有未解锁数据，继续处理
            if(unlockCoinTasks.size() == unlockNum){
                unlockActivityLock(unlockNum);
            }
        }
    }

    public void dealInternalUnlockList(List<UnlockCoinTask> unlockCoinTasks){
        int relUnlockNum = 0;
        for (UnlockCoinTask unlockCoinTask: unlockCoinTasks) {
            try{
                //更新解锁状态为解锁中
                if(unlockCoinTaskService.updateUnlockCoinTaskStatus(unlockCoinTask.getId(), ProcessStatus.PROCESSING, ProcessStatus.NOT_PROCESSED) > 0){
                    unLockCoinDetailService.unlockInternalActivity(unlockCoinTask, restTemplate);
                    relUnlockNum++;
                }
            }
            catch (Exception e){
                unlockCoinTaskService.updateUnlockCoinTaskStatus(unlockCoinTask.getId(), ProcessStatus.NOT_PROCESSED, ProcessStatus.PROCESSING);
                log.error("======内部锁仓活动, 解锁"+ unlockCoinTask.getRefActivitieId() +"失败======", e);
                XxlJobLogger.log("======内部锁仓活动, 解锁"+ unlockCoinTask.getRefActivitieId() +"失败======");
             }
        }
        XxlJobLogger.log("======投资活动, 实际解锁数量" + relUnlockNum + "======");
    }
    /**
      * 处理所有符合条件的解锁数据
      * @author tansitao
      * @time 2018/7/19 16:03 
      */
    public void dealUnlockList(List<LockCoinDetail> lockCoinDetailList, LockType lockType)
    {
        int relUnlockNum = 0;
        if(lockCoinDetailList != null){
            //判断自动解锁类型，如果是锁仓活动则执行以下代码
            if(lockType == LockType.LOCK_ACTIVITY || lockType == LockType.STO){
                for (LockCoinDetail lockCoinDetail:lockCoinDetailList) {
                    try{
                        //更新解锁状态为解锁中
                        if(lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKING, LockStatus.LOCKED, lockCoinDetail.getId()) > 0) {
                            unLockCoinDetailService.unlockCoin(lockCoinDetail, restTemplate);
                            relUnlockNum++;
                        }
                    }
                    catch (Exception e){
                        lockCoinDetailService.updateLockStatus(LockStatus.LOCKED, LockStatus.UNLOCKING, lockCoinDetail.getId());
                        log.error("======投资活动, 解锁"+ lockCoinDetail.getId() +"失败======", e);
                        XxlJobLogger.log("======投资活动, 解锁"+ lockCoinDetail.getId() +"失败======");
                    }
                }
            }else if(lockType == LockType.FINANCIAL_LOCK){
                //判断自动解锁类型，如果是投资活动则执行以下代码
                for (LockCoinDetail lockCoinDetail:lockCoinDetailList) {
                    try{
                        //更新解锁状态为解锁中
                        if(lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKING, LockStatus.LOCKED, lockCoinDetail.getId()) > 0) {
                            unLockCoinDetailService.unlockFinanCoinByUSDT(lockCoinDetail, restTemplate);
                            relUnlockNum++;
                        }
                    }
                    catch (Exception e){
                        lockCoinDetailService.updateLockStatus(LockStatus.LOCKED, LockStatus.UNLOCKING, lockCoinDetail.getId());
                        log.error("======投资活动, 解锁"+ lockCoinDetail.getId() +"失败======", e);
                        XxlJobLogger.log("======投资活动, 解锁"+ lockCoinDetail.getId() +"失败======");
                    }
                }
            }else if(lockType == LockType.QUANTIFY){
                //判断自动解锁类型，如果是节点产品活动则执行以下代码
                for (LockCoinDetail lockCoinDetail:lockCoinDetailList) {
                    try{
                        //更新解锁状态为解锁中
                        if(lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKING, LockStatus.LOCKED, lockCoinDetail.getId()) > 0) {
                            unLockCoinDetailService.unlockQuantify(lockCoinDetail, restTemplate);
                            relUnlockNum++;
                        }
                    }
                    catch (Exception e){
                        lockCoinDetailService.updateLockStatus(LockStatus.LOCKED, LockStatus.UNLOCKING, lockCoinDetail.getId());
                        log.error("======节点产品活动, 解锁"+ lockCoinDetail.getId() +"失败======", e);
                        XxlJobLogger.log("======节点产品活动, 解锁"+ lockCoinDetail.getId() +"失败======");
                    }
                }
            }
        }
        XxlJobLogger.log("======节点产品活动, 实际解锁数量" + relUnlockNum + "======");
    }

    /**
     * 解锁STO锁仓活动
     * @author tansitao
     * @time 2018/11/7 18:05 
     */
    @Override
    public void unlockSTOActivityLock(int unlockNum)
    {

        List<LockCoinDetail> lockCoinDetailList = lockDetailMapper.findLockDetailList(LockStatus.LOCKED.getOrdinal(), LockType.STO.getOrdinal(), unlockNum, new Date());
        if(lockCoinDetailList != null){
            XxlJobLogger.log("======STO锁仓活动, 可解锁数量"+ lockCoinDetailList.size() + "======");
            //批量处理解锁数据
            dealUnlockList(lockCoinDetailList,LockType.STO);
            //如果数据库还有未解锁数据，继续处理
            if(lockCoinDetailList.size() == unlockNum){
                unlockSTOActivityLock(unlockNum);
            }
        }

    }

    /**
     * 批量解锁节点产品活动
     * @author tansitao
     * @time 2018/12/28 10:55 
     */
    @Override
    public void unlockQuantifyActivity(int unlockNum) {
        List<LockCoinDetail> lockCoinDetailList = lockDetailMapper.findLockDetailList(LockStatus.LOCKED.getOrdinal(), LockType.QUANTIFY.getOrdinal(), unlockNum, new Date());
        if(lockCoinDetailList != null){
            XxlJobLogger.log("======节点产品活动, 可解锁数量"+ lockCoinDetailList.size() + "======");
            //批量处理解锁数据
            dealUnlockList(lockCoinDetailList,LockType.QUANTIFY);
            //如果数据库还有未解锁数据，继续处理
            if(lockCoinDetailList.size() == unlockNum){
                unlockQuantifyActivity(unlockNum);
            }
        }
    }

    /**
      * 批量解锁CNYT推荐人分期收益
      * @author tansitao
      * @time 2018/7/31 10:58 
      */
    @Override
    public void unlockCnytRewardIncome(int unlockNum) {
        List<LockMarketRewardIncomePlan> lockMarketRewardIncomePlans = lockMarketRewardIncomePlanService.findCanUnLockList(new Date(), LockBackStatus.BACK, unlockNum);
        if(lockMarketRewardIncomePlans != null){
            XxlJobLogger.log("======CNYT推荐人分期收益, 可解锁数量"+ lockMarketRewardIncomePlans.size() + "======");
            //批量处理解锁数据
            dealCnytRewardIncome(lockMarketRewardIncomePlans);
            //如果数据库还有未解锁数据，继续处理
            if(lockMarketRewardIncomePlans.size() == unlockNum){
                unlockCnytRewardIncome(unlockNum);
            }
        }
    }

    /**
     * 处理CNYT推荐人分期收益
     * @author tansitao
     * @time 2018/12/28 18:20 
     */
    public void dealCnytRewardIncome(List<LockMarketRewardIncomePlan> lockMarketRewardIncomePlans){
        for (LockMarketRewardIncomePlan lockMarketRewardIncomePlan:lockMarketRewardIncomePlans) {
            if(lockMarketRewardIncomePlan.getLockDetailId() != null){
                //获取锁仓详情
                LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(lockMarketRewardIncomePlan.getLockDetailId());
                lockRewardReturnService.returnRewardIncome(lockMarketRewardIncomePlan, lockCoinDetail);
            }else {
                //当为额外奖励的返佣时，锁仓记录为空
                lockRewardReturnService.returnRewardIncome(lockMarketRewardIncomePlan, null);
            }
        }
    }

    /**
      * 处理BTTC锁仓分期返还
      * @author dengdy
      * @time 2019/4/17 11:40 
      */
    private void dealBttcRestitution(List<LockBttcRestitutionIncomePlan> lockBttcRestitutionIncomePlans){
        for (LockBttcRestitutionIncomePlan lockBttcRestitutionIncomePlan : lockBttcRestitutionIncomePlans) {
            LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(lockBttcRestitutionIncomePlan.getLockDetailId());
            if(lockCoinDetail.getStatus()==LockStatus.LOCKED){
                lockRewardReturnService.returnBttcRestitution(lockBttcRestitutionIncomePlan, lockCoinDetail);
            }

        }
    }

    /**
      * 处理ieo锁仓分期返还
      * @author fatKarin
      * @time 2019/6/6 16:10 
      */
    private void dealIeoRestitution(List<LockIeoRestitutionIncomePlan> lockIeoRestitutionIncomePlans){
        for (LockIeoRestitutionIncomePlan lockIeoRestitutionIncomePlan : lockIeoRestitutionIncomePlans) {
            LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(lockIeoRestitutionIncomePlan.getLockDetailId());
            lockRewardReturnService.returnIeoRestitution(lockIeoRestitutionIncomePlan, lockCoinDetail);
        }
    }

    /**
      * IOE解锁BTTC锁仓分期返还
      * @author dengdy  update by huyu 6/19
      * @time 2019/4/17 10:58 
      */
    @Override
    public void unlockBTTCRestitutionIncome(int unlockNum) {
        List<LockBttcRestitutionIncomePlan> lockBttcRestitutionIncomePlans = lockBttcRestitutionIncomePlanService.getCanRestitutionList(LockBackStatus.BACK, unlockNum, new Date());
        if(lockBttcRestitutionIncomePlans .size()>0){
            XxlJobLogger.log("======BTTC锁仓返还, 可解锁数量"+ lockBttcRestitutionIncomePlans.size() + "======");
            //批量处理解锁数据
            dealBttcRestitution(lockBttcRestitutionIncomePlans);
            //如果数据库还有未解锁数据，继续处理
            if(lockBttcRestitutionIncomePlans.size() == unlockNum){
                unlockBTTCRestitutionIncome(unlockNum);
            }
        }

        //----------------------改需求，作废，保留------------------
//        int times = 5;
//        BigDecimal waitingRate = new BigDecimal("0.9");
//        BigDecimal lockRate = BigDecimal.ONE.subtract(waitingRate);
//        BigDecimal unlockRate = new BigDecimal("0.2");
//        int circleDays = 30;
//        SilkDataDist dataDist1 = silkDataDistService.findByIdAndKey("IEO_BTTC_CONFIG", "TIMES");//期数，默认5
//        SilkDataDist dataDist2 = silkDataDistService.findByIdAndKey("IEO_BTTC_CONFIG", "WAITING_RATE");//默认0.9
//        SilkDataDist dataDist3 = silkDataDistService.findByIdAndKey("IEO_BTTC_CONFIG", "UNLOCK_RATE");//默认0.2
//        SilkDataDist dataDist4 = silkDataDistService.findByIdAndKey("IEO_BTTC_CONFIG", "CIRCLE_DAYS");//默认30天
//        if(dataDist1!=null){
//            times = ConvertUtils.lookup(Integer.class).convert(Integer.class, dataDist1.getDictVal());
//        }
//        if(dataDist2!=null){
//            waitingRate = ConvertUtils.lookup(BigDecimal.class).convert(BigDecimal.class, dataDist2.getDictVal());
//            lockRate = BigDecimal.ONE.subtract(waitingRate);
//        }
//        if(dataDist3!=null){
//            unlockRate = ConvertUtils.lookup(BigDecimal.class).convert(BigDecimal.class, dataDist3.getDictVal());
//        }
//        if(dataDist4!=null){
//            circleDays = ConvertUtils.lookup(Integer.class).convert(Integer.class, dataDist4.getDictVal());
//        }
////        long idd = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
//        List<Long> lockCoinDetailIds = lockBttcRestitutionIncomePlanService.findIeoUnlockDetailToday();
////        lockCoinDetailIds.add(6104L);
//        if(lockCoinDetailIds.size()>0){
//            for(int i = 0; i<lockCoinDetailIds.size();i++){
//                try {
//                    lockCoinDetailService.unlockIeoDetailById(Long.valueOf(lockCoinDetailIds.get(i)+""),  waitingRate, lockRate, unlockRate, times , 36L,circleDays);
//                }catch (Exception e){
//                    log.error("======IEO锁仓活动解仓lockCoinDetailId:"+Long.valueOf(lockCoinDetailIds.get(i)+"")+"失败======", e);
//                    e.printStackTrace();
//                }
//
//            }
//
//        }
    }


    /**
      * 处理bcc赋能分期返还
      * @author fatKarin
      * @time 2019/5/20 11:40 
      */
    private void dealBccEnergizeRestitution(List<LockCoinDetail> lockCoinDetailList){
        // 查询活动配置
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("BCC_ENERGIZE", "SETTING_ID");
        isTrue(dataDist != null, "找不到活动配置");
        long activityId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(activityId);
        notNull(lockCoinActivitieSetting, "活动不存在");
        for (LockCoinDetail lockCoinDetail  : lockCoinDetailList) {
            try {
                XxlJobLogger.log("===================" + "会员"+ lockCoinDetail.getMemberId() + "返还bcc赋能锁仓金额开始，锁仓记录id:" + lockCoinDetail.getId() + "======================");
                lockRewardReturnService.returnBccEnergize(lockCoinDetail, lockCoinActivitieSetting);
                XxlJobLogger.log("===================" + "会员"+ lockCoinDetail.getMemberId() + "返还bcc赋能锁仓金额成功，锁仓记录id:" + lockCoinDetail.getId() + "======================");
            } catch (Exception E) {
                XxlJobLogger.log("===================" + "会员"+ lockCoinDetail.getMemberId() + "返还bcc赋能锁仓金额失败，锁仓记录id:" + lockCoinDetail.getId() + "失败原因:" + E.getMessage() + "======================");
            }

        }
    }
    /**
      * 批量解锁BCC锁仓分期返还
      * @author fatKarin
      * @time 2019/6/6 10:58 
      */
    @Override
    public void unlockBccEnergize(int unlockNum) {
        List<LockCoinDetail> lockCoinDetailList = lockDetailMapper.findLockDetailList(LockStatus.LOCKED.getOrdinal(), LockType.ENERGIZE.getOrdinal(), unlockNum, new Date());
        if(lockCoinDetailList != null){
            XxlJobLogger.log("======BCC赋能活动, 可解锁数量"+ lockCoinDetailList.size() + "======");
            //批量处理解锁数据
            dealBccEnergizeRestitution(lockCoinDetailList);
            //如果数据库还有未解锁数据，继续处理
            if(lockCoinDetailList.size() == unlockNum){
                unlockBccEnergize(unlockNum);
            }
        }
    }

    /**
      * 批量解锁Ieo锁仓分期返还
      * @author fatKarin
      * @time 2019/6/6 10:58 
      */
    @Override
    public void unlockIeoRestitutionIncome(int unlockNum) {
        List<LockIeoRestitutionIncomePlan> lockIeoRestitutionIncomePlans = lockIeoRestitutionIncomePlanDao.getCanRestitutionList(LockBackStatus.BACK.getOrdinal(), unlockNum, new Date());
        if(lockIeoRestitutionIncomePlans != null){
            XxlJobLogger.log("======ieo锁仓返还, 可解锁数量"+ lockIeoRestitutionIncomePlans.size() + "======");
            //批量处理解锁数据
            dealIeoRestitution(lockIeoRestitutionIncomePlans);
            //如果数据库还有未解锁数据，继续处理
            if(lockIeoRestitutionIncomePlans.size() == unlockNum){
                unlockIeoRestitutionIncome(unlockNum);
            }
        }
    }


    /**
      * 处理金钥匙分期返还
      * @author dengdy
      * @time 2019/5/20 11:40 
      */
    private void dealGoldenKeyRestitutionPrincipal(List<LockCoinDetail> lockCoinDetailList){
        for (LockCoinDetail lockCoinDetail  : lockCoinDetailList) {
            try {
                XxlJobLogger.log("===================" + "会员"+ lockCoinDetail.getMemberId() + "返还金钥匙锁仓金额开始，锁仓记录id:" + lockCoinDetail.getId() + "======================");
                lockRewardReturnService.returnGoldenKeyPrincipal(lockCoinDetail);
                XxlJobLogger.log("===================" + "会员"+ lockCoinDetail.getMemberId() + "返还金钥匙锁仓金额成功，锁仓记录id:" + lockCoinDetail.getId() + "======================");
            } catch (Exception E) {
                XxlJobLogger.log("===================" + "会员"+ lockCoinDetail.getMemberId() + "返还金钥匙锁仓金额失败，锁仓记录id:" + lockCoinDetail.getId() + "失败原因:" + E.getMessage() + "======================");
            }

        }
    }
    /**
      * 批量解锁金钥匙锁仓金额
      * @author dengdy
      * @time 2019/5/20 10:58 
      */
    @Override
    public void unlockGoldenKeyPrincipal(int unlockNum) {
        List<LockCoinDetail> lockCoinDetailList = lockDetailMapper.findLockDetailList(LockStatus.LOCKED.getOrdinal(), LockType.GOLD_KEY.getOrdinal(), unlockNum, new Date());
        if(lockCoinDetailList != null){
            XxlJobLogger.log("======金钥匙锁仓返还, 可解锁数量"+ lockCoinDetailList.size() + "======");
            //批量处理解锁数据
            dealGoldenKeyRestitutionPrincipal(lockCoinDetailList);
            //如果数据库还有未解锁数据，继续处理
            if(lockCoinDetailList.size() == unlockNum){
                unlockGoldenKeyPrincipal(unlockNum);
            }
        }
    }

    /**
     * 解锁CNYT锁仓收益（自己锁仓的）
     * @author Zhang Yanjun
     * @time 2018.12.11 15:42
     */
    @Override
    public void unlockCNYTActivityLock(){
        //查询出满足条件的数据
        List<LockMemberIncomePlan> list = lockDetailMapper.findLockMemberList(LockBackStatus.BACK.getOrdinal(), new Date());
        //处理数据
        int relUnlockNum = 0;
        if (list != null){
            XxlJobLogger.log("======cnyt锁仓活动, 可解锁数量: "+ list.size() + "======"+list);
            for (LockMemberIncomePlan lockMemberIncomePlan: list){
                try {
                    //修改状态为返还中
                    if (lockMemberIncomePlanService.updateStatus(lockMemberIncomePlan.getId(), LockBackStatus.BACKING, LockBackStatus.BACK)) {
                        //修改账户
                        XxlJobLogger.log("====lockMemberIncomePlanId: " + lockMemberIncomePlan.getId() + "===解锁开始===");

                        unlockIncomeService.unlockFinanCoinByActCoin(lockMemberIncomePlan);
                        relUnlockNum++;
                        XxlJobLogger.log("====lockMemberIncomePlanId: " + lockMemberIncomePlan.getId() + "===解锁成功===");
                    }
                }catch (Exception e) {
                    //解锁失败，状态改为待返还
                    lockMemberIncomePlanService.updateStatus(lockMemberIncomePlan.getId(), LockBackStatus.BACK, LockBackStatus.BACKING);
                    log.error("====lockMemberIncomePlanId: "+lockMemberIncomePlan.getId() + "===解锁失败===", e);
                    XxlJobLogger.log("====lockMemberIncomePlanId: "+lockMemberIncomePlan.getId() + "===解锁失败==="+e.getMessage());
                }
                }
            }
        XxlJobLogger.log("======cnyt锁仓活动, 实际解锁数量: " + relUnlockNum + "======");
    }


    /**
     * 解锁CNYT锁仓本金（自己锁仓的）
     * @author Zhang Yanjun
     * @time 2018.12.11 15:37
     */
    @Override
    public void unlockCNYTActivityCorLock(){
        //查询满足条件的数据
        List<LockCoinDetail> list = lockDetailMapper.findLockDetailByType(LockStatus.LOCKED.getOrdinal(),LockType.STO_CNYT.getOrdinal(),new Date());
        //处理数据
        int relUnlockNum = 0;
        if (list.size()>0){
            XxlJobLogger.log("======cnyt锁仓活动, 可解锁数量: "+ list.size() + "======"+list);
            //解锁账户
            for (LockCoinDetail lockCoinDetail : list){
                //修改状态为解锁中
                try {
                    XxlJobLogger.log("====lockCoinDetailId: "+lockCoinDetail.getId() + "===解锁开始===");
                    if (lockCoinDetailService.updateLockStatus(LockStatus.UNLOCKING, LockStatus.LOCKED, lockCoinDetail.getId()) > 0) {
                        //解锁账户
                        unlockIncomeService.unlockCorByActCoin(lockCoinDetail);
                        relUnlockNum++;
                        XxlJobLogger.log("====lockCoinDetailId: "+lockCoinDetail.getId() + "===解锁成功===");
                    }
                }catch (Exception e) {
                    //解锁失败，状态改回来
                    lockCoinDetailService.updateLockStatus(LockStatus.LOCKED,LockStatus.UNLOCKING,lockCoinDetail.getId());
                    log.error("====lockMemberIncomePlanId: "+lockCoinDetail.getId() + "===解锁失败===", e);
                    XxlJobLogger.log("====lockCoinDetailId: "+lockCoinDetail.getId() + "===解锁失败==="+e.getMessage());
                }
            }

        }
        XxlJobLogger.log("======cnyt锁仓活动, 实际解锁数量: " + relUnlockNum + "======");
    }

    @Override
    public LockCoinDetail getMissRewardLock(Long lockCoinDetailId) {
        return lockDetailMapper.queryLockDetailById(lockCoinDetailId);
    }

    @Override
    public boolean isExistRewardRecord(Long lockCoinDetailId) {
        int row = lockDetailMapper.isExistRewardRecord(lockCoinDetailId);
        return row > 0 ? true : false;
    }
}
