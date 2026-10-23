-- =============================================================
-- 学生管理系统 数据库初始化脚本（幂等, 不删除任何已有表和数据）
-- 目标环境: MySQL 8.4 / utf8mb4
--
-- 安全约定:
--   1. 只使用 CREATE DATABASE / CREATE TABLE ... IF NOT EXISTS,
--      不含任何删除类语句, 重复执行不会破坏已有结构与数据;
--      已存在的表会被直接跳过(CREATE TABLE IF NOT EXISTS 不做结构比对)。
--   2. 初始管理员用 INSERT IGNORE 写入, 依赖 uk_admin_username_active 唯一索引,
--      重复执行不会产生重复账号, 也不会因唯一键冲突中断脚本。
--   3. 需要"重置开发库"时, 请在确认无重要数据后由管理员另行手工处理; 本脚本不提供、也不执行任何删除操作。
--
-- 执行方式(需要 DDL 权限, 例如 root; 建议显式指定 utf8mb4, 避免中文注释/昵称乱码):
--   mysql --default-character-set=utf8mb4 -u root -p -e "source d:/CODE/backend/src/main/resources/db/schema.sql"
-- 也可用 MySQL Workbench / Navicat 打开本文件整体执行。
-- =============================================================

CREATE DATABASE IF NOT EXISTS `sms`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
-- 注: sms 库若已存在, 上面的语句不会改动其字符集, 可用 SHOW CREATE DATABASE sms; 核对。

USE `sms`;

SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 管理员
--
-- 唯一约束说明: 为了支持"逻辑删除后可重新使用同一账号", 用生成列
-- username_active 只在 deleted = 0 时取真实值, 已删除记录该列为 NULL,
-- 而唯一索引允许多个 NULL, 从而不阻塞同名账号重新创建.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `admin`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`        VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`        VARCHAR(100) NOT NULL COMMENT '密码, BCrypt 哈希',
    `nickname`        VARCHAR(50)           DEFAULT NULL COMMENT '显示名称',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1 启用, 0 禁用',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `username_active` VARCHAR(50) GENERATED ALWAYS AS (IF(`deleted` = 0, `username`, NULL)) STORED
        COMMENT '仅未删除记录参与唯一约束',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username_active` (`username_active`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='管理员';

-- -------------------------------------------------------------
-- 班级
-- 唯一约束: 同一"年级 + 班级名称"在未删除记录中不可重复
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `class_info`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `class_name`        VARCHAR(50)  NOT NULL COMMENT '班级名称, 如 计算机1班',
    `grade`             VARCHAR(20)  NOT NULL COMMENT '年级, 如 2026级',
    `head_teacher`      VARCHAR(50)           DEFAULT NULL COMMENT '班主任',
    `remark`            VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `grade_active`      VARCHAR(20) GENERATED ALWAYS AS (IF(`deleted` = 0, `grade`, NULL)) STORED
        COMMENT '仅未删除记录参与唯一约束',
    `class_name_active` VARCHAR(50) GENERATED ALWAYS AS (IF(`deleted` = 0, `class_name`, NULL)) STORED
        COMMENT '仅未删除记录参与唯一约束',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_class_grade_name_active` (`grade_active`, `class_name_active`),
    KEY `idx_class_grade` (`grade`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='班级';

-- -------------------------------------------------------------
-- 学生
-- 说明:
--   1. 不存 age 字段, 年龄由 birth_date 实时计算
--   2. class_id 不建物理外键, 由业务层保证引用有效性(逻辑删除下外键无意义)
--   3. 唯一约束同样使用生成列, 删除后可重新录入相同学号
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `student`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `student_no`        VARCHAR(20)  NOT NULL COMMENT '学号, 手工录入',
    `name`              VARCHAR(50)  NOT NULL COMMENT '姓名',
    `gender`            TINYINT      NOT NULL DEFAULT 0 COMMENT '性别: 0 未知, 1 男, 2 女',
    `birth_date`        DATE                  DEFAULT NULL COMMENT '出生日期, 用于计算年龄',
    `phone`             VARCHAR(20)           DEFAULT NULL COMMENT '学生手机号',
    `email`             VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `guardian_name`     VARCHAR(50)           DEFAULT NULL COMMENT '家长姓名',
    `guardian_phone`    VARCHAR(20)           DEFAULT NULL COMMENT '家长联系电话',
    `class_id`          BIGINT       NOT NULL COMMENT '所属班级 id',
    `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1 在读, 0 离校',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0 未删除, 1 已删除',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `student_no_active` VARCHAR(20) GENERATED ALWAYS AS (IF(`deleted` = 0, `student_no`, NULL)) STORED
        COMMENT '仅未删除记录参与唯一约束',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_no_active` (`student_no_active`),
    KEY `idx_student_class_id` (`class_id`),
    KEY `idx_student_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学生';

-- -------------------------------------------------------------
-- 初始管理员: admin / 123456  (仅开发环境)
--
-- 哈希由 spring-security-crypto 6.2.7 的 BCrypt (strength=10) 生成,
-- 并已用 BCrypt.checkpw("123456", hash) 校验通过 (见 AdminPasswordHashTest).
--
-- INSERT IGNORE 依赖 uk_admin_username_active: 已存在未删除的 admin 时该次插入被忽略,
-- 因此脚本可重复执行; 若存在同名但 deleted = 1 的记录, 会插入一个新的启用账号。
-- 生产环境请删除本段, 或首次登录后立即修改密码。
-- -------------------------------------------------------------
INSERT IGNORE INTO `admin` (`username`, `password`, `nickname`, `status`)
VALUES ('admin', '$2a$10$yDKqu38sAojRlpVT9KYDQ.WV043ddNk6agKblfOR9U2U91iQDdfVC', '超级管理员', 1);
