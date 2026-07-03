-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- ZHYC 快速开发平台 MySQL 5.7 临时本地初始化脚本
-- 目标数据库：zhyc-base-v1（数据库需提前创建，执行时通过 mysql --database 指定）
-- 说明：管理员 password_hash 保留安全占位符，登录前需使用 Shiro 哈希工具物化真实哈希。
SET NAMES utf8mb4;

-- ============================================================
-- 1. 认证中心核心表
-- 来源：zhyc-base-server/zhyc-auth-server/src/main/resources/db/V1__auth_server_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
  id VARCHAR(100) NOT NULL COMMENT '注册客户端主键',
  client_id VARCHAR(100) NOT NULL COMMENT 'OAuth2 客户端标识',
  client_id_issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '客户端标识签发时间',
  client_secret VARCHAR(200) DEFAULT NULL COMMENT 'BCrypt 编码后的客户端密钥',
  client_secret_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT '客户端密钥过期时间',
  client_name VARCHAR(200) NOT NULL COMMENT '客户端名称',
  client_authentication_methods VARCHAR(1000) NOT NULL COMMENT '客户端认证方式集合',
  authorization_grant_types VARCHAR(1000) NOT NULL COMMENT '授权模式集合',
  redirect_uris VARCHAR(1000) DEFAULT NULL COMMENT '授权码回调地址集合',
  post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL COMMENT '登出回调地址集合',
  scopes VARCHAR(1000) NOT NULL COMMENT '授权范围集合',
  client_settings VARCHAR(2000) NOT NULL COMMENT '客户端设置 JSON',
  token_settings VARCHAR(2000) NOT NULL COMMENT '令牌设置 JSON',
  PRIMARY KEY (id),
  UNIQUE KEY uk_oauth2_registered_client_client_id (client_id)
) COMMENT='OAuth2 注册客户端';

CREATE TABLE IF NOT EXISTS oauth2_authorization (
  id VARCHAR(100) NOT NULL COMMENT '授权记录主键',
  registered_client_id VARCHAR(100) NOT NULL COMMENT '注册客户端主键',
  principal_name VARCHAR(200) NOT NULL COMMENT '授权主体名称',
  authorization_grant_type VARCHAR(100) NOT NULL COMMENT '授权模式',
  authorized_scopes VARCHAR(1000) DEFAULT NULL COMMENT '已授权范围集合',
  attributes BLOB DEFAULT NULL COMMENT '授权属性 JSON',
  state VARCHAR(500) DEFAULT NULL COMMENT '授权请求状态值',
  authorization_code_value BLOB DEFAULT NULL COMMENT '授权码密文',
  authorization_code_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT '授权码签发时间',
  authorization_code_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT '授权码过期时间',
  authorization_code_metadata BLOB DEFAULT NULL COMMENT '授权码元数据 JSON',
  access_token_value BLOB DEFAULT NULL COMMENT '访问令牌密文',
  access_token_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT '访问令牌签发时间',
  access_token_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT '访问令牌过期时间',
  access_token_metadata BLOB DEFAULT NULL COMMENT '访问令牌元数据 JSON',
  access_token_type VARCHAR(100) DEFAULT NULL COMMENT '访问令牌类型',
  access_token_scopes VARCHAR(1000) DEFAULT NULL COMMENT '访问令牌范围集合',
  oidc_id_token_value BLOB DEFAULT NULL COMMENT 'OIDC ID Token 密文',
  oidc_id_token_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT 'OIDC ID Token 签发时间',
  oidc_id_token_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'OIDC ID Token 过期时间',
  oidc_id_token_metadata BLOB DEFAULT NULL COMMENT 'OIDC ID Token 元数据 JSON',
  refresh_token_value BLOB DEFAULT NULL COMMENT '刷新令牌密文',
  refresh_token_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT '刷新令牌签发时间',
  refresh_token_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT '刷新令牌过期时间',
  refresh_token_metadata BLOB DEFAULT NULL COMMENT '刷新令牌元数据 JSON',
  user_code_value BLOB DEFAULT NULL COMMENT '设备授权用户码密文',
  user_code_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT '设备授权用户码签发时间',
  user_code_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT '设备授权用户码过期时间',
  user_code_metadata BLOB DEFAULT NULL COMMENT '设备授权用户码元数据 JSON',
  device_code_value BLOB DEFAULT NULL COMMENT '设备授权设备码密文',
  device_code_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT '设备授权设备码签发时间',
  device_code_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT '设备授权设备码过期时间',
  device_code_metadata BLOB DEFAULT NULL COMMENT '设备授权设备码元数据 JSON',
  PRIMARY KEY (id),
  KEY idx_oauth2_authorization_client_principal (registered_client_id, principal_name)
) COMMENT='OAuth2 授权记录';

CREATE TABLE IF NOT EXISTS oauth2_authorization_consent (
  registered_client_id VARCHAR(100) NOT NULL COMMENT '注册客户端主键',
  principal_name VARCHAR(200) NOT NULL COMMENT '授权主体名称',
  authorities VARCHAR(1000) NOT NULL COMMENT '已确认授权范围集合',
  PRIMARY KEY (registered_client_id, principal_name)
) COMMENT='OAuth2 授权确认';

-- ============================================================
-- 2. 系统与租户核心表
-- 来源：zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql
-- ============================================================
-- sys_tenant.tenant_id 是租户业务编码，不是所属租户字段；平台级租户管理查询需要绕过普通租户过滤。
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    tenant_name VARCHAR(128) NOT NULL COMMENT '租户名称',
    package_id BIGINT DEFAULT NULL COMMENT '当前租户套餐 ID',
    isolation_mode VARCHAR(32) NOT NULL DEFAULT 'TENANT_COLUMN' COMMENT '租户隔离模式',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '租户状态',
    contact_name VARCHAR(64) DEFAULT NULL COMMENT '租户联系人',
    contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    expire_at DATETIME DEFAULT NULL COMMENT '到期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_tenant_tenant_id (tenant_id),
    KEY idx_sys_tenant_package (package_id),
    KEY idx_sys_tenant_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户主表';

