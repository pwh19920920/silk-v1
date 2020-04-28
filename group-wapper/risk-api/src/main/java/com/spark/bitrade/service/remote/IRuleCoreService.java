package com.spark.bitrade.service.remote;

import com.deaking.risk.entity.EventDecide;
import com.deaking.risk.entity.result.ResultEntity;
import com.spark.bitrade.service.remote.impl.IRuleCoreServiceImpl;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "risk-drools-core", fallback = IRuleCoreServiceImpl.class)
public interface IRuleCoreService {
    @PostMapping(value = "/rule/core/execute")
    ResultEntity<EventDecide> execute(@RequestBody String event);
}
