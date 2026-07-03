-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS job_task (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '作业任务主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    job_code VARCHAR(64) NOT NULL COMMENT '作业任务编码',
    job_name VARCHAR(128) NOT NULL COMMENT '作业任务名称',
    cron_expression VARCHAR(128) NOT NULL COMMENT 'Cron 表达式',
    handler_name VARCHAR(128) NOT NULL COMMENT '任务处理器名称',
    job_description VARCHAR(500) NULL COMMENT '作业任务说明',
    job_status VARCHAR(32) NOT NULL DEFAULT 'disabled' COMMENT '作业状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_task_tenant_code (tenant_id, job_code),
    KEY idx_job_task_tenant_status (tenant_id, job_status)
) COMMENT='在线作业任务';

CREATE TABLE IF NOT EXISTS job_task_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '作业执行日志主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    job_id BIGINT NOT NULL COMMENT '作业任务主键',
    trigger_type VARCHAR(32) NOT NULL COMMENT '触发类型',
    start_at DATETIME NOT NULL COMMENT '开始时间',
    end_at DATETIME NULL COMMENT '结束时间',
    result VARCHAR(32) NOT NULL COMMENT '执行结果',
    error_message VARCHAR(1000) NULL COMMENT '错误信息',
    operator_id BIGINT NULL COMMENT '操作人用户主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_job_task_log_tenant_job (tenant_id, job_id, start_at)
) COMMENT='作业执行日志';
