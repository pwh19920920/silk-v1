package com.spark.bitrade.service;

import com.spark.bitrade.entity.Member;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-30
 */
public interface IMemberService extends IService<Member> {

    boolean isExist(Long memberId);

}
