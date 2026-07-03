-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

-- ZHYC 快速开发平台 SQL Server 初始化脚本
-- 说明：由 zhyc-base-server/scripts/build-database-init-sql.mjs 从模块 DDL 生成。
-- 说明：当前脚本只包含表结构初始化；基础种子数据需按目标数据库单独审阅后导入。

-- ============================================================
-- 1. 认证中心核心表
-- 来源：zhyc-base-server/zhyc-auth-server/src/main/resources/db/V1__auth_server_core.sql
-- ============================================================
IF OBJECT_ID(N'oauth2_registered_client', N'U') IS NULL
BEGIN
CREATE TABLE [oauth2_registered_client] (
    [id] NVARCHAR(100) NOT NULL,
    [client_id] NVARCHAR(100) NOT NULL,
    [client_id_issued_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [client_secret] NVARCHAR(200) DEFAULT NULL,
    [client_secret_expires_at] DATETIME2 NULL DEFAULT NULL,
    [client_name] NVARCHAR(200) NOT NULL,
    [client_authentication_methods] NVARCHAR(1000) NOT NULL,
    [authorization_grant_types] NVARCHAR(1000) NOT NULL,
    [redirect_uris] NVARCHAR(1000) DEFAULT NULL,
    [post_logout_redirect_uris] NVARCHAR(1000) DEFAULT NULL,
    [scopes] NVARCHAR(1000) NOT NULL,
    [client_settings] NVARCHAR(2000) NOT NULL,
    [token_settings] NVARCHAR(2000) NOT NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_oauth2_registered_client_client_id] UNIQUE ([client_id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'OAuth2 注册客户端', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client';
EXEC sp_addextendedproperty N'MS_Description', N'注册客户端主键', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'OAuth2 客户端标识', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'client_id';
EXEC sp_addextendedproperty N'MS_Description', N'客户端标识签发时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'client_id_issued_at';
EXEC sp_addextendedproperty N'MS_Description', N'BCrypt 编码后的客户端密钥', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'client_secret';
EXEC sp_addextendedproperty N'MS_Description', N'客户端密钥过期时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'client_secret_expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'客户端名称', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'client_name';
EXEC sp_addextendedproperty N'MS_Description', N'客户端认证方式集合', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'client_authentication_methods';
EXEC sp_addextendedproperty N'MS_Description', N'授权模式集合', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'authorization_grant_types';
EXEC sp_addextendedproperty N'MS_Description', N'授权码回调地址集合', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'redirect_uris';
EXEC sp_addextendedproperty N'MS_Description', N'登出回调地址集合', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'post_logout_redirect_uris';
EXEC sp_addextendedproperty N'MS_Description', N'授权范围集合', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'scopes';
EXEC sp_addextendedproperty N'MS_Description', N'客户端设置 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'client_settings';
EXEC sp_addextendedproperty N'MS_Description', N'令牌设置 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_registered_client', N'COLUMN', N'token_settings';

IF OBJECT_ID(N'oauth2_authorization', N'U') IS NULL
BEGIN
CREATE TABLE [oauth2_authorization] (
    [id] NVARCHAR(100) NOT NULL,
    [registered_client_id] NVARCHAR(100) NOT NULL,
    [principal_name] NVARCHAR(200) NOT NULL,
    [authorization_grant_type] NVARCHAR(100) NOT NULL,
    [authorized_scopes] NVARCHAR(1000) DEFAULT NULL,
    [attributes] VARBINARY(MAX) DEFAULT NULL,
    [state] NVARCHAR(500) DEFAULT NULL,
    [authorization_code_value] VARBINARY(MAX) DEFAULT NULL,
    [authorization_code_issued_at] DATETIME2 NULL DEFAULT NULL,
    [authorization_code_expires_at] DATETIME2 NULL DEFAULT NULL,
    [authorization_code_metadata] VARBINARY(MAX) DEFAULT NULL,
    [access_token_value] VARBINARY(MAX) DEFAULT NULL,
    [access_token_issued_at] DATETIME2 NULL DEFAULT NULL,
    [access_token_expires_at] DATETIME2 NULL DEFAULT NULL,
    [access_token_metadata] VARBINARY(MAX) DEFAULT NULL,
    [access_token_type] NVARCHAR(100) DEFAULT NULL,
    [access_token_scopes] NVARCHAR(1000) DEFAULT NULL,
    [oidc_id_token_value] VARBINARY(MAX) DEFAULT NULL,
    [oidc_id_token_issued_at] DATETIME2 NULL DEFAULT NULL,
    [oidc_id_token_expires_at] DATETIME2 NULL DEFAULT NULL,
    [oidc_id_token_metadata] VARBINARY(MAX) DEFAULT NULL,
    [refresh_token_value] VARBINARY(MAX) DEFAULT NULL,
    [refresh_token_issued_at] DATETIME2 NULL DEFAULT NULL,
    [refresh_token_expires_at] DATETIME2 NULL DEFAULT NULL,
    [refresh_token_metadata] VARBINARY(MAX) DEFAULT NULL,
    [user_code_value] VARBINARY(MAX) DEFAULT NULL,
    [user_code_issued_at] DATETIME2 NULL DEFAULT NULL,
    [user_code_expires_at] DATETIME2 NULL DEFAULT NULL,
    [user_code_metadata] VARBINARY(MAX) DEFAULT NULL,
    [device_code_value] VARBINARY(MAX) DEFAULT NULL,
    [device_code_issued_at] DATETIME2 NULL DEFAULT NULL,
    [device_code_expires_at] DATETIME2 NULL DEFAULT NULL,
    [device_code_metadata] VARBINARY(MAX) DEFAULT NULL,
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'OAuth2 授权记录', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization';
EXEC sp_addextendedproperty N'MS_Description', N'授权记录主键', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'注册客户端主键', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'registered_client_id';
EXEC sp_addextendedproperty N'MS_Description', N'授权主体名称', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'principal_name';
EXEC sp_addextendedproperty N'MS_Description', N'授权模式', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'authorization_grant_type';
EXEC sp_addextendedproperty N'MS_Description', N'已授权范围集合', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'authorized_scopes';
EXEC sp_addextendedproperty N'MS_Description', N'授权属性 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'attributes';
EXEC sp_addextendedproperty N'MS_Description', N'授权请求状态值', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'state';
EXEC sp_addextendedproperty N'MS_Description', N'授权码密文', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'authorization_code_value';
EXEC sp_addextendedproperty N'MS_Description', N'授权码签发时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'authorization_code_issued_at';
EXEC sp_addextendedproperty N'MS_Description', N'授权码过期时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'authorization_code_expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'授权码元数据 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'authorization_code_metadata';
EXEC sp_addextendedproperty N'MS_Description', N'访问令牌密文', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'access_token_value';
EXEC sp_addextendedproperty N'MS_Description', N'访问令牌签发时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'access_token_issued_at';
EXEC sp_addextendedproperty N'MS_Description', N'访问令牌过期时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'access_token_expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'访问令牌元数据 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'access_token_metadata';
EXEC sp_addextendedproperty N'MS_Description', N'访问令牌类型', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'access_token_type';
EXEC sp_addextendedproperty N'MS_Description', N'访问令牌范围集合', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'access_token_scopes';
EXEC sp_addextendedproperty N'MS_Description', N'OIDC ID Token 密文', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'oidc_id_token_value';
EXEC sp_addextendedproperty N'MS_Description', N'OIDC ID Token 签发时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'oidc_id_token_issued_at';
EXEC sp_addextendedproperty N'MS_Description', N'OIDC ID Token 过期时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'oidc_id_token_expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'OIDC ID Token 元数据 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'oidc_id_token_metadata';
EXEC sp_addextendedproperty N'MS_Description', N'刷新令牌密文', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'refresh_token_value';
EXEC sp_addextendedproperty N'MS_Description', N'刷新令牌签发时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'refresh_token_issued_at';
EXEC sp_addextendedproperty N'MS_Description', N'刷新令牌过期时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'refresh_token_expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'刷新令牌元数据 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'refresh_token_metadata';
EXEC sp_addextendedproperty N'MS_Description', N'设备授权用户码密文', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'user_code_value';
EXEC sp_addextendedproperty N'MS_Description', N'设备授权用户码签发时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'user_code_issued_at';
EXEC sp_addextendedproperty N'MS_Description', N'设备授权用户码过期时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'user_code_expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'设备授权用户码元数据 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'user_code_metadata';
EXEC sp_addextendedproperty N'MS_Description', N'设备授权设备码密文', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'device_code_value';
EXEC sp_addextendedproperty N'MS_Description', N'设备授权设备码签发时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'device_code_issued_at';
EXEC sp_addextendedproperty N'MS_Description', N'设备授权设备码过期时间', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'device_code_expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'设备授权设备码元数据 JSON', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization', N'COLUMN', N'device_code_metadata';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_oauth2_authorization_client_principal' AND object_id = OBJECT_ID(N'oauth2_authorization'))
CREATE INDEX [idx_oauth2_authorization_client_principal] ON [oauth2_authorization] ([registered_client_id], [principal_name]);

IF OBJECT_ID(N'oauth2_authorization_consent', N'U') IS NULL
BEGIN
CREATE TABLE [oauth2_authorization_consent] (
    [registered_client_id] NVARCHAR(100) NOT NULL,
    [principal_name] NVARCHAR(200) NOT NULL,
    [authorities] NVARCHAR(1000) NOT NULL,
    PRIMARY KEY ([registered_client_id], [principal_name])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'OAuth2 授权确认', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization_consent';
EXEC sp_addextendedproperty N'MS_Description', N'注册客户端主键', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization_consent', N'COLUMN', N'registered_client_id';
EXEC sp_addextendedproperty N'MS_Description', N'授权主体名称', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization_consent', N'COLUMN', N'principal_name';
EXEC sp_addextendedproperty N'MS_Description', N'已确认授权范围集合', N'SCHEMA', N'dbo', N'TABLE', N'oauth2_authorization_consent', N'COLUMN', N'authorities';

-- ============================================================
-- 2. 系统与租户核心表
-- 来源：zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql
-- ============================================================
-- sys_tenant.tenant_id 是租户业务编码，不是所属租户字段；平台级租户管理查询需要绕过普通租户过滤。
IF OBJECT_ID(N'sys_tenant', N'U') IS NULL
BEGIN
CREATE TABLE [sys_tenant] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [tenant_name] NVARCHAR(128) NOT NULL,
    [package_id] BIGINT DEFAULT NULL,
    [isolation_mode] NVARCHAR(32) NOT NULL DEFAULT 'TENANT_COLUMN',
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [contact_name] NVARCHAR(64) DEFAULT NULL,
    [contact_phone] NVARCHAR(32) DEFAULT NULL,
    [expire_at] DATETIME2 DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_tenant_tenant_id] UNIQUE ([tenant_id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'租户主表', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'租户名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'tenant_name';
EXEC sp_addextendedproperty N'MS_Description', N'当前租户套餐 ID', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'package_id';
EXEC sp_addextendedproperty N'MS_Description', N'租户隔离模式', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'isolation_mode';
EXEC sp_addextendedproperty N'MS_Description', N'租户状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'租户联系人', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'contact_name';
EXEC sp_addextendedproperty N'MS_Description', N'联系电话', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'contact_phone';
EXEC sp_addextendedproperty N'MS_Description', N'到期时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'expire_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_tenant_package' AND object_id = OBJECT_ID(N'sys_tenant'))
CREATE INDEX [idx_sys_tenant_package] ON [sys_tenant] ([package_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_tenant_status' AND object_id = OBJECT_ID(N'sys_tenant'))
CREATE INDEX [idx_sys_tenant_status] ON [sys_tenant] ([status]);

IF OBJECT_ID(N'sys_tenant_package', N'U') IS NULL
BEGIN
CREATE TABLE [sys_tenant_package] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [package_code] NVARCHAR(64) NOT NULL,
    [package_name] NVARCHAR(128) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [max_user_count] INT NOT NULL DEFAULT 0,
    [max_storage_mb] INT NOT NULL DEFAULT 0,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_tenant_package_code] UNIQUE ([package_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'租户套餐表', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'套餐编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package', N'COLUMN', N'package_code';
EXEC sp_addextendedproperty N'MS_Description', N'套餐名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package', N'COLUMN', N'package_name';
EXEC sp_addextendedproperty N'MS_Description', N'套餐状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'最大用户数', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package', N'COLUMN', N'max_user_count';
EXEC sp_addextendedproperty N'MS_Description', N'最大存储容量 MB', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package', N'COLUMN', N'max_storage_mb';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_tenant_package_status' AND object_id = OBJECT_ID(N'sys_tenant_package'))
CREATE INDEX [idx_sys_tenant_package_status] ON [sys_tenant_package] ([status]);

IF OBJECT_ID(N'sys_tenant_package_module', N'U') IS NULL
BEGIN
CREATE TABLE [sys_tenant_package_module] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [package_id] BIGINT NOT NULL,
    [module_code] NVARCHAR(64) NOT NULL,
    [menu_code] NVARCHAR(64) DEFAULT NULL,
    [permission] NVARCHAR(128) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_tenant_package_module_resource] UNIQUE ([package_id], [module_code], [menu_code], [permission])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'租户套餐模块授权表', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package_module';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package_module', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户套餐主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package_module', N'COLUMN', N'package_id';
EXEC sp_addextendedproperty N'MS_Description', N'模块编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package_module', N'COLUMN', N'module_code';
EXEC sp_addextendedproperty N'MS_Description', N'菜单编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package_module', N'COLUMN', N'menu_code';
EXEC sp_addextendedproperty N'MS_Description', N'权限标识', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package_module', N'COLUMN', N'permission';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_package_module', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_tenant_package_module_package' AND object_id = OBJECT_ID(N'sys_tenant_package_module'))
CREATE INDEX [idx_sys_tenant_package_module_package] ON [sys_tenant_package_module] ([package_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_tenant_package_module_module' AND object_id = OBJECT_ID(N'sys_tenant_package_module'))
CREATE INDEX [idx_sys_tenant_package_module_module] ON [sys_tenant_package_module] ([module_code]);

IF OBJECT_ID(N'sys_tenant_param', N'U') IS NULL
BEGIN
CREATE TABLE [sys_tenant_param] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [param_key] NVARCHAR(128) NOT NULL,
    [param_value] NVARCHAR(1000) DEFAULT NULL,
    [value_type] NVARCHAR(32) NOT NULL DEFAULT 'string',
    [visible] SMALLINT NOT NULL DEFAULT 1,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_tenant_param_key] UNIQUE ([tenant_id], [param_key])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'租户参数表', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'参数键', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param', N'COLUMN', N'param_key';
EXEC sp_addextendedproperty N'MS_Description', N'参数值', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param', N'COLUMN', N'param_value';
EXEC sp_addextendedproperty N'MS_Description', N'参数值类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param', N'COLUMN', N'value_type';
EXEC sp_addextendedproperty N'MS_Description', N'是否显示', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param', N'COLUMN', N'visible';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_tenant_param', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_tenant_param_tenant_visible' AND object_id = OBJECT_ID(N'sys_tenant_param'))
CREATE INDEX [idx_sys_tenant_param_tenant_visible] ON [sys_tenant_param] ([tenant_id], [visible]);

IF OBJECT_ID(N'sys_user', N'U') IS NULL
BEGIN
CREATE TABLE [sys_user] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [username] NVARCHAR(64) NOT NULL,
    [nickname] NVARCHAR(128) DEFAULT NULL,
    [password_hash] NVARCHAR(255) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_user_tenant_id] UNIQUE ([tenant_id], [id]),
    CONSTRAINT [uk_sys_user_tenant_username] UNIQUE ([tenant_id], [username])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统用户表', N'SCHEMA', N'dbo', N'TABLE', N'sys_user';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_user', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_user', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'登录账号', N'SCHEMA', N'dbo', N'TABLE', N'sys_user', N'COLUMN', N'username';
EXEC sp_addextendedproperty N'MS_Description', N'用户显示名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_user', N'COLUMN', N'nickname';
EXEC sp_addextendedproperty N'MS_Description', N'密码哈希值', N'SCHEMA', N'dbo', N'TABLE', N'sys_user', N'COLUMN', N'password_hash';
EXEC sp_addextendedproperty N'MS_Description', N'用户状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_user', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_user', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_user', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_user_tenant_status' AND object_id = OBJECT_ID(N'sys_user'))
CREATE INDEX [idx_sys_user_tenant_status] ON [sys_user] ([tenant_id], [status]);

IF OBJECT_ID(N'sys_role', N'U') IS NULL
BEGIN
CREATE TABLE [sys_role] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [role_code] NVARCHAR(64) NOT NULL,
    [name] NVARCHAR(128) NOT NULL,
    [data_scope] NVARCHAR(32) NOT NULL DEFAULT 'SELF',
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_role_tenant_id] UNIQUE ([tenant_id], [id]),
    CONSTRAINT [uk_sys_role_tenant_code] UNIQUE ([tenant_id], [role_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统角色表', N'SCHEMA', N'dbo', N'TABLE', N'sys_role';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_role', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_role', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'角色编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_role', N'COLUMN', N'role_code';
EXEC sp_addextendedproperty N'MS_Description', N'角色名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_role', N'COLUMN', N'name';
EXEC sp_addextendedproperty N'MS_Description', N'数据权限范围', N'SCHEMA', N'dbo', N'TABLE', N'sys_role', N'COLUMN', N'data_scope';
EXEC sp_addextendedproperty N'MS_Description', N'角色状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_role', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_role', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_role', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_role_tenant_status' AND object_id = OBJECT_ID(N'sys_role'))
CREATE INDEX [idx_sys_role_tenant_status] ON [sys_role] ([tenant_id], [status]);

IF OBJECT_ID(N'sys_menu', N'U') IS NULL
BEGIN
CREATE TABLE [sys_menu] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [parent_id] BIGINT DEFAULT NULL,
    [menu_code] NVARCHAR(64) NOT NULL,
    [menu_name] NVARCHAR(128) NOT NULL,
    [menu_type] NVARCHAR(32) NOT NULL,
    [path] NVARCHAR(255) DEFAULT NULL,
    [component] NVARCHAR(255) DEFAULT NULL,
    [permission] NVARCHAR(128) DEFAULT NULL,
    [sort_order] INT NOT NULL DEFAULT 0,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_menu_tenant_id] UNIQUE ([tenant_id], [id]),
    CONSTRAINT [uk_sys_menu_tenant_code] UNIQUE ([tenant_id], [menu_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统菜单表', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'父级菜单主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'parent_id';
EXEC sp_addextendedproperty N'MS_Description', N'菜单编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'menu_code';
EXEC sp_addextendedproperty N'MS_Description', N'菜单名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'menu_name';
EXEC sp_addextendedproperty N'MS_Description', N'菜单类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'menu_type';
EXEC sp_addextendedproperty N'MS_Description', N'前端路由路径', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'path';
EXEC sp_addextendedproperty N'MS_Description', N'前端组件路径', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'component';
EXEC sp_addextendedproperty N'MS_Description', N'权限标识', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'permission';
EXEC sp_addextendedproperty N'MS_Description', N'排序号', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'sort_order';
EXEC sp_addextendedproperty N'MS_Description', N'菜单状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_menu', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_menu_tenant_parent' AND object_id = OBJECT_ID(N'sys_menu'))
CREATE INDEX [idx_sys_menu_tenant_parent] ON [sys_menu] ([tenant_id], [parent_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_menu_tenant_permission' AND object_id = OBJECT_ID(N'sys_menu'))
CREATE INDEX [idx_sys_menu_tenant_permission] ON [sys_menu] ([tenant_id], [permission]);

IF OBJECT_ID(N'sys_org', N'U') IS NULL
BEGIN
CREATE TABLE [sys_org] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [parent_id] BIGINT DEFAULT NULL,
    [ancestors] NVARCHAR(500) NOT NULL DEFAULT '0',
    [org_code] NVARCHAR(64) NOT NULL,
    [org_name] NVARCHAR(128) NOT NULL,
    [leader_user_id] BIGINT DEFAULT NULL,
    [sort_order] INT NOT NULL DEFAULT 0,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_org_tenant_id] UNIQUE ([tenant_id], [id]),
    CONSTRAINT [uk_sys_org_tenant_code] UNIQUE ([tenant_id], [org_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统组织机构表', N'SCHEMA', N'dbo', N'TABLE', N'sys_org';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'父级组织主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'parent_id';
EXEC sp_addextendedproperty N'MS_Description', N'祖级组织路径', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'ancestors';
EXEC sp_addextendedproperty N'MS_Description', N'组织编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'org_code';
EXEC sp_addextendedproperty N'MS_Description', N'组织名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'org_name';
EXEC sp_addextendedproperty N'MS_Description', N'负责人用户主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'leader_user_id';
EXEC sp_addextendedproperty N'MS_Description', N'排序号', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'sort_order';
EXEC sp_addextendedproperty N'MS_Description', N'组织状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_org', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_org_tenant_parent' AND object_id = OBJECT_ID(N'sys_org'))
CREATE INDEX [idx_sys_org_tenant_parent] ON [sys_org] ([tenant_id], [parent_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_org_tenant_status' AND object_id = OBJECT_ID(N'sys_org'))
CREATE INDEX [idx_sys_org_tenant_status] ON [sys_org] ([tenant_id], [status]);

IF OBJECT_ID(N'sys_post', N'U') IS NULL
BEGIN
CREATE TABLE [sys_post] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [org_id] BIGINT DEFAULT NULL,
    [post_code] NVARCHAR(64) NOT NULL,
    [post_name] NVARCHAR(128) NOT NULL,
    [sort_order] INT NOT NULL DEFAULT 0,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_post_tenant_id] UNIQUE ([tenant_id], [id]),
    CONSTRAINT [uk_sys_post_tenant_code] UNIQUE ([tenant_id], [post_code]),
    CONSTRAINT [fk_sys_post_org] FOREIGN KEY ([tenant_id], [org_id]) REFERENCES [sys_org] ([tenant_id], [id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统岗位表', N'SCHEMA', N'dbo', N'TABLE', N'sys_post';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'所属组织主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'org_id';
EXEC sp_addextendedproperty N'MS_Description', N'岗位编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'post_code';
EXEC sp_addextendedproperty N'MS_Description', N'岗位名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'post_name';
EXEC sp_addextendedproperty N'MS_Description', N'排序号', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'sort_order';
EXEC sp_addextendedproperty N'MS_Description', N'岗位状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_post', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_post_tenant_org' AND object_id = OBJECT_ID(N'sys_post'))
CREATE INDEX [idx_sys_post_tenant_org] ON [sys_post] ([tenant_id], [org_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_post_tenant_status' AND object_id = OBJECT_ID(N'sys_post'))
CREATE INDEX [idx_sys_post_tenant_status] ON [sys_post] ([tenant_id], [status]);

IF OBJECT_ID(N'sys_user_post', N'U') IS NULL
BEGIN
CREATE TABLE [sys_user_post] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [user_id] BIGINT NOT NULL,
    [post_id] BIGINT NOT NULL,
    [primary_flag] BIT NOT NULL DEFAULT 0,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_user_post_tenant_user_post] UNIQUE ([tenant_id], [user_id], [post_id]),
    CONSTRAINT [fk_sys_user_post_user] FOREIGN KEY ([tenant_id], [user_id]) REFERENCES [sys_user] ([tenant_id], [id]),
    CONSTRAINT [fk_sys_user_post_post] FOREIGN KEY ([tenant_id], [post_id]) REFERENCES [sys_post] ([tenant_id], [id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'用户岗位关联表', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_post';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_post', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_post', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'用户主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_post', N'COLUMN', N'user_id';
EXEC sp_addextendedproperty N'MS_Description', N'岗位主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_post', N'COLUMN', N'post_id';
EXEC sp_addextendedproperty N'MS_Description', N'是否主岗位', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_post', N'COLUMN', N'primary_flag';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_post', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_user_post_tenant_post' AND object_id = OBJECT_ID(N'sys_user_post'))
CREATE INDEX [idx_sys_user_post_tenant_post] ON [sys_user_post] ([tenant_id], [post_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_user_post_tenant_primary' AND object_id = OBJECT_ID(N'sys_user_post'))
CREATE INDEX [idx_sys_user_post_tenant_primary] ON [sys_user_post] ([tenant_id], [user_id], [primary_flag]);

IF OBJECT_ID(N'sys_user_role', N'U') IS NULL
BEGIN
CREATE TABLE [sys_user_role] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [user_id] BIGINT NOT NULL,
    [role_id] BIGINT NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_user_role_tenant_user_role] UNIQUE ([tenant_id], [user_id], [role_id]),
    CONSTRAINT [fk_sys_user_role_user] FOREIGN KEY ([tenant_id], [user_id]) REFERENCES [sys_user] ([tenant_id], [id]),
    CONSTRAINT [fk_sys_user_role_role] FOREIGN KEY ([tenant_id], [role_id]) REFERENCES [sys_role] ([tenant_id], [id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'用户角色关联表', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_role';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_role', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_role', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'用户主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_role', N'COLUMN', N'user_id';
EXEC sp_addextendedproperty N'MS_Description', N'角色主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_role', N'COLUMN', N'role_id';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_user_role', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_user_role_tenant_role' AND object_id = OBJECT_ID(N'sys_user_role'))
CREATE INDEX [idx_sys_user_role_tenant_role] ON [sys_user_role] ([tenant_id], [role_id]);

IF OBJECT_ID(N'sys_admin_scope', N'U') IS NULL
BEGIN
CREATE TABLE [sys_admin_scope] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [user_id] BIGINT NOT NULL,
    [scope_type] NVARCHAR(32) NOT NULL,
    [scope_ref_code] NVARCHAR(128) NOT NULL,
    [scope_name] NVARCHAR(128) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_admin_scope] UNIQUE ([tenant_id], [user_id], [scope_type], [scope_ref_code]),
    CONSTRAINT [fk_sys_admin_scope_user] FOREIGN KEY ([tenant_id], [user_id]) REFERENCES [sys_user] ([tenant_id], [id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'管理员管理范围表', N'SCHEMA', N'dbo', N'TABLE', N'sys_admin_scope';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_admin_scope', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_admin_scope', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'管理员用户主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_admin_scope', N'COLUMN', N'user_id';
EXEC sp_addextendedproperty N'MS_Description', N'范围类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_admin_scope', N'COLUMN', N'scope_type';
EXEC sp_addextendedproperty N'MS_Description', N'范围引用编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_admin_scope', N'COLUMN', N'scope_ref_code';
EXEC sp_addextendedproperty N'MS_Description', N'范围展示名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_admin_scope', N'COLUMN', N'scope_name';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_admin_scope', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_admin_scope_user' AND object_id = OBJECT_ID(N'sys_admin_scope'))
CREATE INDEX [idx_sys_admin_scope_user] ON [sys_admin_scope] ([tenant_id], [user_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_admin_scope_ref' AND object_id = OBJECT_ID(N'sys_admin_scope'))
CREATE INDEX [idx_sys_admin_scope_ref] ON [sys_admin_scope] ([tenant_id], [scope_type], [scope_ref_code]);

IF OBJECT_ID(N'sys_role_menu', N'U') IS NULL
BEGIN
CREATE TABLE [sys_role_menu] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [role_id] BIGINT NOT NULL,
    [menu_id] BIGINT NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_role_menu_tenant_role_menu] UNIQUE ([tenant_id], [role_id], [menu_id]),
    CONSTRAINT [fk_sys_role_menu_role] FOREIGN KEY ([tenant_id], [role_id]) REFERENCES [sys_role] ([tenant_id], [id]),
    CONSTRAINT [fk_sys_role_menu_menu] FOREIGN KEY ([tenant_id], [menu_id]) REFERENCES [sys_menu] ([tenant_id], [id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'角色菜单关联表', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_menu';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_menu', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_menu', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'角色主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_menu', N'COLUMN', N'role_id';
EXEC sp_addextendedproperty N'MS_Description', N'菜单主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_menu', N'COLUMN', N'menu_id';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_menu', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_role_menu_tenant_menu' AND object_id = OBJECT_ID(N'sys_role_menu'))
CREATE INDEX [idx_sys_role_menu_tenant_menu] ON [sys_role_menu] ([tenant_id], [menu_id]);

IF OBJECT_ID(N'sys_role_data_scope', N'U') IS NULL
BEGIN
CREATE TABLE [sys_role_data_scope] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [role_id] BIGINT NOT NULL,
    [org_id] BIGINT NOT NULL,
    [scope_type] NVARCHAR(32) NOT NULL DEFAULT 'org',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_role_data_scope] UNIQUE ([tenant_id], [role_id], [org_id], [scope_type]),
    CONSTRAINT [fk_sys_role_data_scope_role] FOREIGN KEY ([tenant_id], [role_id]) REFERENCES [sys_role] ([tenant_id], [id]),
    CONSTRAINT [fk_sys_role_data_scope_org] FOREIGN KEY ([tenant_id], [org_id]) REFERENCES [sys_org] ([tenant_id], [id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'角色自定义数据权限表', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_data_scope';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_data_scope', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_data_scope', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'角色主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_data_scope', N'COLUMN', N'role_id';
EXEC sp_addextendedproperty N'MS_Description', N'授权组织主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_data_scope', N'COLUMN', N'org_id';
EXEC sp_addextendedproperty N'MS_Description', N'范围类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_data_scope', N'COLUMN', N'scope_type';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_role_data_scope', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_role_data_scope_org' AND object_id = OBJECT_ID(N'sys_role_data_scope'))
CREATE INDEX [idx_sys_role_data_scope_org] ON [sys_role_data_scope] ([tenant_id], [org_id]);

IF OBJECT_ID(N'sys_login_log', N'U') IS NULL
BEGIN
CREATE TABLE [sys_login_log] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [user_id] BIGINT DEFAULT NULL,
    [username] NVARCHAR(64) DEFAULT NULL,
    [login_type] NVARCHAR(32) NOT NULL,
    [result] NVARCHAR(32) NOT NULL,
    [client_ip] NVARCHAR(64) DEFAULT NULL,
    [user_agent] NVARCHAR(512) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统登录日志表', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'登录用户主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'user_id';
EXEC sp_addextendedproperty N'MS_Description', N'登录账号', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'username';
EXEC sp_addextendedproperty N'MS_Description', N'登录方式', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'login_type';
EXEC sp_addextendedproperty N'MS_Description', N'登录结果', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'result';
EXEC sp_addextendedproperty N'MS_Description', N'客户端 IP', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'client_ip';
EXEC sp_addextendedproperty N'MS_Description', N'浏览器或客户端 User-Agent', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'user_agent';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_login_log', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_login_log_tenant_created' AND object_id = OBJECT_ID(N'sys_login_log'))
CREATE INDEX [idx_sys_login_log_tenant_created] ON [sys_login_log] ([tenant_id], [created_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_login_log_tenant_user' AND object_id = OBJECT_ID(N'sys_login_log'))
CREATE INDEX [idx_sys_login_log_tenant_user] ON [sys_login_log] ([tenant_id], [user_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_login_log_tenant_result' AND object_id = OBJECT_ID(N'sys_login_log'))
CREATE INDEX [idx_sys_login_log_tenant_result] ON [sys_login_log] ([tenant_id], [result]);

IF OBJECT_ID(N'sys_exception_log', N'U') IS NULL
BEGIN
CREATE TABLE [sys_exception_log] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [trace_id] NVARCHAR(128) DEFAULT NULL,
    [user_id] BIGINT DEFAULT NULL,
    [username] NVARCHAR(64) DEFAULT NULL,
    [request_uri] NVARCHAR(255) NOT NULL,
    [request_method] NVARCHAR(16) NOT NULL,
    [exception_name] NVARCHAR(255) NOT NULL,
    [message] NVARCHAR(MAX),
    [stack_trace] NVARCHAR(MAX),
    [client_ip] NVARCHAR(64) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统异常日志表', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'链路追踪编号', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'trace_id';
EXEC sp_addextendedproperty N'MS_Description', N'操作用户主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'user_id';
EXEC sp_addextendedproperty N'MS_Description', N'操作账号', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'username';
EXEC sp_addextendedproperty N'MS_Description', N'请求地址', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'request_uri';
EXEC sp_addextendedproperty N'MS_Description', N'请求方法', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'request_method';
EXEC sp_addextendedproperty N'MS_Description', N'异常类名', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'exception_name';
EXEC sp_addextendedproperty N'MS_Description', N'异常消息', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'message';
EXEC sp_addextendedproperty N'MS_Description', N'异常堆栈', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'stack_trace';
EXEC sp_addextendedproperty N'MS_Description', N'客户端 IP', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'client_ip';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_exception_log', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_exception_log_tenant_created' AND object_id = OBJECT_ID(N'sys_exception_log'))
CREATE INDEX [idx_sys_exception_log_tenant_created] ON [sys_exception_log] ([tenant_id], [created_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_exception_log_tenant_trace' AND object_id = OBJECT_ID(N'sys_exception_log'))
CREATE INDEX [idx_sys_exception_log_tenant_trace] ON [sys_exception_log] ([tenant_id], [trace_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_exception_log_tenant_exception' AND object_id = OBJECT_ID(N'sys_exception_log'))
CREATE INDEX [idx_sys_exception_log_tenant_exception] ON [sys_exception_log] ([tenant_id], [exception_name]);

IF OBJECT_ID(N'sys_permission_audit', N'U') IS NULL
BEGIN
CREATE TABLE [sys_permission_audit] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [operator_id] BIGINT DEFAULT NULL,
    [target_type] NVARCHAR(64) NOT NULL,
    [target_id] NVARCHAR(128) NOT NULL,
    [before_value] NVARCHAR(MAX),
    [after_value] NVARCHAR(MAX),
    [change_type] NVARCHAR(64) NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统权限变更审计表', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'操作者用户主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'operator_id';
EXEC sp_addextendedproperty N'MS_Description', N'目标类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'target_type';
EXEC sp_addextendedproperty N'MS_Description', N'目标业务标识', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'target_id';
EXEC sp_addextendedproperty N'MS_Description', N'变更前内容', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'before_value';
EXEC sp_addextendedproperty N'MS_Description', N'变更后内容', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'after_value';
EXEC sp_addextendedproperty N'MS_Description', N'变更类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'change_type';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_permission_audit', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_permission_audit_tenant_created' AND object_id = OBJECT_ID(N'sys_permission_audit'))
CREATE INDEX [idx_sys_permission_audit_tenant_created] ON [sys_permission_audit] ([tenant_id], [created_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_permission_audit_tenant_operator' AND object_id = OBJECT_ID(N'sys_permission_audit'))
CREATE INDEX [idx_sys_permission_audit_tenant_operator] ON [sys_permission_audit] ([tenant_id], [operator_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_permission_audit_tenant_target' AND object_id = OBJECT_ID(N'sys_permission_audit'))
CREATE INDEX [idx_sys_permission_audit_tenant_target] ON [sys_permission_audit] ([tenant_id], [target_type], [target_id]);

IF OBJECT_ID(N'sys_audit_log', N'U') IS NULL
BEGIN
CREATE TABLE [sys_audit_log] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [user_id] BIGINT DEFAULT NULL,
    [username] NVARCHAR(64) DEFAULT NULL,
    [action] NVARCHAR(128) NOT NULL,
    [target_type] NVARCHAR(64) DEFAULT NULL,
    [target_id] NVARCHAR(128) DEFAULT NULL,
    [result] NVARCHAR(32) NOT NULL,
    [client_ip] NVARCHAR(64) DEFAULT NULL,
    [detail] NVARCHAR(MAX),
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统审计日志表', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'操作用户主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'user_id';
EXEC sp_addextendedproperty N'MS_Description', N'操作账号', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'username';
EXEC sp_addextendedproperty N'MS_Description', N'操作动作', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'action';
EXEC sp_addextendedproperty N'MS_Description', N'目标类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'target_type';
EXEC sp_addextendedproperty N'MS_Description', N'目标标识', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'target_id';
EXEC sp_addextendedproperty N'MS_Description', N'操作结果', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'result';
EXEC sp_addextendedproperty N'MS_Description', N'客户端 IP', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'client_ip';
EXEC sp_addextendedproperty N'MS_Description', N'操作详情', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'detail';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_audit_log', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_audit_log_tenant_created' AND object_id = OBJECT_ID(N'sys_audit_log'))
CREATE INDEX [idx_sys_audit_log_tenant_created] ON [sys_audit_log] ([tenant_id], [created_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_audit_log_tenant_user' AND object_id = OBJECT_ID(N'sys_audit_log'))
CREATE INDEX [idx_sys_audit_log_tenant_user] ON [sys_audit_log] ([tenant_id], [user_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_audit_log_tenant_action' AND object_id = OBJECT_ID(N'sys_audit_log'))
CREATE INDEX [idx_sys_audit_log_tenant_action] ON [sys_audit_log] ([tenant_id], [action]);

IF OBJECT_ID(N'sys_param', N'U') IS NULL
BEGIN
CREATE TABLE [sys_param] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [param_key] NVARCHAR(128) NOT NULL,
    [param_value] NVARCHAR(MAX),
    [value_type] NVARCHAR(32) NOT NULL DEFAULT 'string',
    [system_flag] BIT NOT NULL DEFAULT 0,
    [editable] BIT NOT NULL DEFAULT 1,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_param_tenant_key] UNIQUE ([tenant_id], [param_key])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统参数表', N'SCHEMA', N'dbo', N'TABLE', N'sys_param';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'参数键', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'param_key';
EXEC sp_addextendedproperty N'MS_Description', N'参数值', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'param_value';
EXEC sp_addextendedproperty N'MS_Description', N'参数值类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'value_type';
EXEC sp_addextendedproperty N'MS_Description', N'是否系统内置参数', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'system_flag';
EXEC sp_addextendedproperty N'MS_Description', N'是否允许后台编辑', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'editable';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_param', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_param_tenant_system' AND object_id = OBJECT_ID(N'sys_param'))
CREATE INDEX [idx_sys_param_tenant_system] ON [sys_param] ([tenant_id], [system_flag]);

IF OBJECT_ID(N'sys_secret', N'U') IS NULL
BEGIN
CREATE TABLE [sys_secret] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [secret_code] NVARCHAR(64) NOT NULL,
    [secret_name] NVARCHAR(128) NOT NULL,
    [secret_kind] NVARCHAR(64) NOT NULL,
    [secret_cipher] NVARCHAR(MAX) NOT NULL,
    [secret_mask] NVARCHAR(255) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [expire_at] DATETIME2 DEFAULT NULL,
    [last_rotated_at] DATETIME2 DEFAULT NULL,
    [created_by] BIGINT DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_by] BIGINT DEFAULT NULL,
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] BIT NOT NULL DEFAULT 0,
    [version] INT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) DEFAULT NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_secret_tenant_code] UNIQUE ([tenant_id], [secret_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统密钥表', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'密钥编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'secret_code';
EXEC sp_addextendedproperty N'MS_Description', N'密钥名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'secret_name';
EXEC sp_addextendedproperty N'MS_Description', N'密钥类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'secret_kind';
EXEC sp_addextendedproperty N'MS_Description', N'密钥密文', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'secret_cipher';
EXEC sp_addextendedproperty N'MS_Description', N'脱敏展示值', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'secret_mask';
EXEC sp_addextendedproperty N'MS_Description', N'密钥状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'过期时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'expire_at';
EXEC sp_addextendedproperty N'MS_Description', N'最近轮换时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'last_rotated_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建人主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'created_by';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新人主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'updated_by';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'是否删除', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'sys_secret', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_secret_tenant_status' AND object_id = OBJECT_ID(N'sys_secret'))
CREATE INDEX [idx_sys_secret_tenant_status] ON [sys_secret] ([tenant_id], [status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_secret_tenant_kind' AND object_id = OBJECT_ID(N'sys_secret'))
CREATE INDEX [idx_sys_secret_tenant_kind] ON [sys_secret] ([tenant_id], [secret_kind]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_secret_tenant_expire' AND object_id = OBJECT_ID(N'sys_secret'))
CREATE INDEX [idx_sys_secret_tenant_expire] ON [sys_secret] ([tenant_id], [expire_at]);

IF OBJECT_ID(N'sys_access_restriction', N'U') IS NULL
BEGIN
CREATE TABLE [sys_access_restriction] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [restriction_type] NVARCHAR(32) NOT NULL,
    [rule_value] NVARCHAR(255) NOT NULL,
    [effect] NVARCHAR(32) NOT NULL,
    [start_at] DATETIME2 DEFAULT NULL,
    [end_at] DATETIME2 DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_access_restriction_rule] UNIQUE ([tenant_id], [restriction_type], [rule_value])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统访问限制表', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'限制类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'restriction_type';
EXEC sp_addextendedproperty N'MS_Description', N'规则值', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'rule_value';
EXEC sp_addextendedproperty N'MS_Description', N'生效动作', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'effect';
EXEC sp_addextendedproperty N'MS_Description', N'生效开始时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'start_at';
EXEC sp_addextendedproperty N'MS_Description', N'生效结束时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'end_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_access_restriction', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_access_restriction_active' AND object_id = OBJECT_ID(N'sys_access_restriction'))
CREATE INDEX [idx_sys_access_restriction_active] ON [sys_access_restriction] ([tenant_id], [restriction_type], [start_at], [end_at]);

IF OBJECT_ID(N'sys_password_policy', N'U') IS NULL
BEGIN
CREATE TABLE [sys_password_policy] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [policy_code] NVARCHAR(64) NOT NULL,
    [policy_name] NVARCHAR(128) NOT NULL,
    [min_length] INT NOT NULL DEFAULT 8,
    [require_uppercase] BIT NOT NULL DEFAULT 0,
    [require_lowercase] BIT NOT NULL DEFAULT 1,
    [require_digit] BIT NOT NULL DEFAULT 1,
    [require_special] BIT NOT NULL DEFAULT 0,
    [expire_days] INT NOT NULL DEFAULT 90,
    [history_count] INT NOT NULL DEFAULT 3,
    [max_retry_count] INT NOT NULL DEFAULT 5,
    [lock_minutes] INT NOT NULL DEFAULT 30,
    [enabled] BIT NOT NULL DEFAULT 1,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_password_policy_tenant_code] UNIQUE ([tenant_id], [policy_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统密码策略表', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'策略编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'policy_code';
EXEC sp_addextendedproperty N'MS_Description', N'策略名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'policy_name';
EXEC sp_addextendedproperty N'MS_Description', N'密码最小长度', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'min_length';
EXEC sp_addextendedproperty N'MS_Description', N'是否要求大写字母', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'require_uppercase';
EXEC sp_addextendedproperty N'MS_Description', N'是否要求小写字母', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'require_lowercase';
EXEC sp_addextendedproperty N'MS_Description', N'是否要求数字', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'require_digit';
EXEC sp_addextendedproperty N'MS_Description', N'是否要求特殊字符', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'require_special';
EXEC sp_addextendedproperty N'MS_Description', N'密码有效天数', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'expire_days';
EXEC sp_addextendedproperty N'MS_Description', N'历史密码记忆次数', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'history_count';
EXEC sp_addextendedproperty N'MS_Description', N'最大连续失败次数', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'max_retry_count';
EXEC sp_addextendedproperty N'MS_Description', N'账号锁定分钟数', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'lock_minutes';
EXEC sp_addextendedproperty N'MS_Description', N'是否启用', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'enabled';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_password_policy', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_password_policy_tenant_enabled' AND object_id = OBJECT_ID(N'sys_password_policy'))
CREATE INDEX [idx_sys_password_policy_tenant_enabled] ON [sys_password_policy] ([tenant_id], [enabled]);

IF OBJECT_ID(N'sys_code_rule', N'U') IS NULL
BEGIN
CREATE TABLE [sys_code_rule] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [rule_code] NVARCHAR(64) NOT NULL,
    [rule_name] NVARCHAR(128) NOT NULL,
    [prefix] NVARCHAR(32) DEFAULT NULL,
    [date_pattern] NVARCHAR(32) DEFAULT NULL,
    [sequence_length] INT NOT NULL DEFAULT 5,
    [current_value] INT NOT NULL DEFAULT 0,
    [enabled] BIT NOT NULL DEFAULT 1,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_code_rule_tenant_code] UNIQUE ([tenant_id], [rule_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统编码规则表', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'编码规则编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'rule_code';
EXEC sp_addextendedproperty N'MS_Description', N'编码规则名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'rule_name';
EXEC sp_addextendedproperty N'MS_Description', N'编码前缀', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'prefix';
EXEC sp_addextendedproperty N'MS_Description', N'日期格式', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'date_pattern';
EXEC sp_addextendedproperty N'MS_Description', N'序列号长度', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'sequence_length';
EXEC sp_addextendedproperty N'MS_Description', N'当前序列值', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'current_value';
EXEC sp_addextendedproperty N'MS_Description', N'是否启用', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'enabled';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_code_rule', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_code_rule_tenant_enabled' AND object_id = OBJECT_ID(N'sys_code_rule'))
CREATE INDEX [idx_sys_code_rule_tenant_enabled] ON [sys_code_rule] ([tenant_id], [enabled]);

IF OBJECT_ID(N'sys_dict_type', N'U') IS NULL
BEGIN
CREATE TABLE [sys_dict_type] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [dict_code] NVARCHAR(64) NOT NULL,
    [dict_name] NVARCHAR(128) NOT NULL,
    [system_flag] BIT NOT NULL DEFAULT 0,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_dict_type_tenant_code] UNIQUE ([tenant_id], [dict_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统字典类型表', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'字典编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type', N'COLUMN', N'dict_code';
EXEC sp_addextendedproperty N'MS_Description', N'字典名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type', N'COLUMN', N'dict_name';
EXEC sp_addextendedproperty N'MS_Description', N'是否系统内置字典', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type', N'COLUMN', N'system_flag';
EXEC sp_addextendedproperty N'MS_Description', N'字典状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_type', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_dict_type_tenant_status' AND object_id = OBJECT_ID(N'sys_dict_type'))
CREATE INDEX [idx_sys_dict_type_tenant_status] ON [sys_dict_type] ([tenant_id], [status]);

IF OBJECT_ID(N'sys_dict_item', N'U') IS NULL
BEGIN
CREATE TABLE [sys_dict_item] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [dict_code] NVARCHAR(64) NOT NULL,
    [item_label] NVARCHAR(128) NOT NULL,
    [item_value] NVARCHAR(128) NOT NULL,
    [item_color] NVARCHAR(32) DEFAULT NULL,
    [sort_order] INT NOT NULL DEFAULT 0,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_dict_item_tenant_code_value] UNIQUE ([tenant_id], [dict_code], [item_value])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统字典项表', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'字典编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'dict_code';
EXEC sp_addextendedproperty N'MS_Description', N'字典项显示标签', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'item_label';
EXEC sp_addextendedproperty N'MS_Description', N'字典项实际值', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'item_value';
EXEC sp_addextendedproperty N'MS_Description', N'字典项前端展示颜色', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'item_color';
EXEC sp_addextendedproperty N'MS_Description', N'排序号', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'sort_order';
EXEC sp_addextendedproperty N'MS_Description', N'字典项状态', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_dict_item', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_dict_item_tenant_code_sort' AND object_id = OBJECT_ID(N'sys_dict_item'))
CREATE INDEX [idx_sys_dict_item_tenant_code_sort] ON [sys_dict_item] ([tenant_id], [dict_code], [sort_order]);

IF OBJECT_ID(N'sys_module', N'U') IS NULL
BEGIN
CREATE TABLE [sys_module] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [module_code] NVARCHAR(64) NOT NULL,
    [module_name] NVARCHAR(128) NOT NULL,
    [version] NVARCHAR(32) NOT NULL,
    [module_type] NVARCHAR(32) NOT NULL,
    [enabled] BIT NOT NULL DEFAULT 1,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_module_code] UNIQUE ([module_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统模块表', N'SCHEMA', N'dbo', N'TABLE', N'sys_module';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_module', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'模块编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_module', N'COLUMN', N'module_code';
EXEC sp_addextendedproperty N'MS_Description', N'模块名称', N'SCHEMA', N'dbo', N'TABLE', N'sys_module', N'COLUMN', N'module_name';
EXEC sp_addextendedproperty N'MS_Description', N'模块版本', N'SCHEMA', N'dbo', N'TABLE', N'sys_module', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'模块类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_module', N'COLUMN', N'module_type';
EXEC sp_addextendedproperty N'MS_Description', N'是否启用', N'SCHEMA', N'dbo', N'TABLE', N'sys_module', N'COLUMN', N'enabled';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_module', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_module', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_module_type_enabled' AND object_id = OBJECT_ID(N'sys_module'))
CREATE INDEX [idx_sys_module_type_enabled] ON [sys_module] ([module_type], [enabled]);

IF OBJECT_ID(N'sys_module_dependency', N'U') IS NULL
BEGIN
CREATE TABLE [sys_module_dependency] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [module_code] NVARCHAR(64) NOT NULL,
    [depends_on_code] NVARCHAR(64) NOT NULL,
    [required_version] NVARCHAR(32) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_module_dep] UNIQUE ([module_code], [depends_on_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统模块依赖表', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_dependency';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_dependency', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'模块编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_dependency', N'COLUMN', N'module_code';
EXEC sp_addextendedproperty N'MS_Description', N'依赖模块编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_dependency', N'COLUMN', N'depends_on_code';
EXEC sp_addextendedproperty N'MS_Description', N'依赖模块要求版本', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_dependency', N'COLUMN', N'required_version';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_dependency', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_module_dep_depends_on' AND object_id = OBJECT_ID(N'sys_module_dependency'))
CREATE INDEX [idx_sys_module_dep_depends_on] ON [sys_module_dependency] ([depends_on_code]);

IF OBJECT_ID(N'sys_module_resource', N'U') IS NULL
BEGIN
CREATE TABLE [sys_module_resource] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [module_code] NVARCHAR(64) NOT NULL,
    [resource_type] NVARCHAR(32) NOT NULL,
    [resource_code] NVARCHAR(128) NOT NULL,
    [resource_path] NVARCHAR(255) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_sys_module_resource] UNIQUE ([module_code], [resource_type], [resource_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'系统模块资源表', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_resource';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_resource', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'模块编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_resource', N'COLUMN', N'module_code';
EXEC sp_addextendedproperty N'MS_Description', N'资源类型', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_resource', N'COLUMN', N'resource_type';
EXEC sp_addextendedproperty N'MS_Description', N'资源编码', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_resource', N'COLUMN', N'resource_code';
EXEC sp_addextendedproperty N'MS_Description', N'资源路径或权限标识', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_resource', N'COLUMN', N'resource_path';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'sys_module_resource', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_sys_module_resource_type' AND object_id = OBJECT_ID(N'sys_module_resource'))
CREATE INDEX [idx_sys_module_resource_type] ON [sys_module_resource] ([resource_type], [resource_code]);

-- ============================================================
-- 3. 低代码元数据表
-- 来源：zhyc-base-server/zhyc-module-lowcode/src/main/resources/db/V1__lowcode_core.sql
-- ============================================================
IF OBJECT_ID(N'lowcode_data_source', N'U') IS NULL
BEGIN
CREATE TABLE [lowcode_data_source] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [code] NVARCHAR(64) NOT NULL,
    [name] NVARCHAR(128) NOT NULL,
    [dialect] NVARCHAR(32) NOT NULL,
    [jdbc_url] NVARCHAR(512) NOT NULL,
    [username] NVARCHAR(128) NOT NULL,
    [password_secret_ref] NVARCHAR(255) DEFAULT NULL,
    [enabled] BIT NOT NULL DEFAULT 1,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_lowcode_ds_tenant_code] UNIQUE ([tenant_id], [code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'低代码数据源表', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'数据源编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'code';
EXEC sp_addextendedproperty N'MS_Description', N'数据源名称', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'name';
EXEC sp_addextendedproperty N'MS_Description', N'数据库类型', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'dialect';
EXEC sp_addextendedproperty N'MS_Description', N'JDBC 连接地址', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'jdbc_url';
EXEC sp_addextendedproperty N'MS_Description', N'数据库用户名', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'username';
EXEC sp_addextendedproperty N'MS_Description', N'数据库口令密钥引用', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'password_secret_ref';
EXEC sp_addextendedproperty N'MS_Description', N'是否启用', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'enabled';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_data_source', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_ds_tenant_enabled' AND object_id = OBJECT_ID(N'lowcode_data_source'))
CREATE INDEX [idx_lowcode_ds_tenant_enabled] ON [lowcode_data_source] ([tenant_id], [enabled]);

IF OBJECT_ID(N'lowcode_table_model', N'U') IS NULL
BEGIN
CREATE TABLE [lowcode_table_model] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [data_source_id] BIGINT DEFAULT NULL,
    [code] NVARCHAR(64) NOT NULL,
    [name] NVARCHAR(128) NOT NULL,
    [table_name] NVARCHAR(128) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_lowcode_table_tenant_code] UNIQUE ([tenant_id], [code]),
    CONSTRAINT [uk_lowcode_table_tenant_table] UNIQUE ([tenant_id], [table_name]),
    CONSTRAINT [fk_lowcode_table_data_source] FOREIGN KEY ([data_source_id]) REFERENCES [lowcode_data_source] ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'低代码表模型表', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'数据源主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'data_source_id';
EXEC sp_addextendedproperty N'MS_Description', N'模型编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'code';
EXEC sp_addextendedproperty N'MS_Description', N'模型名称', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'name';
EXEC sp_addextendedproperty N'MS_Description', N'物理表名', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'table_name';
EXEC sp_addextendedproperty N'MS_Description', N'模型状态', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_model', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_table_tenant_status' AND object_id = OBJECT_ID(N'lowcode_table_model'))
CREATE INDEX [idx_lowcode_table_tenant_status] ON [lowcode_table_model] ([tenant_id], [status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_table_data_source' AND object_id = OBJECT_ID(N'lowcode_table_model'))
CREATE INDEX [idx_lowcode_table_data_source] ON [lowcode_table_model] ([data_source_id]);

IF OBJECT_ID(N'lowcode_column_model', N'U') IS NULL
BEGIN
CREATE TABLE [lowcode_column_model] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [table_model_id] BIGINT NOT NULL,
    [code] NVARCHAR(64) NOT NULL,
    [name] NVARCHAR(128) NOT NULL,
    [field_type] NVARCHAR(32) NOT NULL,
    [length_value] INT DEFAULT NULL,
    [scale_value] INT DEFAULT NULL,
    [required] BIT NOT NULL DEFAULT 0,
    [primary_key_flag] BIT NOT NULL DEFAULT 0,
    [auto_increment_flag] BIT NOT NULL DEFAULT 0,
    [list_visible] BIT NOT NULL DEFAULT 0,
    [form_visible] BIT NOT NULL DEFAULT 0,
    [queryable] BIT NOT NULL DEFAULT 0,
    [dict_code] NVARCHAR(64) DEFAULT NULL,
    [sort_order] INT NOT NULL DEFAULT 0,
    [comment] NVARCHAR(255) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_lowcode_column_table_code] UNIQUE ([table_model_id], [code]),
    CONSTRAINT [fk_lowcode_column_table] FOREIGN KEY ([table_model_id]) REFERENCES [lowcode_table_model] ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'低代码字段模型表', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'表模型主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'table_model_id';
EXEC sp_addextendedproperty N'MS_Description', N'字段编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'code';
EXEC sp_addextendedproperty N'MS_Description', N'字段名称', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'name';
EXEC sp_addextendedproperty N'MS_Description', N'平台统一字段类型', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'field_type';
EXEC sp_addextendedproperty N'MS_Description', N'字段长度或数值精度', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'length_value';
EXEC sp_addextendedproperty N'MS_Description', N'小数位数', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'scale_value';
EXEC sp_addextendedproperty N'MS_Description', N'是否必填', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'required';
EXEC sp_addextendedproperty N'MS_Description', N'是否主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'primary_key_flag';
EXEC sp_addextendedproperty N'MS_Description', N'是否自增', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'auto_increment_flag';
EXEC sp_addextendedproperty N'MS_Description', N'是否列表展示', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'list_visible';
EXEC sp_addextendedproperty N'MS_Description', N'是否表单展示', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'form_visible';
EXEC sp_addextendedproperty N'MS_Description', N'是否查询条件', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'queryable';
EXEC sp_addextendedproperty N'MS_Description', N'绑定的系统字典编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'dict_code';
EXEC sp_addextendedproperty N'MS_Description', N'排序号', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'sort_order';
EXEC sp_addextendedproperty N'MS_Description', N'字段备注', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'comment';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_column_model', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_column_table_sort' AND object_id = OBJECT_ID(N'lowcode_column_model'))
CREATE INDEX [idx_lowcode_column_table_sort] ON [lowcode_column_model] ([table_model_id], [sort_order]);

IF OBJECT_ID(N'lowcode_table_relation', N'U') IS NULL
BEGIN
CREATE TABLE [lowcode_table_relation] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [main_table_id] BIGINT NOT NULL,
    [sub_table_id] BIGINT NOT NULL,
    [relation_type] NVARCHAR(32) NOT NULL,
    [join_column] NVARCHAR(64) NOT NULL,
    [ref_column] NVARCHAR(64) NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_lowcode_relation_tenant_tables] UNIQUE ([tenant_id], [main_table_id], [sub_table_id], [relation_type]),
    CONSTRAINT [fk_lowcode_relation_main_table] FOREIGN KEY ([main_table_id]) REFERENCES [lowcode_table_model] ([id]),
    CONSTRAINT [fk_lowcode_relation_sub_table] FOREIGN KEY ([sub_table_id]) REFERENCES [lowcode_table_model] ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'低代码表关系模型表', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'主表模型主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'main_table_id';
EXEC sp_addextendedproperty N'MS_Description', N'子表模型主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'sub_table_id';
EXEC sp_addextendedproperty N'MS_Description', N'关系类型', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'relation_type';
EXEC sp_addextendedproperty N'MS_Description', N'主表关联字段编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'join_column';
EXEC sp_addextendedproperty N'MS_Description', N'子表引用字段编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'ref_column';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_table_relation', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_relation_tenant_main' AND object_id = OBJECT_ID(N'lowcode_table_relation'))
CREATE INDEX [idx_lowcode_relation_tenant_main] ON [lowcode_table_relation] ([tenant_id], [main_table_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_relation_tenant_sub' AND object_id = OBJECT_ID(N'lowcode_table_relation'))
CREATE INDEX [idx_lowcode_relation_tenant_sub] ON [lowcode_table_relation] ([tenant_id], [sub_table_id]);

IF OBJECT_ID(N'lowcode_page_model', N'U') IS NULL
BEGIN
CREATE TABLE [lowcode_page_model] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [table_model_id] BIGINT NOT NULL,
    [page_type] NVARCHAR(32) NOT NULL,
    [route_path] NVARCHAR(255) NOT NULL,
    [component_path] NVARCHAR(255) NOT NULL,
    [layout_type] NVARCHAR(64) NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_lowcode_page_tenant_table_type] UNIQUE ([tenant_id], [table_model_id], [page_type]),
    CONSTRAINT [fk_lowcode_page_table] FOREIGN KEY ([table_model_id]) REFERENCES [lowcode_table_model] ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'低代码页面模型表', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'表模型主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'table_model_id';
EXEC sp_addextendedproperty N'MS_Description', N'页面类型', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'page_type';
EXEC sp_addextendedproperty N'MS_Description', N'前端路由路径', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'route_path';
EXEC sp_addextendedproperty N'MS_Description', N'组件路径', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'component_path';
EXEC sp_addextendedproperty N'MS_Description', N'页面布局类型', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'layout_type';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_page_model', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_page_tenant_table' AND object_id = OBJECT_ID(N'lowcode_page_model'))
CREATE INDEX [idx_lowcode_page_tenant_table] ON [lowcode_page_model] ([tenant_id], [table_model_id]);

IF OBJECT_ID(N'lowcode_generation_record', N'U') IS NULL
BEGIN
CREATE TABLE [lowcode_generation_record] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [table_model_code] NVARCHAR(64) NOT NULL,
    [target] NVARCHAR(32) NOT NULL,
    [module_name] NVARCHAR(128) NOT NULL,
    [entity_name] NVARCHAR(128) NOT NULL,
    [overwrite_strategy] NVARCHAR(32) NOT NULL,
    [file_count] INT NOT NULL DEFAULT 0,
    [file_manifest_json] NVARCHAR(MAX) DEFAULT NULL,
    [status] NVARCHAR(32) NOT NULL,
    [error_message] NVARCHAR(1000) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'低代码代码生成记录表', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'表模型编码', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'table_model_code';
EXEC sp_addextendedproperty N'MS_Description', N'生成目标端', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'target';
EXEC sp_addextendedproperty N'MS_Description', N'业务模块名称', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'module_name';
EXEC sp_addextendedproperty N'MS_Description', N'业务实体名称', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'entity_name';
EXEC sp_addextendedproperty N'MS_Description', N'生成文件覆盖策略', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'overwrite_strategy';
EXEC sp_addextendedproperty N'MS_Description', N'生成文件数量', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'file_count';
EXEC sp_addextendedproperty N'MS_Description', N'生成文件清单 JSON', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'file_manifest_json';
EXEC sp_addextendedproperty N'MS_Description', N'生成状态', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'失败原因', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'error_message';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'lowcode_generation_record', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_gen_tenant_model' AND object_id = OBJECT_ID(N'lowcode_generation_record'))
CREATE INDEX [idx_lowcode_gen_tenant_model] ON [lowcode_generation_record] ([tenant_id], [table_model_code]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_gen_tenant_status' AND object_id = OBJECT_ID(N'lowcode_generation_record'))
CREATE INDEX [idx_lowcode_gen_tenant_status] ON [lowcode_generation_record] ([tenant_id], [status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lowcode_gen_target' AND object_id = OBJECT_ID(N'lowcode_generation_record'))
CREATE INDEX [idx_lowcode_gen_target] ON [lowcode_generation_record] ([target]);

IF OBJECT_ID(N'lc_generation_file', N'U') IS NULL
BEGIN
CREATE TABLE [lc_generation_file] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [record_id] BIGINT NOT NULL,
    [template_code] NVARCHAR(128) NOT NULL,
    [file_path] NVARCHAR(500) NOT NULL,
    [file_type] NVARCHAR(32) NOT NULL,
    [overwrite_mode] NVARCHAR(32) NOT NULL,
    [content_hash] NVARCHAR(128) NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [fk_lc_gen_file_record] FOREIGN KEY ([record_id]) REFERENCES [lowcode_generation_record] ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'低代码生成文件明细表', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'生成记录主键', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'record_id';
EXEC sp_addextendedproperty N'MS_Description', N'模板编码', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'template_code';
EXEC sp_addextendedproperty N'MS_Description', N'生成文件路径', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'file_path';
EXEC sp_addextendedproperty N'MS_Description', N'生成文件类型', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'file_type';
EXEC sp_addextendedproperty N'MS_Description', N'覆盖模式', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'overwrite_mode';
EXEC sp_addextendedproperty N'MS_Description', N'文件内容哈希', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'content_hash';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'lc_generation_file', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lc_gen_file_tenant_record' AND object_id = OBJECT_ID(N'lc_generation_file'))
CREATE INDEX [idx_lc_gen_file_tenant_record] ON [lc_generation_file] ([tenant_id], [record_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_lc_gen_file_hash' AND object_id = OBJECT_ID(N'lc_generation_file'))
CREATE INDEX [idx_lc_gen_file_hash] ON [lc_generation_file] ([content_hash]);

-- ============================================================
-- 4. 工作流运行表
-- 来源：zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql
-- ============================================================
IF OBJECT_ID(N'wf_category', N'U') IS NULL
BEGIN
CREATE TABLE [wf_category] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [category_code] NVARCHAR(64) NOT NULL,
    [category_name] NVARCHAR(128) NOT NULL,
    [sort_order] INT NOT NULL DEFAULT 0,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_wf_category_tenant_code] UNIQUE ([tenant_id], [category_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'工作流分类表', N'SCHEMA', N'dbo', N'TABLE', N'wf_category';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程分类编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'category_code';
EXEC sp_addextendedproperty N'MS_Description', N'流程分类名称', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'category_name';
EXEC sp_addextendedproperty N'MS_Description', N'排序号', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'sort_order';
EXEC sp_addextendedproperty N'MS_Description', N'分类状态', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'wf_category', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_category_tenant_status' AND object_id = OBJECT_ID(N'wf_category'))
CREATE INDEX [idx_wf_category_tenant_status] ON [wf_category] ([tenant_id], [status]);

IF OBJECT_ID(N'wf_process_model', N'U') IS NULL
BEGIN
CREATE TABLE [wf_process_model] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [model_code] NVARCHAR(128) NOT NULL,
    [model_name] NVARCHAR(128) NOT NULL,
    [category_id] BIGINT NULL,
    [flowable_model_id] NVARCHAR(128) NOT NULL,
    [bpmn_xml] MEDIUMTEXT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_wf_model_tenant_code] UNIQUE ([tenant_id], [model_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'工作流流程模型表', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程模型编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'model_code';
EXEC sp_addextendedproperty N'MS_Description', N'流程模型名称', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'model_name';
EXEC sp_addextendedproperty N'MS_Description', N'流程分类 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'category_id';
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 模型 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'flowable_model_id';
EXEC sp_addextendedproperty N'MS_Description', N'BPMN XML 设计稿，用于保存在线流程编排草稿', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'bpmn_xml';
EXEC sp_addextendedproperty N'MS_Description', N'流程模型状态', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_model', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_model_tenant_category' AND object_id = OBJECT_ID(N'wf_process_model'))
CREATE INDEX [idx_wf_model_tenant_category] ON [wf_process_model] ([tenant_id], [category_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_model_tenant_status' AND object_id = OBJECT_ID(N'wf_process_model'))
CREATE INDEX [idx_wf_model_tenant_status] ON [wf_process_model] ([tenant_id], [status]);

IF OBJECT_ID(N'wf_form_binding', N'U') IS NULL
BEGIN
CREATE TABLE [wf_form_binding] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [process_key] NVARCHAR(128) NOT NULL,
    [business_module] NVARCHAR(64) NOT NULL,
    [business_table] NVARCHAR(128) NOT NULL,
    [form_route] NVARCHAR(255) NOT NULL,
    [mobile_route] NVARCHAR(255) NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_wf_form_binding_tenant_process] UNIQUE ([tenant_id], [process_key])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'工作流表单绑定表', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程定义 key', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'process_key';
EXEC sp_addextendedproperty N'MS_Description', N'业务模块编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'business_module';
EXEC sp_addextendedproperty N'MS_Description', N'业务表名', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'business_table';
EXEC sp_addextendedproperty N'MS_Description', N'后台表单路由', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'form_route';
EXEC sp_addextendedproperty N'MS_Description', N'移动端表单路由', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'mobile_route';
EXEC sp_addextendedproperty N'MS_Description', N'绑定状态', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'wf_form_binding', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_form_binding_tenant_module' AND object_id = OBJECT_ID(N'wf_form_binding'))
CREATE INDEX [idx_wf_form_binding_tenant_module] ON [wf_form_binding] ([tenant_id], [business_module]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_form_binding_tenant_status' AND object_id = OBJECT_ID(N'wf_form_binding'))
CREATE INDEX [idx_wf_form_binding_tenant_status] ON [wf_form_binding] ([tenant_id], [status]);

IF OBJECT_ID(N'wf_process_definition', N'U') IS NULL
BEGIN
CREATE TABLE [wf_process_definition] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [process_key] NVARCHAR(128) NOT NULL,
    [process_name] NVARCHAR(128) NOT NULL,
    [version] INT NOT NULL,
    [deployment_id] NVARCHAR(128) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'active',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version_no] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_wf_definition_tenant_key_version] UNIQUE ([tenant_id], [process_key], [version])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'工作流流程定义表', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程定义 key', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'process_key';
EXEC sp_addextendedproperty N'MS_Description', N'流程定义名称', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'process_name';
EXEC sp_addextendedproperty N'MS_Description', N'流程定义版本号', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 部署 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'deployment_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程定义状态', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'version_no';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_definition', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_definition_tenant_key' AND object_id = OBJECT_ID(N'wf_process_definition'))
CREATE INDEX [idx_wf_definition_tenant_key] ON [wf_process_definition] ([tenant_id], [process_key]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_definition_tenant_status' AND object_id = OBJECT_ID(N'wf_process_definition'))
CREATE INDEX [idx_wf_definition_tenant_status] ON [wf_process_definition] ([tenant_id], [status]);

IF OBJECT_ID(N'wf_process_instance', N'U') IS NULL
BEGIN
CREATE TABLE [wf_process_instance] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [process_instance_id] NVARCHAR(128) NOT NULL,
    [process_key] NVARCHAR(128) NOT NULL,
    [business_key] NVARCHAR(128) NOT NULL,
    [starter_user_id] BIGINT NULL,
    [status] NVARCHAR(32) NOT NULL,
    [started_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [ended_at] DATETIME2 NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_wf_pi_tenant_instance] UNIQUE ([tenant_id], [process_instance_id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'工作流流程实例表', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程实例 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'process_instance_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程定义 key', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'process_key';
EXEC sp_addextendedproperty N'MS_Description', N'业务对象唯一标识', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'business_key';
EXEC sp_addextendedproperty N'MS_Description', N'流程发起人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'starter_user_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程实例状态', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'流程启动时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'started_at';
EXEC sp_addextendedproperty N'MS_Description', N'流程结束时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'ended_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'wf_process_instance', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_pi_tenant_business' AND object_id = OBJECT_ID(N'wf_process_instance'))
CREATE INDEX [idx_wf_pi_tenant_business] ON [wf_process_instance] ([tenant_id], [business_key]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_pi_tenant_status' AND object_id = OBJECT_ID(N'wf_process_instance'))
CREATE INDEX [idx_wf_pi_tenant_status] ON [wf_process_instance] ([tenant_id], [status]);

IF OBJECT_ID(N'wf_task', N'U') IS NULL
BEGIN
CREATE TABLE [wf_task] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [task_id] NVARCHAR(128) NOT NULL,
    [process_instance_id] NVARCHAR(128) NOT NULL,
    [task_name] NVARCHAR(128) NOT NULL,
    [business_key] NVARCHAR(128) NOT NULL,
    [assignee_user_id] BIGINT NOT NULL,
    [status] NVARCHAR(32) NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [completed_at] DATETIME2 NULL,
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_wf_task_tenant_task] UNIQUE ([tenant_id], [task_id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'工作流任务表', N'SCHEMA', N'dbo', N'TABLE', N'wf_task';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'任务 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'task_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程实例 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'process_instance_id';
EXEC sp_addextendedproperty N'MS_Description', N'任务名称', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'task_name';
EXEC sp_addextendedproperty N'MS_Description', N'业务对象唯一标识', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'business_key';
EXEC sp_addextendedproperty N'MS_Description', N'任务处理人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'assignee_user_id';
EXEC sp_addextendedproperty N'MS_Description', N'任务状态', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'任务创建时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'任务完成时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'completed_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'wf_task', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_task_tenant_assignee' AND object_id = OBJECT_ID(N'wf_task'))
CREATE INDEX [idx_wf_task_tenant_assignee] ON [wf_task] ([tenant_id], [assignee_user_id], [status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_task_tenant_instance' AND object_id = OBJECT_ID(N'wf_task'))
CREATE INDEX [idx_wf_task_tenant_instance] ON [wf_task] ([tenant_id], [process_instance_id]);

IF OBJECT_ID(N'wf_approval_record', N'U') IS NULL
BEGIN
CREATE TABLE [wf_approval_record] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [task_id] NVARCHAR(128) NOT NULL,
    [process_instance_id] NVARCHAR(128) NULL,
    [operator_user_id] BIGINT NOT NULL,
    [action] NVARCHAR(32) NOT NULL,
    [approval_comment] NVARCHAR(1000) NULL,
    [operated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'工作流审批记录表', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'任务 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'task_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程实例 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'process_instance_id';
EXEC sp_addextendedproperty N'MS_Description', N'操作用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'operator_user_id';
EXEC sp_addextendedproperty N'MS_Description', N'审批动作', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'action';
EXEC sp_addextendedproperty N'MS_Description', N'审批意见', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'approval_comment';
EXEC sp_addextendedproperty N'MS_Description', N'操作时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'operated_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_approval_record', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_record_tenant_task' AND object_id = OBJECT_ID(N'wf_approval_record'))
CREATE INDEX [idx_wf_record_tenant_task] ON [wf_approval_record] ([tenant_id], [task_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_record_tenant_instance' AND object_id = OBJECT_ID(N'wf_approval_record'))
CREATE INDEX [idx_wf_record_tenant_instance] ON [wf_approval_record] ([tenant_id], [process_instance_id]);

IF OBJECT_ID(N'wf_cc_record', N'U') IS NULL
BEGIN
CREATE TABLE [wf_cc_record] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [process_instance_id] NVARCHAR(128) NOT NULL,
    [receiver_id] BIGINT NOT NULL,
    [read_flag] SMALLINT NOT NULL DEFAULT 0,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'工作流抄送记录表', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'流程实例 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'process_instance_id';
EXEC sp_addextendedproperty N'MS_Description', N'抄送接收人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'receiver_id';
EXEC sp_addextendedproperty N'MS_Description', N'阅读标识，0 未读，1 已读', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'read_flag';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'wf_cc_record', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_cc_tenant_receiver' AND object_id = OBJECT_ID(N'wf_cc_record'))
CREATE INDEX [idx_wf_cc_tenant_receiver] ON [wf_cc_record] ([tenant_id], [receiver_id], [read_flag]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_wf_cc_tenant_instance' AND object_id = OBJECT_ID(N'wf_cc_record'))
CREATE INDEX [idx_wf_cc_tenant_instance] ON [wf_cc_record] ([tenant_id], [process_instance_id]);

-- ============================================================
-- 5. Flowable 引擎运行表
-- 来源：zhyc-base-server/zhyc-module-workflow/src/main/resources/db/V2__flowable_engine_mysql.sql
-- ============================================================
-- Flowable 引擎运行表结构。
-- 说明：由 CompareLocalDatabaseInit 从本地开发库 SHOW CREATE TABLE 导出，只包含 DDL，不包含流程实例、变量、任务或历史数据。
-- 说明：本脚本不包含 AI 模型供应商密钥、系统密钥、用户密码或 OAuth2 客户端密钥。

IF OBJECT_ID(N'act_evt_log', N'U') IS NULL
BEGIN
CREATE TABLE [act_evt_log] (
    [LOG_NR_] BIGINT IDENTITY(1,1) NOT NULL,
    [TYPE_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [TIME_STAMP_] DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
    [USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [DATA_] VARBINARY(MAX),
    [LOCK_OWNER_] NVARCHAR(255) DEFAULT NULL,
    [LOCK_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [IS_PROCESSED_] SMALLINT DEFAULT '0',
    PRIMARY KEY ([LOG_NR_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_evt_log', N'SCHEMA', N'dbo', N'TABLE', N'act_evt_log';

IF OBJECT_ID(N'act_re_deployment', N'U') IS NULL
BEGIN
CREATE TABLE [act_re_deployment] (
    [ID_] NVARCHAR(64) NOT NULL,
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [KEY_] NVARCHAR(255) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    [DEPLOY_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [DERIVED_FROM_] NVARCHAR(64) DEFAULT NULL,
    [DERIVED_FROM_ROOT_] NVARCHAR(64) DEFAULT NULL,
    [PARENT_DEPLOYMENT_ID_] NVARCHAR(255) DEFAULT NULL,
    [ENGINE_VERSION_] NVARCHAR(255) DEFAULT NULL,
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_re_deployment', N'SCHEMA', N'dbo', N'TABLE', N'act_re_deployment';

IF OBJECT_ID(N'act_ge_bytearray', N'U') IS NULL
BEGIN
CREATE TABLE [act_ge_bytearray] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [DEPLOYMENT_ID_] NVARCHAR(64) DEFAULT NULL,
    [BYTES_] VARBINARY(MAX),
    [GENERATED_] SMALLINT DEFAULT NULL,
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_BYTEARR_DEPL] FOREIGN KEY ([DEPLOYMENT_ID_]) REFERENCES [act_re_deployment] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ge_bytearray', N'SCHEMA', N'dbo', N'TABLE', N'act_ge_bytearray';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_BYTEAR_DEPL' AND object_id = OBJECT_ID(N'act_ge_bytearray'))
CREATE INDEX [ACT_IDX_BYTEAR_DEPL] ON [act_ge_bytearray] ([DEPLOYMENT_ID_]);

IF OBJECT_ID(N'act_ge_property', N'U') IS NULL
BEGIN
CREATE TABLE [act_ge_property] (
    [NAME_] NVARCHAR(64) NOT NULL,
    [VALUE_] NVARCHAR(300) DEFAULT NULL,
    [REV_] INT DEFAULT NULL,
    PRIMARY KEY ([NAME_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ge_property', N'SCHEMA', N'dbo', N'TABLE', N'act_ge_property';

IF OBJECT_ID(N'act_hi_actinst', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_actinst] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT '1',
    [PROC_DEF_ID_] NVARCHAR(64) NOT NULL,
    [PROC_INST_ID_] NVARCHAR(64) NOT NULL,
    [EXECUTION_ID_] NVARCHAR(64) NOT NULL,
    [ACT_ID_] NVARCHAR(255) NOT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [CALL_PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [ACT_NAME_] NVARCHAR(255) DEFAULT NULL,
    [ACT_TYPE_] NVARCHAR(255) NOT NULL,
    [ASSIGNEE_] NVARCHAR(255) DEFAULT NULL,
    [COMPLETED_BY_] NVARCHAR(255) DEFAULT NULL,
    [START_TIME_] DATETIME2(3) NOT NULL,
    [END_TIME_] DATETIME2(3) DEFAULT NULL,
    [TRANSACTION_ORDER_] INT DEFAULT NULL,
    [DURATION_] BIGINT DEFAULT NULL,
    [DELETE_REASON_] NVARCHAR(4000) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_actinst', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_actinst';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_ACT_INST_START' AND object_id = OBJECT_ID(N'act_hi_actinst'))
CREATE INDEX [ACT_IDX_HI_ACT_INST_START] ON [act_hi_actinst] ([START_TIME_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_ACT_INST_END' AND object_id = OBJECT_ID(N'act_hi_actinst'))
CREATE INDEX [ACT_IDX_HI_ACT_INST_END] ON [act_hi_actinst] ([END_TIME_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_ACT_INST_PROCINST' AND object_id = OBJECT_ID(N'act_hi_actinst'))
CREATE INDEX [ACT_IDX_HI_ACT_INST_PROCINST] ON [act_hi_actinst] ([PROC_INST_ID_], [ACT_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_ACT_INST_EXEC' AND object_id = OBJECT_ID(N'act_hi_actinst'))
CREATE INDEX [ACT_IDX_HI_ACT_INST_EXEC] ON [act_hi_actinst] ([EXECUTION_ID_], [ACT_ID_]);

IF OBJECT_ID(N'act_hi_attachment', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_attachment] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [DESCRIPTION_] NVARCHAR(4000) DEFAULT NULL,
    [TYPE_] NVARCHAR(255) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [URL_] NVARCHAR(4000) DEFAULT NULL,
    [CONTENT_ID_] NVARCHAR(64) DEFAULT NULL,
    [TIME_] DATETIME2(3) DEFAULT NULL,
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_attachment', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_attachment';

IF OBJECT_ID(N'act_hi_comment', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_comment] (
    [ID_] NVARCHAR(64) NOT NULL,
    [TYPE_] NVARCHAR(255) DEFAULT NULL,
    [TIME_] DATETIME2(3) NOT NULL,
    [USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [ACTION_] NVARCHAR(255) DEFAULT NULL,
    [MESSAGE_] NVARCHAR(4000) DEFAULT NULL,
    [FULL_MSG_] VARBINARY(MAX),
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_comment', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_comment';

IF OBJECT_ID(N'act_hi_detail', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_detail] (
    [ID_] NVARCHAR(64) NOT NULL,
    [TYPE_] NVARCHAR(255) NOT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [ACT_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [NAME_] NVARCHAR(255) NOT NULL,
    [VAR_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [REV_] INT DEFAULT NULL,
    [TIME_] DATETIME2(3) NOT NULL,
    [BYTEARRAY_ID_] NVARCHAR(64) DEFAULT NULL,
    [DOUBLE_] FLOAT DEFAULT NULL,
    [LONG_] BIGINT DEFAULT NULL,
    [TEXT_] NVARCHAR(4000) DEFAULT NULL,
    [TEXT2_] NVARCHAR(4000) DEFAULT NULL,
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_detail', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_detail';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_DETAIL_PROC_INST' AND object_id = OBJECT_ID(N'act_hi_detail'))
CREATE INDEX [ACT_IDX_HI_DETAIL_PROC_INST] ON [act_hi_detail] ([PROC_INST_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_DETAIL_ACT_INST' AND object_id = OBJECT_ID(N'act_hi_detail'))
CREATE INDEX [ACT_IDX_HI_DETAIL_ACT_INST] ON [act_hi_detail] ([ACT_INST_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_DETAIL_TIME' AND object_id = OBJECT_ID(N'act_hi_detail'))
CREATE INDEX [ACT_IDX_HI_DETAIL_TIME] ON [act_hi_detail] ([TIME_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_DETAIL_NAME' AND object_id = OBJECT_ID(N'act_hi_detail'))
CREATE INDEX [ACT_IDX_HI_DETAIL_NAME] ON [act_hi_detail] ([NAME_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_DETAIL_TASK_ID' AND object_id = OBJECT_ID(N'act_hi_detail'))
CREATE INDEX [ACT_IDX_HI_DETAIL_TASK_ID] ON [act_hi_detail] ([TASK_ID_]);

IF OBJECT_ID(N'act_hi_entitylink', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_entitylink] (
    [ID_] NVARCHAR(64) NOT NULL,
    [LINK_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [PARENT_ELEMENT_ID_] NVARCHAR(255) DEFAULT NULL,
    [REF_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [REF_SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [REF_SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [ROOT_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [ROOT_SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [HIERARCHY_TYPE_] NVARCHAR(255) DEFAULT NULL,
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_entitylink', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_entitylink';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_ENT_LNK_SCOPE' AND object_id = OBJECT_ID(N'act_hi_entitylink'))
CREATE INDEX [ACT_IDX_HI_ENT_LNK_SCOPE] ON [act_hi_entitylink] ([SCOPE_ID_], [SCOPE_TYPE_], [LINK_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_ENT_LNK_REF_SCOPE' AND object_id = OBJECT_ID(N'act_hi_entitylink'))
CREATE INDEX [ACT_IDX_HI_ENT_LNK_REF_SCOPE] ON [act_hi_entitylink] ([REF_SCOPE_ID_], [REF_SCOPE_TYPE_], [LINK_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_ENT_LNK_ROOT_SCOPE' AND object_id = OBJECT_ID(N'act_hi_entitylink'))
CREATE INDEX [ACT_IDX_HI_ENT_LNK_ROOT_SCOPE] ON [act_hi_entitylink] ([ROOT_SCOPE_ID_], [ROOT_SCOPE_TYPE_], [LINK_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_ENT_LNK_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_hi_entitylink'))
CREATE INDEX [ACT_IDX_HI_ENT_LNK_SCOPE_DEF] ON [act_hi_entitylink] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_], [LINK_TYPE_]);

IF OBJECT_ID(N'act_hi_identitylink', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_identitylink] (
    [ID_] NVARCHAR(64) NOT NULL,
    [GROUP_ID_] NVARCHAR(255) DEFAULT NULL,
    [TYPE_] NVARCHAR(255) DEFAULT NULL,
    [USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_identitylink', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_identitylink';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_IDENT_LNK_USER' AND object_id = OBJECT_ID(N'act_hi_identitylink'))
CREATE INDEX [ACT_IDX_HI_IDENT_LNK_USER] ON [act_hi_identitylink] ([USER_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_IDENT_LNK_SCOPE' AND object_id = OBJECT_ID(N'act_hi_identitylink'))
CREATE INDEX [ACT_IDX_HI_IDENT_LNK_SCOPE] ON [act_hi_identitylink] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_IDENT_LNK_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_hi_identitylink'))
CREATE INDEX [ACT_IDX_HI_IDENT_LNK_SUB_SCOPE] ON [act_hi_identitylink] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_IDENT_LNK_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_hi_identitylink'))
CREATE INDEX [ACT_IDX_HI_IDENT_LNK_SCOPE_DEF] ON [act_hi_identitylink] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_IDENT_LNK_TASK' AND object_id = OBJECT_ID(N'act_hi_identitylink'))
CREATE INDEX [ACT_IDX_HI_IDENT_LNK_TASK] ON [act_hi_identitylink] ([TASK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_IDENT_LNK_PROCINST' AND object_id = OBJECT_ID(N'act_hi_identitylink'))
CREATE INDEX [ACT_IDX_HI_IDENT_LNK_PROCINST] ON [act_hi_identitylink] ([PROC_INST_ID_]);

IF OBJECT_ID(N'act_hi_procinst', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_procinst] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT '1',
    [PROC_INST_ID_] NVARCHAR(64) NOT NULL,
    [BUSINESS_KEY_] NVARCHAR(255) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) NOT NULL,
    [START_TIME_] DATETIME2(3) NOT NULL,
    [END_TIME_] DATETIME2(3) DEFAULT NULL,
    [DURATION_] BIGINT DEFAULT NULL,
    [START_USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [START_ACT_ID_] NVARCHAR(255) DEFAULT NULL,
    [END_ACT_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUPER_PROCESS_INSTANCE_ID_] NVARCHAR(64) DEFAULT NULL,
    [DELETE_REASON_] NVARCHAR(4000) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [CALLBACK_ID_] NVARCHAR(255) DEFAULT NULL,
    [CALLBACK_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [REFERENCE_ID_] NVARCHAR(255) DEFAULT NULL,
    [REFERENCE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [PROPAGATED_STAGE_INST_ID_] NVARCHAR(255) DEFAULT NULL,
    [BUSINESS_STATUS_] NVARCHAR(255) DEFAULT NULL,
    [END_USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [STATE_] NVARCHAR(255) DEFAULT NULL,
    PRIMARY KEY ([ID_]),
    CONSTRAINT [PROC_INST_ID_] UNIQUE ([PROC_INST_ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_procinst', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_procinst';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_PRO_INST_END' AND object_id = OBJECT_ID(N'act_hi_procinst'))
CREATE INDEX [ACT_IDX_HI_PRO_INST_END] ON [act_hi_procinst] ([END_TIME_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_PRO_I_BUSKEY' AND object_id = OBJECT_ID(N'act_hi_procinst'))
CREATE INDEX [ACT_IDX_HI_PRO_I_BUSKEY] ON [act_hi_procinst] ([BUSINESS_KEY_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_PRO_SUPER_PROCINST' AND object_id = OBJECT_ID(N'act_hi_procinst'))
CREATE INDEX [ACT_IDX_HI_PRO_SUPER_PROCINST] ON [act_hi_procinst] ([SUPER_PROCESS_INSTANCE_ID_]);

IF OBJECT_ID(N'act_hi_taskinst', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_taskinst] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT '1',
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [TASK_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [TASK_DEF_KEY_] NVARCHAR(255) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [PROPAGATED_STAGE_INST_ID_] NVARCHAR(255) DEFAULT NULL,
    [STATE_] NVARCHAR(255) DEFAULT NULL,
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [PARENT_TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [DESCRIPTION_] NVARCHAR(4000) DEFAULT NULL,
    [OWNER_] NVARCHAR(255) DEFAULT NULL,
    [ASSIGNEE_] NVARCHAR(255) DEFAULT NULL,
    [START_TIME_] DATETIME2(3) NOT NULL,
    [IN_PROGRESS_TIME_] DATETIME2(3) DEFAULT NULL,
    [IN_PROGRESS_STARTED_BY_] NVARCHAR(255) DEFAULT NULL,
    [CLAIM_TIME_] DATETIME2(3) DEFAULT NULL,
    [CLAIMED_BY_] NVARCHAR(255) DEFAULT NULL,
    [SUSPENDED_TIME_] DATETIME2(3) DEFAULT NULL,
    [SUSPENDED_BY_] NVARCHAR(255) DEFAULT NULL,
    [END_TIME_] DATETIME2(3) DEFAULT NULL,
    [COMPLETED_BY_] NVARCHAR(255) DEFAULT NULL,
    [DURATION_] BIGINT DEFAULT NULL,
    [DELETE_REASON_] NVARCHAR(4000) DEFAULT NULL,
    [PRIORITY_] INT DEFAULT NULL,
    [IN_PROGRESS_DUE_DATE_] DATETIME2(3) DEFAULT NULL,
    [DUE_DATE_] DATETIME2(3) DEFAULT NULL,
    [FORM_KEY_] NVARCHAR(255) DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    [LAST_UPDATED_TIME_] DATETIME2(3) DEFAULT NULL,
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_taskinst', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_taskinst';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_TASK_SCOPE' AND object_id = OBJECT_ID(N'act_hi_taskinst'))
CREATE INDEX [ACT_IDX_HI_TASK_SCOPE] ON [act_hi_taskinst] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_TASK_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_hi_taskinst'))
CREATE INDEX [ACT_IDX_HI_TASK_SUB_SCOPE] ON [act_hi_taskinst] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_TASK_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_hi_taskinst'))
CREATE INDEX [ACT_IDX_HI_TASK_SCOPE_DEF] ON [act_hi_taskinst] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_TASK_INST_PROCINST' AND object_id = OBJECT_ID(N'act_hi_taskinst'))
CREATE INDEX [ACT_IDX_HI_TASK_INST_PROCINST] ON [act_hi_taskinst] ([PROC_INST_ID_]);

IF OBJECT_ID(N'act_hi_tsk_log', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_tsk_log] (
    [ID_] BIGINT IDENTITY(1,1) NOT NULL,
    [TYPE_] NVARCHAR(64) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) NOT NULL,
    [TIME_STAMP_] DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
    [USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [DATA_] NVARCHAR(4000) DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_tsk_log', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_tsk_log';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_ACT_HI_TSK_LOG_TASK' AND object_id = OBJECT_ID(N'act_hi_tsk_log'))
CREATE INDEX [ACT_IDX_ACT_HI_TSK_LOG_TASK] ON [act_hi_tsk_log] ([TASK_ID_]);

IF OBJECT_ID(N'act_hi_varinst', N'U') IS NULL
BEGIN
CREATE TABLE [act_hi_varinst] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT '1',
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [NAME_] NVARCHAR(255) NOT NULL,
    [VAR_TYPE_] NVARCHAR(100) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [BYTEARRAY_ID_] NVARCHAR(64) DEFAULT NULL,
    [DOUBLE_] FLOAT DEFAULT NULL,
    [LONG_] BIGINT DEFAULT NULL,
    [TEXT_] NVARCHAR(4000) DEFAULT NULL,
    [TEXT2_] NVARCHAR(4000) DEFAULT NULL,
    [META_INFO_] NVARCHAR(4000) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) DEFAULT NULL,
    [LAST_UPDATED_TIME_] DATETIME2(3) DEFAULT NULL,
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_hi_varinst', N'SCHEMA', N'dbo', N'TABLE', N'act_hi_varinst';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_PROCVAR_NAME_TYPE' AND object_id = OBJECT_ID(N'act_hi_varinst'))
CREATE INDEX [ACT_IDX_HI_PROCVAR_NAME_TYPE] ON [act_hi_varinst] ([NAME_], [VAR_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_VAR_SCOPE_ID_TYPE' AND object_id = OBJECT_ID(N'act_hi_varinst'))
CREATE INDEX [ACT_IDX_HI_VAR_SCOPE_ID_TYPE] ON [act_hi_varinst] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_VAR_SUB_ID_TYPE' AND object_id = OBJECT_ID(N'act_hi_varinst'))
CREATE INDEX [ACT_IDX_HI_VAR_SUB_ID_TYPE] ON [act_hi_varinst] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_PROCVAR_PROC_INST' AND object_id = OBJECT_ID(N'act_hi_varinst'))
CREATE INDEX [ACT_IDX_HI_PROCVAR_PROC_INST] ON [act_hi_varinst] ([PROC_INST_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_PROCVAR_TASK_ID' AND object_id = OBJECT_ID(N'act_hi_varinst'))
CREATE INDEX [ACT_IDX_HI_PROCVAR_TASK_ID] ON [act_hi_varinst] ([TASK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_HI_PROCVAR_EXE' AND object_id = OBJECT_ID(N'act_hi_varinst'))
CREATE INDEX [ACT_IDX_HI_PROCVAR_EXE] ON [act_hi_varinst] ([EXECUTION_ID_]);

IF OBJECT_ID(N'act_re_procdef', N'U') IS NULL
BEGIN
CREATE TABLE [act_re_procdef] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [KEY_] NVARCHAR(255) NOT NULL,
    [VERSION_] INT NOT NULL,
    [DEPLOYMENT_ID_] NVARCHAR(64) DEFAULT NULL,
    [RESOURCE_NAME_] NVARCHAR(4000) DEFAULT NULL,
    [DGRM_RESOURCE_NAME_] NVARCHAR(4000) DEFAULT NULL,
    [DESCRIPTION_] NVARCHAR(4000) DEFAULT NULL,
    [HAS_START_FORM_KEY_] SMALLINT DEFAULT NULL,
    [HAS_GRAPHICAL_NOTATION_] SMALLINT DEFAULT NULL,
    [SUSPENSION_STATE_] INT DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    [ENGINE_VERSION_] NVARCHAR(255) DEFAULT NULL,
    [DERIVED_FROM_] NVARCHAR(64) DEFAULT NULL,
    [DERIVED_FROM_ROOT_] NVARCHAR(64) DEFAULT NULL,
    [DERIVED_VERSION_] INT NOT NULL DEFAULT '0',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_UNIQ_PROCDEF] UNIQUE ([KEY_], [VERSION_], [DERIVED_VERSION_], [TENANT_ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_re_procdef', N'SCHEMA', N'dbo', N'TABLE', N'act_re_procdef';

IF OBJECT_ID(N'act_procdef_info', N'U') IS NULL
BEGIN
CREATE TABLE [act_procdef_info] (
    [ID_] NVARCHAR(64) NOT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [INFO_JSON_ID_] NVARCHAR(64) DEFAULT NULL,
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_UNIQ_INFO_PROCDEF] UNIQUE ([PROC_DEF_ID_]),
    CONSTRAINT [ACT_FK_INFO_JSON_BA] FOREIGN KEY ([INFO_JSON_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_INFO_PROCDEF] FOREIGN KEY ([PROC_DEF_ID_]) REFERENCES [act_re_procdef] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_procdef_info', N'SCHEMA', N'dbo', N'TABLE', N'act_procdef_info';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_INFO_PROCDEF' AND object_id = OBJECT_ID(N'act_procdef_info'))
CREATE INDEX [ACT_IDX_INFO_PROCDEF] ON [act_procdef_info] ([PROC_DEF_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_INFO_JSON_BA' AND object_id = OBJECT_ID(N'act_procdef_info'))
CREATE INDEX [ACT_FK_INFO_JSON_BA] ON [act_procdef_info] ([INFO_JSON_ID_]);

IF OBJECT_ID(N'act_re_model', N'U') IS NULL
BEGIN
CREATE TABLE [act_re_model] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [KEY_] NVARCHAR(255) DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [LAST_UPDATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [VERSION_] INT DEFAULT NULL,
    [META_INFO_] NVARCHAR(4000) DEFAULT NULL,
    [DEPLOYMENT_ID_] NVARCHAR(64) DEFAULT NULL,
    [EDITOR_SOURCE_VALUE_ID_] NVARCHAR(64) DEFAULT NULL,
    [EDITOR_SOURCE_EXTRA_VALUE_ID_] NVARCHAR(64) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_MODEL_DEPLOYMENT] FOREIGN KEY ([DEPLOYMENT_ID_]) REFERENCES [act_re_deployment] ([ID_]),
    CONSTRAINT [ACT_FK_MODEL_SOURCE] FOREIGN KEY ([EDITOR_SOURCE_VALUE_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_MODEL_SOURCE_EXTRA] FOREIGN KEY ([EDITOR_SOURCE_EXTRA_VALUE_ID_]) REFERENCES [act_ge_bytearray] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_re_model', N'SCHEMA', N'dbo', N'TABLE', N'act_re_model';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_MODEL_SOURCE' AND object_id = OBJECT_ID(N'act_re_model'))
CREATE INDEX [ACT_FK_MODEL_SOURCE] ON [act_re_model] ([EDITOR_SOURCE_VALUE_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_MODEL_SOURCE_EXTRA' AND object_id = OBJECT_ID(N'act_re_model'))
CREATE INDEX [ACT_FK_MODEL_SOURCE_EXTRA] ON [act_re_model] ([EDITOR_SOURCE_EXTRA_VALUE_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_MODEL_DEPLOYMENT' AND object_id = OBJECT_ID(N'act_re_model'))
CREATE INDEX [ACT_FK_MODEL_DEPLOYMENT] ON [act_re_model] ([DEPLOYMENT_ID_]);

IF OBJECT_ID(N'act_ru_actinst', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_actinst] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT '1',
    [PROC_DEF_ID_] NVARCHAR(64) NOT NULL,
    [PROC_INST_ID_] NVARCHAR(64) NOT NULL,
    [EXECUTION_ID_] NVARCHAR(64) NOT NULL,
    [ACT_ID_] NVARCHAR(255) NOT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [CALL_PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [ACT_NAME_] NVARCHAR(255) DEFAULT NULL,
    [ACT_TYPE_] NVARCHAR(255) NOT NULL,
    [ASSIGNEE_] NVARCHAR(255) DEFAULT NULL,
    [COMPLETED_BY_] NVARCHAR(255) DEFAULT NULL,
    [START_TIME_] DATETIME2(3) NOT NULL,
    [END_TIME_] DATETIME2(3) DEFAULT NULL,
    [DURATION_] BIGINT DEFAULT NULL,
    [TRANSACTION_ORDER_] INT DEFAULT NULL,
    [DELETE_REASON_] NVARCHAR(4000) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_actinst', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_actinst';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_ACTI_START' AND object_id = OBJECT_ID(N'act_ru_actinst'))
CREATE INDEX [ACT_IDX_RU_ACTI_START] ON [act_ru_actinst] ([START_TIME_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_ACTI_END' AND object_id = OBJECT_ID(N'act_ru_actinst'))
CREATE INDEX [ACT_IDX_RU_ACTI_END] ON [act_ru_actinst] ([END_TIME_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_ACTI_PROC' AND object_id = OBJECT_ID(N'act_ru_actinst'))
CREATE INDEX [ACT_IDX_RU_ACTI_PROC] ON [act_ru_actinst] ([PROC_INST_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_ACTI_PROC_ACT' AND object_id = OBJECT_ID(N'act_ru_actinst'))
CREATE INDEX [ACT_IDX_RU_ACTI_PROC_ACT] ON [act_ru_actinst] ([PROC_INST_ID_], [ACT_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_ACTI_EXEC' AND object_id = OBJECT_ID(N'act_ru_actinst'))
CREATE INDEX [ACT_IDX_RU_ACTI_EXEC] ON [act_ru_actinst] ([EXECUTION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_ACTI_EXEC_ACT' AND object_id = OBJECT_ID(N'act_ru_actinst'))
CREATE INDEX [ACT_IDX_RU_ACTI_EXEC_ACT] ON [act_ru_actinst] ([EXECUTION_ID_], [ACT_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_ACTI_TASK' AND object_id = OBJECT_ID(N'act_ru_actinst'))
CREATE INDEX [ACT_IDX_RU_ACTI_TASK] ON [act_ru_actinst] ([TASK_ID_]);

IF OBJECT_ID(N'act_ru_execution', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_execution] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [BUSINESS_KEY_] NVARCHAR(255) DEFAULT NULL,
    [PARENT_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [SUPER_EXEC_] NVARCHAR(64) DEFAULT NULL,
    [ROOT_PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [ACT_ID_] NVARCHAR(255) DEFAULT NULL,
    [IS_ACTIVE_] SMALLINT DEFAULT NULL,
    [IS_CONCURRENT_] SMALLINT DEFAULT NULL,
    [IS_SCOPE_] SMALLINT DEFAULT NULL,
    [IS_EVENT_SCOPE_] SMALLINT DEFAULT NULL,
    [IS_MI_ROOT_] SMALLINT DEFAULT NULL,
    [SUSPENSION_STATE_] INT DEFAULT NULL,
    [CACHED_ENT_STATE_] INT DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [START_ACT_ID_] NVARCHAR(255) DEFAULT NULL,
    [START_TIME_] DATETIME2(3) DEFAULT NULL,
    [START_USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [LOCK_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [LOCK_OWNER_] NVARCHAR(255) DEFAULT NULL,
    [IS_COUNT_ENABLED_] SMALLINT DEFAULT NULL,
    [EVT_SUBSCR_COUNT_] INT DEFAULT NULL,
    [TASK_COUNT_] INT DEFAULT NULL,
    [JOB_COUNT_] INT DEFAULT NULL,
    [TIMER_JOB_COUNT_] INT DEFAULT NULL,
    [SUSP_JOB_COUNT_] INT DEFAULT NULL,
    [DEADLETTER_JOB_COUNT_] INT DEFAULT NULL,
    [EXTERNAL_WORKER_JOB_COUNT_] INT DEFAULT NULL,
    [VAR_COUNT_] INT DEFAULT NULL,
    [ID_LINK_COUNT_] INT DEFAULT NULL,
    [CALLBACK_ID_] NVARCHAR(255) DEFAULT NULL,
    [CALLBACK_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [REFERENCE_ID_] NVARCHAR(255) DEFAULT NULL,
    [REFERENCE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [PROPAGATED_STAGE_INST_ID_] NVARCHAR(255) DEFAULT NULL,
    [BUSINESS_STATUS_] NVARCHAR(255) DEFAULT NULL,
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_EXE_PARENT] FOREIGN KEY ([PARENT_ID_]) REFERENCES [act_ru_execution] ([ID_]) ON DELETE CASCADE,
    CONSTRAINT [ACT_FK_EXE_PROCDEF] FOREIGN KEY ([PROC_DEF_ID_]) REFERENCES [act_re_procdef] ([ID_]),
    CONSTRAINT [ACT_FK_EXE_PROCINST] FOREIGN KEY ([PROC_INST_ID_]) REFERENCES [act_ru_execution] ([ID_]) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT [ACT_FK_EXE_SUPER] FOREIGN KEY ([SUPER_EXEC_]) REFERENCES [act_ru_execution] ([ID_]) ON DELETE CASCADE
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_execution', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_execution';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EXEC_BUSKEY' AND object_id = OBJECT_ID(N'act_ru_execution'))
CREATE INDEX [ACT_IDX_EXEC_BUSKEY] ON [act_ru_execution] ([BUSINESS_KEY_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDC_EXEC_ROOT' AND object_id = OBJECT_ID(N'act_ru_execution'))
CREATE INDEX [ACT_IDC_EXEC_ROOT] ON [act_ru_execution] ([ROOT_PROC_INST_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EXEC_REF_ID_' AND object_id = OBJECT_ID(N'act_ru_execution'))
CREATE INDEX [ACT_IDX_EXEC_REF_ID_] ON [act_ru_execution] ([REFERENCE_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_EXE_PROCINST' AND object_id = OBJECT_ID(N'act_ru_execution'))
CREATE INDEX [ACT_FK_EXE_PROCINST] ON [act_ru_execution] ([PROC_INST_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_EXE_PARENT' AND object_id = OBJECT_ID(N'act_ru_execution'))
CREATE INDEX [ACT_FK_EXE_PARENT] ON [act_ru_execution] ([PARENT_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_EXE_SUPER' AND object_id = OBJECT_ID(N'act_ru_execution'))
CREATE INDEX [ACT_FK_EXE_SUPER] ON [act_ru_execution] ([SUPER_EXEC_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_EXE_PROCDEF' AND object_id = OBJECT_ID(N'act_ru_execution'))
CREATE INDEX [ACT_FK_EXE_PROCDEF] ON [act_ru_execution] ([PROC_DEF_ID_]);

IF OBJECT_ID(N'act_ru_deadletter_job', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_deadletter_job] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [TYPE_] NVARCHAR(255) NOT NULL,
    [EXCLUSIVE_] BIT DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROCESS_INSTANCE_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [ELEMENT_ID_] NVARCHAR(255) DEFAULT NULL,
    [ELEMENT_NAME_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [CORRELATION_ID_] NVARCHAR(255) DEFAULT NULL,
    [EXCEPTION_STACK_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXCEPTION_MSG_] NVARCHAR(4000) DEFAULT NULL,
    [DUEDATE_] DATETIME2(3) NULL DEFAULT NULL,
    [REPEAT_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_CFG_] NVARCHAR(4000) DEFAULT NULL,
    [CUSTOM_VALUES_ID_] NVARCHAR(64) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_DEADLETTER_JOB_CUSTOM_VALUES] FOREIGN KEY ([CUSTOM_VALUES_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_DEADLETTER_JOB_EXCEPTION] FOREIGN KEY ([EXCEPTION_STACK_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_DEADLETTER_JOB_EXECUTION] FOREIGN KEY ([EXECUTION_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE] FOREIGN KEY ([PROCESS_INSTANCE_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_DEADLETTER_JOB_PROC_DEF] FOREIGN KEY ([PROC_DEF_ID_]) REFERENCES [act_re_procdef] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_deadletter_job', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_deadletter_job';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_DEADLETTER_JOB_EXCEPTION_STACK_ID' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_IDX_DEADLETTER_JOB_EXCEPTION_STACK_ID] ON [act_ru_deadletter_job] ([EXCEPTION_STACK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_DEADLETTER_JOB_CUSTOM_VALUES_ID' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_IDX_DEADLETTER_JOB_CUSTOM_VALUES_ID] ON [act_ru_deadletter_job] ([CUSTOM_VALUES_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_DEADLETTER_JOB_CORRELATION_ID' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_IDX_DEADLETTER_JOB_CORRELATION_ID] ON [act_ru_deadletter_job] ([CORRELATION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_DJOB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_IDX_DJOB_SCOPE] ON [act_ru_deadletter_job] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_DJOB_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_IDX_DJOB_SUB_SCOPE] ON [act_ru_deadletter_job] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_DJOB_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_IDX_DJOB_SCOPE_DEF] ON [act_ru_deadletter_job] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_DEADLETTER_JOB_EXECUTION' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_FK_DEADLETTER_JOB_EXECUTION] ON [act_ru_deadletter_job] ([EXECUTION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE] ON [act_ru_deadletter_job] ([PROCESS_INSTANCE_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_DEADLETTER_JOB_PROC_DEF' AND object_id = OBJECT_ID(N'act_ru_deadletter_job'))
CREATE INDEX [ACT_FK_DEADLETTER_JOB_PROC_DEF] ON [act_ru_deadletter_job] ([PROC_DEF_ID_]);

IF OBJECT_ID(N'act_ru_entitylink', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_entitylink] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) DEFAULT NULL,
    [LINK_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [PARENT_ELEMENT_ID_] NVARCHAR(255) DEFAULT NULL,
    [REF_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [REF_SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [REF_SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [ROOT_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [ROOT_SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [HIERARCHY_TYPE_] NVARCHAR(255) DEFAULT NULL,
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_entitylink', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_entitylink';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_ENT_LNK_SCOPE' AND object_id = OBJECT_ID(N'act_ru_entitylink'))
CREATE INDEX [ACT_IDX_ENT_LNK_SCOPE] ON [act_ru_entitylink] ([SCOPE_ID_], [SCOPE_TYPE_], [LINK_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_ENT_LNK_REF_SCOPE' AND object_id = OBJECT_ID(N'act_ru_entitylink'))
CREATE INDEX [ACT_IDX_ENT_LNK_REF_SCOPE] ON [act_ru_entitylink] ([REF_SCOPE_ID_], [REF_SCOPE_TYPE_], [LINK_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_ENT_LNK_ROOT_SCOPE' AND object_id = OBJECT_ID(N'act_ru_entitylink'))
CREATE INDEX [ACT_IDX_ENT_LNK_ROOT_SCOPE] ON [act_ru_entitylink] ([ROOT_SCOPE_ID_], [ROOT_SCOPE_TYPE_], [LINK_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_ENT_LNK_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_ru_entitylink'))
CREATE INDEX [ACT_IDX_ENT_LNK_SCOPE_DEF] ON [act_ru_entitylink] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_], [LINK_TYPE_]);

IF OBJECT_ID(N'act_ru_event_subscr', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_event_subscr] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [EVENT_TYPE_] NVARCHAR(255) NOT NULL,
    [EVENT_NAME_] NVARCHAR(255) DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [ACTIVITY_ID_] NVARCHAR(64) DEFAULT NULL,
    [CONFIGURATION_] NVARCHAR(255) DEFAULT NULL,
    [CREATED_] DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_DEFINITION_KEY_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(64) DEFAULT NULL,
    [LOCK_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [LOCK_OWNER_] NVARCHAR(255) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_EVENT_EXEC] FOREIGN KEY ([EXECUTION_ID_]) REFERENCES [act_ru_execution] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_event_subscr', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_event_subscr';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EVENT_SUBSCR_CONFIG_' AND object_id = OBJECT_ID(N'act_ru_event_subscr'))
CREATE INDEX [ACT_IDX_EVENT_SUBSCR_CONFIG_] ON [act_ru_event_subscr] ([CONFIGURATION_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EVENT_SUBSCR_EXEC_ID' AND object_id = OBJECT_ID(N'act_ru_event_subscr'))
CREATE INDEX [ACT_IDX_EVENT_SUBSCR_EXEC_ID] ON [act_ru_event_subscr] ([EXECUTION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EVENT_SUBSCR_PROC_ID' AND object_id = OBJECT_ID(N'act_ru_event_subscr'))
CREATE INDEX [ACT_IDX_EVENT_SUBSCR_PROC_ID] ON [act_ru_event_subscr] ([PROC_INST_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EVENT_SUBSCR_SCOPEREF_' AND object_id = OBJECT_ID(N'act_ru_event_subscr'))
CREATE INDEX [ACT_IDX_EVENT_SUBSCR_SCOPEREF_] ON [act_ru_event_subscr] ([SCOPE_ID_], [SCOPE_TYPE_]);

IF OBJECT_ID(N'act_ru_external_job', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_external_job] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [TYPE_] NVARCHAR(255) NOT NULL,
    [LOCK_EXP_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [LOCK_OWNER_] NVARCHAR(255) DEFAULT NULL,
    [EXCLUSIVE_] BIT DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROCESS_INSTANCE_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [ELEMENT_ID_] NVARCHAR(255) DEFAULT NULL,
    [ELEMENT_NAME_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [CORRELATION_ID_] NVARCHAR(255) DEFAULT NULL,
    [RETRIES_] INT DEFAULT NULL,
    [EXCEPTION_STACK_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXCEPTION_MSG_] NVARCHAR(4000) DEFAULT NULL,
    [DUEDATE_] DATETIME2(3) NULL DEFAULT NULL,
    [REPEAT_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_CFG_] NVARCHAR(4000) DEFAULT NULL,
    [CUSTOM_VALUES_ID_] NVARCHAR(64) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_EXTERNAL_JOB_CUSTOM_VALUES] FOREIGN KEY ([CUSTOM_VALUES_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_EXTERNAL_JOB_EXCEPTION] FOREIGN KEY ([EXCEPTION_STACK_ID_]) REFERENCES [act_ge_bytearray] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_external_job', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_external_job';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EXTERNAL_JOB_EXCEPTION_STACK_ID' AND object_id = OBJECT_ID(N'act_ru_external_job'))
CREATE INDEX [ACT_IDX_EXTERNAL_JOB_EXCEPTION_STACK_ID] ON [act_ru_external_job] ([EXCEPTION_STACK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EXTERNAL_JOB_CUSTOM_VALUES_ID' AND object_id = OBJECT_ID(N'act_ru_external_job'))
CREATE INDEX [ACT_IDX_EXTERNAL_JOB_CUSTOM_VALUES_ID] ON [act_ru_external_job] ([CUSTOM_VALUES_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EXTERNAL_JOB_CORRELATION_ID' AND object_id = OBJECT_ID(N'act_ru_external_job'))
CREATE INDEX [ACT_IDX_EXTERNAL_JOB_CORRELATION_ID] ON [act_ru_external_job] ([CORRELATION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EJOB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_external_job'))
CREATE INDEX [ACT_IDX_EJOB_SCOPE] ON [act_ru_external_job] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EJOB_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_external_job'))
CREATE INDEX [ACT_IDX_EJOB_SUB_SCOPE] ON [act_ru_external_job] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_EJOB_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_ru_external_job'))
CREATE INDEX [ACT_IDX_EJOB_SCOPE_DEF] ON [act_ru_external_job] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);

IF OBJECT_ID(N'act_ru_history_job', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_history_job] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [LOCK_EXP_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [LOCK_OWNER_] NVARCHAR(255) DEFAULT NULL,
    [RETRIES_] INT DEFAULT NULL,
    [EXCEPTION_STACK_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXCEPTION_MSG_] NVARCHAR(4000) DEFAULT NULL,
    [HANDLER_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_CFG_] NVARCHAR(4000) DEFAULT NULL,
    [CUSTOM_VALUES_ID_] NVARCHAR(64) DEFAULT NULL,
    [ADV_HANDLER_CFG_ID_] NVARCHAR(64) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_history_job', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_history_job';

IF OBJECT_ID(N'act_ru_task', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_task] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [TASK_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [PROPAGATED_STAGE_INST_ID_] NVARCHAR(255) DEFAULT NULL,
    [STATE_] NVARCHAR(255) DEFAULT NULL,
    [NAME_] NVARCHAR(255) DEFAULT NULL,
    [PARENT_TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [DESCRIPTION_] NVARCHAR(4000) DEFAULT NULL,
    [TASK_DEF_KEY_] NVARCHAR(255) DEFAULT NULL,
    [OWNER_] NVARCHAR(255) DEFAULT NULL,
    [ASSIGNEE_] NVARCHAR(255) DEFAULT NULL,
    [DELEGATION_] NVARCHAR(64) DEFAULT NULL,
    [PRIORITY_] INT DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [IN_PROGRESS_TIME_] DATETIME2(3) DEFAULT NULL,
    [IN_PROGRESS_STARTED_BY_] NVARCHAR(255) DEFAULT NULL,
    [CLAIM_TIME_] DATETIME2(3) DEFAULT NULL,
    [CLAIMED_BY_] NVARCHAR(255) DEFAULT NULL,
    [SUSPENDED_TIME_] DATETIME2(3) DEFAULT NULL,
    [SUSPENDED_BY_] NVARCHAR(255) DEFAULT NULL,
    [IN_PROGRESS_DUE_DATE_] DATETIME2(3) DEFAULT NULL,
    [DUE_DATE_] DATETIME2(3) DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [SUSPENSION_STATE_] INT DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    [FORM_KEY_] NVARCHAR(255) DEFAULT NULL,
    [IS_COUNT_ENABLED_] SMALLINT DEFAULT NULL,
    [VAR_COUNT_] INT DEFAULT NULL,
    [ID_LINK_COUNT_] INT DEFAULT NULL,
    [SUB_TASK_COUNT_] INT DEFAULT NULL,
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_TASK_EXE] FOREIGN KEY ([EXECUTION_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_TASK_PROCDEF] FOREIGN KEY ([PROC_DEF_ID_]) REFERENCES [act_re_procdef] ([ID_]),
    CONSTRAINT [ACT_FK_TASK_PROCINST] FOREIGN KEY ([PROC_INST_ID_]) REFERENCES [act_ru_execution] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_task', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_task';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TASK_CREATE' AND object_id = OBJECT_ID(N'act_ru_task'))
CREATE INDEX [ACT_IDX_TASK_CREATE] ON [act_ru_task] ([CREATE_TIME_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TASK_SCOPE' AND object_id = OBJECT_ID(N'act_ru_task'))
CREATE INDEX [ACT_IDX_TASK_SCOPE] ON [act_ru_task] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TASK_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_task'))
CREATE INDEX [ACT_IDX_TASK_SUB_SCOPE] ON [act_ru_task] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TASK_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_ru_task'))
CREATE INDEX [ACT_IDX_TASK_SCOPE_DEF] ON [act_ru_task] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_TASK_EXE' AND object_id = OBJECT_ID(N'act_ru_task'))
CREATE INDEX [ACT_FK_TASK_EXE] ON [act_ru_task] ([EXECUTION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_TASK_PROCINST' AND object_id = OBJECT_ID(N'act_ru_task'))
CREATE INDEX [ACT_FK_TASK_PROCINST] ON [act_ru_task] ([PROC_INST_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_TASK_PROCDEF' AND object_id = OBJECT_ID(N'act_ru_task'))
CREATE INDEX [ACT_FK_TASK_PROCDEF] ON [act_ru_task] ([PROC_DEF_ID_]);

IF OBJECT_ID(N'act_ru_identitylink', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_identitylink] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [GROUP_ID_] NVARCHAR(255) DEFAULT NULL,
    [TYPE_] NVARCHAR(255) DEFAULT NULL,
    [USER_ID_] NVARCHAR(255) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_ATHRZ_PROCEDEF] FOREIGN KEY ([PROC_DEF_ID_]) REFERENCES [act_re_procdef] ([ID_]),
    CONSTRAINT [ACT_FK_IDL_PROCINST] FOREIGN KEY ([PROC_INST_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_TSKASS_TASK] FOREIGN KEY ([TASK_ID_]) REFERENCES [act_ru_task] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_identitylink', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_identitylink';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_IDENT_LNK_USER' AND object_id = OBJECT_ID(N'act_ru_identitylink'))
CREATE INDEX [ACT_IDX_IDENT_LNK_USER] ON [act_ru_identitylink] ([USER_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_IDENT_LNK_GROUP' AND object_id = OBJECT_ID(N'act_ru_identitylink'))
CREATE INDEX [ACT_IDX_IDENT_LNK_GROUP] ON [act_ru_identitylink] ([GROUP_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_IDENT_LNK_SCOPE' AND object_id = OBJECT_ID(N'act_ru_identitylink'))
CREATE INDEX [ACT_IDX_IDENT_LNK_SCOPE] ON [act_ru_identitylink] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_IDENT_LNK_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_identitylink'))
CREATE INDEX [ACT_IDX_IDENT_LNK_SUB_SCOPE] ON [act_ru_identitylink] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_IDENT_LNK_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_ru_identitylink'))
CREATE INDEX [ACT_IDX_IDENT_LNK_SCOPE_DEF] ON [act_ru_identitylink] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_ATHRZ_PROCEDEF' AND object_id = OBJECT_ID(N'act_ru_identitylink'))
CREATE INDEX [ACT_IDX_ATHRZ_PROCEDEF] ON [act_ru_identitylink] ([PROC_DEF_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_TSKASS_TASK' AND object_id = OBJECT_ID(N'act_ru_identitylink'))
CREATE INDEX [ACT_FK_TSKASS_TASK] ON [act_ru_identitylink] ([TASK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_IDL_PROCINST' AND object_id = OBJECT_ID(N'act_ru_identitylink'))
CREATE INDEX [ACT_FK_IDL_PROCINST] ON [act_ru_identitylink] ([PROC_INST_ID_]);

IF OBJECT_ID(N'act_ru_job', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_job] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [TYPE_] NVARCHAR(255) NOT NULL,
    [LOCK_EXP_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [LOCK_OWNER_] NVARCHAR(255) DEFAULT NULL,
    [EXCLUSIVE_] BIT DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROCESS_INSTANCE_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [ELEMENT_ID_] NVARCHAR(255) DEFAULT NULL,
    [ELEMENT_NAME_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [CORRELATION_ID_] NVARCHAR(255) DEFAULT NULL,
    [RETRIES_] INT DEFAULT NULL,
    [EXCEPTION_STACK_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXCEPTION_MSG_] NVARCHAR(4000) DEFAULT NULL,
    [DUEDATE_] DATETIME2(3) NULL DEFAULT NULL,
    [REPEAT_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_CFG_] NVARCHAR(4000) DEFAULT NULL,
    [CUSTOM_VALUES_ID_] NVARCHAR(64) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_JOB_CUSTOM_VALUES] FOREIGN KEY ([CUSTOM_VALUES_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_JOB_EXCEPTION] FOREIGN KEY ([EXCEPTION_STACK_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_JOB_EXECUTION] FOREIGN KEY ([EXECUTION_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_JOB_PROCESS_INSTANCE] FOREIGN KEY ([PROCESS_INSTANCE_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_JOB_PROC_DEF] FOREIGN KEY ([PROC_DEF_ID_]) REFERENCES [act_re_procdef] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_job', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_job';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_JOB_EXCEPTION_STACK_ID' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_IDX_JOB_EXCEPTION_STACK_ID] ON [act_ru_job] ([EXCEPTION_STACK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_JOB_CUSTOM_VALUES_ID' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_IDX_JOB_CUSTOM_VALUES_ID] ON [act_ru_job] ([CUSTOM_VALUES_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_JOB_CORRELATION_ID' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_IDX_JOB_CORRELATION_ID] ON [act_ru_job] ([CORRELATION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_JOB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_IDX_JOB_SCOPE] ON [act_ru_job] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_JOB_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_IDX_JOB_SUB_SCOPE] ON [act_ru_job] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_JOB_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_IDX_JOB_SCOPE_DEF] ON [act_ru_job] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_JOB_EXECUTION' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_FK_JOB_EXECUTION] ON [act_ru_job] ([EXECUTION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_JOB_PROCESS_INSTANCE' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_FK_JOB_PROCESS_INSTANCE] ON [act_ru_job] ([PROCESS_INSTANCE_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_JOB_PROC_DEF' AND object_id = OBJECT_ID(N'act_ru_job'))
CREATE INDEX [ACT_FK_JOB_PROC_DEF] ON [act_ru_job] ([PROC_DEF_ID_]);

IF OBJECT_ID(N'act_ru_suspended_job', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_suspended_job] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [TYPE_] NVARCHAR(255) NOT NULL,
    [EXCLUSIVE_] BIT DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROCESS_INSTANCE_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [ELEMENT_ID_] NVARCHAR(255) DEFAULT NULL,
    [ELEMENT_NAME_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [CORRELATION_ID_] NVARCHAR(255) DEFAULT NULL,
    [RETRIES_] INT DEFAULT NULL,
    [EXCEPTION_STACK_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXCEPTION_MSG_] NVARCHAR(4000) DEFAULT NULL,
    [DUEDATE_] DATETIME2(3) NULL DEFAULT NULL,
    [REPEAT_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_CFG_] NVARCHAR(4000) DEFAULT NULL,
    [CUSTOM_VALUES_ID_] NVARCHAR(64) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_SUSPENDED_JOB_CUSTOM_VALUES] FOREIGN KEY ([CUSTOM_VALUES_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_SUSPENDED_JOB_EXCEPTION] FOREIGN KEY ([EXCEPTION_STACK_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_SUSPENDED_JOB_EXECUTION] FOREIGN KEY ([EXECUTION_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE] FOREIGN KEY ([PROCESS_INSTANCE_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_SUSPENDED_JOB_PROC_DEF] FOREIGN KEY ([PROC_DEF_ID_]) REFERENCES [act_re_procdef] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_suspended_job', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_suspended_job';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_SUSPENDED_JOB_EXCEPTION_STACK_ID' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_IDX_SUSPENDED_JOB_EXCEPTION_STACK_ID] ON [act_ru_suspended_job] ([EXCEPTION_STACK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_SUSPENDED_JOB_CUSTOM_VALUES_ID' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_IDX_SUSPENDED_JOB_CUSTOM_VALUES_ID] ON [act_ru_suspended_job] ([CUSTOM_VALUES_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_SUSPENDED_JOB_CORRELATION_ID' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_IDX_SUSPENDED_JOB_CORRELATION_ID] ON [act_ru_suspended_job] ([CORRELATION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_SJOB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_IDX_SJOB_SCOPE] ON [act_ru_suspended_job] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_SJOB_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_IDX_SJOB_SUB_SCOPE] ON [act_ru_suspended_job] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_SJOB_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_IDX_SJOB_SCOPE_DEF] ON [act_ru_suspended_job] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_SUSPENDED_JOB_EXECUTION' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_FK_SUSPENDED_JOB_EXECUTION] ON [act_ru_suspended_job] ([EXECUTION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE] ON [act_ru_suspended_job] ([PROCESS_INSTANCE_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_SUSPENDED_JOB_PROC_DEF' AND object_id = OBJECT_ID(N'act_ru_suspended_job'))
CREATE INDEX [ACT_FK_SUSPENDED_JOB_PROC_DEF] ON [act_ru_suspended_job] ([PROC_DEF_ID_]);

IF OBJECT_ID(N'act_ru_timer_job', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_timer_job] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [CATEGORY_] NVARCHAR(255) DEFAULT NULL,
    [TYPE_] NVARCHAR(255) NOT NULL,
    [LOCK_EXP_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [LOCK_OWNER_] NVARCHAR(255) DEFAULT NULL,
    [EXCLUSIVE_] BIT DEFAULT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROCESS_INSTANCE_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_DEF_ID_] NVARCHAR(64) DEFAULT NULL,
    [ELEMENT_ID_] NVARCHAR(255) DEFAULT NULL,
    [ELEMENT_NAME_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_DEFINITION_ID_] NVARCHAR(255) DEFAULT NULL,
    [CORRELATION_ID_] NVARCHAR(255) DEFAULT NULL,
    [RETRIES_] INT DEFAULT NULL,
    [EXCEPTION_STACK_ID_] NVARCHAR(64) DEFAULT NULL,
    [EXCEPTION_MSG_] NVARCHAR(4000) DEFAULT NULL,
    [DUEDATE_] DATETIME2(3) NULL DEFAULT NULL,
    [REPEAT_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [HANDLER_CFG_] NVARCHAR(4000) DEFAULT NULL,
    [CUSTOM_VALUES_ID_] NVARCHAR(64) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NULL DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_TIMER_JOB_CUSTOM_VALUES] FOREIGN KEY ([CUSTOM_VALUES_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_TIMER_JOB_EXCEPTION] FOREIGN KEY ([EXCEPTION_STACK_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_TIMER_JOB_EXECUTION] FOREIGN KEY ([EXECUTION_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_TIMER_JOB_PROCESS_INSTANCE] FOREIGN KEY ([PROCESS_INSTANCE_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_TIMER_JOB_PROC_DEF] FOREIGN KEY ([PROC_DEF_ID_]) REFERENCES [act_re_procdef] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_timer_job', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_timer_job';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TIMER_JOB_EXCEPTION_STACK_ID' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_IDX_TIMER_JOB_EXCEPTION_STACK_ID] ON [act_ru_timer_job] ([EXCEPTION_STACK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TIMER_JOB_CUSTOM_VALUES_ID' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_IDX_TIMER_JOB_CUSTOM_VALUES_ID] ON [act_ru_timer_job] ([CUSTOM_VALUES_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TIMER_JOB_CORRELATION_ID' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_IDX_TIMER_JOB_CORRELATION_ID] ON [act_ru_timer_job] ([CORRELATION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TIMER_JOB_DUEDATE' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_IDX_TIMER_JOB_DUEDATE] ON [act_ru_timer_job] ([DUEDATE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TJOB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_IDX_TJOB_SCOPE] ON [act_ru_timer_job] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TJOB_SUB_SCOPE' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_IDX_TJOB_SUB_SCOPE] ON [act_ru_timer_job] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_TJOB_SCOPE_DEF' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_IDX_TJOB_SCOPE_DEF] ON [act_ru_timer_job] ([SCOPE_DEFINITION_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_TIMER_JOB_EXECUTION' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_FK_TIMER_JOB_EXECUTION] ON [act_ru_timer_job] ([EXECUTION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_TIMER_JOB_PROCESS_INSTANCE' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_FK_TIMER_JOB_PROCESS_INSTANCE] ON [act_ru_timer_job] ([PROCESS_INSTANCE_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_TIMER_JOB_PROC_DEF' AND object_id = OBJECT_ID(N'act_ru_timer_job'))
CREATE INDEX [ACT_FK_TIMER_JOB_PROC_DEF] ON [act_ru_timer_job] ([PROC_DEF_ID_]);

IF OBJECT_ID(N'act_ru_variable', N'U') IS NULL
BEGIN
CREATE TABLE [act_ru_variable] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [TYPE_] NVARCHAR(255) NOT NULL,
    [NAME_] NVARCHAR(255) NOT NULL,
    [EXECUTION_ID_] NVARCHAR(64) DEFAULT NULL,
    [PROC_INST_ID_] NVARCHAR(64) DEFAULT NULL,
    [TASK_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(255) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(255) DEFAULT NULL,
    [BYTEARRAY_ID_] NVARCHAR(64) DEFAULT NULL,
    [DOUBLE_] FLOAT DEFAULT NULL,
    [LONG_] BIGINT DEFAULT NULL,
    [TEXT_] NVARCHAR(4000) DEFAULT NULL,
    [TEXT2_] NVARCHAR(4000) DEFAULT NULL,
    [META_INFO_] NVARCHAR(4000) DEFAULT NULL,
    PRIMARY KEY ([ID_]),
    CONSTRAINT [ACT_FK_VAR_BYTEARRAY] FOREIGN KEY ([BYTEARRAY_ID_]) REFERENCES [act_ge_bytearray] ([ID_]),
    CONSTRAINT [ACT_FK_VAR_EXE] FOREIGN KEY ([EXECUTION_ID_]) REFERENCES [act_ru_execution] ([ID_]),
    CONSTRAINT [ACT_FK_VAR_PROCINST] FOREIGN KEY ([PROC_INST_ID_]) REFERENCES [act_ru_execution] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 act_ru_variable', N'SCHEMA', N'dbo', N'TABLE', N'act_ru_variable';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_VAR_SCOPE_ID_TYPE' AND object_id = OBJECT_ID(N'act_ru_variable'))
CREATE INDEX [ACT_IDX_RU_VAR_SCOPE_ID_TYPE] ON [act_ru_variable] ([SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_RU_VAR_SUB_ID_TYPE' AND object_id = OBJECT_ID(N'act_ru_variable'))
CREATE INDEX [ACT_IDX_RU_VAR_SUB_ID_TYPE] ON [act_ru_variable] ([SUB_SCOPE_ID_], [SCOPE_TYPE_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_VAR_BYTEARRAY' AND object_id = OBJECT_ID(N'act_ru_variable'))
CREATE INDEX [ACT_FK_VAR_BYTEARRAY] ON [act_ru_variable] ([BYTEARRAY_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_IDX_VARIABLE_TASK_ID' AND object_id = OBJECT_ID(N'act_ru_variable'))
CREATE INDEX [ACT_IDX_VARIABLE_TASK_ID] ON [act_ru_variable] ([TASK_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_VAR_EXE' AND object_id = OBJECT_ID(N'act_ru_variable'))
CREATE INDEX [ACT_FK_VAR_EXE] ON [act_ru_variable] ([EXECUTION_ID_]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ACT_FK_VAR_PROCINST' AND object_id = OBJECT_ID(N'act_ru_variable'))
CREATE INDEX [ACT_FK_VAR_PROCINST] ON [act_ru_variable] ([PROC_INST_ID_]);

IF OBJECT_ID(N'flw_ru_batch', N'U') IS NULL
BEGIN
CREATE TABLE [flw_ru_batch] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [TYPE_] NVARCHAR(64) NOT NULL,
    [SEARCH_KEY_] NVARCHAR(255) DEFAULT NULL,
    [SEARCH_KEY2_] NVARCHAR(255) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NOT NULL,
    [COMPLETE_TIME_] DATETIME2(3) DEFAULT NULL,
    [STATUS_] NVARCHAR(255) DEFAULT NULL,
    [BATCH_DOC_ID_] NVARCHAR(64) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 flw_ru_batch', N'SCHEMA', N'dbo', N'TABLE', N'flw_ru_batch';

IF OBJECT_ID(N'flw_ru_batch_part', N'U') IS NULL
BEGIN
CREATE TABLE [flw_ru_batch_part] (
    [ID_] NVARCHAR(64) NOT NULL,
    [REV_] INT DEFAULT NULL,
    [BATCH_ID_] NVARCHAR(64) DEFAULT NULL,
    [TYPE_] NVARCHAR(64) NOT NULL,
    [SCOPE_ID_] NVARCHAR(64) DEFAULT NULL,
    [SUB_SCOPE_ID_] NVARCHAR(64) DEFAULT NULL,
    [SCOPE_TYPE_] NVARCHAR(64) DEFAULT NULL,
    [SEARCH_KEY_] NVARCHAR(255) DEFAULT NULL,
    [SEARCH_KEY2_] NVARCHAR(255) DEFAULT NULL,
    [CREATE_TIME_] DATETIME2(3) NOT NULL,
    [COMPLETE_TIME_] DATETIME2(3) DEFAULT NULL,
    [STATUS_] NVARCHAR(255) DEFAULT NULL,
    [RESULT_DOC_ID_] NVARCHAR(64) DEFAULT NULL,
    [TENANT_ID_] NVARCHAR(255) DEFAULT '',
    PRIMARY KEY ([ID_]),
    CONSTRAINT [FLW_FK_BATCH_PART_PARENT] FOREIGN KEY ([BATCH_ID_]) REFERENCES [flw_ru_batch] ([ID_])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'Flowable 引擎表 flw_ru_batch_part', N'SCHEMA', N'dbo', N'TABLE', N'flw_ru_batch_part';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'FLW_IDX_BATCH_PART' AND object_id = OBJECT_ID(N'flw_ru_batch_part'))
CREATE INDEX [FLW_IDX_BATCH_PART] ON [flw_ru_batch_part] ([BATCH_ID_]);

-- ============================================================
-- 6. 开放平台管理表
-- 来源：zhyc-base-server/zhyc-module-openapi/src/main/resources/db/V1__openapi_core.sql
-- ============================================================
IF OBJECT_ID(N'openapi_app', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_app] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [app_name] NVARCHAR(128) NOT NULL,
    [owner_user_id] BIGINT NOT NULL,
    [auth_mode] NVARCHAR(32) NOT NULL,
    [ip_whitelist] NVARCHAR(MAX),
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_app_tenant_id] UNIQUE ([tenant_id], [id]),
    CONSTRAINT [uk_openapi_app_tenant_code] UNIQUE ([tenant_id], [app_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台开发者应用表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'应用编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'应用名称', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'app_name';
EXEC sp_addextendedproperty N'MS_Description', N'应用负责人用户主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'owner_user_id';
EXEC sp_addextendedproperty N'MS_Description', N'鉴权方式', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'auth_mode';
EXEC sp_addextendedproperty N'MS_Description', N'IP 白名单 JSON', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'ip_whitelist';
EXEC sp_addextendedproperty N'MS_Description', N'应用状态', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_app', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_app_tenant_owner' AND object_id = OBJECT_ID(N'openapi_app'))
CREATE INDEX [idx_openapi_app_tenant_owner] ON [openapi_app] ([tenant_id], [owner_user_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_app_tenant_status' AND object_id = OBJECT_ID(N'openapi_app'))
CREATE INDEX [idx_openapi_app_tenant_status] ON [openapi_app] ([tenant_id], [status]);

IF OBJECT_ID(N'openapi_api_key', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_api_key] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [access_key] NVARCHAR(128) NOT NULL,
    [secret_cipher] NVARCHAR(512) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [expire_at] DATETIME2,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_api_key_access_key] UNIQUE ([access_key]),
    CONSTRAINT [fk_openapi_api_key_app] FOREIGN KEY ([tenant_id], [app_code]) REFERENCES [openapi_app] ([tenant_id], [app_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API Key 表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'开发者应用编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'API 访问密钥', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'access_key';
EXEC sp_addextendedproperty N'MS_Description', N'API Secret 密文', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'secret_cipher';
EXEC sp_addextendedproperty N'MS_Description', N'API Key 状态', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'凭证过期时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'expire_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_key', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_api_key_tenant_app' AND object_id = OBJECT_ID(N'openapi_api_key'))
CREATE INDEX [idx_openapi_api_key_tenant_app] ON [openapi_api_key] ([tenant_id], [app_code]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_api_key_tenant_status' AND object_id = OBJECT_ID(N'openapi_api_key'))
CREATE INDEX [idx_openapi_api_key_tenant_status] ON [openapi_api_key] ([tenant_id], [status]);

IF OBJECT_ID(N'openapi_api_permission', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_api_permission] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [api_code] NVARCHAR(128) NOT NULL,
    [api_name] NVARCHAR(128) NOT NULL,
    [http_method] NVARCHAR(16) NOT NULL,
    [path_pattern] NVARCHAR(256) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_api_permission_app_api] UNIQUE ([tenant_id], [app_code], [api_code]),
    CONSTRAINT [fk_openapi_api_permission_app] FOREIGN KEY ([tenant_id], [app_code]) REFERENCES [openapi_app] ([tenant_id], [app_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API 权限授权表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'开发者应用编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'API 业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'api_code';
EXEC sp_addextendedproperty N'MS_Description', N'API 名称', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'api_name';
EXEC sp_addextendedproperty N'MS_Description', N'HTTP 方法', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'http_method';
EXEC sp_addextendedproperty N'MS_Description', N'请求路径匹配规则', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'path_pattern';
EXEC sp_addextendedproperty N'MS_Description', N'授权状态', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_api_permission', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_api_permission_app_path' AND object_id = OBJECT_ID(N'openapi_api_permission'))
CREATE INDEX [idx_openapi_api_permission_app_path] ON [openapi_api_permission] ([tenant_id], [app_code], [http_method], [path_pattern]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_api_permission_app_status' AND object_id = OBJECT_ID(N'openapi_api_permission'))
CREATE INDEX [idx_openapi_api_permission_app_status] ON [openapi_api_permission] ([tenant_id], [app_code], [status]);

IF OBJECT_ID(N'openapi_oauth_client', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_oauth_client] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [client_id] NVARCHAR(128) NOT NULL,
    [allowed_scopes] NVARCHAR(512) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_oauth_client_app_client] UNIQUE ([tenant_id], [app_code], [client_id]),
    CONSTRAINT [fk_openapi_oauth_client_app] FOREIGN KEY ([tenant_id], [app_code]) REFERENCES [openapi_app] ([tenant_id], [app_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 OAuth2 客户端映射表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'开发者应用编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'认证中心 OAuth2 客户端 ID', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client', N'COLUMN', N'client_id';
EXEC sp_addextendedproperty N'MS_Description', N'允许的 OAuth2 授权范围', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client', N'COLUMN', N'allowed_scopes';
EXEC sp_addextendedproperty N'MS_Description', N'客户端映射状态', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_oauth_client', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_oauth_client_app_status' AND object_id = OBJECT_ID(N'openapi_oauth_client'))
CREATE INDEX [idx_openapi_oauth_client_app_status] ON [openapi_oauth_client] ([tenant_id], [app_code], [status]);

IF OBJECT_ID(N'openapi_catalog', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_catalog] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [api_code] NVARCHAR(128) NOT NULL,
    [api_name] NVARCHAR(128) NOT NULL,
    [group_code] NVARCHAR(64) NOT NULL,
    [http_method] NVARCHAR(16) NOT NULL,
    [path_pattern] NVARCHAR(256) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_catalog_api_code] UNIQUE ([api_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API 目录表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'API 业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'api_code';
EXEC sp_addextendedproperty N'MS_Description', N'API 名称', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'api_name';
EXEC sp_addextendedproperty N'MS_Description', N'API 分组编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'group_code';
EXEC sp_addextendedproperty N'MS_Description', N'HTTP 方法', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'http_method';
EXEC sp_addextendedproperty N'MS_Description', N'请求路径匹配规则', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'path_pattern';
EXEC sp_addextendedproperty N'MS_Description', N'API 目录状态', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_catalog', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_catalog_group_status' AND object_id = OBJECT_ID(N'openapi_catalog'))
CREATE INDEX [idx_openapi_catalog_group_status] ON [openapi_catalog] ([group_code], [status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_catalog_method_path' AND object_id = OBJECT_ID(N'openapi_catalog'))
CREATE INDEX [idx_openapi_catalog_method_path] ON [openapi_catalog] ([http_method], [path_pattern]);

IF OBJECT_ID(N'openapi_version', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_version] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [api_code] NVARCHAR(128) NOT NULL,
    [version] NVARCHAR(32) NOT NULL,
    [backend_route] NVARCHAR(512) NOT NULL,
    [request_schema] JSON,
    [response_schema] JSON,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'published',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_version_api_version] UNIQUE ([api_code], [version]),
    CONSTRAINT [fk_openapi_version_catalog] FOREIGN KEY ([api_code]) REFERENCES [openapi_catalog] ([api_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API 版本发布表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'API 业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'api_code';
EXEC sp_addextendedproperty N'MS_Description', N'API 版本号', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'后端转发路由', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'backend_route';
EXEC sp_addextendedproperty N'MS_Description', N'请求 JSON Schema', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'request_schema';
EXEC sp_addextendedproperty N'MS_Description', N'响应 JSON Schema', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'response_schema';
EXEC sp_addextendedproperty N'MS_Description', N'API 版本状态', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_version', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_version_api_status' AND object_id = OBJECT_ID(N'openapi_version'))
CREATE INDEX [idx_openapi_version_api_status] ON [openapi_version] ([api_code], [status]);

IF OBJECT_ID(N'openapi_signature_policy', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_signature_policy] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [algorithm] NVARCHAR(32) NOT NULL,
    [timestamp_tolerance_seconds] INT NOT NULL,
    [nonce_ttl_seconds] INT NOT NULL,
    [require_body_hash] SMALLINT NOT NULL DEFAULT 1,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_signature_policy_app] UNIQUE ([tenant_id], [app_code]),
    CONSTRAINT [fk_openapi_signature_policy_app] FOREIGN KEY ([tenant_id], [app_code]) REFERENCES [openapi_app] ([tenant_id], [app_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API 签名策略表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'开发者应用编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'签名算法，首期支持 HMAC_SHA256', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'algorithm';
EXEC sp_addextendedproperty N'MS_Description', N'客户端时间戳允许偏差秒数', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'timestamp_tolerance_seconds';
EXEC sp_addextendedproperty N'MS_Description', N'nonce 防重放有效期秒数', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'nonce_ttl_seconds';
EXEC sp_addextendedproperty N'MS_Description', N'是否要求请求体参与摘要，1 是 0 否', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'require_body_hash';
EXEC sp_addextendedproperty N'MS_Description', N'签名策略状态', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_signature_policy', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_signature_policy_app_status' AND object_id = OBJECT_ID(N'openapi_signature_policy'))
CREATE INDEX [idx_openapi_signature_policy_app_status] ON [openapi_signature_policy] ([tenant_id], [app_code], [status]);

IF OBJECT_ID(N'openapi_rate_limit_policy', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_rate_limit_policy] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [api_code] NVARCHAR(128) NOT NULL,
    [limit_count] INT NOT NULL,
    [window_seconds] INT NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_rate_limit_policy_app_api] UNIQUE ([tenant_id], [app_code], [api_code]),
    CONSTRAINT [fk_openapi_rate_limit_policy_app] FOREIGN KEY ([tenant_id], [app_code]) REFERENCES [openapi_app] ([tenant_id], [app_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API 限流策略表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'开发者应用编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'API 业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'api_code';
EXEC sp_addextendedproperty N'MS_Description', N'时间窗口内允许请求次数', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'limit_count';
EXEC sp_addextendedproperty N'MS_Description', N'限流时间窗口秒数', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'window_seconds';
EXEC sp_addextendedproperty N'MS_Description', N'限流策略状态', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_policy', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_rate_limit_policy_app_status' AND object_id = OBJECT_ID(N'openapi_rate_limit_policy'))
CREATE INDEX [idx_openapi_rate_limit_policy_app_status] ON [openapi_rate_limit_policy] ([tenant_id], [app_code], [status]);

IF OBJECT_ID(N'openapi_rate_limit_counter', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_rate_limit_counter] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [api_code] NVARCHAR(128) NOT NULL,
    [window_seconds] BIGINT NOT NULL,
    [window_index] BIGINT NOT NULL,
    [request_count] INT NOT NULL DEFAULT 0,
    [expires_at] DATETIME2 NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_rate_limit_counter_window] UNIQUE ([tenant_id], [app_code], [api_code], [window_seconds], [window_index])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API 运行期限流计数表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'开发者应用编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'API 业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'api_code';
EXEC sp_addextendedproperty N'MS_Description', N'限流窗口秒数', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'window_seconds';
EXEC sp_addextendedproperty N'MS_Description', N'限流窗口序号', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'window_index';
EXEC sp_addextendedproperty N'MS_Description', N'当前窗口请求次数', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'request_count';
EXEC sp_addextendedproperty N'MS_Description', N'窗口过期时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_rate_limit_counter', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_rate_limit_counter_expires' AND object_id = OBJECT_ID(N'openapi_rate_limit_counter'))
CREATE INDEX [idx_openapi_rate_limit_counter_expires] ON [openapi_rate_limit_counter] ([expires_at]);

IF OBJECT_ID(N'openapi_replay_nonce', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_replay_nonce] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [app_key] NVARCHAR(128) NOT NULL,
    [nonce_value] NVARCHAR(128) NOT NULL,
    [expires_at] DATETIME2 NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_openapi_replay_nonce_app_nonce] UNIQUE ([app_key], [nonce_value])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API 防重放 nonce 表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_replay_nonce';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_replay_nonce', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'开放平台应用标识', N'SCHEMA', N'dbo', N'TABLE', N'openapi_replay_nonce', N'COLUMN', N'app_key';
EXEC sp_addextendedproperty N'MS_Description', N'请求一次性随机串', N'SCHEMA', N'dbo', N'TABLE', N'openapi_replay_nonce', N'COLUMN', N'nonce_value';
EXEC sp_addextendedproperty N'MS_Description', N'nonce 过期时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_replay_nonce', N'COLUMN', N'expires_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_replay_nonce', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_replay_nonce_expires' AND object_id = OBJECT_ID(N'openapi_replay_nonce'))
CREATE INDEX [idx_openapi_replay_nonce_expires] ON [openapi_replay_nonce] ([expires_at]);

IF OBJECT_ID(N'openapi_call_audit', N'U') IS NULL
BEGIN
CREATE TABLE [openapi_call_audit] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [access_key] NVARCHAR(128) NOT NULL,
    [api_code] NVARCHAR(128) NOT NULL,
    [http_method] NVARCHAR(16) NOT NULL,
    [request_path] NVARCHAR(512) NOT NULL,
    [response_status] INT NOT NULL,
    [duration_ms] BIGINT NOT NULL,
    [success] SMALLINT NOT NULL,
    [error_code] NVARCHAR(64),
    [client_ip] NVARCHAR(64) NOT NULL,
    [request_id] NVARCHAR(128) NOT NULL,
    [called_at] DATETIME2 NOT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'开放平台 API 调用审计表', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'开发者应用编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'API 访问密钥', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'access_key';
EXEC sp_addextendedproperty N'MS_Description', N'API 业务编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'api_code';
EXEC sp_addextendedproperty N'MS_Description', N'HTTP 方法', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'http_method';
EXEC sp_addextendedproperty N'MS_Description', N'请求路径', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'request_path';
EXEC sp_addextendedproperty N'MS_Description', N'HTTP 响应状态码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'response_status';
EXEC sp_addextendedproperty N'MS_Description', N'调用耗时毫秒', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'duration_ms';
EXEC sp_addextendedproperty N'MS_Description', N'是否调用成功，1 是 0 否', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'success';
EXEC sp_addextendedproperty N'MS_Description', N'错误编码', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'error_code';
EXEC sp_addextendedproperty N'MS_Description', N'客户端 IP', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'client_ip';
EXEC sp_addextendedproperty N'MS_Description', N'请求追踪 ID', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'request_id';
EXEC sp_addextendedproperty N'MS_Description', N'调用时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'called_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'openapi_call_audit', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_call_audit_app_called' AND object_id = OBJECT_ID(N'openapi_call_audit'))
CREATE INDEX [idx_openapi_call_audit_app_called] ON [openapi_call_audit] ([tenant_id], [app_code], [called_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_call_audit_app_api' AND object_id = OBJECT_ID(N'openapi_call_audit'))
CREATE INDEX [idx_openapi_call_audit_app_api] ON [openapi_call_audit] ([tenant_id], [app_code], [api_code]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_openapi_call_audit_request_id' AND object_id = OBJECT_ID(N'openapi_call_audit'))
CREATE INDEX [idx_openapi_call_audit_request_id] ON [openapi_call_audit] ([request_id]);

-- ============================================================
-- 7. AI 能力中心表
-- 来源：zhyc-base-server/zhyc-module-ai/src/main/resources/db/V1__ai_core.sql
-- ============================================================
IF OBJECT_ID(N'ai_provider', N'U') IS NULL
BEGIN
CREATE TABLE [ai_provider] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [provider_code] NVARCHAR(64) NOT NULL,
    [provider_name] NVARCHAR(128) NOT NULL,
    [provider_type] NVARCHAR(64) NOT NULL,
    [base_url] NVARCHAR(512) NOT NULL,
    [secret_ref] NVARCHAR(255) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_ai_provider_tenant_code] UNIQUE ([tenant_id], [provider_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'AI 模型供应商表', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'供应商编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'provider_code';
EXEC sp_addextendedproperty N'MS_Description', N'供应商名称', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'provider_name';
EXEC sp_addextendedproperty N'MS_Description', N'供应商类型', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'provider_type';
EXEC sp_addextendedproperty N'MS_Description', N'模型服务基础地址', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'base_url';
EXEC sp_addextendedproperty N'MS_Description', N'密钥中心引用', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'secret_ref';
EXEC sp_addextendedproperty N'MS_Description', N'状态', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_provider', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_provider_tenant_status' AND object_id = OBJECT_ID(N'ai_provider'))
CREATE INDEX [idx_ai_provider_tenant_status] ON [ai_provider] ([tenant_id], [status]);

IF OBJECT_ID(N'ai_model_config', N'U') IS NULL
BEGIN
CREATE TABLE [ai_model_config] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [provider_id] BIGINT NOT NULL,
    [model_code] NVARCHAR(128) NOT NULL,
    [model_name] NVARCHAR(128) NOT NULL,
    [model_type] NVARCHAR(32) NOT NULL,
    [context_window] INT NOT NULL,
    [support_stream] BIT NOT NULL DEFAULT 1,
    [support_tool] BIT NOT NULL DEFAULT 0,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_ai_model_tenant_code] UNIQUE ([tenant_id], [model_code]),
    CONSTRAINT [fk_ai_model_provider] FOREIGN KEY ([provider_id]) REFERENCES [ai_provider] ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'AI 模型配置表', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'供应商主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'provider_id';
EXEC sp_addextendedproperty N'MS_Description', N'模型编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'model_code';
EXEC sp_addextendedproperty N'MS_Description', N'模型名称', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'model_name';
EXEC sp_addextendedproperty N'MS_Description', N'模型类型', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'model_type';
EXEC sp_addextendedproperty N'MS_Description', N'上下文长度', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'context_window';
EXEC sp_addextendedproperty N'MS_Description', N'是否支持流式输出', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'support_stream';
EXEC sp_addextendedproperty N'MS_Description', N'是否支持工具调用', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'support_tool';
EXEC sp_addextendedproperty N'MS_Description', N'状态', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_model_config', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_model_tenant_provider' AND object_id = OBJECT_ID(N'ai_model_config'))
CREATE INDEX [idx_ai_model_tenant_provider] ON [ai_model_config] ([tenant_id], [provider_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_model_tenant_status' AND object_id = OBJECT_ID(N'ai_model_config'))
CREATE INDEX [idx_ai_model_tenant_status] ON [ai_model_config] ([tenant_id], [status]);

IF OBJECT_ID(N'ai_app', N'U') IS NULL
BEGIN
CREATE TABLE [ai_app] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [app_name] NVARCHAR(128) NOT NULL,
    [default_model_id] BIGINT NOT NULL,
    [system_prompt] NVARCHAR(MAX) NOT NULL,
    [daily_token_quota] INT NOT NULL DEFAULT 100000,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_ai_app_tenant_code] UNIQUE ([tenant_id], [app_code]),
    CONSTRAINT [fk_ai_app_default_model] FOREIGN KEY ([default_model_id]) REFERENCES [ai_model_config] ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'AI 应用接入表', N'SCHEMA', N'dbo', N'TABLE', N'ai_app';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'AI 应用编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'AI 应用名称', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'app_name';
EXEC sp_addextendedproperty N'MS_Description', N'默认模型配置主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'default_model_id';
EXEC sp_addextendedproperty N'MS_Description', N'系统提示词', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'system_prompt';
EXEC sp_addextendedproperty N'MS_Description', N'每日令牌额度', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'daily_token_quota';
EXEC sp_addextendedproperty N'MS_Description', N'状态', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_app', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_app_tenant_status' AND object_id = OBJECT_ID(N'ai_app'))
CREATE INDEX [idx_ai_app_tenant_status] ON [ai_app] ([tenant_id], [status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_app_default_model' AND object_id = OBJECT_ID(N'ai_app'))
CREATE INDEX [idx_ai_app_default_model] ON [ai_app] ([default_model_id]);

IF OBJECT_ID(N'ai_prompt_template', N'U') IS NULL
BEGIN
CREATE TABLE [ai_prompt_template] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [prompt_code] NVARCHAR(64) NOT NULL,
    [prompt_name] NVARCHAR(128) NOT NULL,
    [version] NVARCHAR(32) NOT NULL,
    [template_content] NVARCHAR(MAX) NOT NULL,
    [variables] NVARCHAR(1000) DEFAULT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'draft',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_ai_prompt_tenant_code_version] UNIQUE ([tenant_id], [prompt_code], [version])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'AI 提示词模板表', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'提示词编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'prompt_code';
EXEC sp_addextendedproperty N'MS_Description', N'提示词名称', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'prompt_name';
EXEC sp_addextendedproperty N'MS_Description', N'版本号', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'模板内容', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'template_content';
EXEC sp_addextendedproperty N'MS_Description', N'变量清单', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'variables';
EXEC sp_addextendedproperty N'MS_Description', N'状态', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_prompt_template', N'COLUMN', N'updated_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_prompt_tenant_status' AND object_id = OBJECT_ID(N'ai_prompt_template'))
CREATE INDEX [idx_ai_prompt_tenant_status] ON [ai_prompt_template] ([tenant_id], [status]);

IF OBJECT_ID(N'ai_invocation_audit', N'U') IS NULL
BEGIN
CREATE TABLE [ai_invocation_audit] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [app_code] NVARCHAR(64) NOT NULL,
    [provider_id] BIGINT NOT NULL,
    [model_id] BIGINT NOT NULL,
    [invocation_type] NVARCHAR(32) NOT NULL,
    [prompt_tokens] INT NOT NULL DEFAULT 0,
    [completion_tokens] INT NOT NULL DEFAULT 0,
    [total_tokens] INT NOT NULL DEFAULT 0,
    [latency_ms] BIGINT NOT NULL DEFAULT 0,
    [status] NVARCHAR(32) NOT NULL,
    [error_message] NVARCHAR(1000) DEFAULT NULL,
    [trace_id] NVARCHAR(128) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id]),
    CONSTRAINT [fk_ai_audit_provider] FOREIGN KEY ([provider_id]) REFERENCES [ai_provider] ([id]),
    CONSTRAINT [fk_ai_audit_model] FOREIGN KEY ([model_id]) REFERENCES [ai_model_config] ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'AI 调用审计表', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'AI 应用编码', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'app_code';
EXEC sp_addextendedproperty N'MS_Description', N'供应商主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'provider_id';
EXEC sp_addextendedproperty N'MS_Description', N'模型配置主键', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'model_id';
EXEC sp_addextendedproperty N'MS_Description', N'调用类型', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'invocation_type';
EXEC sp_addextendedproperty N'MS_Description', N'提示词令牌数', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'prompt_tokens';
EXEC sp_addextendedproperty N'MS_Description', N'输出令牌数', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'completion_tokens';
EXEC sp_addextendedproperty N'MS_Description', N'总令牌数', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'total_tokens';
EXEC sp_addextendedproperty N'MS_Description', N'调用耗时毫秒', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'latency_ms';
EXEC sp_addextendedproperty N'MS_Description', N'调用状态', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'错误消息', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'error_message';
EXEC sp_addextendedproperty N'MS_Description', N'链路追踪编号', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'trace_id';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'ai_invocation_audit', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_audit_tenant_app_time' AND object_id = OBJECT_ID(N'ai_invocation_audit'))
CREATE INDEX [idx_ai_audit_tenant_app_time] ON [ai_invocation_audit] ([tenant_id], [app_code], [created_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_audit_tenant_model_time' AND object_id = OBJECT_ID(N'ai_invocation_audit'))
CREATE INDEX [idx_ai_audit_tenant_model_time] ON [ai_invocation_audit] ([tenant_id], [model_id], [created_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_ai_audit_trace' AND object_id = OBJECT_ID(N'ai_invocation_audit'))
CREATE INDEX [idx_ai_audit_trace] ON [ai_invocation_audit] ([trace_id]);

-- ============================================================
-- 8. 消息中心表
-- 来源：zhyc-base-server/zhyc-module-message/src/main/resources/db/V1__message_core.sql
-- ============================================================
IF OBJECT_ID(N'msg_template', N'U') IS NULL
BEGIN
CREATE TABLE [msg_template] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [template_code] NVARCHAR(64) NOT NULL,
    [template_name] NVARCHAR(128) NOT NULL,
    [channel_type] NVARCHAR(32) NOT NULL,
    [title_template] NVARCHAR(255) NOT NULL,
    [content_template] NVARCHAR(MAX) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_by] BIGINT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_by] BIGINT NULL,
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_msg_template_tenant_code] UNIQUE ([tenant_id], [template_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'消息模板表', N'SCHEMA', N'dbo', N'TABLE', N'msg_template';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'模板编码', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'template_code';
EXEC sp_addextendedproperty N'MS_Description', N'模板名称', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'template_name';
EXEC sp_addextendedproperty N'MS_Description', N'消息通道类型', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'channel_type';
EXEC sp_addextendedproperty N'MS_Description', N'标题模板', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'title_template';
EXEC sp_addextendedproperty N'MS_Description', N'内容模板', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'content_template';
EXEC sp_addextendedproperty N'MS_Description', N'模板状态', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'创建人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'created_by';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'updated_by';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'msg_template', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_msg_template_tenant_status' AND object_id = OBJECT_ID(N'msg_template'))
CREATE INDEX [idx_msg_template_tenant_status] ON [msg_template] ([tenant_id], [status]);

IF OBJECT_ID(N'msg_message', N'U') IS NULL
BEGIN
CREATE TABLE [msg_message] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [message_code] NVARCHAR(64) NOT NULL,
    [receiver_id] BIGINT NOT NULL,
    [receiver_name] NVARCHAR(128) NULL,
    [message_type] NVARCHAR(32) NOT NULL,
    [title] NVARCHAR(255) NOT NULL,
    [content] NVARCHAR(MAX) NOT NULL,
    [read_flag] SMALLINT NOT NULL DEFAULT 0,
    [read_at] DATETIME2 NULL,
    [created_by] BIGINT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_msg_message_tenant_code] UNIQUE ([tenant_id], [message_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'站内消息表', N'SCHEMA', N'dbo', N'TABLE', N'msg_message';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'消息编码', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'message_code';
EXEC sp_addextendedproperty N'MS_Description', N'接收人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'receiver_id';
EXEC sp_addextendedproperty N'MS_Description', N'接收人名称', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'receiver_name';
EXEC sp_addextendedproperty N'MS_Description', N'消息类型', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'message_type';
EXEC sp_addextendedproperty N'MS_Description', N'消息标题', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'title';
EXEC sp_addextendedproperty N'MS_Description', N'消息内容', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'content';
EXEC sp_addextendedproperty N'MS_Description', N'是否已读，0 未读，1 已读', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'read_flag';
EXEC sp_addextendedproperty N'MS_Description', N'阅读时间', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'read_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'created_by';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'msg_message', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_msg_message_tenant_receiver' AND object_id = OBJECT_ID(N'msg_message'))
CREATE INDEX [idx_msg_message_tenant_receiver] ON [msg_message] ([tenant_id], [receiver_id], [read_flag], [created_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_msg_message_tenant_type' AND object_id = OBJECT_ID(N'msg_message'))
CREATE INDEX [idx_msg_message_tenant_type] ON [msg_message] ([tenant_id], [message_type]);

-- ============================================================
-- 9. 文件中心表
-- 来源：zhyc-base-server/zhyc-module-file/src/main/resources/db/V1__file_core.sql
-- ============================================================
IF OBJECT_ID(N'file_storage_config', N'U') IS NULL
BEGIN
CREATE TABLE [file_storage_config] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [storage_code] NVARCHAR(64) NOT NULL,
    [storage_name] NVARCHAR(128) NOT NULL,
    [storage_type] NVARCHAR(32) NOT NULL,
    [endpoint] NVARCHAR(255) NOT NULL,
    [status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [default_flag] SMALLINT NOT NULL DEFAULT 0,
    [created_by] BIGINT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_by] BIGINT NULL,
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_file_storage_config_tenant_code] UNIQUE ([tenant_id], [storage_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'文件存储配置表', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'存储配置编码', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'storage_code';
EXEC sp_addextendedproperty N'MS_Description', N'存储配置名称', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'storage_name';
EXEC sp_addextendedproperty N'MS_Description', N'存储类型，例如 local、s3、minio、oss', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'storage_type';
EXEC sp_addextendedproperty N'MS_Description', N'存储访问端点或本地根路径', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'endpoint';
EXEC sp_addextendedproperty N'MS_Description', N'配置状态', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'status';
EXEC sp_addextendedproperty N'MS_Description', N'是否默认存储配置', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'default_flag';
EXEC sp_addextendedproperty N'MS_Description', N'创建人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'created_by';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'updated_by';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'file_storage_config', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_file_storage_config_tenant_status' AND object_id = OBJECT_ID(N'file_storage_config'))
CREATE INDEX [idx_file_storage_config_tenant_status] ON [file_storage_config] ([tenant_id], [status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_file_storage_config_tenant_default' AND object_id = OBJECT_ID(N'file_storage_config'))
CREATE INDEX [idx_file_storage_config_tenant_default] ON [file_storage_config] ([tenant_id], [default_flag]);

IF OBJECT_ID(N'file_object', N'U') IS NULL
BEGIN
CREATE TABLE [file_object] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [file_code] NVARCHAR(64) NOT NULL,
    [storage_code] NVARCHAR(64) NOT NULL,
    [original_name] NVARCHAR(255) NOT NULL,
    [content_type] NVARCHAR(128) NOT NULL,
    [file_size] BIGINT NOT NULL DEFAULT 0,
    [object_key] NVARCHAR(500) NOT NULL,
    [file_status] NVARCHAR(32) NOT NULL DEFAULT 'stored',
    [uploader_id] BIGINT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_file_object_tenant_code] UNIQUE ([tenant_id], [file_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'文件对象表', N'SCHEMA', N'dbo', N'TABLE', N'file_object';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'文件业务编码', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'file_code';
EXEC sp_addextendedproperty N'MS_Description', N'存储配置编码', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'storage_code';
EXEC sp_addextendedproperty N'MS_Description', N'原始文件名', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'original_name';
EXEC sp_addextendedproperty N'MS_Description', N'文件内容类型', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'content_type';
EXEC sp_addextendedproperty N'MS_Description', N'文件大小，单位字节', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'file_size';
EXEC sp_addextendedproperty N'MS_Description', N'存储对象键或相对路径', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'object_key';
EXEC sp_addextendedproperty N'MS_Description', N'文件状态', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'file_status';
EXEC sp_addextendedproperty N'MS_Description', N'上传人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'uploader_id';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'file_object', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_file_object_tenant_storage' AND object_id = OBJECT_ID(N'file_object'))
CREATE INDEX [idx_file_object_tenant_storage] ON [file_object] ([tenant_id], [storage_code]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_file_object_tenant_created' AND object_id = OBJECT_ID(N'file_object'))
CREATE INDEX [idx_file_object_tenant_created] ON [file_object] ([tenant_id], [created_at]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_file_object_tenant_status' AND object_id = OBJECT_ID(N'file_object'))
CREATE INDEX [idx_file_object_tenant_status] ON [file_object] ([tenant_id], [file_status]);

IF OBJECT_ID(N'file_preview_log', N'U') IS NULL
BEGIN
CREATE TABLE [file_preview_log] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [file_code] NVARCHAR(64) NOT NULL,
    [preview_type] NVARCHAR(32) NOT NULL,
    [preview_url] NVARCHAR(512) NOT NULL,
    [result] NVARCHAR(32) NOT NULL,
    [cost_ms] BIGINT NOT NULL DEFAULT 0,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'文件预览日志表', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log';
EXEC sp_addextendedproperty N'MS_Description', N'文件预览日志主键', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'文件业务编码', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log', N'COLUMN', N'file_code';
EXEC sp_addextendedproperty N'MS_Description', N'预览类型', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log', N'COLUMN', N'preview_type';
EXEC sp_addextendedproperty N'MS_Description', N'预览访问地址', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log', N'COLUMN', N'preview_url';
EXEC sp_addextendedproperty N'MS_Description', N'预览结果', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log', N'COLUMN', N'result';
EXEC sp_addextendedproperty N'MS_Description', N'预览耗时毫秒', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log', N'COLUMN', N'cost_ms';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'file_preview_log', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_file_preview_log_tenant_file' AND object_id = OBJECT_ID(N'file_preview_log'))
CREATE INDEX [idx_file_preview_log_tenant_file] ON [file_preview_log] ([tenant_id], [file_code]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_file_preview_log_created_at' AND object_id = OBJECT_ID(N'file_preview_log'))
CREATE INDEX [idx_file_preview_log_created_at] ON [file_preview_log] ([created_at]);

-- ============================================================
-- 10. 内容管理表
-- 来源：zhyc-base-server/zhyc-module-cms/src/main/resources/db/V1__cms_core.sql
-- ============================================================
IF OBJECT_ID(N'cms_channel', N'U') IS NULL
BEGIN
CREATE TABLE [cms_channel] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [parent_id] BIGINT NULL,
    [channel_code] NVARCHAR(64) NOT NULL,
    [channel_name] NVARCHAR(128) NOT NULL,
    [sort_order] INT NOT NULL DEFAULT 0,
    [channel_status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_cms_channel_tenant_code] UNIQUE ([tenant_id], [channel_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'内容栏目', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel';
EXEC sp_addextendedproperty N'MS_Description', N'内容栏目主键', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'父栏目主键', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'parent_id';
EXEC sp_addextendedproperty N'MS_Description', N'栏目编码', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'channel_code';
EXEC sp_addextendedproperty N'MS_Description', N'栏目名称', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'channel_name';
EXEC sp_addextendedproperty N'MS_Description', N'排序号', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'sort_order';
EXEC sp_addextendedproperty N'MS_Description', N'栏目状态', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'channel_status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识', N'SCHEMA', N'dbo', N'TABLE', N'cms_channel', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_cms_channel_tenant_status' AND object_id = OBJECT_ID(N'cms_channel'))
CREATE INDEX [idx_cms_channel_tenant_status] ON [cms_channel] ([tenant_id], [channel_status]);

IF OBJECT_ID(N'cms_content', N'U') IS NULL
BEGIN
CREATE TABLE [cms_content] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [channel_code] NVARCHAR(64) NOT NULL,
    [title] NVARCHAR(200) NOT NULL,
    [summary] NVARCHAR(500) NULL,
    [body_content] NVARCHAR(MAX) NULL,
    [content_status] NVARCHAR(32) NOT NULL DEFAULT 'draft',
    [author_id] BIGINT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'内容文章', N'SCHEMA', N'dbo', N'TABLE', N'cms_content';
EXEC sp_addextendedproperty N'MS_Description', N'内容文章主键', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'栏目编码', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'channel_code';
EXEC sp_addextendedproperty N'MS_Description', N'文章标题', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'title';
EXEC sp_addextendedproperty N'MS_Description', N'文章摘要', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'summary';
EXEC sp_addextendedproperty N'MS_Description', N'文章正文', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'body_content';
EXEC sp_addextendedproperty N'MS_Description', N'文章状态', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'content_status';
EXEC sp_addextendedproperty N'MS_Description', N'作者用户主键', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'author_id';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识', N'SCHEMA', N'dbo', N'TABLE', N'cms_content', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_cms_content_tenant_channel' AND object_id = OBJECT_ID(N'cms_content'))
CREATE INDEX [idx_cms_content_tenant_channel] ON [cms_content] ([tenant_id], [channel_code]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_cms_content_tenant_status' AND object_id = OBJECT_ID(N'cms_content'))
CREATE INDEX [idx_cms_content_tenant_status] ON [cms_content] ([tenant_id], [content_status]);

-- ============================================================
-- 11. 在线作业表
-- 来源：zhyc-base-server/zhyc-module-job/src/main/resources/db/V1__job_core.sql
-- ============================================================
IF OBJECT_ID(N'job_task', N'U') IS NULL
BEGIN
CREATE TABLE [job_task] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [job_code] NVARCHAR(64) NOT NULL,
    [job_name] NVARCHAR(128) NOT NULL,
    [cron_expression] NVARCHAR(128) NOT NULL,
    [handler_name] NVARCHAR(128) NOT NULL,
    [job_description] NVARCHAR(500) NULL,
    [job_status] NVARCHAR(32) NOT NULL DEFAULT 'disabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_job_task_tenant_code] UNIQUE ([tenant_id], [job_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'在线作业任务', N'SCHEMA', N'dbo', N'TABLE', N'job_task';
EXEC sp_addextendedproperty N'MS_Description', N'作业任务主键', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'作业任务编码', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'job_code';
EXEC sp_addextendedproperty N'MS_Description', N'作业任务名称', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'job_name';
EXEC sp_addextendedproperty N'MS_Description', N'Cron 表达式', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'cron_expression';
EXEC sp_addextendedproperty N'MS_Description', N'任务处理器名称', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'handler_name';
EXEC sp_addextendedproperty N'MS_Description', N'作业任务说明', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'job_description';
EXEC sp_addextendedproperty N'MS_Description', N'作业状态', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'job_status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识', N'SCHEMA', N'dbo', N'TABLE', N'job_task', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_job_task_tenant_status' AND object_id = OBJECT_ID(N'job_task'))
CREATE INDEX [idx_job_task_tenant_status] ON [job_task] ([tenant_id], [job_status]);

IF OBJECT_ID(N'job_task_log', N'U') IS NULL
BEGIN
CREATE TABLE [job_task_log] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [job_id] BIGINT NOT NULL,
    [trigger_type] NVARCHAR(32) NOT NULL,
    [start_at] DATETIME2 NOT NULL,
    [end_at] DATETIME2 NULL,
    [result] NVARCHAR(32) NOT NULL,
    [error_message] NVARCHAR(1000) NULL,
    [operator_id] BIGINT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'作业执行日志', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log';
EXEC sp_addextendedproperty N'MS_Description', N'作业执行日志主键', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'作业任务主键', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'job_id';
EXEC sp_addextendedproperty N'MS_Description', N'触发类型', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'trigger_type';
EXEC sp_addextendedproperty N'MS_Description', N'开始时间', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'start_at';
EXEC sp_addextendedproperty N'MS_Description', N'结束时间', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'end_at';
EXEC sp_addextendedproperty N'MS_Description', N'执行结果', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'result';
EXEC sp_addextendedproperty N'MS_Description', N'错误信息', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'error_message';
EXEC sp_addextendedproperty N'MS_Description', N'操作人用户主键', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'operator_id';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'job_task_log', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_job_task_log_tenant_job' AND object_id = OBJECT_ID(N'job_task_log'))
CREATE INDEX [idx_job_task_log_tenant_job] ON [job_task_log] ([tenant_id], [job_id], [start_at]);

-- ============================================================
-- 12. 全文检索表
-- 来源：zhyc-base-server/zhyc-module-search/src/main/resources/db/V1__search_core.sql
-- ============================================================
IF OBJECT_ID(N'search_index_config', N'U') IS NULL
BEGIN
CREATE TABLE [search_index_config] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [index_code] NVARCHAR(128) NOT NULL,
    [index_name] NVARCHAR(128) NOT NULL,
    [source_table] NVARCHAR(128) NOT NULL,
    [search_fields] NVARCHAR(512) NOT NULL,
    [filter_fields] NVARCHAR(512) DEFAULT NULL,
    [index_status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [remark] NVARCHAR(512) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT [uk_search_index_config_tenant_code] UNIQUE ([tenant_id], [index_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'全文检索索引配置', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config';
EXEC sp_addextendedproperty N'MS_Description', N'索引配置主键', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'索引编码', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'index_code';
EXEC sp_addextendedproperty N'MS_Description', N'索引名称', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'index_name';
EXEC sp_addextendedproperty N'MS_Description', N'数据来源表名', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'source_table';
EXEC sp_addextendedproperty N'MS_Description', N'可检索字段列表，逗号分隔', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'search_fields';
EXEC sp_addextendedproperty N'MS_Description', N'可过滤字段列表，逗号分隔', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'filter_fields';
EXEC sp_addextendedproperty N'MS_Description', N'索引状态', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'index_status';
EXEC sp_addextendedproperty N'MS_Description', N'配置备注', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'remark';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标记', N'SCHEMA', N'dbo', N'TABLE', N'search_index_config', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_search_index_config_tenant_status' AND object_id = OBJECT_ID(N'search_index_config'))
CREATE INDEX [idx_search_index_config_tenant_status] ON [search_index_config] ([tenant_id], [index_status]);

IF OBJECT_ID(N'search_rebuild_task', N'U') IS NULL
BEGIN
CREATE TABLE [search_rebuild_task] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [index_code] NVARCHAR(128) NOT NULL,
    [task_status] NVARCHAR(32) NOT NULL DEFAULT 'pending',
    [trigger_type] NVARCHAR(32) NOT NULL DEFAULT 'manual',
    [started_at] DATETIME2 DEFAULT NULL,
    [finished_at] DATETIME2 DEFAULT NULL,
    [error_message] NVARCHAR(1024) DEFAULT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'全文检索索引重建任务', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task';
EXEC sp_addextendedproperty N'MS_Description', N'重建任务主键', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'索引编码', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'index_code';
EXEC sp_addextendedproperty N'MS_Description', N'任务状态', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'task_status';
EXEC sp_addextendedproperty N'MS_Description', N'触发类型', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'trigger_type';
EXEC sp_addextendedproperty N'MS_Description', N'任务开始时间', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'started_at';
EXEC sp_addextendedproperty N'MS_Description', N'任务完成时间', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'finished_at';
EXEC sp_addextendedproperty N'MS_Description', N'失败错误信息', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'error_message';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标记', N'SCHEMA', N'dbo', N'TABLE', N'search_rebuild_task', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_search_rebuild_task_tenant_index' AND object_id = OBJECT_ID(N'search_rebuild_task'))
CREATE INDEX [idx_search_rebuild_task_tenant_index] ON [search_rebuild_task] ([tenant_id], [index_code]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_search_rebuild_task_status' AND object_id = OBJECT_ID(N'search_rebuild_task'))
CREATE INDEX [idx_search_rebuild_task_status] ON [search_rebuild_task] ([task_status]);

IF OBJECT_ID(N'search_query_log', N'U') IS NULL
BEGIN
CREATE TABLE [search_query_log] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [index_code] NVARCHAR(128) NOT NULL,
    [keyword] NVARCHAR(256) NOT NULL,
    [result_count] INT NOT NULL DEFAULT 0,
    [cost_ms] BIGINT NOT NULL DEFAULT 0,
    [query_status] NVARCHAR(32) NOT NULL DEFAULT 'success',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'全文检索查询日志', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log';
EXEC sp_addextendedproperty N'MS_Description', N'查询日志主键', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'索引编码', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log', N'COLUMN', N'index_code';
EXEC sp_addextendedproperty N'MS_Description', N'查询关键词', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log', N'COLUMN', N'keyword';
EXEC sp_addextendedproperty N'MS_Description', N'返回结果数量', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log', N'COLUMN', N'result_count';
EXEC sp_addextendedproperty N'MS_Description', N'查询耗时毫秒', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log', N'COLUMN', N'cost_ms';
EXEC sp_addextendedproperty N'MS_Description', N'查询状态', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log', N'COLUMN', N'query_status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'search_query_log', N'COLUMN', N'created_at';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_search_query_log_tenant_index' AND object_id = OBJECT_ID(N'search_query_log'))
CREATE INDEX [idx_search_query_log_tenant_index] ON [search_query_log] ([tenant_id], [index_code]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_search_query_log_created_at' AND object_id = OBJECT_ID(N'search_query_log'))
CREATE INDEX [idx_search_query_log_created_at] ON [search_query_log] ([created_at]);

-- ============================================================
-- 13. 可视化大屏表
-- 来源：zhyc-base-server/zhyc-module-visual/src/main/resources/db/V1__visual_core.sql
-- ============================================================
IF OBJECT_ID(N'visual_dataset', N'U') IS NULL
BEGIN
CREATE TABLE [visual_dataset] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [dataset_code] NVARCHAR(64) NOT NULL,
    [dataset_name] NVARCHAR(128) NOT NULL,
    [datasource_code] NVARCHAR(64) NOT NULL,
    [sql_text] NVARCHAR(MAX) NOT NULL,
    [dataset_status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_visual_dataset_tenant_code] UNIQUE ([tenant_id], [dataset_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'可视化数据集表', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码，用于共享表模式数据隔离', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'数据集编码，租户内唯一', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'dataset_code';
EXEC sp_addextendedproperty N'MS_Description', N'数据集名称', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'dataset_name';
EXEC sp_addextendedproperty N'MS_Description', N'数据源编码，对应低代码数据源或默认数据源', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'datasource_code';
EXEC sp_addextendedproperty N'MS_Description', N'查询 SQL，由数据集执行器统一校验后执行', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'sql_text';
EXEC sp_addextendedproperty N'MS_Description', N'数据集状态', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'dataset_status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识', N'SCHEMA', N'dbo', N'TABLE', N'visual_dataset', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_visual_dataset_tenant_status' AND object_id = OBJECT_ID(N'visual_dataset'))
CREATE INDEX [idx_visual_dataset_tenant_status] ON [visual_dataset] ([tenant_id], [dataset_status]);

IF OBJECT_ID(N'visual_report', N'U') IS NULL
BEGIN
CREATE TABLE [visual_report] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [report_code] NVARCHAR(64) NOT NULL,
    [report_name] NVARCHAR(128) NOT NULL,
    [dataset_code] NVARCHAR(64) NOT NULL,
    [chart_type] NVARCHAR(32) NOT NULL DEFAULT 'table',
    [config_json] NVARCHAR(MAX) NOT NULL,
    [report_status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_visual_report_tenant_code] UNIQUE ([tenant_id], [report_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'可视化报表表', N'SCHEMA', N'dbo', N'TABLE', N'visual_report';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码，用于共享表模式数据隔离', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'报表编码，租户内唯一', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'report_code';
EXEC sp_addextendedproperty N'MS_Description', N'报表名称', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'report_name';
EXEC sp_addextendedproperty N'MS_Description', N'数据集编码，指向同租户数据集', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'dataset_code';
EXEC sp_addextendedproperty N'MS_Description', N'图表类型', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'chart_type';
EXEC sp_addextendedproperty N'MS_Description', N'图表配置 JSON', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'config_json';
EXEC sp_addextendedproperty N'MS_Description', N'报表状态', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'report_status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识', N'SCHEMA', N'dbo', N'TABLE', N'visual_report', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_visual_report_tenant_status' AND object_id = OBJECT_ID(N'visual_report'))
CREATE INDEX [idx_visual_report_tenant_status] ON [visual_report] ([tenant_id], [report_status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_visual_report_tenant_dataset' AND object_id = OBJECT_ID(N'visual_report'))
CREATE INDEX [idx_visual_report_tenant_dataset] ON [visual_report] ([tenant_id], [dataset_code]);

IF OBJECT_ID(N'visual_screen', N'U') IS NULL
BEGIN
CREATE TABLE [visual_screen] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [screen_code] NVARCHAR(64) NOT NULL,
    [screen_name] NVARCHAR(128) NOT NULL,
    [layout_json] NVARCHAR(MAX) NOT NULL,
    [screen_status] NVARCHAR(32) NOT NULL DEFAULT 'draft',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_visual_screen_tenant_code] UNIQUE ([tenant_id], [screen_code])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'可视化大屏表', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码，用于共享表模式数据隔离', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'大屏编码，租户内唯一', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'screen_code';
EXEC sp_addextendedproperty N'MS_Description', N'大屏名称', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'screen_name';
EXEC sp_addextendedproperty N'MS_Description', N'大屏布局 JSON，保存组件位置、尺寸和报表编码', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'layout_json';
EXEC sp_addextendedproperty N'MS_Description', N'大屏状态', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'screen_status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识', N'SCHEMA', N'dbo', N'TABLE', N'visual_screen', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_visual_screen_tenant_status' AND object_id = OBJECT_ID(N'visual_screen'))
CREATE INDEX [idx_visual_screen_tenant_status] ON [visual_screen] ([tenant_id], [screen_status]);

-- ============================================================
-- 14. 国际化资源表
-- 来源：zhyc-base-server/zhyc-module-i18n/src/main/resources/db/V1__i18n_core.sql
-- ============================================================
IF OBJECT_ID(N'i18n_message', N'U') IS NULL
BEGIN
CREATE TABLE [i18n_message] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [locale] NVARCHAR(32) NOT NULL,
    [message_key] NVARCHAR(190) NOT NULL,
    [message_value] NVARCHAR(1000) NOT NULL,
    [message_status] NVARCHAR(32) NOT NULL DEFAULT 'enabled',
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_i18n_message_tenant_locale_key] UNIQUE ([tenant_id], [locale], [message_key])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'国际化词条表', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message';
EXEC sp_addextendedproperty N'MS_Description', N'主键', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码，用于共享表模式数据隔离', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'语言标识，例如 zh-CN、en-US', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'locale';
EXEC sp_addextendedproperty N'MS_Description', N'词条键', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'message_key';
EXEC sp_addextendedproperty N'MS_Description', N'词条值', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'message_value';
EXEC sp_addextendedproperty N'MS_Description', N'词条状态', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'message_status';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识', N'SCHEMA', N'dbo', N'TABLE', N'i18n_message', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_i18n_message_tenant_locale_status' AND object_id = OBJECT_ID(N'i18n_message'))
CREATE INDEX [idx_i18n_message_tenant_locale_status] ON [i18n_message] ([tenant_id], [locale], [message_status]);

-- ============================================================
-- 15. 采购样板业务表
-- 来源：zhyc-base-server/zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql
-- ============================================================
IF OBJECT_ID(N'pur_request', N'U') IS NULL
BEGIN
CREATE TABLE [pur_request] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [request_no] NVARCHAR(64) NOT NULL,
    [request_title] NVARCHAR(128) NOT NULL,
    [applicant_id] BIGINT NOT NULL,
    [org_id] BIGINT NOT NULL,
    [total_amount] DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    [request_reason] NVARCHAR(1000) NULL,
    [process_status] NVARCHAR(32) NOT NULL,
    [process_instance_id] NVARCHAR(128) NULL,
    [submitted_at] DATETIME2 NULL,
    [created_by] BIGINT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_by] BIGINT NULL,
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_pur_request_tenant_no] UNIQUE ([tenant_id], [request_no])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'采购申请主表', N'SCHEMA', N'dbo', N'TABLE', N'pur_request';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'采购申请单号', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'request_no';
EXEC sp_addextendedproperty N'MS_Description', N'采购申请标题', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'request_title';
EXEC sp_addextendedproperty N'MS_Description', N'申请人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'applicant_id';
EXEC sp_addextendedproperty N'MS_Description', N'申请部门 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'org_id';
EXEC sp_addextendedproperty N'MS_Description', N'采购申请总金额', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'total_amount';
EXEC sp_addextendedproperty N'MS_Description', N'采购申请原因', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'request_reason';
EXEC sp_addextendedproperty N'MS_Description', N'流程状态', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'process_status';
EXEC sp_addextendedproperty N'MS_Description', N'流程实例 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'process_instance_id';
EXEC sp_addextendedproperty N'MS_Description', N'提交审批时间', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'submitted_at';
EXEC sp_addextendedproperty N'MS_Description', N'创建人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'created_by';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'updated_by';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'pur_request', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_pur_request_tenant_applicant' AND object_id = OBJECT_ID(N'pur_request'))
CREATE INDEX [idx_pur_request_tenant_applicant] ON [pur_request] ([tenant_id], [applicant_id]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_pur_request_tenant_status' AND object_id = OBJECT_ID(N'pur_request'))
CREATE INDEX [idx_pur_request_tenant_status] ON [pur_request] ([tenant_id], [process_status]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_pur_request_tenant_process' AND object_id = OBJECT_ID(N'pur_request'))
CREATE INDEX [idx_pur_request_tenant_process] ON [pur_request] ([tenant_id], [process_instance_id]);

IF OBJECT_ID(N'pur_order', N'U') IS NULL
BEGIN
CREATE TABLE [pur_order] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [order_no] NVARCHAR(64) NOT NULL,
    [request_no] NVARCHAR(64) NOT NULL,
    [supplier_id] BIGINT NOT NULL,
    [buyer_id] BIGINT NOT NULL,
    [total_amount] DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    [order_status] NVARCHAR(32) NOT NULL,
    [created_by] BIGINT NULL,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [updated_by] BIGINT NULL,
    [updated_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    [version] BIGINT NOT NULL DEFAULT 0,
    [remark] NVARCHAR(500) NULL,
    PRIMARY KEY ([id]),
    CONSTRAINT [uk_pur_order_tenant_no] UNIQUE ([tenant_id], [order_no])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'采购订单主表', N'SCHEMA', N'dbo', N'TABLE', N'pur_order';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'采购订单号', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'order_no';
EXEC sp_addextendedproperty N'MS_Description', N'采购申请单号', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'request_no';
EXEC sp_addextendedproperty N'MS_Description', N'供应商 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'supplier_id';
EXEC sp_addextendedproperty N'MS_Description', N'采购员用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'buyer_id';
EXEC sp_addextendedproperty N'MS_Description', N'采购订单总金额', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'total_amount';
EXEC sp_addextendedproperty N'MS_Description', N'订单状态', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'order_status';
EXEC sp_addextendedproperty N'MS_Description', N'创建人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'created_by';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'更新人用户 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'updated_by';
EXEC sp_addextendedproperty N'MS_Description', N'更新时间', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'updated_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'deleted';
EXEC sp_addextendedproperty N'MS_Description', N'乐观锁版本号', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'version';
EXEC sp_addextendedproperty N'MS_Description', N'备注', N'SCHEMA', N'dbo', N'TABLE', N'pur_order', N'COLUMN', N'remark';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_pur_order_tenant_request' AND object_id = OBJECT_ID(N'pur_order'))
CREATE INDEX [idx_pur_order_tenant_request] ON [pur_order] ([tenant_id], [request_no]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_pur_order_tenant_status' AND object_id = OBJECT_ID(N'pur_order'))
CREATE INDEX [idx_pur_order_tenant_status] ON [pur_order] ([tenant_id], [order_status]);

IF OBJECT_ID(N'pur_order_item', N'U') IS NULL
BEGIN
CREATE TABLE [pur_order_item] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [tenant_id] NVARCHAR(64) NOT NULL,
    [order_no] NVARCHAR(64) NOT NULL,
    [item_name] NVARCHAR(128) NOT NULL,
    [quantity] DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    [unit_price] DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    [amount] DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [deleted] SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY ([id])
);
END;
GO
EXEC sp_addextendedproperty N'MS_Description', N'采购订单明细表', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item';
EXEC sp_addextendedproperty N'MS_Description', N'主键 ID', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'id';
EXEC sp_addextendedproperty N'MS_Description', N'租户业务编码', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'tenant_id';
EXEC sp_addextendedproperty N'MS_Description', N'采购订单号', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'order_no';
EXEC sp_addextendedproperty N'MS_Description', N'物品名称', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'item_name';
EXEC sp_addextendedproperty N'MS_Description', N'采购数量', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'quantity';
EXEC sp_addextendedproperty N'MS_Description', N'采购单价', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'unit_price';
EXEC sp_addextendedproperty N'MS_Description', N'明细金额', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'amount';
EXEC sp_addextendedproperty N'MS_Description', N'创建时间', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'created_at';
EXEC sp_addextendedproperty N'MS_Description', N'逻辑删除标识，0 未删除，1 已删除', N'SCHEMA', N'dbo', N'TABLE', N'pur_order_item', N'COLUMN', N'deleted';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_pur_order_item_tenant_order' AND object_id = OBJECT_ID(N'pur_order_item'))
CREATE INDEX [idx_pur_order_item_tenant_order] ON [pur_order_item] ([tenant_id], [order_no]);

-- 采购申请状态开放 API 目录注册，用于开放 API 网关运行态路由发现。
-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO openapi_catalog，请按目标数据库语法单独审阅后导入。

-- 采购申请状态开放 API 版本注册，用于开放 API 网关定位后端服务入口。
-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO openapi_version，请按目标数据库语法单独审阅后导入。

-- 采购订单详情开放 API 目录注册，用于开放 API 网关运行态路由发现。
-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO openapi_catalog，请按目标数据库语法单独审阅后导入。

-- 采购订单详情开放 API 版本注册，用于开放 API 网关定位后端服务入口。
-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO openapi_version，请按目标数据库语法单独审阅后导入。

-- 初始化脚本结束。
