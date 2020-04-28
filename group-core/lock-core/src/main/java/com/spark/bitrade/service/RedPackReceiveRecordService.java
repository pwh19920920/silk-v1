package com.spark.bitrade.service;

import com.spark.bitrade.dao.RedPackManageDao;
import com.spark.bitrade.dao.RedPackReceiveRecordDao;
import com.spark.bitrade.entity.RedPackReceiveRecord;
import com.spark.bitrade.service.Base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 红包领取记录(red_pack_receive_record) 服务类
 * </p>
 *
 * @author qiliao
 * @since 2019-11-25
 */
@Service
@Slf4j
public class RedPackReceiveRecordService extends BaseService {

    @Autowired
    private RedPackReceiveRecordDao redPackReceiveRecordDao;
    @Autowired
    private RedPackManageDao redPackManageDao;
    public RedPackReceiveRecord save(RedPackReceiveRecord redPackReceiveRecord) {
        RedPackReceiveRecord save = redPackReceiveRecordDao.save(redPackReceiveRecord);
        return save;
    }

    public RedPackReceiveRecord findValidRecordById(Long id) {
        return redPackReceiveRecordDao.findValidRecordById(id);
    }

    public List<RedPackReceiveRecord> findByMemberIdAndRedpackIdStatus(Long memberId, Long redpackId) {
        return redPackReceiveRecordDao.findByMemberIdAndRedpackIdStatus(memberId, redpackId);
    }

    public List<RedPackReceiveRecord> returnRedPack() {
        List<RedPackReceiveRecord> res = redPackReceiveRecordDao.findByMemberIdIsNullAndLast();
        return res;
    }

    @Transactional
    public void doReturn(RedPackReceiveRecord records) {
        records.setUpdateTime(new Date());
        records.setReceiveStatus(2);
        redPackReceiveRecordDao.save(records);
        redPackManageDao.returnBalance(records.getReceiveAmount(),records.getRedpackId());
        log.info("================红包退回ID:{}=================",records.getId());
    }


}
