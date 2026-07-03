-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- ZHYC 快速开发平台 达梦数据库 初始化脚本
-- 说明：由 zhyc-base-server/scripts/build-database-init-sql.mjs 从模块 DDL 生成。
-- 说明：当前脚本只包含表结构初始化；基础种子数据需按目标数据库单独审阅后导入。

-- ============================================================
-- 1. 认证中心核心表
-- 来源：zhyc-base-server/zhyc-auth-server/src/main/resources/db/V1__auth_server_core.sql
-- ============================================================
CREATE TABLE "oauth2_registered_client" (
    "id" VARCHAR(100) NOT NULL,
    "client_id" VARCHAR(100) NOT NULL,
    "client_id_issued_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "client_secret" VARCHAR(200) DEFAULT NULL,
    "client_secret_expires_at" TIMESTAMP NULL DEFAULT NULL,
    "client_name" VARCHAR(200) NOT NULL,
    "client_authentication_methods" VARCHAR(1000) NOT NULL,
    "authorization_grant_types" VARCHAR(1000) NOT NULL,
    "redirect_uris" VARCHAR(1000) DEFAULT NULL,
    "post_logout_redirect_uris" VARCHAR(1000) DEFAULT NULL,
    "scopes" VARCHAR(1000) NOT NULL,
    "client_settings" VARCHAR(2000) NOT NULL,
    "token_settings" VARCHAR(2000) NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_oauth2_registered_client_client_id" UNIQUE ("client_id")
);
COMMENT ON TABLE "oauth2_registered_client" IS 'OAuth2 注册客户端';
COMMENT ON COLUMN "oauth2_registered_client"."id" IS '注册客户端主键';
COMMENT ON COLUMN "oauth2_registered_client"."client_id" IS 'OAuth2 客户端标识';
COMMENT ON COLUMN "oauth2_registered_client"."client_id_issued_at" IS '客户端标识签发时间';
COMMENT ON COLUMN "oauth2_registered_client"."client_secret" IS 'BCrypt 编码后的客户端密钥';
COMMENT ON COLUMN "oauth2_registered_client"."client_secret_expires_at" IS '客户端密钥过期时间';
COMMENT ON COLUMN "oauth2_registered_client"."client_name" IS '客户端名称';
COMMENT ON COLUMN "oauth2_registered_client"."client_authentication_methods" IS '客户端认证方式集合';
COMMENT ON COLUMN "oauth2_registered_client"."authorization_grant_types" IS '授权模式集合';
COMMENT ON COLUMN "oauth2_registered_client"."redirect_uris" IS '授权码回调地址集合';
COMMENT ON COLUMN "oauth2_registered_client"."post_logout_redirect_uris" IS '登出回调地址集合';
COMMENT ON COLUMN "oauth2_registered_client"."scopes" IS '授权范围集合';
COMMENT ON COLUMN "oauth2_registered_client"."client_settings" IS '客户端设置 JSON';
COMMENT ON COLUMN "oauth2_registered_client"."token_settings" IS '令牌设置 JSON';

CREATE TABLE "oauth2_authorization" (
    "id" VARCHAR(100) NOT NULL,
    "registered_client_id" VARCHAR(100) NOT NULL,
    "principal_name" VARCHAR(200) NOT NULL,
    "authorization_grant_type" VARCHAR(100) NOT NULL,
    "authorized_scopes" VARCHAR(1000) DEFAULT NULL,
    "attributes" BLOB DEFAULT NULL,
    "state" VARCHAR(500) DEFAULT NULL,
    "authorization_code_value" BLOB DEFAULT NULL,
    "authorization_code_issued_at" TIMESTAMP NULL DEFAULT NULL,
    "authorization_code_expires_at" TIMESTAMP NULL DEFAULT NULL,
    "authorization_code_metadata" BLOB DEFAULT NULL,
    "access_token_value" BLOB DEFAULT NULL,
    "access_token_issued_at" TIMESTAMP NULL DEFAULT NULL,
    "access_token_expires_at" TIMESTAMP NULL DEFAULT NULL,
    "access_token_metadata" BLOB DEFAULT NULL,
    "access_token_type" VARCHAR(100) DEFAULT NULL,
    "access_token_scopes" VARCHAR(1000) DEFAULT NULL,
    "oidc_id_token_value" BLOB DEFAULT NULL,
    "oidc_id_token_issued_at" TIMESTAMP NULL DEFAULT NULL,
    "oidc_id_token_expires_at" TIMESTAMP NULL DEFAULT NULL,
    "oidc_id_token_metadata" BLOB DEFAULT NULL,
    "refresh_token_value" BLOB DEFAULT NULL,
    "refresh_token_issued_at" TIMESTAMP NULL DEFAULT NULL,
    "refresh_token_expires_at" TIMESTAMP NULL DEFAULT NULL,
    "refresh_token_metadata" BLOB DEFAULT NULL,
    "user_code_value" BLOB DEFAULT NULL,
    "user_code_issued_at" TIMESTAMP NULL DEFAULT NULL,
    "user_code_expires_at" TIMESTAMP NULL DEFAULT NULL,
    "user_code_metadata" BLOB DEFAULT NULL,
    "device_code_value" BLOB DEFAULT NULL,
    "device_code_issued_at" TIMESTAMP NULL DEFAULT NULL,
    "device_code_expires_at" TIMESTAMP NULL DEFAULT NULL,
    "device_code_metadata" BLOB DEFAULT NULL,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "oauth2_authorization" IS 'OAuth2 授权记录';
COMMENT ON COLUMN "oauth2_authorization"."id" IS '授权记录主键';
COMMENT ON COLUMN "oauth2_authorization"."registered_client_id" IS '注册客户端主键';
COMMENT ON COLUMN "oauth2_authorization"."principal_name" IS '授权主体名称';
COMMENT ON COLUMN "oauth2_authorization"."authorization_grant_type" IS '授权模式';
COMMENT ON COLUMN "oauth2_authorization"."authorized_scopes" IS '已授权范围集合';
COMMENT ON COLUMN "oauth2_authorization"."attributes" IS '授权属性 JSON';
COMMENT ON COLUMN "oauth2_authorization"."state" IS '授权请求状态值';
COMMENT ON COLUMN "oauth2_authorization"."authorization_code_value" IS '授权码密文';
COMMENT ON COLUMN "oauth2_authorization"."authorization_code_issued_at" IS '授权码签发时间';
COMMENT ON COLUMN "oauth2_authorization"."authorization_code_expires_at" IS '授权码过期时间';
COMMENT ON COLUMN "oauth2_authorization"."authorization_code_metadata" IS '授权码元数据 JSON';
COMMENT ON COLUMN "oauth2_authorization"."access_token_value" IS '访问令牌密文';
COMMENT ON COLUMN "oauth2_authorization"."access_token_issued_at" IS '访问令牌签发时间';
COMMENT ON COLUMN "oauth2_authorization"."access_token_expires_at" IS '访问令牌过期时间';
COMMENT ON COLUMN "oauth2_authorization"."access_token_metadata" IS '访问令牌元数据 JSON';
COMMENT ON COLUMN "oauth2_authorization"."access_token_type" IS '访问令牌类型';
COMMENT ON COLUMN "oauth2_authorization"."access_token_scopes" IS '访问令牌范围集合';
COMMENT ON COLUMN "oauth2_authorization"."oidc_id_token_value" IS 'OIDC ID Token 密文';
COMMENT ON COLUMN "oauth2_authorization"."oidc_id_token_issued_at" IS 'OIDC ID Token 签发时间';
COMMENT ON COLUMN "oauth2_authorization"."oidc_id_token_expires_at" IS 'OIDC ID Token 过期时间';
COMMENT ON COLUMN "oauth2_authorization"."oidc_id_token_metadata" IS 'OIDC ID Token 元数据 JSON';
COMMENT ON COLUMN "oauth2_authorization"."refresh_token_value" IS '刷新令牌密文';
COMMENT ON COLUMN "oauth2_authorization"."refresh_token_issued_at" IS '刷新令牌签发时间';
COMMENT ON COLUMN "oauth2_authorization"."refresh_token_expires_at" IS '刷新令牌过期时间';
COMMENT ON COLUMN "oauth2_authorization"."refresh_token_metadata" IS '刷新令牌元数据 JSON';
COMMENT ON COLUMN "oauth2_authorization"."user_code_value" IS '设备授权用户码密文';
COMMENT ON COLUMN "oauth2_authorization"."user_code_issued_at" IS '设备授权用户码签发时间';
COMMENT ON COLUMN "oauth2_authorization"."user_code_expires_at" IS '设备授权用户码过期时间';
COMMENT ON COLUMN "oauth2_authorization"."user_code_metadata" IS '设备授权用户码元数据 JSON';
COMMENT ON COLUMN "oauth2_authorization"."device_code_value" IS '设备授权设备码密文';
COMMENT ON COLUMN "oauth2_authorization"."device_code_issued_at" IS '设备授权设备码签发时间';
COMMENT ON COLUMN "oauth2_authorization"."device_code_expires_at" IS '设备授权设备码过期时间';
COMMENT ON COLUMN "oauth2_authorization"."device_code_metadata" IS '设备授权设备码元数据 JSON';
CREATE INDEX "idx_oauth2_authorization_client_principal" ON "oauth2_authorization" ("registered_client_id", "principal_name");

CREATE TABLE "oauth2_authorization_consent" (
    "registered_client_id" VARCHAR(100) NOT NULL,
    "principal_name" VARCHAR(200) NOT NULL,
    "authorities" VARCHAR(1000) NOT NULL,
    PRIMARY KEY ("registered_client_id", "principal_name")
);
COMMENT ON TABLE "oauth2_authorization_consent" IS 'OAuth2 授权确认';
COMMENT ON COLUMN "oauth2_authorization_consent"."registered_client_id" IS '注册客户端主键';
COMMENT ON COLUMN "oauth2_authorization_consent"."principal_name" IS '授权主体名称';
COMMENT ON COLUMN "oauth2_authorization_consent"."authorities" IS '已确认授权范围集合';

-- ============================================================
-- 2. 系统与租户核心表
-- 来源：zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql
-- ============================================================
-- sys_tenant.tenant_id 是租户业务编码，不是所属租户字段；平台级租户管理查询需要绕过普通租户过滤。
CREATE TABLE "sys_tenant" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "tenant_name" VARCHAR(128) NOT NULL,
    "package_id" BIGINT DEFAULT NULL,
    "isolation_mode" VARCHAR(32) NOT NULL DEFAULT 'TENANT_COLUMN',
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "contact_name" VARCHAR(64) DEFAULT NULL,
    "contact_phone" VARCHAR(32) DEFAULT NULL,
    "expire_at" TIMESTAMP DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_tenant_tenant_id" UNIQUE ("tenant_id")
);
COMMENT ON TABLE "sys_tenant" IS '租户主表';
COMMENT ON COLUMN "sys_tenant"."id" IS '主键';
COMMENT ON COLUMN "sys_tenant"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_tenant"."tenant_name" IS '租户名称';
COMMENT ON COLUMN "sys_tenant"."package_id" IS '当前租户套餐 ID';
COMMENT ON COLUMN "sys_tenant"."isolation_mode" IS '租户隔离模式';
COMMENT ON COLUMN "sys_tenant"."status" IS '租户状态';
COMMENT ON COLUMN "sys_tenant"."contact_name" IS '租户联系人';
COMMENT ON COLUMN "sys_tenant"."contact_phone" IS '联系电话';
COMMENT ON COLUMN "sys_tenant"."expire_at" IS '到期时间';
COMMENT ON COLUMN "sys_tenant"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_tenant"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_tenant_package" ON "sys_tenant" ("package_id");
CREATE INDEX "idx_sys_tenant_status" ON "sys_tenant" ("status");

CREATE TABLE "sys_tenant_package" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "package_code" VARCHAR(64) NOT NULL,
    "package_name" VARCHAR(128) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "max_user_count" INT NOT NULL DEFAULT 0,
    "max_storage_mb" INT NOT NULL DEFAULT 0,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_tenant_package_code" UNIQUE ("package_code")
);
COMMENT ON TABLE "sys_tenant_package" IS '租户套餐表';
COMMENT ON COLUMN "sys_tenant_package"."id" IS '主键';
COMMENT ON COLUMN "sys_tenant_package"."package_code" IS '套餐编码';
COMMENT ON COLUMN "sys_tenant_package"."package_name" IS '套餐名称';
COMMENT ON COLUMN "sys_tenant_package"."status" IS '套餐状态';
COMMENT ON COLUMN "sys_tenant_package"."max_user_count" IS '最大用户数';
COMMENT ON COLUMN "sys_tenant_package"."max_storage_mb" IS '最大存储容量 MB';
COMMENT ON COLUMN "sys_tenant_package"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_tenant_package"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_tenant_package_status" ON "sys_tenant_package" ("status");

CREATE TABLE "sys_tenant_package_module" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "package_id" BIGINT NOT NULL,
    "module_code" VARCHAR(64) NOT NULL,
    "menu_code" VARCHAR(64) DEFAULT NULL,
    "permission" VARCHAR(128) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_tenant_package_module_resource" UNIQUE ("package_id", "module_code", "menu_code", "permission")
);
COMMENT ON TABLE "sys_tenant_package_module" IS '租户套餐模块授权表';
COMMENT ON COLUMN "sys_tenant_package_module"."id" IS '主键';
COMMENT ON COLUMN "sys_tenant_package_module"."package_id" IS '租户套餐主键';
COMMENT ON COLUMN "sys_tenant_package_module"."module_code" IS '模块编码';
COMMENT ON COLUMN "sys_tenant_package_module"."menu_code" IS '菜单编码';
COMMENT ON COLUMN "sys_tenant_package_module"."permission" IS '权限标识';
COMMENT ON COLUMN "sys_tenant_package_module"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_tenant_package_module_package" ON "sys_tenant_package_module" ("package_id");
CREATE INDEX "idx_sys_tenant_package_module_module" ON "sys_tenant_package_module" ("module_code");

CREATE TABLE "sys_tenant_param" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "param_key" VARCHAR(128) NOT NULL,
    "param_value" VARCHAR(1000) DEFAULT NULL,
    "value_type" VARCHAR(32) NOT NULL DEFAULT 'string',
    "visible" NUMBER(3) NOT NULL DEFAULT 1,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_tenant_param_key" UNIQUE ("tenant_id", "param_key")
);
COMMENT ON TABLE "sys_tenant_param" IS '租户参数表';
COMMENT ON COLUMN "sys_tenant_param"."id" IS '主键';
COMMENT ON COLUMN "sys_tenant_param"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_tenant_param"."param_key" IS '参数键';
COMMENT ON COLUMN "sys_tenant_param"."param_value" IS '参数值';
COMMENT ON COLUMN "sys_tenant_param"."value_type" IS '参数值类型';
COMMENT ON COLUMN "sys_tenant_param"."visible" IS '是否显示';
COMMENT ON COLUMN "sys_tenant_param"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_tenant_param"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_tenant_param_tenant_visible" ON "sys_tenant_param" ("tenant_id", "visible");

CREATE TABLE "sys_user" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "username" VARCHAR(64) NOT NULL,
    "nickname" VARCHAR(128) DEFAULT NULL,
    "password_hash" VARCHAR(255) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_user_tenant_id" UNIQUE ("tenant_id", "id"),
    CONSTRAINT "uk_sys_user_tenant_username" UNIQUE ("tenant_id", "username")
);
COMMENT ON TABLE "sys_user" IS '系统用户表';
COMMENT ON COLUMN "sys_user"."id" IS '主键';
COMMENT ON COLUMN "sys_user"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_user"."username" IS '登录账号';
COMMENT ON COLUMN "sys_user"."nickname" IS '用户显示名称';
COMMENT ON COLUMN "sys_user"."password_hash" IS '密码哈希值';
COMMENT ON COLUMN "sys_user"."status" IS '用户状态';
COMMENT ON COLUMN "sys_user"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_user"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_user_tenant_status" ON "sys_user" ("tenant_id", "status");

CREATE TABLE "sys_role" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "role_code" VARCHAR(64) NOT NULL,
    "name" VARCHAR(128) NOT NULL,
    "data_scope" VARCHAR(32) NOT NULL DEFAULT 'SELF',
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_role_tenant_id" UNIQUE ("tenant_id", "id"),
    CONSTRAINT "uk_sys_role_tenant_code" UNIQUE ("tenant_id", "role_code")
);
COMMENT ON TABLE "sys_role" IS '系统角色表';
COMMENT ON COLUMN "sys_role"."id" IS '主键';
COMMENT ON COLUMN "sys_role"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_role"."role_code" IS '角色编码';
COMMENT ON COLUMN "sys_role"."name" IS '角色名称';
COMMENT ON COLUMN "sys_role"."data_scope" IS '数据权限范围';
COMMENT ON COLUMN "sys_role"."status" IS '角色状态';
COMMENT ON COLUMN "sys_role"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_role"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_role_tenant_status" ON "sys_role" ("tenant_id", "status");

CREATE TABLE "sys_menu" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "parent_id" BIGINT DEFAULT NULL,
    "menu_code" VARCHAR(64) NOT NULL,
    "menu_name" VARCHAR(128) NOT NULL,
    "menu_type" VARCHAR(32) NOT NULL,
    "path" VARCHAR(255) DEFAULT NULL,
    "component" VARCHAR(255) DEFAULT NULL,
    "permission" VARCHAR(128) DEFAULT NULL,
    "sort_order" INT NOT NULL DEFAULT 0,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_menu_tenant_id" UNIQUE ("tenant_id", "id"),
    CONSTRAINT "uk_sys_menu_tenant_code" UNIQUE ("tenant_id", "menu_code")
);
COMMENT ON TABLE "sys_menu" IS '系统菜单表';
COMMENT ON COLUMN "sys_menu"."id" IS '主键';
COMMENT ON COLUMN "sys_menu"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_menu"."parent_id" IS '父级菜单主键';
COMMENT ON COLUMN "sys_menu"."menu_code" IS '菜单编码';
COMMENT ON COLUMN "sys_menu"."menu_name" IS '菜单名称';
COMMENT ON COLUMN "sys_menu"."menu_type" IS '菜单类型';
COMMENT ON COLUMN "sys_menu"."path" IS '前端路由路径';
COMMENT ON COLUMN "sys_menu"."component" IS '前端组件路径';
COMMENT ON COLUMN "sys_menu"."permission" IS '权限标识';
COMMENT ON COLUMN "sys_menu"."sort_order" IS '排序号';
COMMENT ON COLUMN "sys_menu"."status" IS '菜单状态';
COMMENT ON COLUMN "sys_menu"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_menu"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_menu_tenant_parent" ON "sys_menu" ("tenant_id", "parent_id");
CREATE INDEX "idx_sys_menu_tenant_permission" ON "sys_menu" ("tenant_id", "permission");

CREATE TABLE "sys_org" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "parent_id" BIGINT DEFAULT NULL,
    "ancestors" VARCHAR(500) NOT NULL DEFAULT '0',
    "org_code" VARCHAR(64) NOT NULL,
    "org_name" VARCHAR(128) NOT NULL,
    "leader_user_id" BIGINT DEFAULT NULL,
    "sort_order" INT NOT NULL DEFAULT 0,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_org_tenant_id" UNIQUE ("tenant_id", "id"),
    CONSTRAINT "uk_sys_org_tenant_code" UNIQUE ("tenant_id", "org_code")
);
COMMENT ON TABLE "sys_org" IS '系统组织机构表';
COMMENT ON COLUMN "sys_org"."id" IS '主键';
COMMENT ON COLUMN "sys_org"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_org"."parent_id" IS '父级组织主键';
COMMENT ON COLUMN "sys_org"."ancestors" IS '祖级组织路径';
COMMENT ON COLUMN "sys_org"."org_code" IS '组织编码';
COMMENT ON COLUMN "sys_org"."org_name" IS '组织名称';
COMMENT ON COLUMN "sys_org"."leader_user_id" IS '负责人用户主键';
COMMENT ON COLUMN "sys_org"."sort_order" IS '排序号';
COMMENT ON COLUMN "sys_org"."status" IS '组织状态';
COMMENT ON COLUMN "sys_org"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_org"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_org_tenant_parent" ON "sys_org" ("tenant_id", "parent_id");
CREATE INDEX "idx_sys_org_tenant_status" ON "sys_org" ("tenant_id", "status");

CREATE TABLE "sys_post" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "org_id" BIGINT DEFAULT NULL,
    "post_code" VARCHAR(64) NOT NULL,
    "post_name" VARCHAR(128) NOT NULL,
    "sort_order" INT NOT NULL DEFAULT 0,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_post_tenant_id" UNIQUE ("tenant_id", "id"),
    CONSTRAINT "uk_sys_post_tenant_code" UNIQUE ("tenant_id", "post_code"),
    CONSTRAINT "fk_sys_post_org" FOREIGN KEY ("tenant_id", "org_id") REFERENCES "sys_org" ("tenant_id", "id")
);
COMMENT ON TABLE "sys_post" IS '系统岗位表';
COMMENT ON COLUMN "sys_post"."id" IS '主键';
COMMENT ON COLUMN "sys_post"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_post"."org_id" IS '所属组织主键';
COMMENT ON COLUMN "sys_post"."post_code" IS '岗位编码';
COMMENT ON COLUMN "sys_post"."post_name" IS '岗位名称';
COMMENT ON COLUMN "sys_post"."sort_order" IS '排序号';
COMMENT ON COLUMN "sys_post"."status" IS '岗位状态';
COMMENT ON COLUMN "sys_post"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_post"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_post_tenant_org" ON "sys_post" ("tenant_id", "org_id");
CREATE INDEX "idx_sys_post_tenant_status" ON "sys_post" ("tenant_id", "status");

CREATE TABLE "sys_user_post" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "user_id" BIGINT NOT NULL,
    "post_id" BIGINT NOT NULL,
    "primary_flag" NUMBER(1) NOT NULL DEFAULT 0,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_user_post_tenant_user_post" UNIQUE ("tenant_id", "user_id", "post_id"),
    CONSTRAINT "fk_sys_user_post_user" FOREIGN KEY ("tenant_id", "user_id") REFERENCES "sys_user" ("tenant_id", "id"),
    CONSTRAINT "fk_sys_user_post_post" FOREIGN KEY ("tenant_id", "post_id") REFERENCES "sys_post" ("tenant_id", "id")
);
COMMENT ON TABLE "sys_user_post" IS '用户岗位关联表';
COMMENT ON COLUMN "sys_user_post"."id" IS '主键';
COMMENT ON COLUMN "sys_user_post"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_user_post"."user_id" IS '用户主键';
COMMENT ON COLUMN "sys_user_post"."post_id" IS '岗位主键';
COMMENT ON COLUMN "sys_user_post"."primary_flag" IS '是否主岗位';
COMMENT ON COLUMN "sys_user_post"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_user_post_tenant_post" ON "sys_user_post" ("tenant_id", "post_id");
CREATE INDEX "idx_sys_user_post_tenant_primary" ON "sys_user_post" ("tenant_id", "user_id", "primary_flag");