CREATE TABLE IF NOT EXISTS sys_tenant_package (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    package_code VARCHAR(64) NOT NULL COMMENT '套餐编码',
    package_name VARCHAR(128) NOT NULL COMMENT '套餐名称',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '套餐状态',
    max_user_count INT NOT NULL DEFAULT 0 COMMENT '最大用户数',
    max_storage_mb INT NOT NULL DEFAULT 0 COMMENT '最大存储容量 MB',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_tenant_package_code (package_code),
    KEY idx_sys_tenant_package_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户套餐表';

CREATE TABLE IF NOT EXISTS sys_tenant_package_module (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    package_id BIGINT NOT NULL COMMENT '租户套餐主键',
    module_code VARCHAR(64) NOT NULL COMMENT '模块编码',
    menu_code VARCHAR(64) DEFAULT NULL COMMENT '菜单编码',
    permission VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_tenant_package_module_resource (package_id, module_code, menu_code, permission),
    KEY idx_sys_tenant_package_module_package (package_id),
    KEY idx_sys_tenant_package_module_module (module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户套餐模块授权表';

CREATE TABLE IF NOT EXISTS sys_tenant_param (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    param_key VARCHAR(128) NOT NULL COMMENT '参数键',
    param_value VARCHAR(1000) DEFAULT NULL COMMENT '参数值',
    value_type VARCHAR(32) NOT NULL DEFAULT 'string' COMMENT '参数值类型',
    visible TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_tenant_param_key (tenant_id, param_key),
    KEY idx_sys_tenant_param_tenant_visible (tenant_id, visible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户参数表';

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    username VARCHAR(64) NOT NULL COMMENT '登录账号',
    nickname VARCHAR(128) DEFAULT NULL COMMENT '用户显示名称',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '用户状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_tenant_id (tenant_id, id),
    UNIQUE KEY uk_sys_user_tenant_username (tenant_id, username),
    KEY idx_sys_user_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    name VARCHAR(128) NOT NULL COMMENT '角色名称',
    data_scope VARCHAR(32) NOT NULL DEFAULT 'SELF' COMMENT '数据权限范围',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '角色状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_tenant_id (tenant_id, id),
    UNIQUE KEY uk_sys_role_tenant_code (tenant_id, role_code),
    KEY idx_sys_role_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    parent_id BIGINT DEFAULT NULL COMMENT '父级菜单主键',
    menu_code VARCHAR(64) NOT NULL COMMENT '菜单编码',
    menu_name VARCHAR(128) NOT NULL COMMENT '菜单名称',
    menu_type VARCHAR(32) NOT NULL COMMENT '菜单类型',
    path VARCHAR(255) DEFAULT NULL COMMENT '前端路由路径',
    component VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径',
    permission VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '菜单状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_tenant_id (tenant_id, id),
    UNIQUE KEY uk_sys_menu_tenant_code (tenant_id, menu_code),
    KEY idx_sys_menu_tenant_parent (tenant_id, parent_id),
    KEY idx_sys_menu_tenant_permission (tenant_id, permission)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

CREATE TABLE IF NOT EXISTS sys_org (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    parent_id BIGINT DEFAULT NULL COMMENT '父级组织主键',
    ancestors VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖级组织路径',
    org_code VARCHAR(64) NOT NULL COMMENT '组织编码',
    org_name VARCHAR(128) NOT NULL COMMENT '组织名称',
    leader_user_id BIGINT DEFAULT NULL COMMENT '负责人用户主键',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '组织状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_org_tenant_id (tenant_id, id),
    UNIQUE KEY uk_sys_org_tenant_code (tenant_id, org_code),
    KEY idx_sys_org_tenant_parent (tenant_id, parent_id),
    KEY idx_sys_org_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统组织机构表';

CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    org_id BIGINT DEFAULT NULL COMMENT '所属组织主键',
    post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
    post_name VARCHAR(128) NOT NULL COMMENT '岗位名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '岗位状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_post_tenant_id (tenant_id, id),
    UNIQUE KEY uk_sys_post_tenant_code (tenant_id, post_code),
    KEY idx_sys_post_tenant_org (tenant_id, org_id),
    KEY idx_sys_post_tenant_status (tenant_id, status),
    CONSTRAINT fk_sys_post_org FOREIGN KEY (tenant_id, org_id) REFERENCES sys_org (tenant_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统岗位表';

CREATE TABLE IF NOT EXISTS sys_user_post (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    user_id BIGINT NOT NULL COMMENT '用户主键',
    post_id BIGINT NOT NULL COMMENT '岗位主键',
    primary_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主岗位',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_post_tenant_user_post (tenant_id, user_id, post_id),
    KEY idx_sys_user_post_tenant_post (tenant_id, post_id),
    KEY idx_sys_user_post_tenant_primary (tenant_id, user_id, primary_flag),
    CONSTRAINT fk_sys_user_post_user FOREIGN KEY (tenant_id, user_id) REFERENCES sys_user (tenant_id, id),
    CONSTRAINT fk_sys_user_post_post FOREIGN KEY (tenant_id, post_id) REFERENCES sys_post (tenant_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户岗位关联表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    user_id BIGINT NOT NULL COMMENT '用户主键',
    role_id BIGINT NOT NULL COMMENT '角色主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_tenant_user_role (tenant_id, user_id, role_id),
    KEY idx_sys_user_role_tenant_role (tenant_id, role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (tenant_id, user_id) REFERENCES sys_user (tenant_id, id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (tenant_id, role_id) REFERENCES sys_role (tenant_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_admin_scope (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    user_id BIGINT NOT NULL COMMENT '管理员用户主键',
    scope_type VARCHAR(32) NOT NULL COMMENT '范围类型',
    scope_ref_code VARCHAR(128) NOT NULL COMMENT '范围引用编码',
    scope_name VARCHAR(128) DEFAULT NULL COMMENT '范围展示名称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_admin_scope (tenant_id, user_id, scope_type, scope_ref_code),
    KEY idx_sys_admin_scope_user (tenant_id, user_id),
    KEY idx_sys_admin_scope_ref (tenant_id, scope_type, scope_ref_code),
    CONSTRAINT fk_sys_admin_scope_user FOREIGN KEY (tenant_id, user_id) REFERENCES sys_user (tenant_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员管理范围表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    role_id BIGINT NOT NULL COMMENT '角色主键',
    menu_id BIGINT NOT NULL COMMENT '菜单主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu_tenant_role_menu (tenant_id, role_id, menu_id),
    KEY idx_sys_role_menu_tenant_menu (tenant_id, menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (tenant_id, role_id) REFERENCES sys_role (tenant_id, id),
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (tenant_id, menu_id) REFERENCES sys_menu (tenant_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS sys_role_data_scope (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    role_id BIGINT NOT NULL COMMENT '角色主键',
    org_id BIGINT NOT NULL COMMENT '授权组织主键',
    scope_type VARCHAR(32) NOT NULL DEFAULT 'org' COMMENT '范围类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_data_scope (tenant_id, role_id, org_id, scope_type),
    KEY idx_sys_role_data_scope_org (tenant_id, org_id),
    CONSTRAINT fk_sys_role_data_scope_role FOREIGN KEY (tenant_id, role_id) REFERENCES sys_role (tenant_id, id),
    CONSTRAINT fk_sys_role_data_scope_org FOREIGN KEY (tenant_id, org_id) REFERENCES sys_org (tenant_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色自定义数据权限表';

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    user_id BIGINT DEFAULT NULL COMMENT '登录用户主键',
    username VARCHAR(64) DEFAULT NULL COMMENT '登录账号',
    login_type VARCHAR(32) NOT NULL COMMENT '登录方式',
    result VARCHAR(32) NOT NULL COMMENT '登录结果',
    client_ip VARCHAR(64) DEFAULT NULL COMMENT '客户端 IP',
    user_agent VARCHAR(512) DEFAULT NULL COMMENT '浏览器或客户端 User-Agent',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sys_login_log_tenant_created (tenant_id, created_at),
    KEY idx_sys_login_log_tenant_user (tenant_id, user_id),
    KEY idx_sys_login_log_tenant_result (tenant_id, result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统登录日志表';

CREATE TABLE IF NOT EXISTS sys_exception_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    trace_id VARCHAR(128) DEFAULT NULL COMMENT '链路追踪编号',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户主键',
    username VARCHAR(64) DEFAULT NULL COMMENT '操作账号',
    request_uri VARCHAR(255) NOT NULL COMMENT '请求地址',
    request_method VARCHAR(16) NOT NULL COMMENT '请求方法',
    exception_name VARCHAR(255) NOT NULL COMMENT '异常类名',
    message TEXT COMMENT '异常消息',
    stack_trace LONGTEXT COMMENT '异常堆栈',
    client_ip VARCHAR(64) DEFAULT NULL COMMENT '客户端 IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sys_exception_log_tenant_created (tenant_id, created_at),
    KEY idx_sys_exception_log_tenant_trace (tenant_id, trace_id),
    KEY idx_sys_exception_log_tenant_exception (tenant_id, exception_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统异常日志表';

CREATE TABLE IF NOT EXISTS sys_permission_audit (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    operator_id BIGINT DEFAULT NULL COMMENT '操作者用户主键',
    target_type VARCHAR(64) NOT NULL COMMENT '目标类型',
    target_id VARCHAR(128) NOT NULL COMMENT '目标业务标识',
    before_value TEXT COMMENT '变更前内容',
    after_value TEXT COMMENT '变更后内容',
    change_type VARCHAR(64) NOT NULL COMMENT '变更类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sys_permission_audit_tenant_created (tenant_id, created_at),
    KEY idx_sys_permission_audit_tenant_operator (tenant_id, operator_id),
    KEY idx_sys_permission_audit_tenant_target (tenant_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限变更审计表';

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户主键',
    username VARCHAR(64) DEFAULT NULL COMMENT '操作账号',
    action VARCHAR(128) NOT NULL COMMENT '操作动作',
    target_type VARCHAR(64) DEFAULT NULL COMMENT '目标类型',
    target_id VARCHAR(128) DEFAULT NULL COMMENT '目标标识',
    result VARCHAR(32) NOT NULL COMMENT '操作结果',
    client_ip VARCHAR(64) DEFAULT NULL COMMENT '客户端 IP',
    detail TEXT COMMENT '操作详情',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sys_audit_log_tenant_created (tenant_id, created_at),
    KEY idx_sys_audit_log_tenant_user (tenant_id, user_id),
    KEY idx_sys_audit_log_tenant_action (tenant_id, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统审计日志表';

CREATE TABLE IF NOT EXISTS sys_param (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    param_key VARCHAR(128) NOT NULL COMMENT '参数键',
    param_value TEXT COMMENT '参数值',
    value_type VARCHAR(32) NOT NULL DEFAULT 'string' COMMENT '参数值类型',
    system_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统内置参数',
    editable TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许后台编辑',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_param_tenant_key (tenant_id, param_key),
    KEY idx_sys_param_tenant_system (tenant_id, system_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';

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

CREATE TABLE IF NOT EXISTS sys_access_restriction (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    restriction_type VARCHAR(32) NOT NULL COMMENT '限制类型',
    rule_value VARCHAR(255) NOT NULL COMMENT '规则值',
    effect VARCHAR(32) NOT NULL COMMENT '生效动作',
    start_at DATETIME DEFAULT NULL COMMENT '生效开始时间',
    end_at DATETIME DEFAULT NULL COMMENT '生效结束时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_access_restriction_rule (tenant_id, restriction_type, rule_value),
    KEY idx_sys_access_restriction_active (tenant_id, restriction_type, start_at, end_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统访问限制表';

CREATE TABLE IF NOT EXISTS sys_password_policy (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    policy_code VARCHAR(64) NOT NULL COMMENT '策略编码',
    policy_name VARCHAR(128) NOT NULL COMMENT '策略名称',
    min_length INT NOT NULL DEFAULT 8 COMMENT '密码最小长度',
    require_uppercase TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否要求大写字母',
    require_lowercase TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否要求小写字母',
    require_digit TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否要求数字',
    require_special TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否要求特殊字符',
    expire_days INT NOT NULL DEFAULT 90 COMMENT '密码有效天数',
    history_count INT NOT NULL DEFAULT 3 COMMENT '历史密码记忆次数',
    max_retry_count INT NOT NULL DEFAULT 5 COMMENT '最大连续失败次数',
    lock_minutes INT NOT NULL DEFAULT 30 COMMENT '账号锁定分钟数',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_password_policy_tenant_code (tenant_id, policy_code),
    KEY idx_sys_password_policy_tenant_enabled (tenant_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统密码策略表';

CREATE TABLE IF NOT EXISTS sys_code_rule (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    rule_code VARCHAR(64) NOT NULL COMMENT '编码规则编码',
    rule_name VARCHAR(128) NOT NULL COMMENT '编码规则名称',
    prefix VARCHAR(32) DEFAULT NULL COMMENT '编码前缀',
    date_pattern VARCHAR(32) DEFAULT NULL COMMENT '日期格式',
    sequence_length INT NOT NULL DEFAULT 5 COMMENT '序列号长度',
    current_value INT NOT NULL DEFAULT 0 COMMENT '当前序列值',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_code_rule_tenant_code (tenant_id, rule_code),
    KEY idx_sys_code_rule_tenant_enabled (tenant_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统编码规则表';

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码',
    dict_name VARCHAR(128) NOT NULL COMMENT '字典名称',
    system_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统内置字典',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '字典状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dict_type_tenant_code (tenant_id, dict_code),
    KEY idx_sys_dict_type_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码',
    item_label VARCHAR(128) NOT NULL COMMENT '字典项显示标签',
    item_value VARCHAR(128) NOT NULL COMMENT '字典项实际值',
    item_color VARCHAR(32) DEFAULT NULL COMMENT '字典项前端展示颜色',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '字典项状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dict_item_tenant_code_value (tenant_id, dict_code, item_value),
    KEY idx_sys_dict_item_tenant_code_sort (tenant_id, dict_code, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典项表';

CREATE TABLE IF NOT EXISTS sys_module (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    module_code VARCHAR(64) NOT NULL COMMENT '模块编码',
    module_name VARCHAR(128) NOT NULL COMMENT '模块名称',
    version VARCHAR(32) NOT NULL COMMENT '模块版本',
    module_type VARCHAR(32) NOT NULL COMMENT '模块类型',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_module_code (module_code),
    KEY idx_sys_module_type_enabled (module_type, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统模块表';

CREATE TABLE IF NOT EXISTS sys_module_dependency (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    module_code VARCHAR(64) NOT NULL COMMENT '模块编码',
    depends_on_code VARCHAR(64) NOT NULL COMMENT '依赖模块编码',
    required_version VARCHAR(32) DEFAULT NULL COMMENT '依赖模块要求版本',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_module_dep (module_code, depends_on_code),
    KEY idx_sys_module_dep_depends_on (depends_on_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统模块依赖表';

CREATE TABLE IF NOT EXISTS sys_module_resource (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    module_code VARCHAR(64) NOT NULL COMMENT '模块编码',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型',
    resource_code VARCHAR(128) NOT NULL COMMENT '资源编码',
    resource_path VARCHAR(255) DEFAULT NULL COMMENT '资源路径或权限标识',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_module_resource (module_code, resource_type, resource_code),
    KEY idx_sys_module_resource_type (resource_type, resource_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统模块资源表';

-- ============================================================
-- 3. 低代码元数据表
-- 来源：zhyc-base-server/zhyc-module-lowcode/src/main/resources/db/V1__lowcode_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS lowcode_data_source (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    code VARCHAR(64) NOT NULL COMMENT '数据源编码',
    name VARCHAR(128) NOT NULL COMMENT '数据源名称',
    dialect VARCHAR(32) NOT NULL COMMENT '数据库类型',
    jdbc_url VARCHAR(512) NOT NULL COMMENT 'JDBC 连接地址',
    username VARCHAR(128) NOT NULL COMMENT '数据库用户名',
    password_secret_ref VARCHAR(255) DEFAULT NULL COMMENT '数据库口令密钥引用',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_ds_tenant_code (tenant_id, code),
    KEY idx_lowcode_ds_tenant_enabled (tenant_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码数据源表';

CREATE TABLE IF NOT EXISTS lowcode_table_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    data_source_id BIGINT DEFAULT NULL COMMENT '数据源主键',
    code VARCHAR(64) NOT NULL COMMENT '模型编码',
    name VARCHAR(128) NOT NULL COMMENT '模型名称',
    table_name VARCHAR(128) NOT NULL COMMENT '物理表名',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '模型状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_table_tenant_code (tenant_id, code),
    UNIQUE KEY uk_lowcode_table_tenant_table (tenant_id, table_name),
    KEY idx_lowcode_table_tenant_status (tenant_id, status),
    KEY idx_lowcode_table_data_source (data_source_id),
    CONSTRAINT fk_lowcode_table_data_source FOREIGN KEY (data_source_id) REFERENCES lowcode_data_source (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码表模型表';

CREATE TABLE IF NOT EXISTS lowcode_column_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    table_model_id BIGINT NOT NULL COMMENT '表模型主键',
    code VARCHAR(64) NOT NULL COMMENT '字段编码',
    name VARCHAR(128) NOT NULL COMMENT '字段名称',
    field_type VARCHAR(32) NOT NULL COMMENT '平台统一字段类型',
    length_value INT DEFAULT NULL COMMENT '字段长度或数值精度',
    scale_value INT DEFAULT NULL COMMENT '小数位数',
    required TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否必填',
    primary_key_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主键',
    auto_increment_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自增',
    list_visible TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否列表展示',
    form_visible TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否表单展示',
    queryable TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否查询条件',
    dict_code VARCHAR(64) DEFAULT NULL COMMENT '绑定的系统字典编码',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    comment VARCHAR(255) DEFAULT NULL COMMENT '字段备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_column_table_code (table_model_id, code),
    KEY idx_lowcode_column_table_sort (table_model_id, sort_order),
    CONSTRAINT fk_lowcode_column_table FOREIGN KEY (table_model_id) REFERENCES lowcode_table_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码字段模型表';

CREATE TABLE IF NOT EXISTS lowcode_table_relation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    main_table_id BIGINT NOT NULL COMMENT '主表模型主键',
    sub_table_id BIGINT NOT NULL COMMENT '子表模型主键',
    relation_type VARCHAR(32) NOT NULL COMMENT '关系类型',
    join_column VARCHAR(64) NOT NULL COMMENT '主表关联字段编码',
    ref_column VARCHAR(64) NOT NULL COMMENT '子表引用字段编码',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_relation_tenant_tables (tenant_id, main_table_id, sub_table_id, relation_type),
    KEY idx_lowcode_relation_tenant_main (tenant_id, main_table_id),
    KEY idx_lowcode_relation_tenant_sub (tenant_id, sub_table_id),
    CONSTRAINT fk_lowcode_relation_main_table FOREIGN KEY (main_table_id) REFERENCES lowcode_table_model (id),
    CONSTRAINT fk_lowcode_relation_sub_table FOREIGN KEY (sub_table_id) REFERENCES lowcode_table_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码表关系模型表';

CREATE TABLE IF NOT EXISTS lowcode_page_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    table_model_id BIGINT NOT NULL COMMENT '表模型主键',
    page_type VARCHAR(32) NOT NULL COMMENT '页面类型',
    route_path VARCHAR(255) NOT NULL COMMENT '前端路由路径',
    component_path VARCHAR(255) NOT NULL COMMENT '组件路径',
    layout_type VARCHAR(64) NOT NULL COMMENT '页面布局类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lowcode_page_tenant_table_type (tenant_id, table_model_id, page_type),
    KEY idx_lowcode_page_tenant_table (tenant_id, table_model_id),
    CONSTRAINT fk_lowcode_page_table FOREIGN KEY (table_model_id) REFERENCES lowcode_table_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码页面模型表';

CREATE TABLE IF NOT EXISTS lowcode_generation_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    table_model_code VARCHAR(64) NOT NULL COMMENT '表模型编码',
    target VARCHAR(32) NOT NULL COMMENT '生成目标端',
    module_name VARCHAR(128) NOT NULL COMMENT '业务模块名称',
    entity_name VARCHAR(128) NOT NULL COMMENT '业务实体名称',
    overwrite_strategy VARCHAR(32) NOT NULL COMMENT '生成文件覆盖策略',
    file_count INT NOT NULL DEFAULT 0 COMMENT '生成文件数量',
    file_manifest_json TEXT DEFAULT NULL COMMENT '生成文件清单 JSON',
    status VARCHAR(32) NOT NULL COMMENT '生成状态',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_lowcode_gen_tenant_model (tenant_id, table_model_code),
    KEY idx_lowcode_gen_tenant_status (tenant_id, status),
    KEY idx_lowcode_gen_target (target)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码代码生成记录表';

CREATE TABLE IF NOT EXISTS lc_generation_file (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    record_id BIGINT NOT NULL COMMENT '生成记录主键',
    template_code VARCHAR(128) NOT NULL COMMENT '模板编码',
    file_path VARCHAR(500) NOT NULL COMMENT '生成文件路径',
    file_type VARCHAR(32) NOT NULL COMMENT '生成文件类型',
    overwrite_mode VARCHAR(32) NOT NULL COMMENT '覆盖模式',
    content_hash VARCHAR(128) NOT NULL COMMENT '文件内容哈希',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_lc_gen_file_tenant_record (tenant_id, record_id),
    KEY idx_lc_gen_file_hash (content_hash),
    CONSTRAINT fk_lc_gen_file_record FOREIGN KEY (record_id) REFERENCES lowcode_generation_record (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码生成文件明细表';

-- ============================================================
-- 4. 工作流运行表
-- 来源：zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS wf_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    category_code VARCHAR(64) NOT NULL COMMENT '流程分类编码',
    category_name VARCHAR(128) NOT NULL COMMENT '流程分类名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '分类状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_category_tenant_code (tenant_id, category_code),
    KEY idx_wf_category_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流分类表';

CREATE TABLE IF NOT EXISTS wf_process_model (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    model_code VARCHAR(128) NOT NULL COMMENT '流程模型编码',
    model_name VARCHAR(128) NOT NULL COMMENT '流程模型名称',
    category_id BIGINT NULL COMMENT '流程分类 ID',
    flowable_model_id VARCHAR(128) NOT NULL COMMENT 'Flowable 模型 ID',
    bpmn_xml MEDIUMTEXT NULL COMMENT 'BPMN XML 设计稿，用于保存在线流程编排草稿',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '流程模型状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_model_tenant_code (tenant_id, model_code),
    KEY idx_wf_model_tenant_category (tenant_id, category_id),
    KEY idx_wf_model_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程模型表';

CREATE TABLE IF NOT EXISTS wf_form_binding (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    process_key VARCHAR(128) NOT NULL COMMENT '流程定义 key',
    business_module VARCHAR(64) NOT NULL COMMENT '业务模块编码',
    business_table VARCHAR(128) NOT NULL COMMENT '业务表名',
    form_route VARCHAR(255) NOT NULL COMMENT '后台表单路由',
    mobile_route VARCHAR(255) NULL COMMENT '移动端表单路由',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '绑定状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_form_binding_tenant_process (tenant_id, process_key),
    KEY idx_wf_form_binding_tenant_module (tenant_id, business_module),
    KEY idx_wf_form_binding_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表单绑定表';

CREATE TABLE IF NOT EXISTS wf_process_definition (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    process_key VARCHAR(128) NOT NULL COMMENT '流程定义 key',
    process_name VARCHAR(128) NOT NULL COMMENT '流程定义名称',
    version INT NOT NULL COMMENT '流程定义版本号',
    deployment_id VARCHAR(128) NOT NULL COMMENT 'Flowable 部署 ID',
    status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '流程定义状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_definition_tenant_key_version (tenant_id, process_key, version),
    KEY idx_wf_definition_tenant_key (tenant_id, process_key),
    KEY idx_wf_definition_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程定义表';

CREATE TABLE IF NOT EXISTS wf_process_instance (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    process_instance_id VARCHAR(128) NOT NULL COMMENT '流程实例 ID',
    process_key VARCHAR(128) NOT NULL COMMENT '流程定义 key',
    business_key VARCHAR(128) NOT NULL COMMENT '业务对象唯一标识',
    starter_user_id BIGINT NULL COMMENT '流程发起人用户 ID',
    status VARCHAR(32) NOT NULL COMMENT '流程实例状态',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '流程启动时间',
    ended_at DATETIME NULL COMMENT '流程结束时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_pi_tenant_instance (tenant_id, process_instance_id),
    KEY idx_wf_pi_tenant_business (tenant_id, business_key),
    KEY idx_wf_pi_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程实例表';

CREATE TABLE IF NOT EXISTS wf_task (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    task_id VARCHAR(128) NOT NULL COMMENT '任务 ID',
    process_instance_id VARCHAR(128) NOT NULL COMMENT '流程实例 ID',
    task_name VARCHAR(128) NOT NULL COMMENT '任务名称',
    business_key VARCHAR(128) NOT NULL COMMENT '业务对象唯一标识',
    assignee_user_id BIGINT NOT NULL COMMENT '任务处理人用户 ID',
    status VARCHAR(32) NOT NULL COMMENT '任务状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务创建时间',
    completed_at DATETIME NULL COMMENT '任务完成时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wf_task_tenant_task (tenant_id, task_id),
    KEY idx_wf_task_tenant_assignee (tenant_id, assignee_user_id, status),
    KEY idx_wf_task_tenant_instance (tenant_id, process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流任务表';

CREATE TABLE IF NOT EXISTS wf_approval_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    task_id VARCHAR(128) NOT NULL COMMENT '任务 ID',
    process_instance_id VARCHAR(128) NULL COMMENT '流程实例 ID',
    operator_user_id BIGINT NOT NULL COMMENT '操作用户 ID',
    action VARCHAR(32) NOT NULL COMMENT '审批动作',
    approval_comment VARCHAR(1000) NULL COMMENT '审批意见',
    operated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_wf_record_tenant_task (tenant_id, task_id),
    KEY idx_wf_record_tenant_instance (tenant_id, process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流审批记录表';

CREATE TABLE IF NOT EXISTS wf_cc_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    process_instance_id VARCHAR(128) NOT NULL COMMENT '流程实例 ID',
    receiver_id BIGINT NOT NULL COMMENT '抄送接收人用户 ID',
    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '阅读标识，0 未读，1 已读',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_wf_cc_tenant_receiver (tenant_id, receiver_id, read_flag),
    KEY idx_wf_cc_tenant_instance (tenant_id, process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流抄送记录表';

-- ============================================================
-- 5. Flowable 引擎运行表
-- 来源：zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V2__flowable_engine_mysql.sql
-- ============================================================
-- Flowable 引擎运行表结构。
-- 说明：由 CompareLocalDatabaseInit 从本地开发库 SHOW CREATE TABLE 导出，只包含 DDL，不包含流程实例、变量、任务或历史数据。
-- 说明：本脚本不包含 AI 模型供应商密钥、系统密钥、用户密码或 OAuth2 客户端密钥。

CREATE TABLE IF NOT EXISTS act_evt_log (
  LOG_NR_ BIGINT NOT NULL AUTO_INCREMENT,
  TYPE_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  TIME_STAMP_ timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  USER_ID_ varchar(255) DEFAULT NULL,
  DATA_ longblob,
  LOCK_OWNER_ varchar(255) DEFAULT NULL,
  LOCK_TIME_ timestamp(3) NULL DEFAULT NULL,
  IS_PROCESSED_ TINYINT DEFAULT '0',
  PRIMARY KEY (LOG_NR_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_evt_log';

CREATE TABLE IF NOT EXISTS act_re_deployment (
  ID_ varchar(64) NOT NULL,
  NAME_ varchar(255) DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  KEY_ varchar(255) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  DEPLOY_TIME_ timestamp(3) NULL DEFAULT NULL,
  DERIVED_FROM_ varchar(64) DEFAULT NULL,
  DERIVED_FROM_ROOT_ varchar(64) DEFAULT NULL,
  PARENT_DEPLOYMENT_ID_ varchar(255) DEFAULT NULL,
  ENGINE_VERSION_ varchar(255) DEFAULT NULL,
  PRIMARY KEY (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_re_deployment';

CREATE TABLE IF NOT EXISTS act_ge_bytearray (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  NAME_ varchar(255) DEFAULT NULL,
  DEPLOYMENT_ID_ varchar(64) DEFAULT NULL,
  BYTES_ longblob,
  GENERATED_ TINYINT DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_BYTEAR_DEPL (DEPLOYMENT_ID_),
  CONSTRAINT ACT_FK_BYTEARR_DEPL FOREIGN KEY (DEPLOYMENT_ID_) REFERENCES act_re_deployment (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ge_bytearray';

CREATE TABLE IF NOT EXISTS act_ge_property (
  NAME_ varchar(64) NOT NULL,
  VALUE_ varchar(300) DEFAULT NULL,
  REV_ INT DEFAULT NULL,
  PRIMARY KEY (NAME_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ge_property';

CREATE TABLE IF NOT EXISTS act_hi_actinst (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT '1',
  PROC_DEF_ID_ varchar(64) NOT NULL,
  PROC_INST_ID_ varchar(64) NOT NULL,
  EXECUTION_ID_ varchar(64) NOT NULL,
  ACT_ID_ varchar(255) NOT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  CALL_PROC_INST_ID_ varchar(64) DEFAULT NULL,
  ACT_NAME_ varchar(255) DEFAULT NULL,
  ACT_TYPE_ varchar(255) NOT NULL,
  ASSIGNEE_ varchar(255) DEFAULT NULL,
  COMPLETED_BY_ varchar(255) DEFAULT NULL,
  START_TIME_ datetime(3) NOT NULL,
  END_TIME_ datetime(3) DEFAULT NULL,
  TRANSACTION_ORDER_ INT DEFAULT NULL,
  DURATION_ BIGINT DEFAULT NULL,
  DELETE_REASON_ varchar(4000) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_HI_ACT_INST_START (START_TIME_),
  KEY ACT_IDX_HI_ACT_INST_END (END_TIME_),
  KEY ACT_IDX_HI_ACT_INST_PROCINST (PROC_INST_ID_,ACT_ID_),
  KEY ACT_IDX_HI_ACT_INST_EXEC (EXECUTION_ID_,ACT_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_actinst';

CREATE TABLE IF NOT EXISTS act_hi_attachment (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  USER_ID_ varchar(255) DEFAULT NULL,
  NAME_ varchar(255) DEFAULT NULL,
  DESCRIPTION_ varchar(4000) DEFAULT NULL,
  TYPE_ varchar(255) DEFAULT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  URL_ varchar(4000) DEFAULT NULL,
  CONTENT_ID_ varchar(64) DEFAULT NULL,
  TIME_ datetime(3) DEFAULT NULL,
  PRIMARY KEY (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_attachment';

CREATE TABLE IF NOT EXISTS act_hi_comment (
  ID_ varchar(64) NOT NULL,
  TYPE_ varchar(255) DEFAULT NULL,
  TIME_ datetime(3) NOT NULL,
  USER_ID_ varchar(255) DEFAULT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  ACTION_ varchar(255) DEFAULT NULL,
  MESSAGE_ varchar(4000) DEFAULT NULL,
  FULL_MSG_ longblob,
  PRIMARY KEY (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_comment';

CREATE TABLE IF NOT EXISTS act_hi_detail (
  ID_ varchar(64) NOT NULL,
  TYPE_ varchar(255) NOT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  ACT_INST_ID_ varchar(64) DEFAULT NULL,
  NAME_ varchar(255) NOT NULL,
  VAR_TYPE_ varchar(255) DEFAULT NULL,
  REV_ INT DEFAULT NULL,
  TIME_ datetime(3) NOT NULL,
  BYTEARRAY_ID_ varchar(64) DEFAULT NULL,
  DOUBLE_ double DEFAULT NULL,
  LONG_ BIGINT DEFAULT NULL,
  TEXT_ varchar(4000) DEFAULT NULL,
  TEXT2_ varchar(4000) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_HI_DETAIL_PROC_INST (PROC_INST_ID_),
  KEY ACT_IDX_HI_DETAIL_ACT_INST (ACT_INST_ID_),
  KEY ACT_IDX_HI_DETAIL_TIME (TIME_),
  KEY ACT_IDX_HI_DETAIL_NAME (NAME_),
  KEY ACT_IDX_HI_DETAIL_TASK_ID (TASK_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_detail';

CREATE TABLE IF NOT EXISTS act_hi_entitylink (
  ID_ varchar(64) NOT NULL,
  LINK_TYPE_ varchar(255) DEFAULT NULL,
  CREATE_TIME_ datetime(3) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  PARENT_ELEMENT_ID_ varchar(255) DEFAULT NULL,
  REF_SCOPE_ID_ varchar(255) DEFAULT NULL,
  REF_SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  REF_SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  ROOT_SCOPE_ID_ varchar(255) DEFAULT NULL,
  ROOT_SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  HIERARCHY_TYPE_ varchar(255) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_HI_ENT_LNK_SCOPE (SCOPE_ID_,SCOPE_TYPE_,LINK_TYPE_),
  KEY ACT_IDX_HI_ENT_LNK_REF_SCOPE (REF_SCOPE_ID_,REF_SCOPE_TYPE_,LINK_TYPE_),
  KEY ACT_IDX_HI_ENT_LNK_ROOT_SCOPE (ROOT_SCOPE_ID_,ROOT_SCOPE_TYPE_,LINK_TYPE_),
  KEY ACT_IDX_HI_ENT_LNK_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_,LINK_TYPE_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_entitylink';

CREATE TABLE IF NOT EXISTS act_hi_identitylink (
  ID_ varchar(64) NOT NULL,
  GROUP_ID_ varchar(255) DEFAULT NULL,
  TYPE_ varchar(255) DEFAULT NULL,
  USER_ID_ varchar(255) DEFAULT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  CREATE_TIME_ datetime(3) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_HI_IDENT_LNK_USER (USER_ID_),
  KEY ACT_IDX_HI_IDENT_LNK_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_HI_IDENT_LNK_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_HI_IDENT_LNK_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_HI_IDENT_LNK_TASK (TASK_ID_),
  KEY ACT_IDX_HI_IDENT_LNK_PROCINST (PROC_INST_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_identitylink';

CREATE TABLE IF NOT EXISTS act_hi_procinst (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT '1',
  PROC_INST_ID_ varchar(64) NOT NULL,
  BUSINESS_KEY_ varchar(255) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) NOT NULL,
  START_TIME_ datetime(3) NOT NULL,
  END_TIME_ datetime(3) DEFAULT NULL,
  DURATION_ BIGINT DEFAULT NULL,
  START_USER_ID_ varchar(255) DEFAULT NULL,
  START_ACT_ID_ varchar(255) DEFAULT NULL,
  END_ACT_ID_ varchar(255) DEFAULT NULL,
  SUPER_PROCESS_INSTANCE_ID_ varchar(64) DEFAULT NULL,
  DELETE_REASON_ varchar(4000) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  NAME_ varchar(255) DEFAULT NULL,
  CALLBACK_ID_ varchar(255) DEFAULT NULL,
  CALLBACK_TYPE_ varchar(255) DEFAULT NULL,
  REFERENCE_ID_ varchar(255) DEFAULT NULL,
  REFERENCE_TYPE_ varchar(255) DEFAULT NULL,
  PROPAGATED_STAGE_INST_ID_ varchar(255) DEFAULT NULL,
  BUSINESS_STATUS_ varchar(255) DEFAULT NULL,
  END_USER_ID_ varchar(255) DEFAULT NULL,
  STATE_ varchar(255) DEFAULT NULL,
  PRIMARY KEY (ID_),
  UNIQUE KEY PROC_INST_ID_ (PROC_INST_ID_),
  KEY ACT_IDX_HI_PRO_INST_END (END_TIME_),
  KEY ACT_IDX_HI_PRO_I_BUSKEY (BUSINESS_KEY_),
  KEY ACT_IDX_HI_PRO_SUPER_PROCINST (SUPER_PROCESS_INSTANCE_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_procinst';

CREATE TABLE IF NOT EXISTS act_hi_taskinst (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT '1',
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  TASK_DEF_ID_ varchar(64) DEFAULT NULL,
  TASK_DEF_KEY_ varchar(255) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  PROPAGATED_STAGE_INST_ID_ varchar(255) DEFAULT NULL,
  STATE_ varchar(255) DEFAULT NULL,
  NAME_ varchar(255) DEFAULT NULL,
  PARENT_TASK_ID_ varchar(64) DEFAULT NULL,
  DESCRIPTION_ varchar(4000) DEFAULT NULL,
  OWNER_ varchar(255) DEFAULT NULL,
  ASSIGNEE_ varchar(255) DEFAULT NULL,
  START_TIME_ datetime(3) NOT NULL,
  IN_PROGRESS_TIME_ datetime(3) DEFAULT NULL,
  IN_PROGRESS_STARTED_BY_ varchar(255) DEFAULT NULL,
  CLAIM_TIME_ datetime(3) DEFAULT NULL,
  CLAIMED_BY_ varchar(255) DEFAULT NULL,
  SUSPENDED_TIME_ datetime(3) DEFAULT NULL,
  SUSPENDED_BY_ varchar(255) DEFAULT NULL,
  END_TIME_ datetime(3) DEFAULT NULL,
  COMPLETED_BY_ varchar(255) DEFAULT NULL,
  DURATION_ BIGINT DEFAULT NULL,
  DELETE_REASON_ varchar(4000) DEFAULT NULL,
  PRIORITY_ INT DEFAULT NULL,
  IN_PROGRESS_DUE_DATE_ datetime(3) DEFAULT NULL,
  DUE_DATE_ datetime(3) DEFAULT NULL,
  FORM_KEY_ varchar(255) DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  LAST_UPDATED_TIME_ datetime(3) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_HI_TASK_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_HI_TASK_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_HI_TASK_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_HI_TASK_INST_PROCINST (PROC_INST_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_taskinst';

CREATE TABLE IF NOT EXISTS act_hi_tsk_log (
  ID_ BIGINT NOT NULL AUTO_INCREMENT,
  TYPE_ varchar(64) DEFAULT NULL,
  TASK_ID_ varchar(64) NOT NULL,
  TIME_STAMP_ timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  USER_ID_ varchar(255) DEFAULT NULL,
  DATA_ varchar(4000) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_ACT_HI_TSK_LOG_TASK (TASK_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_tsk_log';

CREATE TABLE IF NOT EXISTS act_hi_varinst (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT '1',
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  NAME_ varchar(255) NOT NULL,
  VAR_TYPE_ varchar(100) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  BYTEARRAY_ID_ varchar(64) DEFAULT NULL,
  DOUBLE_ double DEFAULT NULL,
  LONG_ BIGINT DEFAULT NULL,
  TEXT_ varchar(4000) DEFAULT NULL,
  TEXT2_ varchar(4000) DEFAULT NULL,
  META_INFO_ varchar(4000) DEFAULT NULL,
  CREATE_TIME_ datetime(3) DEFAULT NULL,
  LAST_UPDATED_TIME_ datetime(3) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_HI_PROCVAR_NAME_TYPE (NAME_,VAR_TYPE_),
  KEY ACT_IDX_HI_VAR_SCOPE_ID_TYPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_HI_VAR_SUB_ID_TYPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_HI_PROCVAR_PROC_INST (PROC_INST_ID_),
  KEY ACT_IDX_HI_PROCVAR_TASK_ID (TASK_ID_),
  KEY ACT_IDX_HI_PROCVAR_EXE (EXECUTION_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_hi_varinst';

CREATE TABLE IF NOT EXISTS act_re_procdef (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  NAME_ varchar(255) DEFAULT NULL,
  KEY_ varchar(255) NOT NULL,
  VERSION_ INT NOT NULL,
  DEPLOYMENT_ID_ varchar(64) DEFAULT NULL,
  RESOURCE_NAME_ varchar(4000) DEFAULT NULL,
  DGRM_RESOURCE_NAME_ varchar(4000) DEFAULT NULL,
  DESCRIPTION_ varchar(4000) DEFAULT NULL,
  HAS_START_FORM_KEY_ TINYINT DEFAULT NULL,
  HAS_GRAPHICAL_NOTATION_ TINYINT DEFAULT NULL,
  SUSPENSION_STATE_ INT DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  ENGINE_VERSION_ varchar(255) DEFAULT NULL,
  DERIVED_FROM_ varchar(64) DEFAULT NULL,
  DERIVED_FROM_ROOT_ varchar(64) DEFAULT NULL,
  DERIVED_VERSION_ INT NOT NULL DEFAULT '0',
  PRIMARY KEY (ID_),
  UNIQUE KEY ACT_UNIQ_PROCDEF (KEY_,VERSION_,DERIVED_VERSION_,TENANT_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_re_procdef';

CREATE TABLE IF NOT EXISTS act_procdef_info (
  ID_ varchar(64) NOT NULL,
  PROC_DEF_ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  INFO_JSON_ID_ varchar(64) DEFAULT NULL,
  PRIMARY KEY (ID_),
  UNIQUE KEY ACT_UNIQ_INFO_PROCDEF (PROC_DEF_ID_),
  KEY ACT_IDX_INFO_PROCDEF (PROC_DEF_ID_),
  KEY ACT_FK_INFO_JSON_BA (INFO_JSON_ID_),
  CONSTRAINT ACT_FK_INFO_JSON_BA FOREIGN KEY (INFO_JSON_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_INFO_PROCDEF FOREIGN KEY (PROC_DEF_ID_) REFERENCES act_re_procdef (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_procdef_info';

CREATE TABLE IF NOT EXISTS act_re_model (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  NAME_ varchar(255) DEFAULT NULL,
  KEY_ varchar(255) DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  CREATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  LAST_UPDATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  VERSION_ INT DEFAULT NULL,
  META_INFO_ varchar(4000) DEFAULT NULL,
  DEPLOYMENT_ID_ varchar(64) DEFAULT NULL,
  EDITOR_SOURCE_VALUE_ID_ varchar(64) DEFAULT NULL,
  EDITOR_SOURCE_EXTRA_VALUE_ID_ varchar(64) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_FK_MODEL_SOURCE (EDITOR_SOURCE_VALUE_ID_),
  KEY ACT_FK_MODEL_SOURCE_EXTRA (EDITOR_SOURCE_EXTRA_VALUE_ID_),
  KEY ACT_FK_MODEL_DEPLOYMENT (DEPLOYMENT_ID_),
  CONSTRAINT ACT_FK_MODEL_DEPLOYMENT FOREIGN KEY (DEPLOYMENT_ID_) REFERENCES act_re_deployment (ID_),
  CONSTRAINT ACT_FK_MODEL_SOURCE FOREIGN KEY (EDITOR_SOURCE_VALUE_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_MODEL_SOURCE_EXTRA FOREIGN KEY (EDITOR_SOURCE_EXTRA_VALUE_ID_) REFERENCES act_ge_bytearray (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_re_model';

CREATE TABLE IF NOT EXISTS act_ru_actinst (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT '1',
  PROC_DEF_ID_ varchar(64) NOT NULL,
  PROC_INST_ID_ varchar(64) NOT NULL,
  EXECUTION_ID_ varchar(64) NOT NULL,
  ACT_ID_ varchar(255) NOT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  CALL_PROC_INST_ID_ varchar(64) DEFAULT NULL,
  ACT_NAME_ varchar(255) DEFAULT NULL,
  ACT_TYPE_ varchar(255) NOT NULL,
  ASSIGNEE_ varchar(255) DEFAULT NULL,
  COMPLETED_BY_ varchar(255) DEFAULT NULL,
  START_TIME_ datetime(3) NOT NULL,
  END_TIME_ datetime(3) DEFAULT NULL,
  DURATION_ BIGINT DEFAULT NULL,
  TRANSACTION_ORDER_ INT DEFAULT NULL,
  DELETE_REASON_ varchar(4000) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_RU_ACTI_START (START_TIME_),
  KEY ACT_IDX_RU_ACTI_END (END_TIME_),
  KEY ACT_IDX_RU_ACTI_PROC (PROC_INST_ID_),
  KEY ACT_IDX_RU_ACTI_PROC_ACT (PROC_INST_ID_,ACT_ID_),
  KEY ACT_IDX_RU_ACTI_EXEC (EXECUTION_ID_),
  KEY ACT_IDX_RU_ACTI_EXEC_ACT (EXECUTION_ID_,ACT_ID_),
  KEY ACT_IDX_RU_ACTI_TASK (TASK_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_actinst';

CREATE TABLE IF NOT EXISTS act_ru_execution (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  BUSINESS_KEY_ varchar(255) DEFAULT NULL,
  PARENT_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  SUPER_EXEC_ varchar(64) DEFAULT NULL,
  ROOT_PROC_INST_ID_ varchar(64) DEFAULT NULL,
  ACT_ID_ varchar(255) DEFAULT NULL,
  IS_ACTIVE_ TINYINT DEFAULT NULL,
  IS_CONCURRENT_ TINYINT DEFAULT NULL,
  IS_SCOPE_ TINYINT DEFAULT NULL,
  IS_EVENT_SCOPE_ TINYINT DEFAULT NULL,
  IS_MI_ROOT_ TINYINT DEFAULT NULL,
  SUSPENSION_STATE_ INT DEFAULT NULL,
  CACHED_ENT_STATE_ INT DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  NAME_ varchar(255) DEFAULT NULL,
  START_ACT_ID_ varchar(255) DEFAULT NULL,
  START_TIME_ datetime(3) DEFAULT NULL,
  START_USER_ID_ varchar(255) DEFAULT NULL,
  LOCK_TIME_ timestamp(3) NULL DEFAULT NULL,
  LOCK_OWNER_ varchar(255) DEFAULT NULL,
  IS_COUNT_ENABLED_ TINYINT DEFAULT NULL,
  EVT_SUBSCR_COUNT_ INT DEFAULT NULL,
  TASK_COUNT_ INT DEFAULT NULL,
  JOB_COUNT_ INT DEFAULT NULL,
  TIMER_JOB_COUNT_ INT DEFAULT NULL,
  SUSP_JOB_COUNT_ INT DEFAULT NULL,
  DEADLETTER_JOB_COUNT_ INT DEFAULT NULL,
  EXTERNAL_WORKER_JOB_COUNT_ INT DEFAULT NULL,
  VAR_COUNT_ INT DEFAULT NULL,
  ID_LINK_COUNT_ INT DEFAULT NULL,
  CALLBACK_ID_ varchar(255) DEFAULT NULL,
  CALLBACK_TYPE_ varchar(255) DEFAULT NULL,
  REFERENCE_ID_ varchar(255) DEFAULT NULL,
  REFERENCE_TYPE_ varchar(255) DEFAULT NULL,
  PROPAGATED_STAGE_INST_ID_ varchar(255) DEFAULT NULL,
  BUSINESS_STATUS_ varchar(255) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_EXEC_BUSKEY (BUSINESS_KEY_),
  KEY ACT_IDC_EXEC_ROOT (ROOT_PROC_INST_ID_),
  KEY ACT_IDX_EXEC_REF_ID_ (REFERENCE_ID_),
  KEY ACT_FK_EXE_PROCINST (PROC_INST_ID_),
  KEY ACT_FK_EXE_PARENT (PARENT_ID_),
  KEY ACT_FK_EXE_SUPER (SUPER_EXEC_),
  KEY ACT_FK_EXE_PROCDEF (PROC_DEF_ID_),
  CONSTRAINT ACT_FK_EXE_PARENT FOREIGN KEY (PARENT_ID_) REFERENCES act_ru_execution (ID_) ON DELETE CASCADE,
  CONSTRAINT ACT_FK_EXE_PROCDEF FOREIGN KEY (PROC_DEF_ID_) REFERENCES act_re_procdef (ID_),
  CONSTRAINT ACT_FK_EXE_PROCINST FOREIGN KEY (PROC_INST_ID_) REFERENCES act_ru_execution (ID_) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ACT_FK_EXE_SUPER FOREIGN KEY (SUPER_EXEC_) REFERENCES act_ru_execution (ID_) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_execution';

CREATE TABLE IF NOT EXISTS act_ru_deadletter_job (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  TYPE_ varchar(255) NOT NULL,
  EXCLUSIVE_ tinyint(1) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROCESS_INSTANCE_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  ELEMENT_ID_ varchar(255) DEFAULT NULL,
  ELEMENT_NAME_ varchar(255) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  CORRELATION_ID_ varchar(255) DEFAULT NULL,
  EXCEPTION_STACK_ID_ varchar(64) DEFAULT NULL,
  EXCEPTION_MSG_ varchar(4000) DEFAULT NULL,
  DUEDATE_ timestamp(3) NULL DEFAULT NULL,
  REPEAT_ varchar(255) DEFAULT NULL,
  HANDLER_TYPE_ varchar(255) DEFAULT NULL,
  HANDLER_CFG_ varchar(4000) DEFAULT NULL,
  CUSTOM_VALUES_ID_ varchar(64) DEFAULT NULL,
  CREATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_DEADLETTER_JOB_EXCEPTION_STACK_ID (EXCEPTION_STACK_ID_),
  KEY ACT_IDX_DEADLETTER_JOB_CUSTOM_VALUES_ID (CUSTOM_VALUES_ID_),
  KEY ACT_IDX_DEADLETTER_JOB_CORRELATION_ID (CORRELATION_ID_),
  KEY ACT_IDX_DJOB_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_DJOB_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_DJOB_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  KEY ACT_FK_DEADLETTER_JOB_EXECUTION (EXECUTION_ID_),
  KEY ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE (PROCESS_INSTANCE_ID_),
  KEY ACT_FK_DEADLETTER_JOB_PROC_DEF (PROC_DEF_ID_),
  CONSTRAINT ACT_FK_DEADLETTER_JOB_CUSTOM_VALUES FOREIGN KEY (CUSTOM_VALUES_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_DEADLETTER_JOB_EXCEPTION FOREIGN KEY (EXCEPTION_STACK_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_DEADLETTER_JOB_EXECUTION FOREIGN KEY (EXECUTION_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE FOREIGN KEY (PROCESS_INSTANCE_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_DEADLETTER_JOB_PROC_DEF FOREIGN KEY (PROC_DEF_ID_) REFERENCES act_re_procdef (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_deadletter_job';

CREATE TABLE IF NOT EXISTS act_ru_entitylink (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  CREATE_TIME_ datetime(3) DEFAULT NULL,
  LINK_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  PARENT_ELEMENT_ID_ varchar(255) DEFAULT NULL,
  REF_SCOPE_ID_ varchar(255) DEFAULT NULL,
  REF_SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  REF_SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  ROOT_SCOPE_ID_ varchar(255) DEFAULT NULL,
  ROOT_SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  HIERARCHY_TYPE_ varchar(255) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_ENT_LNK_SCOPE (SCOPE_ID_,SCOPE_TYPE_,LINK_TYPE_),
  KEY ACT_IDX_ENT_LNK_REF_SCOPE (REF_SCOPE_ID_,REF_SCOPE_TYPE_,LINK_TYPE_),
  KEY ACT_IDX_ENT_LNK_ROOT_SCOPE (ROOT_SCOPE_ID_,ROOT_SCOPE_TYPE_,LINK_TYPE_),
  KEY ACT_IDX_ENT_LNK_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_,LINK_TYPE_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_entitylink';

CREATE TABLE IF NOT EXISTS act_ru_event_subscr (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  EVENT_TYPE_ varchar(255) NOT NULL,
  EVENT_NAME_ varchar(255) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  ACTIVITY_ID_ varchar(64) DEFAULT NULL,
  CONFIGURATION_ varchar(255) DEFAULT NULL,
  CREATED_ timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(64) DEFAULT NULL,
  SCOPE_ID_ varchar(64) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(64) DEFAULT NULL,
  SCOPE_DEFINITION_KEY_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(64) DEFAULT NULL,
  LOCK_TIME_ timestamp(3) NULL DEFAULT NULL,
  LOCK_OWNER_ varchar(255) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_EVENT_SUBSCR_CONFIG_ (CONFIGURATION_),
  KEY ACT_IDX_EVENT_SUBSCR_EXEC_ID (EXECUTION_ID_),
  KEY ACT_IDX_EVENT_SUBSCR_PROC_ID (PROC_INST_ID_),
  KEY ACT_IDX_EVENT_SUBSCR_SCOPEREF_ (SCOPE_ID_,SCOPE_TYPE_),
  CONSTRAINT ACT_FK_EVENT_EXEC FOREIGN KEY (EXECUTION_ID_) REFERENCES act_ru_execution (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_event_subscr';

CREATE TABLE IF NOT EXISTS act_ru_external_job (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  TYPE_ varchar(255) NOT NULL,
  LOCK_EXP_TIME_ timestamp(3) NULL DEFAULT NULL,
  LOCK_OWNER_ varchar(255) DEFAULT NULL,
  EXCLUSIVE_ tinyint(1) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROCESS_INSTANCE_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  ELEMENT_ID_ varchar(255) DEFAULT NULL,
  ELEMENT_NAME_ varchar(255) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  CORRELATION_ID_ varchar(255) DEFAULT NULL,
  RETRIES_ INT DEFAULT NULL,
  EXCEPTION_STACK_ID_ varchar(64) DEFAULT NULL,
  EXCEPTION_MSG_ varchar(4000) DEFAULT NULL,
  DUEDATE_ timestamp(3) NULL DEFAULT NULL,
  REPEAT_ varchar(255) DEFAULT NULL,
  HANDLER_TYPE_ varchar(255) DEFAULT NULL,
  HANDLER_CFG_ varchar(4000) DEFAULT NULL,
  CUSTOM_VALUES_ID_ varchar(64) DEFAULT NULL,
  CREATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_EXTERNAL_JOB_EXCEPTION_STACK_ID (EXCEPTION_STACK_ID_),
  KEY ACT_IDX_EXTERNAL_JOB_CUSTOM_VALUES_ID (CUSTOM_VALUES_ID_),
  KEY ACT_IDX_EXTERNAL_JOB_CORRELATION_ID (CORRELATION_ID_),
  KEY ACT_IDX_EJOB_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_EJOB_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_EJOB_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  CONSTRAINT ACT_FK_EXTERNAL_JOB_CUSTOM_VALUES FOREIGN KEY (CUSTOM_VALUES_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_EXTERNAL_JOB_EXCEPTION FOREIGN KEY (EXCEPTION_STACK_ID_) REFERENCES act_ge_bytearray (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_external_job';

CREATE TABLE IF NOT EXISTS act_ru_history_job (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  LOCK_EXP_TIME_ timestamp(3) NULL DEFAULT NULL,
  LOCK_OWNER_ varchar(255) DEFAULT NULL,
  RETRIES_ INT DEFAULT NULL,
  EXCEPTION_STACK_ID_ varchar(64) DEFAULT NULL,
  EXCEPTION_MSG_ varchar(4000) DEFAULT NULL,
  HANDLER_TYPE_ varchar(255) DEFAULT NULL,
  HANDLER_CFG_ varchar(4000) DEFAULT NULL,
  CUSTOM_VALUES_ID_ varchar(64) DEFAULT NULL,
  ADV_HANDLER_CFG_ID_ varchar(64) DEFAULT NULL,
  CREATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_history_job';

CREATE TABLE IF NOT EXISTS act_ru_task (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  TASK_DEF_ID_ varchar(64) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  PROPAGATED_STAGE_INST_ID_ varchar(255) DEFAULT NULL,
  STATE_ varchar(255) DEFAULT NULL,
  NAME_ varchar(255) DEFAULT NULL,
  PARENT_TASK_ID_ varchar(64) DEFAULT NULL,
  DESCRIPTION_ varchar(4000) DEFAULT NULL,
  TASK_DEF_KEY_ varchar(255) DEFAULT NULL,
  OWNER_ varchar(255) DEFAULT NULL,
  ASSIGNEE_ varchar(255) DEFAULT NULL,
  DELEGATION_ varchar(64) DEFAULT NULL,
  PRIORITY_ INT DEFAULT NULL,
  CREATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  IN_PROGRESS_TIME_ datetime(3) DEFAULT NULL,
  IN_PROGRESS_STARTED_BY_ varchar(255) DEFAULT NULL,
  CLAIM_TIME_ datetime(3) DEFAULT NULL,
  CLAIMED_BY_ varchar(255) DEFAULT NULL,
  SUSPENDED_TIME_ datetime(3) DEFAULT NULL,
  SUSPENDED_BY_ varchar(255) DEFAULT NULL,
  IN_PROGRESS_DUE_DATE_ datetime(3) DEFAULT NULL,
  DUE_DATE_ datetime(3) DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  SUSPENSION_STATE_ INT DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  FORM_KEY_ varchar(255) DEFAULT NULL,
  IS_COUNT_ENABLED_ TINYINT DEFAULT NULL,
  VAR_COUNT_ INT DEFAULT NULL,
  ID_LINK_COUNT_ INT DEFAULT NULL,
  SUB_TASK_COUNT_ INT DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_TASK_CREATE (CREATE_TIME_),
  KEY ACT_IDX_TASK_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_TASK_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_TASK_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  KEY ACT_FK_TASK_EXE (EXECUTION_ID_),
  KEY ACT_FK_TASK_PROCINST (PROC_INST_ID_),
  KEY ACT_FK_TASK_PROCDEF (PROC_DEF_ID_),
  CONSTRAINT ACT_FK_TASK_EXE FOREIGN KEY (EXECUTION_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_TASK_PROCDEF FOREIGN KEY (PROC_DEF_ID_) REFERENCES act_re_procdef (ID_),
  CONSTRAINT ACT_FK_TASK_PROCINST FOREIGN KEY (PROC_INST_ID_) REFERENCES act_ru_execution (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_task';

CREATE TABLE IF NOT EXISTS act_ru_identitylink (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  GROUP_ID_ varchar(255) DEFAULT NULL,
  TYPE_ varchar(255) DEFAULT NULL,
  USER_ID_ varchar(255) DEFAULT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_IDENT_LNK_USER (USER_ID_),
  KEY ACT_IDX_IDENT_LNK_GROUP (GROUP_ID_),
  KEY ACT_IDX_IDENT_LNK_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_IDENT_LNK_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_IDENT_LNK_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_ATHRZ_PROCEDEF (PROC_DEF_ID_),
  KEY ACT_FK_TSKASS_TASK (TASK_ID_),
  KEY ACT_FK_IDL_PROCINST (PROC_INST_ID_),
  CONSTRAINT ACT_FK_ATHRZ_PROCEDEF FOREIGN KEY (PROC_DEF_ID_) REFERENCES act_re_procdef (ID_),
  CONSTRAINT ACT_FK_IDL_PROCINST FOREIGN KEY (PROC_INST_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_TSKASS_TASK FOREIGN KEY (TASK_ID_) REFERENCES act_ru_task (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_identitylink';

CREATE TABLE IF NOT EXISTS act_ru_job (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  TYPE_ varchar(255) NOT NULL,
  LOCK_EXP_TIME_ timestamp(3) NULL DEFAULT NULL,
  LOCK_OWNER_ varchar(255) DEFAULT NULL,
  EXCLUSIVE_ tinyint(1) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROCESS_INSTANCE_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  ELEMENT_ID_ varchar(255) DEFAULT NULL,
  ELEMENT_NAME_ varchar(255) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  CORRELATION_ID_ varchar(255) DEFAULT NULL,
  RETRIES_ INT DEFAULT NULL,
  EXCEPTION_STACK_ID_ varchar(64) DEFAULT NULL,
  EXCEPTION_MSG_ varchar(4000) DEFAULT NULL,
  DUEDATE_ timestamp(3) NULL DEFAULT NULL,
  REPEAT_ varchar(255) DEFAULT NULL,
  HANDLER_TYPE_ varchar(255) DEFAULT NULL,
  HANDLER_CFG_ varchar(4000) DEFAULT NULL,
  CUSTOM_VALUES_ID_ varchar(64) DEFAULT NULL,
  CREATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_JOB_EXCEPTION_STACK_ID (EXCEPTION_STACK_ID_),
  KEY ACT_IDX_JOB_CUSTOM_VALUES_ID (CUSTOM_VALUES_ID_),
  KEY ACT_IDX_JOB_CORRELATION_ID (CORRELATION_ID_),
  KEY ACT_IDX_JOB_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_JOB_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_JOB_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  KEY ACT_FK_JOB_EXECUTION (EXECUTION_ID_),
  KEY ACT_FK_JOB_PROCESS_INSTANCE (PROCESS_INSTANCE_ID_),
  KEY ACT_FK_JOB_PROC_DEF (PROC_DEF_ID_),
  CONSTRAINT ACT_FK_JOB_CUSTOM_VALUES FOREIGN KEY (CUSTOM_VALUES_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_JOB_EXCEPTION FOREIGN KEY (EXCEPTION_STACK_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_JOB_EXECUTION FOREIGN KEY (EXECUTION_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_JOB_PROCESS_INSTANCE FOREIGN KEY (PROCESS_INSTANCE_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_JOB_PROC_DEF FOREIGN KEY (PROC_DEF_ID_) REFERENCES act_re_procdef (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_job';

CREATE TABLE IF NOT EXISTS act_ru_suspended_job (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  TYPE_ varchar(255) NOT NULL,
  EXCLUSIVE_ tinyint(1) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROCESS_INSTANCE_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  ELEMENT_ID_ varchar(255) DEFAULT NULL,
  ELEMENT_NAME_ varchar(255) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  CORRELATION_ID_ varchar(255) DEFAULT NULL,
  RETRIES_ INT DEFAULT NULL,
  EXCEPTION_STACK_ID_ varchar(64) DEFAULT NULL,
  EXCEPTION_MSG_ varchar(4000) DEFAULT NULL,
  DUEDATE_ timestamp(3) NULL DEFAULT NULL,
  REPEAT_ varchar(255) DEFAULT NULL,
  HANDLER_TYPE_ varchar(255) DEFAULT NULL,
  HANDLER_CFG_ varchar(4000) DEFAULT NULL,
  CUSTOM_VALUES_ID_ varchar(64) DEFAULT NULL,
  CREATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_SUSPENDED_JOB_EXCEPTION_STACK_ID (EXCEPTION_STACK_ID_),
  KEY ACT_IDX_SUSPENDED_JOB_CUSTOM_VALUES_ID (CUSTOM_VALUES_ID_),
  KEY ACT_IDX_SUSPENDED_JOB_CORRELATION_ID (CORRELATION_ID_),
  KEY ACT_IDX_SJOB_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_SJOB_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_SJOB_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  KEY ACT_FK_SUSPENDED_JOB_EXECUTION (EXECUTION_ID_),
  KEY ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE (PROCESS_INSTANCE_ID_),
  KEY ACT_FK_SUSPENDED_JOB_PROC_DEF (PROC_DEF_ID_),
  CONSTRAINT ACT_FK_SUSPENDED_JOB_CUSTOM_VALUES FOREIGN KEY (CUSTOM_VALUES_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_SUSPENDED_JOB_EXCEPTION FOREIGN KEY (EXCEPTION_STACK_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_SUSPENDED_JOB_EXECUTION FOREIGN KEY (EXECUTION_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE FOREIGN KEY (PROCESS_INSTANCE_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_SUSPENDED_JOB_PROC_DEF FOREIGN KEY (PROC_DEF_ID_) REFERENCES act_re_procdef (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_suspended_job';

CREATE TABLE IF NOT EXISTS act_ru_timer_job (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  CATEGORY_ varchar(255) DEFAULT NULL,
  TYPE_ varchar(255) NOT NULL,
  LOCK_EXP_TIME_ timestamp(3) NULL DEFAULT NULL,
  LOCK_OWNER_ varchar(255) DEFAULT NULL,
  EXCLUSIVE_ tinyint(1) DEFAULT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROCESS_INSTANCE_ID_ varchar(64) DEFAULT NULL,
  PROC_DEF_ID_ varchar(64) DEFAULT NULL,
  ELEMENT_ID_ varchar(255) DEFAULT NULL,
  ELEMENT_NAME_ varchar(255) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  SCOPE_DEFINITION_ID_ varchar(255) DEFAULT NULL,
  CORRELATION_ID_ varchar(255) DEFAULT NULL,
  RETRIES_ INT DEFAULT NULL,
  EXCEPTION_STACK_ID_ varchar(64) DEFAULT NULL,
  EXCEPTION_MSG_ varchar(4000) DEFAULT NULL,
  DUEDATE_ timestamp(3) NULL DEFAULT NULL,
  REPEAT_ varchar(255) DEFAULT NULL,
  HANDLER_TYPE_ varchar(255) DEFAULT NULL,
  HANDLER_CFG_ varchar(4000) DEFAULT NULL,
  CUSTOM_VALUES_ID_ varchar(64) DEFAULT NULL,
  CREATE_TIME_ timestamp(3) NULL DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY ACT_IDX_TIMER_JOB_EXCEPTION_STACK_ID (EXCEPTION_STACK_ID_),
  KEY ACT_IDX_TIMER_JOB_CUSTOM_VALUES_ID (CUSTOM_VALUES_ID_),
  KEY ACT_IDX_TIMER_JOB_CORRELATION_ID (CORRELATION_ID_),
  KEY ACT_IDX_TIMER_JOB_DUEDATE (DUEDATE_),
  KEY ACT_IDX_TJOB_SCOPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_TJOB_SUB_SCOPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_TJOB_SCOPE_DEF (SCOPE_DEFINITION_ID_,SCOPE_TYPE_),
  KEY ACT_FK_TIMER_JOB_EXECUTION (EXECUTION_ID_),
  KEY ACT_FK_TIMER_JOB_PROCESS_INSTANCE (PROCESS_INSTANCE_ID_),
  KEY ACT_FK_TIMER_JOB_PROC_DEF (PROC_DEF_ID_),
  CONSTRAINT ACT_FK_TIMER_JOB_CUSTOM_VALUES FOREIGN KEY (CUSTOM_VALUES_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_TIMER_JOB_EXCEPTION FOREIGN KEY (EXCEPTION_STACK_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_TIMER_JOB_EXECUTION FOREIGN KEY (EXECUTION_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_TIMER_JOB_PROCESS_INSTANCE FOREIGN KEY (PROCESS_INSTANCE_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_TIMER_JOB_PROC_DEF FOREIGN KEY (PROC_DEF_ID_) REFERENCES act_re_procdef (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_timer_job';

CREATE TABLE IF NOT EXISTS act_ru_variable (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  TYPE_ varchar(255) NOT NULL,
  NAME_ varchar(255) NOT NULL,
  EXECUTION_ID_ varchar(64) DEFAULT NULL,
  PROC_INST_ID_ varchar(64) DEFAULT NULL,
  TASK_ID_ varchar(64) DEFAULT NULL,
  SCOPE_ID_ varchar(255) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(255) DEFAULT NULL,
  SCOPE_TYPE_ varchar(255) DEFAULT NULL,
  BYTEARRAY_ID_ varchar(64) DEFAULT NULL,
  DOUBLE_ double DEFAULT NULL,
  LONG_ BIGINT DEFAULT NULL,
  TEXT_ varchar(4000) DEFAULT NULL,
  TEXT2_ varchar(4000) DEFAULT NULL,
  META_INFO_ varchar(4000) DEFAULT NULL,
  PRIMARY KEY (ID_),
  KEY ACT_IDX_RU_VAR_SCOPE_ID_TYPE (SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_IDX_RU_VAR_SUB_ID_TYPE (SUB_SCOPE_ID_,SCOPE_TYPE_),
  KEY ACT_FK_VAR_BYTEARRAY (BYTEARRAY_ID_),
  KEY ACT_IDX_VARIABLE_TASK_ID (TASK_ID_),
  KEY ACT_FK_VAR_EXE (EXECUTION_ID_),
  KEY ACT_FK_VAR_PROCINST (PROC_INST_ID_),
  CONSTRAINT ACT_FK_VAR_BYTEARRAY FOREIGN KEY (BYTEARRAY_ID_) REFERENCES act_ge_bytearray (ID_),
  CONSTRAINT ACT_FK_VAR_EXE FOREIGN KEY (EXECUTION_ID_) REFERENCES act_ru_execution (ID_),
  CONSTRAINT ACT_FK_VAR_PROCINST FOREIGN KEY (PROC_INST_ID_) REFERENCES act_ru_execution (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 act_ru_variable';

CREATE TABLE IF NOT EXISTS flw_ru_batch (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  TYPE_ varchar(64) NOT NULL,
  SEARCH_KEY_ varchar(255) DEFAULT NULL,
  SEARCH_KEY2_ varchar(255) DEFAULT NULL,
  CREATE_TIME_ datetime(3) NOT NULL,
  COMPLETE_TIME_ datetime(3) DEFAULT NULL,
  STATUS_ varchar(255) DEFAULT NULL,
  BATCH_DOC_ID_ varchar(64) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 flw_ru_batch';

CREATE TABLE IF NOT EXISTS flw_ru_batch_part (
  ID_ varchar(64) NOT NULL,
  REV_ INT DEFAULT NULL,
  BATCH_ID_ varchar(64) DEFAULT NULL,
  TYPE_ varchar(64) NOT NULL,
  SCOPE_ID_ varchar(64) DEFAULT NULL,
  SUB_SCOPE_ID_ varchar(64) DEFAULT NULL,
  SCOPE_TYPE_ varchar(64) DEFAULT NULL,
  SEARCH_KEY_ varchar(255) DEFAULT NULL,
  SEARCH_KEY2_ varchar(255) DEFAULT NULL,
  CREATE_TIME_ datetime(3) NOT NULL,
  COMPLETE_TIME_ datetime(3) DEFAULT NULL,
  STATUS_ varchar(255) DEFAULT NULL,
  RESULT_DOC_ID_ varchar(64) DEFAULT NULL,
  TENANT_ID_ varchar(255) DEFAULT '',
  PRIMARY KEY (ID_),
  KEY FLW_IDX_BATCH_PART (BATCH_ID_),
  CONSTRAINT FLW_FK_BATCH_PART_PARENT FOREIGN KEY (BATCH_ID_) REFERENCES flw_ru_batch (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 flw_ru_batch_part';

-- ============================================================
-- 6. 开放平台管理表
-- 来源：zhyc-base-server/zhyc-module-openapi/src/main/resources/db/V1__openapi_core.sql
-- ============================================================
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

-- ============================================================
-- 7. AI 能力中心表
-- 来源：zhyc-base-server/zhyc-module-ai/src/main/resources/db/V1__ai_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS ai_provider (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    provider_code VARCHAR(64) NOT NULL COMMENT '供应商编码',
    provider_name VARCHAR(128) NOT NULL COMMENT '供应商名称',
    provider_type VARCHAR(64) NOT NULL COMMENT '供应商类型',
    base_url VARCHAR(512) NOT NULL COMMENT '模型服务基础地址',
    secret_ref VARCHAR(255) NOT NULL COMMENT '密钥中心引用',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_provider_tenant_code (tenant_id, provider_code),
    KEY idx_ai_provider_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型供应商表';

CREATE TABLE IF NOT EXISTS ai_model_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    provider_id BIGINT NOT NULL COMMENT '供应商主键',
    model_code VARCHAR(128) NOT NULL COMMENT '模型编码',
    model_name VARCHAR(128) NOT NULL COMMENT '模型名称',
    model_type VARCHAR(32) NOT NULL COMMENT '模型类型',
    context_window INT NOT NULL COMMENT '上下文长度',
    support_stream TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否支持流式输出',
    support_tool TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否支持工具调用',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_model_tenant_code (tenant_id, model_code),
    KEY idx_ai_model_tenant_provider (tenant_id, provider_id),
    KEY idx_ai_model_tenant_status (tenant_id, status),
    CONSTRAINT fk_ai_model_provider FOREIGN KEY (provider_id) REFERENCES ai_provider (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型配置表';

CREATE TABLE IF NOT EXISTS ai_app (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT 'AI 应用编码',
    app_name VARCHAR(128) NOT NULL COMMENT 'AI 应用名称',
    default_model_id BIGINT NOT NULL COMMENT '默认模型配置主键',
    system_prompt TEXT NOT NULL COMMENT '系统提示词',
    daily_token_quota INT NOT NULL DEFAULT 100000 COMMENT '每日令牌额度',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_app_tenant_code (tenant_id, app_code),
    KEY idx_ai_app_tenant_status (tenant_id, status),
    KEY idx_ai_app_default_model (default_model_id),
    CONSTRAINT fk_ai_app_default_model FOREIGN KEY (default_model_id) REFERENCES ai_model_config (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 应用接入表';

CREATE TABLE IF NOT EXISTS ai_prompt_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    prompt_code VARCHAR(64) NOT NULL COMMENT '提示词编码',
    prompt_name VARCHAR(128) NOT NULL COMMENT '提示词名称',
    version VARCHAR(32) NOT NULL COMMENT '版本号',
    template_content TEXT NOT NULL COMMENT '模板内容',
    variables VARCHAR(1000) DEFAULT NULL COMMENT '变量清单',
    status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_prompt_tenant_code_version (tenant_id, prompt_code, version),
    KEY idx_ai_prompt_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词模板表';

CREATE TABLE IF NOT EXISTS ai_invocation_audit (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    app_code VARCHAR(64) NOT NULL COMMENT 'AI 应用编码',
    provider_id BIGINT NOT NULL COMMENT '供应商主键',
    model_id BIGINT NOT NULL COMMENT '模型配置主键',
    invocation_type VARCHAR(32) NOT NULL COMMENT '调用类型',
    prompt_tokens INT NOT NULL DEFAULT 0 COMMENT '提示词令牌数',
    completion_tokens INT NOT NULL DEFAULT 0 COMMENT '输出令牌数',
    total_tokens INT NOT NULL DEFAULT 0 COMMENT '总令牌数',
    latency_ms BIGINT NOT NULL DEFAULT 0 COMMENT '调用耗时毫秒',
    status VARCHAR(32) NOT NULL COMMENT '调用状态',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误消息',
    trace_id VARCHAR(128) DEFAULT NULL COMMENT '链路追踪编号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_ai_audit_tenant_app_time (tenant_id, app_code, created_at),
    KEY idx_ai_audit_tenant_model_time (tenant_id, model_id, created_at),
    KEY idx_ai_audit_trace (trace_id),
    CONSTRAINT fk_ai_audit_provider FOREIGN KEY (provider_id) REFERENCES ai_provider (id),
    CONSTRAINT fk_ai_audit_model FOREIGN KEY (model_id) REFERENCES ai_model_config (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用审计表';

-- ============================================================
-- 8. 消息中心表
-- 来源：zhyc-base-server/zhyc-module-message/src/main/resources/db/V1__message_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS msg_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    channel_type VARCHAR(32) NOT NULL COMMENT '消息通道类型',
    title_template VARCHAR(255) NOT NULL COMMENT '标题模板',
    content_template TEXT NOT NULL COMMENT '内容模板',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '模板状态',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人用户 ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_template_tenant_code (tenant_id, template_code),
    KEY idx_msg_template_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

CREATE TABLE IF NOT EXISTS msg_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    message_code VARCHAR(64) NOT NULL COMMENT '消息编码',
    receiver_id BIGINT NOT NULL COMMENT '接收人用户 ID',
    receiver_name VARCHAR(128) NULL COMMENT '接收人名称',
    message_type VARCHAR(32) NOT NULL COMMENT '消息类型',
    title VARCHAR(255) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读，0 未读，1 已读',
    read_at DATETIME NULL COMMENT '阅读时间',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_message_tenant_code (tenant_id, message_code),
    KEY idx_msg_message_tenant_receiver (tenant_id, receiver_id, read_flag, created_at),
    KEY idx_msg_message_tenant_type (tenant_id, message_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- ============================================================
-- 9. 文件中心表
-- 来源：zhyc-base-server/zhyc-module-file/src/main/resources/db/V1__file_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS file_storage_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    storage_code VARCHAR(64) NOT NULL COMMENT '存储配置编码',
    storage_name VARCHAR(128) NOT NULL COMMENT '存储配置名称',
    storage_type VARCHAR(32) NOT NULL COMMENT '存储类型，例如 local、s3、minio、oss',
    endpoint VARCHAR(255) NOT NULL COMMENT '存储访问端点或本地根路径',
    status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '配置状态',
    default_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认存储配置',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人用户 ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_storage_config_tenant_code (tenant_id, storage_code),
    KEY idx_file_storage_config_tenant_status (tenant_id, status),
    KEY idx_file_storage_config_tenant_default (tenant_id, default_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储配置表';

CREATE TABLE IF NOT EXISTS file_object (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    file_code VARCHAR(64) NOT NULL COMMENT '文件业务编码',
    storage_code VARCHAR(64) NOT NULL COMMENT '存储配置编码',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    content_type VARCHAR(128) NOT NULL COMMENT '文件内容类型',
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
    object_key VARCHAR(500) NOT NULL COMMENT '存储对象键或相对路径',
    file_status VARCHAR(32) NOT NULL DEFAULT 'stored' COMMENT '文件状态',
    uploader_id BIGINT NULL COMMENT '上传人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_object_tenant_code (tenant_id, file_code),
    KEY idx_file_object_tenant_storage (tenant_id, storage_code),
    KEY idx_file_object_tenant_created (tenant_id, created_at),
    KEY idx_file_object_tenant_status (tenant_id, file_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件对象表';

CREATE TABLE IF NOT EXISTS file_preview_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件预览日志主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    file_code VARCHAR(64) NOT NULL COMMENT '文件业务编码',
    preview_type VARCHAR(32) NOT NULL COMMENT '预览类型',
    preview_url VARCHAR(512) NOT NULL COMMENT '预览访问地址',
    result VARCHAR(32) NOT NULL COMMENT '预览结果',
    cost_ms BIGINT NOT NULL DEFAULT 0 COMMENT '预览耗时毫秒',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_file_preview_log_tenant_file (tenant_id, file_code),
    KEY idx_file_preview_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件预览日志表';

-- ============================================================
-- 10. 内容管理表
-- 来源：zhyc-base-server/zhyc-module-cms/src/main/resources/db/V1__cms_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS cms_channel (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '内容栏目主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    parent_id BIGINT NULL COMMENT '父栏目主键',
    channel_code VARCHAR(64) NOT NULL COMMENT '栏目编码',
    channel_name VARCHAR(128) NOT NULL COMMENT '栏目名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    channel_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '栏目状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_channel_tenant_code (tenant_id, channel_code),
    KEY idx_cms_channel_tenant_status (tenant_id, channel_status)
) COMMENT='内容栏目';

CREATE TABLE IF NOT EXISTS cms_content (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '内容文章主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    channel_code VARCHAR(64) NOT NULL COMMENT '栏目编码',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    summary VARCHAR(500) NULL COMMENT '文章摘要',
    body_content LONGTEXT NULL COMMENT '文章正文',
    content_status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '文章状态',
    author_id BIGINT NULL COMMENT '作者用户主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    KEY idx_cms_content_tenant_channel (tenant_id, channel_code),
    KEY idx_cms_content_tenant_status (tenant_id, content_status)
) COMMENT='内容文章';

-- ============================================================
-- 11. 在线作业表
-- 来源：zhyc-base-server/zhyc-module-job/src/main/resources/db/V1__job_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS job_task (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '作业任务主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    job_code VARCHAR(64) NOT NULL COMMENT '作业任务编码',
    job_name VARCHAR(128) NOT NULL COMMENT '作业任务名称',
    cron_expression VARCHAR(128) NOT NULL COMMENT 'Cron 表达式',
    handler_name VARCHAR(128) NOT NULL COMMENT '任务处理器名称',
    job_description VARCHAR(500) NULL COMMENT '作业任务说明',
    job_status VARCHAR(32) NOT NULL DEFAULT 'disabled' COMMENT '作业状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_task_tenant_code (tenant_id, job_code),
    KEY idx_job_task_tenant_status (tenant_id, job_status)
) COMMENT='在线作业任务';

CREATE TABLE IF NOT EXISTS job_task_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '作业执行日志主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    job_id BIGINT NOT NULL COMMENT '作业任务主键',
    trigger_type VARCHAR(32) NOT NULL COMMENT '触发类型',
    start_at DATETIME NOT NULL COMMENT '开始时间',
    end_at DATETIME NULL COMMENT '结束时间',
    result VARCHAR(32) NOT NULL COMMENT '执行结果',
    error_message VARCHAR(1000) NULL COMMENT '错误信息',
    operator_id BIGINT NULL COMMENT '操作人用户主键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_job_task_log_tenant_job (tenant_id, job_id, start_at)
) COMMENT='作业执行日志';

-- ============================================================
-- 12. 全文检索表
-- 来源：zhyc-base-server/zhyc-module-search/src/main/resources/db/V1__search_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS search_index_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '索引配置主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    index_code VARCHAR(128) NOT NULL COMMENT '索引编码',
    index_name VARCHAR(128) NOT NULL COMMENT '索引名称',
    source_table VARCHAR(128) NOT NULL COMMENT '数据来源表名',
    search_fields VARCHAR(512) NOT NULL COMMENT '可检索字段列表，逗号分隔',
    filter_fields VARCHAR(512) DEFAULT NULL COMMENT '可过滤字段列表，逗号分隔',
    index_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '索引状态',
    remark VARCHAR(512) DEFAULT NULL COMMENT '配置备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    UNIQUE KEY uk_search_index_config_tenant_code (tenant_id, index_code),
    KEY idx_search_index_config_tenant_status (tenant_id, index_status)
) COMMENT='全文检索索引配置';

CREATE TABLE IF NOT EXISTS search_rebuild_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '重建任务主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    index_code VARCHAR(128) NOT NULL COMMENT '索引编码',
    task_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '任务状态',
    trigger_type VARCHAR(32) NOT NULL DEFAULT 'manual' COMMENT '触发类型',
    started_at DATETIME DEFAULT NULL COMMENT '任务开始时间',
    finished_at DATETIME DEFAULT NULL COMMENT '任务完成时间',
    error_message VARCHAR(1024) DEFAULT NULL COMMENT '失败错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    KEY idx_search_rebuild_task_tenant_index (tenant_id, index_code),
    KEY idx_search_rebuild_task_status (task_status)
) COMMENT='全文检索索引重建任务';

CREATE TABLE IF NOT EXISTS search_query_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '查询日志主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    index_code VARCHAR(128) NOT NULL COMMENT '索引编码',
    keyword VARCHAR(256) NOT NULL COMMENT '查询关键词',
    result_count INT NOT NULL DEFAULT 0 COMMENT '返回结果数量',
    cost_ms BIGINT NOT NULL DEFAULT 0 COMMENT '查询耗时毫秒',
    query_status VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '查询状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_search_query_log_tenant_index (tenant_id, index_code),
    KEY idx_search_query_log_created_at (created_at)
) COMMENT='全文检索查询日志';

-- ============================================================
-- 13. 可视化大屏表
-- 来源：zhyc-base-server/zhyc-module-visual/src/main/resources/db/V1__visual_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS visual_dataset (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式数据隔离',
    dataset_code VARCHAR(64) NOT NULL COMMENT '数据集编码，租户内唯一',
    dataset_name VARCHAR(128) NOT NULL COMMENT '数据集名称',
    datasource_code VARCHAR(64) NOT NULL COMMENT '数据源编码，对应低代码数据源或默认数据源',
    sql_text TEXT NOT NULL COMMENT '查询 SQL，由数据集执行器统一校验后执行',
    dataset_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '数据集状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_visual_dataset_tenant_code (tenant_id, dataset_code),
    KEY idx_visual_dataset_tenant_status (tenant_id, dataset_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可视化数据集表';

CREATE TABLE IF NOT EXISTS visual_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式数据隔离',
    report_code VARCHAR(64) NOT NULL COMMENT '报表编码，租户内唯一',
    report_name VARCHAR(128) NOT NULL COMMENT '报表名称',
    dataset_code VARCHAR(64) NOT NULL COMMENT '数据集编码，指向同租户数据集',
    chart_type VARCHAR(32) NOT NULL DEFAULT 'table' COMMENT '图表类型',
    config_json TEXT NOT NULL COMMENT '图表配置 JSON',
    report_status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '报表状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_visual_report_tenant_code (tenant_id, report_code),
    KEY idx_visual_report_tenant_status (tenant_id, report_status),
    KEY idx_visual_report_tenant_dataset (tenant_id, dataset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可视化报表表';

CREATE TABLE IF NOT EXISTS visual_screen (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式数据隔离',
    screen_code VARCHAR(64) NOT NULL COMMENT '大屏编码，租户内唯一',
    screen_name VARCHAR(128) NOT NULL COMMENT '大屏名称',
    layout_json TEXT NOT NULL COMMENT '大屏布局 JSON，保存组件位置、尺寸和报表编码',
    screen_status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '大屏状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (id),
    UNIQUE KEY uk_visual_screen_tenant_code (tenant_id, screen_code),
    KEY idx_visual_screen_tenant_status (tenant_id, screen_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可视化大屏表';

-- ============================================================
-- 14. 国际化资源表
-- 来源：zhyc-base-server/zhyc-module-i18n/src/main/resources/db/V1__i18n_core.sql
-- ============================================================
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

-- ============================================================
-- 15. 国际化基础词条数据
-- 来源：zhyc-base-server/zhyc-module-i18n/src/main/resources/db/V2__i18n_seed.sql
-- ============================================================
-- 国际化词条初始化数据。
-- 说明：默认初始化平台菜单、按钮、状态等基础词条；使用唯一键保证重复执行幂等。
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

-- ============================================================
-- 16. 采购样板业务表
-- 来源：zhyc-base-server/zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_request (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    request_no VARCHAR(64) NOT NULL COMMENT '采购申请单号',
    request_title VARCHAR(128) NOT NULL COMMENT '采购申请标题',
    applicant_id BIGINT NOT NULL COMMENT '申请人用户 ID',
    org_id BIGINT NOT NULL COMMENT '申请部门 ID',
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '采购申请总金额',
    request_reason VARCHAR(1000) NULL COMMENT '采购申请原因',
    process_status VARCHAR(32) NOT NULL COMMENT '流程状态',
    process_instance_id VARCHAR(128) NULL COMMENT '流程实例 ID',
    submitted_at DATETIME NULL COMMENT '提交审批时间',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人用户 ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pur_request_tenant_no (tenant_id, request_no),
    KEY idx_pur_request_tenant_applicant (tenant_id, applicant_id),
    KEY idx_pur_request_tenant_status (tenant_id, process_status),
    KEY idx_pur_request_tenant_process (tenant_id, process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请主表';

CREATE TABLE IF NOT EXISTS pur_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    order_no VARCHAR(64) NOT NULL COMMENT '采购订单号',
    request_no VARCHAR(64) NOT NULL COMMENT '采购申请单号',
    supplier_id BIGINT NOT NULL COMMENT '供应商 ID',
    buyer_id BIGINT NOT NULL COMMENT '采购员用户 ID',
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '采购订单总金额',
    order_status VARCHAR(32) NOT NULL COMMENT '订单状态',
    created_by BIGINT NULL COMMENT '创建人用户 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人用户 ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    remark VARCHAR(500) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pur_order_tenant_no (tenant_id, order_no),
    KEY idx_pur_order_tenant_request (tenant_id, request_no),
    KEY idx_pur_order_tenant_status (tenant_id, order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单主表';

CREATE TABLE IF NOT EXISTS pur_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码',
    order_no VARCHAR(64) NOT NULL COMMENT '采购订单号',
    item_name VARCHAR(128) NOT NULL COMMENT '物品名称',
    quantity DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '采购数量',
    unit_price DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '采购单价',
    amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '明细金额',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0 未删除，1 已删除',
    PRIMARY KEY (id),
    KEY idx_pur_order_item_tenant_order (tenant_id, order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- 采购申请状态开放 API 目录注册，用于开放 API 网关运行态路由发现。
INSERT INTO openapi_catalog (
    api_code,
    api_name,
    group_code,
    http_method,
    path_pattern,
    status
) VALUES (
    'purchase-request-status',
    '采购申请状态查询',
    'purchase',
    'GET',
    '/openapi/v1/purchase/requests/{requestNo}/status',
    'enabled'
) ON DUPLICATE KEY UPDATE
    api_name = VALUES(api_name),
    group_code = VALUES(group_code),
    http_method = VALUES(http_method),
    path_pattern = VALUES(path_pattern),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

-- 采购申请状态开放 API 版本注册，用于开放 API 网关定位后端服务入口。
INSERT INTO openapi_version (
    api_code,
    version,
    backend_route,
    request_schema,
    response_schema,
    status
) VALUES (
    'purchase-request-status',
    'v1',
    'http://zhyc-platform-app/openapi/v1/purchase/requests/{requestNo}/status',
    JSON_OBJECT('method', 'GET', 'tenantHeader', 'X-ZHYC-Tenant-Id', 'pathVariable', 'requestNo'),
    JSON_OBJECT('apiCode', 'purchase-request-status', 'fields', JSON_ARRAY('requestNo', 'requestTitle', 'processStatus', 'totalAmount', 'submittedAt')),
    'published'
) ON DUPLICATE KEY UPDATE
    backend_route = VALUES(backend_route),
    request_schema = VALUES(request_schema),
    response_schema = VALUES(response_schema),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

-- 采购订单详情开放 API 目录注册，用于开放 API 网关运行态路由发现。
INSERT INTO openapi_catalog (
    api_code,
    api_name,
    group_code,
    http_method,
    path_pattern,
    status
) VALUES (
    'purchase-order-detail',
    '采购订单详情查询',
    'purchase',
    'GET',
    '/openapi/v1/purchase/orders/{orderNo}',
    'enabled'
) ON DUPLICATE KEY UPDATE
    api_name = VALUES(api_name),
    group_code = VALUES(group_code),
    http_method = VALUES(http_method),
    path_pattern = VALUES(path_pattern),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

-- 采购订单详情开放 API 版本注册，用于开放 API 网关定位后端服务入口。
INSERT INTO openapi_version (
    api_code,
    version,
    backend_route,
    request_schema,
    response_schema,
    status
) VALUES (
    'purchase-order-detail',
    'v1',
    'http://zhyc-platform-app/openapi/v1/purchase/orders/{orderNo}',
    JSON_OBJECT('method', 'GET', 'tenantHeader', 'X-ZHYC-Tenant-Id', 'pathVariable', 'orderNo'),
    JSON_OBJECT('apiCode', 'purchase-order-detail', 'fields', JSON_ARRAY('orderNo', 'requestNo', 'supplierId', 'buyerId', 'totalAmount', 'orderStatus', 'items')),
    'published'
) ON DUPLICATE KEY UPDATE
    backend_route = VALUES(backend_route),
    request_schema = VALUES(request_schema),
    response_schema = VALUES(response_schema),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- 17. 系统基础种子数据
-- 来源：zhyc-base-server/zhyc-module-system/src/main/resources/db/V2__system_seed.sql
-- ============================================================
-- 首期本地初始化种子数据，仅用于搭建可联调的基础租户、用户、角色、菜单和模块目录。
-- password_hash 必须在本地部署前替换为 Shiro PasswordService 生成的哈希值，禁止提交真实默认密码。

INSERT IGNORE INTO sys_tenant_package (id, package_code, package_name, status, max_user_count, max_storage_mb)
VALUES (1, 'default-enterprise', '默认企业版套餐', 'enabled', 1000, 102400);

INSERT IGNORE INTO sys_tenant (id, tenant_id, tenant_name, package_id, isolation_mode, status, contact_name)
VALUES (1, 'zhyc-platform', '众汇云创演示租户', 1, 'TENANT_COLUMN', 'enabled', '平台管理员');

INSERT IGNORE INTO sys_org (id, tenant_id, parent_id, ancestors, org_code, org_name, leader_user_id, sort_order, status)
VALUES (1, 'zhyc-platform', NULL, '0', 'HQ', '总部', NULL, 1, 'enabled');

INSERT IGNORE INTO sys_post (id, tenant_id, org_id, post_code, post_name, sort_order, status)
VALUES (1, 'zhyc-platform', 1, 'platform-admin', '平台管理员', 1, 'enabled');

INSERT IGNORE INTO sys_user (id, tenant_id, username, nickname, password_hash, status)
VALUES (1, 'zhyc-platform', 'admin', '平台管理员', 'replace_with_shiro_password_hash', 'enabled');

INSERT IGNORE INTO sys_role (id, tenant_id, role_code, name, data_scope, status)
VALUES (1, 'zhyc-platform', 'platform-admin', '平台管理员', 'ALL', 'enabled');

INSERT IGNORE INTO sys_user_post (tenant_id, user_id, post_id, primary_flag)
VALUES ('zhyc-platform', 1, 1, 1);

INSERT IGNORE INTO sys_user_role (tenant_id, user_id, role_id)
VALUES ('zhyc-platform', 1, 1);

INSERT IGNORE INTO sys_admin_scope (tenant_id, user_id, scope_type, scope_ref_code, scope_name)
VALUES
    ('zhyc-platform', 1, 'tenant', 'zhyc-platform', '众汇云创演示租户'),
    ('zhyc-platform', 1, 'org', 'HQ', '总部'),
    ('zhyc-platform', 1, 'module', 'system', '系统管理'),
    ('zhyc-platform', 1, 'module', 'lowcode', '低代码中心'),
    ('zhyc-platform', 1, 'module', 'ai', 'AI 能力中心'),
    ('zhyc-platform', 1, 'module', 'workflow', '工作流中心'),
    ('zhyc-platform', 1, 'module', 'openapi', '开放平台');

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_code, menu_name, menu_type, path, component, permission, sort_order, status)
VALUES
    (100, 'zhyc-platform', NULL, 'dashboard', '个人工作台', 'menu', '/dashboard', 'dashboard/index', 'dashboard:view', 10, 'enabled'),
    (200, 'zhyc-platform', NULL, 'system', '系统管理', 'directory', '/system', 'LAYOUT', 'system:view', 20, 'enabled'),
    (210, 'zhyc-platform', 200, 'system-tenant', '租户管理', 'menu', '/system/tenants', 'system/tenant/index', 'system:tenant:query', 10, 'enabled'),
    (211, 'zhyc-platform', 200, 'system-tenant-package', '租户套餐', 'menu', '/system/tenant-packages', 'system/tenant-package/index', 'system:tenant-package:query', 20, 'enabled'),
    (212, 'zhyc-platform', 200, 'system-tenant-package-module', '套餐授权', 'menu', '/system/tenant-package-modules', 'system/tenant-package-module/index', 'system:tenant-package-module:query', 30, 'enabled'),
    (213, 'zhyc-platform', 200, 'system-tenant-param', '租户参数', 'menu', '/system/tenant-params', 'system/tenant-param/index', 'system:tenant-param:query', 40, 'enabled'),
    (214, 'zhyc-platform', 200, 'system-admin-scope', '管理员范围', 'menu', '/system/admin-scopes', 'system/admin-scope/index', 'system:admin:query', 50, 'enabled'),
    (220, 'zhyc-platform', 200, 'system-org', '组织机构', 'menu', '/system/orgs', 'system/org/index', 'system:org:query', 60, 'enabled'),
    (221, 'zhyc-platform', 200, 'system-post', '岗位管理', 'menu', '/system/posts', 'system/post/index', 'system:post:query', 70, 'enabled'),
    (222, 'zhyc-platform', 200, 'system-user', '用户管理', 'menu', '/system/users', 'system/user/index', 'system:user:query', 80, 'enabled'),
    (223, 'zhyc-platform', 200, 'system-user-post', '用户岗位', 'menu', '/system/user-posts', 'system/user-post/index', 'system:user:query', 90, 'disabled'),
    (224, 'zhyc-platform', 200, 'system-user-role', '用户角色', 'menu', '/system/user-roles', 'system/user-role/index', 'system:user:query', 100, 'disabled'),
    (230, 'zhyc-platform', 200, 'system-role', '角色管理', 'menu', '/system/roles', 'system/role/index', 'system:role:query', 110, 'enabled'),
    (240, 'zhyc-platform', 200, 'system-menu', '菜单权限', 'menu', '/system/menus', 'system/menu/index', 'system:permission:query', 120, 'enabled'),
    (241, 'zhyc-platform', 200, 'system-role-data-scope', '角色数据权限', 'menu', '/system/role-data-scopes', 'system/role-data-scope/index', 'system:role:query', 130, 'disabled'),
    (242, 'zhyc-platform', 200, 'system-permission-audit', '权限审计', 'menu', '/system/permission-audits', 'system/permission-audit/index', 'system:audit:query', 140, 'enabled'),
    (250, 'zhyc-platform', 260, 'system-access-restriction', '访问限制查询', 'button', NULL, NULL, 'system:access-restriction:query', 5, 'enabled'),
    (260, 'zhyc-platform', 200, 'system-security-protection', '安全防护中心', 'menu', '/system/security-protection', 'system/security-protection/index', 'system:security-protection:query', 150, 'enabled'),
    (251, 'zhyc-platform', 200, 'system-password-policy', '密码策略', 'menu', '/system/password-policies', 'system/password-policy/index', 'system:password-policy:query', 160, 'enabled'),
    (252, 'zhyc-platform', 200, 'system-module', '模块管理', 'menu', '/system/modules', 'system/module/index', 'system:module:query', 170, 'enabled'),
    (253, 'zhyc-platform', 200, 'system-code-rule', '编码规则', 'menu', '/system/code-rules', 'system/code-rule/index', 'system:code-rule:query', 180, 'enabled'),
    (254, 'zhyc-platform', 200, 'system-param', '系统参数', 'menu', '/system/params', 'system/param/index', 'system:param:query', 190, 'enabled'),
    (255, 'zhyc-platform', 200, 'system-dict', '字典管理', 'menu', '/system/dicts', 'system/dict/index', 'system:dict:query', 200, 'enabled'),
    (256, 'zhyc-platform', 200, 'system-audit-log', '审计日志', 'menu', '/system/audit-logs', 'system/audit-log/index', 'system:audit:query', 210, 'enabled'),
    (257, 'zhyc-platform', 200, 'system-login-log', '登录日志', 'menu', '/system/login-logs', 'system/login-log/index', 'system:audit:query', 220, 'enabled'),
    (258, 'zhyc-platform', 200, 'system-exception-log', '异常日志', 'menu', '/system/exception-logs', 'system/exception-log/index', 'system:audit:query', 230, 'enabled'),
    (259, 'zhyc-platform', 200, 'system-secret', '密钥管理', 'menu', '/system/secrets', 'system/secret/index', 'system:secret:query', 240, 'enabled'),
    (300, 'zhyc-platform', NULL, 'lowcode', '低代码中心', 'directory', '/lowcode', 'LAYOUT', 'lowcode:view', 30, 'enabled'),
    (310, 'zhyc-platform', 300, 'lowcode-datasource', '数据源管理', 'menu', '/lowcode/datasource', 'lowcode/datasource/index', 'lowcode:datasource:query', 10, 'enabled'),
    (320, 'zhyc-platform', 300, 'lowcode-model', '数据表建模', 'menu', '/lowcode/model', 'lowcode/model/index', 'lowcode:table:query', 20, 'enabled'),
    (32001, 'zhyc-platform', 320, 'lowcode-table-save', '保存模型', 'button', NULL, NULL, 'lowcode:table:save', 1, 'enabled'),
    (32002, 'zhyc-platform', 320, 'lowcode-table-import', '导入表结构', 'button', NULL, NULL, 'lowcode:table:import', 2, 'enabled'),
    (32003, 'zhyc-platform', 320, 'lowcode-table-publish', '发布建表', 'button', NULL, NULL, 'lowcode:table:publish', 3, 'enabled'),
    (321, 'zhyc-platform', 300, 'lowcode-relation', '表关系建模', 'menu', '/lowcode/relations', 'lowcode/relation/index', 'lowcode:relation:query', 30, 'enabled'),
    (322, 'zhyc-platform', 300, 'lowcode-page', '页面模型', 'menu', '/lowcode/pages', 'lowcode/page/index', 'lowcode:page:query', 40, 'enabled'),
    (323, 'zhyc-platform', 300, 'lowcode-template', '生成模板', 'menu', '/lowcode/templates', 'lowcode/generator/index', 'lowcode:template:query', 50, 'enabled'),
    (330, 'zhyc-platform', 300, 'lowcode-generator', '代码生成', 'menu', '/lowcode/generator', 'lowcode/generator/index', 'lowcode:generator:query', 60, 'enabled'),
    (331, 'zhyc-platform', 300, 'lowcode-record', '生成记录', 'menu', '/lowcode/records', 'lowcode/generator/index', 'lowcode:generator:query', 70, 'enabled'),
    (350, 'zhyc-platform', NULL, 'ai', 'AI 能力中心', 'directory', '/ai', 'LAYOUT', 'ai:view', 35, 'enabled'),
    (351, 'zhyc-platform', 350, 'ai-provider', '供应商', 'menu', '/ai/providers', 'ai/core/index', 'ai:provider:query', 10, 'enabled'),
    (352, 'zhyc-platform', 350, 'ai-model', '模型配置', 'menu', '/ai/models', 'ai/core/index', 'ai:model:query', 20, 'enabled'),
    (353, 'zhyc-platform', 350, 'ai-app', '应用接入', 'menu', '/ai/apps', 'ai/core/index', 'ai:app:query', 30, 'enabled'),
    (354, 'zhyc-platform', 350, 'ai-prompt', '提示词', 'menu', '/ai/prompts', 'ai/core/index', 'ai:prompt:query', 40, 'enabled'),
    (355, 'zhyc-platform', 350, 'ai-invocation-audit', '调用审计', 'menu', '/ai/invocation-audits', 'ai/core/index', 'ai:audit:query', 50, 'enabled'),
    (35101, 'zhyc-platform', 351, 'ai-provider-save', '保存供应商', 'button', NULL, NULL, 'ai:provider:save', 1, 'enabled'),
    (35201, 'zhyc-platform', 352, 'ai-model-save', '保存模型', 'button', NULL, NULL, 'ai:model:save', 1, 'enabled'),
    (35301, 'zhyc-platform', 353, 'ai-app-save', '保存应用', 'button', NULL, NULL, 'ai:app:save', 1, 'enabled'),
    (35302, 'zhyc-platform', 353, 'ai-runtime-chat', '测试调用', 'button', NULL, NULL, 'ai:runtime:chat', 2, 'enabled'),
    (35401, 'zhyc-platform', 354, 'ai-prompt-save', '保存提示词', 'button', NULL, NULL, 'ai:prompt:save', 1, 'enabled'),
    (35501, 'zhyc-platform', 355, 'ai-audit-record', '记录调用审计', 'button', NULL, NULL, 'ai:audit:record', 1, 'enabled'),
    (400, 'zhyc-platform', NULL, 'workflow', '工作流中心', 'directory', '/workflow', 'LAYOUT', 'workflow:view', 40, 'enabled'),
    (410, 'zhyc-platform', 400, 'workflow-task-todo', '流程待办', 'menu', '/workflow/tasks/todo', 'workflow/task/todo', 'workflow:task:todo', 10, 'enabled'),
    (411, 'zhyc-platform', 400, 'workflow-task-done', '流程已办', 'menu', '/workflow/tasks/done', 'workflow/task/done', 'workflow:task:done', 20, 'enabled'),
    (412, 'zhyc-platform', 400, 'workflow-task-started', '我发起的', 'menu', '/workflow/tasks/started', 'workflow/task/started', 'workflow:task:started', 30, 'enabled'),
    (413, 'zhyc-platform', 400, 'workflow-task-cc', '抄送我的', 'menu', '/workflow/tasks/cc', 'workflow/task/cc', 'workflow:task:cc', 40, 'enabled'),
    (414, 'zhyc-platform', 400, 'workflow-task-monitor', '流程监控', 'menu', '/workflow/tasks/monitor', 'workflow/task/monitor', 'workflow:task:monitor', 50, 'enabled'),
    (420, 'zhyc-platform', 400, 'workflow-category', '流程分类', 'menu', '/workflow/categories', 'workflow/category/index', 'workflow:model:query', 60, 'enabled'),
    (421, 'zhyc-platform', 400, 'workflow-model', '流程模型', 'menu', '/workflow/models', 'workflow/model/index', 'workflow:model:query', 70, 'enabled'),
    (422, 'zhyc-platform', 400, 'workflow-form-binding', '表单绑定', 'menu', '/workflow/form-bindings', 'workflow/binding/index', 'workflow:binding:query', 80, 'enabled'),
    (423, 'zhyc-platform', 400, 'workflow-definition', '流程定义', 'menu', '/workflow/definitions', 'workflow/definition/index', 'workflow:model:query', 90, 'enabled'),
    (500, 'zhyc-platform', NULL, 'openapi', '开放平台', 'directory', '/openapi', 'LAYOUT', 'openapi:view', 50, 'enabled'),
    (505, 'zhyc-platform', 500, 'developer-portal', '开发者门户', 'menu', '/developer/portal', 'developer/portal/index', 'openapi:developer:portal', 10, 'enabled'),
    (510, 'zhyc-platform', 500, 'openapi-app', '开发者应用', 'menu', '/openapi/apps', 'openapi/app/index', 'openapi:app:query', 20, 'enabled'),
    (511, 'zhyc-platform', 500, 'openapi-api-key', 'API Key', 'menu', '/openapi/api-keys', 'openapi/api-key/index', 'openapi:api-key:query', 30, 'enabled'),
    (512, 'zhyc-platform', 500, 'openapi-oauth-client', 'OAuth2 客户端', 'menu', '/openapi/oauth-clients', 'openapi/oauth-client/index', 'openapi:oauth-client:query', 40, 'enabled'),
    (520, 'zhyc-platform', 500, 'openapi-catalog', 'API 目录', 'menu', '/openapi/catalogs', 'openapi/catalog/index', 'openapi:catalog:query', 50, 'enabled'),
    (521, 'zhyc-platform', 500, 'openapi-version', 'API 发布', 'menu', '/openapi/versions', 'openapi/version/index', 'openapi:catalog:query', 60, 'enabled'),
    (522, 'zhyc-platform', 500, 'openapi-permission', 'API 授权', 'menu', '/openapi/api-permissions', 'openapi/api-permission/index', 'openapi:api-permission:query', 70, 'enabled'),
    (523, 'zhyc-platform', 500, 'openapi-signature-policy', '签名策略', 'menu', '/openapi/signature-policies', 'openapi/signature-policy/index', 'openapi:signature-policy:query', 80, 'enabled'),
    (524, 'zhyc-platform', 500, 'openapi-rate-limit-policy', '限流策略', 'menu', '/openapi/rate-limit-policies', 'openapi/rate-limit-policy/index', 'openapi:rate-limit-policy:query', 90, 'enabled'),
    (525, 'zhyc-platform', 500, 'openapi-call-audit', '调用审计', 'menu', '/openapi/call-audits', 'openapi/call-audit/index', 'openapi:call-audit:query', 100, 'enabled'),
    (526, 'zhyc-platform', 500, 'openapi-error-log', '错误日志', 'menu', '/openapi/error-logs', 'openapi/error-log/index', 'openapi:error-log:query', 110, 'enabled'),
    (600, 'zhyc-platform', NULL, 'purchase', '采购样板', 'directory', '/purchase', 'LAYOUT', 'purchase:view', 60, 'enabled'),
    (610, 'zhyc-platform', 600, 'purchase-request', '采购申请', 'menu', '/purchase/requests', 'purchase/request/index', 'purchase:request:view', 10, 'enabled'),
    (611, 'zhyc-platform', 600, 'purchase-order', '采购订单', 'menu', '/purchase/orders', 'purchase/order/index', 'purchase:order:query', 20, 'enabled'),
    (612, 'zhyc-platform', 600, 'purchase-approval', '采购审批记录', 'menu', '/purchase/approvals', 'purchase/approval-record/index', 'purchase:approval:query', 30, 'enabled'),
    (700, 'zhyc-platform', NULL, 'message', '消息中心', 'directory', '/message', 'LAYOUT', 'message:view', 70, 'enabled'),
    (710, 'zhyc-platform', 700, 'message-inbox', '站内消息', 'menu', '/message/inbox', 'message/inbox/index', 'message:inbox:query', 10, 'enabled'),
    (711, 'zhyc-platform', 700, 'message-template', '消息模板', 'menu', '/message/templates', 'message/template/index', 'message:template:query', 20, 'enabled'),
    (800, 'zhyc-platform', NULL, 'file', '文件中心', 'directory', '/file', 'LAYOUT', 'file:view', 80, 'enabled'),
    (810, 'zhyc-platform', 800, 'file-storage-config', '存储配置', 'menu', '/file/storage-configs', 'file/storage/index', 'file:storage:query', 10, 'enabled'),
    (811, 'zhyc-platform', 800, 'file-object', '文件对象', 'menu', '/file/objects', 'file/object/index', 'file:object:query', 20, 'enabled'),
    (812, 'zhyc-platform', 800, 'file-preview-log', '预览记录', 'menu', '/file/preview-logs', 'file/preview/index', 'file:preview:query', 30, 'enabled'),
    (900, 'zhyc-platform', NULL, 'cms', '内容管理', 'directory', '/cms', 'LAYOUT', 'cms:view', 90, 'enabled'),
    (910, 'zhyc-platform', 900, 'cms-channel', '内容栏目', 'menu', '/cms/channels', 'cms/channel/index', 'cms:channel:query', 10, 'enabled'),
    (911, 'zhyc-platform', 900, 'cms-content', '内容文章', 'menu', '/cms/contents', 'cms/content/index', 'cms:content:query', 20, 'enabled'),
    (1000, 'zhyc-platform', NULL, 'visual', '报表大屏', 'directory', '/visual', 'LAYOUT', 'visual:view', 100, 'enabled'),
    (1010, 'zhyc-platform', 1000, 'visual-dataset', '报表数据集', 'menu', '/visual/datasets', 'visual/dataset/index', 'visual:dataset:query', 10, 'enabled'),
    (1011, 'zhyc-platform', 1000, 'visual-report', '报表设计器', 'menu', '/visual/reports', 'visual/report/index', 'visual:report:query', 20, 'enabled'),
    (1012, 'zhyc-platform', 1000, 'visual-screen', '可视化数据大屏', 'menu', '/visual/screens', 'visual/screen/index', 'visual:screen:query', 30, 'enabled'),
    (1100, 'zhyc-platform', NULL, 'job', '在线作业', 'directory', '/job', 'LAYOUT', 'job:view', 110, 'enabled'),
    (1110, 'zhyc-platform', 1100, 'job-task', '在线作业', 'menu', '/job/tasks', 'job/task/index', 'job:task:query', 10, 'enabled'),
    (1200, 'zhyc-platform', NULL, 'i18n', '国际化', 'directory', '/i18n', 'LAYOUT', 'i18n:view', 120, 'enabled'),
    (1210, 'zhyc-platform', 1200, 'i18n-message', '国际化词条', 'menu', '/i18n/messages', 'i18n/message/index', 'i18n:message:query', 10, 'enabled'),
    (121001, 'zhyc-platform', 1210, 'i18n-message-save', '词条保存', 'button', NULL, NULL, 'i18n:message:save', 1, 'enabled'),
    (121002, 'zhyc-platform', 1210, 'i18n-message-resolve', '词条解析', 'button', NULL, NULL, 'i18n:message:resolve', 2, 'enabled'),
    (1300, 'zhyc-platform', NULL, 'search', '全文检索', 'directory', '/search', 'LAYOUT', 'search:view', 130, 'enabled'),
    (1310, 'zhyc-platform', 1300, 'search-index-config', '全文检索', 'menu', '/search/index-configs', 'search/index-config/index', 'search:index:query', 10, 'enabled'),
    (1400, 'zhyc-platform', NULL, 'monitor', '系统监控', 'directory', '/monitor', 'LAYOUT', 'monitor:view', 140, 'enabled'),
    (1410, 'zhyc-platform', 1400, 'monitor-service', '服务监控', 'menu', '/monitor/services', 'monitor/service/index', 'monitor:service:query', 10, 'enabled'),
    (1411, 'zhyc-platform', 1400, 'monitor-data-source', '数据源监控', 'menu', '/monitor/data-sources', 'monitor/datasource/index', 'monitor:data-source:query', 20, 'enabled'),
    (1412, 'zhyc-platform', 1400, 'monitor-sql', 'SQL 监控', 'menu', '/monitor/sql', 'monitor/sql/index', 'monitor:sql:query', 30, 'enabled'),
    (21001, 'zhyc-platform', 210, 'system-tenant-create', '租户新增', 'button', NULL, NULL, 'system:tenant:create', 1, 'enabled'),
    (21002, 'zhyc-platform', 210, 'system-tenant-update', '租户编辑', 'button', NULL, NULL, 'system:tenant:update', 2, 'enabled'),
    (21003, 'zhyc-platform', 210, 'system-tenant-status', '租户启停', 'button', NULL, NULL, 'system:tenant:update-status', 3, 'enabled'),
    (21004, 'zhyc-platform', 210, 'system-tenant-delete', '租户删除', 'button', NULL, NULL, 'system:tenant:delete', 4, 'enabled'),
    (21101, 'zhyc-platform', 211, 'system-tenant-package-status', '套餐启停', 'button', NULL, NULL, 'system:tenant-package:update', 1, 'enabled'),
    (21201, 'zhyc-platform', 212, 'system-tenant-package-module-bind', '套餐授权绑定', 'button', NULL, NULL, 'system:tenant-package:update', 1, 'enabled'),
    (21301, 'zhyc-platform', 213, 'system-tenant-param-save', '租户参数保存', 'button', NULL, NULL, 'system:tenant-param:save', 1, 'enabled'),
    (21401, 'zhyc-platform', 214, 'system-admin-scope-edit', '管理员范围编辑', 'button', NULL, NULL, 'system:admin:edit', 1, 'enabled'),
    (22001, 'zhyc-platform', 220, 'system-org-create', '组织新增', 'button', NULL, NULL, 'system:org:create', 1, 'enabled'),
    (22002, 'zhyc-platform', 220, 'system-org-update', '组织编辑', 'button', NULL, NULL, 'system:org:update', 2, 'enabled'),
    (22003, 'zhyc-platform', 220, 'system-org-status', '组织启停', 'button', NULL, NULL, 'system:org:update-status', 3, 'enabled'),
    (22004, 'zhyc-platform', 220, 'system-org-delete', '组织删除', 'button', NULL, NULL, 'system:org:delete', 4, 'enabled'),
    (22101, 'zhyc-platform', 221, 'system-post-create', '岗位新增', 'button', NULL, NULL, 'system:post:create', 1, 'enabled'),
    (22102, 'zhyc-platform', 221, 'system-post-update', '岗位编辑', 'button', NULL, NULL, 'system:post:update', 2, 'enabled'),
    (22103, 'zhyc-platform', 221, 'system-post-status', '岗位启停', 'button', NULL, NULL, 'system:post:update-status', 3, 'enabled'),
    (22104, 'zhyc-platform', 221, 'system-post-delete', '岗位删除', 'button', NULL, NULL, 'system:post:delete', 4, 'enabled'),
    (22201, 'zhyc-platform', 222, 'system-user-create', '用户新增', 'button', NULL, NULL, 'system:user:create', 1, 'enabled'),
    (22202, 'zhyc-platform', 222, 'system-user-update', '用户编辑', 'button', NULL, NULL, 'system:user:update', 2, 'enabled'),
    (22203, 'zhyc-platform', 222, 'system-user-status', '用户启停', 'button', NULL, NULL, 'system:user:update-status', 3, 'enabled'),
    (22204, 'zhyc-platform', 222, 'system-user-delete', '用户删除', 'button', NULL, NULL, 'system:user:delete', 4, 'enabled'),
    (22205, 'zhyc-platform', 222, 'system-user-reset-password', '重置密码', 'button', NULL, NULL, 'system:user:reset-password', 5, 'enabled'),
    (22301, 'zhyc-platform', 222, 'system-user-post-bind', '绑定用户岗位', 'button', NULL, NULL, 'system:user:edit', 6, 'enabled'),
    (22401, 'zhyc-platform', 222, 'system-user-role-bind', '绑定用户角色', 'button', NULL, NULL, 'system:user:edit', 7, 'enabled'),
    (23001, 'zhyc-platform', 230, 'system-role-create', '角色新增', 'button', NULL, NULL, 'system:role:create', 1, 'enabled'),
    (23002, 'zhyc-platform', 230, 'system-role-update', '角色编辑', 'button', NULL, NULL, 'system:role:update', 2, 'enabled'),
    (23003, 'zhyc-platform', 230, 'system-role-status', '角色启停', 'button', NULL, NULL, 'system:role:update-status', 3, 'enabled'),
    (23004, 'zhyc-platform', 230, 'system-role-delete', '角色删除', 'button', NULL, NULL, 'system:role:delete', 4, 'enabled'),
    (23005, 'zhyc-platform', 230, 'system-role-authorize', '角色菜单授权', 'button', NULL, NULL, 'system:role:authorize', 5, 'enabled'),
    (24001, 'zhyc-platform', 240, 'system-menu-create', '菜单新增', 'button', NULL, NULL, 'system:permission:create', 1, 'enabled'),
    (24002, 'zhyc-platform', 240, 'system-menu-update', '菜单编辑', 'button', NULL, NULL, 'system:permission:update', 2, 'enabled'),
    (24003, 'zhyc-platform', 240, 'system-menu-status', '菜单启停', 'button', NULL, NULL, 'system:permission:update-status', 3, 'enabled'),
    (24004, 'zhyc-platform', 240, 'system-menu-delete', '菜单删除', 'button', NULL, NULL, 'system:permission:delete', 4, 'enabled'),
    (24101, 'zhyc-platform', 230, 'system-role-data-scope-edit', '角色数据权限编辑', 'button', NULL, NULL, 'system:role:edit', 6, 'enabled'),
    (25001, 'zhyc-platform', 260, 'system-access-restriction-save', '访问限制保存', 'button', NULL, NULL, 'system:access-restriction:save', 6, 'enabled'),
    (25002, 'zhyc-platform', 260, 'system-access-restriction-evaluate', '访问限制校验', 'button', NULL, NULL, 'system:access-restriction:evaluate', 7, 'enabled'),
    (25101, 'zhyc-platform', 251, 'system-password-policy-save', '密码策略保存', 'button', NULL, NULL, 'system:password-policy:save', 1, 'enabled'),
    (25102, 'zhyc-platform', 251, 'system-password-policy-validate', '密码策略校验', 'button', NULL, NULL, 'system:password-policy:validate', 2, 'enabled'),
    (25201, 'zhyc-platform', 252, 'system-module-update', '模块启停', 'button', NULL, NULL, 'system:module:update', 1, 'enabled'),
    (25301, 'zhyc-platform', 253, 'system-code-rule-save', '编码规则保存', 'button', NULL, NULL, 'system:code-rule:save', 1, 'enabled'),
    (25302, 'zhyc-platform', 253, 'system-code-rule-generate', '编码规则生成', 'button', NULL, NULL, 'system:code-rule:generate', 2, 'enabled'),
    (25401, 'zhyc-platform', 254, 'system-param-save', '系统参数保存', 'button', NULL, NULL, 'system:param:save', 1, 'enabled'),
    (25901, 'zhyc-platform', 259, 'system-secret-query', '密钥查看', 'button', NULL, NULL, 'system:secret:query', 1, 'enabled'),
    (25902, 'zhyc-platform', 259, 'system-secret-create', '密钥新增', 'button', NULL, NULL, 'system:secret:create', 2, 'enabled'),
    (25903, 'zhyc-platform', 259, 'system-secret-update', '密钥编辑', 'button', NULL, NULL, 'system:secret:update', 3, 'enabled'),
    (25904, 'zhyc-platform', 259, 'system-secret-delete', '密钥删除', 'button', NULL, NULL, 'system:secret:delete', 4, 'enabled'),
    (25905, 'zhyc-platform', 259, 'system-secret-enable', '密钥启用', 'button', NULL, NULL, 'system:secret:enable', 5, 'enabled'),
    (25906, 'zhyc-platform', 259, 'system-secret-disable', '密钥禁用', 'button', NULL, NULL, 'system:secret:disable', 6, 'enabled'),
    (25907, 'zhyc-platform', 259, 'system-secret-rotate', '密钥轮换', 'button', NULL, NULL, 'system:secret:rotate', 7, 'enabled'),
    (25908, 'zhyc-platform', 259, 'system-secret-copy-ref', '复制引用', 'button', NULL, NULL, 'system:secret:copy-ref', 8, 'enabled'),
    (26001, 'zhyc-platform', 260, 'system-security-protection-save', '保存策略', 'button', NULL, NULL, 'system:security-protection:save', 1, 'enabled'),
    (26002, 'zhyc-platform', 260, 'system-security-protection-block', '封禁 IP', 'button', NULL, NULL, 'system:security-protection:block', 2, 'enabled'),
    (26003, 'zhyc-platform', 260, 'system-security-protection-unblock', '解封 IP', 'button', NULL, NULL, 'system:security-protection:unblock', 3, 'enabled'),
    (26004, 'zhyc-platform', 260, 'system-security-protection-record', '记录事件', 'button', NULL, NULL, 'system:security-protection:record', 4, 'enabled'),
    (25501, 'zhyc-platform', 255, 'system-dict-type-create', '字典类型新增', 'button', NULL, NULL, 'system:dict:create', 1, 'enabled'),
    (25502, 'zhyc-platform', 255, 'system-dict-type-update', '字典类型编辑', 'button', NULL, NULL, 'system:dict:update', 2, 'enabled'),
    (25503, 'zhyc-platform', 255, 'system-dict-type-delete', '字典类型删除', 'button', NULL, NULL, 'system:dict:delete', 3, 'enabled'),
    (25504, 'zhyc-platform', 255, 'system-dict-item-create', '字典项新增', 'button', NULL, NULL, 'system:dict:item:create', 4, 'enabled'),
    (25505, 'zhyc-platform', 255, 'system-dict-item-update', '字典项编辑', 'button', NULL, NULL, 'system:dict:item:update', 5, 'enabled'),
    (25506, 'zhyc-platform', 255, 'system-dict-item-delete', '字典项删除', 'button', NULL, NULL, 'system:dict:item:delete', 6, 'enabled'),
    (81101, 'zhyc-platform', 811, 'file-object-upload', '文件上传', 'button', NULL, NULL, 'file:object:upload', 1, 'enabled')
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
WHERE tenant_id = 'zhyc-platform';

INSERT IGNORE INTO sys_password_policy (
    tenant_id, policy_code, policy_name, min_length, require_uppercase, require_lowercase,
    require_digit, require_special, expire_days, history_count, max_retry_count, lock_minutes, enabled
)
VALUES ('zhyc-platform', 'default', '默认密码策略', 10, 0, 1, 1, 0, 90, 3, 5, 30, 1);

INSERT IGNORE INTO sys_code_rule (tenant_id, rule_code, rule_name, prefix, date_pattern, sequence_length, current_value, enabled)
VALUES
    ('zhyc-platform', 'purchase-request', '采购申请编码', 'PR', 'yyyyMMdd', 5, 0, 1),
    ('zhyc-platform', 'purchase-order', '采购订单编码', 'PO', 'yyyyMMdd', 5, 0, 1);

INSERT IGNORE INTO sys_param (tenant_id, param_key, param_value, value_type, system_flag, editable)
VALUES
    ('zhyc-platform', 'platform.name', 'ZHYC 快速开发平台', 'string', 1, 1),
    ('zhyc-platform', 'security.login.maxRetryCount', '5', 'number', 1, 1),
    ('zhyc-platform', 'security.password.resetRequired', 'true', 'boolean', 1, 1);

INSERT IGNORE INTO sys_dict_type (tenant_id, dict_code, dict_name, system_flag, status)
VALUES
    ('zhyc-platform', 'common_status', '通用状态', 1, 'enabled'),
    ('zhyc-platform', 'audit_result', '审计结果', 1, 'enabled'),
    ('zhyc-platform', 'workflow_task_status', '流程任务状态', 1, 'enabled');

INSERT IGNORE INTO sys_dict_item (tenant_id, dict_code, item_label, item_value, item_color, sort_order, status)
VALUES
    ('zhyc-platform', 'common_status', '启用', 'enabled', 'green', 1, 'enabled'),
    ('zhyc-platform', 'common_status', '停用', 'disabled', 'red', 2, 'enabled'),
    ('zhyc-platform', 'audit_result', '成功', 'success', 'green', 1, 'enabled'),
    ('zhyc-platform', 'audit_result', '失败', 'failed', 'red', 2, 'enabled'),
    ('zhyc-platform', 'workflow_task_status', '待办', 'todo', 'blue', 1, 'enabled'),
    ('zhyc-platform', 'workflow_task_status', '已办', 'done', 'green', 2, 'enabled');

INSERT IGNORE INTO sys_module (id, module_code, module_name, version, module_type, enabled)
VALUES
    (1, 'system', '系统管理', '0.0.1', 'core', 1),
    (2, 'lowcode', '低代码中心', '0.0.1', 'core', 1),
    (3, 'workflow', '工作流中心', '0.0.1', 'core', 1),
    (4, 'openapi', '开放平台', '0.0.1', 'core', 1),
    (5, 'purchase', '采购样板', '0.0.1', 'sample', 1),
    (6, 'message', '消息中心', '0.0.1', 'extension', 1),
    (7, 'file', '文件中心', '0.0.1', 'extension', 1),
    (8, 'job', '在线作业', '0.0.1', 'extension', 1),
    (9, 'search', '全文检索', '0.0.1', 'extension', 1),
    (10, 'visual', '可视化大屏', '0.0.1', 'extension', 1),
    (11, 'i18n', '国际化', '0.0.1', 'extension', 1),
    (12, 'cms', '内容管理', '0.0.1', 'extension', 1),
    (13, 'ai', 'AI 能力中心', '0.0.1', 'core', 1);

INSERT IGNORE INTO sys_tenant_package_module (package_id, module_code, menu_code, permission)
SELECT 1, module_code, NULL, NULL
FROM sys_module
WHERE enabled = 1;

-- 初始化脚本结束。
-- 如需启用 admin 登录，请运行 PlatformPasswordHashCli 生成 Shiro 密码哈希，并替换 replace_with_shiro_password_hash。
