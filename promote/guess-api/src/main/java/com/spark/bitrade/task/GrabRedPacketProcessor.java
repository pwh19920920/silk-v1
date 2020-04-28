package com.spark.bitrade.task;

import com.spark.bitrade.entity.RedPacket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>抢红包处理器</p>
 * @author octopus
 * @date 2018-9-17
 */
@Component
public class GrabRedPacketProcessor {

    @Autowired
    private RedPacketManager redPacketManager;

    /**
     * 抢红包
     * @return
     */
    public RedPacket gradRedPacket(){
        return  redPacketManager.robRedPacket();
    }

    /**
     * 剩余红包个数
     * @return
     */
    public int redPacketSize(){
        return redPacketManager.getRedPacketSize();
    }

}