CREATE TABLE "sys_user_role" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "user_id" BIGINT NOT NULL,
    "role_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_user_role_tenant_user_role" UNIQUE ("tenant_id", "user_id", "role_id"),
    CONSTRAINT "fk_sys_user_role_user" FOREIGN KEY ("tenant_id", "user_id") REFERENCES "sys_user" ("tenant_id", "id"),
    CONSTRAINT "fk_sys_user_role_role" FOREIGN KEY ("tenant_id", "role_id") REFERENCES "sys_role" ("tenant_id", "id")
);
COMMENT ON TABLE "sys_user_role" IS '用户角色关联表';
COMMENT ON COLUMN "sys_user_role"."id" IS '主键';
COMMENT ON COLUMN "sys_user_role"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_user_role"."user_id" IS '用户主键';
COMMENT ON COLUMN "sys_user_role"."role_id" IS '角色主键';
COMMENT ON COLUMN "sys_user_role"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_user_role_tenant_role" ON "sys_user_role" ("tenant_id", "role_id");

CREATE TABLE "sys_admin_scope" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "user_id" BIGINT NOT NULL,
    "scope_type" VARCHAR(32) NOT NULL,
    "scope_ref_code" VARCHAR(128) NOT NULL,
    "scope_name" VARCHAR(128) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_admin_scope" UNIQUE ("tenant_id", "user_id", "scope_type", "scope_ref_code"),
    CONSTRAINT "fk_sys_admin_scope_user" FOREIGN KEY ("tenant_id", "user_id") REFERENCES "sys_user" ("tenant_id", "id")
);
COMMENT ON TABLE "sys_admin_scope" IS '管理员管理范围表';
COMMENT ON COLUMN "sys_admin_scope"."id" IS '主键';
COMMENT ON COLUMN "sys_admin_scope"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_admin_scope"."user_id" IS '管理员用户主键';
COMMENT ON COLUMN "sys_admin_scope"."scope_type" IS '范围类型';
COMMENT ON COLUMN "sys_admin_scope"."scope_ref_code" IS '范围引用编码';
COMMENT ON COLUMN "sys_admin_scope"."scope_name" IS '范围展示名称';
COMMENT ON COLUMN "sys_admin_scope"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_admin_scope_user" ON "sys_admin_scope" ("tenant_id", "user_id");
CREATE INDEX "idx_sys_admin_scope_ref" ON "sys_admin_scope" ("tenant_id", "scope_type", "scope_ref_code");

CREATE TABLE "sys_role_menu" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "role_id" BIGINT NOT NULL,
    "menu_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_role_menu_tenant_role_menu" UNIQUE ("tenant_id", "role_id", "menu_id"),
    CONSTRAINT "fk_sys_role_menu_role" FOREIGN KEY ("tenant_id", "role_id") REFERENCES "sys_role" ("tenant_id", "id"),
    CONSTRAINT "fk_sys_role_menu_menu" FOREIGN KEY ("tenant_id", "menu_id") REFERENCES "sys_menu" ("tenant_id", "id")
);
COMMENT ON TABLE "sys_role_menu" IS '角色菜单关联表';
COMMENT ON COLUMN "sys_role_menu"."id" IS '主键';
COMMENT ON COLUMN "sys_role_menu"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_role_menu"."role_id" IS '角色主键';
COMMENT ON COLUMN "sys_role_menu"."menu_id" IS '菜单主键';
COMMENT ON COLUMN "sys_role_menu"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_role_menu_tenant_menu" ON "sys_role_menu" ("tenant_id", "menu_id");

CREATE TABLE "sys_role_data_scope" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "role_id" BIGINT NOT NULL,
    "org_id" BIGINT NOT NULL,
    "scope_type" VARCHAR(32) NOT NULL DEFAULT 'org',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_role_data_scope" UNIQUE ("tenant_id", "role_id", "org_id", "scope_type"),
    CONSTRAINT "fk_sys_role_data_scope_role" FOREIGN KEY ("tenant_id", "role_id") REFERENCES "sys_role" ("tenant_id", "id"),
    CONSTRAINT "fk_sys_role_data_scope_org" FOREIGN KEY ("tenant_id", "org_id") REFERENCES "sys_org" ("tenant_id", "id")
);
COMMENT ON TABLE "sys_role_data_scope" IS '角色自定义数据权限表';
COMMENT ON COLUMN "sys_role_data_scope"."id" IS '主键';
COMMENT ON COLUMN "sys_role_data_scope"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_role_data_scope"."role_id" IS '角色主键';
COMMENT ON COLUMN "sys_role_data_scope"."org_id" IS '授权组织主键';
COMMENT ON COLUMN "sys_role_data_scope"."scope_type" IS '范围类型';
COMMENT ON COLUMN "sys_role_data_scope"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_role_data_scope_org" ON "sys_role_data_scope" ("tenant_id", "org_id");

CREATE TABLE "sys_login_log" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "user_id" BIGINT DEFAULT NULL,
    "username" VARCHAR(64) DEFAULT NULL,
    "login_type" VARCHAR(32) NOT NULL,
    "result" VARCHAR(32) NOT NULL,
    "client_ip" VARCHAR(64) DEFAULT NULL,
    "user_agent" VARCHAR(512) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "sys_login_log" IS '系统登录日志表';
COMMENT ON COLUMN "sys_login_log"."id" IS '主键';
COMMENT ON COLUMN "sys_login_log"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_login_log"."user_id" IS '登录用户主键';
COMMENT ON COLUMN "sys_login_log"."username" IS '登录账号';
COMMENT ON COLUMN "sys_login_log"."login_type" IS '登录方式';
COMMENT ON COLUMN "sys_login_log"."result" IS '登录结果';
COMMENT ON COLUMN "sys_login_log"."client_ip" IS '客户端 IP';
COMMENT ON COLUMN "sys_login_log"."user_agent" IS '浏览器或客户端 User-Agent';
COMMENT ON COLUMN "sys_login_log"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_login_log_tenant_created" ON "sys_login_log" ("tenant_id", "created_at");
CREATE INDEX "idx_sys_login_log_tenant_user" ON "sys_login_log" ("tenant_id", "user_id");
CREATE INDEX "idx_sys_login_log_tenant_result" ON "sys_login_log" ("tenant_id", "result");

CREATE TABLE "sys_exception_log" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "trace_id" VARCHAR(128) DEFAULT NULL,
    "user_id" BIGINT DEFAULT NULL,
    "username" VARCHAR(64) DEFAULT NULL,
    "request_uri" VARCHAR(255) NOT NULL,
    "request_method" VARCHAR(16) NOT NULL,
    "exception_name" VARCHAR(255) NOT NULL,
    "message" CLOB,
    "stack_trace" CLOB,
    "client_ip" VARCHAR(64) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "sys_exception_log" IS '系统异常日志表';
COMMENT ON COLUMN "sys_exception_log"."id" IS '主键';
COMMENT ON COLUMN "sys_exception_log"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_exception_log"."trace_id" IS '链路追踪编号';
COMMENT ON COLUMN "sys_exception_log"."user_id" IS '操作用户主键';
COMMENT ON COLUMN "sys_exception_log"."username" IS '操作账号';
COMMENT ON COLUMN "sys_exception_log"."request_uri" IS '请求地址';
COMMENT ON COLUMN "sys_exception_log"."request_method" IS '请求方法';
COMMENT ON COLUMN "sys_exception_log"."exception_name" IS '异常类名';
COMMENT ON COLUMN "sys_exception_log"."message" IS '异常消息';
COMMENT ON COLUMN "sys_exception_log"."stack_trace" IS '异常堆栈';
COMMENT ON COLUMN "sys_exception_log"."client_ip" IS '客户端 IP';
COMMENT ON COLUMN "sys_exception_log"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_exception_log_tenant_created" ON "sys_exception_log" ("tenant_id", "created_at");
CREATE INDEX "idx_sys_exception_log_tenant_trace" ON "sys_exception_log" ("tenant_id", "trace_id");
CREATE INDEX "idx_sys_exception_log_tenant_exception" ON "sys_exception_log" ("tenant_id", "exception_name");

CREATE TABLE "sys_permission_audit" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "operator_id" BIGINT DEFAULT NULL,
    "target_type" VARCHAR(64) NOT NULL,
    "target_id" VARCHAR(128) NOT NULL,
    "before_value" CLOB,
    "after_value" CLOB,
    "change_type" VARCHAR(64) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "sys_permission_audit" IS '系统权限变更审计表';
COMMENT ON COLUMN "sys_permission_audit"."id" IS '主键';
COMMENT ON COLUMN "sys_permission_audit"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_permission_audit"."operator_id" IS '操作者用户主键';
COMMENT ON COLUMN "sys_permission_audit"."target_type" IS '目标类型';
COMMENT ON COLUMN "sys_permission_audit"."target_id" IS '目标业务标识';
COMMENT ON COLUMN "sys_permission_audit"."before_value" IS '变更前内容';
COMMENT ON COLUMN "sys_permission_audit"."after_value" IS '变更后内容';
COMMENT ON COLUMN "sys_permission_audit"."change_type" IS '变更类型';
COMMENT ON COLUMN "sys_permission_audit"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_permission_audit_tenant_created" ON "sys_permission_audit" ("tenant_id", "created_at");
CREATE INDEX "idx_sys_permission_audit_tenant_operator" ON "sys_permission_audit" ("tenant_id", "operator_id");
CREATE INDEX "idx_sys_permission_audit_tenant_target" ON "sys_permission_audit" ("tenant_id", "target_type", "target_id");

CREATE TABLE "sys_audit_log" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "user_id" BIGINT DEFAULT NULL,
    "username" VARCHAR(64) DEFAULT NULL,
    "action" VARCHAR(128) NOT NULL,
    "target_type" VARCHAR(64) DEFAULT NULL,
    "target_id" VARCHAR(128) DEFAULT NULL,
    "result" VARCHAR(32) NOT NULL,
    "client_ip" VARCHAR(64) DEFAULT NULL,
    "detail" CLOB,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "sys_audit_log" IS '系统审计日志表';
COMMENT ON COLUMN "sys_audit_log"."id" IS '主键';
COMMENT ON COLUMN "sys_audit_log"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_audit_log"."user_id" IS '操作用户主键';
COMMENT ON COLUMN "sys_audit_log"."username" IS '操作账号';
COMMENT ON COLUMN "sys_audit_log"."action" IS '操作动作';
COMMENT ON COLUMN "sys_audit_log"."target_type" IS '目标类型';
COMMENT ON COLUMN "sys_audit_log"."target_id" IS '目标标识';
COMMENT ON COLUMN "sys_audit_log"."result" IS '操作结果';
COMMENT ON COLUMN "sys_audit_log"."client_ip" IS '客户端 IP';
COMMENT ON COLUMN "sys_audit_log"."detail" IS '操作详情';
COMMENT ON COLUMN "sys_audit_log"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_audit_log_tenant_created" ON "sys_audit_log" ("tenant_id", "created_at");
CREATE INDEX "idx_sys_audit_log_tenant_user" ON "sys_audit_log" ("tenant_id", "user_id");
CREATE INDEX "idx_sys_audit_log_tenant_action" ON "sys_audit_log" ("tenant_id", "action");

CREATE TABLE "sys_param" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "param_key" VARCHAR(128) NOT NULL,
    "param_value" CLOB,
    "value_type" VARCHAR(32) NOT NULL DEFAULT 'string',
    "system_flag" NUMBER(1) NOT NULL DEFAULT 0,
    "editable" NUMBER(1) NOT NULL DEFAULT 1,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_param_tenant_key" UNIQUE ("tenant_id", "param_key")
);
COMMENT ON TABLE "sys_param" IS '系统参数表';
COMMENT ON COLUMN "sys_param"."id" IS '主键';
COMMENT ON COLUMN "sys_param"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_param"."param_key" IS '参数键';
COMMENT ON COLUMN "sys_param"."param_value" IS '参数值';
COMMENT ON COLUMN "sys_param"."value_type" IS '参数值类型';
COMMENT ON COLUMN "sys_param"."system_flag" IS '是否系统内置参数';
COMMENT ON COLUMN "sys_param"."editable" IS '是否允许后台编辑';
COMMENT ON COLUMN "sys_param"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_param"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_param_tenant_system" ON "sys_param" ("tenant_id", "system_flag");

CREATE TABLE "sys_secret" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "secret_code" VARCHAR(64) NOT NULL,
    "secret_name" VARCHAR(128) NOT NULL,
    "secret_kind" VARCHAR(64) NOT NULL,
    "secret_cipher" CLOB NOT NULL,
    "secret_mask" VARCHAR(255) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "expire_at" TIMESTAMP DEFAULT NULL,
    "last_rotated_at" TIMESTAMP DEFAULT NULL,
    "created_by" BIGINT DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_by" BIGINT DEFAULT NULL,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(1) NOT NULL DEFAULT 0,
    "version" INT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_secret_tenant_code" UNIQUE ("tenant_id", "secret_code")
);
COMMENT ON TABLE "sys_secret" IS '系统密钥表';
COMMENT ON COLUMN "sys_secret"."id" IS '主键';
COMMENT ON COLUMN "sys_secret"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_secret"."secret_code" IS '密钥编码';
COMMENT ON COLUMN "sys_secret"."secret_name" IS '密钥名称';
COMMENT ON COLUMN "sys_secret"."secret_kind" IS '密钥类型';
COMMENT ON COLUMN "sys_secret"."secret_cipher" IS '密钥密文';
COMMENT ON COLUMN "sys_secret"."secret_mask" IS '脱敏展示值';
COMMENT ON COLUMN "sys_secret"."status" IS '密钥状态';
COMMENT ON COLUMN "sys_secret"."expire_at" IS '过期时间';
COMMENT ON COLUMN "sys_secret"."last_rotated_at" IS '最近轮换时间';
COMMENT ON COLUMN "sys_secret"."created_by" IS '创建人主键';
COMMENT ON COLUMN "sys_secret"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_secret"."updated_by" IS '更新人主键';
COMMENT ON COLUMN "sys_secret"."updated_at" IS '更新时间';
COMMENT ON COLUMN "sys_secret"."deleted" IS '是否删除';
COMMENT ON COLUMN "sys_secret"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "sys_secret"."remark" IS '备注';
CREATE INDEX "idx_sys_secret_tenant_status" ON "sys_secret" ("tenant_id", "status");
CREATE INDEX "idx_sys_secret_tenant_kind" ON "sys_secret" ("tenant_id", "secret_kind");
CREATE INDEX "idx_sys_secret_tenant_expire" ON "sys_secret" ("tenant_id", "expire_at");

CREATE TABLE "sys_access_restriction" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "restriction_type" VARCHAR(32) NOT NULL,
    "rule_value" VARCHAR(255) NOT NULL,
    "effect" VARCHAR(32) NOT NULL,
    "start_at" TIMESTAMP DEFAULT NULL,
    "end_at" TIMESTAMP DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_access_restriction_rule" UNIQUE ("tenant_id", "restriction_type", "rule_value")
);
COMMENT ON TABLE "sys_access_restriction" IS '系统访问限制表';
COMMENT ON COLUMN "sys_access_restriction"."id" IS '主键';
COMMENT ON COLUMN "sys_access_restriction"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_access_restriction"."restriction_type" IS '限制类型';
COMMENT ON COLUMN "sys_access_restriction"."rule_value" IS '规则值';
COMMENT ON COLUMN "sys_access_restriction"."effect" IS '生效动作';
COMMENT ON COLUMN "sys_access_restriction"."start_at" IS '生效开始时间';
COMMENT ON COLUMN "sys_access_restriction"."end_at" IS '生效结束时间';
COMMENT ON COLUMN "sys_access_restriction"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_access_restriction"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_access_restriction_active" ON "sys_access_restriction" ("tenant_id", "restriction_type", "start_at", "end_at");

CREATE TABLE "sys_password_policy" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "policy_code" VARCHAR(64) NOT NULL,
    "policy_name" VARCHAR(128) NOT NULL,
    "min_length" INT NOT NULL DEFAULT 8,
    "require_uppercase" NUMBER(1) NOT NULL DEFAULT 0,
    "require_lowercase" NUMBER(1) NOT NULL DEFAULT 1,
    "require_digit" NUMBER(1) NOT NULL DEFAULT 1,
    "require_special" NUMBER(1) NOT NULL DEFAULT 0,
    "expire_days" INT NOT NULL DEFAULT 90,
    "history_count" INT NOT NULL DEFAULT 3,
    "max_retry_count" INT NOT NULL DEFAULT 5,
    "lock_minutes" INT NOT NULL DEFAULT 30,
    "enabled" NUMBER(1) NOT NULL DEFAULT 1,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_password_policy_tenant_code" UNIQUE ("tenant_id", "policy_code")
);
COMMENT ON TABLE "sys_password_policy" IS '系统密码策略表';
COMMENT ON COLUMN "sys_password_policy"."id" IS '主键';
COMMENT ON COLUMN "sys_password_policy"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_password_policy"."policy_code" IS '策略编码';
COMMENT ON COLUMN "sys_password_policy"."policy_name" IS '策略名称';
COMMENT ON COLUMN "sys_password_policy"."min_length" IS '密码最小长度';
COMMENT ON COLUMN "sys_password_policy"."require_uppercase" IS '是否要求大写字母';
COMMENT ON COLUMN "sys_password_policy"."require_lowercase" IS '是否要求小写字母';
COMMENT ON COLUMN "sys_password_policy"."require_digit" IS '是否要求数字';
COMMENT ON COLUMN "sys_password_policy"."require_special" IS '是否要求特殊字符';
COMMENT ON COLUMN "sys_password_policy"."expire_days" IS '密码有效天数';
COMMENT ON COLUMN "sys_password_policy"."history_count" IS '历史密码记忆次数';
COMMENT ON COLUMN "sys_password_policy"."max_retry_count" IS '最大连续失败次数';
COMMENT ON COLUMN "sys_password_policy"."lock_minutes" IS '账号锁定分钟数';
COMMENT ON COLUMN "sys_password_policy"."enabled" IS '是否启用';
COMMENT ON COLUMN "sys_password_policy"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_password_policy"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_password_policy_tenant_enabled" ON "sys_password_policy" ("tenant_id", "enabled");

CREATE TABLE "sys_code_rule" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "rule_code" VARCHAR(64) NOT NULL,
    "rule_name" VARCHAR(128) NOT NULL,
    "prefix" VARCHAR(32) DEFAULT NULL,
    "date_pattern" VARCHAR(32) DEFAULT NULL,
    "sequence_length" INT NOT NULL DEFAULT 5,
    "current_value" INT NOT NULL DEFAULT 0,
    "enabled" NUMBER(1) NOT NULL DEFAULT 1,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_code_rule_tenant_code" UNIQUE ("tenant_id", "rule_code")
);
COMMENT ON TABLE "sys_code_rule" IS '系统编码规则表';
COMMENT ON COLUMN "sys_code_rule"."id" IS '主键';
COMMENT ON COLUMN "sys_code_rule"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_code_rule"."rule_code" IS '编码规则编码';
COMMENT ON COLUMN "sys_code_rule"."rule_name" IS '编码规则名称';
COMMENT ON COLUMN "sys_code_rule"."prefix" IS '编码前缀';
COMMENT ON COLUMN "sys_code_rule"."date_pattern" IS '日期格式';
COMMENT ON COLUMN "sys_code_rule"."sequence_length" IS '序列号长度';
COMMENT ON COLUMN "sys_code_rule"."current_value" IS '当前序列值';
COMMENT ON COLUMN "sys_code_rule"."enabled" IS '是否启用';
COMMENT ON COLUMN "sys_code_rule"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_code_rule"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_code_rule_tenant_enabled" ON "sys_code_rule" ("tenant_id", "enabled");

CREATE TABLE "sys_dict_type" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "dict_code" VARCHAR(64) NOT NULL,
    "dict_name" VARCHAR(128) NOT NULL,
    "system_flag" NUMBER(1) NOT NULL DEFAULT 0,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_dict_type_tenant_code" UNIQUE ("tenant_id", "dict_code")
);
COMMENT ON TABLE "sys_dict_type" IS '系统字典类型表';
COMMENT ON COLUMN "sys_dict_type"."id" IS '主键';
COMMENT ON COLUMN "sys_dict_type"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_dict_type"."dict_code" IS '字典编码';
COMMENT ON COLUMN "sys_dict_type"."dict_name" IS '字典名称';
COMMENT ON COLUMN "sys_dict_type"."system_flag" IS '是否系统内置字典';
COMMENT ON COLUMN "sys_dict_type"."status" IS '字典状态';
COMMENT ON COLUMN "sys_dict_type"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_dict_type"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_dict_type_tenant_status" ON "sys_dict_type" ("tenant_id", "status");

CREATE TABLE "sys_dict_item" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "dict_code" VARCHAR(64) NOT NULL,
    "item_label" VARCHAR(128) NOT NULL,
    "item_value" VARCHAR(128) NOT NULL,
    "item_color" VARCHAR(32) DEFAULT NULL,
    "sort_order" INT NOT NULL DEFAULT 0,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_dict_item_tenant_code_value" UNIQUE ("tenant_id", "dict_code", "item_value")
);
COMMENT ON TABLE "sys_dict_item" IS '系统字典项表';
COMMENT ON COLUMN "sys_dict_item"."id" IS '主键';
COMMENT ON COLUMN "sys_dict_item"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "sys_dict_item"."dict_code" IS '字典编码';
COMMENT ON COLUMN "sys_dict_item"."item_label" IS '字典项显示标签';
COMMENT ON COLUMN "sys_dict_item"."item_value" IS '字典项实际值';
COMMENT ON COLUMN "sys_dict_item"."item_color" IS '字典项前端展示颜色';
COMMENT ON COLUMN "sys_dict_item"."sort_order" IS '排序号';
COMMENT ON COLUMN "sys_dict_item"."status" IS '字典项状态';
COMMENT ON COLUMN "sys_dict_item"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_dict_item"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_dict_item_tenant_code_sort" ON "sys_dict_item" ("tenant_id", "dict_code", "sort_order");

CREATE TABLE "sys_module" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "module_code" VARCHAR(64) NOT NULL,
    "module_name" VARCHAR(128) NOT NULL,
    "version" VARCHAR(32) NOT NULL,
    "module_type" VARCHAR(32) NOT NULL,
    "enabled" NUMBER(1) NOT NULL DEFAULT 1,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_module_code" UNIQUE ("module_code")
);
COMMENT ON TABLE "sys_module" IS '系统模块表';
COMMENT ON COLUMN "sys_module"."id" IS '主键';
COMMENT ON COLUMN "sys_module"."module_code" IS '模块编码';
COMMENT ON COLUMN "sys_module"."module_name" IS '模块名称';
COMMENT ON COLUMN "sys_module"."version" IS '模块版本';
COMMENT ON COLUMN "sys_module"."module_type" IS '模块类型';
COMMENT ON COLUMN "sys_module"."enabled" IS '是否启用';
COMMENT ON COLUMN "sys_module"."created_at" IS '创建时间';
COMMENT ON COLUMN "sys_module"."updated_at" IS '更新时间';
CREATE INDEX "idx_sys_module_type_enabled" ON "sys_module" ("module_type", "enabled");

