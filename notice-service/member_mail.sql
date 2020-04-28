/*
Navicat MySQL Data Transfer

Source Server         : 172.16.0.100
Source Server Version : 50634
Source Host           : 172.16.0.100:3306
Source Database       : otc_sync

Target Server Type    : MYSQL
Target Server Version : 50634
File Encoding         : 65001

Date: 2019-09-30 10:14:12
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for member_mail
-- ----------------------------
DROP TABLE IF EXISTS `member_mail`;
CREATE TABLE `member_mail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_member_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '站内信  用户ID',
  `to_member_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '用户ID',
  `subject` varchar(255) NOT NULL COMMENT '站内信 主题',
  `content_id` bigint(20) NOT NULL COMMENT '站内信 内容表ID',
  `create_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '创建时间',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '站内信 状态 0未读 1 已读',
  PRIMARY KEY (`id`),
  KEY `from_member_id_index` (`from_member_id`),
  KEY `to_member_id_index` (`to_member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22232 DEFAULT CHARSET=utf8;
