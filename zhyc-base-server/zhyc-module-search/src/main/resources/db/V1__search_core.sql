-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS search_index_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '索引配置主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    index_code VARCHAR(128) NOT NULL COMMENT '索引编码',
    index_name VARCHAR(128) NOT NULL COMMENT '索引名称',
    source_table VARCHAR(128) NOT NULL COMMENT '数据来源表名',
    search_fields VARCHAR(512) NOT NULL COMMENT '可检索字段列表，逗号分隔',
    filter_fields VARCHAR(512) DEFAULT NULL COMMENT '可过滤字段列表，逗号分隔',
    index_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '索引状态',
    remark VARCHAR(512) DEFAULT NULL COMMENT '配置备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    UNIQUE KEY uk_search_index_config_tenant_code (tenant_id, index_code),
    KEY idx_search_index_config_tenant_status (tenant_id, index_status)
) COMMENT='全文检索索引配置';

CREATE TABLE IF NOT EXISTS search_rebuild_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '重建任务主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    index_code VARCHAR(128) NOT NULL COMMENT '索引编码',
    task_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '任务状态',
    trigger_type VARCHAR(32) NOT NULL DEFAULT 'manual' COMMENT '触发类型',
    started_at DATETIME DEFAULT NULL COMMENT '任务开始时间',
    finished_at DATETIME DEFAULT NULL COMMENT '任务完成时间',
    error_message VARCHAR(1024) DEFAULT NULL COMMENT '失败错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    KEY idx_search_rebuild_task_tenant_index (tenant_id, index_code),
    KEY idx_search_rebuild_task_status (task_status)
) COMMENT='全文检索索引重建任务';

CREATE TABLE IF NOT EXISTS search_query_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '查询日志主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    index_code VARCHAR(128) NOT NULL COMMENT '索引编码',
    keyword VARCHAR(256) NOT NULL COMMENT '查询关键词',
    result_count INT NOT NULL DEFAULT 0 COMMENT '返回结果数量',
    cost_ms BIGINT NOT NULL DEFAULT 0 COMMENT '查询耗时毫秒',
    query_status VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '查询状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_search_query_log_tenant_index (tenant_id, index_code),
    KEY idx_search_query_log_created_at (created_at)
) COMMENT='全文检索查询日志';
