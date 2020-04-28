package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dto.BettingRecordDTO;
import com.spark.bitrade.dto.JackpotInstructDTO;
import com.spark.bitrade.dto.RecordDTO;
import com.spark.bitrade.dto.RewardDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.processor.CoinExchangeRateService;
import com.spark.bitrade.processor.GrabRedPacketService;
import com.spark.bitrade.processor.StatJackpotService;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.GuessConfigVo;
import com.spark.bitrade.vo.LoginInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.hasText;

/**
  * 竞猜活动控制器
  * @author tansitao
  * @time 2018/9/13 10:06 
  */
@RestController
@RequestMapping("/guessActivity")
@Slf4j
public class GuessActivityController {
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private PriceRangeService priceRangeService;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private BettingRecordService bettingRecordService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private BranchRecordService branchRecordService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PushMessageService pushMessageService;
    @Autowired
    private GrabRedPacketService grabRedPacketService;
    @Autowired
    private StatJackpotService statJackpotService;
    @Autowired
    private CoinExchangeRateService coinExchangeRateService;

//    RedisLock redisLock = new RedisLock(redisTemplate, "openRedPacket");

    /**
     * 检查是否登录
     * @author tansitao
     * @time 2018/9/25 22:06 
     */
    @RequestMapping("/check/login")
    public MessageResult checkLogin(HttpServletRequest request) {
        AuthMember authMember = (AuthMember) request.getSession().getAttribute(SESSION_MEMBER);
        MessageResult result = MessageResult.success();
        if (authMember != null) {
            Member member = memberService.findOne(authMember.getId());
            //设置登录成功后的返回信息
            LoginInfoVo loginInfo = new LoginInfoVo();
            loginInfo.setLocation(member.getLocation());
            loginInfo.setMemberLevel(member.getMemberLevel());
            loginInfo.setUsername(member.getUsername());
            String token1 = request.getSession().getId();
            loginInfo.setToken(token1);
            loginInfo.setRealName(member.getRealName());
            loginInfo.setCountry(member.getCountry());
            loginInfo.setAvatar(member.getAvatar());
            loginInfo.setPromotionCode(member.getPromotionCode());
            loginInfo.setId(member.getId());
            loginInfo.setPhone(member.getMobilePhone());
            result.setData(loginInfo);
        } else {
            String token = request.getHeader("access-auth-token");
            Member member = memberService.loginWithToken(token, request.getRemoteAddr(), "");
            if(member != null){
                //设置登录成功后的返回信息
                LoginInfoVo loginInfo = new LoginInfoVo();
                loginInfo.setLocation(member.getLocation());
                loginInfo.setMemberLevel(member.getMemberLevel());
                loginInfo.setUsername(member.getUsername());
                String token1 = request.getSession().getId();
                loginInfo.setToken(token1);
                loginInfo.setRealName(member.getRealName());
                loginInfo.setCountry(member.getCountry());
                loginInfo.setAvatar(member.getAvatar());
                loginInfo.setPromotionCode(member.getPromotionCode());
                loginInfo.setId(member.getId());
                loginInfo.setPhone(member.getMobilePhone());
                result.setData(loginInfo);
            }else {
                result.setData(false);
            }
        }

        return result;
    }

