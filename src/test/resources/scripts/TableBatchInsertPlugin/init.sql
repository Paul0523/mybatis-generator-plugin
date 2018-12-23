/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : mybatis-generator-plugin

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2017-06-26 17:30:13
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tb1
-- ----------------------------
DROP TABLE IF EXISTS `tb1`;
CREATE TABLE `tb1` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_update` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `record_status` smallint(6) NOT NULL DEFAULT '0',
  `market_type` int(11) NOT NULL COMMENT '选品类型  0-flash sale 1-best seller 2-new arrivial 3-price zone 4-cf mall',
  `countryCode` varchar(10) NOT NULL COMMENT '国家二字码，小写',
  `gender` int(5) NOT NULL COMMENT '性别 0-男 1-女 -1-未知',
  `product_no` varchar(20) NOT NULL COMMENT '商品货号',
  `product_id` int(11) NOT NULL COMMENT '商品Id spuId',
  `price_local` double NOT NULL DEFAULT '0' COMMENT '活动价',
  `sequence` int(11) NOT NULL DEFAULT '0' COMMENT '商品排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;