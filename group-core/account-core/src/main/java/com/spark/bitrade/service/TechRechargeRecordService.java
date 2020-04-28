package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.TechRechargeRecordDao;
import com.spark.bitrade.dto.TechRechargeRecordDto;
import com.spark.bitrade.entity.TechRechargeRecord;
import com.spark.bitrade.mapper.dao.TechRechargeRecordMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shenzucai
 * @time 2018.06.20 16:36
 */
@Service
public class TechRechargeRecordService extends BaseService<TechRechargeRecord>{

    @Autowired
    private TechRechargeRecordDao techRechargeRecordDao;
    @Autowired
    private TechRechargeRecordMapper mapper;

    public Page<TechRechargeRecord> findAll(Predicate predicate, Pageable pageable) {
        return techRechargeRecordDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<TechRechargeRecord> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("createTime");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<TechRechargeRecord> geted = techRechargeRecordDao.findAll(predicate, pageModel.getPageable());

        List<TechRechargeRecord> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        return list;
    }
    /**
     * 获取条件查询的所有记录
     * @author lingxing
     * @time 2018.07.18 08:51
     * @return trues
     */
    @ReadDataSource
    public PageInfo<TechRechargeRecord> findByTechRechargeRecord(TechRechargeRecordDto techRechargeRecordDto, PageModel pageModel){
        com.github.pagehelper.Page<TechRechargeRecord> page = PageHelper.startPage(pageModel.getPageNo(), pageModel.getPageSize());
        this. mapper.findByTechRechargeRecord(techRechargeRecordDto);
        return  page.toPageInfo();
    }


    /**
     * 技术充币导出
     * @author Zhang Yanjun
     * @time 2018.10.10 15:41
     * @param techRechargeRecordDto
     */
    public List<TechRechargeRecord> findByTechRechargeRecordOut(TechRechargeRecordDto techRechargeRecordDto){
        List<TechRechargeRecord> list = mapper.findByTechRechargeRecord(techRechargeRecordDto);
        for (int i=0;i<list.size();i++){
            list.get(i).setRechargeTypeOut(list.get(i).getRechargeType()==0?"充":"减");//交易类型
        }
        return list;
    }
}
