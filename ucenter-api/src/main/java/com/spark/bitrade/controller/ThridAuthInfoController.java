package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.MemberDepositService;
import com.spark.bitrade.service.ThridAuthInfoService;
import com.spark.bitrade.service.WithdrawRecordService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.ThirdAuthQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;
import java.util.Map;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @author fumy
 * @time 2018.09.19 18:05
 */
@RestController
@Slf4j
@RequestMapping("/thirdAuth")
public class ThridAuthInfoController extends BaseController {

    @Autowired
    WithdrawRecordService withdrawRecordService;
    @Autowired
    MemberDepositService memberDepositService;
    @Autowired
    ThridAuthInfoService thridAuthInfoService;


    /**
     * 提币记录查询
     * @author fumy
     * @time 2018.09.22 10:33
     * @param symbol
     * @param pageNo
     * @param pageSize
     * @param user
     * @return true
     */
    @GetMapping("/withdraw/page-query")
    public MessageResult withdrawRecordPage(String symbol, int pageNo, int pageSize, @SessionAttribute(SESSION_MEMBER) AuthMember user){
        Assert.isTrue(thridAuthInfoService.getAuthByMerberIdAndSymbol(user.getId(),symbol),"暂无权限查询");
        PageInfo<ThirdAuthQueryVo> pageInfo=withdrawRecordService.findWithdrawRecordForThirdAuth(symbol,pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 充币记录查询
     * @author fumy
     * @time 2018.09.22 10:34
     * @param symbol
     * @param pageNo
     * @param pageSize
     * @param user
     * @return true
     */
    @GetMapping("/memberDeposit/page-query")
    public MessageResult memberDepositPage(String symbol,int pageNo,int pageSize,@SessionAttribute(SESSION_MEMBER) AuthMember user){
        Assert.isTrue(thridAuthInfoService.getAuthByMerberIdAndSymbol(user.getId(),symbol),"暂无权限查询");
        PageInfo<ThirdAuthQueryVo> pageInfo=memberDepositService.findMemberDeposit(symbol,pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 授权币种查询
     * @author fumy
     * @time 2018.09.22 10:34
     * @param user
     * @return true
     */
    @GetMapping("/auth-info/coin-list")
    public MessageResult getCoinList(@SessionAttribute(SESSION_MEMBER) AuthMember user){
        List<Map<String,String>> list=thridAuthInfoService.getAuthCoin(user.getId());
        return success(list);
    }
}
