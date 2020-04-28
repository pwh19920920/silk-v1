package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年03月30日
 */
@Entity
@Data
public class VoteDetail {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long userId;

    @JoinColumn(name = "vote_id")
    @ManyToOne
    private Vote vote;

    @JoinColumn(name = "pre_coin_id")
    @ManyToOne
    private PreCoin preCoin;

    /** 花费的平台币数目 */
    private BigDecimal amount;

    /** 票数 */
    private int voteAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @CreationTimestamp
    private Date createTime;
}
