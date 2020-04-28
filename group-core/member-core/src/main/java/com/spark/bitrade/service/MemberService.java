package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.RealNameStatus;
import com.spark.bitrade.dao.MemberDao;
import com.spark.bitrade.dao.MemberDiscountRuleDao;
import com.spark.bitrade.dao.MemberPaymentAccountDao;
import com.spark.bitrade.dto.MemberInfoDTO;
import com.spark.bitrade.dto.MemberSecurityInfoDto;
import com.spark.bitrade.dto.PromotionMemberDTO;
import com.spark.bitrade.entity.ExchangeMemberDiscountRule;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.QMember;
import com.spark.bitrade.exception.AuthenticationException;
import com.spark.bitrade.mapper.dao.DeakingMemberMapper;
import com.spark.bitrade.mapper.dao.MemberMabatisMapper;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vo.MemberVO;
import com.spark.bitrade.vo.PromotionMemberVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;

@Slf4j
@Service
public class MemberService extends BaseService {

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private DeakingMemberMapper deakingMemberMapper;

    @Autowired
    private MemberMabatisMapper mapper;

    @Autowired
    MemberDiscountRuleDao discountRuleDao;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private MemberPaymentAccountDao memberPaymentAccountDao;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<Member> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        List<Member> list;
        JPAQuery<Member> jpaQuery = queryFactory.selectFrom(QMember.member)
                .where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        jpaQuery.orderBy(QMember.member.id.desc());
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    @CacheEvict(cacheNames = "member", key = "'entity:member:'+#member.id")
    public Member save(Member member) {
        getService().cacheEvict(member);
        return memberDao.save(member);
    }

    @CacheEvict(cacheNames = "member", key = "'entity:member:uid-'+#member.id")
    public void cacheEvict(Member member) {
    }

    public MemberService getService() {
        return SpringContextUtil.getBean(MemberService.class);
    }

    /***
      * 更新token
     *
      * @author yangch
      * @time 2018.07.13 11:05 
     * @param member
     */
    @CacheEvict(cacheNames = "member", key = "'entity:member:'+#member.token")
    @Transactional(rollbackFor = Exception.class)
    public int updateToken(Member member) {
        return memberDao.updateToken(member.getId(), member.getToken(), member.getTokenExpireTime());
    }


