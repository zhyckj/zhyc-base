-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 修复平台管理员角色被误停用的问题。
-- 适用场景：zhyc-platform 租户下 platform-admin 角色状态被改为 disabled，导致角色管理按钮或权限链路不可用。

START TRANSACTION;

UPDATE sys_role
SET status = 'enabled'
WHERE tenant_id = 'zhyc-platform'
  AND role_code = 'platform-admin';

SELECT id,
       tenant_id,
       role_code,
       name,
       data_scope,
       status,
       updated_at
FROM sys_role
WHERE tenant_id = 'zhyc-platform'
  AND role_code = 'platform-admin';

COMMIT;
