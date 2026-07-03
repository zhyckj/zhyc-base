-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 合并低代码中心生成相关菜单。
-- 说明：生成模板、代码生成、生成记录实际共用同一页面，侧边栏只保留“代码生成”一个入口。

UPDATE sys_menu
SET status = 'disabled',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = 'zhyc-platform'
  AND menu_code IN ('lowcode-template', 'lowcode-record');

UPDATE sys_menu
SET menu_name = '代码生成',
    path = '/lowcode/generator',
    component = 'lowcode/generator/index',
    permission = 'lowcode:generator:query',
    sort_order = 50,
    status = 'enabled',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = 'zhyc-platform'
  AND menu_code = 'lowcode-generator';