    @Cacheable(cacheNames = "member", key = "'entity:member:'+#token") //防止频繁的调用（无效果？？？） todo 缓存无效果？？？
    @Transactional(rollbackFor = Exception.class)
    public Member loginWithToken(String token, String ip, String device) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        Member mr = null;
        if (StringUtils.isNotBlank(token) && Pattern.matches(".+\\..+\\..+", token)) { // token 为jwt时
            try {
                // MemberClaim claim = HttpJwtToken.getInstance().verifyToken(token);
                // add wsy, time: 2019-4-19 21:26:46, note: pom 引入 session-wapper 可能会出现嵌套依赖问题，因此使用反射调用
                Class<?> clazz = Class.forName("com.spark.bitrade.ext.HttpJwtToken");
                Object httpJwtToken = clazz.getDeclaredMethod("getInstance").invoke(null);
                Object claim = clazz.getDeclaredMethod("verifyToken", String.class).invoke(httpJwtToken, token);
                if (claim != null) {
                    Long userId = (Long) claim.getClass().getDeclaredField("userId").get(claim);
                    String username = (String) claim.getClass().getDeclaredField("username").get(claim);
                    mr = loginWithUserId(userId, username, ip);
                }
            } catch (Exception e) {
                log.error("jwt token decode exception", e);
            }
        } else {
//        Member mr = memberDao.findMemberByTokenAndTokenExpireTimeAfter(token,new Date());
            mr = memberDao.findMemberByToken(token);
            //add by tansitao 时间： 2018/7/27 原因：增加对token时间的判断
            if (mr != null && mr.getTokenExpireTime() != null) {
                if (mr.getTokenExpireTime().compareTo(new Date()) < 0) {
                    return null;
                }
            }
        }
        return mr;
    }

    public Member loginWithUserId(Long userId, String username, String ip) {
        if (userId == null || StringUtils.isEmpty(username)) {
            return null;
        } else {
            return memberDao.findMemberByIdAndUsernameAndStatus(userId, username, CommonStatus.NORMAL);
        }
    }

    public Member login(String username, String password) throws Exception {
        Member member = memberDao.findMemberByMobilePhoneOrEmail(username, username);
        //edit by yangch 时间： 2018.04.20 原因：修改加密规则
        if (member == null) {
            throw new AuthenticationException("LOGIN_FALSE");
        }
        String userPassWord = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();
        if (!userPassWord.equals(member.getPassword())) {
            throw new AuthenticationException("LOGIN_FALSE");
        } else if (member.getStatus().equals(CommonStatus.ILLEGAL)) {
            throw new AuthenticationException("ACCOUNT_DISABLE");
        }
        return member;
    }

    @Cacheable(cacheNames = "member", key = "'entity:member:phone-'+#phone+'|email-'+#email")
    public Member findMemberByMobilePhoneOrEmail(String phone, String email) {
        return memberDao.findMemberByMobilePhoneOrEmail(phone, email);
    }

    /**
     *  * 根据手机号和邮箱查询用户
     *  * @author yangch
     *  * @time 2019.03.01 16:47 
     *
     * @param accountId 帐号ID，如手机号、邮箱
     *                   
     */
    public Member findMemberByMobilePhoneOrEmail(String accountId) {
        return memberDao.findMemberByMobilePhoneOrEmail(accountId, accountId);
    }

    /**
     * @author rongyu
     * @description
     * @date 2017/12/25 18:42
     */
    @Cacheable(cacheNames = "member", key = "'entity:member:'+#id")
    public Member findOne(Long id) {
        return memberDao.findOne(id);
    }

    /**
     * 根据会员ID，获取会员详情，不写入缓存
     *
     * @param id
     * @return
     */
    public Member findMemberId(Long id) {
        return memberDao.findOne(id);
    }

    public Member findOneById(Long id) {
        return memberDao.findOne(id);
    }

    /**
     *  * 判断是否为迪肯员工
     *  * @author tansitao
     *  * @time 2018/7/2 11:18 
     *  
     */
    @Cacheable(cacheNames = "member", key = "'entity:member:is-'+#id")
    @ReadDataSource
    public boolean isDeakingMember(Long id) {
        boolean isTrue = false;
        if (deakingMemberMapper.findOneBymemberId(id) != null) {
            isTrue = true;
        }
        return isTrue;
    }


    /**
     * @author rongyu
     * @description 查询所有会员
     * @date 2017/12/25 18:43
     */
    public List<Member> findAll() {
        return memberDao.findAll();
    }


    public List<Member> findPromotionMember(Long id) {
        return memberDao.findAllByInviterId(id);
    }

    /**
     * @author rongyu
     * @description 分页
     * @date 2018/1/12 15:35
     */
    public Page<Member> page(Integer pageNo, Integer pageSize, CommonStatus status) {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("id");
        //分页参数
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        //查询条件
        Criteria<Member> specification = new Criteria<Member>();
        specification.add(Restrictions.eq("status", status, false));
        return memberDao.findAll(specification, pageRequest);
    }

    public boolean emailIsExist(String email) {
        List<Member> list = memberDao.getAllByEmailEquals(email);
        return list.size() > 0 ? true : false;
    }

    public boolean usernameIsExist(String username) {
        return memberDao.getAllByUsernameEquals(username).size() > 0 ? true : false;
    }

    public boolean phoneAndUsernameIsExist(String username, String phone, String email) {
        return mapper.findByPhoneAndUserNameAndEmail(username, phone, email) != null ? true : false;
    }

    public boolean emailAndUsernameIsExist(String username, String phone, String email) {
        return mapper.findByPhoneAndUserNameAndEmail(username, phone, email) != null ? true : false;
    }

    public boolean phoneIsExist(String phone) {
        return memberDao.getAllByMobilePhoneEquals(phone).size() > 0 ? true : false;
    }

    public Member findByUsername(String username) {
        return memberDao.findByUsername(username);
    }

    public Member findByEmail(String email) {
        return memberDao.findMemberByEmail(email);
    }

    public Member findByPhone(String phone) {
        return memberDao.findMemberByMobilePhone(phone);
    }

    public Page<Member> findAll(Predicate predicate, Pageable pageable) {
        return memberDao.findAll(predicate, pageable);
    }


    /**
     * 根据身份证号码查询所有用户
     *
     * @param idNumber
     * @return true
     * @author shenzucai
     * @time 2018.04.24 18:55
     */
    public List<Member> findAllByIdNumber(String idNumber) {
        return memberDao.findAllByIdNumber(idNumber);
    }

    /**
     * 根据身份证号码、审核状态（待审核、审核通过），获取会员总条数
     *
     * @param idNumber 身份证号码
     * @return 会员总条数
     * @author zhongxj
     * @date 2019.08.02
     * @desc true不通过，false通过
     */
    public int countMemberByIdNumberAndRealNameStatus(String idNumber) {
        return memberDao.countMemberByIdNumberAndRealNameStatus(idNumber);
    }

    /**
     * @param areaId
     * @return true
     * @author shenzucai
     * @time 2018.05.30 11:53
     */
    public List<Member> findAllByAreaId(String areaId) {
        return memberDao.findAllByAreaId(areaId);
    }

    @ReadDataSource
    public Member findByPromotionCode(String promotionCode) {
        return this.mapper.findByPromotionCode(promotionCode);
    }

    /**
     * 更新交易次数
     *
     * @param uid 用户ID
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "member", key = "'entity:member:'+#uid") //add by tansitao 时间： 2018/11/23 原因：清空member缓存
    public int updateTransactionsTime(long uid) {
        return memberDao.updateTransactionsTime(uid);
    }


    /**
     * 分页查询推荐会员信息
     *
     * @param account
     * @param pageNo
     * @param pageSize
     * @return true
     * @author fumy
     * @time 2018.08.14 17:12
     */
    @ReadDataSource
    public PageInfo<PromotionMemberVO> findByPage(String account, int pageNo, int pageSize) {
        com.github.pagehelper.Page<PromotionMemberVO> page = PageHelper.startPage(pageNo, pageSize);
        List<PromotionMemberVO> list = this.mapper.findBy(account);
        if (list != null && list.size() != 0) {
            //获取会员邀请统计人数
            List<Map<String, Object>> items = mapper.countSingleTotal(list);
            Map<Long, Integer> map = new HashMap<>();

            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    //获取邀请Id已经 对应的人数 放在map
                    Long var1 = (Long) items.get(i).get("inviter_id");
                    Long var2 = (Long) items.get(i).get("inviter_count");
                    map.put(var1, Integer.parseInt(var2 + ""));
                }
            }
            //迭代之前的数据，跟存入的map比对存入响应的人数
            list.forEach(item -> {
                map.forEach((k, v) -> {
                    if (k.equals(item.getId())) {
                        item.setPromotionNum(v);
                    }
                });
            });
        }
        return page.toPageInfo();
    }

    /**
     * 推荐会员信息导出
     *
     * @param username
     * @return true
     * @author fumy
     * @time 2018.08.14 17:12
     */
    @ReadDataSource
    public List<PromotionMemberVO> findByPromotionMemberAllForOut(String username) {
        //获取会员信息
        List<PromotionMemberVO> list = mapper.findBy(username);
        if (list != null && list.size() != 0) {
            //获取会员邀请统计人数
            List<Map<String, Object>> items = mapper.countSingleTotal(list);
            Map<Long, Integer> map = new HashMap<>();

            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    //获取邀请Id已经 对应的人数 放在map
                    Long var1 = (Long) items.get(i).get("inviter_id");
                    Long var2 = (Long) items.get(i).get("inviter_count");
                    map.put(var1, Integer.parseInt(var2 + ""));
                }
            }
            //迭代之前的数据，跟存入的map比对存入响应的人数
            list.forEach(item -> {
                map.forEach((k, v) -> {
                    if (k.equals(item.getId())) {
                        item.setPromotionNum(v);
                    }
                });
            });
        }
        return list;
    }


    public MemberInfoDTO findByMemberInfoId(Long userId) {
        return mapper.findByMemberInfoId(userId);
    }

    /**
     *  * 分页查询推荐会员
     *  * @author tansitao
     *  * @time 2018/8/18 14:40 
     *  
     */
    @ReadDataSource
    public PageInfo<PromotionMemberDTO> pageGetPromotionMember(long memberId, int pageNum, int pageSize) {
        com.github.pagehelper.Page<PromotionMemberDTO> page = PageHelper.startPage(pageNum, pageSize);
        mapper.findPromotionMemberList(memberId);
        return page.toPageInfo();
    }

    //会员导出
    public List<MemberVO> findByMemberAllForOut(Integer commonStatus, String account) {
        List<MemberVO> list = mapper.findAllBy(commonStatus, account);
        for (int i = 0; i < list.size(); i++) {
            //edit by zyj :修改日期显示毫秒的问题，会员状态、等级显示为中文
            String registrationTime = list.get(i).getRegistrationTime() == null ? "" : list.get(i).getRegistrationTime();
            list.get(i).setRegistrationTime(registrationTime.substring(0, registrationTime.length() - 2 > 0 ? registrationTime.length() - 2 : 0));
            list.get(i).setTs(list.get(i).getTransactionStatus().getNameCn() == "是" ? "正常" : "禁用");
            list.get(i).setCs(list.get(i).getStatus().getCnName() == "正常" ? "正常" : "禁用");
            list.get(i).setMl(list.get(i).getMemberLevel().getCnName());
        }
        return list;
    }

    /**
     * 根据条件分页查询会员的优惠规则
     *
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return true
     * @author fumy
     * @time 2018.08.29 17:20
     */
    public PageInfo<ExchangeMemberDiscountRule> discountRulePageInfo(Long memberId, String symbol, int pageNo, int pageSize) {
        com.github.pagehelper.Page<ExchangeMemberDiscountRule> page = PageHelper.startPage(pageNo, pageSize);
        mapper.findMemberDiscountRule(memberId, symbol);
        return page.toPageInfo();
    }

    /**
     * 添加会员优惠规则
     *
     * @param rule
     * @return true
     * @author fumy
     * @time 2018.08.30 14:01
     */
    public ExchangeMemberDiscountRule addDiscountRule(ExchangeMemberDiscountRule rule) {
        return discountRuleDao.save(rule);
//        int row = mapper.insertDiscountRule(params);
//        return row > 0 ? true : false;
    }

    /**
     * 修改会员优惠规则
     *
     * @param rule
     * @return true
     * @author fumy
     * @time 2018.08.30 14:38
     */
