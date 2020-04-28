package com.spark.bitrade.messager.service;

import com.spark.bitrade.messager.model.MemberMailContent;

/**
 * @author ww
 * @time 2019.09.17 14:34
 */

public interface IMemberMailContentService {

    public MemberMailContent getMailContentById(Long id);

    public int insert(MemberMailContent content);
}
