package com.spark.bitrade.processor;

import com.spark.bitrade.entity.RedPacket;
import com.spark.bitrade.task.GrabRedPacketProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * <p>抢红包服务</p>
 * @author octopus
 * @date 2018-9-17
 */
@Service
public class GrabRedPacketService {


    @Autowired
    private GrabRedPacketProcessor grabRedPacketProcessor;

    /**
     * 抢红包
     * @return RedPacket
     *             为空未中奖,不为空则中奖
     */
    public RedPacket grabRedPacket(){
        return grabRedPacketProcessor.gradRedPacket();
    }

    /**
     * 剩余红包个数
     * @return
     */
    public int redPacketSize(){
        return grabRedPacketProcessor.redPacketSize();
    }


}
