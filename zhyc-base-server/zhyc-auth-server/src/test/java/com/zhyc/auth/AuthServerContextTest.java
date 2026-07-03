/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 认证中心上下文启动测试。
 */
@SpringBootTest(properties = {
    "zhyc.auth.client-secret=zhyc-auth-secret",
    "zhyc.auth.user-name=auth-admin",
    "zhyc.auth.user-password=zhyc-auth-password",
    "zhyc.auth.platform-tenant-id=tenant_a",
    "zhyc.auth.platform-user-id=1001",
    "zhyc.auth.platform-username=platform_admin",
    "zhyc.auth.redirect-uri=http://127.0.0.1:8081/login/oauth2/code/zhyc",
    "zhyc.auth.login-success-redirect-uri=http://127.0.0.1:5173/login",
    "zhyc.auth.frontend-login-uri=http://127.0.0.1:5173/login?authRequest=1",
    "zhyc.auth.datasource.url="
})
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class AuthServerContextTest {

  /**
   * 用于认证中心 HTTP 端点冒烟验证的 MockMvc 客户端。
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * 验证认证中心 Spring Boot 上下文能够正常加载。
   */
  @Test
  void contextLoads() {
  }

  /**
   * 验证授权服务器元数据端点可匿名访问。
   *
   * @throws Exception MockMvc 请求失败时抛出
   */
  @Test
  void authorizationServerMetadataIsAvailable() throws Exception {
    mockMvc.perform(get("/.well-known/oauth-authorization-server"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authorization_endpoint").value("http://127.0.0.1:8090/oauth2/authorize"))
        .andExpect(jsonPath("$.token_endpoint").value("http://127.0.0.1:8090/oauth2/token"))
        .andExpect(jsonPath("$.jwks_uri").value("http://127.0.0.1:8090/oauth2/jwks"));
  }

  /**
   * 验证 OIDC 发现端点可匿名访问，供第三方应用按标准发现授权、令牌和用户信息端点。
   *
   * @throws Exception MockMvc 请求失败时抛出
   */
  @Test
  void openIdProviderConfigurationIsAvailable() throws Exception {
    mockMvc.perform(get("/.well-known/openid-configuration"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authorization_endpoint").value("http://127.0.0.1:8090/oauth2/authorize"))
        .andExpect(jsonPath("$.token_endpoint").value("http://127.0.0.1:8090/oauth2/token"))
        .andExpect(jsonPath("$.jwks_uri").value("http://127.0.0.1:8090/oauth2/jwks"))
        .andExpect(jsonPath("$.userinfo_endpoint").value("http://127.0.0.1:8090/userinfo"));
  }

  /**
   * 验证 JWK Set 端点可返回运行时生成的公钥集合。
   *
   * @throws Exception MockMvc 请求失败时抛出
   */
  @Test
  void jwksEndpointReturnsKeys() throws Exception {
    mockMvc.perform(get("/oauth2/jwks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys").isArray())
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"));
  }

  /**
   * 验证未登录访问授权端点时会进入登录流程。
   *
   * @throws Exception MockMvc 请求失败时抛出
   */
  @Test
  void authorizeEndpointRedirectsAnonymousUserToLogin() throws Exception {
    mockMvc.perform(get("/oauth2/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", "zhyc-auth-client")
            .queryParam("scope", "openid profile")
            .queryParam("redirect_uri", "http://127.0.0.1:8081/login/oauth2/code/zhyc")
            .queryParam("code_challenge", "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFG")
            .queryParam("code_challenge_method", "S256")
            .queryParam("state", "smoke"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", "http://127.0.0.1:5173/login?authRequest=1"));
  }

  /**
   * 验证 Token 端点不会被表单登录入口接管，避免刷新令牌请求 302 到登录页。
   *
   * @throws Exception MockMvc 请求失败时抛出
   */
  @Test
  void tokenEndpointReturnsOAuthErrorInsteadOfLoginRedirect() throws Exception {
    mockMvc.perform(post("/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .param("grant_type", "refresh_token")
            .param("refresh_token", "expired-refresh-token"))
        .andExpect(status().isBadRequest())
        .andExpect(header().doesNotExist("Location"))
        .andExpect(jsonPath("$.error").value("invalid_client"));
  }

  /**
   * 验证认证中心登录页入口重定向到后台前端登录页。
   *
   * @throws Exception MockMvc 请求失败时抛出
   */
  @Test
  void loginPageRedirectsToFrontendLogin() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", "http://127.0.0.1:5173/login?authRequest=1"));
  }

  /**
   * 验证直接登录认证中心成功后不会落到未映射的根路径。
   *
   * <p>后台 OAuth2 授权入口进入时仍由 SavedRequest 回到授权端点；直接打开认证中心登录页时需要兜底回后台登录入口。</p>
   *
   * @throws Exception MockMvc 请求失败时抛出
   */
  @Test
  void directLoginRedirectsToAdminLoginEntry() throws Exception {
    mockMvc.perform(post("/login")
            .param("username", "auth-admin")
            .param("password", "zhyc-auth-password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", "http://127.0.0.1:5173/login"));
  }

  /**
   * 验证前端表单登录失败后回到前端登录页并携带失败标记。
   *
   * @throws Exception MockMvc 请求失败时抛出
   */
  @Test
  void failedLoginRedirectsToFrontendLogin() throws Exception {
    mockMvc.perform(post("/login")
            .param("username", "auth-admin")
            .param("password", "wrong-password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", "http://127.0.0.1:5173/login?authRequest=1&error=1"));
  }

  /**
   * 验证配置注入的认证中心登录用户生效，不再输出 Spring Security 随机默认密码日志。
   *
   * @param output 测试期间捕获的日志输出
   */
  @Test
  void defaultGeneratedPasswordLogIsDisabled(CapturedOutput output) {
    org.hamcrest.MatcherAssert.assertThat(
        output.getOut(),
        not(containsString("Using generated security password")));
  }
}
