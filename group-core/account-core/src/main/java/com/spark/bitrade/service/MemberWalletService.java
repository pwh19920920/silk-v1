package com.spark.bitrade.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dao.MemberDepositDao;
import com.spark.bitrade.dao.MemberWalletDao;
import com.spark.bitrade.dao.OrderDao;
import com.spark.bitrade.dto.MemberWalletDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.mapper.dao.MemberWalletMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vo.MemberWalletBalanceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;


@Service
@Slf4j
public class MemberWalletService extends BaseService {
    @Autowired
    private MemberWalletDao memberWalletDao;
    @Autowired
    //private CoinDao coinDao;
    private CoinService coinService;

    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private MemberDepositDao depositDao;
    @Autowired
    private OrderDao orderDao;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private MemberWalletMapper memberWalletMapper;
    @Autowired
    private ICreditCoinEvent iCreditCoinEvent;
    @Autowired
    private MemberService memberService;


    //del by tansitao 时间： 2018/11/21 原因：取消保存时候对钱包进行缓存
//    @Cacheable(cacheNames = "memberWallet", key = "'entity:memberWallet:'+#wallet.memberId+'-'+#wallet.coin.name")
    public MemberWallet save(MemberWallet wallet) {
        return memberWalletDao.saveAndFlush(wallet);
    }

    /**
     * 获取钱包
     *
     * @param coin     otc币种
     * @param memberId
     * @return
     */
    public MemberWallet findByOtcCoinAndMemberId(OtcCoin coin, long memberId) {
        Coin coin1 = coinService.findByUnit(coin.getUnit());
        return memberWalletDao.findByCoinAndMemberId(coin1, memberId);
    }

    /**
     *  * 判断地址是否存在
     *  * @author tansitao
     *  * @time 2018/7/31 16:48 
     *  
     */
    @ReadDataSource
    //addby  shenzucai 时间： 2019.01.25  原因：使用缓存，减少数据库穿透
    @Cacheable(cacheNames = "memberWallets", key = "'entity:memberWallet:'+#address")
    public boolean hasExistByAddr(String address) {
        String addr = memberWalletMapper.hasExistByAddr(address);
        if (StringUtils.isEmpty(addr)) {
            return false;
        }
        return true;
    }

    /**
     * 获取钱包（优先从缓存中获取）
     *
     * @param coin     otc币种
     * @param memberId
     * @return
     */
    @Cacheable(cacheNames = "memberWallet", key = "'entity:memberWallet:'+#memberId+'-'+#coin.name")
    public MemberWallet findCacheByOtcCoinAndMemberId(OtcCoin coin, long memberId) {
        Coin coin1 = coinService.findByUnit(coin.getUnit());
        return memberWalletDao.findByCoinAndMemberId(coin1, memberId);
    }

