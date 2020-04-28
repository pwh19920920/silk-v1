package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.ExchangeCoinDisplayArea;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.MarketService;
import com.spark.bitrade.service.RankService;
import com.spark.bitrade.util.MessageRespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  行情数据
 *
 * @author young
 * @time 2019.12.10 11:40
 */
@Slf4j
@RestController
public class CoinThumbController extends CommonController {
    @Autowired
    private MarketService marketService;
    @Autowired
    private ExchangeCoinService coinService;
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private RankService rankService;

    final String URL_PREFIX = "api/v1/";

    /**
     * 推荐交易对及涨幅榜
     *
     * @param top 涨幅榜top数量
     * @return
     */
    @RequestMapping("overview")
    public Map<String, List<CoinThumb>> overview(Integer top) {
        if (StringUtils.isEmpty(top) || top < 0) {
            top = 10;
        }

        Map<String, List<CoinThumb>> result = new HashMap<>(2);
        List<ExchangeCoin> recommendCoin = coinService.findAllByFlag(1);
        List<CoinThumb> recommendThumbs = new ArrayList<>();
        for (ExchangeCoin coin : recommendCoin) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            if (processor != null) {
                CoinThumb thumb = processor.getThumb();
                recommendThumbs.add(thumb);
            }
        }
        // 推荐交易对
        result.put("recommend", recommendThumbs);

        // 涨幅榜数据
        List<CoinThumb> allThumbs = findSymbolThumb(null, null);
        Collections.sort(allThumbs, (o1, o2) -> o2.getChg().compareTo(o1.getChg()));
        int limit = allThumbs.size() > top ? top : allThumbs.size();

        // 过滤不符合排名要求的交易对
        List<CoinThumb> changeRankTop = new ArrayList<>(limit);
        for (CoinThumb thumb : allThumbs) {
            // 需求：60分钟交易数量大于XX且大于x笔交易，才能参与首页排名，小于等于设置的数值自动退出排名
            ExchangeCoin coin = coinService.findBySymbol(thumb.getSymbol());
            //if (coin.getDisplayArea().equals(ExchangeCoinDisplayArea.INNOVATIVE)) { // 只控制创新区
            if (coin.getRankMinTradeTimes() > 0 || coin.getRankMinTradeAmount().compareTo(BigDecimal.ZERO) > 0) {
                RankService.RankDependData data = rankService.getRankDependData(thumb.getSymbol());
                if (coin.getRankMinTradeTimes() > data.getTradeTimes()) {
                    // 不满足 交易次数
                    continue;
                }
                if (coin.getRankMinTradeAmount().compareTo(data.getTradeAmount()) > 0) {
                    // 不满足 交易数量
                    continue;
                }
            } else {
                /// log.info("创新区币种{}，未配置排名过滤条件", coin.getSymbol());
            }
            //}
            //如果为隐藏则不显示
            if(coin.getIsShow()==0){
                continue;
            }
            // 添加可排名的交易对
            changeRankTop.add(thumb);

            // 满足top数量后 退出
            if (changeRankTop.size() >= limit) {
                break;
            }
        }

