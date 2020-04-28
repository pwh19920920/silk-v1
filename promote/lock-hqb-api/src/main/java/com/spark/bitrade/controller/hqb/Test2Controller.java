package com.spark.bitrade.controller.hqb;

import com.spark.bitrade.util.MessageResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/***
 * 
 * @author yangch
 * @time 2019-04-23 13:26:51
 */

@RestController
@RequestMapping(value = "/hqb",method = {RequestMethod.GET, RequestMethod.POST})
public class Test2Controller {
    @RequestMapping("/test2")
    public MessageResult test(Long id){

        return MessageResult.success("test2--------");
    }

}