CREATE TABLE "sys_module_dependency" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "module_code" VARCHAR(64) NOT NULL,
    "depends_on_code" VARCHAR(64) NOT NULL,
    "required_version" VARCHAR(32) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_module_dep" UNIQUE ("module_code", "depends_on_code")
);
COMMENT ON TABLE "sys_module_dependency" IS '系统模块依赖表';
COMMENT ON COLUMN "sys_module_dependency"."id" IS '主键';
COMMENT ON COLUMN "sys_module_dependency"."module_code" IS '模块编码';
COMMENT ON COLUMN "sys_module_dependency"."depends_on_code" IS '依赖模块编码';
COMMENT ON COLUMN "sys_module_dependency"."required_version" IS '依赖模块要求版本';
COMMENT ON COLUMN "sys_module_dependency"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_module_dep_depends_on" ON "sys_module_dependency" ("depends_on_code");

CREATE TABLE "sys_module_resource" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "module_code" VARCHAR(64) NOT NULL,
    "resource_type" VARCHAR(32) NOT NULL,
    "resource_code" VARCHAR(128) NOT NULL,
    "resource_path" VARCHAR(255) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_sys_module_resource" UNIQUE ("module_code", "resource_type", "resource_code")
);
COMMENT ON TABLE "sys_module_resource" IS '系统模块资源表';
COMMENT ON COLUMN "sys_module_resource"."id" IS '主键';
COMMENT ON COLUMN "sys_module_resource"."module_code" IS '模块编码';
COMMENT ON COLUMN "sys_module_resource"."resource_type" IS '资源类型';
COMMENT ON COLUMN "sys_module_resource"."resource_code" IS '资源编码';
COMMENT ON COLUMN "sys_module_resource"."resource_path" IS '资源路径或权限标识';
COMMENT ON COLUMN "sys_module_resource"."created_at" IS '创建时间';
CREATE INDEX "idx_sys_module_resource_type" ON "sys_module_resource" ("resource_type", "resource_code");

-- ============================================================
-- 3. 低代码元数据表
-- 来源：zhyc-base-server/zhyc-module-lowcode/src/main/resources/db/V1__lowcode_core.sql
-- ============================================================
CREATE TABLE "lowcode_data_source" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "code" VARCHAR(64) NOT NULL,
    "name" VARCHAR(128) NOT NULL,
    "dialect" VARCHAR(32) NOT NULL,
    "jdbc_url" VARCHAR(512) NOT NULL,
    "username" VARCHAR(128) NOT NULL,
    "password_secret_ref" VARCHAR(255) DEFAULT NULL,
    "enabled" NUMBER(1) NOT NULL DEFAULT 1,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_lowcode_ds_tenant_code" UNIQUE ("tenant_id", "code")
);
COMMENT ON TABLE "lowcode_data_source" IS '低代码数据源表';
COMMENT ON COLUMN "lowcode_data_source"."id" IS '主键';
COMMENT ON COLUMN "lowcode_data_source"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "lowcode_data_source"."code" IS '数据源编码';
COMMENT ON COLUMN "lowcode_data_source"."name" IS '数据源名称';
COMMENT ON COLUMN "lowcode_data_source"."dialect" IS '数据库类型';
COMMENT ON COLUMN "lowcode_data_source"."jdbc_url" IS 'JDBC 连接地址';
COMMENT ON COLUMN "lowcode_data_source"."username" IS '数据库用户名';
COMMENT ON COLUMN "lowcode_data_source"."password_secret_ref" IS '数据库口令密钥引用';
COMMENT ON COLUMN "lowcode_data_source"."enabled" IS '是否启用';
COMMENT ON COLUMN "lowcode_data_source"."created_at" IS '创建时间';
COMMENT ON COLUMN "lowcode_data_source"."updated_at" IS '更新时间';
CREATE INDEX "idx_lowcode_ds_tenant_enabled" ON "lowcode_data_source" ("tenant_id", "enabled");

CREATE TABLE "lowcode_table_model" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "data_source_id" BIGINT DEFAULT NULL,
    "code" VARCHAR(64) NOT NULL,
    "name" VARCHAR(128) NOT NULL,
    "table_name" VARCHAR(128) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_lowcode_table_tenant_code" UNIQUE ("tenant_id", "code"),
    CONSTRAINT "uk_lowcode_table_tenant_table" UNIQUE ("tenant_id", "table_name"),
    CONSTRAINT "fk_lowcode_table_data_source" FOREIGN KEY ("data_source_id") REFERENCES "lowcode_data_source" ("id")
);
COMMENT ON TABLE "lowcode_table_model" IS '低代码表模型表';
COMMENT ON COLUMN "lowcode_table_model"."id" IS '主键';
COMMENT ON COLUMN "lowcode_table_model"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "lowcode_table_model"."data_source_id" IS '数据源主键';
COMMENT ON COLUMN "lowcode_table_model"."code" IS '模型编码';
COMMENT ON COLUMN "lowcode_table_model"."name" IS '模型名称';
COMMENT ON COLUMN "lowcode_table_model"."table_name" IS '物理表名';
COMMENT ON COLUMN "lowcode_table_model"."status" IS '模型状态';
COMMENT ON COLUMN "lowcode_table_model"."created_at" IS '创建时间';
COMMENT ON COLUMN "lowcode_table_model"."updated_at" IS '更新时间';
CREATE INDEX "idx_lowcode_table_tenant_status" ON "lowcode_table_model" ("tenant_id", "status");
CREATE INDEX "idx_lowcode_table_data_source" ON "lowcode_table_model" ("data_source_id");

CREATE TABLE "lowcode_column_model" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "table_model_id" BIGINT NOT NULL,
    "code" VARCHAR(64) NOT NULL,
    "name" VARCHAR(128) NOT NULL,
    "field_type" VARCHAR(32) NOT NULL,
    "length_value" INT DEFAULT NULL,
    "scale_value" INT DEFAULT NULL,
    "required" NUMBER(1) NOT NULL DEFAULT 0,
    "primary_key_flag" NUMBER(1) NOT NULL DEFAULT 0,
    "auto_increment_flag" NUMBER(1) NOT NULL DEFAULT 0,
    "list_visible" NUMBER(1) NOT NULL DEFAULT 0,
    "form_visible" NUMBER(1) NOT NULL DEFAULT 0,
    "queryable" NUMBER(1) NOT NULL DEFAULT 0,
    "dict_code" VARCHAR(64) DEFAULT NULL,
    "sort_order" INT NOT NULL DEFAULT 0,
    "comment" VARCHAR(255) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_lowcode_column_table_code" UNIQUE ("table_model_id", "code"),
    CONSTRAINT "fk_lowcode_column_table" FOREIGN KEY ("table_model_id") REFERENCES "lowcode_table_model" ("id")
);
COMMENT ON TABLE "lowcode_column_model" IS '低代码字段模型表';
COMMENT ON COLUMN "lowcode_column_model"."id" IS '主键';
COMMENT ON COLUMN "lowcode_column_model"."table_model_id" IS '表模型主键';
COMMENT ON COLUMN "lowcode_column_model"."code" IS '字段编码';
COMMENT ON COLUMN "lowcode_column_model"."name" IS '字段名称';
COMMENT ON COLUMN "lowcode_column_model"."field_type" IS '平台统一字段类型';
COMMENT ON COLUMN "lowcode_column_model"."length_value" IS '字段长度或数值精度';
COMMENT ON COLUMN "lowcode_column_model"."scale_value" IS '小数位数';
COMMENT ON COLUMN "lowcode_column_model"."required" IS '是否必填';
COMMENT ON COLUMN "lowcode_column_model"."primary_key_flag" IS '是否主键';
COMMENT ON COLUMN "lowcode_column_model"."auto_increment_flag" IS '是否自增';
COMMENT ON COLUMN "lowcode_column_model"."list_visible" IS '是否列表展示';
COMMENT ON COLUMN "lowcode_column_model"."form_visible" IS '是否表单展示';
COMMENT ON COLUMN "lowcode_column_model"."queryable" IS '是否查询条件';
COMMENT ON COLUMN "lowcode_column_model"."dict_code" IS '绑定的系统字典编码';
COMMENT ON COLUMN "lowcode_column_model"."sort_order" IS '排序号';
COMMENT ON COLUMN "lowcode_column_model"."comment" IS '字段备注';
COMMENT ON COLUMN "lowcode_column_model"."created_at" IS '创建时间';
COMMENT ON COLUMN "lowcode_column_model"."updated_at" IS '更新时间';
CREATE INDEX "idx_lowcode_column_table_sort" ON "lowcode_column_model" ("table_model_id", "sort_order");

CREATE TABLE "lowcode_table_relation" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "main_table_id" BIGINT NOT NULL,
    "sub_table_id" BIGINT NOT NULL,
    "relation_type" VARCHAR(32) NOT NULL,
    "join_column" VARCHAR(64) NOT NULL,
    "ref_column" VARCHAR(64) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_lowcode_relation_tenant_tables" UNIQUE ("tenant_id", "main_table_id", "sub_table_id", "relation_type"),
    CONSTRAINT "fk_lowcode_relation_main_table" FOREIGN KEY ("main_table_id") REFERENCES "lowcode_table_model" ("id"),
    CONSTRAINT "fk_lowcode_relation_sub_table" FOREIGN KEY ("sub_table_id") REFERENCES "lowcode_table_model" ("id")
);
COMMENT ON TABLE "lowcode_table_relation" IS '低代码表关系模型表';
COMMENT ON COLUMN "lowcode_table_relation"."id" IS '主键';
COMMENT ON COLUMN "lowcode_table_relation"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "lowcode_table_relation"."main_table_id" IS '主表模型主键';
COMMENT ON COLUMN "lowcode_table_relation"."sub_table_id" IS '子表模型主键';
COMMENT ON COLUMN "lowcode_table_relation"."relation_type" IS '关系类型';
COMMENT ON COLUMN "lowcode_table_relation"."join_column" IS '主表关联字段编码';
COMMENT ON COLUMN "lowcode_table_relation"."ref_column" IS '子表引用字段编码';
COMMENT ON COLUMN "lowcode_table_relation"."created_at" IS '创建时间';
COMMENT ON COLUMN "lowcode_table_relation"."updated_at" IS '更新时间';
CREATE INDEX "idx_lowcode_relation_tenant_main" ON "lowcode_table_relation" ("tenant_id", "main_table_id");
CREATE INDEX "idx_lowcode_relation_tenant_sub" ON "lowcode_table_relation" ("tenant_id", "sub_table_id");

CREATE TABLE "lowcode_page_model" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "table_model_id" BIGINT NOT NULL,
    "page_type" VARCHAR(32) NOT NULL,
    "route_path" VARCHAR(255) NOT NULL,
    "component_path" VARCHAR(255) NOT NULL,
    "layout_type" VARCHAR(64) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_lowcode_page_tenant_table_type" UNIQUE ("tenant_id", "table_model_id", "page_type"),
    CONSTRAINT "fk_lowcode_page_table" FOREIGN KEY ("table_model_id") REFERENCES "lowcode_table_model" ("id")
);
COMMENT ON TABLE "lowcode_page_model" IS '低代码页面模型表';
COMMENT ON COLUMN "lowcode_page_model"."id" IS '主键';
COMMENT ON COLUMN "lowcode_page_model"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "lowcode_page_model"."table_model_id" IS '表模型主键';
COMMENT ON COLUMN "lowcode_page_model"."page_type" IS '页面类型';
COMMENT ON COLUMN "lowcode_page_model"."route_path" IS '前端路由路径';
COMMENT ON COLUMN "lowcode_page_model"."component_path" IS '组件路径';
COMMENT ON COLUMN "lowcode_page_model"."layout_type" IS '页面布局类型';
COMMENT ON COLUMN "lowcode_page_model"."created_at" IS '创建时间';
COMMENT ON COLUMN "lowcode_page_model"."updated_at" IS '更新时间';
CREATE INDEX "idx_lowcode_page_tenant_table" ON "lowcode_page_model" ("tenant_id", "table_model_id");

CREATE TABLE "lowcode_generation_record" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "table_model_code" VARCHAR(64) NOT NULL,
    "target" VARCHAR(32) NOT NULL,
    "module_name" VARCHAR(128) NOT NULL,
    "entity_name" VARCHAR(128) NOT NULL,
    "overwrite_strategy" VARCHAR(32) NOT NULL,
    "file_count" INT NOT NULL DEFAULT 0,
    "file_manifest_json" CLOB DEFAULT NULL,
    "status" VARCHAR(32) NOT NULL,
    "error_message" VARCHAR(1000) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "lowcode_generation_record" IS '低代码代码生成记录表';
COMMENT ON COLUMN "lowcode_generation_record"."id" IS '主键';
COMMENT ON COLUMN "lowcode_generation_record"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "lowcode_generation_record"."table_model_code" IS '表模型编码';
COMMENT ON COLUMN "lowcode_generation_record"."target" IS '生成目标端';
COMMENT ON COLUMN "lowcode_generation_record"."module_name" IS '业务模块名称';
COMMENT ON COLUMN "lowcode_generation_record"."entity_name" IS '业务实体名称';
COMMENT ON COLUMN "lowcode_generation_record"."overwrite_strategy" IS '生成文件覆盖策略';
COMMENT ON COLUMN "lowcode_generation_record"."file_count" IS '生成文件数量';
COMMENT ON COLUMN "lowcode_generation_record"."file_manifest_json" IS '生成文件清单 JSON';
COMMENT ON COLUMN "lowcode_generation_record"."status" IS '生成状态';
COMMENT ON COLUMN "lowcode_generation_record"."error_message" IS '失败原因';
COMMENT ON COLUMN "lowcode_generation_record"."created_at" IS '创建时间';
COMMENT ON COLUMN "lowcode_generation_record"."updated_at" IS '更新时间';
CREATE INDEX "idx_lowcode_gen_tenant_model" ON "lowcode_generation_record" ("tenant_id", "table_model_code");
CREATE INDEX "idx_lowcode_gen_tenant_status" ON "lowcode_generation_record" ("tenant_id", "status");
CREATE INDEX "idx_lowcode_gen_target" ON "lowcode_generation_record" ("target");

CREATE TABLE "lc_generation_file" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "record_id" BIGINT NOT NULL,
    "template_code" VARCHAR(128) NOT NULL,
    "file_path" VARCHAR(500) NOT NULL,
    "file_type" VARCHAR(32) NOT NULL,
    "overwrite_mode" VARCHAR(32) NOT NULL,
    "content_hash" VARCHAR(128) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "fk_lc_gen_file_record" FOREIGN KEY ("record_id") REFERENCES "lowcode_generation_record" ("id")
);
COMMENT ON TABLE "lc_generation_file" IS '低代码生成文件明细表';
COMMENT ON COLUMN "lc_generation_file"."id" IS '主键';
COMMENT ON COLUMN "lc_generation_file"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "lc_generation_file"."record_id" IS '生成记录主键';
COMMENT ON COLUMN "lc_generation_file"."template_code" IS '模板编码';
COMMENT ON COLUMN "lc_generation_file"."file_path" IS '生成文件路径';
COMMENT ON COLUMN "lc_generation_file"."file_type" IS '生成文件类型';
COMMENT ON COLUMN "lc_generation_file"."overwrite_mode" IS '覆盖模式';
COMMENT ON COLUMN "lc_generation_file"."content_hash" IS '文件内容哈希';
COMMENT ON COLUMN "lc_generation_file"."created_at" IS '创建时间';
CREATE INDEX "idx_lc_gen_file_tenant_record" ON "lc_generation_file" ("tenant_id", "record_id");
CREATE INDEX "idx_lc_gen_file_hash" ON "lc_generation_file" ("content_hash");

-- ============================================================
-- 4. 工作流运行表
-- 来源：zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql
-- ============================================================
CREATE TABLE "wf_category" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "category_code" VARCHAR(64) NOT NULL,
    "category_name" VARCHAR(128) NOT NULL,
    "sort_order" INT NOT NULL DEFAULT 0,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_wf_category_tenant_code" UNIQUE ("tenant_id", "category_code")
);
COMMENT ON TABLE "wf_category" IS '工作流分类表';
COMMENT ON COLUMN "wf_category"."id" IS '主键 ID';
COMMENT ON COLUMN "wf_category"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "wf_category"."category_code" IS '流程分类编码';
COMMENT ON COLUMN "wf_category"."category_name" IS '流程分类名称';
COMMENT ON COLUMN "wf_category"."sort_order" IS '排序号';
COMMENT ON COLUMN "wf_category"."status" IS '分类状态';
COMMENT ON COLUMN "wf_category"."created_at" IS '创建时间';
COMMENT ON COLUMN "wf_category"."updated_at" IS '更新时间';
COMMENT ON COLUMN "wf_category"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "wf_category"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "wf_category"."remark" IS '备注';
CREATE INDEX "idx_wf_category_tenant_status" ON "wf_category" ("tenant_id", "status");

CREATE TABLE "wf_process_model" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "model_code" VARCHAR(128) NOT NULL,
    "model_name" VARCHAR(128) NOT NULL,
    "category_id" BIGINT NULL,
    "flowable_model_id" VARCHAR(128) NOT NULL,
    "bpmn_xml" MEDIUMTEXT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_wf_model_tenant_code" UNIQUE ("tenant_id", "model_code")
);
COMMENT ON TABLE "wf_process_model" IS '工作流流程模型表';
COMMENT ON COLUMN "wf_process_model"."id" IS '主键 ID';
COMMENT ON COLUMN "wf_process_model"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "wf_process_model"."model_code" IS '流程模型编码';
COMMENT ON COLUMN "wf_process_model"."model_name" IS '流程模型名称';
COMMENT ON COLUMN "wf_process_model"."category_id" IS '流程分类 ID';
COMMENT ON COLUMN "wf_process_model"."flowable_model_id" IS 'Flowable 模型 ID';
COMMENT ON COLUMN "wf_process_model"."bpmn_xml" IS 'BPMN XML 设计稿，用于保存在线流程编排草稿';
COMMENT ON COLUMN "wf_process_model"."status" IS '流程模型状态';
COMMENT ON COLUMN "wf_process_model"."created_at" IS '创建时间';
COMMENT ON COLUMN "wf_process_model"."updated_at" IS '更新时间';
COMMENT ON COLUMN "wf_process_model"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "wf_process_model"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "wf_process_model"."remark" IS '备注';
CREATE INDEX "idx_wf_model_tenant_category" ON "wf_process_model" ("tenant_id", "category_id");
CREATE INDEX "idx_wf_model_tenant_status" ON "wf_process_model" ("tenant_id", "status");

CREATE TABLE "wf_form_binding" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "process_key" VARCHAR(128) NOT NULL,
    "business_module" VARCHAR(64) NOT NULL,
    "business_table" VARCHAR(128) NOT NULL,
    "form_route" VARCHAR(255) NOT NULL,
    "mobile_route" VARCHAR(255) NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_wf_form_binding_tenant_process" UNIQUE ("tenant_id", "process_key")
);
COMMENT ON TABLE "wf_form_binding" IS '工作流表单绑定表';
COMMENT ON COLUMN "wf_form_binding"."id" IS '主键 ID';
COMMENT ON COLUMN "wf_form_binding"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "wf_form_binding"."process_key" IS '流程定义 key';
COMMENT ON COLUMN "wf_form_binding"."business_module" IS '业务模块编码';
COMMENT ON COLUMN "wf_form_binding"."business_table" IS '业务表名';
COMMENT ON COLUMN "wf_form_binding"."form_route" IS '后台表单路由';
COMMENT ON COLUMN "wf_form_binding"."mobile_route" IS '移动端表单路由';
COMMENT ON COLUMN "wf_form_binding"."status" IS '绑定状态';
COMMENT ON COLUMN "wf_form_binding"."created_at" IS '创建时间';
COMMENT ON COLUMN "wf_form_binding"."updated_at" IS '更新时间';
COMMENT ON COLUMN "wf_form_binding"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "wf_form_binding"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "wf_form_binding"."remark" IS '备注';
CREATE INDEX "idx_wf_form_binding_tenant_module" ON "wf_form_binding" ("tenant_id", "business_module");
CREATE INDEX "idx_wf_form_binding_tenant_status" ON "wf_form_binding" ("tenant_id", "status");

CREATE TABLE "wf_process_definition" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "process_key" VARCHAR(128) NOT NULL,
    "process_name" VARCHAR(128) NOT NULL,
    "version" INT NOT NULL,
    "deployment_id" VARCHAR(128) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'active',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version_no" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_wf_definition_tenant_key_version" UNIQUE ("tenant_id", "process_key", "version")
);
COMMENT ON TABLE "wf_process_definition" IS '工作流流程定义表';
COMMENT ON COLUMN "wf_process_definition"."id" IS '主键 ID';
COMMENT ON COLUMN "wf_process_definition"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "wf_process_definition"."process_key" IS '流程定义 key';
COMMENT ON COLUMN "wf_process_definition"."process_name" IS '流程定义名称';
COMMENT ON COLUMN "wf_process_definition"."version" IS '流程定义版本号';
COMMENT ON COLUMN "wf_process_definition"."deployment_id" IS 'Flowable 部署 ID';
COMMENT ON COLUMN "wf_process_definition"."status" IS '流程定义状态';
COMMENT ON COLUMN "wf_process_definition"."created_at" IS '创建时间';
COMMENT ON COLUMN "wf_process_definition"."updated_at" IS '更新时间';
COMMENT ON COLUMN "wf_process_definition"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "wf_process_definition"."version_no" IS '乐观锁版本号';
COMMENT ON COLUMN "wf_process_definition"."remark" IS '备注';
CREATE INDEX "idx_wf_definition_tenant_key" ON "wf_process_definition" ("tenant_id", "process_key");
CREATE INDEX "idx_wf_definition_tenant_status" ON "wf_process_definition" ("tenant_id", "status");

CREATE TABLE "wf_process_instance" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "process_instance_id" VARCHAR(128) NOT NULL,
    "process_key" VARCHAR(128) NOT NULL,
    "business_key" VARCHAR(128) NOT NULL,
    "starter_user_id" BIGINT NULL,
    "status" VARCHAR(32) NOT NULL,
    "started_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ended_at" TIMESTAMP NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_wf_pi_tenant_instance" UNIQUE ("tenant_id", "process_instance_id")
);
COMMENT ON TABLE "wf_process_instance" IS '工作流流程实例表';
COMMENT ON COLUMN "wf_process_instance"."id" IS '主键 ID';
COMMENT ON COLUMN "wf_process_instance"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "wf_process_instance"."process_instance_id" IS '流程实例 ID';
COMMENT ON COLUMN "wf_process_instance"."process_key" IS '流程定义 key';
COMMENT ON COLUMN "wf_process_instance"."business_key" IS '业务对象唯一标识';
COMMENT ON COLUMN "wf_process_instance"."starter_user_id" IS '流程发起人用户 ID';
COMMENT ON COLUMN "wf_process_instance"."status" IS '流程实例状态';
COMMENT ON COLUMN "wf_process_instance"."started_at" IS '流程启动时间';
COMMENT ON COLUMN "wf_process_instance"."ended_at" IS '流程结束时间';
COMMENT ON COLUMN "wf_process_instance"."created_at" IS '创建时间';
COMMENT ON COLUMN "wf_process_instance"."updated_at" IS '更新时间';
COMMENT ON COLUMN "wf_process_instance"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "wf_process_instance"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "wf_process_instance"."remark" IS '备注';
CREATE INDEX "idx_wf_pi_tenant_business" ON "wf_process_instance" ("tenant_id", "business_key");
CREATE INDEX "idx_wf_pi_tenant_status" ON "wf_process_instance" ("tenant_id", "status");

