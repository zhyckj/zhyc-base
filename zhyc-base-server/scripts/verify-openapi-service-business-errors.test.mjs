/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';

const scriptPath = resolve(process.cwd(), 'scripts/verify-openapi-service-business-errors.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-openapi-service-errors-fail-'));
writeJava(failedRoot, 'zhyc-module-openapi/src/main/java/com/zhyc/openapi/demo/service/DemoOpenApiService.java', `
package com.zhyc.openapi.demo.service;

/**
 * 测试开放 API 服务。
 */
public class DemoOpenApiService {

    /**
     * 校验开放 API 调用方参数。
     */
    public void validate() {
        throw new IllegalArgumentException("开放 API 参数错误");
    }
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '开放 API 服务层裸参数异常必须触发门禁失败');
assert.match(failedResult.stderr, /DemoOpenApiService\.java/, '应报告违规服务文件');
assert.match(failedResult.stderr, /BusinessException/, '应提示改用稳定业务异常');

const appOwnershipFailedRoot = mkdtempSync(join(tmpdir(), 'zhyc-openapi-app-ownership-fail-'));
writeJava(appOwnershipFailedRoot,
  'zhyc-module-openapi/src/main/java/com/zhyc/openapi/apikey/service/DefaultOpenApiApiKeyService.java', `
package com.zhyc.openapi.apikey.service;

/**
 * 测试 API Key 服务。
 */
public class DefaultOpenApiApiKeyService {

    /**
     * 保存 API Key。
     */
    public void save() {
        apiKeyRepository.save(apiKey);
    }
}
`);

const appOwnershipFailedResult = spawnSync('node', [scriptPath, appOwnershipFailedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(appOwnershipFailedResult.status, 0, 'API Key 服务缺少应用归属校验时必须触发门禁失败');
assert.match(appOwnershipFailedResult.stderr, /DefaultOpenApiApiKeyService\.java/, '应报告缺少应用归属校验的服务文件');
assert.match(appOwnershipFailedResult.stderr, /requireAppSupportsApiKey/, '应提示保存前必须校验应用归属和鉴权模式');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-openapi-service-errors-pass-'));
writeJava(passedRoot, 'zhyc-module-openapi/src/main/java/com/zhyc/openapi/demo/service/DemoOpenApiService.java', `
package com.zhyc.openapi.demo.service;

import com.zhyc.common.exception.BusinessException;

/**
 * 测试开放 API 服务。
 */
public class DemoOpenApiService {

    /**
     * 校验开放 API 调用方参数。
     */
    public void validate() {
        throw new BusinessException("ZHYC_OPENAPI_DEMO_INVALID", "开放 API 参数错误");
    }
}
`);
writeJava(passedRoot, 'zhyc-module-openapi/src/main/java/com/zhyc/openapi/support/DemoOpenApiSupport.java', `
package com.zhyc.openapi.support;

/**
 * 测试开放 API 内部支持组件。
 */
public class DemoOpenApiSupport {

    /**
     * 校验内部枚举或工具参数。
     */
    public void validate() {
        throw new IllegalArgumentException("内部工具参数错误");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-openapi/src/main/java/com/zhyc/openapi/apikey/service/DefaultOpenApiApiKeyService.java', `
package com.zhyc.openapi.apikey.service;

/**
 * 测试 API Key 服务。
 */
public class DefaultOpenApiApiKeyService {

    /**
     * 保存 API Key。
     */
    public void save() {
        String tenantId = "tenant_a";
        String appCode = "purchase-app";
        requireAppSupportsApiKey(tenantId, appCode);
    }

    /**
     * 校验应用归属。
     */
    private void requireAppSupportsApiKey(String tenantId, String appCode) {
        appRepository.findByTenantIdAndAppCode(tenantId, appCode);
        OpenApiAppAuthMode.API_KEY.getCode().equals(app.getAuthMode());
        throw new com.zhyc.common.exception.BusinessException("ZHYC_OPENAPI_API_KEY_APP_NOT_FOUND",
            "开发者应用不存在或不属于当前租户: purchase-app");
        throw new com.zhyc.common.exception.BusinessException("ZHYC_OPENAPI_API_KEY_APP_AUTH_MODE_INVALID",
            "开发者应用未启用 API Key 鉴权: purchase-app");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-openapi/src/main/java/com/zhyc/openapi/permission/service/DefaultOpenApiPermissionService.java', `
package com.zhyc.openapi.permission.service;

/**
 * 测试开放 API 权限服务。
 */
public class DefaultOpenApiPermissionService {

    /**
     * 保存开放 API 权限。
     */
    public void save() {
        String tenantId = "tenant_a";
        String appCode = "purchase-app";
        requireAppBelongsToTenant(tenantId, appCode);
    }

    /**
     * 校验应用归属。
     */
    private void requireAppBelongsToTenant(String tenantId, String appCode) {
        appRepository.findByTenantIdAndAppCode(tenantId, appCode);
        throw new com.zhyc.common.exception.BusinessException("ZHYC_OPENAPI_PERMISSION_APP_NOT_FOUND",
            "开发者应用不存在或不属于当前租户: purchase-app");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-openapi/src/main/java/com/zhyc/openapi/oauthclient/service/DefaultOpenApiOauthClientService.java', `
package com.zhyc.openapi.oauthclient.service;

/**
 * 测试 OAuth2 客户端映射服务。
 */
public class DefaultOpenApiOauthClientService {

    /**
     * 保存 OAuth2 客户端映射。
     */
    public void save() {
        String tenantId = "tenant_a";
        String appCode = "purchase-app";
        requireAppSupportsOauth2(tenantId, appCode);
    }

    /**
     * 校验应用归属。
     */
    private void requireAppSupportsOauth2(String tenantId, String appCode) {
        appRepository.findByTenantIdAndAppCode(tenantId, appCode);
        OpenApiAppAuthMode.OAUTH2.getCode().equals(app.getAuthMode());
        throw new com.zhyc.common.exception.BusinessException("ZHYC_OPENAPI_OAUTH_CLIENT_APP_NOT_FOUND",
            "开发者应用不存在或不属于当前租户: purchase-app");
        throw new com.zhyc.common.exception.BusinessException("ZHYC_OPENAPI_OAUTH_CLIENT_APP_AUTH_MODE_INVALID",
            "开发者应用未启用 OAuth2/OIDC 鉴权: purchase-app");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-openapi/src/main/java/com/zhyc/openapi/signature/service/DefaultOpenApiSignaturePolicyService.java', `
package com.zhyc.openapi.signature.service;

/**
 * 测试签名策略服务。
 */
public class DefaultOpenApiSignaturePolicyService {

    /**
     * 保存签名策略。
     */
    public void save() {
        String tenantId = "tenant_a";
        String appCode = "purchase-app";
        requireAppSupportsApiKey(tenantId, appCode);
    }

    /**
     * 校验应用归属。
     */
    private void requireAppSupportsApiKey(String tenantId, String appCode) {
        appRepository.findByTenantIdAndAppCode(tenantId, appCode);
        OpenApiAppAuthMode.API_KEY.getCode().equals(app.getAuthMode());
        throw new com.zhyc.common.exception.BusinessException("ZHYC_OPENAPI_SIGNATURE_APP_NOT_FOUND",
            "开发者应用不存在或不属于当前租户: purchase-app");
        throw new com.zhyc.common.exception.BusinessException("ZHYC_OPENAPI_SIGNATURE_APP_AUTH_MODE_INVALID",
            "开发者应用未启用 API Key 鉴权: purchase-app");
    }
}
`);
writeJava(passedRoot,
  'zhyc-module-openapi/src/main/java/com/zhyc/openapi/ratelimit/service/DefaultOpenApiRateLimitPolicyService.java', `
package com.zhyc.openapi.ratelimit.service;

/**
 * 测试限流策略服务。
 */
public class DefaultOpenApiRateLimitPolicyService {

    /**
     * 保存限流策略。
     */
    public void save() {
        String tenantId = "tenant_a";
        String appCode = "purchase-app";
        requireAppBelongsToTenant(tenantId, appCode);
    }

    /**
     * 校验应用归属。
     */
    private void requireAppBelongsToTenant(String tenantId, String appCode) {
        appRepository.findByTenantIdAndAppCode(tenantId, appCode);
        throw new com.zhyc.common.exception.BusinessException("ZHYC_OPENAPI_RATE_LIMIT_APP_NOT_FOUND",
            "开发者应用不存在或不属于当前租户: purchase-app");
    }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /OpenAPI 服务业务异常门禁通过/);

/**
 * 写入测试用 Java 源码。
 *
 * @param root 测试工程根目录
 * @param file Java 源码相对路径
 * @param content Java 源码内容
 */
function writeJava(root, file, content) {
  const absolutePath = join(root, file);
  mkdirSync(dirname(absolutePath), { recursive: true });
  writeFileSync(absolutePath, content.trim());
}
