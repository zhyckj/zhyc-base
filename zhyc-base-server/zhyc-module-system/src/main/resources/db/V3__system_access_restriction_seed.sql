-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 系统访问限制初始化数据。
-- 说明：默认只初始化本机、内网和平台管理员允许规则，不写入拒绝规则，避免本地联调被误阻断。

INSERT IGNORE INTO sys_access_restriction (
    tenant_id, restriction_type, rule_value, effect, start_at, end_at
)
VALUES
    ('zhyc-platform', 'ip', '127.0.0.1', 'allow', NULL, NULL),
    ('zhyc-platform', 'ip', '192.168.0.0/16', 'allow', NULL, NULL),
    ('zhyc-platform', 'account', 'admin', 'allow', NULL, NULL);
