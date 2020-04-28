/*
Navicat MySQL Data Transfer

Source Server         : 172.16.0.100
Source Server Version : 50634
Source Host           : 172.16.0.100:3306
Source Database       : otc_sync

Target Server Type    : MYSQL
Target Server Version : 50634
File Encoding         : 65001

Date: 2019-09-30 10:14:26
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for member_mail_content
-- ----------------------------
DROP TABLE IF EXISTS `member_mail_content`;
CREATE TABLE `member_mail_content` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL COMMENT '站内信内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44555 DEFAULT CHARSET=utf8;
