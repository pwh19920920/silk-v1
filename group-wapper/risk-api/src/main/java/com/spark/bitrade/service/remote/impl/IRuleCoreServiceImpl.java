package com.spark.bitrade.service.remote.impl;

import com.deaking.risk.entity.EventDecide;
import com.deaking.risk.entity.result.ResultEntity;
import com.deaking.risk.enums.ResultCode;
import com.spark.bitrade.service.remote.IRuleCoreService;
import org.springframework.stereotype.Component;

@Component
public class IRuleCoreServiceImpl implements IRuleCoreService {
    @Override
    public ResultEntity<EventDecide> execute(String event) {
        return new ResultEntity<EventDecide>(ResultCode.SERVICE_DISABLE);
    }
}
