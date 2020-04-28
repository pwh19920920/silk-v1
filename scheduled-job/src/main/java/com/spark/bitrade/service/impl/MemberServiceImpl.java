package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.mapper.dao.MemberMapper;
import com.spark.bitrade.service.IMemberService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-30
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements IMemberService {

    @Override
    public boolean isExist(Long memberId) {
        EntityWrapper<Member> w=new EntityWrapper<>();
        w.setSqlSelect("id").eq("id",memberId);
        Member member = this.selectOne(w);
        return member!=null;
    }
}
