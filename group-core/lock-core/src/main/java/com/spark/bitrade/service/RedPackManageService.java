package com.spark.bitrade.service;

import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dao.MemberTransactionDao;
import com.spark.bitrade.dao.RedPackManageDao;
import com.spark.bitrade.dao.RedPackReceiveRecordDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 红包信息活动管理表（red_pack_manage） 服务类
 * </p>
 *
 * @author qiliao
 * @since 2019-11-25
 */
@Service
public class RedPackManageService extends BaseService {

    @Autowired
    private RedPackManageDao redPackManageDao;
    @Autowired
    private ISilkDataDistService silkDataDistService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RedPackReceiveRecordDao redPackReceiveRecordDao;
    @Autowired
    private RedPackReceiveRecordService recordService;
    @Resource
    private MemberTransactionDao memberTransactionDao;
    /**
     * 查询最近有效的红包
     * @return
     */
    public RedPackManage findValidRedPack() {
        return redPackManageDao.findValidRedPack();
    }

    public RedPackManage findById(Long id){
        return redPackManageDao.findOne(id);
    }

    public RedPackManage save(RedPackManage redPackManage){
        return redPackManageDao.save(redPackManage);
    }

    public int updateBalance(BigDecimal changeAmount,   Long id,Integer count){
        return redPackManageDao.updateBalance(changeAmount,id,count);
    }

    public List<RedPackManage> findExpireManages(){
        return redPackManageDao.findExpireManages();
    }

    @Transactional
    public void returnManage(RedPackManage manage) {
        //add by qhliao 查询出未领取的红包
        boolean hasReload=false;
        List<RedPackReceiveRecord> beExpire = redPackReceiveRecordDao.findRecordExpireByManageId(manage.getId());
        for (RedPackReceiveRecord re:beExpire){
            recordService.doReturn(re);
            hasReload=true;
        }

        SilkDataDist silkDataDist=silkDataDistService.findByIdAndKey("RED_PACK_CONFIG","TOTAL_ACCOUNT_ID");
        Assert.notNull(silkDataDist,"红包支付账户未配置,请联系管理员");
        Long totalAccountId = Long.valueOf(silkDataDist.getDictVal());
        //更新剩余金额为0
        if(hasReload){
            manage=redPackManageDao.findOne(manage.getId());
        }
        if(manage.getRedPacketBalance().compareTo(BigDecimal.ZERO)>0){
            int i = redPackManageDao.updateManageRedBalance(manage.getId(),  manage.getRedPacketBalance());
            Assert.isTrue(i>0,"更新剩余金额失败");
            MemberWallet walletTotal = memberWalletService.findByCoinUnitAndMemberId(manage.getUnit(), totalAccountId);
            Assert.notNull(walletTotal,"红包支付钱包不存在,请联系管理员");
            //冻结余额到可用余额
            MessageResult messageResult = memberWalletService.thawBalance(walletTotal, manage.getRedPacketBalance());
            Assert.isTrue(messageResult.isSuccess(),messageResult.getMessage());

            //新增如果红包是 项目方红包 则需要将总账户的钱 再转移到 项目方账户
            Long supportProjectMemberId = redPackManageDao.findSupportProjectMemberId(manage.getId());
            if(supportProjectMemberId!=null){

                //红包总账户扣款
                MessageResult decreaseBalance = memberWalletService.decreaseBalance(walletTotal.getId(), manage.getRedPacketBalance());
                Assert.isTrue(decreaseBalance.isSuccess(),messageResult.getMessage());

                MemberTransaction m1=new MemberTransaction();
                m1.setMemberId(totalAccountId);
                m1.setAmount(manage.getRedPacketBalance().negate());
                m1.setCreateTime(new Date());
                m1.setType(TransactionType.RED_PACK_RETURN);
                m1.setSymbol(manage.getUnit());
                m1.setFee(BigDecimal.ZERO);
                m1.setComment("项目方红包结束剩余退还");
                memberTransactionDao.save(m1);

                //项目方增加
                MemberWallet projectWallet = memberWalletService.findByCoinUnitAndMemberId(manage.getUnit(), supportProjectMemberId);
                Assert.notNull(projectWallet,"项目方钱包不存在");
                MessageResult increaseBalance = memberWalletService.increaseBalance(projectWallet.getId(), manage.getRedPacketBalance());
                Assert.isTrue(increaseBalance.isSuccess(),increaseBalance.getMessage());

                MemberTransaction m2=new MemberTransaction();
                m2.setMemberId(supportProjectMemberId);
                m2.setAmount(manage.getRedPacketBalance());
                m2.setCreateTime(new Date());
                m2.setType(TransactionType.RED_PACK_RETURN);
                m2.setSymbol(manage.getUnit());
                m2.setFee(BigDecimal.ZERO);
                m2.setComment("项目方红包结束剩余退还");
                memberTransactionDao.save(m2);

            }

        }







    }
}