    /**
     * 钱包充值
     *
     * @param wallet
     * @param amount
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult recharge(MemberWallet wallet, BigDecimal amount) {
        if (wallet == null) {
            return new MessageResult(500, "wallet cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new MessageResult(500, "amount must large then 0");
        }
        int result = memberWalletDao.increaseBalance(wallet.getId(), amount);
        if (result > 0) {
            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(amount);
            transaction.setSymbol(wallet.getCoin().getUnit());
            transaction.setAddress(wallet.getAddress());
            transaction.setMemberId(wallet.getMemberId());
            transaction.setType(TransactionType.RECHARGE);
            transaction.setFee(BigDecimal.ZERO);
            transactionService.save(transaction);
            //增加记录
            return new MessageResult(0, "success");
        } else {
            return new MessageResult(500, "recharge failed");
        }
    }

    /**
     * 钱包充值
     *
     * @param coin    币种名称
     * @param address 地址
     * @param amount  金额
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult recharge(Coin coin, String address, BigDecimal amount, String txid) {
        MemberWallet wallet = findByCoinAndAddress(coin, address);

        if (wallet == null) {

            //add by tansitao 时间： 2018/11/19 原因：优化充币体验，针对主从币（谌祖财核查）
            if (!StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                Coin baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());

                MemberWallet baseWallet = findByCoinAndAddress(baseCoin, address);

                if (baseWallet != null) {

                    //add by  shenzucai 时间： 2018.11.20  原因：针对有该币种记录但是无address的情况进行处理
                    wallet = findByCoinAndMemberId(coin, baseWallet.getMemberId());

                    if (wallet == null) {

                        wallet = createMemberWallet(baseWallet.getMemberId(), coin);
                        wallet.setAddress(address);
                        save(wallet);

                    } else {

                        memberWalletDao.updateMemberWalletAddress(wallet.getId(), address);

                    }
                } else {
                    return new MessageResult(500, "wallet cannot be null1");
                }
            } else {

                return new MessageResult(500, "wallet cannot be null2");
            }
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new MessageResult(500, "amount must large then 0");
        }


        //add by zyj 2019.01.07:接入风控
        Member member = memberService.findOneById(wallet.getMemberId());
        CreditCoinInfo creditCoinInfo = new CreditCoinInfo();
        creditCoinInfo.setCoin(coin.getUnit());
        creditCoinInfo.setAmount(amount);
        creditCoinInfo.setSource("");
        creditCoinInfo.setTarget(address);
        MessageResult res = iCreditCoinEvent.creditCoin(member,creditCoinInfo);


        MemberDeposit deposit = new MemberDeposit();
        deposit.setAddress(address);
        deposit.setAmount(amount);
        deposit.setMemberId(wallet.getMemberId());
        deposit.setTxid(txid);
        deposit.setUnit(wallet.getCoin().getUnit());
        deposit =  depositDao.save(deposit);

        //edit by tansitao 时间： 2018/12/21 原因：按照谌哥要求修改代码 ，满足DCCT转变DCC
        if ("DCCT".equalsIgnoreCase(wallet.getCoin().getUnit())) {
            //add by  shenzucai 时间： 2018.12.25  原因：修复异常赋值的情况
            Long memberId = wallet.getMemberId();
            wallet = findByCoinUnitAndMemberId("DCC", memberId);
            if(wallet == null){
                Coin dccCoin = coinService.findByUnit("DCC");
                wallet = createMemberWallet(memberId, dccCoin);
            }
        }

        //edit by tansitao 时间： 2018/5/18 原因：修改钱包操作为sql
//        wallet.setBalance(wallet.getBalance().add(amount));
        memberWalletDao.increaseBalance(wallet.getId(), amount);
        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(amount);
        transaction.setSymbol(wallet.getCoin().getUnit());
        transaction.setAddress(address);
        transaction.setMemberId(wallet.getMemberId());
        transaction.setType(TransactionType.RECHARGE);
        transaction.setFee(BigDecimal.ZERO);

        transactionService.save(transaction);


        //增加记录
        return new MessageResult(0, "success",deposit);

    }


    /**
     * 根据币种和钱包地址获取钱包
     *
     * @param coin
     * @param address
     * @return
     */
    //add by  shenzucai 时间： 2019.01.25  原因：启用缓存，指定从只读库读取数据信息，该方法不做账务处理，只做存在性判断，此次修改设计模块为ucenter-api wallet
    @Cacheable(cacheNames = "memberWallet", key = "'entity:memberWallet:'+#coin.name+'-'+#address")
    @ReadDataSource
    public MemberWallet findByCoinAndAddress(Coin coin, String address) {
        return memberWalletDao.findByCoinAndAddress(coin, address);
    }


    /**
     * 根据币种和用户ID获取钱包
     *
     * @param coin
     * @param member
     * @return
     */
    public MemberWallet findByCoinAndMember(Coin coin, Member member) {
        return memberWalletDao.findByCoinAndMemberId(coin, member.getId());
    }

    public MemberWallet findByCoinUnitAndMemberId(String coinUnit, Long memberId) {
        Coin coin = coinService.findByUnit(coinUnit);
        return memberWalletDao.findByCoinAndMemberId(coin, memberId);
    }

