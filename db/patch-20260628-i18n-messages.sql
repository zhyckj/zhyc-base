-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 国际化词条初始化补丁。
-- 说明：可在已存在的 zhyc-base-v1 库重复执行，补齐基础词条和国际化维护权限。
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

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_code, menu_name, menu_type, path, component, permission, sort_order, status)
VALUES
    (121001, 'zhyc-platform', 1210, 'i18n-message-save', '词条保存', 'button', NULL, NULL, 'i18n:message:save', 1, 'enabled'),
    (121002, 'zhyc-platform', 1210, 'i18n-message-resolve', '词条解析', 'button', NULL, NULL, 'i18n:message:resolve', 2, 'enabled')
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    menu_name = VALUES(menu_name),
    menu_type = VALUES(menu_type),
    path = VALUES(path),
    component = VALUES(component),
    permission = VALUES(permission),
    sort_order = VALUES(sort_order),
    status = VALUES(status);

INSERT IGNORE INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 'zhyc-platform', 1, id
FROM sys_menu
WHERE tenant_id = 'zhyc-platform'
  AND (menu_code LIKE 'i18n%' OR permission LIKE 'i18n:%');

INSERT INTO i18n_message (tenant_id, locale, message_key, message_value, message_status)
VALUES
    ('zhyc-platform', 'zh-CN', 'platform.name', 'ZHYC 快速开发平台', 'enabled'),
    ('zhyc-platform', 'en-US', 'platform.name', 'ZHYC Rapid Development Platform', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'platform.subtitle', '企业级低代码快速开发平台', 'enabled'),
    ('zhyc-platform', 'en-US', 'platform.subtitle', 'Enterprise Low-Code Rapid Development Platform', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'platform.auth.login', '统一认证', 'enabled'),
    ('zhyc-platform', 'en-US', 'platform.auth.login', 'SSO Login', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'platform.auth.logout', '退出登录', 'enabled'),
    ('zhyc-platform', 'en-US', 'platform.auth.logout', 'Logout', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'platform.runtime', '运行时', 'enabled'),
    ('zhyc-platform', 'en-US', 'platform.runtime', 'Runtime', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'status.enabled', '启用', 'enabled'),
    ('zhyc-platform', 'en-US', 'status.enabled', 'Enabled', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'status.disabled', '停用', 'enabled'),
    ('zhyc-platform', 'en-US', 'status.disabled', 'Disabled', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'status.certified', '已认证', 'enabled'),
    ('zhyc-platform', 'en-US', 'status.certified', 'Certified', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'status.unauthenticated', '未认证', 'enabled'),
    ('zhyc-platform', 'en-US', 'status.unauthenticated', 'Unauthenticated', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.query', '查询', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.query', 'Query', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.refresh', '刷新', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.refresh', 'Refresh', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.create', '新增', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.create', 'Create', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.edit', '编辑', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.edit', 'Edit', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.delete', '删除', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.delete', 'Delete', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.save', '保存', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.save', 'Save', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.publish', '发布', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.publish', 'Publish', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.authorize', '授权', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.authorize', 'Authorize', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.bind', '绑定', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.bind', 'Bind', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.testConnection', '连接测试', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.testConnection', 'Test Connection', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'button.copyReference', '复制引用', 'enabled'),
    ('zhyc-platform', 'en-US', 'button.copyReference', 'Copy Reference', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.dashboard', '个人工作台', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.dashboard', 'Dashboard', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.system', '系统管理', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.system', 'System', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.system.tenant', '租户管理', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.system.tenant', 'Tenant Management', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.system.org', '组织机构', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.system.org', 'Organization', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.system.user', '用户管理', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.system.user', 'User Management', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.system.role', '角色管理', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.system.role', 'Role Management', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.system.menu', '菜单权限', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.system.menu', 'Menu Permissions', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.system.dict', '字典管理', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.system.dict', 'Dictionary', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.system.secret', '密钥管理', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.system.secret', 'Secret Management', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.lowcode', '低代码中心', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.lowcode', 'Low-Code Center', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.lowcode.datasource', '数据源管理', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.lowcode.datasource', 'Data Sources', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.lowcode.model', '数据表建模', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.lowcode.model', 'Data Modeling', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.workflow', '工作流中心', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.workflow', 'Workflow Center', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.workflow.model', '流程模型', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.workflow.model', 'Process Models', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.openapi', '开放平台', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.openapi', 'Open Platform', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.purchase', '采购样板', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.purchase', 'Purchase Sample', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.i18n', '国际化', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.i18n', 'Internationalization', 'enabled'),
    ('zhyc-platform', 'zh-CN', 'menu.i18n.message', '国际化词条', 'enabled'),
    ('zhyc-platform', 'en-US', 'menu.i18n.message', 'I18n Messages', 'enabled')
ON DUPLICATE KEY UPDATE
    message_value = VALUES(message_value),
    message_status = VALUES(message_status),
    deleted = 0,
    updated_at = CURRENT_TIMESTAMP;
