-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

CREATE TABLE IF NOT EXISTS openapi_app (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT '应用编码',
    app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
    owner_user_id BIGINT NOT NULL COMMENT '应用负责人用户主键',
    auth_mode VARCHAR(32) NOT NULL COMMENT '鉴权方式',
    ip_whitelist TEXT COMMENT 'IP 白名单 JSON',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '应用状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_app_tenant_id (tenant_id, id),
    UNIQUE KEY uk_openapi_app_tenant_code (tenant_id, app_code),
    KEY idx_openapi_app_tenant_owner (tenant_id, owner_user_id),
    KEY idx_openapi_app_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台开发者应用表';

CREATE TABLE IF NOT EXISTS openapi_api_key (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT '开发者应用编码',
    access_key VARCHAR(128) NOT NULL COMMENT 'API 访问密钥',
    secret_cipher VARCHAR(512) NOT NULL COMMENT 'API Secret 密文',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT 'API Key 状态',
    expire_at DATETIME COMMENT '凭证过期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_api_key_access_key (access_key),
    KEY idx_openapi_api_key_tenant_app (tenant_id, app_code),
    KEY idx_openapi_api_key_tenant_status (tenant_id, status),
    CONSTRAINT fk_openapi_api_key_app FOREIGN KEY (tenant_id, app_code)
        REFERENCES openapi_app (tenant_id, app_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API Key 表';

CREATE TABLE IF NOT EXISTS openapi_api_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT '开发者应用编码',
    api_code VARCHAR(128) NOT NULL COMMENT 'API 业务编码',
    api_name VARCHAR(128) NOT NULL COMMENT 'API 名称',
    http_method VARCHAR(16) NOT NULL COMMENT 'HTTP 方法',
    path_pattern VARCHAR(256) NOT NULL COMMENT '请求路径匹配规则',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '授权状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_api_permission_app_api (tenant_id, app_code, api_code),
    KEY idx_openapi_api_permission_app_path (tenant_id, app_code, http_method, path_pattern),
    KEY idx_openapi_api_permission_app_status (tenant_id, app_code, status),
    CONSTRAINT fk_openapi_api_permission_app FOREIGN KEY (tenant_id, app_code)
        REFERENCES openapi_app (tenant_id, app_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API 权限授权表';

CREATE TABLE IF NOT EXISTS openapi_oauth_client (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT '开发者应用编码',
    client_id VARCHAR(128) NOT NULL COMMENT '认证中心 OAuth2 客户端 ID',
    allowed_scopes VARCHAR(512) NOT NULL COMMENT '允许的 OAuth2 授权范围',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '客户端映射状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_oauth_client_app_client (tenant_id, app_code, client_id),
    KEY idx_openapi_oauth_client_app_status (tenant_id, app_code, status),
    CONSTRAINT fk_openapi_oauth_client_app FOREIGN KEY (tenant_id, app_code)
        REFERENCES openapi_app (tenant_id, app_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 OAuth2 客户端映射表';

CREATE TABLE IF NOT EXISTS openapi_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    api_code VARCHAR(128) NOT NULL COMMENT 'API 业务编码',
    api_name VARCHAR(128) NOT NULL COMMENT 'API 名称',
    group_code VARCHAR(64) NOT NULL COMMENT 'API 分组编码',
    http_method VARCHAR(16) NOT NULL COMMENT 'HTTP 方法',
    path_pattern VARCHAR(256) NOT NULL COMMENT '请求路径匹配规则',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT 'API 目录状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_catalog_api_code (api_code),
    KEY idx_openapi_catalog_group_status (group_code, status),
    KEY idx_openapi_catalog_method_path (http_method, path_pattern)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API 目录表';

CREATE TABLE IF NOT EXISTS openapi_version (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    api_code VARCHAR(128) NOT NULL COMMENT 'API 业务编码',
    version VARCHAR(32) NOT NULL COMMENT 'API 版本号',
    backend_route VARCHAR(512) NOT NULL COMMENT '后端转发路由',
    request_schema JSON COMMENT '请求 JSON Schema',
    response_schema JSON COMMENT '响应 JSON Schema',
    status VARCHAR(32) NOT NULL DEFAULT 'published' COMMENT 'API 版本状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_version_api_version (api_code, version),
    KEY idx_openapi_version_api_status (api_code, status),
    CONSTRAINT fk_openapi_version_catalog FOREIGN KEY (api_code)
        REFERENCES openapi_catalog (api_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API 版本发布表';

CREATE TABLE IF NOT EXISTS openapi_signature_policy (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT '开发者应用编码',
    algorithm VARCHAR(32) NOT NULL COMMENT '签名算法，首期支持 HMAC_SHA256',
    timestamp_tolerance_seconds INT NOT NULL COMMENT '客户端时间戳允许偏差秒数',
    nonce_ttl_seconds INT NOT NULL COMMENT 'nonce 防重放有效期秒数',
    require_body_hash TINYINT NOT NULL DEFAULT 1 COMMENT '是否要求请求体参与摘要，1 是 0 否',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '签名策略状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_signature_policy_app (tenant_id, app_code),
    KEY idx_openapi_signature_policy_app_status (tenant_id, app_code, status),
    CONSTRAINT fk_openapi_signature_policy_app FOREIGN KEY (tenant_id, app_code)
        REFERENCES openapi_app (tenant_id, app_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API 签名策略表';

CREATE TABLE IF NOT EXISTS openapi_rate_limit_policy (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT '开发者应用编码',
    api_code VARCHAR(128) NOT NULL COMMENT 'API 业务编码',
    limit_count INT NOT NULL COMMENT '时间窗口内允许请求次数',
    window_seconds INT NOT NULL COMMENT '限流时间窗口秒数',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '限流策略状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_rate_limit_policy_app_api (tenant_id, app_code, api_code),
    KEY idx_openapi_rate_limit_policy_app_status (tenant_id, app_code, status),
    CONSTRAINT fk_openapi_rate_limit_policy_app FOREIGN KEY (tenant_id, app_code)
        REFERENCES openapi_app (tenant_id, app_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API 限流策略表';

CREATE TABLE IF NOT EXISTS openapi_rate_limit_counter (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT '开发者应用编码',
    api_code VARCHAR(128) NOT NULL COMMENT 'API 业务编码',
    window_seconds BIGINT NOT NULL COMMENT '限流窗口秒数',
    window_index BIGINT NOT NULL COMMENT '限流窗口序号',
    request_count INT NOT NULL DEFAULT 0 COMMENT '当前窗口请求次数',
    expires_at DATETIME NOT NULL COMMENT '窗口过期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_rate_limit_counter_window (
        tenant_id, app_code, api_code, window_seconds, window_index
    ),
    KEY idx_openapi_rate_limit_counter_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API 运行期限流计数表';

CREATE TABLE IF NOT EXISTS openapi_replay_nonce (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    app_key VARCHAR(128) NOT NULL COMMENT '开放平台应用标识',
    nonce_value VARCHAR(128) NOT NULL COMMENT '请求一次性随机串',
    expires_at DATETIME NOT NULL COMMENT 'nonce 过期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openapi_replay_nonce_app_nonce (app_key, nonce_value),
    KEY idx_openapi_replay_nonce_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API 防重放 nonce 表';

CREATE TABLE IF NOT EXISTS openapi_call_audit (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT '开发者应用编码',
    access_key VARCHAR(128) NOT NULL COMMENT 'API 访问密钥',
    api_code VARCHAR(128) NOT NULL COMMENT 'API 业务编码',
    http_method VARCHAR(16) NOT NULL COMMENT 'HTTP 方法',
    request_path VARCHAR(512) NOT NULL COMMENT '请求路径',
    response_status INT NOT NULL COMMENT 'HTTP 响应状态码',
    duration_ms BIGINT NOT NULL COMMENT '调用耗时毫秒',
    success TINYINT NOT NULL COMMENT '是否调用成功，1 是 0 否',
    error_code VARCHAR(64) COMMENT '错误编码',
    client_ip VARCHAR(64) NOT NULL COMMENT '客户端 IP',
    request_id VARCHAR(128) NOT NULL COMMENT '请求追踪 ID',
    called_at DATETIME NOT NULL COMMENT '调用时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_openapi_call_audit_app_called (tenant_id, app_code, called_at),
    KEY idx_openapi_call_audit_app_api (tenant_id, app_code, api_code),
    KEY idx_openapi_call_audit_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放平台 API 调用审计表';
