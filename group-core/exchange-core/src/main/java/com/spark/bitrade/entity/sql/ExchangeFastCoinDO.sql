-- auto Generated on 2019-03-27 10:26:36 
-- DROP TABLE IF EXISTS `exchange_fast_coin`;
CREATE TABLE exchange_fast_coin(
    `id` varchar(150) NOT NULL COMMENT 'id,可用是baseSymbol，coinSymbol，appId组合',
    `base_symbol` VARCHAR(32) NOT NULL  COMMENT '闪兑基币币种名称,如CNYT、BT',
    `coin_symbol` VARCHAR(32) NOT NULL  COMMENT '闪兑币种名称，如BTC、LTC',
    /*`rate_reference_symbol` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '实时汇率参考交易对名称',*/
    `enable` INTEGER(12) NOT NULL DEFAULT 1 COMMENT '启用状态，1=启用/2=禁止',
    `app_id` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '渠道',
    `rate_reference_base_symbol` VARCHAR(32) NULL COMMENT '兑换基币实时汇率参考币种名称，可用为null',
    `rate_reference_coin_symbol` VARCHAR(32) NULL COMMENT '兑换币种实时汇率参考币种名称，可用为null',
    `buy_adjust_rate` DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT '买入时价格上调默认比例,取值[0-1]；闪兑用户买入时，基于实时价的上调价格的浮动比例。',
    `sell_adjust_rate` DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT '卖出时价格下调默认比例，取值[0-1]；闪兑用户买出时，基于实时价的下调价格的浮动比例。',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '闪兑币种配置';


ALTER TABLE exchange_fast_coin add create_time datetime(0) NULL DEFAULT NULL COMMENT '创建时间';
ALTER TABLE exchange_fast_coin add update_time datetime(0) NULL DEFAULT NULL COMMENT '更新时间';

ALTER TABLE exchange_fast_coin add base_symbol_fixed_rate DECIMAL(18,8) NOT NULL DEFAULT 0 COMMENT '兑换基币的固定费率';
ALTER TABLE exchange_fast_coin add coin_symbol_fixed_rate DECIMAL(18,8) NOT NULL DEFAULT 0 COMMENT '兑换币种的固定费率';
