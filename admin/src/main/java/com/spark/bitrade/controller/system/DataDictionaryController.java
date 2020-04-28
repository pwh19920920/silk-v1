package com.spark.bitrade.controller.system;

import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.DataDictionary;
import com.spark.bitrade.model.create.DataDictionaryCreate;
import com.spark.bitrade.model.update.DataDictionaryUpdate;
import com.spark.bitrade.service.DataDictionaryService;
import com.spark.bitrade.util.BindingResultUtil;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:21
 */
@RestController
//edit by yangch 时间： 2018.04.26 原因：更改映射名称
//@RequestMapping("data-dictionary")
@RequestMapping("system/data-dictionary")
public class DataDictionaryController extends BaseAdminController {
    @Autowired
    private DataDictionaryService service;

    @PostMapping
    //edit by yangch 时间： 2018.04.26 原因：方法名 更改 save -> post
    public MessageResult post(@Valid DataDictionaryCreate model, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        //edit by yangch 时间： 2018.04.26 原因：代码合并更新
        /*if (result != null) {
            return result;
        }
        DataDictionary data = service.findByBond(model.getBond());
        if (data != null) return error("bond 已经存在!");
        service.save(new DataDictionary(model.getBond(), model.getValue(), model.getComment()));*/
        if (result != null) return result;
        DataDictionary data = service.findByBond(model.getBond());
        if (data != null) return error("bond already existed!");
        service.save(model);
        return success();
    }

    @GetMapping
    public MessageResult page(PageModel pageModel) { //edit by yangch 时间： 2018.04.26 原因：更改方法名 get -> page
        Page<DataDictionary> all = service.findAll(null, pageModel);
        return success(all);
    }

    @PutMapping("{bond}")
    public MessageResult put(@PathVariable("bond") String bond, DataDictionaryUpdate model) {
        //edit by yangch 时间： 2018.04.26 原因：合并时更新
        /*DataDictionary byKey = service.findByBond(bond);
        Assert.notNull(byKey, "validate bond");
        service.updateByModelUpdate(byKey, model.getValue(), model.getComment());*/
        DataDictionary dataDictionary = service.findByBond(bond);
        Assert.notNull(dataDictionary, "validate bond");
        service.update(model, dataDictionary);
        return success();
    }

}