    /**
     * 只写数据源，查询用户钱包
     *
     * @param coinName 币种名称，eg：Silubium
     * @param memberId 会员ID
     * @return
     */
    @ReadDataSource
    public MemberWallet findByCoinNameAndMemberIdReadOnly(String coinName, Long memberId) {
        return memberWalletMapper.findByCoinAndMemberId(coinName, memberId);
    }

    /**
     * 只写数据源，查询用户钱包
     *
     * @param coinUnit 币种名称，eg：SLB
     * @param memberId 会员ID
     * @return
     */
    @Cacheable(cacheNames = "memberWallet", key = "'entity:memberWallet:read:'+#memberId+'-'+#coinUnit")
    @ReadDataSource
    public MemberWallet findByCoinUnitAndMemberIdReadOnly(String coinUnit, Long memberId) {
        return memberWalletMapper.findByCoinUnitAndMemberId(coinUnit, memberId);
    }


    /***
      * 钱包缓存接口
      * @author yangch
      * @time 2018.06.24 16:02 
     * @param coinUnit
     * @param memberId
     */
    @Cacheable(cacheNames = "memberWallet", key = "'entity:memberWallet:'+#memberId+'-'+#coinUnit")
    public MemberWallet findCacheByCoinUnitAndMemberId(String coinUnit, Long memberId) {
        Coin coin = coinService.findByUnit(coinUnit);
        MemberWallet memberWallet = memberWalletDao.findByCoinAndMemberId(coin, memberId);

        //账户不存在时 创建账户
        if (memberWallet == null) {
            try {
                memberWallet = getService().createMemberWallet(memberId, coin);
            } catch (Exception ex) {
                log.warn("创建账户失败,memberId={},coinUnit={}", memberId, coinUnit);
            }
        }

        return memberWallet;
    }

    /***
      * 钱包缓存接口
      * @author yangch
      * @time 2018.06.24 16:02 
     * @param coinUnit
     * @param memberId
     */
    @Cacheable(cacheNames = "memberWallet", key = "'entity:memberWallet:'+#address+'-'+#coinUnit")
    public MemberWallet findCacheByCoinUnitAndAddress(String coinUnit, String address) {
        Coin coin = coinService.findByUnit(coinUnit);
        MemberWallet memberWallet = memberWalletDao.findByCoinAndAddress(coin, address);
        return memberWallet;
    }

    public MemberWallet findByCoinAndMemberId(Coin coin, Long memberId) {
        return memberWalletDao.findByCoinAndMemberId(coin, memberId);
    }

    /**
     * 根据用户查找所有钱包
     *
     * @param member
     * @return
     */
    public List<MemberWallet> findAllByMemberId(Member member) {
        return memberWalletDao.findAllByMemberId(member.getId());
    }

    public List<MemberWallet> findAllByMemberId(Long memberId) {
        return memberWalletDao.findAllByMemberId(memberId);
    }

