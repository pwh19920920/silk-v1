package com.spark.bitrade.service;

import com.spark.bitrade.entity.TechRechargeRecord;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * <p>
 * 技术充（减）币记录 服务类
 * </p>
 *
 * @author fumy
 * @since 2018-06-20
 */
public interface ITechRechargeRecordService extends IService<TechRechargeRecord> {

    List<TechRechargeRecord> techRechargeList();

    int insertRecord(List<TechRechargeRecord> list);

}
