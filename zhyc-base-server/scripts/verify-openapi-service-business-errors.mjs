/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const root = resolve(process.argv[2] || process.cwd());
const scanRoot = resolveOpenApiSourceRoot(root);
const violations = [];

for (const file of listOpenApiServiceFiles(scanRoot)) {
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  lines.forEach((line, index) => {
    if (isAllowedLine(line)) {
      return;
    }
    if (line.includes('throw new IllegalArgumentException(')) {
      violations.push(`${relative(root, file)}:${index + 1} -> 开放 API 服务层不得直接抛裸参数异常: ${line.trim()}`);
    }
  });
}
verifyRequiredOpenApiServiceProtections(root);

if (violations.length > 0) {
  console.error('OpenAPI 服务业务异常门禁失败。服务层面向调用方的参数或业务错误必须使用带稳定错误码的 BusinessException：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('OpenAPI 服务业务异常门禁通过。');

/**
 * 解析开放 API 模块生产源码根目录。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 * @returns 开放 API 生产源码根目录
 */
function resolveOpenApiSourceRoot(startRoot) {
  const candidates = [
    resolve(startRoot, 'zhyc-module-openapi/src/main/java/com/zhyc/openapi'),
    resolve(startRoot, 'zhyc-base-server/zhyc-module-openapi/src/main/java/com/zhyc/openapi'),
  ];
  const matchedRoot = candidates.find((candidate) => existsSync(candidate));
  if (matchedRoot) {
    return matchedRoot;
  }
  console.error(`OpenAPI 服务业务异常门禁失败。未找到开放 API 生产源码目录：${candidates.join(' 或 ')}`);
  process.exit(1);
}

/**
 * 递归列出开放 API 生产服务源码文件。
 *
 * @param dir 当前扫描目录
 * @returns 服务源码文件路径
 */
function listOpenApiServiceFiles(dir) {
  const rootStat = statSync(dir);
  if (rootStat.isFile()) {
    return isOpenApiServiceFile(dir) ? [dir] : [];
  }
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', 'node_modules', '.git'].includes(entry.name)) {
        return [];
      }
      if (path.includes('/src/test/') || path.endsWith('/test')) {
        return [];
      }
      return listOpenApiServiceFiles(path);
    }
    return entry.isFile() && isOpenApiServiceFile(path) ? [path] : [];
  });
}

/**
 * 判断文件是否为开放 API 生产服务源码文件。
 *
 * @param file 文件路径
 * @returns 属于扫描范围时返回 true
 */
function isOpenApiServiceFile(file) {
  const normalizedPath = file.split('\\').join('/');
  return normalizedPath.endsWith('.java')
    && normalizedPath.includes('/src/main/java/com/zhyc/openapi/')
    && normalizedPath.includes('/service/')
    && !normalizedPath.includes('/src/test/')
    && !normalizedPath.includes('/target/');
}

/**
 * 判断源码行是否属于允许跳过的注释内容。
 *
 * @param line 源码行
 * @returns 允许跳过时返回 true
 */
function isAllowedLine(line) {
  const trimmed = line.trim();
  return trimmed.startsWith('//') || trimmed.startsWith('*') || trimmed.startsWith('/*');
}

/**
 * 校验开放 API 子资源服务必须绑定真实开发者应用。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 */
function verifyRequiredOpenApiServiceProtections(startRoot) {
  const requirements = [
    {
      file: 'zhyc-module-openapi/src/main/java/com/zhyc/openapi/apikey/service/DefaultOpenApiApiKeyService.java',
      snippets: [
        'requireAppSupportsApiKey(tenantId, appCode);',
        'appRepository.findByTenantIdAndAppCode(tenantId, appCode)',
        'ZHYC_OPENAPI_API_KEY_APP_NOT_FOUND',
        'ZHYC_OPENAPI_API_KEY_APP_AUTH_MODE_INVALID',
        'OpenApiAppAuthMode.API_KEY.getCode().equals(app.getAuthMode())',
      ],
      description: 'API Key 保存和轮换前必须校验开发者应用属于当前租户且启用 API Key 鉴权',
    },
    {
      file: 'zhyc-module-openapi/src/main/java/com/zhyc/openapi/permission/service/DefaultOpenApiPermissionService.java',
      snippets: [
        'requireAppBelongsToTenant(tenantId, appCode);',
        'appRepository.findByTenantIdAndAppCode(tenantId, appCode)',
        'ZHYC_OPENAPI_PERMISSION_APP_NOT_FOUND',
      ],
      description: '开放 API 授权保存前必须校验开发者应用属于当前租户',
    },
    {
      file: 'zhyc-module-openapi/src/main/java/com/zhyc/openapi/oauthclient/service/DefaultOpenApiOauthClientService.java',
      snippets: [
        'requireAppSupportsOauth2(tenantId, appCode);',
        'appRepository.findByTenantIdAndAppCode(tenantId, appCode)',
        'ZHYC_OPENAPI_OAUTH_CLIENT_APP_NOT_FOUND',
        'ZHYC_OPENAPI_OAUTH_CLIENT_APP_AUTH_MODE_INVALID',
        'OpenApiAppAuthMode.OAUTH2.getCode().equals(app.getAuthMode())',
      ],
      description: 'OAuth2 客户端映射保存前必须校验开发者应用属于当前租户且启用 OAuth2/OIDC 鉴权',
    },
    {
      file: 'zhyc-module-openapi/src/main/java/com/zhyc/openapi/signature/service/DefaultOpenApiSignaturePolicyService.java',
      snippets: [
        'requireAppSupportsApiKey(tenantId, appCode);',
        'appRepository.findByTenantIdAndAppCode(tenantId, appCode)',
        'ZHYC_OPENAPI_SIGNATURE_APP_NOT_FOUND',
        'ZHYC_OPENAPI_SIGNATURE_APP_AUTH_MODE_INVALID',
        'OpenApiAppAuthMode.API_KEY.getCode().equals(app.getAuthMode())',
      ],
      description: '签名策略保存前必须校验开发者应用属于当前租户且启用 API Key 鉴权',
    },
    {
      file: 'zhyc-module-openapi/src/main/java/com/zhyc/openapi/ratelimit/service/DefaultOpenApiRateLimitPolicyService.java',
      snippets: [
        'requireAppBelongsToTenant(tenantId, appCode);',
        'appRepository.findByTenantIdAndAppCode(tenantId, appCode)',
        'ZHYC_OPENAPI_RATE_LIMIT_APP_NOT_FOUND',
      ],
      description: '限流策略保存前必须校验开发者应用属于当前租户',
    },
  ];

  for (const requirement of requirements) {
    const file = resolveRequiredFile(startRoot, requirement.file);
    if (!file) {
      continue;
    }
    const content = readFileSync(file, 'utf8');
    for (const snippet of requirement.snippets) {
      if (!content.includes(snippet)) {
        violations.push(`${relative(startRoot, file)} -> ${requirement.description}，缺少关键片段: ${snippet}`);
      }
    }
  }
}

/**
 * 解析真实工程或测试工程中的指定源码文件。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 * @param relativeFile 模块内源码相对路径
 * @returns 匹配到的源码文件路径，不存在时返回空
 */
function resolveRequiredFile(startRoot, relativeFile) {
  const candidates = [
    resolve(startRoot, relativeFile),
    resolve(startRoot, 'zhyc-base-server', relativeFile),
  ];
  return candidates.find((candidate) => existsSync(candidate));
}