   /**
    * 最近一期的活动信息
    * @author tansitao
    * @time 2018/9/17 20:40 
    */
    @RequestMapping("/guessConfig")
    public MessageResult getGuessConfig(String guessSymbol) {
        if(null != guessSymbol) {
            guessSymbol = guessSymbol.toUpperCase();
        }
        //查询最近一期活动
        GuessConfigVo guessConfig = bettingConfigService.findByGuessSymbolLately(guessSymbol);
        if(guessConfig != null){
            //从redis 中获取奖池余额，如果获取不到调用api实时获取
            ValueOperations valueOperations = redisTemplate.opsForValue();
            //判断活动结束后从直接拿本期沉淀的奖池
            BigDecimal jackpotBalance = BigDecimal.ZERO;
//            if(guessConfig.getStatus() == BettingConfigStatus.STAGE_FINISHED) {
//                jackpotBalance = (BigDecimal) valueOperations.get(GuessActivityController.JACKPOT_BALANCE_FINISHED + "_" +guessSymbol);
//                if (jackpotBalance == null) {
//                    //从api实时获取奖池，并放入到redis中
//                    jackpotBalance = statJackpotService.statJackpot(guessConfig.getId());
//                    valueOperations.set(GuessActivityController.JACKPOT_BALANCE_FINISHED + "_" +guessSymbol, jackpotBalance, 10, TimeUnit.MINUTES);
//                }
//            } else {
                jackpotBalance = (BigDecimal) valueOperations.get(RedPacketConstant.JACKPOT_BALANCE + "_" +guessSymbol);
                if (jackpotBalance == null) {
                    //从api实时获取奖池，并放入到redis中
                    jackpotBalance = statJackpotService.statCurrentJackpot(guessConfig.getId());
                    valueOperations.set(RedPacketConstant.JACKPOT_BALANCE + "_" +guessSymbol, jackpotBalance, 10, TimeUnit.MINUTES);
                }
//            }

            guessConfig.setJackpotBalance(jackpotBalance);
        }
        MessageResult result = MessageResult.success();
        result.setData(guessConfig);
        return result;
    }

    /**
      * 某一期的奖池说明
      * @author tansitao
      * @time 2018/9/17 20:40 
      */
    @RequestMapping("/jackpotInstruct")
    public MessageResult jackpotInstruct(long periodId) {
        JackpotInstructDTO jackpotInstructDTO = new JackpotInstructDTO();
        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);

        BigDecimal prevJackpotBalanceTotal = BigDecimal.ZERO;
        BigDecimal betTotalPrize = BigDecimal.ZERO;
        if(bettingConfig != null){
            //上一期总奖池数量
            prevJackpotBalanceTotal = statJackpotService.statPrevJackpot(bettingConfig);
            log.info("info-NextJackpot:上一期总奖池数量={}", prevJackpotBalanceTotal);

            //投票总额
            betTotalPrize = bettingRecordService.queryBetTotal(periodId);
        }

