package com.spark.bitrade.task.redpacket;

import com.spark.bitrade.constant.RedPacketConstant;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <p>随机红包生成实现</p>
 * @author octopus
 * @date 2018-9-18
 */
public class RandomRedPacketGenerate implements RedPacketGenerate {

    //public static Random rand = new Random();
    public static DecimalFormat df = new DecimalFormat("#.00000000");

    @Override
    public List<BigDecimal> generateRedPacket(BigDecimal amount, int redpacketNum, BigDecimal limit) {

        List<Double> tempRedpackets = new ArrayList<Double>();
        //已生成红包额累计
        double accumulation = 0;
        //生成的单个红包面额
        double unit = 0;
        //生成的总的红包面额
        double unitSum = 0;
        BigDecimal diff = null;
        for(int i=0; i<redpacketNum-1; i++){
            //unit = nextDouble(limit.doubleValue(), limit.multiply(new BigDecimal(redpacketNum)).doubleValue());
            try {
                unit = this.nextDouble(limit.doubleValue(), amount.divide(new BigDecimal(redpacketNum), RedPacketConstant.DIGITS,BigDecimal.ROUND_HALF_DOWN).doubleValue() + 0.7);
                accumulation += unit;
                diff = amount.subtract(new BigDecimal(accumulation));
                if(diff.compareTo(BigDecimal.ZERO) >= 0){
                    Double temp = Double.valueOf(df.format(unit));
                    unitSum += temp;
                    tempRedpackets.add(temp);
                    continue;
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        //System.out.println("amount = " + df.format(amount));
        tempRedpackets.add(Double.valueOf(df.format(amount.subtract(new BigDecimal(unitSum)).doubleValue())));
        //转换为BigDecimal
        List<BigDecimal> redpackets = new LinkedList<>();
        int size = tempRedpackets.size();
        //可以考虑随机打乱原来的顺序
        for(int i=0; i<size; i++){
            Double d = tempRedpackets.get(i);
            redpackets.add(new BigDecimal(df.format(new BigDecimal(d))));
        }
        return redpackets;
    }

    /**
     * 生成指定区间的小数
     * @param min
     *           最小值
     * @param max
     *           最大值
     * @return
     */
    public double nextDouble(final double min, final double max) {
        double result = min + ((max - min) * new Random().nextDouble());
        return Double.valueOf(df.format(result));
    }

}