CREATE TABLE "wf_task" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "task_id" VARCHAR(128) NOT NULL,
    "process_instance_id" VARCHAR(128) NOT NULL,
    "task_name" VARCHAR(128) NOT NULL,
    "business_key" VARCHAR(128) NOT NULL,
    "assignee_user_id" BIGINT NOT NULL,
    "status" VARCHAR(32) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "completed_at" TIMESTAMP NULL,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_wf_task_tenant_task" UNIQUE ("tenant_id", "task_id")
);
COMMENT ON TABLE "wf_task" IS '工作流任务表';
COMMENT ON COLUMN "wf_task"."id" IS '主键 ID';
COMMENT ON COLUMN "wf_task"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "wf_task"."task_id" IS '任务 ID';
COMMENT ON COLUMN "wf_task"."process_instance_id" IS '流程实例 ID';
COMMENT ON COLUMN "wf_task"."task_name" IS '任务名称';
COMMENT ON COLUMN "wf_task"."business_key" IS '业务对象唯一标识';
COMMENT ON COLUMN "wf_task"."assignee_user_id" IS '任务处理人用户 ID';
COMMENT ON COLUMN "wf_task"."status" IS '任务状态';
COMMENT ON COLUMN "wf_task"."created_at" IS '任务创建时间';
COMMENT ON COLUMN "wf_task"."completed_at" IS '任务完成时间';
COMMENT ON COLUMN "wf_task"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "wf_task"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "wf_task"."remark" IS '备注';
CREATE INDEX "idx_wf_task_tenant_assignee" ON "wf_task" ("tenant_id", "assignee_user_id", "status");
CREATE INDEX "idx_wf_task_tenant_instance" ON "wf_task" ("tenant_id", "process_instance_id");

CREATE TABLE "wf_approval_record" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "task_id" VARCHAR(128) NOT NULL,
    "process_instance_id" VARCHAR(128) NULL,
    "operator_user_id" BIGINT NOT NULL,
    "action" VARCHAR(32) NOT NULL,
    "approval_comment" VARCHAR(1000) NULL,
    "operated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "wf_approval_record" IS '工作流审批记录表';
COMMENT ON COLUMN "wf_approval_record"."id" IS '主键 ID';
COMMENT ON COLUMN "wf_approval_record"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "wf_approval_record"."task_id" IS '任务 ID';
COMMENT ON COLUMN "wf_approval_record"."process_instance_id" IS '流程实例 ID';
COMMENT ON COLUMN "wf_approval_record"."operator_user_id" IS '操作用户 ID';
COMMENT ON COLUMN "wf_approval_record"."action" IS '审批动作';
COMMENT ON COLUMN "wf_approval_record"."approval_comment" IS '审批意见';
COMMENT ON COLUMN "wf_approval_record"."operated_at" IS '操作时间';
COMMENT ON COLUMN "wf_approval_record"."created_at" IS '创建时间';
CREATE INDEX "idx_wf_record_tenant_task" ON "wf_approval_record" ("tenant_id", "task_id");
CREATE INDEX "idx_wf_record_tenant_instance" ON "wf_approval_record" ("tenant_id", "process_instance_id");

CREATE TABLE "wf_cc_record" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "process_instance_id" VARCHAR(128) NOT NULL,
    "receiver_id" BIGINT NOT NULL,
    "read_flag" NUMBER(3) NOT NULL DEFAULT 0,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "wf_cc_record" IS '工作流抄送记录表';
COMMENT ON COLUMN "wf_cc_record"."id" IS '主键 ID';
COMMENT ON COLUMN "wf_cc_record"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "wf_cc_record"."process_instance_id" IS '流程实例 ID';
COMMENT ON COLUMN "wf_cc_record"."receiver_id" IS '抄送接收人用户 ID';
COMMENT ON COLUMN "wf_cc_record"."read_flag" IS '阅读标识，0 未读，1 已读';
COMMENT ON COLUMN "wf_cc_record"."created_at" IS '创建时间';
COMMENT ON COLUMN "wf_cc_record"."updated_at" IS '更新时间';
COMMENT ON COLUMN "wf_cc_record"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "wf_cc_record"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "wf_cc_record"."remark" IS '备注';
CREATE INDEX "idx_wf_cc_tenant_receiver" ON "wf_cc_record" ("tenant_id", "receiver_id", "read_flag");
CREATE INDEX "idx_wf_cc_tenant_instance" ON "wf_cc_record" ("tenant_id", "process_instance_id");

-- ============================================================
-- 5. Flowable 引擎运行表
-- 来源：zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V2__flowable_engine_mysql.sql
-- ============================================================
-- Flowable 引擎运行表结构。
-- 说明：由 CompareLocalDatabaseInit 从本地开发库 SHOW CREATE TABLE 导出，只包含 DDL，不包含流程实例、变量、任务或历史数据。
-- 说明：本脚本不包含 AI 模型供应商密钥、系统密钥、用户密码或 OAuth2 客户端密钥。

CREATE TABLE "act_evt_log" (
    "LOG_NR_" BIGINT IDENTITY(1,1) NOT NULL,
    "TYPE_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "TIME_STAMP_" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    "USER_ID_" varchar(255) DEFAULT NULL,
    "DATA_" BLOB,
    "LOCK_OWNER_" varchar(255) DEFAULT NULL,
    "LOCK_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "IS_PROCESSED_" NUMBER(3) DEFAULT '0',
    PRIMARY KEY ("LOG_NR_")
);
COMMENT ON TABLE "act_evt_log" IS 'Flowable 引擎表 act_evt_log';

CREATE TABLE "act_re_deployment" (
    "ID_" varchar(64) NOT NULL,
    "NAME_" varchar(255) DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "KEY_" varchar(255) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    "DEPLOY_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "DERIVED_FROM_" varchar(64) DEFAULT NULL,
    "DERIVED_FROM_ROOT_" varchar(64) DEFAULT NULL,
    "PARENT_DEPLOYMENT_ID_" varchar(255) DEFAULT NULL,
    "ENGINE_VERSION_" varchar(255) DEFAULT NULL,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_re_deployment" IS 'Flowable 引擎表 act_re_deployment';

CREATE TABLE "act_ge_bytearray" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "NAME_" varchar(255) DEFAULT NULL,
    "DEPLOYMENT_ID_" varchar(64) DEFAULT NULL,
    "BYTES_" BLOB,
    "GENERATED_" NUMBER(3) DEFAULT NULL,
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_BYTEARR_DEPL" FOREIGN KEY ("DEPLOYMENT_ID_") REFERENCES "act_re_deployment" ("ID_")
);
COMMENT ON TABLE "act_ge_bytearray" IS 'Flowable 引擎表 act_ge_bytearray';
CREATE INDEX "ACT_IDX_BYTEAR_DEPL" ON "act_ge_bytearray" ("DEPLOYMENT_ID_");

CREATE TABLE "act_ge_property" (
    "NAME_" varchar(64) NOT NULL,
    "VALUE_" varchar(300) DEFAULT NULL,
    "REV_" INT DEFAULT NULL,
    PRIMARY KEY ("NAME_")
);
COMMENT ON TABLE "act_ge_property" IS 'Flowable 引擎表 act_ge_property';

CREATE TABLE "act_hi_actinst" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT '1',
    "PROC_DEF_ID_" varchar(64) NOT NULL,
    "PROC_INST_ID_" varchar(64) NOT NULL,
    "EXECUTION_ID_" varchar(64) NOT NULL,
    "ACT_ID_" varchar(255) NOT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "CALL_PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "ACT_NAME_" varchar(255) DEFAULT NULL,
    "ACT_TYPE_" varchar(255) NOT NULL,
    "ASSIGNEE_" varchar(255) DEFAULT NULL,
    "COMPLETED_BY_" varchar(255) DEFAULT NULL,
    "START_TIME_" TIMESTAMP(3) NOT NULL,
    "END_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "TRANSACTION_ORDER_" INT DEFAULT NULL,
    "DURATION_" BIGINT DEFAULT NULL,
    "DELETE_REASON_" varchar(4000) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_actinst" IS 'Flowable 引擎表 act_hi_actinst';
CREATE INDEX "ACT_IDX_HI_ACT_INST_START" ON "act_hi_actinst" ("START_TIME_");
CREATE INDEX "ACT_IDX_HI_ACT_INST_END" ON "act_hi_actinst" ("END_TIME_");
CREATE INDEX "ACT_IDX_HI_ACT_INST_PROCINST" ON "act_hi_actinst" ("PROC_INST_ID_", "ACT_ID_");
CREATE INDEX "ACT_IDX_HI_ACT_INST_EXEC" ON "act_hi_actinst" ("EXECUTION_ID_", "ACT_ID_");

CREATE TABLE "act_hi_attachment" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "USER_ID_" varchar(255) DEFAULT NULL,
    "NAME_" varchar(255) DEFAULT NULL,
    "DESCRIPTION_" varchar(4000) DEFAULT NULL,
    "TYPE_" varchar(255) DEFAULT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "URL_" varchar(4000) DEFAULT NULL,
    "CONTENT_ID_" varchar(64) DEFAULT NULL,
    "TIME_" TIMESTAMP(3) DEFAULT NULL,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_attachment" IS 'Flowable 引擎表 act_hi_attachment';

CREATE TABLE "act_hi_comment" (
    "ID_" varchar(64) NOT NULL,
    "TYPE_" varchar(255) DEFAULT NULL,
    "TIME_" TIMESTAMP(3) NOT NULL,
    "USER_ID_" varchar(255) DEFAULT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "ACTION_" varchar(255) DEFAULT NULL,
    "MESSAGE_" varchar(4000) DEFAULT NULL,
    "FULL_MSG_" BLOB,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_comment" IS 'Flowable 引擎表 act_hi_comment';

CREATE TABLE "act_hi_detail" (
    "ID_" varchar(64) NOT NULL,
    "TYPE_" varchar(255) NOT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "ACT_INST_ID_" varchar(64) DEFAULT NULL,
    "NAME_" varchar(255) NOT NULL,
    "VAR_TYPE_" varchar(255) DEFAULT NULL,
    "REV_" INT DEFAULT NULL,
    "TIME_" TIMESTAMP(3) NOT NULL,
    "BYTEARRAY_ID_" varchar(64) DEFAULT NULL,
    "DOUBLE_" DOUBLE PRECISION DEFAULT NULL,
    "LONG_" BIGINT DEFAULT NULL,
    "TEXT_" varchar(4000) DEFAULT NULL,
    "TEXT2_" varchar(4000) DEFAULT NULL,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_detail" IS 'Flowable 引擎表 act_hi_detail';
CREATE INDEX "ACT_IDX_HI_DETAIL_PROC_INST" ON "act_hi_detail" ("PROC_INST_ID_");
CREATE INDEX "ACT_IDX_HI_DETAIL_ACT_INST" ON "act_hi_detail" ("ACT_INST_ID_");
CREATE INDEX "ACT_IDX_HI_DETAIL_TIME" ON "act_hi_detail" ("TIME_");
CREATE INDEX "ACT_IDX_HI_DETAIL_NAME" ON "act_hi_detail" ("NAME_");
CREATE INDEX "ACT_IDX_HI_DETAIL_TASK_ID" ON "act_hi_detail" ("TASK_ID_");

CREATE TABLE "act_hi_entitylink" (
    "ID_" varchar(64) NOT NULL,
    "LINK_TYPE_" varchar(255) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "PARENT_ELEMENT_ID_" varchar(255) DEFAULT NULL,
    "REF_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "REF_SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "REF_SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "ROOT_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "ROOT_SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "HIERARCHY_TYPE_" varchar(255) DEFAULT NULL,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_entitylink" IS 'Flowable 引擎表 act_hi_entitylink';
CREATE INDEX "ACT_IDX_HI_ENT_LNK_SCOPE" ON "act_hi_entitylink" ("SCOPE_ID_", "SCOPE_TYPE_", "LINK_TYPE_");
CREATE INDEX "ACT_IDX_HI_ENT_LNK_REF_SCOPE" ON "act_hi_entitylink" ("REF_SCOPE_ID_", "REF_SCOPE_TYPE_", "LINK_TYPE_");
CREATE INDEX "ACT_IDX_HI_ENT_LNK_ROOT_SCOPE" ON "act_hi_entitylink" ("ROOT_SCOPE_ID_", "ROOT_SCOPE_TYPE_", "LINK_TYPE_");
CREATE INDEX "ACT_IDX_HI_ENT_LNK_SCOPE_DEF" ON "act_hi_entitylink" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_", "LINK_TYPE_");

CREATE TABLE "act_hi_identitylink" (
    "ID_" varchar(64) NOT NULL,
    "GROUP_ID_" varchar(255) DEFAULT NULL,
    "TYPE_" varchar(255) DEFAULT NULL,
    "USER_ID_" varchar(255) DEFAULT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_identitylink" IS 'Flowable 引擎表 act_hi_identitylink';
CREATE INDEX "ACT_IDX_HI_IDENT_LNK_USER" ON "act_hi_identitylink" ("USER_ID_");
CREATE INDEX "ACT_IDX_HI_IDENT_LNK_SCOPE" ON "act_hi_identitylink" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_HI_IDENT_LNK_SUB_SCOPE" ON "act_hi_identitylink" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_HI_IDENT_LNK_SCOPE_DEF" ON "act_hi_identitylink" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_HI_IDENT_LNK_TASK" ON "act_hi_identitylink" ("TASK_ID_");
CREATE INDEX "ACT_IDX_HI_IDENT_LNK_PROCINST" ON "act_hi_identitylink" ("PROC_INST_ID_");

CREATE TABLE "act_hi_procinst" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT '1',
    "PROC_INST_ID_" varchar(64) NOT NULL,
    "BUSINESS_KEY_" varchar(255) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) NOT NULL,
    "START_TIME_" TIMESTAMP(3) NOT NULL,
    "END_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "DURATION_" BIGINT DEFAULT NULL,
    "START_USER_ID_" varchar(255) DEFAULT NULL,
    "START_ACT_ID_" varchar(255) DEFAULT NULL,
    "END_ACT_ID_" varchar(255) DEFAULT NULL,
    "SUPER_PROCESS_INSTANCE_ID_" varchar(64) DEFAULT NULL,
    "DELETE_REASON_" varchar(4000) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    "NAME_" varchar(255) DEFAULT NULL,
    "CALLBACK_ID_" varchar(255) DEFAULT NULL,
    "CALLBACK_TYPE_" varchar(255) DEFAULT NULL,
    "REFERENCE_ID_" varchar(255) DEFAULT NULL,
    "REFERENCE_TYPE_" varchar(255) DEFAULT NULL,
    "PROPAGATED_STAGE_INST_ID_" varchar(255) DEFAULT NULL,
    "BUSINESS_STATUS_" varchar(255) DEFAULT NULL,
    "END_USER_ID_" varchar(255) DEFAULT NULL,
    "STATE_" varchar(255) DEFAULT NULL,
    PRIMARY KEY ("ID_"),
    CONSTRAINT "PROC_INST_ID_" UNIQUE ("PROC_INST_ID_")
);
COMMENT ON TABLE "act_hi_procinst" IS 'Flowable 引擎表 act_hi_procinst';
CREATE INDEX "ACT_IDX_HI_PRO_INST_END" ON "act_hi_procinst" ("END_TIME_");
CREATE INDEX "ACT_IDX_HI_PRO_I_BUSKEY" ON "act_hi_procinst" ("BUSINESS_KEY_");
CREATE INDEX "ACT_IDX_HI_PRO_SUPER_PROCINST" ON "act_hi_procinst" ("SUPER_PROCESS_INSTANCE_ID_");

CREATE TABLE "act_hi_taskinst" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT '1',
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "TASK_DEF_ID_" varchar(64) DEFAULT NULL,
    "TASK_DEF_KEY_" varchar(255) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "PROPAGATED_STAGE_INST_ID_" varchar(255) DEFAULT NULL,
    "STATE_" varchar(255) DEFAULT NULL,
    "NAME_" varchar(255) DEFAULT NULL,
    "PARENT_TASK_ID_" varchar(64) DEFAULT NULL,
    "DESCRIPTION_" varchar(4000) DEFAULT NULL,
    "OWNER_" varchar(255) DEFAULT NULL,
    "ASSIGNEE_" varchar(255) DEFAULT NULL,
    "START_TIME_" TIMESTAMP(3) NOT NULL,
    "IN_PROGRESS_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "IN_PROGRESS_STARTED_BY_" varchar(255) DEFAULT NULL,
    "CLAIM_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "CLAIMED_BY_" varchar(255) DEFAULT NULL,
    "SUSPENDED_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "SUSPENDED_BY_" varchar(255) DEFAULT NULL,
    "END_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "COMPLETED_BY_" varchar(255) DEFAULT NULL,
    "DURATION_" BIGINT DEFAULT NULL,
    "DELETE_REASON_" varchar(4000) DEFAULT NULL,
    "PRIORITY_" INT DEFAULT NULL,
    "IN_PROGRESS_DUE_DATE_" TIMESTAMP(3) DEFAULT NULL,
    "DUE_DATE_" TIMESTAMP(3) DEFAULT NULL,
    "FORM_KEY_" varchar(255) DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    "LAST_UPDATED_TIME_" TIMESTAMP(3) DEFAULT NULL,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_taskinst" IS 'Flowable 引擎表 act_hi_taskinst';
CREATE INDEX "ACT_IDX_HI_TASK_SCOPE" ON "act_hi_taskinst" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_HI_TASK_SUB_SCOPE" ON "act_hi_taskinst" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_HI_TASK_SCOPE_DEF" ON "act_hi_taskinst" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_HI_TASK_INST_PROCINST" ON "act_hi_taskinst" ("PROC_INST_ID_");

CREATE TABLE "act_hi_tsk_log" (
    "ID_" BIGINT IDENTITY(1,1) NOT NULL,
    "TYPE_" varchar(64) DEFAULT NULL,
    "TASK_ID_" varchar(64) NOT NULL,
    "TIME_STAMP_" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)(3),
    "USER_ID_" varchar(255) DEFAULT NULL,
    "DATA_" varchar(4000) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_tsk_log" IS 'Flowable 引擎表 act_hi_tsk_log';
CREATE INDEX "ACT_IDX_ACT_HI_TSK_LOG_TASK" ON "act_hi_tsk_log" ("TASK_ID_");

CREATE TABLE "act_hi_varinst" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT '1',
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "NAME_" varchar(255) NOT NULL,
    "VAR_TYPE_" varchar(100) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "BYTEARRAY_ID_" varchar(64) DEFAULT NULL,
    "DOUBLE_" DOUBLE PRECISION DEFAULT NULL,
    "LONG_" BIGINT DEFAULT NULL,
    "TEXT_" varchar(4000) DEFAULT NULL,
    "TEXT2_" varchar(4000) DEFAULT NULL,
    "META_INFO_" varchar(4000) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "LAST_UPDATED_TIME_" TIMESTAMP(3) DEFAULT NULL,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_hi_varinst" IS 'Flowable 引擎表 act_hi_varinst';
CREATE INDEX "ACT_IDX_HI_PROCVAR_NAME_TYPE" ON "act_hi_varinst" ("NAME_", "VAR_TYPE_");
CREATE INDEX "ACT_IDX_HI_VAR_SCOPE_ID_TYPE" ON "act_hi_varinst" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_HI_VAR_SUB_ID_TYPE" ON "act_hi_varinst" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_HI_PROCVAR_PROC_INST" ON "act_hi_varinst" ("PROC_INST_ID_");
CREATE INDEX "ACT_IDX_HI_PROCVAR_TASK_ID" ON "act_hi_varinst" ("TASK_ID_");
CREATE INDEX "ACT_IDX_HI_PROCVAR_EXE" ON "act_hi_varinst" ("EXECUTION_ID_");

CREATE TABLE "act_re_procdef" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "NAME_" varchar(255) DEFAULT NULL,
    "KEY_" varchar(255) NOT NULL,
    "VERSION_" INT NOT NULL,
    "DEPLOYMENT_ID_" varchar(64) DEFAULT NULL,
    "RESOURCE_NAME_" varchar(4000) DEFAULT NULL,
    "DGRM_RESOURCE_NAME_" varchar(4000) DEFAULT NULL,
    "DESCRIPTION_" varchar(4000) DEFAULT NULL,
    "HAS_START_FORM_KEY_" NUMBER(3) DEFAULT NULL,
    "HAS_GRAPHICAL_NOTATION_" NUMBER(3) DEFAULT NULL,
    "SUSPENSION_STATE_" INT DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    "ENGINE_VERSION_" varchar(255) DEFAULT NULL,
    "DERIVED_FROM_" varchar(64) DEFAULT NULL,
    "DERIVED_FROM_ROOT_" varchar(64) DEFAULT NULL,
    "DERIVED_VERSION_" INT NOT NULL DEFAULT '0',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_UNIQ_PROCDEF" UNIQUE ("KEY_", "VERSION_", "DERIVED_VERSION_", "TENANT_ID_")
);
COMMENT ON TABLE "act_re_procdef" IS 'Flowable 引擎表 act_re_procdef';

CREATE TABLE "act_procdef_info" (
    "ID_" varchar(64) NOT NULL,
    "PROC_DEF_ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "INFO_JSON_ID_" varchar(64) DEFAULT NULL,
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_UNIQ_INFO_PROCDEF" UNIQUE ("PROC_DEF_ID_"),
    CONSTRAINT "ACT_FK_INFO_JSON_BA" FOREIGN KEY ("INFO_JSON_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_INFO_PROCDEF" FOREIGN KEY ("PROC_DEF_ID_") REFERENCES "act_re_procdef" ("ID_")
);
COMMENT ON TABLE "act_procdef_info" IS 'Flowable 引擎表 act_procdef_info';
CREATE INDEX "ACT_IDX_INFO_PROCDEF" ON "act_procdef_info" ("PROC_DEF_ID_");
CREATE INDEX "ACT_FK_INFO_JSON_BA" ON "act_procdef_info" ("INFO_JSON_ID_");

CREATE TABLE "act_re_model" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "NAME_" varchar(255) DEFAULT NULL,
    "KEY_" varchar(255) DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "LAST_UPDATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "VERSION_" INT DEFAULT NULL,
    "META_INFO_" varchar(4000) DEFAULT NULL,
    "DEPLOYMENT_ID_" varchar(64) DEFAULT NULL,
    "EDITOR_SOURCE_VALUE_ID_" varchar(64) DEFAULT NULL,
    "EDITOR_SOURCE_EXTRA_VALUE_ID_" varchar(64) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_MODEL_DEPLOYMENT" FOREIGN KEY ("DEPLOYMENT_ID_") REFERENCES "act_re_deployment" ("ID_"),
    CONSTRAINT "ACT_FK_MODEL_SOURCE" FOREIGN KEY ("EDITOR_SOURCE_VALUE_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_MODEL_SOURCE_EXTRA" FOREIGN KEY ("EDITOR_SOURCE_EXTRA_VALUE_ID_") REFERENCES "act_ge_bytearray" ("ID_")
);
COMMENT ON TABLE "act_re_model" IS 'Flowable 引擎表 act_re_model';
CREATE INDEX "ACT_FK_MODEL_SOURCE" ON "act_re_model" ("EDITOR_SOURCE_VALUE_ID_");
CREATE INDEX "ACT_FK_MODEL_SOURCE_EXTRA" ON "act_re_model" ("EDITOR_SOURCE_EXTRA_VALUE_ID_");
CREATE INDEX "ACT_FK_MODEL_DEPLOYMENT" ON "act_re_model" ("DEPLOYMENT_ID_");

CREATE TABLE "act_ru_actinst" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT '1',
    "PROC_DEF_ID_" varchar(64) NOT NULL,
    "PROC_INST_ID_" varchar(64) NOT NULL,
    "EXECUTION_ID_" varchar(64) NOT NULL,
    "ACT_ID_" varchar(255) NOT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "CALL_PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "ACT_NAME_" varchar(255) DEFAULT NULL,
    "ACT_TYPE_" varchar(255) NOT NULL,
    "ASSIGNEE_" varchar(255) DEFAULT NULL,
    "COMPLETED_BY_" varchar(255) DEFAULT NULL,
    "START_TIME_" TIMESTAMP(3) NOT NULL,
    "END_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "DURATION_" BIGINT DEFAULT NULL,
    "TRANSACTION_ORDER_" INT DEFAULT NULL,
    "DELETE_REASON_" varchar(4000) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_ru_actinst" IS 'Flowable 引擎表 act_ru_actinst';
CREATE INDEX "ACT_IDX_RU_ACTI_START" ON "act_ru_actinst" ("START_TIME_");
CREATE INDEX "ACT_IDX_RU_ACTI_END" ON "act_ru_actinst" ("END_TIME_");
CREATE INDEX "ACT_IDX_RU_ACTI_PROC" ON "act_ru_actinst" ("PROC_INST_ID_");
CREATE INDEX "ACT_IDX_RU_ACTI_PROC_ACT" ON "act_ru_actinst" ("PROC_INST_ID_", "ACT_ID_");
CREATE INDEX "ACT_IDX_RU_ACTI_EXEC" ON "act_ru_actinst" ("EXECUTION_ID_");
CREATE INDEX "ACT_IDX_RU_ACTI_EXEC_ACT" ON "act_ru_actinst" ("EXECUTION_ID_", "ACT_ID_");
CREATE INDEX "ACT_IDX_RU_ACTI_TASK" ON "act_ru_actinst" ("TASK_ID_");

CREATE TABLE "act_ru_execution" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "BUSINESS_KEY_" varchar(255) DEFAULT NULL,
    "PARENT_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "SUPER_EXEC_" varchar(64) DEFAULT NULL,
    "ROOT_PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "ACT_ID_" varchar(255) DEFAULT NULL,
    "IS_ACTIVE_" NUMBER(3) DEFAULT NULL,
    "IS_CONCURRENT_" NUMBER(3) DEFAULT NULL,
    "IS_SCOPE_" NUMBER(3) DEFAULT NULL,
    "IS_EVENT_SCOPE_" NUMBER(3) DEFAULT NULL,
    "IS_MI_ROOT_" NUMBER(3) DEFAULT NULL,
    "SUSPENSION_STATE_" INT DEFAULT NULL,
    "CACHED_ENT_STATE_" INT DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    "NAME_" varchar(255) DEFAULT NULL,
    "START_ACT_ID_" varchar(255) DEFAULT NULL,
    "START_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "START_USER_ID_" varchar(255) DEFAULT NULL,
    "LOCK_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "LOCK_OWNER_" varchar(255) DEFAULT NULL,
    "IS_COUNT_ENABLED_" NUMBER(3) DEFAULT NULL,
    "EVT_SUBSCR_COUNT_" INT DEFAULT NULL,
    "TASK_COUNT_" INT DEFAULT NULL,
    "JOB_COUNT_" INT DEFAULT NULL,
    "TIMER_JOB_COUNT_" INT DEFAULT NULL,
    "SUSP_JOB_COUNT_" INT DEFAULT NULL,
    "DEADLETTER_JOB_COUNT_" INT DEFAULT NULL,
    "EXTERNAL_WORKER_JOB_COUNT_" INT DEFAULT NULL,
    "VAR_COUNT_" INT DEFAULT NULL,
    "ID_LINK_COUNT_" INT DEFAULT NULL,
    "CALLBACK_ID_" varchar(255) DEFAULT NULL,
    "CALLBACK_TYPE_" varchar(255) DEFAULT NULL,
    "REFERENCE_ID_" varchar(255) DEFAULT NULL,
    "REFERENCE_TYPE_" varchar(255) DEFAULT NULL,
    "PROPAGATED_STAGE_INST_ID_" varchar(255) DEFAULT NULL,
    "BUSINESS_STATUS_" varchar(255) DEFAULT NULL,
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_EXE_PARENT" FOREIGN KEY ("PARENT_ID_") REFERENCES "act_ru_execution" ("ID_") ON DELETE CASCADE,
    CONSTRAINT "ACT_FK_EXE_PROCDEF" FOREIGN KEY ("PROC_DEF_ID_") REFERENCES "act_re_procdef" ("ID_"),
    CONSTRAINT "ACT_FK_EXE_PROCINST" FOREIGN KEY ("PROC_INST_ID_") REFERENCES "act_ru_execution" ("ID_") ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "ACT_FK_EXE_SUPER" FOREIGN KEY ("SUPER_EXEC_") REFERENCES "act_ru_execution" ("ID_") ON DELETE CASCADE
);
COMMENT ON TABLE "act_ru_execution" IS 'Flowable 引擎表 act_ru_execution';
CREATE INDEX "ACT_IDX_EXEC_BUSKEY" ON "act_ru_execution" ("BUSINESS_KEY_");
CREATE INDEX "ACT_IDC_EXEC_ROOT" ON "act_ru_execution" ("ROOT_PROC_INST_ID_");
CREATE INDEX "ACT_IDX_EXEC_REF_ID_" ON "act_ru_execution" ("REFERENCE_ID_");
CREATE INDEX "ACT_FK_EXE_PROCINST" ON "act_ru_execution" ("PROC_INST_ID_");
CREATE INDEX "ACT_FK_EXE_PARENT" ON "act_ru_execution" ("PARENT_ID_");
CREATE INDEX "ACT_FK_EXE_SUPER" ON "act_ru_execution" ("SUPER_EXEC_");
CREATE INDEX "ACT_FK_EXE_PROCDEF" ON "act_ru_execution" ("PROC_DEF_ID_");

CREATE TABLE "act_ru_deadletter_job" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "TYPE_" varchar(255) NOT NULL,
    "EXCLUSIVE_" NUMBER(1) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROCESS_INSTANCE_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "ELEMENT_ID_" varchar(255) DEFAULT NULL,
    "ELEMENT_NAME_" varchar(255) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "CORRELATION_ID_" varchar(255) DEFAULT NULL,
    "EXCEPTION_STACK_ID_" varchar(64) DEFAULT NULL,
    "EXCEPTION_MSG_" varchar(4000) DEFAULT NULL,
    "DUEDATE_" TIMESTAMP(3) NULL DEFAULT NULL,
    "REPEAT_" varchar(255) DEFAULT NULL,
    "HANDLER_TYPE_" varchar(255) DEFAULT NULL,
    "HANDLER_CFG_" varchar(4000) DEFAULT NULL,
    "CUSTOM_VALUES_ID_" varchar(64) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_DEADLETTER_JOB_CUSTOM_VALUES" FOREIGN KEY ("CUSTOM_VALUES_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_DEADLETTER_JOB_EXCEPTION" FOREIGN KEY ("EXCEPTION_STACK_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_DEADLETTER_JOB_EXECUTION" FOREIGN KEY ("EXECUTION_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE" FOREIGN KEY ("PROCESS_INSTANCE_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_DEADLETTER_JOB_PROC_DEF" FOREIGN KEY ("PROC_DEF_ID_") REFERENCES "act_re_procdef" ("ID_")
);
COMMENT ON TABLE "act_ru_deadletter_job" IS 'Flowable 引擎表 act_ru_deadletter_job';
CREATE INDEX "ACT_IDX_DEADLETTER_JOB_EXCEPTION_STACK_ID" ON "act_ru_deadletter_job" ("EXCEPTION_STACK_ID_");
CREATE INDEX "ACT_IDX_DEADLETTER_JOB_CUSTOM_VALUES_ID" ON "act_ru_deadletter_job" ("CUSTOM_VALUES_ID_");
CREATE INDEX "ACT_IDX_DEADLETTER_JOB_CORRELATION_ID" ON "act_ru_deadletter_job" ("CORRELATION_ID_");
CREATE INDEX "ACT_IDX_DJOB_SCOPE" ON "act_ru_deadletter_job" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_DJOB_SUB_SCOPE" ON "act_ru_deadletter_job" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_DJOB_SCOPE_DEF" ON "act_ru_deadletter_job" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_FK_DEADLETTER_JOB_EXECUTION" ON "act_ru_deadletter_job" ("EXECUTION_ID_");
CREATE INDEX "ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE" ON "act_ru_deadletter_job" ("PROCESS_INSTANCE_ID_");
CREATE INDEX "ACT_FK_DEADLETTER_JOB_PROC_DEF" ON "act_ru_deadletter_job" ("PROC_DEF_ID_");