    //add by tansitao 时间： 2018/4/21 原因：增加字段miner_fee、base_coin_unit，如果coin表字段添加，修改，可能会影响前端钱包、币种获取不到地址
    public List<MemberWallet> findAllByMemberIdLeftJion(Long memberId) {
        List<MemberWallet> memberWalletList = new ArrayList<MemberWallet>();
        //左管理查询用户钱包地址信息
        String sql = "SELECT a.id, a.address, a.balance, a.frozen_balance, a.member_id, a.version, a.coin_id, a.is_lock, a.lock_balance, " +
                " c.can_auto_withdraw, c.can_recharge, c.can_transfer, c.can_withdraw, c.cny_rate, c.enable_rpc, c.has_legal, c.is_platform_coin, c.max_tx_fee, c.max_withdraw_amount," +
                " c.min_tx_fee,c.min_withdraw_amount, c.`name`, c.name_cn, c.sort, c.`status`, c.unit, c.usd_rate, c.withdraw_threshold, c.miner_fee, c.base_coin_unit,c.min_deposit_amount,c.has_label " +
                //add|edit|del by  shenzucai 时间： 2018.08.23  原因：添加where c.`status` = 0 币种的状态
                "FROM coin c LEFT JOIN (SELECT * FROM member_wallet m WHERE m.member_id = :memberId) a ON c.`name` = a.coin_id where c.`status` = 0 order by c.sort";
        Query query = em.createNativeQuery(sql);
        //设置结果转成Map类型
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List rowslist = query.setParameter("memberId", memberId).getResultList();
        //遍历查询结果，将其保存到内存中
        for (Object object : rowslist) {
            Map map = (HashMap) object;
            MemberWallet memberWallet = new MemberWallet();
            memberWallet.setIsLock("1".equals(String.valueOf(map.get("is_lock"))) ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE);
            if (!StringUtils.isEmpty(map.get("address") + "") && !"null".equals(map.get("address") + "")) {
                memberWallet.setAddress("" + map.get("address"));
            }
            memberWallet.setMemberId(memberId);
            if ((BigDecimal) map.get("frozen_balance") == null) {
                memberWallet.setFrozenBalance(BigDecimal.valueOf(0));
            } else {
                memberWallet.setFrozenBalance((BigDecimal) map.get("frozen_balance"));
            }

            if ((BigDecimal) map.get("balance") == null) {
                memberWallet.setBalance(BigDecimal.valueOf(0));
            } else {
                memberWallet.setBalance((BigDecimal) map.get("balance"));
            }

            if ((BigDecimal) map.get("lock_balance") == null) {
                memberWallet.setLockBalance(BigDecimal.valueOf(0));
            } else {
                memberWallet.setLockBalance((BigDecimal) map.get("lock_balance"));
            }

            if (!StringUtils.isEmpty(map.get("id") + "") && !"null".equals(map.get("id") + "")) {
                memberWallet.setId(Long.parseLong(map.get("id") + ""));
            }
            if (!StringUtils.isEmpty(map.get("version") + "") && !"null".equals(map.get("id") + "")) {
                memberWallet.setVersion((Integer) map.get("version"));
            }

            Coin coin = new Coin();
            coin.setName("" + map.get("name"));

            coin.setCanAutoWithdraw("0".equals(String.valueOf(map.get("can_auto_withdraw"))) ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE);
            coin.setCanRecharge("0".equals(map.get("can_recharge") + "") ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE);
            coin.setCanTransfer("0".equals(map.get("can_transfer") + "") ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE);
            coin.setCanWithdraw("0".equals(map.get("can_withdraw") + "") ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE);
            coin.setCnyRate((Double) map.get("cny_rate"));
            coin.setEnableRpc("0".equals(map.get("enable_rpc")) ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE);
            coin.setHasLegal((Boolean) map.get("has_legal"));
            coin.setIsPlatformCoin("0".equals(map.get("is_platform_coin") + "") ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE);
            coin.setMaxTxFee((Double) map.get("max_tx_fee"));
            coin.setMaxWithdrawAmount((BigDecimal) map.get("max_withdraw_amount"));
            coin.setMinTxFee((Double) map.get("min_tx_fee"));
            coin.setMinWithdrawAmount((BigDecimal) map.get("min_withdraw_amount"));
            coin.setNameCn("" + map.get("name_cn"));
            coin.setSort((Integer) map.get("sort"));
            coin.setStatus("0".equals(map.get("is_platform_coin") + "") ? CommonStatus.NORMAL : CommonStatus.ILLEGAL);
            coin.setUnit("" + map.get("unit"));
            coin.setUsdRate((Double) map.get("usd_rate"));
            coin.setWithdrawThreshold((BigDecimal) map.get("withdraw_threshold"));
            coin.setMinerFee((BigDecimal) map.get("miner_fee"));
            coin.setBaseCoinUnit((String) map.get("base_coin_unit"));
            //add  shenzucai 时间： 2018.10.11  原因：添加最小到账 是否具有标签 start
            coin.setMinDepositAmount((Double) map.get("min_deposit_amount"));
            coin.setHasLabel("0".equals(map.get("has_label") + "") ? CommonStatus.NORMAL : CommonStatus.ILLEGAL);
            //add  shenzucai 时间： 2018.10.11  原因：添加最小到账 是否具有标签 end
            memberWallet.setCoin(coin);
            memberWalletList.add(memberWallet);
        }
        return memberWalletList;
    }


