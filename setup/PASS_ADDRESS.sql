/*
 Navicat Premium Data Transfer

 Source Server         : localhost19c
 Source Server Type    : Oracle
 Source Server Version : 190000 (Oracle Database 19c Standard Edition 2 Release 19.0.0.0.0 - Production)
 Source Host           : localhost:1524
 Source Schema         : C##TEST

 Target Server Type    : Oracle
 Target Server Version : 190000 (Oracle Database 19c Standard Edition 2 Release 19.0.0.0.0 - Production)
 File Encoding         : 65001

 Date: 31/01/2023 15:07:29
*/


-- ----------------------------
-- Table structure for PASS_ADDRESS
-- ----------------------------
DROP TABLE "C##TEST"."PASS_ADDRESS";
CREATE TABLE "C##TEST"."PASS_ADDRESS" (
  "ID" NUMBER(10,0) VISIBLE NOT NULL,
  "CITY" VARCHAR2(255 CHAR) VISIBLE,
  "ZIP" VARCHAR2(255 CHAR) VISIBLE,
  "PASS_ID" NUMBER(10,0) VISIBLE
)
LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  INITIAL 65536 
  NEXT 1048576 
  MINEXTENTS 1
  MAXEXTENTS 2147483645
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;

-- ----------------------------
-- Records of PASS_ADDRESS
-- ----------------------------
INSERT INTO "C##TEST"."PASS_ADDRESS" VALUES ('1', 'bj', '100260', '1');
INSERT INTO "C##TEST"."PASS_ADDRESS" VALUES ('2', 'sh', '200260', '1');
INSERT INTO "C##TEST"."PASS_ADDRESS" VALUES ('3', 'sl', '300260', '2');
INSERT INTO "C##TEST"."PASS_ADDRESS" VALUES ('4', 'dl', '400260', '2');

-- ----------------------------
-- Checks structure for table PASS_ADDRESS
-- ----------------------------
ALTER TABLE "C##TEST"."PASS_ADDRESS" ADD CONSTRAINT "SYS_C007562" CHECK ("ID" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
