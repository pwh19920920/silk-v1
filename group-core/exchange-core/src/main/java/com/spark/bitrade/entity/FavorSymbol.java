package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="exchange_favor_symbol")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavorSymbol {
    @Id
    @GeneratedValue
    private Long id;
    private String symbol;
    private Long memberId;
    private String addTime;
    @Transient
    private CoinThumb coinThumb;
}
