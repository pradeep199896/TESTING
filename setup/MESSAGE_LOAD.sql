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

 Date: 31/01/2023 15:09:31
*/


-- ----------------------------
-- Table structure for MESSAGE_LOAD
-- ----------------------------
DROP TABLE "C##TEST"."MESSAGE_LOAD";
CREATE TABLE "C##TEST"."MESSAGE_LOAD" (
  "ID" NUMBER VISIBLE NOT NULL,
  "LOAD" BLOB VISIBLE,
  "TRACKING_ID" VARCHAR2(50 BYTE) VISIBLE,
  "READ_TIME" DATE VISIBLE,
  "FLAG" VARCHAR2(20 BYTE) VISIBLE
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
-- Records of MESSAGE_LOAD
-- ----------------------------
INSERT INTO "C##TEST"."MESSAGE_LOAD" VALUES ('1', NULL, '8d6d8705-9c0c-4c23-bd4f-ae241bc05578', TO_DATE('2023-01-31 14:52:12', 'SYYYY-MM-DD HH24:MI:SS'), 'c');

-- ----------------------------
-- Primary Key structure for table MESSAGE_LOAD
-- ----------------------------
ALTER TABLE "C##TEST"."MESSAGE_LOAD" ADD CONSTRAINT "SYS_C007592" PRIMARY KEY ("ID");

-- ----------------------------
-- Checks structure for table MESSAGE_LOAD
-- ----------------------------
ALTER TABLE "C##TEST"."MESSAGE_LOAD" ADD CONSTRAINT "SYS_C007560" CHECK ("ID" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
