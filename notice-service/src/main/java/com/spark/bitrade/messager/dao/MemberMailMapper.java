package com.spark.bitrade.messager.dao;

import com.spark.bitrade.messager.model.MemberMailEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface MemberMailMapper {

    @Select("select * from member_mail where id = #{id}")
    public MemberMailEntity getMailById(Long id);


    @Select("select * from member_mail where id>${start} and ( from_member_id = ${memberId}  or from_member_id = 0 or to_member_id = ${memberId} or to_member_id=0 )order by id limit ${size}")
    public List<MemberMailEntity> getMailsByMemberId(long memberId, int size, long start);

    @Select("<script>" +
            "select * from member_mail where 1=1  " +
            "<if test='start>-1'> and id>#{start} </if>" +
            "<if test='status>-1'>and status=#{status} </if>" +
            "<if test='memberId>0'>and ( from_member_id = #{memberId}  or from_member_id = 0 or to_member_id = #{memberId} or to_member_id=0 ) </if>" +
            " order by id desc limit ${size}" +
            "</script>")
    public List<MemberMailEntity> getMailsByStatusAndMemberId(@Param("memberId") long memberId, @Param("status") int status ,@Param("size") int size, @Param("start") long start);

    @Select("<script>" +
            "select * from member_mail where 1=1  " +
            "<if test='start>-1'> and id>#{start} </if>" +
            "<if test='status>-1'>and status=#{status} </if>" +
            "<if test='memberId>0'>and ( from_member_id = #{memberId}  or from_member_id = 0 or to_member_id = #{memberId} or to_member_id=0 ) </if>" +
            " order by id desc limit ${size}" +
            "</script>")

    public List<MemberMailEntity> getMails(@Param("memberId") long memberId, @Param("status") int status ,@Param("size") int size, @Param("start") long start);


    @Select("select count(*) from member_mail where id>${start} and status=${status} and ( from_member_id = ${memberId}  or from_member_id = 0 or to_member_id = ${memberId} or to_member_id=0 )order by id limit ${size}")
    public int getMailsCountByStatusAndMemberId(long memberId, int status , int size,long start);

    @Insert("insert into member_mail(to_member_id,subject,content_id,create_time) values(#{toMemberId},#{subject},#{contentId},#{createTime})")
    @Options(useGeneratedKeys = true)
    int insert(MemberMailEntity memberMailEntity);

    @Select("select * from member_mail where id = #{id} and ( from_member_id = ${memberId}  or from_member_id = 0 or to_member_id = ${memberId} or to_member_id=0 )")
    MemberMailEntity getMailByIdWithUserId(Long id, long memberId);

    @Update("update member_mail set status=${status} where id = #{id}  and ( from_member_id = ${memberId}  or from_member_id = 0 or to_member_id = ${memberId} or to_member_id=0 )")
    int setStatusWithIdAndMemberId(long id ,long memberId,int status);




    //int insertSelective(OtcUserMail record);

    //OtcUserMail selectByPrimaryKey(Long id);

}
