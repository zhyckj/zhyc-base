/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import { loadRuntimeEnv, resolveProfile } from './lib/profile-runtime-env.mjs';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');
const args = process.argv.slice(2);
const profileMode = args.includes('--profile') || args.includes('--env');
const strictMode = args.includes('--strict');
const exampleMode = args.includes('--example') || !strictMode;
const errors = [];

const requiredKeys = [
  'SPRING_PROFILES_ACTIVE',
  'ZHYC_PLATFORM_DATASOURCE_URL',
  'ZHYC_PLATFORM_DATASOURCE_USERNAME',
  'ZHYC_PLATFORM_DATASOURCE_PASSWORD',
  'ZHYC_PLATFORM_AUTH_JWK_SET_URI',
  'ZHYC_PLATFORM_AUTH_ISSUER',
  'ZHYC_PLATFORM_AUTH_TOKEN_URI',
  'ZHYC_PLATFORM_AUTH_CLIENT_ID',
  'ZHYC_PLATFORM_AUTH_CLIENT_SECRET',
  'ZHYC_AUTH_CLIENT_ID',
  'ZHYC_AUTH_CLIENT_SECRET',
  'ZHYC_AUTH_REDIRECT_URI',
  'ZHYC_AUTH_ISSUER',
  'ZHYC_AUTH_USER_NAME',
  'ZHYC_AUTH_USER_PASSWORD',
  'ZHYC_AUTH_PLATFORM_TENANT_ID',
  'ZHYC_AUTH_PLATFORM_USER_ID',
  'ZHYC_AUTH_PLATFORM_USERNAME',
  'ZHYC_AUTH_DATASOURCE_URL',
  'ZHYC_AUTH_DATASOURCE_USERNAME',
  'ZHYC_AUTH_DATASOURCE_PASSWORD',
  'ZHYC_AUTH_DATASOURCE_DRIVER',
  'ZHYC_AUTH_JWK_PRIVATE_KEY_PEM',
  'ZHYC_AUTH_JWK_PUBLIC_KEY_PEM',
  'ZHYC_AUTH_JWK_KEY_ID',
  'SPRING_DATASOURCE_URL',
  'SPRING_DATASOURCE_USERNAME',
  'SPRING_DATASOURCE_PASSWORD',
  'ZHYC_OPENAPI_OAUTH2_INTROSPECTION_URI',
  'ZHYC_OPENAPI_OAUTH2_CLIENT_ID',
  'ZHYC_OPENAPI_OAUTH2_CLIENT_SECRET',
  'VITE_AUTH_AUTHORIZATION_ENDPOINT',
  'VITE_AUTH_CLIENT_ID',
  'VITE_AUTH_REDIRECT_URI',
  'VITE_AUTH_SCOPE',
];

const profileRequiredKeys = requiredKeys.filter((key) => !key.startsWith('VITE_'));

if (profileMode) {
  const env = loadRuntimeEnv(process.argv, serverRoot);
  assertRequiredKeys(env, profileRequiredKeys);
  assertAuthConsistency(env);
  assertExampleValues(env);
} else {
  const envFile = resolveEnvFile(args, exampleMode);
  if (!existsSync(envFile)) {
    errors.push(`环境变量文件不存在：${envFile}`);
  } else {
    const env = parseEnvFile(envFile);
    assertRequiredKeys(env, requiredKeys);
    assertAuthConsistency(env);
    if (strictMode) {
      assertStrictValues(env);
    } else {
      assertExampleValues(env);
    }
  }
}

if (errors.length > 0) {
  const profile = profileMode ? resolveProfile(process.argv) : '';
  console.error(profileMode ? `首期 Spring Profile 配置检查失败：${profile}` : (strictMode ? '首期本地环境变量严格检查失败。' : '首期本地环境变量样例检查失败。'));
  errors.forEach((error) => console.error(`- ${error}`));
  process.exit(1);
}

console.log(profileMode ? `首期 Spring Profile 配置检查通过：${resolveProfile(process.argv)}` : (strictMode ? '首期本地环境变量严格检查通过。' : '首期本地环境变量样例检查通过。'));

