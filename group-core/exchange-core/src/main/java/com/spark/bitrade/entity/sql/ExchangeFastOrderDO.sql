-- auto Generated on 2019-03-27 11:22:29 
-- DROP TABLE IF EXISTS `exchange_fast_order`;
CREATE TABLE exchange_fast_order(
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `member_id` BIGINT NOT NULL DEFAULT 1 COMMENT '总账户ID',
    `base_symbol` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '闪兑基币币种名称,如CNYT、BT',
    `coin_symbol` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '闪兑币种名称，如BTC、LTC',
    `amount` DECIMAL(24,8) NOT NULL DEFAULT 0 COMMENT '闪兑数量，由闪兑用户输入的数量',
    `traded_amount` DECIMAL(24,8) NOT NULL DEFAULT 0 COMMENT '成交数量，根据闪兑规则计算得到的成交数量',
    `direction` INTEGER(12) NOT NULL DEFAULT 0 COMMENT '订单方向:买入/卖出',
    `adjust_rate` DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT '闪兑调整比例，取值[0-1]，冗余数据，记录成交时基于实时汇率价格的调整比例',
    `current_price` DECIMAL(24,8) NOT NULL DEFAULT 0 COMMENT '实时汇率价（即实时价格），冗余数据，记录成交当时的实时汇率',
    `traded_price` DECIMAL(24,8) NOT NULL DEFAULT 0 COMMENT '成交价，根据实时汇率、闪兑浮动比例以及方向计算出来的成交价',
    `initiator_status` INTEGER(12) NOT NULL DEFAULT 0 COMMENT '兑换发起方处理状态：0=TRADING（交易中）/1=COMPLETED(完成)',
    `receiver_status` INTEGER(12) NOT NULL DEFAULT 0 COMMENT '兑换接收方处理状态：0=TRADING（交易中）/1=COMPLETED(完成)',
    `create_time` BIGINT NOT NULL DEFAULT 1 COMMENT '下单时间',
    `completed_time` BIGINT NOT NULL DEFAULT 1 COMMENT '成交时间',
    `virtual_brokerage_fee` DECIMAL(24,8) NOT NULL DEFAULT 0 COMMENT '虚拟佣金，冗余数据；兑总账户获得的虚拟收益=计算并记录基于实时汇率和调整后的汇率计算出来的虚拟收益。',
    `receive_id` BIGINT NOT NULL DEFAULT 1 COMMENT '兑换接收方用户ID',
    `app_id` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '渠道',
    PRIMARY KEY (`order_id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT '闪兑订单';