        jackpotInstructDTO.setPrevJackpotBalanceTotal(prevJackpotBalanceTotal);
        jackpotInstructDTO.setBetTotalPrize(betTotalPrize);
        MessageResult result = MessageResult.success();
        result.setData(jackpotInstructDTO);
        return result;
    }

    /**
      * 查询某期活动的最新投注信息
      * @author tansitao
      * @time 2018/9/14 17:26 
      */
    @RequestMapping("/pageOnePeriodBettingRecord")
    public MessageResult pageOnePeriodBettingRecord(long periodId, PageModel pageModel) {
        PageInfo<BettingRecord> page = bettingRecordService.queryByPeriodId(periodId, null, pageModel.getPageNo(), pageModel.getPageSize());
        //add by tansitao 时间： 2018/11/22 原因：对推荐码进行隐藏处理
        page.getList().forEach(BettingRecord->{
            BettingRecord.setPromotionCode(StringUtils.isEmpty(BettingRecord.getPromotionCode()) ? "" : BettingRecord.getPromotionCode().replaceAll("(\\w{5})\\w{2}(\\w{0})","$1**$2"));
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
     * 查询某期的价格区间
     * @author tansitao
     * @time 2018/9/14 17:20 
     */
    @RequestMapping("/priceRange")
    public MessageResult getPriceRange(long periodId) {
        List<BettingPriceRange> priceRangeVoList = priceRangeService.findByPeriodId(periodId);
        MessageResult mr = new MessageResult();
        mr.setData(priceRangeVoList);
        return mr;
    }

    /**
     * 分页查询某一期所有用户获奖记录
     * @author tansitao
     * @time 2018/9/14 17:26 
     */
    @RequestMapping("/pageRewardByPeriodId")
    public MessageResult pageRewardByPeriodId(String guessSymbol, @RequestParam RewardBusinessType businessType, PageModel pageModel) {
        GuessConfigVo guessConfig = bettingConfigService.findByGuessSymbolLately(guessSymbol);
        PageInfo<Reward> page = rewardService.queryPageByPeriodId(guessConfig.getId(),businessType.getOrdinal(),pageModel.getPageNo(),pageModel.getPageSize());
        page.getList().forEach(reward->{
            reward.setPromotionCode(StringUtils.isEmpty(reward.getPromotionCode()) ? "" : reward.getPromotionCode().replaceAll("(\\w{5})\\w{2}(\\w{0})","$1**$2"));
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
     * 分页最近一期活动的用户获奖历史记录
     * @author tansitao
     * @time 2018/9/27 9:25 
     */
    @RequestMapping("/rewardHistory")
    public MessageResult rewardHistory(String guessSymbol, PageModel pageModel) {
        GuessConfigVo guessConfig = bettingConfigService.findByGuessSymbolLately(guessSymbol);
        PageInfo<Reward> page = rewardService.pageQueryAll(guessConfig.getId(),pageModel.getPageNo(),pageModel.getPageSize());
        page.getList().forEach(reward->{
            reward.setPromotionCode(StringUtils.isEmpty(reward.getPromotionCode()) ? "" : reward.getPromotionCode().replaceAll("(\\w{5})\\w{2}(\\w{0})","$1**$2"));
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
      * 我的投票记录，分页查询
      * @author tansitao
      * @time 2018/9/14 17:26 
      */
    @RequestMapping("/myBettingRecord")
    public MessageResult pageBettingRecord(@SessionAttribute(SESSION_MEMBER) AuthMember user, PageModel pageModel) {
        PageInfo<BettingRecordDTO> page = bettingRecordService.queryPageByMemberId(user.getId(), pageModel.getPageNo(), pageModel.getPageSize());
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
      * 我的红包记录，分页查询
      * @author tansitao
      * @time 2018/9/14 17:26 
      */
    @RequestMapping("/myRewardByMember")
    public MessageResult pageRewardByMember(@SessionAttribute(SESSION_MEMBER) AuthMember user, PageModel pageModel) {
        PageInfo<Reward> page = rewardService.pageQueryByMemberId(user.getId(), RewardBusinessType.REDPACKET.getOrdinal(), pageModel.getPageNo(), pageModel.getPageSize());
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
     * 分页查询每期活动历史记录
     * @author tansitao
     * @time 2018/9/17 9:29 
     */
    @RequestMapping("/pageRecord")
    public MessageResult pageRecord(PageModel pageModel) {
        PageInfo<RecordDTO> page = bettingConfigService.findAllRecord(pageModel.getPageNo(), pageModel.getPageSize());
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
      * 查询某一期活动记录
      * @author tansitao
      * @time 2018/9/17 9:29 
      */
    @RequestMapping("/oneRecord")
    public MessageResult pageRecord(@RequestParam long periodId) {
        RecordDTO recordDTO = bettingConfigService.findOneRecord(periodId);

        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);
        BigDecimal currentJackpotTotal = statJackpotService.statCurrentJackpot(periodId);
        log.info("info-NextJackpot:本期总奖池数量={}", currentJackpotTotal);

        //本期的推荐返佣总额，即10%用于本期的推荐返佣
        BigDecimal rebateRatio = bettingConfig.getRebateRatio(); //返佣比例
        log.info("info-NextJackpot:返佣比例={}", rebateRatio);
        BigDecimal rewardAmountTotal = currentJackpotTotal
                .multiply(rebateRatio).setScale(8, BigDecimal.ROUND_UP);
        log.info("info-NextJackpot:本期的推荐返佣总额={}", rewardAmountTotal);

        //计算本期红包的数量，转换为投注币种
        BigDecimal redpacketAmount = BigDecimal.ZERO;
//        if(bettingConfig.getRedpacketState() == BooleanEnum.IS_TRUE) {
            BigDecimal redpacketRatio = bettingConfig.getRedpacketRatio(); //红包发布比例
            log.info("info-NextJackpot:红包发布比例={}", redpacketRatio);
            redpacketAmount = currentJackpotTotal
                    .multiply(redpacketRatio).setScale(8, BigDecimal.ROUND_UP);
            log.info("info-NextJackpot:1计算本期红包的数量={}", redpacketAmount);
            redpacketAmount = coinExchangeRateService.toRate(redpacketAmount,
                    bettingConfig.getRedpacketPrizeSymbol(), bettingConfig.getBetSymbol());
            log.info("info-NextJackpot:2计算本期红包的数量={}", redpacketAmount);
//        } else {
//            log.info("info-statCurr:计算本期红包的数量={}，不发送", redpacketAmount);
//        }

        //计算回购的数量，为投注币种
        BigDecimal backRatio = bettingConfig.getBackRatio();    //回购比例
        log.info("info-NextJackpot:回购比例={}", backRatio);
        BigDecimal backAmount = currentJackpotTotal
                .multiply(backRatio).setScale(8, BigDecimal.ROUND_UP);
        log.info("info-NextJackpot:计算回购的数量={}", backAmount);

        //计算奖池沉淀
        BigDecimal jackpotPrecipitation = currentJackpotTotal
                .multiply(bettingConfig.getNextPeriodRatio()).setScale(8, BigDecimal.ROUND_UP);

        if(recordDTO == null){
            recordDTO = new RecordDTO();
        }
        recordDTO.setRedpacketAmount(redpacketAmount);
        recordDTO.setRewardAmount(rewardAmountTotal);
        recordDTO.setBackAmount(backAmount);
        recordDTO.setJackpotPrecipitation(jackpotPrecipitation);
        recordDTO.setBetSymbol(bettingConfig.getBetSymbol());

        MessageResult mr = MessageResult.success("success");
        mr.setData(recordDTO);
        return mr;
    }

    /**
      * 用户某一期活动的中奖信息
      * @author tansitao
      * @time 2018/9/17 9:29 
      */
    @RequestMapping("/onePeriodReward")
    public MessageResult onePeriodReward(@RequestParam long periodId, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);
        Assert.isTrue(bettingConfig != null, msService.getMessage("NOT_HAVE_ACTIVITY"));
        Assert.isTrue(bettingConfig.getStatus() == BettingConfigStatus.STAGE_PRIZING || bettingConfig.getStatus() == BettingConfigStatus.STAGE_FINISHED , msService.getMessage("HAVE_NOT_OPEN"));

        RewardDTO rewardDTO = new RewardDTO();
        rewardDTO.setPeriodId(bettingConfig.getId());
        //判断用户是否参加了本期活动
        boolean isJoin = bettingRecordService.findListByPeriodId(periodId, user.getId());
        if(isJoin){
            rewardDTO.setIsJoin(BooleanEnum.IS_TRUE);
            //分别查询用户中奖信息和开红包信息
            List<Reward> rewardList = rewardService.findOneByMemberIdAndPeriodId(user.getId(), periodId, RewardBusinessType.GUESS , -1);
            List<Reward> redRewardList = rewardService.findOneByMemberIdAndPeriodId(user.getId(), periodId, RewardBusinessType.REDPACKET,-1);
            //如果中奖，设置中奖金额，是否领奖，等信息
            if(rewardList != null && rewardList.size() > 0){
                rewardDTO.setIsWin(BooleanEnum.IS_TRUE);
                BigDecimal allReward = BigDecimal.ZERO;
                RewardStatus rewardStatus = RewardStatus.RECEIVED;
                //所有奖励加起来为总金额，只要有一个未领取，则领奖状态为待领取
                for (Reward reward:rewardList) {
                    allReward = allReward.add(reward.getRewardNum());
                    if(reward.getStatus() == RewardStatus.UNRECEIVE){
                        rewardStatus = RewardStatus.UNRECEIVE;
                    }
                }
                rewardDTO.setRewardNum(allReward);
                rewardDTO.setPrizeSymbol(rewardList.get(0).getSymbol());
                rewardDTO.setWinStatus(rewardStatus);
            }else {
                rewardDTO.setIsWin(BooleanEnum.IS_FALSE);
            }

            //如果红包抢到了，设置抢到的是否为最佳幸运，设置抢到的红包数量
            if(redRewardList != null && redRewardList.size() > 0){
                rewardDTO.setIsOpenRed(BooleanEnum.IS_TRUE);
                //活动一期只会有一个，所以只取第一条数据
                rewardDTO.setOpenRedNum(redRewardList.get(0).getRewardNum());
                rewardDTO.setIsBestLuck(redRewardList.get(0).getIsBestLuck());
                rewardDTO.setRedStatus(redRewardList.get(0).getStatus());
                rewardDTO.setRedpacketPrizeSymbol(redRewardList.get(0).getSymbol());
            }else {
                rewardDTO.setIsOpenRed(BooleanEnum.IS_FALSE);
            }
        }else {
            rewardDTO.setIsJoin(BooleanEnum.IS_FALSE);
        }

        MessageResult mr = MessageResult.success("success");
        mr.setData(rewardDTO);
        return mr;
    }

    /**
      * 投票活动
      * @author tansitao
      * @time 2018/9/14 17:20 
      */
    @RequestMapping("/betting")
    public MessageResult betting(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam long periodId, @RequestParam BigDecimal coinNum,
                                 @RequestParam long rangeId, @RequestParam String jyPassword, @RequestParam BooleanEnum useSms) throws Exception{
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));

        //验证活动
        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);
        BettingPriceRange bettingPriceRange = priceRangeService.findOne(rangeId);
        Assert.isTrue(bettingConfig != null && bettingPriceRange != null, msService.getMessage("NOT_HAVE_ACTIVITY"));
        Assert.isTrue(bettingPriceRange.getPeriodId().equals(bettingConfig.getId()), msService.getMessage("INTERVAL_WRONG"));
        Assert.isTrue(bettingConfig.getStatus() == BettingConfigStatus.STAGE_VOTING, msService.getMessage("NOT_IN_VOTING_TIME"));
        Assert.isTrue(coinNum.compareTo(bettingConfig.getLowerLimit()) >= 0, msService.getMessage("NOT_LT") + bettingConfig.getLowerLimit());

        BranchRecord smsbranchRecord = null;
        //扣除短信费用
        if(useSms == BooleanEnum.IS_TRUE){
            smsbranchRecord = new BranchRecord();
            //短信支出记录
            smsbranchRecord.setAmount(bettingConfig.getSmsUseNum());
            smsbranchRecord.setBranchType(BranchRecordBranchType.DISBURSE);
            smsbranchRecord.setBusinessType(BranchRecordBusinessType.SMS);
            smsbranchRecord.setExpendMemberId(user.getId());
            smsbranchRecord.setPeriodId(periodId);
            smsbranchRecord.setSpecial(BooleanEnum.IS_FALSE);
            smsbranchRecord.setSymbol(bettingConfig.getSmsSymbol());
//            branchRecordService.save(smsbranchRecord);
//
//            //获取用户钱包信息
//            MemberWallet sluMemberWallet = memberWalletService.findByCoinUnitAndMemberId(bettingConfig.getSmsSymbol(),member.getId());
//            Assert.isTrue(sluMemberWallet != null && sluMemberWallet.getBalance().compareTo(bettingConfig.getSmsUseNum()) >= 0, bettingConfig.getSmsSymbol() + msService.getMessage("INSUFFICIENT_BALANCE"));
//            MessageResult walletResult = memberWalletService.decreaseBalance(sluMemberWallet.getId(), bettingConfig.getSmsUseNum());
//            if (walletResult.getCode() != 0)
//            {
//                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
//            }
        }


        //投票记录
        BettingRecord bettingRecord = new BettingRecord();
        bettingRecord.setBetNum(coinNum);
        bettingRecord.setBetSymbol(bettingConfig.getBetSymbol());
        bettingRecord.setGuessSymbol(bettingConfig.getGuessSymbol());
        bettingRecord.setMemberId(user.getId());
        bettingRecord.setPeriodId(periodId);
        bettingRecord.setPromotionCode(member.getPromotionCode());
        bettingRecord.setRangeId(rangeId);
        bettingRecord.setStatus(BettingRecordStatus.WAITING);
        bettingRecord.setUseSms(useSms);
        bettingRecord.setBeginRange(bettingPriceRange.getBeginRange());
        bettingRecord.setEndRange(bettingPriceRange.getEndRange());
//        bettingRecordService.save(bettingRecord);

        //头片支出流水记录
        BranchRecord branchRecord = new BranchRecord();
        branchRecord.setAmount(coinNum);
        branchRecord.setBranchType(BranchRecordBranchType.DISBURSE);
        branchRecord.setBusinessType(BranchRecordBusinessType.BET);
        branchRecord.setExpendMemberId(user.getId());
//        branchRecord.setHappenTime();
//        branchRecord.setIncomeMemberId();
        branchRecord.setPeriodId(periodId);
        branchRecord.setSpecial(BooleanEnum.IS_FALSE);
        branchRecord.setSymbol(bettingConfig.getBetSymbol());
//        branchRecordService.save(branchRecord);

        //获取、减少用户钱包余额
//        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(bettingRecord.getBetSymbol(),member.getId());
//        if(memberWallet == null)
//        {
//            memberWalletService.createMemberWallet(user.getId(), coinService.findByUnit(bettingRecord.getBetSymbol()));
//            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
//        }
//        MessageResult walletResult = memberWalletService.decreaseBalance(memberWallet.getId(), coinNum);
//        if (walletResult.getCode() != 0)
//        {
//            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
//        }

        //处理投票
        bettingRecordService.dealBetting(useSms, bettingConfig, member, bettingRecord, branchRecord, smsbranchRecord);

        //从缓存中取出奖池信息，修改缓存中的奖池信息
        ValueOperations valueOperations = redisTemplate.opsForValue();
        BigDecimal jackpotBalance = (BigDecimal)valueOperations.get(RedPacketConstant.JACKPOT_BALANCE + "_" + bettingConfig.getGuessSymbol().toUpperCase());
        if(jackpotBalance == null){
            jackpotBalance = statJackpotService.statCurrentJackpot(bettingConfig.getId());
            valueOperations.set(RedPacketConstant.JACKPOT_BALANCE + "_" + bettingConfig.getGuessSymbol().toUpperCase(), jackpotBalance, 10, TimeUnit.MINUTES);
        }else{
            jackpotBalance = jackpotBalance.add(coinNum);
            valueOperations.set(RedPacketConstant.JACKPOT_BALANCE + "_" + bettingConfig.getGuessSymbol().toUpperCase(), jackpotBalance, 10, TimeUnit.MINUTES);
        }
        //推送投票信息
        pushMessageService.pushVoteMessage(bettingRecord);
        //推送投票后的区间
        pushMessageService.pushAllVoteMessage(bettingRecord);
        //推送将次余额
        pushMessageService.pushJackpotBalance(jackpotBalance);
        return MessageResult.success();
    }


    /**
     * 中奖，领取奖励
     * @author tansitao
     * @time 2018/9/17 21:31 
     */
    @RequestMapping("/receiveAward")
    public MessageResult receiveAward(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam long periodId, Long betingId)throws Exception {
        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);
        Member member = memberService.findOne(user.getId());
        Assert.isTrue(bettingConfig != null, msService.getMessage("NOT_HAVE_ACTIVITY"));
        Assert.isTrue(bettingConfig.getStatus() == BettingConfigStatus.STAGE_PRIZING, msService.getMessage("NOT_IN_RECEIVE_TIME"));
        Assert.isTrue(bettingRecordService.findListByPeriodId(periodId, user.getId()), msService.getMessage("NOT_JOIN_ACTIVITY"));
        BigDecimal rewardNum = BigDecimal.ZERO;
        Reward rsReward = new Reward();
        if(betingId == null){
            List<Reward> rewardList = rewardService.findOneByMemberIdAndPeriodId(user.getId(), periodId, RewardBusinessType.GUESS, RewardStatus.UNRECEIVE.getOrdinal());
            Assert.isTrue(rewardList != null && rewardList.size() > 0, msService.getMessage("NOT_REPEAT"));
            rewardNum = rewardService.dealOnePeriodReward(rewardList);
        }else
        {
            Reward reward = rewardService.findByBettingId(user.getId(), betingId, RewardBusinessType.GUESS, RewardStatus.UNRECEIVE.getOrdinal());
            Assert.isTrue(reward != null, msService.getMessage("NOT_REPEAT"));
            rewardNum = rewardService.dealOneBettingReward(reward);
        }
        rsReward.setRewardNum(rewardNum);
        rsReward.setSymbol(bettingConfig.getPrizeSymbol());
        rsReward.setBusinessType(RewardBusinessType.GUESS);
        rsReward.setPromotionCode(member.getPromotionCode());
        rsReward.setPeriodId(bettingConfig.getId());
        rsReward.setPeriod(bettingConfig.getPeriod());
        MessageResult mr = MessageResult.success("success");
        mr.setData(rsReward);
        return mr;
    }

        /**
          * 开启红包
          * @author tansitao
          * @time 2018/9/14 17:26 
          */
    @RequestMapping("/openRedPacket")
    public MessageResult openRedPacket(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam long periodId) {
        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);
        Member member = memberService.findOne(user.getId());
        Assert.isTrue(bettingConfig != null, msService.getMessage("NOT_HAVE_ACTIVITY"));
        Assert.isTrue(bettingRecordService.findListByPeriodId(periodId, user.getId()), msService.getMessage("NOT_JOIN_ACTIVITY"));
        Assert.isTrue(DateUtil.compareDateSec(new Date(), bettingConfig.getRedpacketBeginTime()) >= 0  , msService.getMessage("NOT_IN_RECEIVE_TIME"));
        Assert.isTrue(DateUtil.compareDateSec(new Date(), bettingConfig.getRedpacketEndTime()) <= 0  , msService.getMessage("NOT_IN_RECEIVE_TIME"));

        //开启红包
        RedPacket redPacket = null;
        RedisLock redisLock = RedisLock.getInstance(redisTemplate);
        try {
            if(redisLock.lock()){
                //抢红包
                redPacket = grabRedPacketService.grabRedPacket();
                //处理抢到的红包
                rewardService.dealOpenRedPacket(member, bettingConfig, redPacket);
//                Reward reward = rewardService.findRedPacket(user.getId(), periodId, RewardBusinessType.REDPACKET, -1);
//                Assert.isTrue(reward == null , msService.getMessage("NOT_REPEAT_DRAW"));
//
//                //保存开启红包支出记录
//                BranchRecord branchRecordOut = new BranchRecord();
//                branchRecordOut.setAmount(bettingConfig.getRedpacketUseNum());
//                branchRecordOut.setBranchType(BranchRecordBranchType.DISBURSE);
//                branchRecordOut.setBusinessType(BranchRecordBusinessType.REDPACKET_BET);
//                branchRecordOut.setExpendMemberId(user.getId());
////        branchRecord.setHappenTime();
////        branchRecord.setIncomeMemberId();
//                branchRecordOut.setPeriodId(periodId);
//                branchRecordOut.setSpecial(BooleanEnum.IS_FALSE);
//                branchRecordOut.setSymbol(bettingConfig.getRedpacketSymbol());
//                branchRecordService.save(branchRecordOut);
//
//                //获取、减少用户钱包余额
//                MemberWallet redMemberWallet = memberWalletService.findByCoinUnitAndMemberId(bettingConfig.getRedpacketSymbol(),user.getId());
//                if(redMemberWallet == null)
//                {
//                    memberWalletService.createMemberWallet(user.getId(), coinService.findByUnit(bettingConfig.getRedpacketSymbol()));
//                    throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
//                }
//                MessageResult resWalletResult = memberWalletService.decreaseBalance(redMemberWallet.getId(), bettingConfig.getRedpacketUseNum());
//                if (resWalletResult.getCode() != 0)
//                {
//                    throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
//                }


//                //红包中奖
//                if(redPacket != null
//                        && redPacket.getAmount().compareTo(BigDecimal.ZERO) > 0){
//                    //保存红包抽奖记录
//                    reward = new Reward();
//                    reward.setBusinessType(RewardBusinessType.REDPACKET);
//                    reward.setGetTime(new Date());
//                    reward.setIsBestLuck(redPacket.getIsMax());
//                    reward.setMemberId(user.getId());
//                    reward.setPeriodId(bettingConfig.getId());
//                    reward.setPromotionCode(member.getPromotionCode());
//                    reward.setRewardNum(redPacket.getAmount());
//                    reward.setStatus(RewardStatus.PRIZE);
//                    reward.setSymbol(redPacket.getSymbol());
//                    reward.setVersion(0);
//                    rewardService.save(reward);
//
//                    //保存红包收入记录
//                    BranchRecord branchRecordIn = new BranchRecord();
//                    branchRecordIn.setAmount(reward.getRewardNum());
//                    branchRecordIn.setBranchType(BranchRecordBranchType.INCOME);
//                    branchRecordIn.setBusinessType(BranchRecordBusinessType.REDPACKET_AWARD);
//                    branchRecordIn.setExpendMemberId(user.getId());
////        branchRecord.setHappenTime();
////        branchRecord.setIncomeMemberId();
//                    branchRecordIn.setPeriodId(periodId);
//                    branchRecordIn.setSpecial(BooleanEnum.IS_FALSE);
//                    branchRecordIn.setSymbol(bettingConfig.getGuessSymbol());
//                    branchRecordService.save(branchRecordIn);
//
//                    //获取、增加用户钱包余额
//                    MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(reward.getSymbol(),user.getId());
//                    if(memberWallet == null)
//                    {
//                        memberWallet = memberWalletService.createMemberWallet(user.getId(), coinService.findByUnit(reward.getSymbol()));
//                    }
//                    MessageResult walletResult = memberWalletService.increaseBalance(memberWallet.getId(), reward.getRewardNum());
//                    if (walletResult.getCode() != 0)
//                    {
//                        throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
//                    }
//                    //推送中奖红包信息
//                    pushMessageService.pushOpenRedMessage(redPacket, member.getPromotionCode());
//                }else {
//                    //红包未中奖，保存红包抽奖记录
//                    reward = new Reward();
//                    reward.setBusinessType(RewardBusinessType.REDPACKET);
//                    reward.setGetTime(new Date());
//                    reward.setIsBestLuck(BooleanEnum.IS_FALSE);
//                    reward.setMemberId(user.getId());
//                    reward.setPeriodId(bettingConfig.getId());
//                    reward.setPromotionCode(member.getPromotionCode());
//                    reward.setRewardNum(BigDecimal.ZERO);
//                    reward.setStatus(RewardStatus.UNPRIZE);
//                    reward.setSymbol(bettingConfig.getRedpacketPrizeSymbol());
//                    reward.setVersion(0);
//                    rewardService.save(reward);
//                }
            }else{
                return MessageResult.error(msService.getMessage("OPEN_RED_FIAL"));
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }finally {
            redisLock.unlock();
        }

        MessageResult mr = MessageResult.success("success");
        mr.setData(redPacket);
        return mr;
    }
}
