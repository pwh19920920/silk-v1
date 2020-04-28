package com.spark.bitrade.job;

import com.spark.bitrade.entity.Member;
import com.spark.bitrade.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MemberStatisticsJob {

    @Autowired
    private MemberService memberService ;


    @Scheduled(cron = "0/10 23 15 * * ?")
    public void statisticsHourMember(){
        //Member
        System.out.println("1111111");
    }

}
