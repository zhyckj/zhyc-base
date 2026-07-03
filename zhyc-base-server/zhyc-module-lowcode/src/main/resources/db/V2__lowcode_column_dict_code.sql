-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 低代码字段模型字典绑定能力升级脚本。
-- 旧环境已存在 lowcode_column_model 表时，V1 的 CREATE TABLE IF NOT EXISTS 不会自动补齐新增列。
SET @lowcode_column_dict_code_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'lowcode_column_model'
      AND COLUMN_NAME = 'dict_code'
);

SET @lowcode_column_dict_code_ddl := IF(
    @lowcode_column_dict_code_exists = 0,
    'ALTER TABLE lowcode_column_model ADD COLUMN dict_code VARCHAR(64) DEFAULT NULL COMMENT ''绑定的系统字典编码'' AFTER queryable',
    'SELECT 1'
);

PREPARE lowcode_column_dict_code_stmt FROM @lowcode_column_dict_code_ddl;
EXECUTE lowcode_column_dict_code_stmt;
DEALLOCATE PREPARE lowcode_column_dict_code_stmt;
