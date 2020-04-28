package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionType implements BaseEnum {
    RECHARGE("充值"), //0
    WITHDRAW("提现"), //1
    TRANSFER_ACCOUNTS("转账"), //2
    EXCHANGE("币币交易"), //3
    OTC_BUY("法币买入"), //4
    OTC_SELL("法币卖出"), //5
    ACTIVITY_AWARD("活动奖励"), //6
    PROMOTION_AWARD("推广奖励"), //7
    TRANSFER("划转"), //8
    VOTE("投票"), //9
    ADMIN_RECHARGE("人工充值"), //10
    MATCH("配对"), //11
    EXCHANGE_PROMOTION_AWARD("币币交易返佣奖励"), //edit by yangch 时间： 2018.05.16 原因：添加“币币交易返佣奖励”类型    12
    EXCHANGE_PARTNER_AWARD("币币交易合伙人奖励"), //edit by yangch 时间： 2018.05.29 原因：添加“币币交易合伙人奖励”类型  13
    BUSINESS_DEPOSIT("商家认证保证金"), //edit by tansitao 时间： 2018/6/9 原因：添加商家认证保证金类型 14
    ADMIN_LOCK_RECHARGE("锁仓充值"),  //edit by yangch 时间： 2018.06.12 原因：添加“锁仓充值”类型 15
    ADMIN_LOCK_ACTIVITY("锁仓活动"), //add tansitao 时间： 2018/6/20 原因：添加锁仓活动 16
    ADMIN_ADJUST_BALANCE("手动调账"), //add by yangch 时间： 2018.06.27 原因：添加手动调账类型 17
    FINANCIAL_ACTIVITY("理财锁仓"), //add tansitao 时间： 2018/6/20 原因：添加理财锁仓 18
    THIRD_PAY("三方支付"),          //add by fumy dete: 2018.07.24 reason : 第三方支付 19
    QUANTIFY_ACTIVITY("SLB节点产品"),   //add by fumy dete: 2018.07.24 reason : SLB节点产品 20
    LOCK_COIN_PROMOTION_AWARD("SLB节点产品共识奖励"), //锁币活动推广返佣奖励  SLB节点产品共识奖励 21
    STO_ACTIVITY("STO锁仓"), //22  STO锁仓 //edit by tansitao 时间： 2018/11/20 原因：修改活动类型，之前的类型多了"_"
    LOCK_COIN_PROMOTION_AWARD_STO("STO推荐奖励"),//23 锁仓推荐奖励
    ADVERTISE_FEE("广告手续费"),//广告费用 24
    EXCHANGE_FAST("闪兑"),//闪兑 25
    IEO_ACTIVITY("IEO锁仓活动"),//闪兑 26
    HQB_ACTIVITY("活期宝活动"), //活期宝 27
    GOLD_KEY_OWN("本人锁仓奖励"), //金钥匙本人锁仓 28
    GOLD_KEY_TEAM("团队锁仓奖励"), //金钥匙团队锁仓 29
    /**
     * BCC赋能计划 30
     */
    ENERGIZE_LOCK("BCC赋能计划"),
    /**
     * 参与布朗计划 31
     */
    SLP_PARTICIPATION_PLAN("参与布朗计划"),
    /**
     * 布朗计划释放 32
     */
    SLP_RELEASE("布朗计划释放"),
    SUPER_PARTNER_AWARD("超级合伙人手续费20%奖励"),//33
    SUPER_PARTNER_LOCK("超级合伙人锁仓"),//34
    SUPER_PARTNER_EXIT("退出超级合伙人"),//35
    SUPER_EXIT_COMMUNITY("违约退出社区"),//36
    SUPER_PARTNER_ACTIVE_AWARD("超级合伙人活跃成员奖励"),//37
    DIRECT_PAY("微信/支付宝-直接支付"),//38
    LOCK_UTT("UTT活动锁仓"),//39
    UNLOCK_UTT("UTT活动释放"),//40
    /**
     * 41 微信/支付宝-直接支付收益归集
     */
    DIRECT_PAY_PROFIT("微信/支付宝-直接支付收益归集"),
    /**
     * 42 法币交易手续费归集
     */
    OTC_JY_RATE_FEE("法币交易手续费归集"),
    /**
     * 43 孵化区锁仓
     */
    INCUBOTORS_LOCK("孵化区锁仓"),
    /**
     * 44 孵化区解仓
     */
    INCUBOTORS_UNLOCK("孵化区解仓"),
    /**
     *法币广告手续费归集45
     */
    ADVERTISE_FEE_COLLECTION("法币广告手续费归集"),
    /**
     * BB交易手续费归集46
     */
    EXCHANGE_FEE_COLLECTION("BB交易手续费归集"),
    /**
     * 提币手续费归集 47
     *
     */
    UP_COIN_FEE_COLLECTION("提币手续费归集"),
    /**
     * 项目方中心冻结金额48
     */
    SUPPORT_PROJECT_RETURN("项目方中心服务退回"),
    /**
     * 项目方中心服务支出49
     */
    SUPPORT_PROJECT_PAY("项目方中心服务支出"),
    /**
     * 50 资金账户划转
     */
    FUND_TRANSFER("资金账户划转"),
    /**
     * 51 币币账户划转
     */
    EXCHANGE_TRANSFER("币币账户划转"),
    /**
     * 52 OTC账户划转
     */
    OTC_TRANSFER("OTC账户划转"),
    /**
     * 53 活期宝划转
     */
    HQB_TRANSFER("活期宝划转"),
    /**
     * 54
     */
    LOCK_BTLF_PAY("BTLF锁仓"),
    /**
     * 55
     */
    RELEASE_BTLF("BTLF释放"),
    /**
     * 会员VIP权益开通费用 56
     */
    MEMBER_VIP_OPENING("会员VIP权益开通费用"),
    /**
     * 57
     */
    BUY_MEMBER_LOCK("购买会员锁仓"),
    /**
     * 58
     */
    BUY_MEMBER_UNLOCK("购买会员锁仓到期返还"),
    /**
     * 59
     */
    RELEASE_ESP("ESP释放"),
    /**
     * 60
     */
    LOCK_ESP("ESP充值锁仓"),
    /**
     * 61
     */
    BUY_LUCKY_BULL("购买小牛快跑"),
    /**
     * 62  小牛快跑中奖
     */
    LUCKY_WIN_BULL("小牛快跑中奖"),
    /**
     * 63 小牛快跑开奖资金返还
     */
    LUCKY_RETURN_BULL("小牛快跑开奖合局资金返还"),
    /**
     * 64 小牛快跑追加奖金发放
     */
    LUCKY_APPEND_WX_BULL("小牛快跑追加奖金发放"),
    /**
     * 65 购买幸运宝幸运号
     */
    BUY_LUCKY_NUMBER("购买幸运宝幸运号"),
    /**
     * 66  幸运宝幸运号中奖
     */
    LUCKY_WIN_NUMBER("幸运宝幸运号中奖"),
    /**
     * 67 幸运宝幸运号取消开奖资金返还
     */
    LUCKY_RETURN_NUMBER("幸运宝幸运号取消开奖资金返还"),
    /**
     * 68 幸运宝幸运号追加奖金发放
     */
    LUCKY_APPEND_WX_NUMBER("幸运宝幸运号追加奖金发放"),
    /**
     * 69 会员费归集总账号
     */
    MEMBER_ADD_TOTAL_ACCOUNT("会员费归集"),
    /**
     * 70 年终活动奖励发放
     */
    FESTIVAL_NUMBER_LOCK("年终活动奖励锁仓") ,
    /**
     * 71 年终活动奖励释放
     */
    FESTIVAL_NUMBER_LOCK_RELEASED("年终活动奖励锁仓释放"),
    /**
     * 72
     */
    RED_PACK_COST("红包活动支出"),
    /**
     * 73
     */
    RED_PACK_RETURN("红包活动退回"),
    /**
     * 74 修改默认法币
     */
    CHANGE_CURRENCY("修改默认法币"),
    /**
     * 75 广告上架冻结
     */
    PUT_ON_SHELVES_FROZEN("广告上架冻结"),
    /**
     * 76 广告下架解冻
     */
    PUT_OFF_SHELVES_FROZEN("广告下架解冻"),
    /**
     * 77 DCC锁仓
     */
    LOCK_DCC("DCC锁仓"),
    /**
     * 78 DCC释放
     */
    RELEASE_DCC("DCC释放"),
    /**
     * 79 经纪人购买USDC
     */
    AGENT_BUY_USDC("经纪人购买USDC"),
    /**
     * 80 法币交易返佣
     */
    CURRENCY_GET("法币交易返佣"),

    EXT8("占位，扩展8"),
    EXT9("占位，扩展9");


    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }

    public static TransactionType valueOfOrdinal(int ordinal) {
        TransactionType[] values = TransactionType.values();
        for (TransactionType transactionType : values) {
            int o = transactionType.getOrdinal();
            if (o == ordinal) {
                return transactionType;
            }
        }
        return null;
    }
}
