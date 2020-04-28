package com.spark.bitrade.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.AsyncNotificationBusiness;
import com.spark.bitrade.mapper.dao.AsyncNotificationBusinessMapper;
import com.spark.bitrade.service.IAsyncNotificationBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsyncNotificationBusinessServiceImpl extends ServiceImpl<AsyncNotificationBusinessMapper, AsyncNotificationBusiness> implements IAsyncNotificationBusinessService {
}
