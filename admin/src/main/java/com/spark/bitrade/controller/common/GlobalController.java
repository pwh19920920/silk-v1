package com.spark.bitrade.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
public class GlobalController {

    @ExceptionHandler({Exception.class})
    public String exception(Exception e) {
        e.printStackTrace();
        return "exception";
    }
}
