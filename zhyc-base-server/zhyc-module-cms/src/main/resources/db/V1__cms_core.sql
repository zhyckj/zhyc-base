-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS cms_channel (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '内容栏目主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    parent_id BIGINT NULL COMMENT '父栏目主键',
    channel_code VARCHAR(64) NOT NULL COMMENT '栏目编码',
    channel_name VARCHAR(128) NOT NULL COMMENT '栏目名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    channel_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '栏目状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_channel_tenant_code (tenant_id, channel_code),
    KEY idx_cms_channel_tenant_status (tenant_id, channel_status)
) COMMENT='内容栏目';

CREATE TABLE IF NOT EXISTS cms_content (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '内容文章主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    channel_code VARCHAR(64) NOT NULL COMMENT '栏目编码',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    summary VARCHAR(500) NULL COMMENT '文章摘要',
    body_content LONGTEXT NULL COMMENT '文章正文',
    content_status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '文章状态',
    author_id BIGINT NULL COMMENT '作者用户主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    KEY idx_cms_content_tenant_channel (tenant_id, channel_code),
    KEY idx_cms_content_tenant_status (tenant_id, content_status)
) COMMENT='内容文章';
