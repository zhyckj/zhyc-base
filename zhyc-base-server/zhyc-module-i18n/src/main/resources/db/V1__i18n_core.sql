-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS i18n_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式数据隔离',
    locale VARCHAR(32) NOT NULL COMMENT '语言标识，例如 zh-CN、en-US',
    message_key VARCHAR(190) NOT NULL COMMENT '词条键',
    message_value VARCHAR(1000) NOT NULL COMMENT '词条值',
    message_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '词条状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_i18n_message_tenant_locale_key (tenant_id, locale, message_key),
    KEY idx_i18n_message_tenant_locale_status (tenant_id, locale, message_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='国际化词条表';
