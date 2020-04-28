package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.PayTransferStatus;
import com.spark.bitrade.constant.PayTransferType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包划转记录表
 * （记录钱包和星客账户之间的划拨记录）
 * @author Zhang Yanjun
 * @time 2019.01.09 15:26
 */
@Data
@TableName("pay_wallet_member_transfer_record")
public class PayWalletMemberTransferRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    //用户ID
    private Long memberId;
    //账户id（memberWallet表）
    private Long memberWalletId;
    //转入地址
    private String intoAddress;
    //转入地址标签
    private String intoAddressTag;
    //转出地址
    private String outAddress;
    //转出地址标签
    private String outAddressTag;
    //交易哈希值
    private String txid;
    //划转数量
    private BigDecimal amount;
    //币种
    private String unit;
    //手续费
    private BigDecimal fee;
    //优惠手续费
    private BigDecimal discountsFee;
    //类型（转入、转出）
    private PayTransferType type;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //状态（发起、成功、失败）
    private PayTransferStatus status;
}