CREATE TABLE "act_ru_entitylink" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "LINK_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "PARENT_ELEMENT_ID_" varchar(255) DEFAULT NULL,
    "REF_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "REF_SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "REF_SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "ROOT_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "ROOT_SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "HIERARCHY_TYPE_" varchar(255) DEFAULT NULL,
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_ru_entitylink" IS 'Flowable 引擎表 act_ru_entitylink';
CREATE INDEX "ACT_IDX_ENT_LNK_SCOPE" ON "act_ru_entitylink" ("SCOPE_ID_", "SCOPE_TYPE_", "LINK_TYPE_");
CREATE INDEX "ACT_IDX_ENT_LNK_REF_SCOPE" ON "act_ru_entitylink" ("REF_SCOPE_ID_", "REF_SCOPE_TYPE_", "LINK_TYPE_");
CREATE INDEX "ACT_IDX_ENT_LNK_ROOT_SCOPE" ON "act_ru_entitylink" ("ROOT_SCOPE_ID_", "ROOT_SCOPE_TYPE_", "LINK_TYPE_");
CREATE INDEX "ACT_IDX_ENT_LNK_SCOPE_DEF" ON "act_ru_entitylink" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_", "LINK_TYPE_");

CREATE TABLE "act_ru_event_subscr" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "EVENT_TYPE_" varchar(255) NOT NULL,
    "EVENT_NAME_" varchar(255) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "ACTIVITY_ID_" varchar(64) DEFAULT NULL,
    "CONFIGURATION_" varchar(255) DEFAULT NULL,
    "CREATED_" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_DEFINITION_KEY_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(64) DEFAULT NULL,
    "LOCK_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "LOCK_OWNER_" varchar(255) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_EVENT_EXEC" FOREIGN KEY ("EXECUTION_ID_") REFERENCES "act_ru_execution" ("ID_")
);
COMMENT ON TABLE "act_ru_event_subscr" IS 'Flowable 引擎表 act_ru_event_subscr';
CREATE INDEX "ACT_IDX_EVENT_SUBSCR_CONFIG_" ON "act_ru_event_subscr" ("CONFIGURATION_");
CREATE INDEX "ACT_IDX_EVENT_SUBSCR_EXEC_ID" ON "act_ru_event_subscr" ("EXECUTION_ID_");
CREATE INDEX "ACT_IDX_EVENT_SUBSCR_PROC_ID" ON "act_ru_event_subscr" ("PROC_INST_ID_");
CREATE INDEX "ACT_IDX_EVENT_SUBSCR_SCOPEREF_" ON "act_ru_event_subscr" ("SCOPE_ID_", "SCOPE_TYPE_");

CREATE TABLE "act_ru_external_job" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "TYPE_" varchar(255) NOT NULL,
    "LOCK_EXP_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "LOCK_OWNER_" varchar(255) DEFAULT NULL,
    "EXCLUSIVE_" NUMBER(1) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROCESS_INSTANCE_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "ELEMENT_ID_" varchar(255) DEFAULT NULL,
    "ELEMENT_NAME_" varchar(255) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "CORRELATION_ID_" varchar(255) DEFAULT NULL,
    "RETRIES_" INT DEFAULT NULL,
    "EXCEPTION_STACK_ID_" varchar(64) DEFAULT NULL,
    "EXCEPTION_MSG_" varchar(4000) DEFAULT NULL,
    "DUEDATE_" TIMESTAMP(3) NULL DEFAULT NULL,
    "REPEAT_" varchar(255) DEFAULT NULL,
    "HANDLER_TYPE_" varchar(255) DEFAULT NULL,
    "HANDLER_CFG_" varchar(4000) DEFAULT NULL,
    "CUSTOM_VALUES_ID_" varchar(64) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_EXTERNAL_JOB_CUSTOM_VALUES" FOREIGN KEY ("CUSTOM_VALUES_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_EXTERNAL_JOB_EXCEPTION" FOREIGN KEY ("EXCEPTION_STACK_ID_") REFERENCES "act_ge_bytearray" ("ID_")
);
COMMENT ON TABLE "act_ru_external_job" IS 'Flowable 引擎表 act_ru_external_job';
CREATE INDEX "ACT_IDX_EXTERNAL_JOB_EXCEPTION_STACK_ID" ON "act_ru_external_job" ("EXCEPTION_STACK_ID_");
CREATE INDEX "ACT_IDX_EXTERNAL_JOB_CUSTOM_VALUES_ID" ON "act_ru_external_job" ("CUSTOM_VALUES_ID_");
CREATE INDEX "ACT_IDX_EXTERNAL_JOB_CORRELATION_ID" ON "act_ru_external_job" ("CORRELATION_ID_");
CREATE INDEX "ACT_IDX_EJOB_SCOPE" ON "act_ru_external_job" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_EJOB_SUB_SCOPE" ON "act_ru_external_job" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_EJOB_SCOPE_DEF" ON "act_ru_external_job" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");

CREATE TABLE "act_ru_history_job" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "LOCK_EXP_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "LOCK_OWNER_" varchar(255) DEFAULT NULL,
    "RETRIES_" INT DEFAULT NULL,
    "EXCEPTION_STACK_ID_" varchar(64) DEFAULT NULL,
    "EXCEPTION_MSG_" varchar(4000) DEFAULT NULL,
    "HANDLER_TYPE_" varchar(255) DEFAULT NULL,
    "HANDLER_CFG_" varchar(4000) DEFAULT NULL,
    "CUSTOM_VALUES_ID_" varchar(64) DEFAULT NULL,
    "ADV_HANDLER_CFG_ID_" varchar(64) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "act_ru_history_job" IS 'Flowable 引擎表 act_ru_history_job';

CREATE TABLE "act_ru_task" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "TASK_DEF_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "PROPAGATED_STAGE_INST_ID_" varchar(255) DEFAULT NULL,
    "STATE_" varchar(255) DEFAULT NULL,
    "NAME_" varchar(255) DEFAULT NULL,
    "PARENT_TASK_ID_" varchar(64) DEFAULT NULL,
    "DESCRIPTION_" varchar(4000) DEFAULT NULL,
    "TASK_DEF_KEY_" varchar(255) DEFAULT NULL,
    "OWNER_" varchar(255) DEFAULT NULL,
    "ASSIGNEE_" varchar(255) DEFAULT NULL,
    "DELEGATION_" varchar(64) DEFAULT NULL,
    "PRIORITY_" INT DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "IN_PROGRESS_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "IN_PROGRESS_STARTED_BY_" varchar(255) DEFAULT NULL,
    "CLAIM_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "CLAIMED_BY_" varchar(255) DEFAULT NULL,
    "SUSPENDED_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "SUSPENDED_BY_" varchar(255) DEFAULT NULL,
    "IN_PROGRESS_DUE_DATE_" TIMESTAMP(3) DEFAULT NULL,
    "DUE_DATE_" TIMESTAMP(3) DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "SUSPENSION_STATE_" INT DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    "FORM_KEY_" varchar(255) DEFAULT NULL,
    "IS_COUNT_ENABLED_" NUMBER(3) DEFAULT NULL,
    "VAR_COUNT_" INT DEFAULT NULL,
    "ID_LINK_COUNT_" INT DEFAULT NULL,
    "SUB_TASK_COUNT_" INT DEFAULT NULL,
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_TASK_EXE" FOREIGN KEY ("EXECUTION_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_TASK_PROCDEF" FOREIGN KEY ("PROC_DEF_ID_") REFERENCES "act_re_procdef" ("ID_"),
    CONSTRAINT "ACT_FK_TASK_PROCINST" FOREIGN KEY ("PROC_INST_ID_") REFERENCES "act_ru_execution" ("ID_")
);
COMMENT ON TABLE "act_ru_task" IS 'Flowable 引擎表 act_ru_task';
CREATE INDEX "ACT_IDX_TASK_CREATE" ON "act_ru_task" ("CREATE_TIME_");
CREATE INDEX "ACT_IDX_TASK_SCOPE" ON "act_ru_task" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_TASK_SUB_SCOPE" ON "act_ru_task" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_TASK_SCOPE_DEF" ON "act_ru_task" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_FK_TASK_EXE" ON "act_ru_task" ("EXECUTION_ID_");
CREATE INDEX "ACT_FK_TASK_PROCINST" ON "act_ru_task" ("PROC_INST_ID_");
CREATE INDEX "ACT_FK_TASK_PROCDEF" ON "act_ru_task" ("PROC_DEF_ID_");

CREATE TABLE "act_ru_identitylink" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "GROUP_ID_" varchar(255) DEFAULT NULL,
    "TYPE_" varchar(255) DEFAULT NULL,
    "USER_ID_" varchar(255) DEFAULT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_ATHRZ_PROCEDEF" FOREIGN KEY ("PROC_DEF_ID_") REFERENCES "act_re_procdef" ("ID_"),
    CONSTRAINT "ACT_FK_IDL_PROCINST" FOREIGN KEY ("PROC_INST_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_TSKASS_TASK" FOREIGN KEY ("TASK_ID_") REFERENCES "act_ru_task" ("ID_")
);
COMMENT ON TABLE "act_ru_identitylink" IS 'Flowable 引擎表 act_ru_identitylink';
CREATE INDEX "ACT_IDX_IDENT_LNK_USER" ON "act_ru_identitylink" ("USER_ID_");
CREATE INDEX "ACT_IDX_IDENT_LNK_GROUP" ON "act_ru_identitylink" ("GROUP_ID_");
CREATE INDEX "ACT_IDX_IDENT_LNK_SCOPE" ON "act_ru_identitylink" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_IDENT_LNK_SUB_SCOPE" ON "act_ru_identitylink" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_IDENT_LNK_SCOPE_DEF" ON "act_ru_identitylink" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_ATHRZ_PROCEDEF" ON "act_ru_identitylink" ("PROC_DEF_ID_");
CREATE INDEX "ACT_FK_TSKASS_TASK" ON "act_ru_identitylink" ("TASK_ID_");
CREATE INDEX "ACT_FK_IDL_PROCINST" ON "act_ru_identitylink" ("PROC_INST_ID_");

CREATE TABLE "act_ru_job" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "TYPE_" varchar(255) NOT NULL,
    "LOCK_EXP_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "LOCK_OWNER_" varchar(255) DEFAULT NULL,
    "EXCLUSIVE_" NUMBER(1) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROCESS_INSTANCE_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "ELEMENT_ID_" varchar(255) DEFAULT NULL,
    "ELEMENT_NAME_" varchar(255) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "CORRELATION_ID_" varchar(255) DEFAULT NULL,
    "RETRIES_" INT DEFAULT NULL,
    "EXCEPTION_STACK_ID_" varchar(64) DEFAULT NULL,
    "EXCEPTION_MSG_" varchar(4000) DEFAULT NULL,
    "DUEDATE_" TIMESTAMP(3) NULL DEFAULT NULL,
    "REPEAT_" varchar(255) DEFAULT NULL,
    "HANDLER_TYPE_" varchar(255) DEFAULT NULL,
    "HANDLER_CFG_" varchar(4000) DEFAULT NULL,
    "CUSTOM_VALUES_ID_" varchar(64) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_JOB_CUSTOM_VALUES" FOREIGN KEY ("CUSTOM_VALUES_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_JOB_EXCEPTION" FOREIGN KEY ("EXCEPTION_STACK_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_JOB_EXECUTION" FOREIGN KEY ("EXECUTION_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_JOB_PROCESS_INSTANCE" FOREIGN KEY ("PROCESS_INSTANCE_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_JOB_PROC_DEF" FOREIGN KEY ("PROC_DEF_ID_") REFERENCES "act_re_procdef" ("ID_")
);
COMMENT ON TABLE "act_ru_job" IS 'Flowable 引擎表 act_ru_job';
CREATE INDEX "ACT_IDX_JOB_EXCEPTION_STACK_ID" ON "act_ru_job" ("EXCEPTION_STACK_ID_");
CREATE INDEX "ACT_IDX_JOB_CUSTOM_VALUES_ID" ON "act_ru_job" ("CUSTOM_VALUES_ID_");
CREATE INDEX "ACT_IDX_JOB_CORRELATION_ID" ON "act_ru_job" ("CORRELATION_ID_");
CREATE INDEX "ACT_IDX_JOB_SCOPE" ON "act_ru_job" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_JOB_SUB_SCOPE" ON "act_ru_job" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_JOB_SCOPE_DEF" ON "act_ru_job" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_FK_JOB_EXECUTION" ON "act_ru_job" ("EXECUTION_ID_");
CREATE INDEX "ACT_FK_JOB_PROCESS_INSTANCE" ON "act_ru_job" ("PROCESS_INSTANCE_ID_");
CREATE INDEX "ACT_FK_JOB_PROC_DEF" ON "act_ru_job" ("PROC_DEF_ID_");

