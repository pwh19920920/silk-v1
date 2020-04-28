package com.spark.bitrade.consumer;

import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.ext.LimitQueue;
import com.spark.bitrade.service.PushTradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class TradeMessageDataManager {
	
	@Autowired
	private PushTradeMessage pushTradeMessage;

	@Autowired
	private ExecutorService taskExecutor;

	//默认延时
	@Value("${push.interval.time:500}")
	private long default_sleep;
	//private final long default_sleep = 500;
	//实时成交 集合大小
	@Value("${push.trade.newest.size:20}")
	private int trade_newest_size;
	@Value("${push.plate.size:7}")
	private int push_plate_size; //盘口推送条数


	private  Map<String, TradePlate> pushTradePlateBuy;
	private  Map<String, TradePlate> pushTradePlateSell;
	private  Map<String, LimitQueue<ExchangeTrade>> pushLatestTrade;
	private  Map<String, CoinThumb> pushCoinThumb;
	private  Map<String, Map<String, KLine>> pushKLine;

	//初始化缓存map
	private TradeMessageDataManager(){
		pushLatestTrade = new ConcurrentHashMap<>();
		pushTradePlateBuy = new ConcurrentHashMap<>();
		pushTradePlateSell = new ConcurrentHashMap<>();
		pushCoinThumb = new ConcurrentHashMap<>();
		pushKLine = new ConcurrentHashMap<>();
	}

	//初始化 推送的线程
	public void initPushThread(){
		taskExecutor.execute(new PushKLine(default_sleep));
		taskExecutor.execute(new PushCoinThumb(default_sleep));
		taskExecutor.execute(new PushLatestTrade(default_sleep));
		taskExecutor.execute(new PushTradePlate(default_sleep));
	}


	//最新的实时成交（注：推送的是最新的集合）
	public void pushLatestTrade(String symbol, ExchangeTrade trade){
		//System.out.println("********************pushNewestTrade4Socket*********************");
		if(!pushLatestTrade.containsKey(symbol)){
			pushLatestTrade.put(symbol, new LimitQueue<>(trade_newest_size));
		}

		pushLatestTrade.get(symbol).offer(trade);


//		List<ExchangeTrade> trades = new ArrayList<>();
//		trades.add(trade);
//
//		pushTradeMessage.pushNewestTrade4Socket(symbol,trades);
//		pushTradeMessage.pushNewestTrade4Netty(symbol, trade);
	}

	public void pushLatestTrade4Socket(String symbol, List<ExchangeTrade> trades){
		//System.out.println("********************pushNewestTrade4Socket*********************");
		if(!pushLatestTrade.containsKey(symbol)){
			pushLatestTrade.put(symbol, new LimitQueue<>(trade_newest_size));
		}

		trades.forEach( exchangeTrade -> {
			pushLatestTrade.get(symbol).offer(exchangeTrade);
		});

		///pushTradeMessage.pushNewestTrade4Socket(symbol,trades);
		//taskExecutor.execute(new PushNewestTrade4Socket(default_sleep));
	}

	public class PushLatestTrade implements Runnable {
		private Long sleep;
		public PushLatestTrade(Long sleep){
			this.sleep = sleep;
		}
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(sleep == null ? default_sleep : sleep);

					if(pushLatestTrade.isEmpty()){
						continue;
					}

					for (Map.Entry<String, LimitQueue<ExchangeTrade>> entry : pushLatestTrade.entrySet()) {
						String symbol = entry.getKey();

						List<ExchangeTrade> trades =  entry.getValue().pollAll();
						if(null == trades) {
							continue;
						}


						//推送pc端
						pushTradeMessage.pushNewestTrade4Socket(symbol, trades);

						//推送app端
						trades.forEach(trade ->{
							pushTradeMessage.pushNewestTrade4Netty(symbol, trade);
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


//	public void pushTradePlate4SocketAndNetty(TradePlate plateMini){
//		pushTradePlate4SocketAndNetty.put(plateMini.getSymbol(),plateMini);
//		System.out.println("********************pushTradePlate4SocketAndNetty*1********************");
//	}
//
//	public class PushTradePlate4SocketAndNetty implements Runnable {
//		private Long sleep;
//		public PushTradePlate4SocketAndNetty(Long sleep){
//			this.sleep = sleep;
//		}
//		@Override
//		public void run() {
//			while (true) {
//				//System.out.println("********************pushTradePlate4SocketAndNetty*2********************");
//				try {
//					Thread.sleep(sleep == null ? default_sleep : sleep);
//
//					//推送手机端和pc端盘口
//					for (Map.Entry<String, TradePlate> entry : pushTradePlate4SocketAndNetty.entrySet()) {
//						String symbol = entry.getKey();
//						TradePlate plateMini = pushTradePlate4SocketAndNetty.remove(symbol);	//移除数据 同时得到移除的数据
//
//						//System.out.println("Key = " + symbol + ", Value = " + plateMini);
//						pushTradeMessage.pushTradePlate4Socket(plateMini);
//						pushTradeMessage.pushTradePlate4Netty(plateMini);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//
//			}
//		}
//	}

	//盘口推送（注：需要区分买卖盘口）
	public void pushTradePlate(TradePlate plateFull){
		if(plateFull.getDirection() == ExchangeOrderDirection.BUY){
			pushTradePlateBuy.put(plateFull.getSymbol(),plateFull);
		} else {
			pushTradePlateSell.put(plateFull.getSymbol(),plateFull);
		}

		//System.out.println("********************pushTradePlateFull4Socket*1********************");
		////pushTradeMessage.pushTradePlateFull4Socket(plateFull);

	}

	public class PushTradePlate implements Runnable {
		private Long sleep;
		public PushTradePlate(Long sleep){
			this.sleep = sleep;
		}
		@Override
		public void run() {
			while (true) {
				try {
					//System.out.println("********************pushTradePlateFull4Socket*2*******************");
					Thread.sleep(sleep == null ? default_sleep : sleep);
					if(!pushTradePlateBuy.isEmpty()){
						pushTradePlate(pushTradePlateBuy);
					}

					if(!pushTradePlateSell.isEmpty()) {
						pushTradePlate(pushTradePlateSell);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void pushTradePlate(Map<String, TradePlate> pushTradePlate){
		for (Map.Entry<String, TradePlate> entry : pushTradePlate.entrySet()) {
			String symbol = entry.getKey();
			TradePlate plateFull = pushTradePlate.remove(symbol);	//移除数据 同时得到移除的数据
			if(null == plateFull){
				return;
			}

			TradePlate plateMini;
			if(plateFull.getItems().size() > push_plate_size){
				//edit by yangch 时间： 2018.04.23 原因：list 强转为 LinkedList会报错
				//plate.setItems((LinkedList<TradePlateItem>) plate.getItems().subList(0,10));
				plateMini = new TradePlate();
				plateMini.setDirection(plateFull.getDirection());

				plateMini.setItems(new LinkedList<TradePlateItem>(plateFull.getItems().subList(0, push_plate_size)));
			} else {
				plateMini = plateFull;
			}
			plateMini.setSymbol(symbol);

			//推送实时成交(币币交易盘口)
			pushTradeMessage.pushTradePlate4Socket(plateMini);
			pushTradeMessage.pushTradePlate4Netty(plateMini);

			//System.out.println("Key = " + symbol + ", Value = " + plateFull);
			pushTradeMessage.pushTradePlateFull4Socket(plateFull);
		}
	}

	//推送实时行情
	public void pushCoinThumb(String symbol,CoinThumb thumb){
		//System.out.println("********************pushCoinThumb4Socket*1********************");
		pushCoinThumb.put(symbol,thumb);

		///pushTradeMessage.pushCoinThumb4Socket(thumb);
		///pushTradeMessage.pushCoinThumb4Netty(thumb);

		///taskExecutor.execute(new PushCoinThumb4Socket(default_sleep));
	}

	public class PushCoinThumb implements Runnable {
		private Long sleep;
		public PushCoinThumb(Long sleep){
			this.sleep = sleep;
		}
		@Override
		public void run() {
			while (true) {
				try {
					//System.out.println("********************pushCoinThumb4Socket*2********************");
					Thread.sleep(sleep == null ? default_sleep : sleep);
					if(pushCoinThumb.isEmpty()){
						continue;
					}

					for (Map.Entry<String, CoinThumb> entry : pushCoinThumb.entrySet()) {
						String symbol = entry.getKey();
						CoinThumb thumb = pushCoinThumb.remove(symbol);	//移除数据 同时得到移除的数据

						//System.out.println("Key = " + symbol + ", Value = " + thumb);
						pushTradeMessage.pushCoinThumb4Socket(thumb);
						pushTradeMessage.pushCoinThumb4Netty(thumb);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void pushKLine(String symbol, KLine kLine) {
		//System.out.println("**********###############**********pushKLine*1****###############***************");
		if(!pushKLine.containsKey(symbol)){
			pushKLine.put(symbol, new ConcurrentHashMap<>());
		}
		pushKLine.get(symbol).put(kLine.getPeriod(), kLine);
	}

	public class PushKLine implements Runnable {
		private Long sleep;
		public PushKLine(Long sleep){
			this.sleep = sleep;
		}
		@Override
		public void run() {
			while (true) {
				try {
					//System.out.println("**********###############**********pushKLine*2*****###############***************");
					Thread.sleep(sleep == null ? default_sleep : sleep);
					if(pushKLine.isEmpty()){
						continue;
					}

					for (Map.Entry<String, Map<String, KLine>> entry : pushKLine.entrySet()) {
						String symbol = entry.getKey();
						Map<String, KLine> entryMap = entry.getValue();
						for (Map.Entry<String, KLine> klineEntry : entryMap.entrySet()) {
							String period = klineEntry.getKey();
							KLine kLine = entryMap.remove(period);	//移除数据 同时得到移除的数据

							//System.out.println("symbol="+symbol+"period = " + period + ", Value = " + kLine);
							pushTradeMessage.pushKLine4Socket(symbol, kLine);
							pushTradeMessage.pushKLine4Netty(symbol, kLine);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
