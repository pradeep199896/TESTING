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

 Date: 31/01/2023 15:06:39
*/


-- ----------------------------
-- Table structure for SOURCE_PASSENGER
-- ----------------------------
DROP TABLE "C##TEST"."SOURCE_PASSENGER";
CREATE TABLE "C##TEST"."SOURCE_PASSENGER" (
  "PASSENGER_ID" NUMBER(10,0) VISIBLE NOT NULL,
  "DBTRACKID" VARCHAR2(255 CHAR) VISIBLE,
  "READTIME" TIMESTAMP(6) VISIBLE,
  "ADDRESS" VARCHAR2(255 CHAR) VISIBLE,
  "BIRTHDAY" TIMESTAMP(6) VISIBLE,
  "BLOBC" BLOB VISIBLE,
  "CLOBC" VARCHAR2(255 CHAR) VISIBLE,
  "CONTACT_NO" VARCHAR2(255 CHAR) VISIBLE,
  "CREATED_AT" TIMESTAMP(6) VISIBLE,
  "FLAG" VARCHAR2(255 CHAR) VISIBLE,
  "NAME" VARCHAR2(255 CHAR) VISIBLE,
  "NATIONALITY" VARCHAR2(255 CHAR) VISIBLE
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
-- Records of SOURCE_PASSENGER
-- ----------------------------
INSERT INTO "C##TEST"."SOURCE_PASSENGER" VALUES ('1', '587b8e0e-38d2-4022-806c-3df412f3b581', TO_TIMESTAMP('2023-01-31 14:52:26.844000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), 'abc_us', NULL, HEXTORAW('74657374'), '<passengers>
   <passenger>
     <entity_name>C##TEST</entity_name>
    <id>134</id>
  </passenger>
</passengers>', '18610711915', NULL, 'c', 'faddy', 'cn');
INSERT INTO "C##TEST"."SOURCE_PASSENGER" VALUES ('2', '84338206-1c8d-4e91-b503-e159cf45a23d', TO_TIMESTAMP('2023-01-31 14:52:29.846000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), 'abc_ind', NULL, HEXTORAW('74657374'), '<passengers>
   <passenger>
     <entity_name>C##TEST</entity_name>
    <id>134</id>
  </passenger>
</passengers>', '18610711915', NULL, 'c', 'unknown', 'cn');
INSERT INTO "C##TEST"."SOURCE_PASSENGER" VALUES ('3', '9bb66191-09b6-4e13-8f89-62cec16484f5', TO_TIMESTAMP('2023-01-31 14:52:32.690000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), 'abc_us', NULL, HEXTORAW('74657374'), '<passengers>
   <passenger>
     <entity_name>C##TEST</entity_name>
    <id>134</id>
  </passenger>
</passengers>', '18610711915', NULL, 'c', 'faddy', 'cn');
INSERT INTO "C##TEST"."SOURCE_PASSENGER" VALUES ('4', '339b17e5-f446-454c-8db8-65ce19d47339', TO_TIMESTAMP('2022-12-27 09:56:07.477000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), 'abc_ind', NULL, HEXTORAW('74657374'), '<passengers>
   <passenger>
     <entity_name>C##TEST</entity_name>
    <id>134</id>
  </passenger>
</passengers>', '18610711915', NULL, 'c', 'unknown', 'cn');
INSERT INTO "C##TEST"."SOURCE_PASSENGER" VALUES ('7', '5059358f-6507-49a7-814e-78de08beb7e5', TO_TIMESTAMP('2022-12-27 09:56:07.477000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), 'abc_us', NULL, HEXTORAW('74657374'), '<passengers>
   <passenger>
     <entity_name>C##TEST</entity_name>
    <id>134</id>
  </passenger>
</passengers>', '18610711915', NULL, 'c', 'faddy', 'cn');
INSERT INTO "C##TEST"."SOURCE_PASSENGER" VALUES ('5', 'c2eda349-f41b-4c6e-8368-0277518d27bd', TO_TIMESTAMP('2022-12-27 09:56:07.477000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), 'abc_us', NULL, HEXTORAW('74657374'), '<passengers>
   <passenger>
     <entity_name>C##TEST</entity_name>
    <id>134</id>
  </passenger>
</passengers>', '18610711915', NULL, 'c', 'faddy', 'cn');
INSERT INTO "C##TEST"."SOURCE_PASSENGER" VALUES ('6', 'c8172d2a-6151-43e2-a8a7-3c34d8b69756', TO_TIMESTAMP('2022-12-27 09:56:07.477000', 'SYYYY-MM-DD HH24:MI:SS:FF6'), 'abc_ind', NULL, HEXTORAW('74657374'), '<passengers>
   <passenger>
     <entity_name>C##TEST</entity_name>
    <id>134</id>
  </passenger>
</passengers>', '18610711915', NULL, 'c', 'unknown', 'cn');

-- ----------------------------
-- Checks structure for table SOURCE_PASSENGER
-- ----------------------------
ALTER TABLE "C##TEST"."SOURCE_PASSENGER" ADD CONSTRAINT "SYS_C007564" CHECK ("PASSENGER_ID" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;
