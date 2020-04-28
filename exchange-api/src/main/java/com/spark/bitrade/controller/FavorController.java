package com.spark.bitrade.controller;

import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.FavorSymbol;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.feign.ICoinExchange;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.FavorSymbolService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;


@Slf4j
@RestController
@RequestMapping("/favor")
public class FavorController {
    @Autowired
    private FavorSymbolService favorSymbolService;

    @Autowired
    private ExchangeCoinService coinService;

    @Autowired
    private ICoinExchange iCoinExchange;


    private volatile Set<String> memberSymbols=new HashSet<>();

    /**
     * 添加自选
     * @param member
     * @param symbol
     * @return
     */
    @RequestMapping(value = "add", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageResult addFavor(@SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol){
        String key = member+symbol;
        if(memberSymbols.contains(key)){
            return MessageResult.error("symbol already favored");
        }
        memberSymbols.add(key);
        if(StringUtils.isEmpty(symbol)){
            memberSymbols.remove(key);
            return MessageResult.error("symbol cannot be empty");
        }
        List<FavorSymbol> favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
        if(!CollectionUtils.isEmpty(favorSymbol)){
            memberSymbols.remove(key);
            return MessageResult.error("symbol already favored");
        }
        FavorSymbol favor =  favorSymbolService.add(member.getId(),symbol);
        memberSymbols.remove(key);
        if(favor!= null){
            return MessageResult.success("success");
        }
        return MessageResult.error("error");
    }

    /**
     * 查询当前用户自选
     * * @param member
     *
     * @return
     */
    @RequestMapping(value = "find", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public List<FavorSymbol> findFavor(@SessionAttribute(SESSION_MEMBER) AuthMember member) {
        List<FavorSymbol> favorSymbols = favorSymbolService.findByMemberId(member.getId());
        List<FavorSymbol> newFavorSymbols = new ArrayList<>(favorSymbols);
        //查询不显示的币种
        List<ExchangeCoin> coins = coinService.selectByShow();
        if(favorSymbols.size() != 0){
            for(FavorSymbol symbol:favorSymbols){
                for(ExchangeCoin coin:coins){
                    if(coin.getSymbol().equals(symbol.getSymbol())){
                        newFavorSymbols.remove(symbol);
                    }
                }
            }
        }
        favorSymbols = newFavorSymbols;
        // edit by zyj 2019-07-01: 去掉CNYT的交易对
        List<FavorSymbol> list = new ArrayList<>();
        for (FavorSymbol favorSymbol : favorSymbols) {
            String symbol = favorSymbol.getSymbol();
            String[] symbols = symbol.split("/");
            if (!("CNYT".equals(symbols[1])||"CNYT".equals(symbols[0]))) {
                list.add(favorSymbol);
            }
        }
        return list;
    }

    /**
     * 查询当前用户自选  新
     * * @param member
     *
     * @return
     */
    @RequestMapping(value = "findNew", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public List<FavorSymbol> findNewFavor(@SessionAttribute(SESSION_MEMBER) AuthMember member) {
        List<FavorSymbol> favorSymbols = favorSymbolService.findByMemberId(member.getId());
        List<FavorSymbol> newFavorSymbols = new ArrayList<>(favorSymbols);
        //查询不显示的币种
        List<ExchangeCoin> coins = coinService.selectByShow();
        if(favorSymbols.size() != 0){
            for(FavorSymbol symbol:favorSymbols){
                for(ExchangeCoin coin:coins){
                    if(coin.getSymbol().equals(symbol.getSymbol())){
                        newFavorSymbols.remove(symbol);
                    }
                }
            }
        }
        favorSymbols = newFavorSymbols;
        // edit by zyj 2019-07-01: 去掉CNYT的交易对
        List<FavorSymbol> list = new ArrayList<>();
        for (FavorSymbol favorSymbol : favorSymbols) {
            String symbol = favorSymbol.getSymbol();
            String[] symbols = symbol.split("/");
            if (!("CNYT".equals(symbols[1])||"CNYT".equals(symbols[0]))) {
                list.add(favorSymbol);
            }
        }

        for (FavorSymbol symbol : list) {
            MessageRespResult<CoinThumb> record = iCoinExchange.findCoinThumbBySymol(symbol.getSymbol());
            if (record.getData() != null) {
                symbol.setCoinThumb(record.getData());
            }
        }
        return list;
    }



    /**
     * 删除自选
     * @param member
     * @param symbol
     * @return
     */
    @RequestMapping(value = "delete", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageResult deleteFavor(@SessionAttribute(SESSION_MEMBER) AuthMember member,String symbol){
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error("symbol cannot be empty");
        }
        List<FavorSymbol> favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
        if(CollectionUtils.isEmpty(favorSymbol)){
            return MessageResult.error("favor not exists");
        }
        favorSymbolService.delete(member.getId(),symbol);
        return MessageResult.success("success");
    }


    /**
     * 删除自选 数组逗号分割
     * @param member
     * @param symbols
     * @return
     */
    @RequestMapping(value = "deleteByIds", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageResult deleteFavors(@SessionAttribute(SESSION_MEMBER) AuthMember member,String[] symbols){
        if(symbols==null){
            return MessageResult.error("symbol cannot be empty");
        }
        if (symbols.length==0){
            return MessageResult.error("symbol cannot be empty");
        }

        for (String symbol:symbols){
            List<FavorSymbol> favorSymbol = favorSymbolService.findByMemberIdAndSymbol(member.getId(),symbol);
            if(CollectionUtils.isEmpty(favorSymbol)){
                return MessageResult.error(symbol+"favor not exists");
            }
            favorSymbolService.delete(member.getId(),symbol);
        }
        return MessageResult.success("success");
    }


}















