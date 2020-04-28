package com.spark.bitrade.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2019/1/9.
 */

//@RestController
@Slf4j
@RequestMapping("")
@Controller
public class TestController {

    @RequestMapping(value = "/test/get",method = RequestMethod.GET)
    public String test(){


        return "test";
    }


/*   @GetMapping("{orderId}")
   public String qrpay(@PathVariable(name="orderId") String orderId){

       log.info("orderId={}",orderId);
       return orderId;

   }*/





}