CREATE TABLE "act_ru_suspended_job" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "TYPE_" varchar(255) NOT NULL,
    "EXCLUSIVE_" NUMBER(1) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROCESS_INSTANCE_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "ELEMENT_ID_" varchar(255) DEFAULT NULL,
    "ELEMENT_NAME_" varchar(255) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "CORRELATION_ID_" varchar(255) DEFAULT NULL,
    "RETRIES_" INT DEFAULT NULL,
    "EXCEPTION_STACK_ID_" varchar(64) DEFAULT NULL,
    "EXCEPTION_MSG_" varchar(4000) DEFAULT NULL,
    "DUEDATE_" TIMESTAMP(3) NULL DEFAULT NULL,
    "REPEAT_" varchar(255) DEFAULT NULL,
    "HANDLER_TYPE_" varchar(255) DEFAULT NULL,
    "HANDLER_CFG_" varchar(4000) DEFAULT NULL,
    "CUSTOM_VALUES_ID_" varchar(64) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_SUSPENDED_JOB_CUSTOM_VALUES" FOREIGN KEY ("CUSTOM_VALUES_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_SUSPENDED_JOB_EXCEPTION" FOREIGN KEY ("EXCEPTION_STACK_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_SUSPENDED_JOB_EXECUTION" FOREIGN KEY ("EXECUTION_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE" FOREIGN KEY ("PROCESS_INSTANCE_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_SUSPENDED_JOB_PROC_DEF" FOREIGN KEY ("PROC_DEF_ID_") REFERENCES "act_re_procdef" ("ID_")
);
COMMENT ON TABLE "act_ru_suspended_job" IS 'Flowable 引擎表 act_ru_suspended_job';
CREATE INDEX "ACT_IDX_SUSPENDED_JOB_EXCEPTION_STACK_ID" ON "act_ru_suspended_job" ("EXCEPTION_STACK_ID_");
CREATE INDEX "ACT_IDX_SUSPENDED_JOB_CUSTOM_VALUES_ID" ON "act_ru_suspended_job" ("CUSTOM_VALUES_ID_");
CREATE INDEX "ACT_IDX_SUSPENDED_JOB_CORRELATION_ID" ON "act_ru_suspended_job" ("CORRELATION_ID_");
CREATE INDEX "ACT_IDX_SJOB_SCOPE" ON "act_ru_suspended_job" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_SJOB_SUB_SCOPE" ON "act_ru_suspended_job" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_SJOB_SCOPE_DEF" ON "act_ru_suspended_job" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_FK_SUSPENDED_JOB_EXECUTION" ON "act_ru_suspended_job" ("EXECUTION_ID_");
CREATE INDEX "ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE" ON "act_ru_suspended_job" ("PROCESS_INSTANCE_ID_");
CREATE INDEX "ACT_FK_SUSPENDED_JOB_PROC_DEF" ON "act_ru_suspended_job" ("PROC_DEF_ID_");

CREATE TABLE "act_ru_timer_job" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "CATEGORY_" varchar(255) DEFAULT NULL,
    "TYPE_" varchar(255) NOT NULL,
    "LOCK_EXP_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "LOCK_OWNER_" varchar(255) DEFAULT NULL,
    "EXCLUSIVE_" NUMBER(1) DEFAULT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROCESS_INSTANCE_ID_" varchar(64) DEFAULT NULL,
    "PROC_DEF_ID_" varchar(64) DEFAULT NULL,
    "ELEMENT_ID_" varchar(255) DEFAULT NULL,
    "ELEMENT_NAME_" varchar(255) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "SCOPE_DEFINITION_ID_" varchar(255) DEFAULT NULL,
    "CORRELATION_ID_" varchar(255) DEFAULT NULL,
    "RETRIES_" INT DEFAULT NULL,
    "EXCEPTION_STACK_ID_" varchar(64) DEFAULT NULL,
    "EXCEPTION_MSG_" varchar(4000) DEFAULT NULL,
    "DUEDATE_" TIMESTAMP(3) NULL DEFAULT NULL,
    "REPEAT_" varchar(255) DEFAULT NULL,
    "HANDLER_TYPE_" varchar(255) DEFAULT NULL,
    "HANDLER_CFG_" varchar(4000) DEFAULT NULL,
    "CUSTOM_VALUES_ID_" varchar(64) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NULL DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_TIMER_JOB_CUSTOM_VALUES" FOREIGN KEY ("CUSTOM_VALUES_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_TIMER_JOB_EXCEPTION" FOREIGN KEY ("EXCEPTION_STACK_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_TIMER_JOB_EXECUTION" FOREIGN KEY ("EXECUTION_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_TIMER_JOB_PROCESS_INSTANCE" FOREIGN KEY ("PROCESS_INSTANCE_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_TIMER_JOB_PROC_DEF" FOREIGN KEY ("PROC_DEF_ID_") REFERENCES "act_re_procdef" ("ID_")
);
COMMENT ON TABLE "act_ru_timer_job" IS 'Flowable 引擎表 act_ru_timer_job';
CREATE INDEX "ACT_IDX_TIMER_JOB_EXCEPTION_STACK_ID" ON "act_ru_timer_job" ("EXCEPTION_STACK_ID_");
CREATE INDEX "ACT_IDX_TIMER_JOB_CUSTOM_VALUES_ID" ON "act_ru_timer_job" ("CUSTOM_VALUES_ID_");
CREATE INDEX "ACT_IDX_TIMER_JOB_CORRELATION_ID" ON "act_ru_timer_job" ("CORRELATION_ID_");
CREATE INDEX "ACT_IDX_TIMER_JOB_DUEDATE" ON "act_ru_timer_job" ("DUEDATE_");
CREATE INDEX "ACT_IDX_TJOB_SCOPE" ON "act_ru_timer_job" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_TJOB_SUB_SCOPE" ON "act_ru_timer_job" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_TJOB_SCOPE_DEF" ON "act_ru_timer_job" ("SCOPE_DEFINITION_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_FK_TIMER_JOB_EXECUTION" ON "act_ru_timer_job" ("EXECUTION_ID_");
CREATE INDEX "ACT_FK_TIMER_JOB_PROCESS_INSTANCE" ON "act_ru_timer_job" ("PROCESS_INSTANCE_ID_");
CREATE INDEX "ACT_FK_TIMER_JOB_PROC_DEF" ON "act_ru_timer_job" ("PROC_DEF_ID_");

CREATE TABLE "act_ru_variable" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "TYPE_" varchar(255) NOT NULL,
    "NAME_" varchar(255) NOT NULL,
    "EXECUTION_ID_" varchar(64) DEFAULT NULL,
    "PROC_INST_ID_" varchar(64) DEFAULT NULL,
    "TASK_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(255) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(255) DEFAULT NULL,
    "BYTEARRAY_ID_" varchar(64) DEFAULT NULL,
    "DOUBLE_" DOUBLE PRECISION DEFAULT NULL,
    "LONG_" BIGINT DEFAULT NULL,
    "TEXT_" varchar(4000) DEFAULT NULL,
    "TEXT2_" varchar(4000) DEFAULT NULL,
    "META_INFO_" varchar(4000) DEFAULT NULL,
    PRIMARY KEY ("ID_"),
    CONSTRAINT "ACT_FK_VAR_BYTEARRAY" FOREIGN KEY ("BYTEARRAY_ID_") REFERENCES "act_ge_bytearray" ("ID_"),
    CONSTRAINT "ACT_FK_VAR_EXE" FOREIGN KEY ("EXECUTION_ID_") REFERENCES "act_ru_execution" ("ID_"),
    CONSTRAINT "ACT_FK_VAR_PROCINST" FOREIGN KEY ("PROC_INST_ID_") REFERENCES "act_ru_execution" ("ID_")
);
COMMENT ON TABLE "act_ru_variable" IS 'Flowable 引擎表 act_ru_variable';
CREATE INDEX "ACT_IDX_RU_VAR_SCOPE_ID_TYPE" ON "act_ru_variable" ("SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_IDX_RU_VAR_SUB_ID_TYPE" ON "act_ru_variable" ("SUB_SCOPE_ID_", "SCOPE_TYPE_");
CREATE INDEX "ACT_FK_VAR_BYTEARRAY" ON "act_ru_variable" ("BYTEARRAY_ID_");
CREATE INDEX "ACT_IDX_VARIABLE_TASK_ID" ON "act_ru_variable" ("TASK_ID_");
CREATE INDEX "ACT_FK_VAR_EXE" ON "act_ru_variable" ("EXECUTION_ID_");
CREATE INDEX "ACT_FK_VAR_PROCINST" ON "act_ru_variable" ("PROC_INST_ID_");

CREATE TABLE "flw_ru_batch" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "TYPE_" varchar(64) NOT NULL,
    "SEARCH_KEY_" varchar(255) DEFAULT NULL,
    "SEARCH_KEY2_" varchar(255) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NOT NULL,
    "COMPLETE_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "STATUS_" varchar(255) DEFAULT NULL,
    "BATCH_DOC_ID_" varchar(64) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_")
);
COMMENT ON TABLE "flw_ru_batch" IS 'Flowable 引擎表 flw_ru_batch';

CREATE TABLE "flw_ru_batch_part" (
    "ID_" varchar(64) NOT NULL,
    "REV_" INT DEFAULT NULL,
    "BATCH_ID_" varchar(64) DEFAULT NULL,
    "TYPE_" varchar(64) NOT NULL,
    "SCOPE_ID_" varchar(64) DEFAULT NULL,
    "SUB_SCOPE_ID_" varchar(64) DEFAULT NULL,
    "SCOPE_TYPE_" varchar(64) DEFAULT NULL,
    "SEARCH_KEY_" varchar(255) DEFAULT NULL,
    "SEARCH_KEY2_" varchar(255) DEFAULT NULL,
    "CREATE_TIME_" TIMESTAMP(3) NOT NULL,
    "COMPLETE_TIME_" TIMESTAMP(3) DEFAULT NULL,
    "STATUS_" varchar(255) DEFAULT NULL,
    "RESULT_DOC_ID_" varchar(64) DEFAULT NULL,
    "TENANT_ID_" varchar(255) DEFAULT '',
    PRIMARY KEY ("ID_"),
    CONSTRAINT "FLW_FK_BATCH_PART_PARENT" FOREIGN KEY ("BATCH_ID_") REFERENCES "flw_ru_batch" ("ID_")
);
COMMENT ON TABLE "flw_ru_batch_part" IS 'Flowable 引擎表 flw_ru_batch_part';
CREATE INDEX "FLW_IDX_BATCH_PART" ON "flw_ru_batch_part" ("BATCH_ID_");

-- ============================================================
-- 6. 开放平台管理表
-- 来源：zhyc-base-server/zhyc-module-openapi/src/main/resources/db/V1__openapi_core.sql
-- ============================================================
CREATE TABLE "openapi_app" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "app_name" VARCHAR(128) NOT NULL,
    "owner_user_id" BIGINT NOT NULL,
    "auth_mode" VARCHAR(32) NOT NULL,
    "ip_whitelist" CLOB,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_app_tenant_id" UNIQUE ("tenant_id", "id"),
    CONSTRAINT "uk_openapi_app_tenant_code" UNIQUE ("tenant_id", "app_code")
);
COMMENT ON TABLE "openapi_app" IS '开放平台开发者应用表';
COMMENT ON COLUMN "openapi_app"."id" IS '主键';
COMMENT ON COLUMN "openapi_app"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "openapi_app"."app_code" IS '应用编码';
COMMENT ON COLUMN "openapi_app"."app_name" IS '应用名称';
COMMENT ON COLUMN "openapi_app"."owner_user_id" IS '应用负责人用户主键';
COMMENT ON COLUMN "openapi_app"."auth_mode" IS '鉴权方式';
COMMENT ON COLUMN "openapi_app"."ip_whitelist" IS 'IP 白名单 JSON';
COMMENT ON COLUMN "openapi_app"."status" IS '应用状态';
COMMENT ON COLUMN "openapi_app"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_app"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_app_tenant_owner" ON "openapi_app" ("tenant_id", "owner_user_id");
CREATE INDEX "idx_openapi_app_tenant_status" ON "openapi_app" ("tenant_id", "status");

CREATE TABLE "openapi_api_key" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "access_key" VARCHAR(128) NOT NULL,
    "secret_cipher" VARCHAR(512) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "expire_at" TIMESTAMP,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_api_key_access_key" UNIQUE ("access_key"),
    CONSTRAINT "fk_openapi_api_key_app" FOREIGN KEY ("tenant_id", "app_code") REFERENCES "openapi_app" ("tenant_id", "app_code")
);
COMMENT ON TABLE "openapi_api_key" IS '开放平台 API Key 表';
COMMENT ON COLUMN "openapi_api_key"."id" IS '主键';
COMMENT ON COLUMN "openapi_api_key"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "openapi_api_key"."app_code" IS '开发者应用编码';
COMMENT ON COLUMN "openapi_api_key"."access_key" IS 'API 访问密钥';
COMMENT ON COLUMN "openapi_api_key"."secret_cipher" IS 'API Secret 密文';
COMMENT ON COLUMN "openapi_api_key"."status" IS 'API Key 状态';
COMMENT ON COLUMN "openapi_api_key"."expire_at" IS '凭证过期时间';
COMMENT ON COLUMN "openapi_api_key"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_api_key"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_api_key_tenant_app" ON "openapi_api_key" ("tenant_id", "app_code");
CREATE INDEX "idx_openapi_api_key_tenant_status" ON "openapi_api_key" ("tenant_id", "status");

CREATE TABLE "openapi_api_permission" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "api_code" VARCHAR(128) NOT NULL,
    "api_name" VARCHAR(128) NOT NULL,
    "http_method" VARCHAR(16) NOT NULL,
    "path_pattern" VARCHAR(256) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_api_permission_app_api" UNIQUE ("tenant_id", "app_code", "api_code"),
    CONSTRAINT "fk_openapi_api_permission_app" FOREIGN KEY ("tenant_id", "app_code") REFERENCES "openapi_app" ("tenant_id", "app_code")
);
COMMENT ON TABLE "openapi_api_permission" IS '开放平台 API 权限授权表';
COMMENT ON COLUMN "openapi_api_permission"."id" IS '主键';
COMMENT ON COLUMN "openapi_api_permission"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "openapi_api_permission"."app_code" IS '开发者应用编码';
COMMENT ON COLUMN "openapi_api_permission"."api_code" IS 'API 业务编码';
COMMENT ON COLUMN "openapi_api_permission"."api_name" IS 'API 名称';
COMMENT ON COLUMN "openapi_api_permission"."http_method" IS 'HTTP 方法';
COMMENT ON COLUMN "openapi_api_permission"."path_pattern" IS '请求路径匹配规则';
COMMENT ON COLUMN "openapi_api_permission"."status" IS '授权状态';
COMMENT ON COLUMN "openapi_api_permission"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_api_permission"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_api_permission_app_path" ON "openapi_api_permission" ("tenant_id", "app_code", "http_method", "path_pattern");
CREATE INDEX "idx_openapi_api_permission_app_status" ON "openapi_api_permission" ("tenant_id", "app_code", "status");

CREATE TABLE "openapi_oauth_client" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "client_id" VARCHAR(128) NOT NULL,
    "allowed_scopes" VARCHAR(512) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_oauth_client_app_client" UNIQUE ("tenant_id", "app_code", "client_id"),
    CONSTRAINT "fk_openapi_oauth_client_app" FOREIGN KEY ("tenant_id", "app_code") REFERENCES "openapi_app" ("tenant_id", "app_code")
);
COMMENT ON TABLE "openapi_oauth_client" IS '开放平台 OAuth2 客户端映射表';
COMMENT ON COLUMN "openapi_oauth_client"."id" IS '主键';
COMMENT ON COLUMN "openapi_oauth_client"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "openapi_oauth_client"."app_code" IS '开发者应用编码';
COMMENT ON COLUMN "openapi_oauth_client"."client_id" IS '认证中心 OAuth2 客户端 ID';
COMMENT ON COLUMN "openapi_oauth_client"."allowed_scopes" IS '允许的 OAuth2 授权范围';
COMMENT ON COLUMN "openapi_oauth_client"."status" IS '客户端映射状态';
COMMENT ON COLUMN "openapi_oauth_client"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_oauth_client"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_oauth_client_app_status" ON "openapi_oauth_client" ("tenant_id", "app_code", "status");

CREATE TABLE "openapi_catalog" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "api_code" VARCHAR(128) NOT NULL,
    "api_name" VARCHAR(128) NOT NULL,
    "group_code" VARCHAR(64) NOT NULL,
    "http_method" VARCHAR(16) NOT NULL,
    "path_pattern" VARCHAR(256) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_catalog_api_code" UNIQUE ("api_code")
);
COMMENT ON TABLE "openapi_catalog" IS '开放平台 API 目录表';
COMMENT ON COLUMN "openapi_catalog"."id" IS '主键';
COMMENT ON COLUMN "openapi_catalog"."api_code" IS 'API 业务编码';
COMMENT ON COLUMN "openapi_catalog"."api_name" IS 'API 名称';
COMMENT ON COLUMN "openapi_catalog"."group_code" IS 'API 分组编码';
COMMENT ON COLUMN "openapi_catalog"."http_method" IS 'HTTP 方法';
COMMENT ON COLUMN "openapi_catalog"."path_pattern" IS '请求路径匹配规则';
COMMENT ON COLUMN "openapi_catalog"."status" IS 'API 目录状态';
COMMENT ON COLUMN "openapi_catalog"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_catalog"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_catalog_group_status" ON "openapi_catalog" ("group_code", "status");
CREATE INDEX "idx_openapi_catalog_method_path" ON "openapi_catalog" ("http_method", "path_pattern");

CREATE TABLE "openapi_version" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "api_code" VARCHAR(128) NOT NULL,
    "version" VARCHAR(32) NOT NULL,
    "backend_route" VARCHAR(512) NOT NULL,
    "request_schema" JSON,
    "response_schema" JSON,
    "status" VARCHAR(32) NOT NULL DEFAULT 'published',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_version_api_version" UNIQUE ("api_code", "version"),
    CONSTRAINT "fk_openapi_version_catalog" FOREIGN KEY ("api_code") REFERENCES "openapi_catalog" ("api_code")
);
COMMENT ON TABLE "openapi_version" IS '开放平台 API 版本发布表';
COMMENT ON COLUMN "openapi_version"."id" IS '主键';
COMMENT ON COLUMN "openapi_version"."api_code" IS 'API 业务编码';
COMMENT ON COLUMN "openapi_version"."version" IS 'API 版本号';
COMMENT ON COLUMN "openapi_version"."backend_route" IS '后端转发路由';
COMMENT ON COLUMN "openapi_version"."request_schema" IS '请求 JSON Schema';
COMMENT ON COLUMN "openapi_version"."response_schema" IS '响应 JSON Schema';
COMMENT ON COLUMN "openapi_version"."status" IS 'API 版本状态';
COMMENT ON COLUMN "openapi_version"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_version"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_version_api_status" ON "openapi_version" ("api_code", "status");

CREATE TABLE "openapi_signature_policy" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "algorithm" VARCHAR(32) NOT NULL,
    "timestamp_tolerance_seconds" INT NOT NULL,
    "nonce_ttl_seconds" INT NOT NULL,
    "require_body_hash" NUMBER(3) NOT NULL DEFAULT 1,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_signature_policy_app" UNIQUE ("tenant_id", "app_code"),
    CONSTRAINT "fk_openapi_signature_policy_app" FOREIGN KEY ("tenant_id", "app_code") REFERENCES "openapi_app" ("tenant_id", "app_code")
);
COMMENT ON TABLE "openapi_signature_policy" IS '开放平台 API 签名策略表';
COMMENT ON COLUMN "openapi_signature_policy"."id" IS '主键';
COMMENT ON COLUMN "openapi_signature_policy"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "openapi_signature_policy"."app_code" IS '开发者应用编码';
COMMENT ON COLUMN "openapi_signature_policy"."algorithm" IS '签名算法，首期支持 HMAC_SHA256';
COMMENT ON COLUMN "openapi_signature_policy"."timestamp_tolerance_seconds" IS '客户端时间戳允许偏差秒数';
COMMENT ON COLUMN "openapi_signature_policy"."nonce_ttl_seconds" IS 'nonce 防重放有效期秒数';
COMMENT ON COLUMN "openapi_signature_policy"."require_body_hash" IS '是否要求请求体参与摘要，1 是 0 否';
COMMENT ON COLUMN "openapi_signature_policy"."status" IS '签名策略状态';
COMMENT ON COLUMN "openapi_signature_policy"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_signature_policy"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_signature_policy_app_status" ON "openapi_signature_policy" ("tenant_id", "app_code", "status");

CREATE TABLE "openapi_rate_limit_policy" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "api_code" VARCHAR(128) NOT NULL,
    "limit_count" INT NOT NULL,
    "window_seconds" INT NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_rate_limit_policy_app_api" UNIQUE ("tenant_id", "app_code", "api_code"),
    CONSTRAINT "fk_openapi_rate_limit_policy_app" FOREIGN KEY ("tenant_id", "app_code") REFERENCES "openapi_app" ("tenant_id", "app_code")
);
COMMENT ON TABLE "openapi_rate_limit_policy" IS '开放平台 API 限流策略表';
COMMENT ON COLUMN "openapi_rate_limit_policy"."id" IS '主键';
COMMENT ON COLUMN "openapi_rate_limit_policy"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "openapi_rate_limit_policy"."app_code" IS '开发者应用编码';
COMMENT ON COLUMN "openapi_rate_limit_policy"."api_code" IS 'API 业务编码';
COMMENT ON COLUMN "openapi_rate_limit_policy"."limit_count" IS '时间窗口内允许请求次数';
COMMENT ON COLUMN "openapi_rate_limit_policy"."window_seconds" IS '限流时间窗口秒数';
COMMENT ON COLUMN "openapi_rate_limit_policy"."status" IS '限流策略状态';
COMMENT ON COLUMN "openapi_rate_limit_policy"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_rate_limit_policy"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_rate_limit_policy_app_status" ON "openapi_rate_limit_policy" ("tenant_id", "app_code", "status");

CREATE TABLE "openapi_rate_limit_counter" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "api_code" VARCHAR(128) NOT NULL,
    "window_seconds" BIGINT NOT NULL,
    "window_index" BIGINT NOT NULL,
    "request_count" INT NOT NULL DEFAULT 0,
    "expires_at" TIMESTAMP NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_rate_limit_counter_window" UNIQUE ("tenant_id", "app_code", "api_code", "window_seconds", "window_index")
);
COMMENT ON TABLE "openapi_rate_limit_counter" IS '开放平台 API 运行期限流计数表';
COMMENT ON COLUMN "openapi_rate_limit_counter"."id" IS '主键';
COMMENT ON COLUMN "openapi_rate_limit_counter"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "openapi_rate_limit_counter"."app_code" IS '开发者应用编码';
COMMENT ON COLUMN "openapi_rate_limit_counter"."api_code" IS 'API 业务编码';
COMMENT ON COLUMN "openapi_rate_limit_counter"."window_seconds" IS '限流窗口秒数';
COMMENT ON COLUMN "openapi_rate_limit_counter"."window_index" IS '限流窗口序号';
COMMENT ON COLUMN "openapi_rate_limit_counter"."request_count" IS '当前窗口请求次数';
COMMENT ON COLUMN "openapi_rate_limit_counter"."expires_at" IS '窗口过期时间';
COMMENT ON COLUMN "openapi_rate_limit_counter"."created_at" IS '创建时间';
COMMENT ON COLUMN "openapi_rate_limit_counter"."updated_at" IS '更新时间';
CREATE INDEX "idx_openapi_rate_limit_counter_expires" ON "openapi_rate_limit_counter" ("expires_at");

