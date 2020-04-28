package com.spark.bitrade.model;

import com.spark.bitrade.constant.WithdrawStatus;
import lombok.Data;

@Data
public class LegalWalletWithdrawScreen {
    WithdrawStatus status;
    String username;
    String coinName;

}
