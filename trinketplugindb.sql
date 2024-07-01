/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80400
 Source Host           : localhost:3306
 Source Schema         : trinketplugindb

 Target Server Type    : MySQL
 Target Server Version : 80400
 File Encoding         : 65001

 Date: 01/07/2024 16:08:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for player_inventories
-- ----------------------------
DROP TABLE IF EXISTS `player_inventories`;
CREATE TABLE `player_inventories`  (
  `player_uuid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `player_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `inventory` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`player_uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_inventories
-- ----------------------------
INSERT INTO `player_inventories` VALUES ('990b0f82-bd20-4dfc-a8c1-a9961378c1d6', 'EwahV1', 'rO0ABXcEAAAACXNyABpvcmcuYnVra2l0LnV0aWwuaW8uV3JhcHBlcvJQR+zxEm8FAgABTAADbWFw\r\ndAAPTGphdmEvdXRpbC9NYXA7eHBzcgA1Y29tLmdvb2dsZS5jb21tb24uY29sbGVjdC5JbW11dGFi\r\nbGVNYXAkU2VyaWFsaXplZEZvcm0AAAAAAAAAAAIAAkwABGtleXN0ABJMamF2YS9sYW5nL09iamVj\r\ndDtMAAZ2YWx1ZXNxAH4ABHhwdXIAE1tMamF2YS5sYW5nLk9iamVjdDuQzlifEHMpbAIAAHhwAAAA\r\nBHQAAj09dAABdnQABHR5cGV0AARtZXRhdXEAfgAGAAAABHQAHm9yZy5idWtraXQuaW52ZW50b3J5\r\nLkl0ZW1TdGFja3NyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cgAQamF2\r\nYS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAAAA50dAAYV0FSUEVEX0ZVTkdVU19PTl9BX1NUSUNL\r\nc3EAfgAAc3EAfgADdXEAfgAGAAAABnEAfgAIdAAJbWV0YS10eXBldAAMZGlzcGxheS1uYW1ldAAE\r\nbG9yZXQAEWN1c3RvbS1tb2RlbC1kYXRhdAAJSXRlbUZsYWdzdXEAfgAGAAAABnQACEl0ZW1NZXRh\r\ndAAKVU5TUEVDSUZJQ3QAl3sidGV4dCI6IiIsImV4dHJhIjpbeyJ0ZXh0IjoiRGVzY2FsY2lmaWNh\r\nZG9yIiwib2JmdXNjYXRlZCI6ZmFsc2UsIml0YWxpYyI6ZmFsc2UsInVuZGVybGluZWQiOmZhbHNl\r\nLCJzdHJpa2V0aHJvdWdoIjpmYWxzZSwiY29sb3IiOiJncmVlbiIsImJvbGQiOnRydWV9XX1zcgA2\r\nY29tLmdvb2dsZS5jb21tb24uY29sbGVjdC5JbW11dGFibGVMaXN0JFNlcmlhbGl6ZWRGb3JtAAAA\r\nAAAAAAACAAFbAAhlbGVtZW50c3QAE1tMamF2YS9sYW5nL09iamVjdDt4cHVxAH4ABgAAAAJ0AL17\r\nInRleHQiOiIiLCJleHRyYSI6W3sidGV4dCI6IlR1cyBhdGFxdWVzIGLDoXNpY29zIHJlYWxpemFu\r\nICsx4p2kIGEgbG9zIEVzcXVlbGV0b3MiLCJvYmZ1c2NhdGVkIjpmYWxzZSwiaXRhbGljIjpmYWxz\r\nZSwidW5kZXJsaW5lZCI6ZmFsc2UsInN0cmlrZXRocm91Z2giOmZhbHNlLCJjb2xvciI6ImdyZWVu\r\nIiwiYm9sZCI6ZmFsc2V9XX10AJJ7InRleHQiOiIiLCJleHRyYSI6W3sidGV4dCI6IlNsb3Q6IEIu\r\nTy5UIiwib2JmdXNjYXRlZCI6ZmFsc2UsIml0YWxpYyI6ZmFsc2UsInVuZGVybGluZWQiOmZhbHNl\r\nLCJzdHJpa2V0aHJvdWdoIjpmYWxzZSwiY29sb3IiOiJnb2xkIiwiYm9sZCI6dHJ1ZX1dfXNxAH4A\r\nDgAAAANzcgATamF2YS51dGlsLkFycmF5TGlzdHiB0h2Zx2GdAwABSQAEc2l6ZXhwAAAAAXcEAAAA\r\nAXQAD0hJREVfQVRUUklCVVRFU3hwcHBwcHBwcA==\r\n');

-- ----------------------------
-- Table structure for tri_count_settings
-- ----------------------------
DROP TABLE IF EXISTS `tri_count_settings`;
CREATE TABLE `tri_count_settings`  (
  `ID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Trinket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `CountNormal` int NULL DEFAULT NULL,
  `CountGold` int NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tri_count_settings
-- ----------------------------
INSERT INTO `tri_count_settings` VALUES ('1', 'Descalcificador', 59, 47);
INSERT INTO `tri_count_settings` VALUES ('2', 'MAD', 0, 0);
INSERT INTO `tri_count_settings` VALUES ('3', 'Warmog', 0, 0);
INSERT INTO `tri_count_settings` VALUES ('4', 'ZombArm', 0, 3);

SET FOREIGN_KEY_CHECKS = 1;