CREATE TABLE "openapi_replay_nonce" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "app_key" VARCHAR(128) NOT NULL,
    "nonce_value" VARCHAR(128) NOT NULL,
    "expires_at" TIMESTAMP NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_openapi_replay_nonce_app_nonce" UNIQUE ("app_key", "nonce_value")
);
COMMENT ON TABLE "openapi_replay_nonce" IS '开放平台 API 防重放 nonce 表';
COMMENT ON COLUMN "openapi_replay_nonce"."id" IS '主键';
COMMENT ON COLUMN "openapi_replay_nonce"."app_key" IS '开放平台应用标识';
COMMENT ON COLUMN "openapi_replay_nonce"."nonce_value" IS '请求一次性随机串';
COMMENT ON COLUMN "openapi_replay_nonce"."expires_at" IS 'nonce 过期时间';
COMMENT ON COLUMN "openapi_replay_nonce"."created_at" IS '创建时间';
CREATE INDEX "idx_openapi_replay_nonce_expires" ON "openapi_replay_nonce" ("expires_at");

CREATE TABLE "openapi_call_audit" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "access_key" VARCHAR(128) NOT NULL,
    "api_code" VARCHAR(128) NOT NULL,
    "http_method" VARCHAR(16) NOT NULL,
    "request_path" VARCHAR(512) NOT NULL,
    "response_status" INT NOT NULL,
    "duration_ms" BIGINT NOT NULL,
    "success" NUMBER(3) NOT NULL,
    "error_code" VARCHAR(64),
    "client_ip" VARCHAR(64) NOT NULL,
    "request_id" VARCHAR(128) NOT NULL,
    "called_at" TIMESTAMP NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "openapi_call_audit" IS '开放平台 API 调用审计表';
COMMENT ON COLUMN "openapi_call_audit"."id" IS '主键';
COMMENT ON COLUMN "openapi_call_audit"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "openapi_call_audit"."app_code" IS '开发者应用编码';
COMMENT ON COLUMN "openapi_call_audit"."access_key" IS 'API 访问密钥';
COMMENT ON COLUMN "openapi_call_audit"."api_code" IS 'API 业务编码';
COMMENT ON COLUMN "openapi_call_audit"."http_method" IS 'HTTP 方法';
COMMENT ON COLUMN "openapi_call_audit"."request_path" IS '请求路径';
COMMENT ON COLUMN "openapi_call_audit"."response_status" IS 'HTTP 响应状态码';
COMMENT ON COLUMN "openapi_call_audit"."duration_ms" IS '调用耗时毫秒';
COMMENT ON COLUMN "openapi_call_audit"."success" IS '是否调用成功，1 是 0 否';
COMMENT ON COLUMN "openapi_call_audit"."error_code" IS '错误编码';
COMMENT ON COLUMN "openapi_call_audit"."client_ip" IS '客户端 IP';
COMMENT ON COLUMN "openapi_call_audit"."request_id" IS '请求追踪 ID';
COMMENT ON COLUMN "openapi_call_audit"."called_at" IS '调用时间';
COMMENT ON COLUMN "openapi_call_audit"."created_at" IS '创建时间';
CREATE INDEX "idx_openapi_call_audit_app_called" ON "openapi_call_audit" ("tenant_id", "app_code", "called_at");
CREATE INDEX "idx_openapi_call_audit_app_api" ON "openapi_call_audit" ("tenant_id", "app_code", "api_code");
CREATE INDEX "idx_openapi_call_audit_request_id" ON "openapi_call_audit" ("request_id");

-- ============================================================
-- 7. AI 能力中心表
-- 来源：zhyc-base-server/zhyc-module-ai/src/main/resources/db/V1__ai_core.sql
-- ============================================================
CREATE TABLE "ai_provider" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "provider_code" VARCHAR(64) NOT NULL,
    "provider_name" VARCHAR(128) NOT NULL,
    "provider_type" VARCHAR(64) NOT NULL,
    "base_url" VARCHAR(512) NOT NULL,
    "secret_ref" VARCHAR(255) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_ai_provider_tenant_code" UNIQUE ("tenant_id", "provider_code")
);
COMMENT ON TABLE "ai_provider" IS 'AI 模型供应商表';
COMMENT ON COLUMN "ai_provider"."id" IS '主键';
COMMENT ON COLUMN "ai_provider"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "ai_provider"."provider_code" IS '供应商编码';
COMMENT ON COLUMN "ai_provider"."provider_name" IS '供应商名称';
COMMENT ON COLUMN "ai_provider"."provider_type" IS '供应商类型';
COMMENT ON COLUMN "ai_provider"."base_url" IS '模型服务基础地址';
COMMENT ON COLUMN "ai_provider"."secret_ref" IS '密钥中心引用';
COMMENT ON COLUMN "ai_provider"."status" IS '状态';
COMMENT ON COLUMN "ai_provider"."created_at" IS '创建时间';
COMMENT ON COLUMN "ai_provider"."updated_at" IS '更新时间';
CREATE INDEX "idx_ai_provider_tenant_status" ON "ai_provider" ("tenant_id", "status");

CREATE TABLE "ai_model_config" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "provider_id" BIGINT NOT NULL,
    "model_code" VARCHAR(128) NOT NULL,
    "model_name" VARCHAR(128) NOT NULL,
    "model_type" VARCHAR(32) NOT NULL,
    "context_window" INT NOT NULL,
    "support_stream" NUMBER(1) NOT NULL DEFAULT 1,
    "support_tool" NUMBER(1) NOT NULL DEFAULT 0,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_ai_model_tenant_code" UNIQUE ("tenant_id", "model_code"),
    CONSTRAINT "fk_ai_model_provider" FOREIGN KEY ("provider_id") REFERENCES "ai_provider" ("id")
);
COMMENT ON TABLE "ai_model_config" IS 'AI 模型配置表';
COMMENT ON COLUMN "ai_model_config"."id" IS '主键';
COMMENT ON COLUMN "ai_model_config"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "ai_model_config"."provider_id" IS '供应商主键';
COMMENT ON COLUMN "ai_model_config"."model_code" IS '模型编码';
COMMENT ON COLUMN "ai_model_config"."model_name" IS '模型名称';
COMMENT ON COLUMN "ai_model_config"."model_type" IS '模型类型';
COMMENT ON COLUMN "ai_model_config"."context_window" IS '上下文长度';
COMMENT ON COLUMN "ai_model_config"."support_stream" IS '是否支持流式输出';
COMMENT ON COLUMN "ai_model_config"."support_tool" IS '是否支持工具调用';
COMMENT ON COLUMN "ai_model_config"."status" IS '状态';
COMMENT ON COLUMN "ai_model_config"."created_at" IS '创建时间';
COMMENT ON COLUMN "ai_model_config"."updated_at" IS '更新时间';
CREATE INDEX "idx_ai_model_tenant_provider" ON "ai_model_config" ("tenant_id", "provider_id");
CREATE INDEX "idx_ai_model_tenant_status" ON "ai_model_config" ("tenant_id", "status");

CREATE TABLE "ai_app" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "app_name" VARCHAR(128) NOT NULL,
    "default_model_id" BIGINT NOT NULL,
    "system_prompt" CLOB NOT NULL,
    "daily_token_quota" INT NOT NULL DEFAULT 100000,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_ai_app_tenant_code" UNIQUE ("tenant_id", "app_code"),
    CONSTRAINT "fk_ai_app_default_model" FOREIGN KEY ("default_model_id") REFERENCES "ai_model_config" ("id")
);
COMMENT ON TABLE "ai_app" IS 'AI 应用接入表';
COMMENT ON COLUMN "ai_app"."id" IS '主键';
COMMENT ON COLUMN "ai_app"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "ai_app"."app_code" IS 'AI 应用编码';
COMMENT ON COLUMN "ai_app"."app_name" IS 'AI 应用名称';
COMMENT ON COLUMN "ai_app"."default_model_id" IS '默认模型配置主键';
COMMENT ON COLUMN "ai_app"."system_prompt" IS '系统提示词';
COMMENT ON COLUMN "ai_app"."daily_token_quota" IS '每日令牌额度';
COMMENT ON COLUMN "ai_app"."status" IS '状态';
COMMENT ON COLUMN "ai_app"."created_at" IS '创建时间';
COMMENT ON COLUMN "ai_app"."updated_at" IS '更新时间';
CREATE INDEX "idx_ai_app_tenant_status" ON "ai_app" ("tenant_id", "status");
CREATE INDEX "idx_ai_app_default_model" ON "ai_app" ("default_model_id");

CREATE TABLE "ai_prompt_template" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "prompt_code" VARCHAR(64) NOT NULL,
    "prompt_name" VARCHAR(128) NOT NULL,
    "version" VARCHAR(32) NOT NULL,
    "template_content" CLOB NOT NULL,
    "variables" VARCHAR(1000) DEFAULT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'draft',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_ai_prompt_tenant_code_version" UNIQUE ("tenant_id", "prompt_code", "version")
);
COMMENT ON TABLE "ai_prompt_template" IS 'AI 提示词模板表';
COMMENT ON COLUMN "ai_prompt_template"."id" IS '主键';
COMMENT ON COLUMN "ai_prompt_template"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "ai_prompt_template"."prompt_code" IS '提示词编码';
COMMENT ON COLUMN "ai_prompt_template"."prompt_name" IS '提示词名称';
COMMENT ON COLUMN "ai_prompt_template"."version" IS '版本号';
COMMENT ON COLUMN "ai_prompt_template"."template_content" IS '模板内容';
COMMENT ON COLUMN "ai_prompt_template"."variables" IS '变量清单';
COMMENT ON COLUMN "ai_prompt_template"."status" IS '状态';
COMMENT ON COLUMN "ai_prompt_template"."created_at" IS '创建时间';
COMMENT ON COLUMN "ai_prompt_template"."updated_at" IS '更新时间';
CREATE INDEX "idx_ai_prompt_tenant_status" ON "ai_prompt_template" ("tenant_id", "status");

CREATE TABLE "ai_invocation_audit" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "app_code" VARCHAR(64) NOT NULL,
    "provider_id" BIGINT NOT NULL,
    "model_id" BIGINT NOT NULL,
    "invocation_type" VARCHAR(32) NOT NULL,
    "prompt_tokens" INT NOT NULL DEFAULT 0,
    "completion_tokens" INT NOT NULL DEFAULT 0,
    "total_tokens" INT NOT NULL DEFAULT 0,
    "latency_ms" BIGINT NOT NULL DEFAULT 0,
    "status" VARCHAR(32) NOT NULL,
    "error_message" VARCHAR(1000) DEFAULT NULL,
    "trace_id" VARCHAR(128) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "fk_ai_audit_provider" FOREIGN KEY ("provider_id") REFERENCES "ai_provider" ("id"),
    CONSTRAINT "fk_ai_audit_model" FOREIGN KEY ("model_id") REFERENCES "ai_model_config" ("id")
);
COMMENT ON TABLE "ai_invocation_audit" IS 'AI 调用审计表';
COMMENT ON COLUMN "ai_invocation_audit"."id" IS '主键';
COMMENT ON COLUMN "ai_invocation_audit"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "ai_invocation_audit"."app_code" IS 'AI 应用编码';
COMMENT ON COLUMN "ai_invocation_audit"."provider_id" IS '供应商主键';
COMMENT ON COLUMN "ai_invocation_audit"."model_id" IS '模型配置主键';
COMMENT ON COLUMN "ai_invocation_audit"."invocation_type" IS '调用类型';
COMMENT ON COLUMN "ai_invocation_audit"."prompt_tokens" IS '提示词令牌数';
COMMENT ON COLUMN "ai_invocation_audit"."completion_tokens" IS '输出令牌数';
COMMENT ON COLUMN "ai_invocation_audit"."total_tokens" IS '总令牌数';
COMMENT ON COLUMN "ai_invocation_audit"."latency_ms" IS '调用耗时毫秒';
COMMENT ON COLUMN "ai_invocation_audit"."status" IS '调用状态';
COMMENT ON COLUMN "ai_invocation_audit"."error_message" IS '错误消息';
COMMENT ON COLUMN "ai_invocation_audit"."trace_id" IS '链路追踪编号';
COMMENT ON COLUMN "ai_invocation_audit"."created_at" IS '创建时间';
CREATE INDEX "idx_ai_audit_tenant_app_time" ON "ai_invocation_audit" ("tenant_id", "app_code", "created_at");
CREATE INDEX "idx_ai_audit_tenant_model_time" ON "ai_invocation_audit" ("tenant_id", "model_id", "created_at");
CREATE INDEX "idx_ai_audit_trace" ON "ai_invocation_audit" ("trace_id");

-- ============================================================
-- 8. 消息中心表
-- 来源：zhyc-base-server/zhyc-module-message/src/main/resources/db/V1__message_core.sql
-- ============================================================
CREATE TABLE "msg_template" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "template_code" VARCHAR(64) NOT NULL,
    "template_name" VARCHAR(128) NOT NULL,
    "channel_type" VARCHAR(32) NOT NULL,
    "title_template" VARCHAR(255) NOT NULL,
    "content_template" CLOB NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_by" BIGINT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_by" BIGINT NULL,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_msg_template_tenant_code" UNIQUE ("tenant_id", "template_code")
);
COMMENT ON TABLE "msg_template" IS '消息模板表';
COMMENT ON COLUMN "msg_template"."id" IS '主键 ID';
COMMENT ON COLUMN "msg_template"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "msg_template"."template_code" IS '模板编码';
COMMENT ON COLUMN "msg_template"."template_name" IS '模板名称';
COMMENT ON COLUMN "msg_template"."channel_type" IS '消息通道类型';
COMMENT ON COLUMN "msg_template"."title_template" IS '标题模板';
COMMENT ON COLUMN "msg_template"."content_template" IS '内容模板';
COMMENT ON COLUMN "msg_template"."status" IS '模板状态';
COMMENT ON COLUMN "msg_template"."created_by" IS '创建人用户 ID';
COMMENT ON COLUMN "msg_template"."created_at" IS '创建时间';
COMMENT ON COLUMN "msg_template"."updated_by" IS '更新人用户 ID';
COMMENT ON COLUMN "msg_template"."updated_at" IS '更新时间';
COMMENT ON COLUMN "msg_template"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "msg_template"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "msg_template"."remark" IS '备注';
CREATE INDEX "idx_msg_template_tenant_status" ON "msg_template" ("tenant_id", "status");

CREATE TABLE "msg_message" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "message_code" VARCHAR(64) NOT NULL,
    "receiver_id" BIGINT NOT NULL,
    "receiver_name" VARCHAR(128) NULL,
    "message_type" VARCHAR(32) NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "content" CLOB NOT NULL,
    "read_flag" NUMBER(3) NOT NULL DEFAULT 0,
    "read_at" TIMESTAMP NULL,
    "created_by" BIGINT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_msg_message_tenant_code" UNIQUE ("tenant_id", "message_code")
);
COMMENT ON TABLE "msg_message" IS '站内消息表';
COMMENT ON COLUMN "msg_message"."id" IS '主键 ID';
COMMENT ON COLUMN "msg_message"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "msg_message"."message_code" IS '消息编码';
COMMENT ON COLUMN "msg_message"."receiver_id" IS '接收人用户 ID';
COMMENT ON COLUMN "msg_message"."receiver_name" IS '接收人名称';
COMMENT ON COLUMN "msg_message"."message_type" IS '消息类型';
COMMENT ON COLUMN "msg_message"."title" IS '消息标题';
COMMENT ON COLUMN "msg_message"."content" IS '消息内容';
COMMENT ON COLUMN "msg_message"."read_flag" IS '是否已读，0 未读，1 已读';
COMMENT ON COLUMN "msg_message"."read_at" IS '阅读时间';
COMMENT ON COLUMN "msg_message"."created_by" IS '创建人用户 ID';
COMMENT ON COLUMN "msg_message"."created_at" IS '创建时间';
COMMENT ON COLUMN "msg_message"."updated_at" IS '更新时间';
COMMENT ON COLUMN "msg_message"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
CREATE INDEX "idx_msg_message_tenant_receiver" ON "msg_message" ("tenant_id", "receiver_id", "read_flag", "created_at");
CREATE INDEX "idx_msg_message_tenant_type" ON "msg_message" ("tenant_id", "message_type");

-- ============================================================
-- 9. 文件中心表
-- 来源：zhyc-base-server/zhyc-module-file/src/main/resources/db/V1__file_core.sql
-- ============================================================
CREATE TABLE "file_storage_config" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "storage_code" VARCHAR(64) NOT NULL,
    "storage_name" VARCHAR(128) NOT NULL,
    "storage_type" VARCHAR(32) NOT NULL,
    "endpoint" VARCHAR(255) NOT NULL,
    "status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "default_flag" NUMBER(3) NOT NULL DEFAULT 0,
    "created_by" BIGINT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_by" BIGINT NULL,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_file_storage_config_tenant_code" UNIQUE ("tenant_id", "storage_code")
);
COMMENT ON TABLE "file_storage_config" IS '文件存储配置表';
COMMENT ON COLUMN "file_storage_config"."id" IS '主键 ID';
COMMENT ON COLUMN "file_storage_config"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "file_storage_config"."storage_code" IS '存储配置编码';
COMMENT ON COLUMN "file_storage_config"."storage_name" IS '存储配置名称';
COMMENT ON COLUMN "file_storage_config"."storage_type" IS '存储类型，例如 local、s3、minio、oss';
COMMENT ON COLUMN "file_storage_config"."endpoint" IS '存储访问端点或本地根路径';
COMMENT ON COLUMN "file_storage_config"."status" IS '配置状态';
COMMENT ON COLUMN "file_storage_config"."default_flag" IS '是否默认存储配置';
COMMENT ON COLUMN "file_storage_config"."created_by" IS '创建人用户 ID';
COMMENT ON COLUMN "file_storage_config"."created_at" IS '创建时间';
COMMENT ON COLUMN "file_storage_config"."updated_by" IS '更新人用户 ID';
COMMENT ON COLUMN "file_storage_config"."updated_at" IS '更新时间';
COMMENT ON COLUMN "file_storage_config"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "file_storage_config"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "file_storage_config"."remark" IS '备注';
CREATE INDEX "idx_file_storage_config_tenant_status" ON "file_storage_config" ("tenant_id", "status");
CREATE INDEX "idx_file_storage_config_tenant_default" ON "file_storage_config" ("tenant_id", "default_flag");

CREATE TABLE "file_object" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "file_code" VARCHAR(64) NOT NULL,
    "storage_code" VARCHAR(64) NOT NULL,
    "original_name" VARCHAR(255) NOT NULL,
    "content_type" VARCHAR(128) NOT NULL,
    "file_size" BIGINT NOT NULL DEFAULT 0,
    "object_key" VARCHAR(500) NOT NULL,
    "file_status" VARCHAR(32) NOT NULL DEFAULT 'stored',
    "uploader_id" BIGINT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_file_object_tenant_code" UNIQUE ("tenant_id", "file_code")
);
COMMENT ON TABLE "file_object" IS '文件对象表';
COMMENT ON COLUMN "file_object"."id" IS '主键 ID';
COMMENT ON COLUMN "file_object"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "file_object"."file_code" IS '文件业务编码';
COMMENT ON COLUMN "file_object"."storage_code" IS '存储配置编码';
COMMENT ON COLUMN "file_object"."original_name" IS '原始文件名';
COMMENT ON COLUMN "file_object"."content_type" IS '文件内容类型';
COMMENT ON COLUMN "file_object"."file_size" IS '文件大小，单位字节';
COMMENT ON COLUMN "file_object"."object_key" IS '存储对象键或相对路径';
COMMENT ON COLUMN "file_object"."file_status" IS '文件状态';
COMMENT ON COLUMN "file_object"."uploader_id" IS '上传人用户 ID';
COMMENT ON COLUMN "file_object"."created_at" IS '创建时间';
COMMENT ON COLUMN "file_object"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
CREATE INDEX "idx_file_object_tenant_storage" ON "file_object" ("tenant_id", "storage_code");
CREATE INDEX "idx_file_object_tenant_created" ON "file_object" ("tenant_id", "created_at");
CREATE INDEX "idx_file_object_tenant_status" ON "file_object" ("tenant_id", "file_status");

CREATE TABLE "file_preview_log" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "file_code" VARCHAR(64) NOT NULL,
    "preview_type" VARCHAR(32) NOT NULL,
    "preview_url" VARCHAR(512) NOT NULL,
    "result" VARCHAR(32) NOT NULL,
    "cost_ms" BIGINT NOT NULL DEFAULT 0,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "file_preview_log" IS '文件预览日志表';
COMMENT ON COLUMN "file_preview_log"."id" IS '文件预览日志主键';
COMMENT ON COLUMN "file_preview_log"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "file_preview_log"."file_code" IS '文件业务编码';
COMMENT ON COLUMN "file_preview_log"."preview_type" IS '预览类型';
COMMENT ON COLUMN "file_preview_log"."preview_url" IS '预览访问地址';
COMMENT ON COLUMN "file_preview_log"."result" IS '预览结果';
COMMENT ON COLUMN "file_preview_log"."cost_ms" IS '预览耗时毫秒';
COMMENT ON COLUMN "file_preview_log"."created_at" IS '创建时间';
CREATE INDEX "idx_file_preview_log_tenant_file" ON "file_preview_log" ("tenant_id", "file_code");
CREATE INDEX "idx_file_preview_log_created_at" ON "file_preview_log" ("created_at");

-- ============================================================
-- 10. 内容管理表
-- 来源：zhyc-base-server/zhyc-module-cms/src/main/resources/db/V1__cms_core.sql
-- ============================================================
CREATE TABLE "cms_channel" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "parent_id" BIGINT NULL,
    "channel_code" VARCHAR(64) NOT NULL,
    "channel_name" VARCHAR(128) NOT NULL,
    "sort_order" INT NOT NULL DEFAULT 0,
    "channel_status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_cms_channel_tenant_code" UNIQUE ("tenant_id", "channel_code")
);
COMMENT ON TABLE "cms_channel" IS '内容栏目';
COMMENT ON COLUMN "cms_channel"."id" IS '内容栏目主键';
COMMENT ON COLUMN "cms_channel"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "cms_channel"."parent_id" IS '父栏目主键';
COMMENT ON COLUMN "cms_channel"."channel_code" IS '栏目编码';
COMMENT ON COLUMN "cms_channel"."channel_name" IS '栏目名称';
COMMENT ON COLUMN "cms_channel"."sort_order" IS '排序号';
COMMENT ON COLUMN "cms_channel"."channel_status" IS '栏目状态';
COMMENT ON COLUMN "cms_channel"."created_at" IS '创建时间';
COMMENT ON COLUMN "cms_channel"."updated_at" IS '更新时间';
COMMENT ON COLUMN "cms_channel"."deleted" IS '逻辑删除标识';
CREATE INDEX "idx_cms_channel_tenant_status" ON "cms_channel" ("tenant_id", "channel_status");

