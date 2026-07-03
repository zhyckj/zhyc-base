-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS msg_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    channel_type VARCHAR(32) NOT NULL COMMENT '消息通道类型',
    title_template VARCHAR(255) NOT NULL COMMENT '标题模板',
    content_template TEXT NOT NULL COMMENT '内容模板',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '模板状态',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人用户 ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_template_tenant_code (tenant_id, template_code),
    KEY idx_msg_template_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

CREATE TABLE IF NOT EXISTS msg_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    message_code VARCHAR(64) NOT NULL COMMENT '消息编码',
    receiver_id BIGINT NOT NULL COMMENT '接收人用户 ID',
    receiver_name VARCHAR(128) NULL COMMENT '接收人名称',
    message_type VARCHAR(32) NOT NULL COMMENT '消息类型',
    title VARCHAR(255) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读，0 未读，1 已读',
    read_at DATETIME NULL COMMENT '阅读时间',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_message_tenant_code (tenant_id, message_code),
    KEY idx_msg_message_tenant_receiver (tenant_id, receiver_id, read_flag, created_at),
    KEY idx_msg_message_tenant_type (tenant_id, message_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';
