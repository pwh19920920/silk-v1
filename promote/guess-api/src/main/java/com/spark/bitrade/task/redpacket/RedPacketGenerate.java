package com.spark.bitrade.task.redpacket;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>红包生成接口</p>
 *  @author octopus
 *  @date 2018-9-18
 */
public interface RedPacketGenerate {

    /**
     * 生成红包
     * @param redpacketAmount
     *            红包总额
     * @param redpacketNum
     *            红包个数
     * @param limit
     *            单个红包面额限制
     * @return
     */
    List<BigDecimal> generateRedPacket(BigDecimal redpacketAmount, int redpacketNum, BigDecimal limit);

}