/**
 * 解析待检查的环境变量文件。
 *
 * @param argv 命令行参数
 * @param useExample 是否使用样例文件
 * @returns 环境变量文件绝对路径
 */
function resolveEnvFile(argv, useExample) {
  const fileIndex = argv.indexOf('--file');
  if (fileIndex >= 0 && argv[fileIndex + 1]) {
    return resolve(process.cwd(), argv[fileIndex + 1]);
  }
  if (useExample) {
    return resolve(workspaceRoot, 'docs/release/phase1.env.example');
  }
  return resolve(workspaceRoot, 'docs/release/phase1.env.local');
}

/**
 * 解析 dotenv 风格环境变量文件。
 *
 * @param file 环境变量文件
 * @returns 键值映射
 */
function parseEnvFile(file) {
  const env = new Map();
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  for (const line of lines) {
    const trimmedLine = line.trim();
    if (!trimmedLine || trimmedLine.startsWith('#')) {
      continue;
    }
    const separatorIndex = trimmedLine.indexOf('=');
    if (separatorIndex <= 0) {
      errors.push(`环境变量格式错误：${line}`);
      continue;
    }
    const key = trimmedLine.slice(0, separatorIndex).trim();
    const value = trimQuotes(trimmedLine.slice(separatorIndex + 1).trim());
    env.set(key, value);
  }
  return env;
}

/**
 * 去除环境变量值两侧引号。
 *
 * @param value 原始变量值
 * @returns 去除引号后的变量值
 */
function trimQuotes(value) {
  if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
    return value.slice(1, -1);
  }
  return value;
}

/**
 * 校验必填环境变量是否齐全。
 *
 * @param env 环境变量映射
 */
function assertRequiredKeys(env, keys) {
  for (const key of keys) {
    if (!env.has(key)) {
      errors.push(`缺少环境变量：${key}`);
    }
  }
}

/**
 * 校验认证中心、核心平台和前端认证配置一致性。
 *
 * @param env 环境变量映射
 */
function assertAuthConsistency(env) {
  assertEqual(env, 'ZHYC_PLATFORM_AUTH_CLIENT_ID', 'ZHYC_AUTH_CLIENT_ID', '核心平台与认证中心 clientId 不一致');
  assertEqual(env, 'ZHYC_PLATFORM_AUTH_CLIENT_SECRET', 'ZHYC_AUTH_CLIENT_SECRET', '核心平台与认证中心 clientSecret 不一致');
  assertEqual(env, 'VITE_AUTH_CLIENT_ID', 'ZHYC_AUTH_CLIENT_ID', '后台管理端与认证中心 clientId 不一致');
  assertEqual(env, 'VITE_AUTH_REDIRECT_URI', 'ZHYC_AUTH_REDIRECT_URI', '后台管理端回调地址与认证中心注册回调地址不一致');
  assertEndpointPrefix(env, 'VITE_AUTH_AUTHORIZATION_ENDPOINT', 'ZHYC_AUTH_ISSUER', '/oauth2/authorize');
  assertEndpointPrefix(env, 'ZHYC_PLATFORM_AUTH_TOKEN_URI', 'ZHYC_AUTH_ISSUER', '/oauth2/token');
  assertEndpointPrefix(env, 'ZHYC_PLATFORM_AUTH_JWK_SET_URI', 'ZHYC_AUTH_ISSUER', '/oauth2/jwks');
  assertEndpointPrefix(env, 'ZHYC_OPENAPI_OAUTH2_INTROSPECTION_URI', 'ZHYC_AUTH_ISSUER', '/oauth2/introspect');
}

/**
 * 校验两个环境变量值相等。
 *
 * @param env 环境变量映射
 * @param leftKey 左侧变量名
 * @param rightKey 右侧变量名
 * @param message 错误提示
 */
function assertEqual(env, leftKey, rightKey, message) {
  if (!env.has(leftKey) || !env.has(rightKey)) {
    return;
  }
  if (env.get(leftKey) !== env.get(rightKey)) {
    errors.push(`${message}：${leftKey}=${env.get(leftKey)}，${rightKey}=${env.get(rightKey)}`);
  }
}

