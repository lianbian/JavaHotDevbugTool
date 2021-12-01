/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50639
 Source Host           : localhost:3306
 Source Schema         : lianbian

 Target Server Type    : MySQL
 Target Server Version : 50639
 File Encoding         : 65001

 Date: 01/12/2021 21:48:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for User
-- ----------------------------
DROP TABLE IF EXISTS `User`;
CREATE TABLE `User` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '姓名',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `email` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of User
-- ----------------------------
BEGIN;
INSERT INTO `User` VALUES (1, 'Jone', 18, 'test1@baomidou.com');
INSERT INTO `User` VALUES (2, 'Jack', 20, 'test2@baomidou.com');
INSERT INTO `User` VALUES (3, 'Tom', 28, 'test3@baomidou.com');
INSERT INTO `User` VALUES (4, 'Sandy', 21, 'test4@baomidou.com');
INSERT INTO `User` VALUES (5, 'Billie', 24, 'test5@baomidou.com');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
