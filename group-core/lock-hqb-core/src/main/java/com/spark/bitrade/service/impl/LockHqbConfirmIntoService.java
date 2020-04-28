package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.MybatisTransactional;
import com.spark.bitrade.constant.HqbInRecordStatusEnum;
import com.spark.bitrade.entity.LockHqbInRecord;
import com.spark.bitrade.mapper.dao.LockHqbInRecordMapper;
import com.spark.bitrade.mapper.dao.LockHqbMemberWalletMapper;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class LockHqbConfirmIntoService extends ServiceImpl<LockHqbInRecordMapper, LockHqbInRecord> {

    @Autowired
    private ILockHqbMemberWalletService walletService;
    @Autowired
    private LockHqbInRecordMapper lockHqbInRecordMapper;
    @Autowired
    private LockHqbMemberWalletMapper lockHqbMemberWalletMapper;

    /**
     * 确认转入
     *
     * @param calendar   确认日期
     * @param appId      应用ID
     * @param coinSymbol 币种
     */
    public void confirmInto(Calendar calendar, Long appId, String coinSymbol) {
        log.info("确认转入[ {} ]之前的转入记录", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(calendar.getTime()));
        int current = 1, size = 200;
        List<LockHqbInRecord> list;
        do {
            list = lockHqbInRecordMapper.selectAsPage(calendar.getTimeInMillis(), appId, coinSymbol, (current++ - 1) * size, size);
            list.forEach(i -> {
                try {
                    SpringContextUtil.getBean(LockHqbConfirmIntoService.class).confirmInto(i);
                } catch (Exception e) {
                    log.error("确认转入异常：{}", i.getId(), e);
                }
            });
        } while (list.size() >= size);
    }

    /**
     * 单条记录确认
     *
     * @param record 转入记录
     */
    @MybatisTransactional(rollbackFor = Exception.class)
    public void confirmInto(LockHqbInRecord record) {
        // 正常确认转入
        int count = lockHqbMemberWalletMapper.confirmInto(record.getWalletId(), record.getApplyAmount());

        // 确认转入失败，校验账户平衡
        if (count <= 0 && checkBalance(record)) {
            // 校验账户平衡通过，确认所有待确认金额
            count = lockHqbMemberWalletMapper.confirmIntoAll(record.getWalletId());
        }

        if (count <= 0) {
            log.error("确认转入失败，账户平衡校验失败 => {}", record.getId());
        }
        // 更新记录表
        LockHqbInRecord update = new LockHqbInRecord();
        update.setId(record.getId());
        update.setStatus(count > 0 ? HqbInRecordStatusEnum.CONFIRMED : HqbInRecordStatusEnum.CONFIRMED_Failed);
        update.setPlanEffectiveDate(count > 0 ? System.currentTimeMillis() : null);
        updateById(update);
        // 清除缓存
        walletService.clearWalletCache(record.getMemberId(), record.getAppId(), record.getCoinSymbol());
    }

    /**
     * 校验用户账户平衡
     */
    private boolean checkBalance(LockHqbInRecord record) {
        int count = lockHqbMemberWalletMapper.checkBalance(record.getWalletId());
        return count == 1;
    }
}
