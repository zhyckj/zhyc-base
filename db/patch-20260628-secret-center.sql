-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- 修复 zhyc-base-v1 现有库缺少密钥中心表、菜单和角色授权的问题。
-- 本脚本不初始化任何真实密钥值，避免数据库口令明文进入脚本或版本库。

CREATE TABLE IF NOT EXISTS sys_secret (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    secret_code VARCHAR(64) NOT NULL COMMENT '密钥编码',
    secret_name VARCHAR(128) NOT NULL COMMENT '密钥名称',
    secret_kind VARCHAR(64) NOT NULL COMMENT '密钥类型',
    secret_cipher LONGTEXT NOT NULL COMMENT '密钥密文',
    secret_mask VARCHAR(255) NOT NULL COMMENT '脱敏展示值',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '密钥状态',
    expire_at DATETIME DEFAULT NULL COMMENT '过期时间',
    last_rotated_at DATETIME DEFAULT NULL COMMENT '最近轮换时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人主键',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_secret_tenant_code (tenant_id, secret_code),
    KEY idx_sys_secret_tenant_status (tenant_id, status),
    KEY idx_sys_secret_tenant_kind (tenant_id, secret_kind),
    KEY idx_sys_secret_tenant_expire (tenant_id, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统密钥表';

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_code, menu_name, menu_type, path, component, permission, sort_order, status)
VALUES
    (259, 'zhyc-platform', 200, 'system-secret', '密钥管理', 'menu', '/system/secrets', 'system/secret/index', 'system:secret:query', 240, 'enabled'),
    (25901, 'zhyc-platform', 259, 'system-secret-query', '密钥查看', 'button', NULL, NULL, 'system:secret:query', 1, 'enabled'),
    (25902, 'zhyc-platform', 259, 'system-secret-create', '密钥新增', 'button', NULL, NULL, 'system:secret:create', 2, 'enabled'),
    (25903, 'zhyc-platform', 259, 'system-secret-update', '密钥编辑', 'button', NULL, NULL, 'system:secret:update', 3, 'enabled'),
    (25904, 'zhyc-platform', 259, 'system-secret-delete', '密钥删除', 'button', NULL, NULL, 'system:secret:delete', 4, 'enabled'),
    (25905, 'zhyc-platform', 259, 'system-secret-enable', '密钥启用', 'button', NULL, NULL, 'system:secret:enable', 5, 'enabled'),
    (25906, 'zhyc-platform', 259, 'system-secret-disable', '密钥禁用', 'button', NULL, NULL, 'system:secret:disable', 6, 'enabled'),
    (25907, 'zhyc-platform', 259, 'system-secret-rotate', '密钥轮换', 'button', NULL, NULL, 'system:secret:rotate', 7, 'enabled'),
    (25908, 'zhyc-platform', 259, 'system-secret-copy-ref', '复制引用', 'button', NULL, NULL, 'system:secret:copy-ref', 8, 'enabled')
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
  AND (menu_code = 'system-secret' OR permission LIKE 'system:secret:%');
