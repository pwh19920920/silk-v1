package com.spark.bitrade.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.ActivityRewardType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.RewardActivitySettingDao;
import com.spark.bitrade.dto.PageParam;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.RewardActivityMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.service.Base.TopBaseService;
import com.spark.bitrade.vo.RewardActivityResp;
import com.spark.bitrade.vo.RewardActivitySettingVO;
import com.spark.bitrade.vo.RewardActivityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Service
public class RewardActivitySettingService extends TopBaseService<RewardActivitySetting,RewardActivitySettingDao> {

    @Autowired
    private RewardActivityMapper rewardActivityMapper;

    @Autowired
    public void setDao(RewardActivitySettingDao dao) {
        this.dao = dao ;
    }


    public RewardActivitySetting findByType(ActivityRewardType type){
        return dao.findByStatusAndType(BooleanEnum.IS_TRUE, type);
    }

    //add by tansitao 时间： 2018/5/11 原因：查找注册活动
    public List<RewardActivitySetting> findListByType(ActivityRewardType type){
        return dao.findAllByStatusAndType(BooleanEnum.IS_TRUE, type);
    }

    public RewardActivitySetting save(RewardActivitySetting rewardActivitySetting){
        return dao.save(rewardActivitySetting);
    }

   /* public List<RewardActivitySetting> page(Predicate predicate){
        Pageable pageable = new PageRequest()
        Iterable<RewardActivitySetting> iterable = rewardActivitySettingDao.findAll(predicate, QRewardActivitySetting.rewardActivitySetting.updateTime.desc());
        return (List<RewardActivitySetting>) iterable ;
    }*/

   //用户奖励分页
    @ReadDataSource
    public PageInfo<RewardActivitySettingVO> findAllPage(int pageNo, int pageSize){
        Page<RewardActivitySettingVO> page= PageHelper.startPage(pageNo, pageSize);
        List<RewardActivitySettingVO> list= this.rewardActivityMapper.findRewardActivity();
        for (RewardActivitySettingVO rewardActivitySettingVO:list){
            JSONObject jsonObject=JSONObject.parseObject(rewardActivitySettingVO.getInfo());
            rewardActivitySettingVO.setAmount(jsonObject.getString("amount"));
            rewardActivitySettingVO.setUpdateTime(rewardActivitySettingVO.getUpdateTime().substring(0,rewardActivitySettingVO.getUpdateTime().length()-2));
        }
        return page.toPageInfo();
    }

    public RewardActivitySetting findOneById(Long id){
        return dao.findOne(id);
    }

    public RewardActivitySetting findOneByType(ActivityRewardType type){
        return dao.findByType(type);
    }


    /**
     * 得到推荐返佣活动的标题列表
     * @author fumy
     * @time 2018.10.16 11:13
     * @param
     * @return true
     */
    @ReadDataSource
    public List<RewardActivityVo> getRewardList(){
        return rewardActivityMapper.getRewardList();
    }

    @ReadDataSource
    public RewardActivityResp getRewardList(Long id, String tName){
        return rewardActivityMapper.getRewardActivityDetail(id,tName);
    }
}
