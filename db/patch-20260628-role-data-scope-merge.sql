-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 角色数据权限合并到角色管理。
-- 说明：禁用旧的独立菜单入口，并把数据权限编辑按钮挂到当前库实际的角色管理菜单下。

SET @zhyc_role_menu_id := (
    SELECT id
    FROM sys_menu
    WHERE tenant_id = 'zhyc-platform'
      AND menu_code = 'system-role'
    LIMIT 1
);

SET @zhyc_platform_admin_role_id := (
    SELECT id
    FROM sys_role
    WHERE tenant_id = 'zhyc-platform'
      AND role_code = 'platform-admin'
    LIMIT 1
);

UPDATE sys_menu
SET status = 'disabled'
WHERE tenant_id = 'zhyc-platform'
  AND menu_code = 'system-role-data-scope';

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_code, menu_name, menu_type, path, component, permission, sort_order, status)
SELECT
    24101,
    'zhyc-platform',
    @zhyc_role_menu_id,
    'system-role-data-scope-edit',
    '角色数据权限编辑',
    'button',
    NULL,
    NULL,
    'system:role:edit',
    6,
    'enabled'
WHERE @zhyc_role_menu_id IS NOT NULL
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
SELECT 'zhyc-platform', @zhyc_platform_admin_role_id, 24101
WHERE @zhyc_platform_admin_role_id IS NOT NULL;
