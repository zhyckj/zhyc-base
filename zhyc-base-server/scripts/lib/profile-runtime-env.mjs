/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const profileTargets = [
  'zhyc-auth-server/src/main/resources/application-{profile}.yml',
  'zhyc-auth-server/src/main/resources/application-{profile}.local.yml',
  'zhyc-platform-app/src/main/resources/application-{profile}.yml',
  'zhyc-platform-app/src/main/resources/application-{profile}.local.yml',
  'zhyc-openapi-gateway/src/main/resources/application-{profile}.yml',
  'zhyc-openapi-gateway/src/main/resources/application-{profile}.local.yml',
];

/**
 * 加载 Spring Profile 对应的运行期配置。
 *
 * <p>解析顺序与 Spring Boot 保持一致：系统环境变量优先，profile YAML 默认值兜底，
 * 兼容模式下允许 --env-file 覆盖未显式设置的本机变量。</p>
 *
 * @param {string[]} argv 命令行参数
 * @param {string} serverRoot 后端工程根目录
 * @returns {Map<string, string>} 运行期配置映射
 */
export function loadRuntimeEnv(argv, serverRoot) {
  const env = new Map(Object.entries(process.env));
  const profile = resolveProfile(argv, env);
  loadProfileDefaults(env, serverRoot, profile);
  loadEnvFileOverrides(env, argv);
  env.set('SPRING_PROFILES_ACTIVE', profile);
  return env;
}

/**
 * 获取命令行指定的 Spring Profile。
 *
 * @param {string[]} argv 命令行参数
 * @param {Map<string, string>} env 环境变量
 * @returns {string} profile 名称
 */
export function resolveProfile(argv, env = new Map(Object.entries(process.env))) {
  const environmentIndex = argv.indexOf('--env');
  const profileIndex = argv.indexOf('--profile');
  const configuredProfile = environmentIndex >= 0 && argv[environmentIndex + 1]
    ? argv[environmentIndex + 1]
    : profileIndex >= 0 && argv[profileIndex + 1]
      ? argv[profileIndex + 1]
      : env.get('SPRING_PROFILES_ACTIVE') || env.get('ZHYC_ENV');
  const profile = String(configuredProfile || 'dev').trim();
  return ['dev', 'test', 'prod'].includes(profile) ? profile : 'dev';
}

/**
 * 解析 profile YAML 中的 Spring 占位符默认值。
 *
 * @param {Map<string, string>} env 运行期配置映射
 * @param {string} serverRoot 后端工程根目录
 * @param {string} profile profile 名称
 */
function loadProfileDefaults(env, serverRoot, profile) {
  for (const target of profileTargets) {
    const filePath = resolve(serverRoot, target.replace('{profile}', profile));
    if (!existsSync(filePath)) {
      continue;
    }
    const content = readFileSync(filePath, 'utf8');
    for (const match of content.matchAll(/\$\{([A-Z0-9_]+)(?::([^}]*))?}/g)) {
      const [, key, defaultValue = ''] = match;
      if (!env.has(key) || env.get(key) === '') {
        env.set(key, defaultValue);
      }
    }
  }
}

/**
 * 兼容读取旧 dotenv 文件，仅用于本机历史脚本覆盖。
 *
 * @param {Map<string, string>} env 运行期配置映射
 * @param {string[]} argv 命令行参数
 */
function loadEnvFileOverrides(env, argv) {
  const fileIndex = argv.indexOf('--env-file');
  if (fileIndex < 0 || !argv[fileIndex + 1]) {
    return;
  }
  const envFile = resolve(process.cwd(), argv[fileIndex + 1]);
  if (!existsSync(envFile)) {
    return;
  }
  for (const line of readFileSync(envFile, 'utf8').split(/\r?\n/)) {
    const trimmedLine = line.trim();
    if (!trimmedLine || trimmedLine.startsWith('#')) {
      continue;
    }
    const separatorIndex = trimmedLine.indexOf('=');
    if (separatorIndex <= 0) {
      continue;
    }
    env.set(trimmedLine.slice(0, separatorIndex).trim(), trimQuotes(trimmedLine.slice(separatorIndex + 1).trim()));
  }
}

/**
 * 去除配置值两侧引号。
 *
 * @param {string} value 原始值
 * @returns {string} 去除引号后的值
 */
function trimQuotes(value) {
  const trimmedValue = value.trim();
  if ((trimmedValue.startsWith('"') && trimmedValue.endsWith('"')) || (trimmedValue.startsWith("'") && trimmedValue.endsWith("'"))) {
    return trimmedValue.slice(1, -1);
  }
  return trimmedValue;
}
