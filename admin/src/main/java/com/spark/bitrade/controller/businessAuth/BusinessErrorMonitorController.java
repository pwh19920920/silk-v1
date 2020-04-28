package com.spark.bitrade.controller.businessAuth;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.BusinessErrorMonitor;
import com.spark.bitrade.service.BusinessErrorMonitorService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.RedoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**业务异常监控控制器
 * @author Zhang Yanjun
 * @time 2018.07.13 18:10
 */
@Api(description = "业务异常监控",tags={"业务异常监控接口操作"})
@RestController
@RequestMapping("business/error")
@Slf4j
public class BusinessErrorMonitorController extends BaseController {

    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;

    @Autowired
    RestTemplate restTemplate;

    /*
    * 分页（未用）
    * @author Zhang Yanjun
    * @time 2018.07.14 15:03
     * @param pageModel
     * @param screen
    * @return  * @param pageModel
     * @param screen
    */
    @PostMapping("page")
    @AccessLog(module = AdminModule.MEMBER,operation = "分页查找BusinessErrorMonitor")
    public MessageResult pageOld(PageModel pageModel, int type){
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        //条件   0：升序   1：降序
        if(type==1){
            directions.add(0,Sort.Direction.DESC);
            property.add(0,"maintenanceStatus");
        }else{
            directions.add(0,Sort.Direction.ASC);
            property.add(0,"maintenanceStatus");
        }
        directions.add(1,Sort.Direction.DESC);
        property.add(1,"maintenanceTime");
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Page<BusinessErrorMonitor> page=businessErrorMonitorService.findAll(pageModel.getPageable());
        return success(page);
    }


    /**
      * 根据条件分页
      * @author Zhang Yanjun
      * @time 2018.07.24 9:31
      * @param statusSort  对maintenanceStatus（是否处理）进行排序，升序（1）排列   弃用
      * @param type  根据表中type列（业务类型）进行筛选
      * @param maintenanceStatus  根据表中maintenanceStatus列（是否处理）进行筛选
      * @param timeSort 时间排序 0为升序，1为降序
      * @param pageNo
      * @param pageSize
     */
    @RequiresPermissions("system:business-error-monitor-page-query")
    @PostMapping("page-query")
    public MessageResult page(Integer type,Integer maintenanceStatus,String timeSort, int pageNo, int pageSize){
//        if (statusSort==null||"".equals(statusSort)){
//            statusSort="0";
//        }
        if (timeSort==null||"".equals(timeSort)){
            timeSort="1";
        }
        int ts=Integer.parseInt(timeSort);
        if (ts>1){
            return error("值为0或1");
        }
        PageInfo<BusinessErrorMonitor> pageInfo=businessErrorMonitorService.findBy(type,maintenanceStatus,ts,PageData.pageNo4PageHelper(pageNo),pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 重做
     * @author Zhang Yanjun
     * @time 2018.11.16 15:47
     * @param id
     */
    @RequiresPermissions("system:business-error-monitor-redo")
    @ApiOperation(value = "重做",notes = "重做")
    @PostMapping("redo")
    public MessageResult redo(@ApiParam(name ="id",required = true)Long id){
        RedoUtil redoUtil=new RedoUtil();
        MessageResult result= redoUtil.redo(restTemplate,id);
        return result;
    }

    /**
     * 统计未处理条数
     * @author Zhang Yanjun
     * @time 2018.07.21 17:21
     * @param
     */
    @RequiresPermissions("business:error:monitor:count")
    @PostMapping("count")
    public MessageResult count(){
        int count=businessErrorMonitorService.findUnMaintenanceStatus();
        return success(count);
    }

}
