-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS sys_security_policy (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    policy_code VARCHAR(64) NOT NULL COMMENT '策略编码',
    policy_name VARCHAR(128) NOT NULL COMMENT '策略名称',
    protection_scope VARCHAR(64) NOT NULL COMMENT '防护范围',
    target_pattern VARCHAR(255) NOT NULL DEFAULT '*' COMMENT '目标匹配表达式',
    threshold_limit INT NOT NULL COMMENT '阈值次数',
    window_seconds INT NOT NULL COMMENT '统计窗口秒数',
    action VARCHAR(32) NOT NULL DEFAULT 'observe' COMMENT '触发动作',
    block_seconds INT DEFAULT NULL COMMENT '自动封禁秒数',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '策略状态',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_security_policy_code (tenant_id, policy_code),
    KEY idx_sys_security_policy_scope (tenant_id, protection_scope, status),
    KEY idx_sys_security_policy_status (tenant_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统安全防护策略表';

CREATE TABLE IF NOT EXISTS sys_security_event (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型',
    event_level VARCHAR(32) NOT NULL DEFAULT 'low' COMMENT '事件等级',
    source_ip VARCHAR(128) DEFAULT NULL COMMENT '来源 IP',
    user_id BIGINT DEFAULT NULL COMMENT '用户主键',
    username VARCHAR(64) DEFAULT NULL COMMENT '用户账号',
    request_path VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
    http_method VARCHAR(16) DEFAULT NULL COMMENT 'HTTP 方法',
    action VARCHAR(32) NOT NULL DEFAULT 'observe' COMMENT '处置动作',
    result VARCHAR(32) NOT NULL DEFAULT 'recorded' COMMENT '处置结果',
    message VARCHAR(500) DEFAULT NULL COMMENT '事件描述',
    occurred_at DATETIME NOT NULL COMMENT '发生时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sys_security_event_tenant_time (tenant_id, occurred_at),
    KEY idx_sys_security_event_ip_time (tenant_id, source_ip, occurred_at),
    KEY idx_sys_security_event_path_time (tenant_id, request_path, occurred_at),
    KEY idx_sys_security_event_type_level (tenant_id, event_type, event_level, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统安全事件表';

CREATE TABLE IF NOT EXISTS sys_security_ip_block (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    ip_value VARCHAR(128) NOT NULL COMMENT 'IP、IPv6 或 CIDR 规则值',
    block_type VARCHAR(32) NOT NULL DEFAULT 'manual' COMMENT '封禁类型',
    reason VARCHAR(500) DEFAULT NULL COMMENT '封禁原因',
    start_at DATETIME DEFAULT NULL COMMENT '封禁开始时间',
    end_at DATETIME DEFAULT NULL COMMENT '封禁结束时间',
    status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '封禁状态',
    created_by BIGINT DEFAULT NULL COMMENT '创建人主键',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_security_ip_block_rule (tenant_id, ip_value),
    KEY idx_sys_security_ip_block_active (tenant_id, status, start_at, end_at, deleted),
    KEY idx_sys_security_ip_block_type (tenant_id, block_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统安全 IP 封禁表';

INSERT IGNORE INTO sys_security_policy (
    tenant_id, policy_code, policy_name, protection_scope, target_pattern,
    threshold_limit, window_seconds, action, block_seconds, sort_order, status, remark
) VALUES
    ('zhyc-platform', 'admin-ip-minute', '后台单 IP 访问阈值', 'admin_api', '*',
     180, 60, 'observe', NULL, 10, 'enabled', '单个来源 IP 每分钟后台请求阈值，超过后进入安全事件排行'),
    ('zhyc-platform', 'admin-write-minute', '后台写接口阈值', 'admin_api', 'POST,PUT,PATCH,DELETE',
     60, 60, 'observe', NULL, 20, 'enabled', '后台写接口高频访问阈值'),
    ('zhyc-platform', 'login-fail-minute', '登录失败阈值', 'login', '/sys/login',
     10, 60, 'block', 180, 30, 'enabled', '登录失败高频请求自动封禁建议策略'),
    ('zhyc-platform', 'ai-high-cost-minute', 'AI 高成本调用阈值', 'ai', '*',
     60, 60, 'observe', NULL, 40, 'enabled', 'AI 高成本接口调用观察阈值');
