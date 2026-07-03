/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 OAuth2 Token introspection 的访问令牌校验器。
 */
public class IntrospectionOAuth2TokenVerifier implements OAuth2TokenVerifier {

  /** Token 缺失错误编码。 */
  public static final String ERROR_MISSING_TOKEN = "MISSING_TOKEN";
  /** Token 无效错误编码。 */
  public static final String ERROR_INVALID_TOKEN = "INVALID_TOKEN";
  /** Token 缺少租户声明错误编码。 */
  public static final String ERROR_MISSING_TENANT = "MISSING_TENANT";
  /** OAuth2 客户端未授权错误编码。 */
  public static final String ERROR_UNAUTHORIZED_CLIENT = "UNAUTHORIZED_CLIENT";
  /** 授权范围不足错误编码。 */
  public static final String ERROR_INSUFFICIENT_SCOPE = "INSUFFICIENT_SCOPE";
  /** 仅用于身份登录的 OIDC 基础授权范围，不允许单独访问开放 API。 */
  private static final Set<String> OIDC_BASIC_SCOPES = Set.of(
      "openid", "profile", "email", "phone", "address", "offline_access");

  /** OAuth2 Token introspection 客户端。 */
  private final OAuth2TokenIntrospectionClient introspectionClient;
  /** OAuth2 客户端映射仓储。 */
  private final OpenApiOAuth2ClientMappingRepository mappingRepository;

  /**
   * 创建 OAuth2 Token introspection 校验器。
   *
   * @param introspectionClient OAuth2 Token introspection 客户端
   * @param mappingRepository OAuth2 客户端映射仓储
   */
  public IntrospectionOAuth2TokenVerifier(OAuth2TokenIntrospectionClient introspectionClient,
      OpenApiOAuth2ClientMappingRepository mappingRepository) {
    this.introspectionClient = Objects.requireNonNull(introspectionClient, "Token introspection 客户端不能为空");
    this.mappingRepository = Objects.requireNonNull(mappingRepository, "OAuth2 客户端映射仓储不能为空");
  }

  /**
   * 校验 OAuth2 访问令牌并映射到开放 API 应用。
   *
   * @param accessToken OAuth2 访问令牌
   * @return OAuth2 Token 鉴权结果，包含租户、应用和客户端标识
   */
  @Override
  public OAuth2TokenAuthenticationResult verify(String accessToken) {
    String requiredToken = trimToNull(accessToken);
    if (requiredToken == null) {
      return OAuth2TokenAuthenticationResult.failure(ERROR_MISSING_TOKEN);
    }
    OAuth2TokenIntrospectionResponse response = introspectionClient.introspect(requiredToken);
    if (response == null || !response.isActive()) {
      return OAuth2TokenAuthenticationResult.failure(ERROR_INVALID_TOKEN);
    }

    String tenantId = claimAsText(response, "tenant_id");
    String clientId = firstNonBlank(claimAsText(response, "client_id"), claimAsText(response, "azp"));
    if (tenantId == null || clientId == null) {
      return OAuth2TokenAuthenticationResult.failure(ERROR_MISSING_TENANT);
    }

    return mappingRepository.findEnabledByTenantIdAndClientId(tenantId, clientId)
        .map(mapping -> verifyScopes(response, mapping))
        .orElseGet(() -> OAuth2TokenAuthenticationResult.failure(ERROR_UNAUTHORIZED_CLIENT));
  }

  private OAuth2TokenAuthenticationResult verifyScopes(OAuth2TokenIntrospectionResponse response,
      OpenApiOAuth2ClientMapping mapping) {
    Set<String> tokenScopes = splitScopes(claimAsText(response, "scope"));
    Set<String> allowedScopes = splitScopes(mapping.getAllowedScopes());
    if (tokenScopes.isEmpty() || !allowedScopes.containsAll(tokenScopes) || !hasOpenApiBusinessScope(tokenScopes)) {
      return OAuth2TokenAuthenticationResult.failure(ERROR_INSUFFICIENT_SCOPE);
    }
    return OAuth2TokenAuthenticationResult.success(mapping.getTenantId(), mapping.getAppCode(),
        mapping.getClientId());
  }

  /**
   * 判断 Token 是否包含开放 API 业务授权范围。
   *
   * <p>OIDC 基础 scope 只能证明身份登录意图，不能单独代表第三方应用获得开放 API 调用授权。</p>
   *
   * @param tokenScopes Token 携带的授权范围
   * @return 包含业务授权范围时返回 {@code true}
   */
  private boolean hasOpenApiBusinessScope(Set<String> tokenScopes) {
    return tokenScopes.stream().anyMatch(scope -> !OIDC_BASIC_SCOPES.contains(scope));
  }

  private String claimAsText(OAuth2TokenIntrospectionResponse response, String claimName) {
    Object value = response.getClaims().get(claimName);
    return value == null ? null : trimToNull(String.valueOf(value));
  }

  private Set<String> splitScopes(String scopes) {
    String normalized = trimToNull(scopes);
    if (normalized == null) {
      return Set.of();
    }
    return Arrays.stream(normalized.split("\\s+"))
        .filter(scope -> !scope.isBlank())
        .collect(Collectors.toSet());
  }

  private String firstNonBlank(String first, String second) {
    return first != null ? first : second;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
