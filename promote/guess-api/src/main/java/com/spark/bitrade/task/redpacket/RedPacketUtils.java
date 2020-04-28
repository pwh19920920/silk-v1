package com.spark.bitrade.task.redpacket;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 红包生成工具
 */
public class RedPacketUtils {

	/**
	 * @param amount
	 *             红包金额
	 * @param redpacketNum
	 *             发出的红包数
	 */
	public static LinkedList<BigDecimal> generateRedPacket(int amount, int redpacketNum) {
		LinkedList<BigDecimal> redPackets = new LinkedList<BigDecimal>();
		// 将金额数按照100*amount 范围分
		int seed = 100 * amount;
		int length = 0;
		// 在0~seed中生成m_length个随机不重复的数字
		int m_length = redpacketNum - 1;
		// 利用SecureRandom生成随机数
		SecureRandom secureRandom = null;
		// 保证不重复且升序排序
		SortedSet<Integer> set = new TreeSet<>();
		try {
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		while (length < m_length) {
			int num = secureRandom.nextInt(seed);
			if (num == 0)
				num = 1;
			set.add(num);
			length = set.size();
		}
		int preNum = 0;
		int currentNum = 0;
		int calc = 0;
		int setSize = set.toArray().length;
		for (int i = 0; i < setSize + 1; i++) {
			if (i < setSize) {
				currentNum = (int) set.toArray()[i];
			} else {
				currentNum = seed;
			}
			//System.out.println("第" + (i + 1) + "个红包");
			BigDecimal redPacket = new BigDecimal((currentNum - preNum)).divide(new BigDecimal(100));
			redPackets.add(redPacket);
			//System.out.println("红包金额=" + new BigDecimal((currentNum - preNum)).divide(new BigDecimal(100)) + "EOS");
			calc += (currentNum - preNum);
			//System.out.println("已发:"+ new BigDecimal(calc).divide(new BigDecimal(100)) + "EOS");
			preNum = currentNum;
		}
		return redPackets;
	}




}