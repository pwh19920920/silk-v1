package com.spark.bitrade.controller.pay;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.ContractEnum;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.core.PageData;

import com.spark.bitrade.entity.SilkTraderContract;
import com.spark.bitrade.entity.SilkTraderContractDetail;
import com.spark.bitrade.service.SilkTraderPayService;
import com.spark.bitrade.util.BindingResultUtil;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.util.Assert.*;

/**第三方支付商家签约信息配置
 * @author Zhang Yanjun
 * @time 2018.07.31 10:53
 */
@RestController
@RequestMapping("pay/contract")
@Slf4j
public class SilkTraderPayManageController extends BaseController{

    @Autowired
    SilkTraderPayService silkTraderPayService;

    /*
    * 分页查询
    * @author Zhang Yanjun
    * @time 2018.07.31 14:19
    * @param pageNo
    * @param pageSize
    * @return
    * @param pageNo
    * @param pageSize
    */
    @PostMapping("page-query")
    public MessageResult contractPage(int pageNo, int pageSize){
        PageInfo<SilkTraderContract> pageInfo=silkTraderPayService.findAll(pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /*
    * 添加商家签约信息
    * @author Zhang Yanjun
    * @time 2018.07.31 17:00
     * @param silkTraderContract
    * @return  * @param silkTraderContract
    */
    @PostMapping("create")
    public MessageResult create(@Valid SilkTraderContract silkTraderContract,BindingResult bindingResult){
        isNull(silkTraderContract.getId(),"validate silkTraderContract.id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        if (silkTraderPayService.create(silkTraderContract)==0){
            return error("未添加成功");
        }else
            return success();
    }
    /*
    * 商家签约信息详情
    * @author Zhang Yanjun
    * @time 2018.08.01 17:32
     * @param id
    * @return  * @param id
    */
    @PostMapping("detail")
    public MessageResult detail(@RequestParam("id") Long id){
        notNull(id, "validate id!");
        return success(silkTraderPayService.findOne(id));
    }
    /*
    * 删除商家签约信息
    * @author Zhang Yanjun
    * @time 2018.08.01 17:32
     * @param ids
    * @return  * @param ids
    */
    @PostMapping("deletes")
    public MessageResult delete(@RequestParam(value = "ids") Long[] ids){
        int column=silkTraderPayService.deletes(ids);
        if (column==0){
            return error("删除失败");
        }
        return success();
    }

    /*
    *  修改商家签约信息
    * @author Zhang Yanjun
    * @time 2018.08.01 17:32
     * @param silkTraderContract
    * @return  * @param silkTraderContract
    */
    @PostMapping("update")
    public MessageResult update(@Valid SilkTraderContract silkTraderContract,BindingResult bindingResult){
        notNull(silkTraderContract.getId(), "validate silkTraderContract.id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        SilkTraderContract s=silkTraderPayService.findOne(silkTraderContract.getId());
        Assert.isTrue(s.getStatus() != ContractEnum.IS_TRUE,"启用中的商家签约信息不允许修改");
        silkTraderPayService.update(silkTraderContract);
        return success();
    }

    /*
    * 修改启用状态
    * @author Zhang Yanjun
    * @time 2018.08.01 17:54
     * @param id
    * @return  * @param status
     * @param id
    */
    @PostMapping("update/status")
    public MessageResult updateStatus(Long id){
        int status=silkTraderPayService.findStatusById(id)==0?1:0;
        silkTraderPayService.updateStatus(status,id);
        return success();
    }

    /*
    * 查看某商家签约币种详情分页
    * @author Zhang Yanjun
    * @time 2018.08.05 15:19
     * @param pageNo
     * @param pageSize
     * @param contractNo
    * @return  * @param pageNo
     * @param pageSize
     * @param contractNo
    */
    @PostMapping("detail/page-query")
    public MessageResult contractDetailPage(int pageNo, int pageSize,String contractNo){
        PageInfo<SilkTraderContractDetail> pageInfo=silkTraderPayService.findDetailAll(pageNo,pageSize,contractNo);
        return success(PageData.toPageData(pageInfo));
    }

    /*
       * 添加商家签约币种详情
       * @author Zhang Yanjun
       * @time 2018.08.02 11:23
        * @param silkTraderContractDetail
       * @return  * @param silkTraderContractDetail
       */
    @PostMapping("detail/create")
    public MessageResult createDetail(@Valid SilkTraderContractDetail silkTraderContractDetail,BindingResult bindingResult){
        isNull(silkTraderContractDetail.getId(), "validate silkTraderContractDetail.id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        silkTraderPayService.createDetail(silkTraderContractDetail);
        return success();
    }
    /*
    * 删除商家签约币种详情
    * @author Zhang Yanjun
    * @time 2018.08.02 15:39
     * @param ids
    * @return  * @param ids
    */
    @PostMapping("detail/deletes")
    public MessageResult deleteDetail(@RequestParam(value = "ids") Long[] ids){
        int row=silkTraderPayService.deletesDetail(ids);
        if (row==0){
            return error("删除失败");
        }
        return success();
    }

    /*
    *  修改商家签约币种详情
    * @author Zhang Yanjun
    * @time 2018.08.01 17:32
     * @param silkTraderContract
    * @return  * @param silkTraderContract
    */
    @PostMapping("detail/update")
    public MessageResult updateDetail(@Valid SilkTraderContractDetail silkTraderContractDetail,BindingResult bindingResult){
        notNull(silkTraderContractDetail.getId(), "validate silkTraderContractDetail.id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        if(silkTraderPayService.updateDetail(silkTraderContractDetail)==0){
            return error("修改失败");
        }else
            return success();
    }

    /*
    * 查询某个币种详情
    * @author Zhang Yanjun
    * @time 2018.08.05 15:18
     * @param id
    * @return  * @param id
    */
    @PostMapping("detail/findOne")
    public MessageResult findDetailOne(long id){
        SilkTraderContractDetail silkTraderContractDetail=silkTraderPayService.findOneDetail(id);
        return success(silkTraderContractDetail);
    }

}
