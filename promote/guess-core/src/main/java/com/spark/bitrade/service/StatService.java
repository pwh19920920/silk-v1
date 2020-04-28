package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.mapper.dao.StatisticsMapper;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.vo.JackpotStatisticsVo;
import com.spark.bitrade.vo.RewardStatisticsVo;
import com.spark.bitrade.vo.VoteStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 报表统计service
 * @author Zhang Yanjun
 * @time 2018.09.17 10:07
 */
@Service
public class StatService {
    @Autowired
    StatisticsMapper statisticsMapper;

    /**
     * 报表统计分页
     * @author Zhang Yanjun
     * @time 2018.09.18 17:35
     * @param type  投票统计0,中奖统计1,奖池统计2
     * @param startTime
     * @param endTime
     * @param pageNo
     * @param pageSize
     */
    public PageInfo<Object> statistics(long type, String startTime, String endTime, int pageNo, int pageSize){
        Page<Object> page= PageHelper.startPage(pageNo,pageSize);
        if (type==0){//投票统计
//            this.statisticsMapper.voteStatistics(startTime,endTime);
            this.voteStatisticsUtil(startTime, endTime);
        }else if (type==1){//中奖统计
//            this.statisticsMapper.rewardStatistivs(startTime, endTime);
            this.rewardStatisticsUtil(startTime, endTime);
        }else if (type==2){//奖池统计
            this.jackpotStatisticsUtil(startTime, endTime);
        }
        return page.toPageInfo();
    }

    //投票统计时间格式
    List<VoteStatisticsVo> voteStatisticsUtil(String startTime, String endTime){
        List<VoteStatisticsVo> list=statisticsMapper.voteStatistics(startTime, endTime);
        for (int i=0;i<list.size();i++){
            String openTime=list.get(i).getOpenTime().substring(0,list.get(i).getOpenTime().length()-2);
            list.get(i).setOpenTime(openTime);
        }
        return list;
    }

    //中奖统计时间格式
    List<RewardStatisticsVo> rewardStatisticsUtil(String startTime,String endTime){
        List<RewardStatisticsVo> list=statisticsMapper.rewardStatistivs(startTime, endTime);
        for (int i=0;i<list.size();i++){
            String openTime=list.get(i).getOpenTime().substring(0,list.get(i).getOpenTime().length()-2);
            list.get(i).setOpenTime(openTime);
        }
        return list;
    }

    //奖池统计计算
    List<JackpotStatisticsVo> jackpotStatisticsUtil(String startTime, String endTime) {
        List<JackpotStatisticsVo> list = statisticsMapper.jackpotStatistics(startTime, endTime);
        for (int i = list.size()-1; i >= 0; i--) {//上一期或者第一期
            int j=list.size()-1;//第一期
            if(i>0){//不是第一期
                j=i-1;//本期
                BigDecimal lastDeposit=list.get(i).getJackpotBalance().add(list.get(i).getRedpacketBalance());//上期沉淀=奖池余量+红包余量
//                list.get(j).setJackpotAll(list.get(i).getJackpotBalance().add(list.get(j).getBetNum()));//奖池累计=上期沉淀+本期投注
                list.get(j).setJackpotAll(lastDeposit.add(list.get(j).getBetNum()));//奖池累计=上期沉淀+本期投注
            }else {//第一期
                list.get(j).setJackpotAll(list.get(j).getBetNum());//奖池累计=本期投注
            }
            BigDecimal jackpotAll = list.get(j).getJackpotAll(); //奖池累计
            list.get(j).setPromotion(jackpotAll.multiply(list.get(j).getRebateRatio()));//推荐分红=奖池累计*返佣比例
            list.get(j).setPromotionBalance(list.get(j).getPromotion().subtract(list.get(j).getCountPromote().multiply(list.get(j).getRebateRatio())));//推荐分红剩余=推荐分红-返佣用户的投注总额*返佣比例
            //是否开启红包，否：SLU回购（弃用）
            //是否开启红包，否：下期沉淀
            BigDecimal b = jackpotAll.multiply(list.get(j).getRedpacketRatio());//红包=奖池累计*红包比例
            if (list.get(j).getRedpacketState() == 1) {//开启
                list.get(j).setRedpacket(b);//红包活动总额
            } else {
                list.get(j).setRedpacket(BigDecimal.valueOf(0));//未开启，红包活动总额为0
//                list.get(j).setSluBack(b);//SLU回购
                list.get(j).setDeposit(b);//下期沉淀
            }
//            list.get(j).setReward(jackpotAll.multiply(list.get(j).getPrizeRatio()));//奖金发放=奖池累计*奖励比例
            list.get(j).setRewardBalance(list.get(j).getReward().subtract(list.get(j).getRewardNum()));//奖金剩余=奖金-已被领取的
//            list.get(j).setSluBack((list.get(j).getSluBack() == null ? BigDecimal.valueOf(0) : list.get(j).getSluBack()).add(jackpotAll.multiply(list.get(j).getBackRatio())));//SLU回购=红包没开启的+奖池累计*回购比例
            list.get(j).setSluBack(list.get(j).getPromotionBalance().add(jackpotAll.multiply(list.get(j).getBackRatio())));//SLU回购=推荐分红余量+奖池累计*SLU回购比例
            //本期沉淀=红包未开启（前面已加）+奖池余量+红包余量
            list.get(j).setDeposit((list.get(j).getDeposit()==null? BigDecimal.valueOf(0):list.get(j).getDeposit()).add(list.get(j).getJackpotBalance()).add(list.get(j).getRedpacketBalance()));
            String openTime=list.get(j).getOpenTime().substring(0,list.get(j).getOpenTime().length()-2);//时间格式设置
            list.get(j).setOpenTime(openTime);
        }
        //筛选时间
        if (startTime!=null&!"".equals(startTime)&endTime!=null&!"".equals(endTime)){
            List<JackpotStatisticsVo> list1=new ArrayList<>();
            for (int i=0;i<list.size();i++){
                if (DateUtil.stringToDate(list.get(i).getOpenTime()+"").compareTo(DateUtil.stringToDate(startTime+""))>=0 && DateUtil.stringToDate(list.get(i).getOpenTime()+"").compareTo(DateUtil.stringToDate(endTime+"")) <=0  )
                    list1.add(list.get(i));
            }
            list.clear();
            for (int k=0;k<list1.size();k++){
                list.add(list1.get(k));
            }
            return list;
        }else
            return list;
        }


    /**
     * 投票统计导出
     * @author Zhang Yanjun
     * @time 2018.09.18 17:35
     * @param startTime
     * @param endTime
     */
    public List<VoteStatisticsVo> outVoteStatistics (String startTime, String endTime){
//        return  statisticsMapper.voteStatistics(startTime, endTime);//投票统计
        return this.voteStatisticsUtil(startTime, endTime);
    }
    /**
     * 中奖统计导出
     * @author Zhang Yanjun
     * @time 2018.09.18 17:39
     * @param startTime
     * @param endTime
     */
    public List<RewardStatisticsVo> outRewardStatistics(String startTime, String endTime){
//        return statisticsMapper.rewardStatistivs(startTime, endTime);
        return rewardStatisticsUtil(startTime, endTime);
    }
    /**
     * 奖池统计导出
     * @author Zhang Yanjun
     * @time 2018.09.18 17:39
     * @param startTime
     * @param endTime
     */
    public List<JackpotStatisticsVo> outJackpotStatistics(String startTime, String endTime){
        return this.jackpotStatisticsUtil(startTime, endTime);
    }



}
