/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * 认证中心授权服务器配置单元测试。
 *
 * <p>聚焦认证中心与核心平台之间的令牌契约，确保签发令牌包含 Shiro 主体映射所需平台 Claims。</p>
 */
class AuthorizationServerConfigTest {

  /**
   * 验证访问令牌会写入核心平台需要的租户、用户和登录账号 Claims。
   */
  @Test
  void shouldAddPlatformClaimsToAccessToken() {
    AuthorizationServerConfig config = new AuthorizationServerConfig();
    OAuth2TokenCustomizer<JwtEncodingContext> customizer = config.platformJwtTokenCustomizer(
        "tenant_a", "1001", "platform_admin");
    JwtEncodingContext context = JwtEncodingContext.with(
            JwsHeader.with(SignatureAlgorithm.RS256),
            JwtClaimsSet.builder().subject("platform_admin"))
        .tokenType(OAuth2TokenType.ACCESS_TOKEN)
        .build();

    customizer.customize(context);

    Map<String, Object> claims = context.getClaims().build().getClaims();
    assertEquals("tenant_a", claims.get("tenant_id"));
    assertEquals(1001L, claims.get("user_id"));
    assertEquals("platform_admin", claims.get("preferred_username"));
  }

  /**
   * 验证刷新令牌不会写入核心平台业务 Claims，避免非访问令牌被误当作业务访问凭据。
   */
  @Test
  void shouldSkipPlatformClaimsForRefreshToken() {
    AuthorizationServerConfig config = new AuthorizationServerConfig();
    OAuth2TokenCustomizer<JwtEncodingContext> customizer = config.platformJwtTokenCustomizer(
        "tenant_a", "1001", "platform_admin");
    JwtEncodingContext context = JwtEncodingContext.with(
            JwsHeader.with(SignatureAlgorithm.RS256),
            JwtClaimsSet.builder().subject("platform_admin"))
        .tokenType(OAuth2TokenType.REFRESH_TOKEN)
        .build();

    customizer.customize(context);

    Map<String, Object> claims = context.getClaims().build().getClaims();
    assertFalse(claims.containsKey("tenant_id"));
    assertFalse(claims.containsKey("user_id"));
    assertFalse(claims.containsKey("preferred_username"));
  }

  /**
   * 验证平台用户主键配置不是正整数时拒绝创建令牌定制器。
   */
  @Test
  void shouldRejectInvalidPlatformUserId() {
    AuthorizationServerConfig config = new AuthorizationServerConfig();

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> config.platformJwtTokenCustomizer("tenant_a", "0", "platform_admin"));

    assertEquals("认证中心平台用户主键必须为正整数", exception.getMessage());
  }

  /**
   * 验证前端登录页地址缺少授权请求标记时会自动补齐，避免浏览器在授权端点和登录页之间循环跳转。
   */
  @Test
  void shouldAppendAuthRequestMarkerWhenFrontendLoginUriMissingIt() {
    String normalizedUri = AuthFrontendLoginUris.normalizeAuthRequestLoginUri(
        "https://base.zhyc-cloud.com/login");

    assertEquals("https://base.zhyc-cloud.com/login?authRequest=1", normalizedUri);
  }

  /**
   * 验证前端登录页地址已有错误授权请求标记时会覆盖为有效值。
   */
  @Test
  void shouldReplaceInvalidAuthRequestMarker() {
    String normalizedUri = AuthFrontendLoginUris.normalizeAuthRequestLoginUri(
        "https://base.zhyc-cloud.com/login?authRequest=0&returnTo=/dashboard");

    assertEquals("https://base.zhyc-cloud.com/login?returnTo=/dashboard&authRequest=1", normalizedUri);
  }
}
