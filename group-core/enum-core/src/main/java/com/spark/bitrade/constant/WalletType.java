package com.spark.bitrade.constant;

import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Administrator on 2019/3/8.
 */
@AllArgsConstructor
@Getter
public enum WalletType implements BaseEnum {

    CLOUDWALLET("云端钱包"),BLOCKWALLET("区块链钱包");

    private String cnName;

    @Override
    public int getOrdinal() {
        return this.ordinal();
    }


}
