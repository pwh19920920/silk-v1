package com.spark.bitrade.service;

import com.spark.bitrade.entity.H5GamePayRecord;
import com.spark.bitrade.entity.Member;

import java.math.BigDecimal;

/**
 * IH5GameService
 *
 * @author archx
 * @time 2019/4/25 10:35
 */
public interface IH5GameService {

    /**
     * 转入
     *
     * @param member 会员ID
     * @param mobile 推荐码
     * @param amount 数额
     * @return resp
     */
    Resp topUp(Member member, String mobile, BigDecimal amount);

    /**
     * 提现
     *
     * @param member 会员
     * @param mobile 推荐码
     * @param amount 数额
     * @return resp
     */
    Resp withdraw(Member member, String mobile, BigDecimal amount);

    /**
     * 退款
     *
     * @param member 会员
     * @param refId  转账记录Id
     * @return resp
     */
    Resp refund(Member member, Long refId);

    /**
     * 更新
     *
     * @param record 记录
     */
    void update(H5GamePayRecord record);

    /**
     * 响应体
     */
    class Resp {
        private int code;
        private String message;
        private H5GamePayRecord record;


        private Resp(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public static Resp Ok(H5GamePayRecord record) {
            Resp resp = new Resp(0, "ok");
            resp.record = record;
            return resp;
        }

        public static Resp Fail(int code, String msg) {
            return new Resp(code, msg);
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return code == 0;
        }

        public H5GamePayRecord getRecord() {
            return record;
        }
    }

}
