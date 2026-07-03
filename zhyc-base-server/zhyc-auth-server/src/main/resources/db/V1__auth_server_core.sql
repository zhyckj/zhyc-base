-- Copyright (c) 2026 众汇云创科技（深圳）有限公司.
-- This file is part of ZHYC and is licensed for non-commercial use only.
-- Commercial use requires a separate written license from the copyright holder.
-- SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial

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
