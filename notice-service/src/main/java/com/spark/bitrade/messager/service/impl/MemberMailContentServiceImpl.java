package com.spark.bitrade.messager.service.impl;

import com.spark.bitrade.messager.model.MemberMailContent;
import com.spark.bitrade.messager.dao.MemberMailContentMapper;
import com.spark.bitrade.messager.service.IMemberMailContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * @author ww
 * @time 2019.09.17 14:35*/


@Component
public class MemberMailContentServiceImpl  implements IMemberMailContentService {

    @Autowired
    MemberMailContentMapper memberMailContentMapper;

    public MemberMailContent getMailContentById(Long id){
        return memberMailContentMapper.getMailContentById(id);
    }
    public int insert(MemberMailContent memberMailContent){

        int num =  memberMailContentMapper.insert(memberMailContent);;


        return memberMailContentMapper.insert(memberMailContent);
    }
}
