package com.spark.bitrade.emuns;

/**
 * H5GameDirection
 *
 * @author archx
 * @time 2019/4/25 10:47
 */
public enum H5GameDirection {

    TOP_UP(0, "转入"), WITHDRAW(1, "转出"), REFUND(2, "退款");

    private final int code;
    private final String description;

    H5GameDirection(int code, String description) {
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