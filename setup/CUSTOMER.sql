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

 Date: 31/01/2023 15:07:44
*/


-- ----------------------------
-- Table structure for CUSTOMER
-- ----------------------------
DROP TABLE "C##TEST"."CUSTOMER";
CREATE TABLE "C##TEST"."CUSTOMER" (
  "CUSTOMER_ID" NUMBER(10,0) VISIBLE NOT NULL,
  "ADB_TRACKINGID" VARCHAR2(255 CHAR) VISIBLE,
  "D_STATUS" VARCHAR2(5 CHAR) VISIBLE,
  "SEQUENCE_ID" VARCHAR2(50 CHAR) VISIBLE,
  "CITY" VARCHAR2(255 CHAR) VISIBLE,
  "FIRST_NAME" VARCHAR2(255 CHAR) VISIBLE,
  "LAST_NAME" VARCHAR2(255 CHAR) VISIBLE,
  "STATE" VARCHAR2(255 CHAR) VISIBLE,
  "ZIP" VARCHAR2(255 CHAR) VISIBLE,
  "DBTIME" TIMESTAMP(6) VISIBLE,
  "BLOBC" BLOB VISIBLE,
  "CLOBC" CLOB VISIBLE,
  "SYSTEMTIME" TIMESTAMP(6) VISIBLE
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
-- Records of CUSTOMER
-- ----------------------------
INSERT INTO "C##TEST"."CUSTOMER" VALUES ('1', '21caa1ca-b837-4caa-8719-d976b7947ffa', 'c', '1', 'sgp', 'tom', 'murphy', 'CA', '1000', TO_TIMESTAMP('2023-01-31 14:56:55.552000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), NULL, NULL, TO_TIMESTAMP('2022-10-26 17:40:00.000000', 'SYYYY-MM-DD HH24:MI:SS:FF6'));
INSERT INTO "C##TEST"."CUSTOMER" VALUES ('2', 'd3d4ba21-2510-47ec-90ee-696b2e44c7ce', 'c', '2', 'sgp', 'tom', 'murphy', 'CA', '1000', TO_TIMESTAMP('2023-01-31 14:56:58.560000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), NULL, NULL, TO_TIMESTAMP('2022-10-26 17:40:00.000000', 'SYYYY-MM-DD HH24:MI:SS:FF6'));
INSERT INTO "C##TEST"."CUSTOMER" VALUES ('3', '230153fc-7a07-4c47-96fd-dade47f33364', 'c', '3', NULL, 'Michelle', 'Butler', NULL, NULL, TO_TIMESTAMP('2023-01-31 14:51:58.207000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), NULL, NULL, NULL);
INSERT INTO "C##TEST"."CUSTOMER" VALUES ('4', '8a73ccde-670e-4453-914b-4b61d0813f31', 'c', '4', NULL, 'Michelle', 'Butler', NULL, NULL, TO_TIMESTAMP('2023-01-13 08:10:47.563000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), NULL, NULL, NULL);
INSERT INTO "C##TEST"."CUSTOMER" VALUES ('5', '50d18216-a012-44ef-ab92-2a1d1fc941b6', 'c', '5', NULL, 'Michelle', 'Butler', NULL, NULL, TO_TIMESTAMP('2023-01-13 08:10:47.563000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), NULL, NULL, NULL);

-- ----------------------------
-- Primary Key structure for table CUSTOMER
-- ----------------------------
ALTER TABLE "C##TEST"."CUSTOMER" ADD CONSTRAINT "SYS_C007598" PRIMARY KEY ("CUSTOMER_ID");

-- ----------------------------
-- Checks structure for table CUSTOMER
-- ----------------------------
ALTER TABLE "C##TEST"."CUSTOMER" ADD CONSTRAINT "SYS_C007558" CHECK ("CUSTOMER_ID" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
