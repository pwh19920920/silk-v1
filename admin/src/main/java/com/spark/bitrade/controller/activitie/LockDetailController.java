package com.spark.bitrade.controller.activitie;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.dto.LockAbstractDto;
import com.spark.bitrade.dto.LockTypeDto;
import com.spark.bitrade.service.LockTypeService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.ExcelUtil;
import com.spark.bitrade.util.MessageResult;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * Created by lingxing on 2018/7/12.
 */

@RestController
@RequestMapping("lockDetail")
public class LockDetailController extends BaseAdminController {
    @Autowired
    private LockTypeService lockTypeService;

    /**
     * 锁仓查询
     * @param pageModel
     * @param lockAbstract
     * @return
     */
    @PostMapping("/page-query")
    @RequiresPermissions("lock:page-query")
    public MessageResult page(
            PageModel pageModel
            , LockAbstractDto lockAbstract
    ) {
        PageInfo<LockTypeDto> pageInfo=null;
        if(lockAbstract.getType()==null){
            lockAbstract.setLockTypeStatus("detail");
            pageInfo = lockTypeService.queryPageByLockType(lockAbstract, pageModel.getPageNo(),pageModel.getPageSize());
        }else if(lockAbstract.getType()!= 0 || lockAbstract.getType() !=1){ //edit by tansitao 时间： 2018/11/5 原因：修改查询类型
            pageInfo = lockTypeService.queryPageByLockType(lockAbstract, pageModel.getPageNo(),pageModel.getPageSize());
        }else {
           return error("查询锁仓类型错误");
        }
        return success(pageInfo);
    }

    /**
     * 锁仓内部查询
     * @param pageModel
     * @param lockAbstract
     * @return
     */
    @PostMapping("/internal/page-query")
    @RequiresPermissions("lock:internal-page-query")
    public MessageResult internalPage(
            PageModel pageModel
            , LockAbstractDto lockAbstract
    ) {
        PageInfo<LockTypeDto> pageInfo=null;
        if(lockAbstract.getType()==null){
            lockAbstract.setLockTypeStatus("internalDetail");
            pageInfo = lockTypeService.queryPageByLockType(lockAbstract, pageModel.getPageNo(),pageModel.getPageSize());
        }else if(lockAbstract.getType()==1){
            pageInfo = lockTypeService.queryPageByLockType(lockAbstract, pageModel.getPageNo(),pageModel.getPageSize());
        }else {
           return error("查询锁仓类型错误");
        }
        return success(pageInfo);
    }

    /**
     * 锁仓查询明细
     * @param id
     * @return
     */
    @RequiresPermissions("lock:lock-detail")
    @GetMapping("/detail/{id}")
    public MessageResult detail(
             @PathVariable("id") Long id) {
            return success(lockTypeService.findByLockDetail(id));
    }

    /**
     * 手动锁仓（内部锁仓）查询明细
     * @param id

     * @return
     */
    @GetMapping("/internal/detail/{id}")
    public MessageResult internal(
            @PathVariable("id") Long id) {
        return success(lockTypeService.findByLockInternalDetail(id));
    }

    /**
     * 锁仓明细导出
     * @author Zhang Yanjun
     * @time 2018.08.20 17:21
     *  @param lockAbstract
     * @param response
     * @return
    */
    @RequiresPermissions("lock:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.FINANCE, operation = "导出锁仓明细 Excel")
    public void outExcel(LockAbstractDto lockAbstract,HttpServletResponse response) throws Exception {
        lockAbstract.setLockTypeStatus("detail");
        List<LockTypeDto> list=lockTypeService.findByInternalLockAllForOut(lockAbstract);
        String fileName="lockDetail_"+ DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,LockTypeDto.class.getDeclaredFields(),response,fileName);

    }

    /**
     * 内部锁仓明细导出
     * @author Zhang Yanjun
     * @time 2018.08.20 17:23
     * @param lockAbstract
     * @param response
     * @return
    */
    @GetMapping("/internal/out-excel")
    @RequiresPermissions("lock:internal-out-excel")
    @AccessLog(module = AdminModule.FINANCE, operation = "导出内部锁仓明细 Excel")
    public void outExcelInternal(LockAbstractDto lockAbstract,HttpServletResponse response) throws Exception {
        lockAbstract.setLockTypeStatus("internalDetail");
        List<LockTypeDto> list=lockTypeService.findByInternalLockAllForOut(lockAbstract);
        String fileName="internalLockDetail_"+ DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,LockTypeDto.class.getDeclaredFields(),response,fileName);

    }
}
