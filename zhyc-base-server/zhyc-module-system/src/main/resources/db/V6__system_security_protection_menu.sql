-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 安全防护中心菜单增量脚本。
-- 说明：用于已初始化数据库补齐安全防护中心入口和按钮权限，脚本可重复执行。

INSERT INTO sys_menu (
    id,
    tenant_id,
    parent_id,
    menu_code,
    menu_name,
    menu_type,
    path,
    component,
    permission,
    sort_order,
    status
)
VALUES
    (260, 'zhyc-platform', 200, 'system-security-protection', '安全防护中心', 'menu',
     '/system/security-protection', 'system/security-protection/index',
     'system:security-protection:query', 150, 'enabled'),
    (250, 'zhyc-platform', 260, 'system-access-restriction', '访问限制查询', 'button',
     NULL, NULL, 'system:access-restriction:query', 5, 'enabled'),
    (25001, 'zhyc-platform', 260, 'system-access-restriction-save', '访问限制保存', 'button',
     NULL, NULL, 'system:access-restriction:save', 6, 'enabled'),
    (25002, 'zhyc-platform', 260, 'system-access-restriction-evaluate', '访问限制校验', 'button',
     NULL, NULL, 'system:access-restriction:evaluate', 7, 'enabled'),
    (26001, 'zhyc-platform', 260, 'system-security-protection-save', '保存策略', 'button',
     NULL, NULL, 'system:security-protection:save', 1, 'enabled'),
    (26002, 'zhyc-platform', 260, 'system-security-protection-block', '封禁 IP', 'button',
     NULL, NULL, 'system:security-protection:block', 2, 'enabled'),
    (26003, 'zhyc-platform', 260, 'system-security-protection-unblock', '解封 IP', 'button',
     NULL, NULL, 'system:security-protection:unblock', 3, 'enabled'),
    (26004, 'zhyc-platform', 260, 'system-security-protection-record', '记录事件', 'button',
     NULL, NULL, 'system:security-protection:record', 4, 'enabled')
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    menu_name = VALUES(menu_name),
    menu_type = VALUES(menu_type),
    path = VALUES(path),
    component = VALUES(component),
    permission = VALUES(permission),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

INSERT IGNORE INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 'zhyc-platform', r.id, m.id
FROM sys_role r
JOIN sys_menu m
  ON m.tenant_id = r.tenant_id
WHERE r.tenant_id = 'zhyc-platform'
  AND r.role_code = 'platform-admin'
  AND m.menu_code IN (
      'system-security-protection',
      'system-access-restriction',
      'system-access-restriction-save',
      'system-access-restriction-evaluate',
      'system-security-protection-save',
      'system-security-protection-block',
      'system-security-protection-unblock',
      'system-security-protection-record'
  );
