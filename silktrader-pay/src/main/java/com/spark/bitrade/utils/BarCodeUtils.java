package com.spark.bitrade.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>条码生成工具</p>
 * @author tian.bo
 * @since 2019/3/8.
 */
public class BarCodeUtils {


    /**
     * 16-22位随机数
     */
    public static int dataBit = 18;


    /**
     * 条码前缀62-82
     */
    private static List<String> barCodePrefixList = new ArrayList<>();

    /**
     * 初始化条码前缀
     */
    static {
        for(int i=62; i<=68; i++) {
            barCodePrefixList.add(String.valueOf(i));
        }
    }


    public static boolean barCodePerFixCheck(String prefix){
        return barCodePrefixList.contains(prefix);
    }

    public static String barCodeGenerate(int num){
        String dataBit = NumberUtils.generatorVercode(num);
        String prefix =  barCodeGenerateFix();
        StringBuilder sb = new StringBuilder(prefix);
        return sb.append(dataBit).toString();
    }

    private static String barCodeGenerateFix(){
        Random rand = new Random();
        int offset = rand.nextInt(barCodePrefixList.size());
        String fix = barCodePrefixList.get(offset);
        return String.valueOf(fix);
    }

    public static void main(String[] args){
        System.out.println(barCodeGenerateFix());
    }


}