//    @WriteDataSource
    public ExchangeMemberDiscountRule updateDiscountRule(ExchangeMemberDiscountRule rule) {
        return discountRuleDao.save(rule);
//        int row = mapper.updateDiscountRule(params);
//        return row > 0 ? true : false;
    }


    /**
     * 查询会员是否可以添加某个币种优惠规则
     *
     * @param memberId
     * @param symbol
     * @return true
     * @author fumy
     * @time 2018.08.30 16:39
     */
    @ReadDataSource
    public boolean countByMemberIdAndSymbol(Long memberId, String symbol) {
        int count = mapper.countByMemberIdAndSymbol(memberId, symbol);
        return count > 0 ? false : true;
    }

    /**
     * 获取用户安全设置信息
     *
     * @param memberId
     * @return true
     * @author fumy
     * @time 2018.11.01 14:19
     */
    @ReadDataSource
    @Cacheable(cacheNames = "member", key = "'entity:memberSecurityInfo:'+#memberId")
    public MemberSecurityInfoDto findSecurityInfoByMemberId(Long memberId) {
        return mapper.findSecurityInfo(memberId);
    }

    /**
     *  * 获取用户的一级推荐人数
     *  * @author tansitao
     *  * @time 2018/12/17 10:51 
     *  
     */
    public int getOneInviteeNumd(Long memberId) {
        return mapper.countOneInviteeByMemberId(memberId);
    }

    /**
     *     * 核查推荐码是否正确
     *  
     */
    public boolean checkPromotion(String promotionCode) {
        Member memberPromotion = this.findByPromotionCode(promotionCode);
        if (memberPromotion == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 实名认证检查
     *
     * @param member
     */
    public void checkRealName(Member member) {
        isTrue(member.getRealNameStatus() != null && member.getRealNameStatus() == RealNameStatus.VERIFIED
                , msService.getMessage("NO_REAL_NAME"));
    }


    /**
     * 更新密码
     *
     * @param memberId
     * @param password
     */
    @CacheEvict(cacheNames = "member", key = "'entity:member:'+#memberId")
    public int updateMemberPassword(Long memberId, String password) {
        return memberDao.updateMemberPassword(memberId, password);
    }

    /**
     * 更新用户状态
     *
     * @param memberId
     * @param status
     */
    @CacheEvict(cacheNames = "member", key = "'entity:member:'+#memberId")
    public int updateMemberStatus(Long memberId, CommonStatus status) {
        return memberDao.updateMemberStatus(memberId, status);
    }

    /**
     * 更新用户交易状态
     *
     * @param memberId
     * @param status
     */
    @CacheEvict(cacheNames = "member", key = "'entity:member:'+#memberId")
    public int updateMemberTransactionStatus(Long memberId, BooleanEnum status) {
        return memberDao.updateMemberTransactionStatus(memberId, status);
    }

    /**
     * 更新用户发布广告状态
     *
     * @param memberId
     * @param status
     */
    @CacheEvict(cacheNames = "member", key = "'entity:member:'+#memberId")
    public int updateMemberPublishStatus(Long memberId, BooleanEnum status) {
        return memberDao.updateMemberPublishStatus(memberId, status);
    }

    @CacheEvict(cacheNames = "member", key = "'entity:member:'+#memberId")
    @Transactional
    public boolean unBindPay(Long memberId,Integer unBindType){
        boolean flag=true;
        switch (unBindType){
            case 1:
                //解绑支付宝
                int i1 = memberDao.deleteMemberAli(memberId);
                flag=i1>0;
                break;
            case 2:
                //解绑微信
                int i = memberDao.deleteMemberWeChat(memberId);
                flag=i>0;
                break;
            case 3:
                //解绑epay
                int i2 = memberPaymentAccountDao.deleteEpayByMemberId(memberId);
                flag=i2>0;
                getService().epayCacheClear(memberId);
                break;
            default:
                break;
        }
        return flag;
    }
    @CacheEvict(cacheNames = "member", key = "'entity:memberPaymentAccount:'+#memberId")
    public void epayCacheClear(Long memberId){

    }
}
