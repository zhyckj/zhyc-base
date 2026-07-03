-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

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
