package com.spark.bitrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties("wallet")
public class WalletConfig {
    private BigDecimal ethNum;


    public BigDecimal getEthNum() {
        return ethNum;
    }

    public void setEthNum(BigDecimal ethNum) {
        this.ethNum = ethNum;
    }
}
