import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderType;
import com.spark.bitrade.util.MessageResult;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *  * 
 *  * @author yangch
 *  * @time 2018.10.26 14:48
 *  
 */
public class Test {
    public static void main(String[] args) {
        //批量撤单
        /*String ids = "[\"orderId1\",\"...\",\"orderIdn\"]";
        JSONArray jsonArray = JSON.parseArray(ids);


        //String error = MessageResult.error("失败").toString();

        JSONObject result = new JSONObject();

        jsonArray.forEach(orderId-> {
            //调用订单
            MessageResult messageResult = MessageResult.success("成功");
            result.put(orderId.toString(), messageResult);
        });

        System.out.println(result);
        System.out.println(MessageResult.success("ok",result));*/

        //批量下单
//        String batchOrderInfo = "[{symbol:\"SLU/USDT\",direction:\"0\",type:\"1\",price:1,amount:10}" +
//                ",{symbol:\"SLU/USDT\",direction:\"BUY\",type:\"LIMIT_PRICE\",price:2,amount:20}" +
//                ",{symbol:\"SLU/USDT\",direction:\"1\",type:\"1\",price:3,amount:30}" +
//                ",{symbol:\"SLU/USDT\",direction:\"SELL\",type:\"1\",price:4,amount:40}]";


        String batchOrderInfo = "[{symbol:\"SLU/USDT\",direction:0,type:1,price:1,amount:10}" +
                ",{symbol:\"SLU/USDT\",direction:\"BUY\",type:\"LIMIT_PRICE\",price:2,amount:20}" +
                ",{symbol:\"SLU/USDT\",direction:1,type:1,price:3,amount:30}" +
                ",{symbol:\"SLU/USDT\",direction:\"SELL\",type:1,price:4,amount:40}]";
        List list = JSON.parseArray(batchOrderInfo, OrderAddParam.class);
        list.forEach(param -> System.out.println(param));
        /*
        JSONArray jsonArray = JSON.parseArray(batchOrderInfo);
        jsonArray.forEach(orderInfo -> {

            AddOrderInfo addOrderInfo = JSON.parseObject(orderInfo.toString(), AddOrderInfo.class);
            System.out.println(orderInfo);
            MessageResult messageResult = MessageResult.success("成功");
            addOrderInfo.setData(messageResult);
            System.out.println("---"+addOrderInfo);

        });*/
    }

    //订单下单实体类
    @Data
    public static class OrderAddParam{
        private String symbol;  //交易对
        private ExchangeOrderDirection direction;   //交易方向
        private ExchangeOrderType type; //交易类型
        private BigDecimal price;   //委托价格
        private BigDecimal amount;  //委托数量

        private String customContent; //自定义内容，非必须
        private Object resultData;  //下单处理结果

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }
}