CREATE TABLE "cms_content" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "channel_code" VARCHAR(64) NOT NULL,
    "title" VARCHAR(200) NOT NULL,
    "summary" VARCHAR(500) NULL,
    "body_content" CLOB NULL,
    "content_status" VARCHAR(32) NOT NULL DEFAULT 'draft',
    "author_id" BIGINT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "cms_content" IS '内容文章';
COMMENT ON COLUMN "cms_content"."id" IS '内容文章主键';
COMMENT ON COLUMN "cms_content"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "cms_content"."channel_code" IS '栏目编码';
COMMENT ON COLUMN "cms_content"."title" IS '文章标题';
COMMENT ON COLUMN "cms_content"."summary" IS '文章摘要';
COMMENT ON COLUMN "cms_content"."body_content" IS '文章正文';
COMMENT ON COLUMN "cms_content"."content_status" IS '文章状态';
COMMENT ON COLUMN "cms_content"."author_id" IS '作者用户主键';
COMMENT ON COLUMN "cms_content"."created_at" IS '创建时间';
COMMENT ON COLUMN "cms_content"."updated_at" IS '更新时间';
COMMENT ON COLUMN "cms_content"."deleted" IS '逻辑删除标识';
CREATE INDEX "idx_cms_content_tenant_channel" ON "cms_content" ("tenant_id", "channel_code");
CREATE INDEX "idx_cms_content_tenant_status" ON "cms_content" ("tenant_id", "content_status");

-- ============================================================
-- 11. 在线作业表
-- 来源：zhyc-base-server/zhyc-module-job/src/main/resources/db/V1__job_core.sql
-- ============================================================
CREATE TABLE "job_task" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "job_code" VARCHAR(64) NOT NULL,
    "job_name" VARCHAR(128) NOT NULL,
    "cron_expression" VARCHAR(128) NOT NULL,
    "handler_name" VARCHAR(128) NOT NULL,
    "job_description" VARCHAR(500) NULL,
    "job_status" VARCHAR(32) NOT NULL DEFAULT 'disabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_job_task_tenant_code" UNIQUE ("tenant_id", "job_code")
);
COMMENT ON TABLE "job_task" IS '在线作业任务';
COMMENT ON COLUMN "job_task"."id" IS '作业任务主键';
COMMENT ON COLUMN "job_task"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "job_task"."job_code" IS '作业任务编码';
COMMENT ON COLUMN "job_task"."job_name" IS '作业任务名称';
COMMENT ON COLUMN "job_task"."cron_expression" IS 'Cron 表达式';
COMMENT ON COLUMN "job_task"."handler_name" IS '任务处理器名称';
COMMENT ON COLUMN "job_task"."job_description" IS '作业任务说明';
COMMENT ON COLUMN "job_task"."job_status" IS '作业状态';
COMMENT ON COLUMN "job_task"."created_at" IS '创建时间';
COMMENT ON COLUMN "job_task"."updated_at" IS '更新时间';
COMMENT ON COLUMN "job_task"."deleted" IS '逻辑删除标识';
CREATE INDEX "idx_job_task_tenant_status" ON "job_task" ("tenant_id", "job_status");

CREATE TABLE "job_task_log" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "job_id" BIGINT NOT NULL,
    "trigger_type" VARCHAR(32) NOT NULL,
    "start_at" TIMESTAMP NOT NULL,
    "end_at" TIMESTAMP NULL,
    "result" VARCHAR(32) NOT NULL,
    "error_message" VARCHAR(1000) NULL,
    "operator_id" BIGINT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "job_task_log" IS '作业执行日志';
COMMENT ON COLUMN "job_task_log"."id" IS '作业执行日志主键';
COMMENT ON COLUMN "job_task_log"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "job_task_log"."job_id" IS '作业任务主键';
COMMENT ON COLUMN "job_task_log"."trigger_type" IS '触发类型';
COMMENT ON COLUMN "job_task_log"."start_at" IS '开始时间';
COMMENT ON COLUMN "job_task_log"."end_at" IS '结束时间';
COMMENT ON COLUMN "job_task_log"."result" IS '执行结果';
COMMENT ON COLUMN "job_task_log"."error_message" IS '错误信息';
COMMENT ON COLUMN "job_task_log"."operator_id" IS '操作人用户主键';
COMMENT ON COLUMN "job_task_log"."created_at" IS '创建时间';
CREATE INDEX "idx_job_task_log_tenant_job" ON "job_task_log" ("tenant_id", "job_id", "start_at");

-- ============================================================
-- 12. 全文检索表
-- 来源：zhyc-base-server/zhyc-module-search/src/main/resources/db/V1__search_core.sql
-- ============================================================
CREATE TABLE "search_index_config" (
    "id" BIGINT IDENTITY(1,1) PRIMARY KEY,
    "tenant_id" VARCHAR(64) NOT NULL,
    "index_code" VARCHAR(128) NOT NULL,
    "index_name" VARCHAR(128) NOT NULL,
    "source_table" VARCHAR(128) NOT NULL,
    "search_fields" VARCHAR(512) NOT NULL,
    "filter_fields" VARCHAR(512) DEFAULT NULL,
    "index_status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "remark" VARCHAR(512) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    CONSTRAINT "uk_search_index_config_tenant_code" UNIQUE ("tenant_id", "index_code")
);
COMMENT ON TABLE "search_index_config" IS '全文检索索引配置';
COMMENT ON COLUMN "search_index_config"."id" IS '索引配置主键';
COMMENT ON COLUMN "search_index_config"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "search_index_config"."index_code" IS '索引编码';
COMMENT ON COLUMN "search_index_config"."index_name" IS '索引名称';
COMMENT ON COLUMN "search_index_config"."source_table" IS '数据来源表名';
COMMENT ON COLUMN "search_index_config"."search_fields" IS '可检索字段列表，逗号分隔';
COMMENT ON COLUMN "search_index_config"."filter_fields" IS '可过滤字段列表，逗号分隔';
COMMENT ON COLUMN "search_index_config"."index_status" IS '索引状态';
COMMENT ON COLUMN "search_index_config"."remark" IS '配置备注';
COMMENT ON COLUMN "search_index_config"."created_at" IS '创建时间';
COMMENT ON COLUMN "search_index_config"."updated_at" IS '更新时间';
COMMENT ON COLUMN "search_index_config"."deleted" IS '逻辑删除标记';
CREATE INDEX "idx_search_index_config_tenant_status" ON "search_index_config" ("tenant_id", "index_status");

CREATE TABLE "search_rebuild_task" (
    "id" BIGINT IDENTITY(1,1) PRIMARY KEY,
    "tenant_id" VARCHAR(64) NOT NULL,
    "index_code" VARCHAR(128) NOT NULL,
    "task_status" VARCHAR(32) NOT NULL DEFAULT 'pending',
    "trigger_type" VARCHAR(32) NOT NULL DEFAULT 'manual',
    "started_at" TIMESTAMP DEFAULT NULL,
    "finished_at" TIMESTAMP DEFAULT NULL,
    "error_message" VARCHAR(1024) DEFAULT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0
);
COMMENT ON TABLE "search_rebuild_task" IS '全文检索索引重建任务';
COMMENT ON COLUMN "search_rebuild_task"."id" IS '重建任务主键';
COMMENT ON COLUMN "search_rebuild_task"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "search_rebuild_task"."index_code" IS '索引编码';
COMMENT ON COLUMN "search_rebuild_task"."task_status" IS '任务状态';
COMMENT ON COLUMN "search_rebuild_task"."trigger_type" IS '触发类型';
COMMENT ON COLUMN "search_rebuild_task"."started_at" IS '任务开始时间';
COMMENT ON COLUMN "search_rebuild_task"."finished_at" IS '任务完成时间';
COMMENT ON COLUMN "search_rebuild_task"."error_message" IS '失败错误信息';
COMMENT ON COLUMN "search_rebuild_task"."created_at" IS '创建时间';
COMMENT ON COLUMN "search_rebuild_task"."updated_at" IS '更新时间';
COMMENT ON COLUMN "search_rebuild_task"."deleted" IS '逻辑删除标记';
CREATE INDEX "idx_search_rebuild_task_tenant_index" ON "search_rebuild_task" ("tenant_id", "index_code");
CREATE INDEX "idx_search_rebuild_task_status" ON "search_rebuild_task" ("task_status");

CREATE TABLE "search_query_log" (
    "id" BIGINT IDENTITY(1,1) PRIMARY KEY,
    "tenant_id" VARCHAR(64) NOT NULL,
    "index_code" VARCHAR(128) NOT NULL,
    "keyword" VARCHAR(256) NOT NULL,
    "result_count" INT NOT NULL DEFAULT 0,
    "cost_ms" BIGINT NOT NULL DEFAULT 0,
    "query_status" VARCHAR(32) NOT NULL DEFAULT 'success',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE "search_query_log" IS '全文检索查询日志';
COMMENT ON COLUMN "search_query_log"."id" IS '查询日志主键';
COMMENT ON COLUMN "search_query_log"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "search_query_log"."index_code" IS '索引编码';
COMMENT ON COLUMN "search_query_log"."keyword" IS '查询关键词';
COMMENT ON COLUMN "search_query_log"."result_count" IS '返回结果数量';
COMMENT ON COLUMN "search_query_log"."cost_ms" IS '查询耗时毫秒';
COMMENT ON COLUMN "search_query_log"."query_status" IS '查询状态';
COMMENT ON COLUMN "search_query_log"."created_at" IS '创建时间';
CREATE INDEX "idx_search_query_log_tenant_index" ON "search_query_log" ("tenant_id", "index_code");
CREATE INDEX "idx_search_query_log_created_at" ON "search_query_log" ("created_at");

-- ============================================================
-- 13. 可视化大屏表
-- 来源：zhyc-base-server/zhyc-module-visual/src/main/resources/db/V1__visual_core.sql
-- ============================================================
CREATE TABLE "visual_dataset" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "dataset_code" VARCHAR(64) NOT NULL,
    "dataset_name" VARCHAR(128) NOT NULL,
    "datasource_code" VARCHAR(64) NOT NULL,
    "sql_text" CLOB NOT NULL,
    "dataset_status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_visual_dataset_tenant_code" UNIQUE ("tenant_id", "dataset_code")
);
COMMENT ON TABLE "visual_dataset" IS '可视化数据集表';
COMMENT ON COLUMN "visual_dataset"."id" IS '主键';
COMMENT ON COLUMN "visual_dataset"."tenant_id" IS '租户业务编码，用于共享表模式数据隔离';
COMMENT ON COLUMN "visual_dataset"."dataset_code" IS '数据集编码，租户内唯一';
COMMENT ON COLUMN "visual_dataset"."dataset_name" IS '数据集名称';
COMMENT ON COLUMN "visual_dataset"."datasource_code" IS '数据源编码，对应低代码数据源或默认数据源';
COMMENT ON COLUMN "visual_dataset"."sql_text" IS '查询 SQL，由数据集执行器统一校验后执行';
COMMENT ON COLUMN "visual_dataset"."dataset_status" IS '数据集状态';
COMMENT ON COLUMN "visual_dataset"."created_at" IS '创建时间';
COMMENT ON COLUMN "visual_dataset"."updated_at" IS '更新时间';
COMMENT ON COLUMN "visual_dataset"."deleted" IS '逻辑删除标识';
CREATE INDEX "idx_visual_dataset_tenant_status" ON "visual_dataset" ("tenant_id", "dataset_status");

CREATE TABLE "visual_report" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "report_code" VARCHAR(64) NOT NULL,
    "report_name" VARCHAR(128) NOT NULL,
    "dataset_code" VARCHAR(64) NOT NULL,
    "chart_type" VARCHAR(32) NOT NULL DEFAULT 'table',
    "config_json" CLOB NOT NULL,
    "report_status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_visual_report_tenant_code" UNIQUE ("tenant_id", "report_code")
);
COMMENT ON TABLE "visual_report" IS '可视化报表表';
COMMENT ON COLUMN "visual_report"."id" IS '主键';
COMMENT ON COLUMN "visual_report"."tenant_id" IS '租户业务编码，用于共享表模式数据隔离';
COMMENT ON COLUMN "visual_report"."report_code" IS '报表编码，租户内唯一';
COMMENT ON COLUMN "visual_report"."report_name" IS '报表名称';
COMMENT ON COLUMN "visual_report"."dataset_code" IS '数据集编码，指向同租户数据集';
COMMENT ON COLUMN "visual_report"."chart_type" IS '图表类型';
COMMENT ON COLUMN "visual_report"."config_json" IS '图表配置 JSON';
COMMENT ON COLUMN "visual_report"."report_status" IS '报表状态';
COMMENT ON COLUMN "visual_report"."created_at" IS '创建时间';
COMMENT ON COLUMN "visual_report"."updated_at" IS '更新时间';
COMMENT ON COLUMN "visual_report"."deleted" IS '逻辑删除标识';
CREATE INDEX "idx_visual_report_tenant_status" ON "visual_report" ("tenant_id", "report_status");
CREATE INDEX "idx_visual_report_tenant_dataset" ON "visual_report" ("tenant_id", "dataset_code");

CREATE TABLE "visual_screen" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "screen_code" VARCHAR(64) NOT NULL,
    "screen_name" VARCHAR(128) NOT NULL,
    "layout_json" CLOB NOT NULL,
    "screen_status" VARCHAR(32) NOT NULL DEFAULT 'draft',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_visual_screen_tenant_code" UNIQUE ("tenant_id", "screen_code")
);
COMMENT ON TABLE "visual_screen" IS '可视化大屏表';
COMMENT ON COLUMN "visual_screen"."id" IS '主键';
COMMENT ON COLUMN "visual_screen"."tenant_id" IS '租户业务编码，用于共享表模式数据隔离';
COMMENT ON COLUMN "visual_screen"."screen_code" IS '大屏编码，租户内唯一';
COMMENT ON COLUMN "visual_screen"."screen_name" IS '大屏名称';
COMMENT ON COLUMN "visual_screen"."layout_json" IS '大屏布局 JSON，保存组件位置、尺寸和报表编码';
COMMENT ON COLUMN "visual_screen"."screen_status" IS '大屏状态';
COMMENT ON COLUMN "visual_screen"."created_at" IS '创建时间';
COMMENT ON COLUMN "visual_screen"."updated_at" IS '更新时间';
COMMENT ON COLUMN "visual_screen"."deleted" IS '逻辑删除标识';
CREATE INDEX "idx_visual_screen_tenant_status" ON "visual_screen" ("tenant_id", "screen_status");

-- ============================================================
-- 14. 国际化资源表
-- 来源：zhyc-base-server/zhyc-module-i18n/src/main/resources/db/V1__i18n_core.sql
-- ============================================================
CREATE TABLE "i18n_message" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "locale" VARCHAR(32) NOT NULL,
    "message_key" VARCHAR(190) NOT NULL,
    "message_value" VARCHAR(1000) NOT NULL,
    "message_status" VARCHAR(32) NOT NULL DEFAULT 'enabled',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_i18n_message_tenant_locale_key" UNIQUE ("tenant_id", "locale", "message_key")
);
COMMENT ON TABLE "i18n_message" IS '国际化词条表';
COMMENT ON COLUMN "i18n_message"."id" IS '主键';
COMMENT ON COLUMN "i18n_message"."tenant_id" IS '租户业务编码，用于共享表模式数据隔离';
COMMENT ON COLUMN "i18n_message"."locale" IS '语言标识，例如 zh-CN、en-US';
COMMENT ON COLUMN "i18n_message"."message_key" IS '词条键';
COMMENT ON COLUMN "i18n_message"."message_value" IS '词条值';
COMMENT ON COLUMN "i18n_message"."message_status" IS '词条状态';
COMMENT ON COLUMN "i18n_message"."created_at" IS '创建时间';
COMMENT ON COLUMN "i18n_message"."updated_at" IS '更新时间';
COMMENT ON COLUMN "i18n_message"."deleted" IS '逻辑删除标识';
CREATE INDEX "idx_i18n_message_tenant_locale_status" ON "i18n_message" ("tenant_id", "locale", "message_status");

-- ============================================================
-- 15. 采购样板业务表
-- 来源：zhyc-base-server/zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql
-- ============================================================
CREATE TABLE "pur_request" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "request_no" VARCHAR(64) NOT NULL,
    "request_title" VARCHAR(128) NOT NULL,
    "applicant_id" BIGINT NOT NULL,
    "org_id" BIGINT NOT NULL,
    "total_amount" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    "request_reason" VARCHAR(1000) NULL,
    "process_status" VARCHAR(32) NOT NULL,
    "process_instance_id" VARCHAR(128) NULL,
    "submitted_at" TIMESTAMP NULL,
    "created_by" BIGINT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_by" BIGINT NULL,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_pur_request_tenant_no" UNIQUE ("tenant_id", "request_no")
);
COMMENT ON TABLE "pur_request" IS '采购申请主表';
COMMENT ON COLUMN "pur_request"."id" IS '主键 ID';
COMMENT ON COLUMN "pur_request"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "pur_request"."request_no" IS '采购申请单号';
COMMENT ON COLUMN "pur_request"."request_title" IS '采购申请标题';
COMMENT ON COLUMN "pur_request"."applicant_id" IS '申请人用户 ID';
COMMENT ON COLUMN "pur_request"."org_id" IS '申请部门 ID';
COMMENT ON COLUMN "pur_request"."total_amount" IS '采购申请总金额';
COMMENT ON COLUMN "pur_request"."request_reason" IS '采购申请原因';
COMMENT ON COLUMN "pur_request"."process_status" IS '流程状态';
COMMENT ON COLUMN "pur_request"."process_instance_id" IS '流程实例 ID';
COMMENT ON COLUMN "pur_request"."submitted_at" IS '提交审批时间';
COMMENT ON COLUMN "pur_request"."created_by" IS '创建人用户 ID';
COMMENT ON COLUMN "pur_request"."created_at" IS '创建时间';
COMMENT ON COLUMN "pur_request"."updated_by" IS '更新人用户 ID';
COMMENT ON COLUMN "pur_request"."updated_at" IS '更新时间';
COMMENT ON COLUMN "pur_request"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "pur_request"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "pur_request"."remark" IS '备注';
CREATE INDEX "idx_pur_request_tenant_applicant" ON "pur_request" ("tenant_id", "applicant_id");
CREATE INDEX "idx_pur_request_tenant_status" ON "pur_request" ("tenant_id", "process_status");
CREATE INDEX "idx_pur_request_tenant_process" ON "pur_request" ("tenant_id", "process_instance_id");

CREATE TABLE "pur_order" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "order_no" VARCHAR(64) NOT NULL,
    "request_no" VARCHAR(64) NOT NULL,
    "supplier_id" BIGINT NOT NULL,
    "buyer_id" BIGINT NOT NULL,
    "total_amount" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    "order_status" VARCHAR(32) NOT NULL,
    "created_by" BIGINT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_by" BIGINT NULL,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "remark" VARCHAR(500) NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_pur_order_tenant_no" UNIQUE ("tenant_id", "order_no")
);
COMMENT ON TABLE "pur_order" IS '采购订单主表';
COMMENT ON COLUMN "pur_order"."id" IS '主键 ID';
COMMENT ON COLUMN "pur_order"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "pur_order"."order_no" IS '采购订单号';
COMMENT ON COLUMN "pur_order"."request_no" IS '采购申请单号';
COMMENT ON COLUMN "pur_order"."supplier_id" IS '供应商 ID';
COMMENT ON COLUMN "pur_order"."buyer_id" IS '采购员用户 ID';
COMMENT ON COLUMN "pur_order"."total_amount" IS '采购订单总金额';
COMMENT ON COLUMN "pur_order"."order_status" IS '订单状态';
COMMENT ON COLUMN "pur_order"."created_by" IS '创建人用户 ID';
COMMENT ON COLUMN "pur_order"."created_at" IS '创建时间';
COMMENT ON COLUMN "pur_order"."updated_by" IS '更新人用户 ID';
COMMENT ON COLUMN "pur_order"."updated_at" IS '更新时间';
COMMENT ON COLUMN "pur_order"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
COMMENT ON COLUMN "pur_order"."version" IS '乐观锁版本号';
COMMENT ON COLUMN "pur_order"."remark" IS '备注';
CREATE INDEX "idx_pur_order_tenant_request" ON "pur_order" ("tenant_id", "request_no");
CREATE INDEX "idx_pur_order_tenant_status" ON "pur_order" ("tenant_id", "order_status");

CREATE TABLE "pur_order_item" (
    "id" BIGINT IDENTITY(1,1) NOT NULL,
    "tenant_id" VARCHAR(64) NOT NULL,
    "order_no" VARCHAR(64) NOT NULL,
    "item_name" VARCHAR(128) NOT NULL,
    "quantity" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    "unit_price" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    "amount" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" NUMBER(3) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "pur_order_item" IS '采购订单明细表';
COMMENT ON COLUMN "pur_order_item"."id" IS '主键 ID';
COMMENT ON COLUMN "pur_order_item"."tenant_id" IS '租户业务编码';
COMMENT ON COLUMN "pur_order_item"."order_no" IS '采购订单号';
COMMENT ON COLUMN "pur_order_item"."item_name" IS '物品名称';
COMMENT ON COLUMN "pur_order_item"."quantity" IS '采购数量';
COMMENT ON COLUMN "pur_order_item"."unit_price" IS '采购单价';
COMMENT ON COLUMN "pur_order_item"."amount" IS '明细金额';
COMMENT ON COLUMN "pur_order_item"."created_at" IS '创建时间';
COMMENT ON COLUMN "pur_order_item"."deleted" IS '逻辑删除标识，0 未删除，1 已删除';
CREATE INDEX "idx_pur_order_item_tenant_order" ON "pur_order_item" ("tenant_id", "order_no");

-- 采购申请状态开放 API 目录注册，用于开放 API 网关运行态路由发现。
-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO openapi_catalog，请按目标数据库语法单独审阅后导入。

-- 采购申请状态开放 API 版本注册，用于开放 API 网关定位后端服务入口。
-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO openapi_version，请按目标数据库语法单独审阅后导入。

-- 采购订单详情开放 API 目录注册，用于开放 API 网关运行态路由发现。
-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO openapi_catalog，请按目标数据库语法单独审阅后导入。

-- 采购订单详情开放 API 版本注册，用于开放 API 网关定位后端服务入口。
-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO openapi_version，请按目标数据库语法单独审阅后导入。

-- 初始化脚本结束。
