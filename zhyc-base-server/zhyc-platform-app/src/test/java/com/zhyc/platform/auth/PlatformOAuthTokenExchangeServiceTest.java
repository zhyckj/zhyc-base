/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.zhyc.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * 平台 OAuth2 授权码换令牌服务测试。
 */
class PlatformOAuthTokenExchangeServiceTest {

  /**
   * 验证核心平台用服务端 client secret 调认证中心 token 端点，不把密钥下发到浏览器。
   */
  @Test
  void shouldExchangeAuthorizationCodeWithServerSideClientSecret() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PlatformOAuthTokenExchangeService service = new RestClientPlatformOAuthTokenExchangeService(
        "http://127.0.0.1:8090/oauth2/token", "zhyc-auth-client", "secret-value", builder.build());

    server.expect(once(), requestTo("http://127.0.0.1:8090/oauth2/token"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", "Basic " + basic("zhyc-auth-client", "secret-value")))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(content().string("grant_type=authorization_code&code=auth-code-001&redirect_uri=http%3A%2F%2F127.0.0.1%3A5173%2Fauth%2Fcallback&code_verifier=pkce-code-verifier-001"))
        .andRespond(withSuccess("""
            {
              "access_token": "access-token-value",
              "refresh_token": "refresh-token-value",
              "token_type": "Bearer",
              "expires_in": 3600,
              "scope": "openid profile"
            }
            """, MediaType.APPLICATION_JSON));

    PlatformOAuthTokenResponse response = service.exchangeAuthorizationCode(
        new PlatformOAuthTokenExchangeCommand("auth-code-001", "http://127.0.0.1:5173/auth/callback",
            "pkce-code-verifier-001"));

    assertEquals("access-token-value", response.accessToken());
    assertEquals("refresh-token-value", response.refreshToken());
    assertEquals("Bearer", response.tokenType());
    assertEquals(3600L, response.expiresIn());
    assertEquals("openid profile", response.scope());
    server.verify();
  }

  /**
   * 验证授权码为空时直接拒绝，避免向认证中心发送无效请求。
   */
  @Test
  void shouldRejectBlankAuthorizationCode() {
    PlatformOAuthTokenExchangeService service = new RestClientPlatformOAuthTokenExchangeService(
        "http://127.0.0.1:8090/oauth2/token", "zhyc-auth-client", "secret-value", RestClient.create());

    assertThrows(IllegalArgumentException.class,
        () -> service.exchangeAuthorizationCode(new PlatformOAuthTokenExchangeCommand(" ",
            "http://127.0.0.1:5173/auth/callback", "pkce-code-verifier-001")));
  }

  /**
   * 验证认证中心返回 OAuth2 标准错误时，平台只向前端透出错误码，不暴露客户端密钥。
   */
  @Test
  void shouldExposeOAuthErrorCodeWithoutSecretWhenTokenEndpointRejectsRequest() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PlatformOAuthTokenExchangeService service = new RestClientPlatformOAuthTokenExchangeService(
        "http://127.0.0.1:8090/oauth2/token", "zhyc-auth-client", "secret-value", builder.build());

    server.expect(once(), requestTo("http://127.0.0.1:8090/oauth2/token"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withBadRequest()
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "error": "invalid_grant",
                  "error_description": "Bad credentials"
                }
                """));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.exchangeAuthorizationCode(new PlatformOAuthTokenExchangeCommand("auth-code-002",
            "http://127.0.0.1:5173/auth/callback", "pkce-code-verifier-002")));

    assertEquals("AUTH_CENTER_TOKEN_EXCHANGE_FAILED", exception.getCode());
    assertTrue(exception.getMessage().contains("invalid_grant"));
    assertFalseContainsSecret(exception.getMessage());
    server.verify();
  }

  /**
   * 验证核心平台使用服务端 client secret 和刷新令牌换取新的访问令牌。
   */
  @Test
  void shouldRefreshAccessTokenWithServerSideClientSecret() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PlatformOAuthTokenExchangeService service = new RestClientPlatformOAuthTokenExchangeService(
        "http://127.0.0.1:8090/oauth2/token", "zhyc-auth-client", "secret-value", builder.build());

    server.expect(once(), requestTo("http://127.0.0.1:8090/oauth2/token"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", "Basic " + basic("zhyc-auth-client", "secret-value")))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(content().string("grant_type=refresh_token&refresh_token=refresh-token-001"))
        .andRespond(withSuccess("""
            {
              "access_token": "access-token-refreshed",
              "refresh_token": "refresh-token-rotated",
              "token_type": "Bearer",
              "expires_in": 1800,
              "scope": "openid profile"
            }
            """, MediaType.APPLICATION_JSON));

    PlatformOAuthTokenResponse response = service.refreshAccessToken(
        new PlatformOAuthRefreshTokenCommand("refresh-token-001"));

    assertEquals("access-token-refreshed", response.accessToken());
    assertEquals("refresh-token-rotated", response.refreshToken());
    assertEquals(1800L, response.expiresIn());
    server.verify();
  }

  /**
   * 构建 HTTP Basic 凭证。
   *
   * @param clientId OAuth2 客户端标识
   * @param clientSecret OAuth2 客户端密钥
   * @return Basic 凭证值
   */
  private static String basic(String clientId, String clientSecret) {
    return Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 断言错误消息不包含测试客户端密钥。
   *
   * @param message 错误消息
   */
  private static void assertFalseContainsSecret(String message) {
    assertTrue(!message.contains("secret-value"), "错误消息不得暴露客户端密钥");
  }
}
