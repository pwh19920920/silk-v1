package com.spark.bitrade.test;

import com.spark.bitrade.util.Md5;

public class Md5Test {
    public static void main(String[] args) {
        String password = ".admin";
        String salt = "3333343837343930333436323538343332";
        try{
            String enStr = Md5.md5Digest(password + salt).toLowerCase();
            System.out.println(""+enStr);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
