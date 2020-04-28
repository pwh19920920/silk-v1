
import com.spark.bitrade.util.Md5;

/**
 * 生成授权码
 */
public class GenAuthkey {
    public static void main(String[] args) throws Exception {
        String pref = "DeakingSilktrader.";
        //String uid = "71639";   //个人测试账号
        //String uid = "103307";   //103307	免手续费，机器人帐号-slbpubshua@liankuan.top
        //String uid = "103400";      //103400	免手续费，机器人帐号-slbnewbai@liankuan.top
        //String uid = "103309";      //103309	免手续费，机器人帐号-slbpubhedge@liankuan.top
        //String uid = "103310";      //103310	免手续费，机器人帐号-slbsishua@liankuan.top
        //String uid = "155414";      //155414 新机器人帐号 2116698830@qq.com
        //String uid = "70146";      //70146 新机器人帐号(付渝) 1978922019@qq.com //70146:D89B98B12DBDBE764C6C9A90CD76B361

        //String uid = "280574";      //280574 13631223921  接入方的机器人超盘手    280574:0C86890954AF67C04A67C00AE50BF861

        //String uid = "281564";   // 账号1： 14775037966  281564  281564:AA93EE83F206573568E29F81B727C772
        String uid = "281565";  // 账号2： 18476354414  281565  281565:23E75CC9FBF2F24EE054969239990698

        System.out.println(uid+":"+Md5.md5Digest(pref+uid));
    }
}
