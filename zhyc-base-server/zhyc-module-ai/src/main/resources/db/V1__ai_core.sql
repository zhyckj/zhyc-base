-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS ai_provider (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    provider_code VARCHAR(64) NOT NULL COMMENT '供应商编码',
    provider_name VARCHAR(128) NOT NULL COMMENT '供应商名称',
    provider_type VARCHAR(64) NOT NULL COMMENT '供应商类型',
    base_url VARCHAR(512) NOT NULL COMMENT '模型服务基础地址',
    secret_ref VARCHAR(255) NOT NULL COMMENT '密钥中心引用',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_provider_tenant_code (tenant_id, provider_code),
    KEY idx_ai_provider_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型供应商表';

CREATE TABLE IF NOT EXISTS ai_model_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    provider_id BIGINT NOT NULL COMMENT '供应商主键',
    model_code VARCHAR(128) NOT NULL COMMENT '模型编码',
    model_name VARCHAR(128) NOT NULL COMMENT '模型名称',
    model_type VARCHAR(32) NOT NULL COMMENT '模型类型',
    context_window INT NOT NULL COMMENT '上下文长度',
    support_stream TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否支持流式输出',
    support_tool TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否支持工具调用',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_model_tenant_code (tenant_id, model_code),
    KEY idx_ai_model_tenant_provider (tenant_id, provider_id),
    KEY idx_ai_model_tenant_status (tenant_id, status),
    CONSTRAINT fk_ai_model_provider FOREIGN KEY (provider_id) REFERENCES ai_provider (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型配置表';

CREATE TABLE IF NOT EXISTS ai_app (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT 'AI 应用编码',
    app_name VARCHAR(128) NOT NULL COMMENT 'AI 应用名称',
    default_model_id BIGINT NOT NULL COMMENT '默认模型配置主键',
    system_prompt TEXT NOT NULL COMMENT '系统提示词',
    daily_token_quota INT NOT NULL DEFAULT 100000 COMMENT '每日令牌额度',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_app_tenant_code (tenant_id, app_code),
    KEY idx_ai_app_tenant_status (tenant_id, status),
    KEY idx_ai_app_default_model (default_model_id),
    CONSTRAINT fk_ai_app_default_model FOREIGN KEY (default_model_id) REFERENCES ai_model_config (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 应用接入表';

CREATE TABLE IF NOT EXISTS ai_prompt_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    prompt_code VARCHAR(64) NOT NULL COMMENT '提示词编码',
    prompt_name VARCHAR(128) NOT NULL COMMENT '提示词名称',
    version VARCHAR(32) NOT NULL COMMENT '版本号',
    template_content TEXT NOT NULL COMMENT '模板内容',
    variables VARCHAR(1000) DEFAULT NULL COMMENT '变量清单',
    status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_prompt_tenant_code_version (tenant_id, prompt_code, version),
    KEY idx_ai_prompt_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词模板表';

CREATE TABLE IF NOT EXISTS ai_invocation_audit (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT 'AI 应用编码',
    provider_id BIGINT NOT NULL COMMENT '供应商主键',
    model_id BIGINT NOT NULL COMMENT '模型配置主键',
    invocation_type VARCHAR(32) NOT NULL COMMENT '调用类型',
    prompt_tokens INT NOT NULL DEFAULT 0 COMMENT '提示词令牌数',
    completion_tokens INT NOT NULL DEFAULT 0 COMMENT '输出令牌数',
    total_tokens INT NOT NULL DEFAULT 0 COMMENT '总令牌数',
    latency_ms BIGINT NOT NULL DEFAULT 0 COMMENT '调用耗时毫秒',
    status VARCHAR(32) NOT NULL COMMENT '调用状态',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误消息',
    trace_id VARCHAR(128) DEFAULT NULL COMMENT '链路追踪编号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_ai_audit_tenant_app_time (tenant_id, app_code, created_at),
    KEY idx_ai_audit_tenant_model_time (tenant_id, model_id, created_at),
    KEY idx_ai_audit_trace (trace_id),
    CONSTRAINT fk_ai_audit_provider FOREIGN KEY (provider_id) REFERENCES ai_provider (id),
    CONSTRAINT fk_ai_audit_model FOREIGN KEY (model_id) REFERENCES ai_model_config (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用审计表';
