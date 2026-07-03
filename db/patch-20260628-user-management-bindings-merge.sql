-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 用户管理合并岗位和角色绑定补丁。
-- 说明：保留旧菜单记录但禁用入口，将岗位/角色绑定按钮权限挂到用户管理菜单下。
SET @zhyc_system_user_menu_id := (
    SELECT id
    FROM sys_menu
    WHERE tenant_id = 'zhyc-platform'
      AND menu_code = 'system-user'
    LIMIT 1
);

UPDATE sys_menu
SET status = 'disabled'
WHERE tenant_id = 'zhyc-platform'
  AND id IN (223, 224);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_code, menu_name, menu_type, path, component, permission, sort_order, status)
VALUES
    (22301, 'zhyc-platform', @zhyc_system_user_menu_id, 'system-user-post-bind', '绑定用户岗位', 'button', NULL, NULL, 'system:user:edit', 6, 'enabled'),
    (22401, 'zhyc-platform', @zhyc_system_user_menu_id, 'system-user-role-bind', '绑定用户角色', 'button', NULL, NULL, 'system:user:edit', 7, 'enabled')
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
  AND id IN (22301, 22401);
