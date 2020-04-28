import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.MemberWallet;

import java.text.MessageFormat;

/**
 *  * 
 *  * @author yangch
 *  * @time 2018.11.10 15:44
 *  
 */
public class Test {
    public static void main(String[] args) throws Exception {
        MemberWallet incomeWalletCache = new MemberWallet();
        incomeWalletCache.setId(1L);
        incomeWalletCache.setMemberId(123L);

        ExchangeOrder order = new ExchangeOrder();
        order.setMemberId(1233L);
        order.setOrderId("olll33333333");

        if(incomeWalletCache.getMemberId() != order.getMemberId()){
            throw new Exception(MessageFormat.format("获取的钱包和订单的会员信息不一致。钱包信息={0}，订单信息={1}"
                    , incomeWalletCache, order));
        }
    }
}
