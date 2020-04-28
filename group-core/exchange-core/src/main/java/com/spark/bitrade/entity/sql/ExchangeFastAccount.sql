-- auto Generated on 2019-03-27 11:00:11 
-- DROP TABLE IF EXISTS `exchange_fast_account`; 
CREATE TABLE exchange_fast_account(
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `member_id` BIGINT NOT NULL DEFAULT 0 COMMENT '总账户ID',
    `base_symbol` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '闪兑基币币种名称,如CNYT、BT',
    `coin_symbol` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '闪兑币种名称，如BTC、LTC',
    `buy_adjust_rate` DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT '买入时价格上调比例,取值[0-1]；闪兑用户买入时，基于实时价的上调价格的浮动比例。',
    `sell_adjust_rate` DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT '卖出时价格下调比例，取值[0-1]；闪兑用户买出时，基于实时价的下调价格的浮动比例。',
    `enable` INTEGER(12) NOT NULL DEFAULT 1 COMMENT '启用状态，1=启用/2=禁止',
    `app_id` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '渠道',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT 'exchange_fast_account';

ALTER TABLE exchange_fast_account add create_time datetime(0) NULL DEFAULT NULL COMMENT '创建时间';
ALTER TABLE exchange_fast_account add update_time datetime(0) NULL DEFAULT NULL COMMENT '更新时间';