/**
 * 校验端点必须以认证中心 issuer 开头并以指定路径结尾。
 *
 * @param env 环境变量映射
 * @param endpointKey 端点变量名
 * @param issuerKey issuer 变量名
 * @param suffix 端点后缀
 */
function assertEndpointPrefix(env, endpointKey, issuerKey, suffix) {
  if (!env.has(endpointKey) || !env.has(issuerKey)) {
    return;
  }
  const endpoint = env.get(endpointKey);
  const issuer = env.get(issuerKey);
  if (!endpoint.startsWith(issuer) || !endpoint.endsWith(suffix)) {
    errors.push(`认证端点不一致：${endpointKey} 必须等于 ${issuerKey} + ${suffix}`);
  }
}

/**
 * 校验样例环境变量保持安全占位，不得出现真实密钥形态。
 *
 * @param env 环境变量映射
 */
function assertExampleValues(env) {
  for (const [key, value] of env.entries()) {
    if (isSecretKey(key) && value && !value.startsWith('replace_with_') && !isLocalEndpointValue(value)) {
      errors.push(`样例环境变量疑似写入真实敏感值：${key}`);
    }
  }
}

/**
 * 严格校验真实环境变量不能保留占位符或空值。
 *
 * @param env 环境变量映射
 */
function assertStrictValues(env) {
  for (const key of requiredKeys) {
    const value = env.get(key);
    if (isOptionalLocalJwk(env, key)) {
      continue;
    }
    if (!value) {
      errors.push(`环境变量不能为空：${key}`);
      continue;
    }
    if (value.includes('replace_with_')) {
      errors.push(`环境变量仍为占位符：${key}`);
    }
  }
  if (env.get('ZHYC_AUTH_PLATFORM_TENANT_ID') !== 'zhyc-platform') {
    errors.push('认证中心平台租户编码必须与首期种子租户一致：ZHYC_AUTH_PLATFORM_TENANT_ID=zhyc-platform');
  }
  if (env.get('ZHYC_AUTH_PLATFORM_USER_ID') !== '1') {
    errors.push('认证中心平台用户主键必须与首期种子管理员一致：ZHYC_AUTH_PLATFORM_USER_ID=1');
  }
  if (env.get('ZHYC_AUTH_PLATFORM_USERNAME') !== 'admin') {
    errors.push('认证中心平台登录账号必须与首期种子管理员一致：ZHYC_AUTH_PLATFORM_USERNAME=admin');
  }
}

/**
 * 判断当前键是否为 local 模式下允许留空的 JWK 配置。
 *
 * <p>认证中心在本地开发模式允许运行时生成 RSA 密钥；生产环境仍应由密钥管理系统注入。</p>
 *
 * @param env 环境变量映射
 * @param key 当前环境变量名
 * @returns local 模式下允许留空时返回 true
 */
function isOptionalLocalJwk(env, key) {
  return ['dev', 'local'].includes(env.get('SPRING_PROFILES_ACTIVE'))
      && ['ZHYC_AUTH_JWK_PRIVATE_KEY_PEM', 'ZHYC_AUTH_JWK_PUBLIC_KEY_PEM', 'ZHYC_AUTH_JWK_KEY_ID'].includes(key)
      && !env.get(key);
}

/**
 * 判断变量名是否承载敏感值。
 *
 * @param key 变量名
 * @returns 敏感变量返回 true
 */
function isSecretKey(key) {
  return /PASSWORD|SECRET|PRIVATE_KEY|PUBLIC_KEY|KEY_ID|USERNAME|USER_NAME/i.test(key);
}

/**
 * 判断变量值是否为允许出现在样例中的本地端点。
 *
 * @param value 变量值
 * @returns 本地端点或标准驱动类名返回 true
 */
function isLocalEndpointValue(value) {
  return value.startsWith('http://127.0.0.1:')
      || value.startsWith('jdbc:mysql://127.0.0.1:')
      || value === 'com.mysql.cj.jdbc.Driver'
      || value === 'zhyc-auth-client';
}
