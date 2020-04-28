package com.spark.bitrade.mocker.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huobi.api.ApiClient;
import com.huobi.api.response.Symbol;
import com.spark.bitrade.mocker.entity.ExchangeOrderDirection;
import com.spark.bitrade.mocker.entity.ExchangeTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Component
public class QueryTradeJob {
    @Value("${huobi.api.key}")
    private String API_KEY;
    @Value("${huobi.api.secret}")
    private String API_SECRET;

    //交易量缩放因子，默认为1,0-1之间表示缩小，大于1表示放大
    @Value("${trade.amount.factor:1}")
    private double amountFactor;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ExchangeRate exchangeRate;

    //private List<String> symbols = Arrays.asList("BTC/USDT","ETH/USDT","LTC/USDT","BCH/USDT","DASH/USDT","ZEC/USDT");
    @Value("#{'${query.trade.symbols}'.split(',')}")
    private List<String> symbols;

    //cnyt交易区的币种
    @Value("#{'${cnyt.trade.symbols}'.split(',')}")
    private List<String> cnytSymbols;

    //BT交易区的币种
    @Value("#{'${bt.trade.symbols}'.split(',')}")
    private List<String> btSymbols;

    //转变为cnyt的数据
    private Random random = new Random();


    @Scheduled(fixedRate = 10 * 1000)
    public void huobiJob() {
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        Map<String, String> params = new HashMap<>();
        symbols.forEach(symbol -> {
            System.out.println(symbol);
            String huobiSymbol = symbol.replaceAll("/", "").toLowerCase();
            params.put("symbol", huobiSymbol);
            String resp = client.get("/market/trade", params);

            JSONObject json = JSON.parseObject(resp);
            if (json.getString("status").equals("ok")) {
                JSONObject data = json.getJSONObject("tick");
                List<ExchangeTrade> tradeList = parse(data, false);

                // BTC/ETH/USDT 交易区数据直推
                kafkaTemplate.send("exchange-trade-mocker", symbol, JSON.toJSONString(tradeList));

                //转换为cnyt的行情
                if (StringUtils.endsWithIgnoreCase(symbol, "/USDT")) {
                    String coin = symbol.substring(0, symbol.indexOf("/"));

                    if (cnytSymbols.contains(coin)) {
                        String cnytSymbol = coin + "/CNYT";
                        //List<ExchangeTrade> tradeCnytList = parseUsdt2Cnyt(data);
                        List<ExchangeTrade> tradeCnytList = parse(data, true);
                        //System.out.println("tradeCnytList="+tradeCnytList);
                        kafkaTemplate.send("exchange-trade-mocker", cnytSymbol, JSON.toJSONString(tradeCnytList));
                    }
                    // BT 下线
//                    if (btSymbols.contains(coin)) {
//                        String btSymbol = coin + "/BT";
//                        List<ExchangeTrade> tradeBTList = parse(data, true);
//                        kafkaTemplate.send("exchange-trade-mocker", btSymbol, JSON.toJSONString(tradeBTList));
//                    }
                }
            }
        });

    }


    private List<ExchangeTrade> parse(JSONObject data, boolean bt) {

        List<ExchangeTrade> trades = new ArrayList<>();
        JSONArray list = data.getJSONArray("data");

        long now = Calendar.getInstance().getTimeInMillis();

        for (int i = 0; i < list.size(); i++) {

            JSONObject item = list.getJSONObject(i);
            BigDecimal amount = BigDecimal.valueOf(item.getDouble("amount"));
            BigDecimal price = BigDecimal.valueOf(item.getDouble("price"));
            Long ts = item.getLong("ts");

            // 超过10s的不处理
            if (now - ts > 10 * 1000) {
                continue;
            }


            //过滤价格和数量都相同的交易明细
            int tradesSize = trades.size();
            if (tradesSize > 0 && trades.get(tradesSize - 1).getAmount().compareTo(amount) == 0
                    && trades.get(tradesSize - 1).getPrice().compareTo(price) == 0) {
                continue;
            }

            if (bt) {
                //处理数量
                //System.out.println("转换前：amount="+amount);
                BigDecimal tmpAmount;
                if (amount.compareTo(BigDecimal.valueOf(1)) < 0) {
                    tmpAmount = amount.multiply(BigDecimal.valueOf(random.nextDouble() / 100)).setScale(amount.scale(), BigDecimal.ROUND_DOWN);
                } else {
                    tmpAmount = amount.multiply(BigDecimal.valueOf(random.nextDouble())).setScale(amount.scale(), BigDecimal.ROUND_DOWN);
                }

                if (tmpAmount.compareTo(BigDecimal.valueOf(0.0001)) > 0) {
                    amount = tmpAmount;
                }

                int scale = price.scale();

                price = price.divide(exchangeRate.getCnytPrice(), scale, BigDecimal.ROUND_DOWN);
                //System.out.println("转换后：price="+price);
//                if (price.compareTo(BigDecimal.valueOf(50)) > 0) {
//                    price = price.subtract(BigDecimal.valueOf(random.nextDouble())).setScale(scale, BigDecimal.ROUND_DOWN);
//                } else if (price.compareTo(BigDecimal.valueOf(10)) > 0) {
//                    price = price.subtract(BigDecimal.valueOf(random.nextDouble() / 10)).setScale(scale, BigDecimal.ROUND_DOWN);
//                } else if (price.compareTo(BigDecimal.valueOf(1)) > 0) {
//                    price = price.subtract(BigDecimal.valueOf(random.nextDouble() / 100)).setScale(scale, BigDecimal.ROUND_DOWN);
//                } else {
//                    // 修正转换计算问题，部分币种价格可能突破底价过多
//                    int rate = getRate(price);
//                    price = price.subtract(BigDecimal.valueOf(random.nextDouble() / rate / 2)).setScale(scale, BigDecimal.ROUND_DOWN);
//                }
            }


            ExchangeTrade trade = new ExchangeTrade();
            trade.setAmount(amount.multiply(new BigDecimal(amountFactor)));
            trade.setTime(ts);
            trade.setPrice(price);
            trade.setDirection(ExchangeOrderDirection.valueOf(item.getString("direction").toUpperCase()));
            // 标识来源为 火币
            trade.setBuyOrderId("HB");
            trades.add(trade);
        }

        return trades;
    }

    private int getRate(BigDecimal price) {
        String s = price.toPlainString();
        int i = 1;
        for (char c : s.split("\\.")[1].toCharArray()) {
            if (c == '0') {
                i = i * 10;
            } else {
                break;
            }

        }
        return i * 100;
    }
}
