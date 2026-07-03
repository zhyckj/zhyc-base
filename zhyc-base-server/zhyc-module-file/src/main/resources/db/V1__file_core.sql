-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS file_storage_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    storage_code VARCHAR(64) NOT NULL COMMENT '存储配置编码',
    storage_name VARCHAR(128) NOT NULL COMMENT '存储配置名称',
    storage_type VARCHAR(32) NOT NULL COMMENT '存储类型，例如 local、s3、minio、oss',
    endpoint VARCHAR(255) NOT NULL COMMENT '存储访问端点或本地根路径',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '配置状态',
    default_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认存储配置',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人用户 ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_storage_config_tenant_code (tenant_id, storage_code),
    KEY idx_file_storage_config_tenant_status (tenant_id, status),
    KEY idx_file_storage_config_tenant_default (tenant_id, default_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储配置表';

CREATE TABLE IF NOT EXISTS file_object (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    file_code VARCHAR(64) NOT NULL COMMENT '文件业务编码',
    storage_code VARCHAR(64) NOT NULL COMMENT '存储配置编码',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    content_type VARCHAR(128) NOT NULL COMMENT '文件内容类型',
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
    object_key VARCHAR(500) NOT NULL COMMENT '存储对象键或相对路径',
    file_status VARCHAR(32) NOT NULL DEFAULT 'stored' COMMENT '文件状态',
    uploader_id BIGINT NULL COMMENT '上传人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_object_tenant_code (tenant_id, file_code),
    KEY idx_file_object_tenant_storage (tenant_id, storage_code),
    KEY idx_file_object_tenant_created (tenant_id, created_at),
    KEY idx_file_object_tenant_status (tenant_id, file_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件对象表';

CREATE TABLE IF NOT EXISTS file_preview_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件预览日志主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    file_code VARCHAR(64) NOT NULL COMMENT '文件业务编码',
    preview_type VARCHAR(32) NOT NULL COMMENT '预览类型',
    preview_url VARCHAR(512) NOT NULL COMMENT '预览访问地址',
    result VARCHAR(32) NOT NULL COMMENT '预览结果',
    cost_ms BIGINT NOT NULL DEFAULT 0 COMMENT '预览耗时毫秒',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_file_preview_log_tenant_file (tenant_id, file_code),
    KEY idx_file_preview_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件预览日志表';
