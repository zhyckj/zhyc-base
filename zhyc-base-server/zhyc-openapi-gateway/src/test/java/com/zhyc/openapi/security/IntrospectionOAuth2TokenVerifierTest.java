/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * OAuth2 Token introspection 校验器测试。
 */
class IntrospectionOAuth2TokenVerifierTest {

  /**
   * 验证 active Token 会按租户和客户端映射解析为开放平台应用。
   */
  @Test
  void shouldVerifyActiveTokenAndResolveOpenApiApp() {
    OAuth2TokenVerifier verifier = new IntrospectionOAuth2TokenVerifier(
        token -> OAuth2TokenIntrospectionResponse.active(Map.of(
            "tenant_id", "tenant_a",
            "client_id", "purchase-portal-client",
            "scope", "openid purchase.request")),
        new StaticMappingRepository());

    OAuth2TokenAuthenticationResult result = verifier.verify("access-token");

    assertTrue(result.isAuthenticated());
    assertEquals("tenant_a", result.getTenantId());
    assertEquals("purchase-app", result.getAppCode());
    assertEquals("purchase-portal-client", result.getClientId());
  }

  /**
   * 验证未激活 Token 会返回稳定错误编码。
   */
  @Test
  void shouldRejectInactiveToken() {
    OAuth2TokenVerifier verifier = new IntrospectionOAuth2TokenVerifier(
        token -> OAuth2TokenIntrospectionResponse.inactive(), new StaticMappingRepository());

    OAuth2TokenAuthenticationResult result = verifier.verify("access-token");

    assertEquals(false, result.isAuthenticated());
    assertEquals("INVALID_TOKEN", result.getFailureCode());
  }

  /**
   * 验证 Token scope 超出客户端映射允许范围时会拒绝。
   */
  @Test
  void shouldRejectTokenWhenScopeExceedsMapping() {
    OAuth2TokenVerifier verifier = new IntrospectionOAuth2TokenVerifier(
        token -> OAuth2TokenIntrospectionResponse.active(Map.of(
            "tenant_id", "tenant_a",
            "client_id", "purchase-portal-client",
            "scope", "openid admin")),
        new StaticMappingRepository());

    OAuth2TokenAuthenticationResult result = verifier.verify("access-token");

    assertEquals(false, result.isAuthenticated());
    assertEquals("INSUFFICIENT_SCOPE", result.getFailureCode());
  }

  /**
   * 验证 active Token 缺少 scope 声明时会拒绝，避免无授权范围的第三方 Token 访问开放 API。
   */
  @Test
  void shouldRejectActiveTokenWithoutScope() {
    OAuth2TokenVerifier verifier = new IntrospectionOAuth2TokenVerifier(
        token -> OAuth2TokenIntrospectionResponse.active(Map.of(
            "tenant_id", "tenant_a",
            "client_id", "purchase-portal-client")),
        new StaticMappingRepository());

    OAuth2TokenAuthenticationResult result = verifier.verify("access-token");

    assertEquals(false, result.isAuthenticated());
    assertEquals("INSUFFICIENT_SCOPE", result.getFailureCode());
  }

  /**
   * 验证仅携带 OIDC 基础授权范围的 Token 会被拒绝，避免第三方登录令牌直接访问开放 API。
   */
  @Test
  void shouldRejectTokenWhenOnlyOidcBasicScopesPresent() {
    OAuth2TokenVerifier verifier = new IntrospectionOAuth2TokenVerifier(
        token -> OAuth2TokenIntrospectionResponse.active(Map.of(
            "tenant_id", "tenant_a",
            "client_id", "purchase-portal-client",
            "scope", "openid profile")),
        new StaticMappingRepository());

    OAuth2TokenAuthenticationResult result = verifier.verify("access-token");

    assertEquals(false, result.isAuthenticated());
    assertEquals("INSUFFICIENT_SCOPE", result.getFailureCode());
  }

  /**
   * 测试用 OAuth2 客户端映射仓储。
   */
  private static final class StaticMappingRepository implements OpenApiOAuth2ClientMappingRepository {

    @Override
    public Optional<OpenApiOAuth2ClientMapping> findEnabledByTenantIdAndClientId(
        String tenantId, String clientId) {
      if ("tenant_a".equals(tenantId) && "purchase-portal-client".equals(clientId)) {
        return Optional.of(new OpenApiOAuth2ClientMapping("tenant_a", "purchase-app",
            "purchase-portal-client", "openid profile purchase.request"));
      }
      return Optional.empty();
    }
  }
}
