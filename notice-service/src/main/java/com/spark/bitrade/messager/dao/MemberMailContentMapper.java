package com.spark.bitrade.messager.dao;

import com.spark.bitrade.messager.model.MemberMailContent;
import org.apache.ibatis.annotations.*;


@Mapper
public interface MemberMailContentMapper {

    @Select("select * from member_mail_content where id = #{id}")
    public MemberMailContent getMailContentById(Long id);




    @Insert("insert into member_mail_content(content) values(#{content})")
    @Options(useGeneratedKeys = true)
    //@SelectKey(statement = "SELECT @@IDENTITY", keyProperty = "id", before = false, resultType = MemberMailContent.class)
    public int insert(MemberMailContent memberMailContent);


   // @Select("select * from otc_user_mail where id>${start} and ( from_member_id = ${memberId}  or from_member_id = 0 or to_member_id = ${memberId} or to_member_id=0 )order by id limit ${size}")
    //public List<OtcUserMailContent> getMailsByMemberId(long memberId, int size, long start);

    //int insert(OtcUserMail record);

    //int insertSelective(OtcUserMail record);

    //OtcUserMail selectByPrimaryKey(Long id);

}
