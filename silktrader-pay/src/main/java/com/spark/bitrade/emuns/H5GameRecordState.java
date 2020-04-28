package com.spark.bitrade.emuns;

/**
 * H5GameRecordState
 *
 * @author archx
 * @time 2019/4/25 10:49
 */
public enum H5GameRecordState {

    STARTED(0, "发起"), SUCCESSFUL(1, "成功"), FAILED(2, "失败");

    private final int code;
    private final String description;

    H5GameRecordState(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean eq(int code) {
        return this.code == code;
    }

}
