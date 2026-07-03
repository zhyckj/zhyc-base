/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));

const requiredSnippets = [
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/ZhycOpenApiGatewayApplication.java', 'OAuth2/OIDC Token 校验'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/ZhycOpenApiGatewayApplication.java', '限流策略'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/ZhycOpenApiGatewayApplication.java', '真实网关路由'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfig.java', 'openApiOAuth2AuthenticationFilterRegistration'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfig.java', 'openApiApiKeyAuthenticationFilterRegistration'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfig.java', 'openApiRateLimitFilterRegistration'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfig.java', 'openApiRoutingFilterRegistration'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfig.java', 'JdbcOpenApiReplayNonceStore'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfig.java', 'JdbcOpenApiRateLimiter'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/JdbcOpenApiReplayNonceStore.java', 'openapi_replay_nonce'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/JdbcOpenApiRateLimiter.java', 'openapi_rate_limit_counter'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiRateLimitResult.java', 'retryAfterSeconds'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/InMemoryOpenApiRateLimiter.java', 'retryAfterSeconds(policy)'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/JdbcOpenApiRateLimiter.java', 'windowKey.expiresAt().getEpochSecond() - now.getEpochSecond()'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiRateLimitFilter.java', 'Retry-After'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiRateLimitFilter.java', 'writeError(httpResponse, 429'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRateLimitFilterTest.java', 'secondResponse.getHeader("Retry-After")'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRateLimitFilterTest.java', '开放 API 调用已超过限流阈值'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiGatewayCredentialStatus.java', 'ENABLED("enabled", "启用")'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiGatewayCredentialStatus.java', 'DISABLED("disabled", "禁用")'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiGatewayCredentialStatus.java', 'EXPIRED("expired", "已过期")'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/ApiKeyCredential.java', 'OpenApiGatewayCredentialStatus.fromCode(status).isEnabled()'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/ApiKeyAuthenticator.java', 'credential.isEnabled()'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/ApiKeyAuthenticator.java', 'isExpired'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiGatewayCredentialStatusTest.java', 'shouldRejectUnsupportedCredentialStatusCode'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/ApiKeyAuthenticatorTest.java', 'authenticateRejectsUnsupportedCredentialStatusAsDisabled'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/ApiKeyAuthenticatorTest.java', 'authenticateRejectsCredentialExpiringAtCurrentTime'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiClientIpMatcher.java', 'parseEscapedJsonCharacter'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiClientIpMatcherTest.java', 'shouldMatchIpv4WhenWhitelistUsesUnicodeEscapes'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'X-ZHYC-Request-Id'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'X-ZHYC-Body-SHA256'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'startsWithBearerPrefix'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'buildSignaturePath(httpRequest, requestPath)'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'requestPath + "?" + queryString'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'recordAuthenticationFailure(httpRequest, accessKey, requestPath'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'recordAuditSafely(new ApiCallAuditRecord'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'LOGGER.warn("开放 API 调用审计写入失败'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'OPENAPI_AUTHENTICATION'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', 'writeError(httpResponse'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilterTest.java', 'assertOpenApiErrorBody(response, "INVALID_SIGNATURE")'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilterTest.java', 'shouldAuthenticateOpenApiRequestWhenSignatureIncludesQueryString'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilterTest.java', 'shouldRecordAuditWhenApiKeyAuthenticationFails'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilterTest.java', 'shouldKeepAuthenticationFailureResponseWhenAuditRecorderThrows'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilterTest.java', 'shouldPassThroughOpenApiRequestWithLowercaseBearerTokenForOauth2Filter'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/ApiKeySignatureVerifier.java', 'verifyBodySha256'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/ApiKeyAuthenticator.java', 'signaturePolicy.isRequireBodyHash()'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/ApiKeyAuthenticatorTest.java', 'authenticateRejectsMissingBodyHashWhenSignaturePolicyRequiresIt'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/ApiKeyAuthenticatorTest.java', 'authenticateAcceptsBodyHashWhenSignaturePolicyRequiresIt'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilter.java', 'X-ZHYC-Request-Id'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilter.java', 'startsWithBearerPrefix'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilter.java', 'OPENAPI_OAUTH2_AUTHENTICATION'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilter.java', 'UNKNOWN_OAUTH2_CLIENT_ACCESS_KEY'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilter.java', 'recordAuthenticationFailure(httpRequest, requestPath'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilter.java', 'recordAuditSafely(new ApiCallAuditRecord'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilter.java', 'writeError(httpResponse'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilterTest.java', 'assertOpenApiErrorBody(response, "INVALID_TOKEN")'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilterTest.java', 'OPENAPI_OAUTH2_AUTHENTICATION'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilterTest.java', 'shouldKeepOAuth2AuthenticationFailureResponseWhenAuditRecorderThrows'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilterTest.java', 'shouldAuthenticateLowercaseBearerTokenAndRecordSuccessAudit'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/IntrospectionOAuth2TokenVerifier.java', 'OIDC_BASIC_SCOPES'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/IntrospectionOAuth2TokenVerifier.java', 'hasOpenApiBusinessScope(tokenScopes)'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/IntrospectionOAuth2TokenVerifierTest.java', 'shouldRejectTokenWhenOnlyOidcBasicScopesPresent'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiBackendRequest.java', 'queryString'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiRoutingFilter.java', 'OPENAPI_AUTH_CONTEXT_MISSING'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiRoutingFilter.java', 'OPENAPI_ROUTE_NOT_FOUND'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiRoutingFilter.java', 'writeError(httpResponse'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiRoutingFilter.java', 'httpRequest.getQueryString()'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiResponseHeaderPolicy.java', 'ALLOWED_RESPONSE_HEADERS'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiResponseHeaderPolicy.java', 'x-zhyc-request-id'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiRoutingFilter.java', 'OpenApiResponseHeaderPolicy.isAllowedResponseHeader(headerName)'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRoutingFilterTest.java', 'shouldFilterSensitiveBackendResponseHeadersBeforeReturnToCaller'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRoutingFilterTest.java', 'X-ZHYC-Internal-Token'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/RestClientOpenApiBackendInvoker.java', 'buildBackendUri'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/RestClientOpenApiBackendInvoker.java', 'extractAllowedResponseHeaders'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/RestClientOpenApiBackendInvoker.java', 'OpenApiResponseHeaderPolicy.isAllowedResponseHeader(headerName)'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRoutingFilterTest.java', 'shouldRejectRoutingWhenAuthenticationContextMissing'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRoutingFilterTest.java', 'response.getHeader("X-ZHYC-Openapi-Error")'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRoutingFilterTest.java', 'shouldForwardQueryStringToBackendRequest'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRoutingFilterTest.java', 'shouldOnlyForwardGatewayContextHeadersToBackend'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiRoutingFilterTest.java', 'shouldWriteAllowedBackendResponseHeaders'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/RestClientOpenApiBackendInvokerTest.java', 'shouldAppendQueryStringToBackendRouteWhenInvokeBackend'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/RestClientOpenApiBackendInvokerTest.java', 'shouldExtractAllowedBackendResponseHeaders'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfigTest.java', 'OpenApiOAuth2AuthenticationFilter'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfigTest.java', 'OpenApiRateLimitFilter'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfigTest.java', 'OpenApiRoutingFilter'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilterTest.java', 'HEADER_REQUEST_ID'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilterTest.java', 'HEADER_REQUEST_ID'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/JdbcOpenApiReplayNonceStoreTest.java', 'shouldRejectRepeatedNonceAndAllowReuseAfterExpiration'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/JdbcOpenApiRateLimiterTest.java', 'shouldRejectRequestWhenJdbcWindowQuotaExceeded'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/config/OpenApiGatewaySecurityConfigTest.java', 'assertEquals(4'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/JdbcOpenApiRouteRepositoryTest.java', 'shouldResolvePurchaseOrderDetailRouteWithPathVariable'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/JdbcOpenApiRouteRepositoryTest.java', 'purchase-order-detail'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/JdbcApiPermissionRepositoryTest.java', 'shouldResolvePurchaseOrderDetailPermission'],
  ['zhyc-openapi-gateway/src/test/java/com/zhyc/openapi/security/JdbcApiPermissionRepositoryTest.java', 'purchase-order-detail'],
];

const forbiddenSnippets = [
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/ZhycOpenApiGatewayApplication.java', '仍由后续迭代接入'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiApiKeyAuthenticationFilter.java', '"X-Request-Id"'],
  ['zhyc-openapi-gateway/src/main/java/com/zhyc/openapi/security/OpenApiOAuth2AuthenticationFilter.java', '"X-Request-Id"'],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const path = resolve(rootDir, file);
  return !existsSync(path) || !readFileSync(path, 'utf8').includes(snippet);
});

const forbiddenHits = forbiddenSnippets.filter(([file, snippet]) => {
  const path = resolve(rootDir, file);
  return existsSync(path) && readFileSync(path, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0 || forbiddenHits.length > 0) {
  console.error('Open API 网关运行时校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  for (const [file, snippet] of forbiddenHits) {
    console.error(`存在过期描述: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('Open API 网关运行时校验通过。');