        result.put("changeRank", changeRankTop);
        return result;
    }


    /**
     * 涨幅榜
     *
     * @param top top数量
     * @return
     */
    @RequestMapping("topOverview")
    public MessageRespResult<Map<String, List<CoinThumb>>> topOverview(Integer top) {
        if (StringUtils.isEmpty(top) || top < 0) {
            top = 10;
        }

        Map<String, List<CoinThumb>> result = new HashMap<>();
        List<CoinThumb> allThumbs = findSymbolThumb(null, null);
        //根据涨幅进行排序
        Collections.sort(allThumbs, (o1, o2) -> o2.getChg().compareTo(o1.getChg()));

        int bottomStart = 0, topEnd = allThumbs.size();
        if (allThumbs.size() > top) {
            bottomStart = allThumbs.size() - top;
            topEnd = top;
        }

        result.put("top", allThumbs.subList(0, topEnd));

        List<CoinThumb> listBottom = allThumbs.subList(bottomStart, allThumbs.size());
        listBottom.sort((o1, o2) -> o2.getChg().compareTo(o1.getChg()) * -1);
        result.put("bottom", listBottom);

        return MessageRespResult.success4Data(result);
    }


    /**
     * 获取币种缩略行情
     *
     * @param displayArea 可选，0/MASTER=主区，1/INNOVATIVE=创新区
     * @param keyWord     可选，查询关键字
     *                     
     * @author yangch
     * @time 2018.07.12 11:39 
     */
    @RequestMapping(value = "symbol-thumb", method = {RequestMethod.OPTIONS, RequestMethod.POST, RequestMethod.GET})
    public List<CoinThumb> findSymbolThumb(@RequestParam(value = "displayArea", required = false) String displayArea,
                                           @RequestParam(value = "keyWord", required = false) String keyWord) {
        return findSymbolThumb2(displayArea, keyWord, null, 1);
    }

    /**
     * 获取币种缩略行情-v2
     *
     * @param displayArea 选填，显示区域
     * @param keyWord     选填，过滤关键字
     * @param baseSymbol  选填，过滤的基币
     * @return
     * @author yangch
     * @time 2019-06-26 10:18:34
     */
    @RequestMapping(value = "symbol-thumb-v2", method = {RequestMethod.OPTIONS, RequestMethod.POST, RequestMethod.GET})
    public List<CoinThumb> findSymbolThumb2(@RequestParam(value = "displayArea", required = false) String displayArea,
                                            @RequestParam(value = "keyWord", required = false) String keyWord,
                                            @RequestParam(value = "baseSymbol", required = false) String baseSymbol,
                                            @RequestParam(value = "showAll", defaultValue = "1") Integer showAll
    ) {
        List<ExchangeCoin> coins;
        if ("0".equalsIgnoreCase(displayArea)
                || ExchangeCoinDisplayArea.MASTER.name().equalsIgnoreCase(displayArea)) {
            //查询主区
            coins = coinService.findAllByDisplayArea(ExchangeCoinDisplayArea.MASTER);
        } else if ("1".equalsIgnoreCase(displayArea)
                || ExchangeCoinDisplayArea.INNOVATIVE.name().equalsIgnoreCase(displayArea)) {
            //查询创新区
            coins = coinService.findAllByDisplayArea(ExchangeCoinDisplayArea.INNOVATIVE);
        } else {
            //查询所有
            coins = coinService.findAllEnabled();
        }

        List<CoinThumb> thumbs = new ArrayList<>();
        //新增过滤的关键字
        coins.stream().filter(coin -> {
            if (baseSymbol != null) {
                return coin.getSymbol().endsWith(baseSymbol.toUpperCase());
            }
            return true;
        }).filter(coin ->
                //显示所有或是要求显示 默认显示所有
                (showAll == null || showAll == 1 || coin.getIsShow() == 1)
                        && (keyWord == null
                        || coin.getSymbol().toUpperCase().indexOf(keyWord.toUpperCase()) != -1
                        || coin.getSymbol().equalsIgnoreCase(keyWord.toUpperCase()))
        ).forEach(coin -> {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            if (null != processor) {
                CoinThumb thumb = processor.getThumb();
                // 更新扶持项目的标志
                thumb.setSupport(StringUtils.hasText(coin.getTradeCaptcha()));
                if (null != thumb) {
                    thumbs.add(thumb);
                }
            }
        });
        return thumbs;
    }

    /**
     * 首页行情及趋势
     *
     * @param symbol
     * @param showAll
     * @return
     */
    @RequestMapping("symbol-thumb-trend")
    public JSONArray findSymbolThumbWithTrend(String symbol, @RequestParam(defaultValue = "1") Integer showAll) {
        List<ExchangeCoin> coins = coinService.findAllEnabled();

        //添加过滤
        coins = coins.stream().filter(coin -> {
            if (null != showAll && showAll != 1) {
                return coin.getIsShow() == 1;
            }
            return true;
        }).collect(Collectors.toList());

        Calendar calendar = Calendar.getInstance();
        // 将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, -24);

        JSONArray array = new JSONArray();
        long firstTimeOfToday = calendar.getTimeInMillis();
        for (ExchangeCoin coin : coins) {
            if (symbol == null || coin.getCoinSymbol().toLowerCase().indexOf(symbol.toLowerCase()) != -1) {
                CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
                if (null == processor) {
                    continue;
                }
                CoinThumb thumb = processor.getThumb();
                if (null == thumb) {
                    continue;
                }

                // 更新扶持项目的标志
                thumb.setSupport(StringUtils.hasText(coin.getTradeCaptcha()));
                JSONObject json = (JSONObject) JSON.toJSON(thumb);

                long endCacheTime = toHourTime(nowTime);
                long periodCacheTime = endCacheTime - toHourTime(firstTimeOfToday);
                // 24小时趋势
                JSONArray trend = marketService.findSymbolThumbWithTrendCache(thumb.getSymbol(),
                        firstTimeOfToday, nowTime, "1hour", endCacheTime, periodCacheTime);
                json.put("trend", trend);
                array.add(json);
            }

        }
        return array;
    }

    /**
     * 获取指定交易对的行情数据
     *
     * @param symbol 交易对，可忽略大小写。eg：BTC/USDT
     * @return
     */
    @RequestMapping(URL_PREFIX + "symbol-thumb/one")
    public MessageRespResult<CoinThumb> findOneCoinThumb(@RequestParam("symbol") String symbol) {
        CoinProcessor processor = coinProcessorFactory.getProcessor(this.toUpperCase(symbol));
        if (Objects.nonNull(processor)) {
            return MessageRespResult.success4Data(processor.getThumb());
        }
        return MessageRespResult.success4Data(null);
    }


    /**
     * 时间错精度保留到分钟，resolution为1小时内的1分、5分、15分、30分K线的标识
     *
     * @param time
     * @param resolution
     * @return
     */
    private Long toMinuteTime(long time, int resolution) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        //去掉毫秒
        calendar.set(Calendar.MILLISECOND, 0);
        //去掉秒
        calendar.set(Calendar.SECOND, 0);
        if (resolution > 0) {
            //获取分钟
            int currMinute = new Date(calendar.getTimeInMillis()).getMinutes() + 1;
            //获取指定周期的开始时间
            calendar.set(Calendar.MINUTE, currMinute / resolution * resolution);
        }

        return calendar.getTimeInMillis();
    }

    /**
     * 时间错精度保留到小时
     *
     * @param time
     * @return
     */
    private Long toHourTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        //去掉毫秒
        calendar.set(Calendar.MILLISECOND, 0);
        //去掉秒
        calendar.set(Calendar.SECOND, 0);
        //去掉分钟
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }
}
