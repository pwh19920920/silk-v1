package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.LoginType;
import com.spark.bitrade.dao.MemberLoginHistoryDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.MemberLoginHistoryMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.EnumHelperUtil;
import com.spark.bitrade.util.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
  * 会员登录历史service
  * @author tansitao
  * @time 2018/7/10 10:03 
  */
@Service
public class MemberLoginHistoryService extends BaseService {
    @Autowired
    private MemberLoginHistoryDao memberLoginHistoryDao;

    @Autowired
    private GyDmcodeService gyDmcodeService;

    @Autowired
    private MemberLoginHistoryMapper memberLoginHistoryMapper;

    @Value("${login.postion.areay.ip:false}")
    private boolean login_postion_areay_ip; //登录时是否根据IP进行区域定位

    @Async
    public MemberLoginHistory save(Member member, HttpServletRequest request, LoginType type, String thirdMark, BooleanEnum IsRegistrate){
        MemberLoginHistory memberLoginHistory = new MemberLoginHistory();
        //add by tansitao 时间： 2018/12/27 原因：增加设备信息的保存
        String producers = request.getHeader("producers");
        String systemVersion = request.getHeader("systemVersion");
        String model = request.getHeader("model");
        String uuid = request.getHeader("uuid");
        String isRootOrJailbreakStr = request.getHeader("isRootOrJailbreak") ;
        memberLoginHistory.setIsRegistrate(IsRegistrate);
        memberLoginHistory.setProducers(producers);
        memberLoginHistory.setSystemVersion(systemVersion);
        memberLoginHistory.setModel(model);
        memberLoginHistory.setUuid(uuid);
        //判断是否有手机root过的信息
        if(null != isRootOrJailbreakStr && !"".equals(isRootOrJailbreakStr)){
            //如果有信息者将其装换为boolean枚举类型
            BooleanEnum isRootOrJailbreak = EnumHelperUtil.getByIntegerTypeCode(BooleanEnum.class,"getOrdinal",Integer.parseInt(isRootOrJailbreakStr));
            memberLoginHistory.setIsRootOrJailbreak(isRootOrJailbreak);
        }
        //设置其他信息
        memberLoginHistory.setMemberId(member.getId());
        if(login_postion_areay_ip) {
            DimArea dimArea = gyDmcodeService.getPostionInfo(null, null, IpUtils.getIpAddr(request));
            if (dimArea != null) {
                memberLoginHistory.setArea(dimArea.getAreaName());
            }
        }
        memberLoginHistory.setLoginIP(IpUtils.getIp(request));
        memberLoginHistory.setThirdMark(thirdMark);
        memberLoginHistory.setType(type);
        return memberLoginHistoryDao.save(memberLoginHistory);
    }

    /**
      * 通过用户查看登录记录
      * @author tansitao
      * @time 2018/6/22 14:33 
      */
    @ReadDataSource
    public PageInfo<MemberLoginHistory> queryPageByMemberAndActId(long memberId, int pageNum, int pageSize){
        Page<MemberLoginHistory> page = PageHelper.startPage(pageNum, pageSize);
        memberLoginHistoryMapper.findloginHistory(memberId);
        return page.toPageInfo();
    }

}
