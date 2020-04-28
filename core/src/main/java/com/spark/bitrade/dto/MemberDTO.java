package com.spark.bitrade.dto;

import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberSecuritySet;
import com.spark.bitrade.entity.MemberWallet;
import lombok.Data;

import java.util.List;

@Data
public class MemberDTO {

    private Member member ;

    private List<MemberWallet> list ;

    private MemberSecuritySet memberSecuritySet;

}
