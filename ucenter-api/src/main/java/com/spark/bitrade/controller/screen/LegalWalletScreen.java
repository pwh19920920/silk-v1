package com.spark.bitrade.controller.screen;

import com.spark.bitrade.constant.LegalWalletState;
import lombok.Data;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/217:44
 */
@Data
public class LegalWalletScreen {
    private LegalWalletState state;
    private String coinName;
}
