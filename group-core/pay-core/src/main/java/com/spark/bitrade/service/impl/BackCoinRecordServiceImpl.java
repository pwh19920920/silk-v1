package com.spark.bitrade.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.mapper.dao.BackCoinRecordMapper;
import com.spark.bitrade.entity.BackCoinRecord;
import com.spark.bitrade.service.BackCoinRecordService;
import org.springframework.stereotype.Service;

/**
 * 退币记录(BackCoinRecord)表服务实现类
 *
 * @author daring5920
 * @since 2019-09-04 10:51:08
 */
@Service("backCoinRecordService")
public class BackCoinRecordServiceImpl extends ServiceImpl<BackCoinRecordMapper, BackCoinRecord> implements BackCoinRecordService {

}