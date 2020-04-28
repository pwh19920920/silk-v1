package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.entity.SilkTraderContract;
import com.spark.bitrade.entity.SilkTraderContractDetail;
import com.spark.bitrade.mapper.dao.SilkTraderPayMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**第三方支付service
 * @author Zhang Yanjun
 * @time 2018.07.31 16:08
 */
@Service
public class SilkTraderPayService {

    @Autowired
    SilkTraderPayMapper silkTraderPayMapper;
    //商家签约信息分页
    @ReadDataSource
    public PageInfo<SilkTraderContract> findAll(int pageNo, int pageSize){
        com.github.pagehelper.Page<SilkTraderContract> page= PageHelper.startPage(pageNo,pageSize);
        this.silkTraderPayMapper.findAll();
        return page.toPageInfo();
    }

    //商家签约信息添加
    public int create(SilkTraderContract silkTraderContract){
       return silkTraderPayMapper.create(silkTraderContract);
    }

    //商家签约信息详情
    public SilkTraderContract findOne(Long id){
        return silkTraderPayMapper.findOne(id);
    }

    //商家签约信息删除
    public int deletes(Long[] ids){
      int row= silkTraderPayMapper.deletes(ids);
        return row;
    }
    //商家签约信息修改
    public void update(SilkTraderContract silkTraderContract){
        silkTraderPayMapper.update(silkTraderContract);
    }

    //查询商家签约状态
    public int findStatusById(long id){
        return silkTraderPayMapper.findStatusById(id);
    }
    //商家签约修改启用状态
    public void updateStatus(long status, long id){
        silkTraderPayMapper.updateStatus(status,id);
    }

    //某商家签约币种详情分页
    @ReadDataSource
    public PageInfo<SilkTraderContractDetail> findDetailAll(int pageNo, int pageSize,String contractNo){
        com.github.pagehelper.Page<SilkTraderContractDetail> page= PageHelper.startPage(pageNo,pageSize);
        this.silkTraderPayMapper.findDetailAll(contractNo);
        return page.toPageInfo();
    }

    //商家签约信息详情
    public SilkTraderContractDetail findOneDetail(Long id){
        return silkTraderPayMapper.findDetailOne(id);
    }

    //商家签约币种详情添加
    public void createDetail(SilkTraderContractDetail silkTraderContractDetail){
        silkTraderPayMapper.createDetail(silkTraderContractDetail);
    }

    //商家签约币种详情删除
    public int deletesDetail(Long[] ids){
       int row=silkTraderPayMapper.deletesDetail(ids);
        return row;
    }

    //商家签约币种详情修改
    public int  updateDetail(SilkTraderContractDetail silkTraderContractDetail){
       return silkTraderPayMapper.updateDetail(silkTraderContractDetail);
    }

}
