package com.spark.bitrade.service;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.MonitorExecuteEnvent;
import com.spark.bitrade.constant.RelievePermissionsStaus;
import com.spark.bitrade.dao.OperateLogDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/***
 ?*?会员权限服务
 ?*?@author?yangch
 ?*?@time?2018.11.06?16:43
 ?*/
@Service
@Slf4j
public class MemberPermissionService {
    @Autowired
    MemberService memberService;

    @Autowired
    AdvertiseService advertiseService;

    @Autowired
    MemberPermissionsRelieveTaskService memberPermissionsRelieveTaskService;
    @Autowired
    private AlarmMonitorService alarmMonitorService;
    @Autowired
    private OperateLogDao operateLogDao;
    /**
     * 修改会员的权限
     *
     * @param member
     * @param executeEnvent
     */
    public void updatePermission(Member member, MonitorExecuteEnvent executeEnvent) {
        if (null == member) {
            return;
        }
        OperateLog operateLog=new OperateLog();
        operateLog.setMemberId(member.getId());

        String cnName = executeEnvent.name();
        if(cnName.contains("ALLOW")){
            operateLog.setSwitchType(1);
        }else if(cnName.contains("FORBID")){
            operateLog.setSwitchType(0);
        }

        operateLog.setAutoManal(0);
        operateLog.setCreateTime(new Date());
        operateLog.setUpdateTime(new Date());

        switch (executeEnvent) {
            case ALLOW_LOGIN:  //允许登陆
                allowLogin(member);
                operateLog.setOperateType(2);
                operateLog.setRemark("自动解冻");
                break;
            case FORBID_LOGIN: //禁止登陆
                forbidLogin(member);
                operateLog.setOperateType(2);
                operateLog.setRemark("自动冻结");
                break;
            case ALLOW_TRADE: //允许交易
                allowTrade(member);
                operateLog.setOperateType(1);
                operateLog.setRemark("自动解冻");
                break;
            case FORBID_TRADE: //禁止交易,禁止交易后，不允许卖出、币币交易和提币操作
                forbidTrade(member);
                operateLog.setOperateType(1);
                operateLog.setRemark("自动冻结");
                break;
            case ALLOW_OTC: //OTC,允许C2C交易
                allowOtc(member);
                break;
            case FORBID_OTC: //OTC,禁止C2C交易
                forbidOtc(member);
                break;
            case ALLOW_OTC_BUY: //OTC,允许C2C买入交易
                allowOtcBuy(member);
                break;
            case FORBID_OTC_BUY: //OTC,禁止C2C买入交易
                forbidOtcBuy(member);
                break;
            case ALLOW_OTC_SELL: //OTC,允许C2C卖出交易
                allowOtcSell(member);
                break;
            case FORBID_OTC_SELL://OTC,禁止C2C卖出交易
                forbidOtcSell(member);
                break;
            case ALLOW_ADVERTISE: //OTC,允许发布广告
                allowAdvertise(member);
                break;
            case FORBID_ADVERTISE: //OTC,禁止发布广告
                forbidAdvertise(member);
                break;

            //币币交易权限
            case ALLOW_EXCHANGE: //EXCHANGE,允许币币交易
                allowExchange(member);
                break;
            case FORBID_EXCHANGE: //EXCHANGE,禁止币币交易
                forbidExchange(member);
                break;
            case ALLOW_EXCHANGE_BUY: //EXCHANGE,允许币币买入交易
                allowExchangeBuy(member);
                break;
            case FORBID_EXCHANGE_BUY: //EXCHANGE,禁止币币买入交易
                forbidExchangeBuy(member);
                break;
            case ALLOW_EXCHANGE_SELL: //EXCHANGE,允许币币卖出交易
                allowExchangeSell(member);
                break;
            case FORBID_EXCHANGE_SELL: //EXCHANGE,禁止币币卖出交易
                forbidExchangeSell(member);
                break;

            //充提币权限
            case ALLOW_COIN: //COIN,允许充值提币
                allowCoin(member);
                break;
            case FORBID_COIN: //COIN,禁止充值提币
                forbidCoin(member);
                break;
            case ALLOW_COIN_IN: //COIN,允许充值
                allowCoinIn(member);
                break;
            case FORBID_COIN_IN: //COIN,禁止充值
                forbidCoinIn(member);
                break;
            case ALLOW_COIN_OUT: //COIN,允许提币
                allowCoinOut(member);
                break;
            case FORBID_COIN_OUT: //COIN,禁止提币
                forbidCoinOut(member);
                break;
            case ALLOW_MOBILE_TRANSACTIONS_STATUS://修改手机邮箱禁止交易
                allowTrade(member);
                operateLog.setOperateType(1);
                operateLog.setRemark(executeEnvent.getCnName());
                break;
            case FORBID_MOBILE_TRANSACTIONS_STATUS://修改手机邮箱禁止交易
                forbidTrade(member);
                operateLog.setOperateType(1);
                operateLog.setRemark(executeEnvent.getCnName());
                break;
            case ALLOW_EMAIL_TRANSACTIONS_STATUS:
                allowTrade(member);
                operateLog.setOperateType(1);
                operateLog.setRemark(executeEnvent.getCnName());
                break;
            case FORBID_EMAIL_TRANSACTIONS_STATUS:
                forbidTrade(member);
                operateLog.setOperateType(1);
                operateLog.setRemark(executeEnvent.getCnName());
                break;

        }
        if(operateLog.getOperateType()!=null){
            try {
                operateLogDao.save(operateLog);
            }catch (Exception e){
                log.info("保存操作日志失败!");
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    /**
     * 修改会员的权限
     *
     * @param memberId
     * @param executeEnvent
     */
    public void updatePermission(long memberId, MonitorExecuteEnvent executeEnvent) {
        Member member = memberService.findOne(memberId);
        updatePermission(member, executeEnvent);
    }


    //允许登陆
    public void allowLogin(Member member) {
        //允许登陆
        memberService.updateMemberStatus(member.getId(), CommonStatus.NORMAL);
    }

    //禁止登陆
    public void forbidLogin(Member member) {
        //禁止登陆
        memberService.updateMemberStatus(member.getId(), CommonStatus.ILLEGAL);
    }

    //允许交易
    public void allowTrade(Member member) {
        //允许交易
        memberService.updateMemberTransactionStatus(member.getId(), BooleanEnum.IS_TRUE);
    }

    //禁止交易
    public void forbidTrade(Member member) {
        //禁止交易。禁止交易后，不允许卖出、币币交易和提币操作
        memberService.updateMemberTransactionStatus(member.getId(), BooleanEnum.IS_FALSE);
        //add by tansitao 时间： 2018/11/12 原因：下架该用户所有广告
        List<Advertise> list = advertiseService.getAllPutOnAdvertis(member.getId());
        if (list != null && list.size() > 0) {
            for (Advertise advertise : list) {
                try {
                    advertiseService.putOffShelves(advertise);
                } catch (Exception e) {
                    log.error("===============下架广告失败================id:" + advertise.getId(), e);
                }
            }
        }

    }


    //允许C2C交易
    public void allowOtc(Member member) {
        // todo 待完善
    }

    //禁止C2C交易
    public void forbidOtc(Member member) {
        // todo 待完善
    }

    //允许C2C买入交易
    public void allowOtcBuy(Member member) {
        // todo 待完善
    }

    //禁止C2C买入交易
    public void forbidOtcBuy(Member member) {
        // todo 待完善
    }

    //允许C2C卖出交易
    public void allowOtcSell(Member member) {
        // todo 待完善
    }

    //禁止C2C卖出交易
    public void forbidOtcSell(Member member) {
        // todo 待完善
    }

    //允许发布广告
    public void allowAdvertise(Member member) {
        //允许发布广告
        memberService.updateMemberPublishStatus(member.getId(),BooleanEnum.IS_TRUE);
    }

    //禁止发布广告
    public void forbidAdvertise(Member member) {
        //禁止发布广告

        memberService.updateMemberPublishStatus(member.getId(),BooleanEnum.IS_FALSE);
        //add by tansitao 时间： 2018/11/12 原因：下架该用户所有广告
        List<Advertise> list = advertiseService.getAllPutOnAdvertis(member.getId());
        if (list != null && list.size() > 0) {
            for (Advertise advertise : list) {
                try {
                    advertiseService.putOffShelves(advertise);
                } catch (Exception e) {
                    log.error("===============下架广告失败================id:" + advertise.getId(), e);
                }
            }
        }
    }

    //允许币币交易
    public void allowExchange(Member member) {
        // todo 待完善
    }

    //禁止币币交易
    public void forbidExchange(Member member) {
        // todo 待完善
    }

    //允许币币买入交易
    public void allowExchangeBuy(Member member) {
        // todo 待完善
    }

    //禁止币币买入交易
    public void forbidExchangeBuy(Member member) {
        // todo 待完善
    }

    //允许币币卖出交易
    public void allowExchangeSell(Member member) {
        // todo 待完善
    }

    //禁止币币卖出交易
    public void forbidExchangeSell(Member member) {
        // todo 待完善
    }

    //允许充值提币
    public void allowCoin(Member member) {
        // todo 待完善
    }

    //禁止充值提币
    public void forbidCoin(Member member) {
        // todo 待完善
    }

    //允许充值
    public void allowCoinIn(Member member) {
        // todo 待完善
    }

    //禁止充值
    public void forbidCoinIn(Member member) {
        // todo 待完善
    }

    //允许提币
    public void allowCoinOut(Member member) {
        // todo 待完善
    }

    //禁止提币
    public void forbidCoinOut(Member member) {
        // todo 待完善
    }

    /**
     * 解冻用户权限
     * @author?tansitao
     * @time?2018/12/12?15:01?
     *
     */
    @Transactional(rollbackFor = Exception.class)
    public void relievePermission(MemberPermissionsRelieveTask mprTask) {
        //权限限制操作
        MonitorExecuteEnvent permissionsType = MonitorExecuteEnvent.valueOf(mprTask.getRelievePermissionsType().name().replaceAll("FORBID", "ALLOW"));
        getService().updatePermission(mprTask.getMemberId(), permissionsType);
        //处理自动解冻任务
        mprTask.setStatus(RelievePermissionsStaus.processed);
        mprTask.setDealTime(new Date());
        memberPermissionsRelieveTaskService.save(mprTask);
        if(mprTask.getAlarmMonitorId()!=null){
            AlarmMonitor alarmMonitor = alarmMonitorService.findOneById(mprTask.getAlarmMonitorId());
            alarmMonitorService.updateAlarMonitor(BooleanEnum.IS_TRUE,new Date(),alarmMonitor.getId());
        }

    }

    public MemberPermissionService getService() {
        return SpringContextUtil.getBean(MemberPermissionService.class);
    }
}