    /**
     * 冻结钱包
     *
     * @param memberWallet
     * @param amount
     * @return
     */
    public MessageResult freezeBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.freezeBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("冻结钱包失败");
        }
    }


    /**
     * 添加钱包地址更新操作
     *
     * @param walletId
     * @param address
     * @return true
     * @author shenzucai
     * @time 2018.11.20 9:31
     */
    public MessageResult updateMemberWalletAddress(long walletId, String address) {
        int ret = memberWalletDao.updateMemberWalletAddress(walletId, address);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("钱包地址更新失败");
        }
    }

    /**
     * 冻结钱包到锁仓余额
     *
     * @param memberWallet
     * @param amount
     * @return
     */
    public MessageResult freezeBalanceToLockBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.freezeBalanceToLockBlance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("冻结钱包到锁仓余额失败");
        }
    }



    /**
     * 解冻钱包
     *
     * @param memberWallet
     * @param amount
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult thawBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.thawBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("解冻钱包失败");
        }
    }

    /**
     * 从锁仓余额解冻钱包
     *
     * @param memberWallet
     * @param amount
     * @return
     */
    public MessageResult thawBalanceFromLockBlance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.thawBalanceFromLockBlance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("从锁仓余额解冻钱包失败");
        }
    }

    /**
     * 从可用余额中减去指定金额
     *
     * @param memberWallet
     * @param amount
     * @author Zhang Yanjun
     * @time 2018.08.06 10:19
     */
    public MessageResult subtractBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.subtractBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("可用余额减去金额失败");
        }
    }

    /**
     * 从冻结余额中减去指定金额
     *
     * @param memberWallet
     * @param amount
     * @author Zhang Yanjun
     * @time 2018.08.06 10:19
     */
    public MessageResult subtractFreezeBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.subtractFreezeBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("冻结余额减去失败");
        }
    }

    /**
     * 从锁仓余额中减去指定金额
     *
     * @param memberWallet
     * @param amount
     * @author Zhang Yanjun
     * @time 2018.08.06 10:19
     */
    public MessageResult subtractLockBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.subtractLockBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("锁仓余额减去金额失败");
        }
    }

    /**
     *  * 从锁仓余额解冻钱包
     *  * @author tansitao
     *  * @time 2018/7/3 17:01 
     *  
     */
    public MessageResult updateBlanceAndLockBlance(MemberWallet memberWallet, BigDecimal addBalanceAmount, BigDecimal subLockBalanceAamount) {
        int ret = memberWalletDao.updateBlanceAndLockBlance(memberWallet.getId(), addBalanceAmount, subLockBalanceAamount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("锁仓余额解冻钱包失败");
        }
    }


    /**
     * 手动平账 佣金余额+当前余额
     *
     * @param memberWallet
     * @param amount
     * @return
     */
    public MessageResult commissionBalanceFromBlance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.commissionBalanceFromBlance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("账户余额增加佣金失败");
        }
    }


    /**
     * 放行更改双方钱包余额
     *
     * @param order
     * @param ret
     */
    //edit by tansitao 时间： 2018/9/6 原因：修改手续费为传入参数
    public MessageResult transfer(Order order, int ret) {
        if (ret == 1) {
            //用户放行
            MemberWallet customerWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getCustomerId());
            int is = memberWalletDao.decreaseFrozen(customerWallet.getId(), order.getNumber());
            if (is > 0) {
                MemberWallet memberWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
                if (memberWallet == null) {
                    //add by tansitao 时间： 2018/11/15 原因：钱包记录不存在则自动创建
                    Coin coin = coinService.findByUnit(order.getCoin().getUnit());
                    memberWallet = createMemberWallet(order.getMemberId(), coin);
                }
                int a = memberWalletDao.increaseBalance(memberWallet.getId(), BigDecimalUtils.sub(order.getNumber(), order.getCommission()));
                if (a <= 0) {
                    //throw new UnexpectedException("Information Expired");
                    return MessageResult.error(-1, "账户余额增加失败");
                }
            } else {
                return MessageResult.error(-2, "账户余额扣减失败");
                //throw new UnexpectedException("Information Expired");
            }
        } else {
            //商家放行
            MemberWallet customerWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
            int is;
            if("USDC".equals(order.getCoin().getUnit())){
                //如果是USDC 扣除商家订单数量的币，增加用户订单数量减去手续费的币
                is = memberWalletDao.decreaseFrozen(customerWallet.getId(), order.getNumber());
            }else{
                is = memberWalletDao.decreaseFrozen(customerWallet.getId(), BigDecimalUtils.add(order.getNumber(), order.getCommission()));
            }
            if (is > 0) {
                MemberWallet memberWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getCustomerId());
                if (memberWallet == null) {
                    //add by tansitao 时间： 2018/11/15 原因：钱包记录不存在则自动创建
                    Coin coin = coinService.findByUnit(order.getCoin().getUnit());
                    memberWallet = createMemberWallet(order.getCustomerId(), coin);
                }
                int a;
                if("USDC".equals(order.getCoin().getUnit())){
                    //如果是USDC 扣除商家订单数量的币，增加用户订单数量减去手续费的币
                    a = memberWalletDao.increaseBalance(memberWallet.getId(), BigDecimalUtils.sub(order.getNumber(),order.getCommission()));
                }else{
                    a = memberWalletDao.increaseBalance(memberWallet.getId(), order.getNumber());
                }
                if (a <= 0) {
                    //throw new UnexpectedException("Information Expired");
                    return MessageResult.error(-1, "账户余额增加失败");
                }
            } else {
                //throw new UnexpectedException("Information Expired");
                return MessageResult.error(-2, "账户余额扣减失败");
            }
        }

        return MessageResult.success();
    }

    /**
     *  放行更改双方钱包余额
     * @author shenzucai
     * @time 2019.10.01 18:45
     * @param order
     * @param ret
     * @return true
     */
    public MessageResult transferBOA(Order order, int ret) {
        if (ret == 1) {
            int is = orderDao.updateOrderBalanceByhandleBOA(new Date(),order.getOrderMoney(),order.getOrderSn(),order.getCustomerId());
            if (is > 0) {
                MemberWallet memberWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
                if (memberWallet == null) {
                    //add by tansitao 时间： 2018/11/15 原因：钱包记录不存在则自动创建
                    Coin coin = coinService.findByUnit(order.getCoin().getUnit());
                    memberWallet = createMemberWallet(order.getMemberId(), coin);
                }
                int a = memberWalletDao.increaseBalance(memberWallet.getId(), BigDecimalUtils.sub(order.getNumber(), order.getCommission()));
                if (a <= 0) {
                    //throw new UnexpectedException("Information Expired");
                    return MessageResult.error(-1, "账户余额增加失败");
                }
            } else {
                return MessageResult.error(-2, "账户余额扣减失败");
                //throw new UnexpectedException("Information Expired");
            }
        }

        return MessageResult.success();
    }


    /* */

    /**
     * 放行更改双方钱包余额
     *
     * @param order
     * @param ret
     * @throws InformationExpiredException
     */
    public void transferAdmin(Order order, int ret) throws InformationExpiredException {
        if (ret == 1 || ret == 4) {
            trancerDetail(order, order.getCustomerId(), order.getMemberId());
        } else {
            trancerDetail(order, order.getMemberId(), order.getCustomerId());
        }

    }


    private void trancerDetail(Order order, long sellerId, long buyerId) throws InformationExpiredException {
        MemberWallet customerWallet = findByOtcCoinAndMemberId(order.getCoin(), sellerId);//卖币者
        //edit by fumy. date:2019.04.01. reason:新上币种用户没有该币种的钱包记录.
        if(customerWallet == null){
            //若客户没有钱包，给用户创建新的钱包
            Coin coin = coinService.findByUnit(order.getCoin().getUnit());
            customerWallet = createMemberWallet(sellerId,coin);
        }
        //卖币者，买币者要处理的金额
        BigDecimal sellerAmount, buyerAmount;
        if (order.getMemberId() == sellerId) {
            sellerAmount = BigDecimalUtils.add(order.getNumber(), order.getCommission());
            buyerAmount = order.getNumber();
        } else {
            sellerAmount = order.getNumber();
            buyerAmount = order.getNumber().subtract(order.getCommission());
        }
        int is = memberWalletDao.decreaseFrozen(customerWallet.getId(), sellerAmount);
        if (is > 0) {
            MemberWallet memberWallet = findByOtcCoinAndMemberId(order.getCoin(), buyerId);
            if(memberWallet == null){
                //若客户没有钱包，给用户创建新的钱包
                Coin coin = coinService.findByUnit(order.getCoin().getUnit());
               memberWallet = createMemberWallet(buyerId,coin);
            }
            int a = memberWalletDao.increaseBalance(memberWallet.getId(), buyerAmount);
            if (a <= 0) {
                throw new InformationExpiredException("信息过期");
            }
        } else {
            throw new InformationExpiredException("信息过期");
        }
    }

    /**
     * 减少钱包余额
     *
     * @param memberWallet
     * @param amount
     * @return true
     * @author fumy
     * @time 2018.07.09 9:06
     */
    public int deductBalance(MemberWallet memberWallet, BigDecimal amount) {
        return memberWalletDao.decreaseBalance(memberWallet.getId(), amount);
    }

    public List<MemberWallet> findAll() {
        return memberWalletDao.findAll();
    }

    public List<MemberWallet> findAllByCoin(Coin coin) {
        return memberWalletDao.findAllByCoin(coin);
    }

    /**
     * 锁定钱包
     *
     * @param uid
     * @param unit
     * @return
     */
    @Transactional
    public boolean lockWallet(Long uid, String unit) {
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getIsLock() == BooleanEnum.IS_FALSE) {
            wallet.setIsLock(BooleanEnum.IS_TRUE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 解锁钱包
     *
     * @param uid
     * @param unit
     * @return
     */
    @Transactional
    public boolean unlockWallet(Long uid, String unit) {
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getIsLock() == BooleanEnum.IS_TRUE) {
            wallet.setIsLock(BooleanEnum.IS_FALSE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 提币开启
     *
     * @param uid
     * @param unit
     * @return
     */
    @Transactional
    public boolean coinWalletEnabled(Long uid, String unit) {
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getEnabledOut() == BooleanEnum.IS_FALSE) {
            wallet.setEnabledOut(BooleanEnum.IS_TRUE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 提币关闭
     *
     * @param uid
     * @param unit
     * @return
     */
    @Transactional
    public boolean coinWalletClose(Long uid, String unit) {
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getEnabledOut() == BooleanEnum.IS_TRUE) {
            wallet.setEnabledOut(BooleanEnum.IS_FALSE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 充值钱包关闭
     *
     * @param uid
     * @param unit
     * @return
     */
    @Transactional
    public boolean payWalletClose(Long uid, String unit) {
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getEnabledIn() == BooleanEnum.IS_TRUE) {
            wallet.setEnabledIn(BooleanEnum.IS_FALSE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 充值开启
     *
     * @param uid
     * @param unit
     * @return
     */
    @Transactional
    public boolean payWalletEnabled(Long uid, String unit) {
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getEnabledIn() == BooleanEnum.IS_FALSE) {
            wallet.setEnabledIn(BooleanEnum.IS_TRUE);
            return true;
        } else {
            return false;
        }
    }

    public MemberWallet findOneByCoinNameAndMemberId(String coinName, long memberId) {
        BooleanExpression and = QMemberWallet.memberWallet.coin.name.eq(coinName)
                .and(QMemberWallet.memberWallet.memberId.eq(memberId));
        return memberWalletDao.findOne(and);
    }

    public Page<MemberWalletDTO> joinFind(List<Predicate> predicates, QMember qMember, QMemberWallet qMemberWallet, PageModel pageModel) {
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        predicates.add(qMember.id.eq(qMemberWallet.memberId));
        JPAQuery<MemberWalletDTO> query = queryFactory.select(
                Projections.fields(MemberWalletDTO.class, qMemberWallet.id.as("id"), qMemberWallet.memberId.as("memberId"), qMember.username, qMember.realName.as("realName"),
                        qMember.email, qMember.mobilePhone.as("mobilePhone"), qMemberWallet.balance, qMemberWallet.address, qMemberWallet.coin.unit
                        , qMemberWallet.frozenBalance.as("frozenBalance"), qMemberWallet.balance.add(qMemberWallet.frozenBalance).as("allBalance"))).from(QMember.member, QMemberWallet.memberWallet).where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        List<MemberWalletDTO> content = query.offset((pageModel.getPageNo() - 1) * pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        long total = query.fetchCount();
        return new PageImpl<>(content, pageModel.getPageable(), total);
    }

    public BigDecimal getAllBalance(String coinName) {
        return memberWalletDao.getWalletAllBalance(coinName);
    }

    public MemberDeposit findDeposit(String address, String txid, String unit, BigDecimal amount) {
        return depositDao.findByAddressAndTxidAndUnitAndAmount(address, txid, unit, amount);
    }

    /**
     * 减少钱包余额
     *
     * @param walletId
     * @param amount
     * @return true
     * @author fumy
     * @time 2018.07.09 9:07
     */
    //add by tansitao 时间： 2018/6/26 原因：减少余额
    public MessageResult decreaseBalance(Long walletId, BigDecimal amount) {
        int ret = memberWalletDao.decreaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("减少钱包余额失败");
        }
    }


    //add by yangch 时间： 2018.05.11 原因：代码合并
    public MessageResult decreaseFrozen(Long walletId, BigDecimal amount) {
        int ret = memberWalletDao.decreaseFrozen(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("减少冻结余额失败");
        }
    }

    /**
     * 增加钱包余额
     *
     * @param walletId
     * @param amount
     * @return true
     * @author fumy
     * @time 2018.07.09 9:06
     */
    //add by yangch 时间： 2018.05.11 原因：代码合并
    public MessageResult increaseBalance(Long walletId, BigDecimal amount) {
        int ret = memberWalletDao.increaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("增加钱包余额失败");
        }
    }

    //add by yangch 时间： 2018.06.12 原因：新增锁仓余额的添加接口
    public MessageResult increaseLockBalance(Long walletId, BigDecimal amount) {
        int ret = memberWalletDao.increaseLockBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("锁仓余额添加失败");
        }
    }

    //add by tansitao 时间： 2018/7/4 原因：减少锁仓余额
    public MessageResult decreaseLockBalance(Long walletId, BigDecimal amount) {
        int ret = memberWalletDao.decreaseLockFrozen(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("锁仓余额减少失败");
        }
    }

    /***
      * 创建钱包账户
     *
      * @author yangch
      * @time 2018.06.08 15:29 
     * @param memberId 会员ID
      * @param coin 币种
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public MemberWallet createMemberWallet(long memberId, Coin coin) {
        MemberWallet memberWallet = new MemberWallet();
        memberWallet.setCoin(coin);
        memberWallet.setMemberId(memberId);
        memberWallet.setBalance(new BigDecimal(0));
        memberWallet.setFrozenBalance(new BigDecimal(0));
        memberWallet.setLockBalance(new BigDecimal(0));
        //memberWallet.setAddress("");
        memberWallet.setIsLock(BooleanEnum.IS_FALSE);

        return memberWalletDao.save(memberWallet);
    }

    @ReadDataSource
    public List<MemberWalletBalanceVO> findByMemberWalletAllForOut(Map<String, Object> map) {
        List<MemberWalletBalanceVO> list = memberWalletMapper.findAllBy(map);
        return list;
    }

    /**
     * 查询用户钱包币种
     * @param
     * @return
     */
    public List<MemberWallet> findMemberWalletByMemberId(long memberId) {
        List<MemberWallet> memberWallets = memberWalletMapper.selectWallerListByMemberId(memberId);
        return memberWallets;
    }



    public MemberWalletService getService(){
        return SpringContextUtil.getBean(MemberWalletService.class);
    }
}
