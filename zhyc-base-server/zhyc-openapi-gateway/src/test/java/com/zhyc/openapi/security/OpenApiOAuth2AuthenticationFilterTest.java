/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Open API OAuth2 Token 鉴权过滤器测试。
 */
class OpenApiOAuth2AuthenticationFilterTest {

  /** 固定时钟。 */
  private final Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);

  /**
   * 验证携带有效 Bearer Token 的开放 API 请求会通过授权校验并记录成功审计。
   */
  @Test
  void shouldAuthenticateBearerTokenAndRecordSuccessAudit()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader("Authorization", "Bearer valid-token");
    request.addHeader(OpenApiOAuth2AuthenticationFilter.HEADER_REQUEST_ID, "req-oauth2-001");
    request.setRemoteAddr("10.0.0.10");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain(200);
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiOAuth2AuthenticationFilter filter = new OpenApiOAuth2AuthenticationFilter(
        new StaticOAuth2TokenVerifier(), new AllowApiPermissionRepository(), auditRecorder, clock);

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
    assertEquals("tenant_a", auditRecorder.recorded.get().getTenantId());
    assertEquals("purchase-app", auditRecorder.recorded.get().getAppCode());
    assertEquals("oauth2:purchase-portal-client", auditRecorder.recorded.get().getAccessKey());
    assertEquals("purchase.request.view", auditRecorder.recorded.get().getApiCode());
    assertEquals("GET", auditRecorder.recorded.get().getHttpMethod());
    assertEquals("/openapi/v1/purchase/requests/1", auditRecorder.recorded.get().getRequestPath());
    assertTrue(auditRecorder.recorded.get().isSuccess());
    assertEquals("req-oauth2-001", auditRecorder.recorded.get().getRequestId());
  }

  /**
   * 验证小写 Bearer 前缀仍按 OAuth2 Bearer Token 处理。
   */
  @Test
  void shouldAuthenticateLowercaseBearerTokenAndRecordSuccessAudit()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader("Authorization", "bearer valid-token");
    request.setRemoteAddr("10.0.0.10");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain(200);
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiOAuth2AuthenticationFilter filter = new OpenApiOAuth2AuthenticationFilter(
        new StaticOAuth2TokenVerifier(), new AllowApiPermissionRepository(), auditRecorder, clock);

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
    assertEquals("tenant_a", auditRecorder.recorded.get().getTenantId());
    assertEquals("purchase-app", auditRecorder.recorded.get().getAppCode());
    assertEquals("oauth2:purchase-portal-client", auditRecorder.recorded.get().getAccessKey());
  }

  /**
   * 验证无效 Bearer Token 会被拒绝，且不会进入后续业务链路。
   */
  @Test
  void shouldRejectOpenApiRequestWhenBearerTokenInvalid()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader("Authorization", "Bearer invalid-token");
    request.addHeader(OpenApiOAuth2AuthenticationFilter.HEADER_REQUEST_ID, "req-invalid-token");
    request.setRemoteAddr("10.0.0.12");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiOAuth2AuthenticationFilter filter = new OpenApiOAuth2AuthenticationFilter(
        new StaticOAuth2TokenVerifier(), new AllowApiPermissionRepository(),
        auditRecorder, clock);

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    assertEquals("INVALID_TOKEN", response.getHeader("X-ZHYC-Openapi-Error"));
    assertOpenApiErrorBody(response, "INVALID_TOKEN");
    assertEquals(false, chain.called);
    assertEquals("UNKNOWN_TENANT", auditRecorder.recorded.get().getTenantId());
    assertEquals("UNKNOWN_APP", auditRecorder.recorded.get().getAppCode());
    assertEquals("oauth2:UNKNOWN_CLIENT", auditRecorder.recorded.get().getAccessKey());
    assertEquals("OPENAPI_OAUTH2_AUTHENTICATION", auditRecorder.recorded.get().getApiCode());
    assertEquals("GET", auditRecorder.recorded.get().getHttpMethod());
    assertEquals("/openapi/v1/purchase/requests/1", auditRecorder.recorded.get().getRequestPath());
    assertEquals(401, auditRecorder.recorded.get().getResponseStatus());
    assertEquals(false, auditRecorder.recorded.get().isSuccess());
    assertEquals("INVALID_TOKEN", auditRecorder.recorded.get().getErrorCode());
    assertEquals("10.0.0.12", auditRecorder.recorded.get().getClientIp());
    assertEquals("req-invalid-token", auditRecorder.recorded.get().getRequestId());
  }

  /**
   * 验证 OAuth2 鉴权失败审计写入异常时仍返回原始鉴权失败响应，避免审计存储故障放大为网关 500。
   */
  @Test
  void shouldKeepOAuth2AuthenticationFailureResponseWhenAuditRecorderThrows() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader("Authorization", "Bearer invalid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();
    OpenApiOAuth2AuthenticationFilter filter = new OpenApiOAuth2AuthenticationFilter(
        new StaticOAuth2TokenVerifier(), new AllowApiPermissionRepository(), record -> {
          throw new IllegalStateException("审计存储异常");
        }, clock);

    assertDoesNotThrow(() -> filter.doFilter(request, response, chain));

    assertEquals(401, response.getStatus());
    assertEquals("INVALID_TOKEN", response.getHeader("X-ZHYC-Openapi-Error"));
    assertEquals(false, chain.called);
  }

  /**
   * 验证 Bearer Token 有效但未授权访问开放 API 时会拒绝并记录审计。
   */
  @Test
  void shouldRejectBearerTokenWhenApiPermissionDeniedAndRecordAudit()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.addHeader("Authorization", "Bearer valid-token");
    request.setRemoteAddr("10.0.0.11");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiOAuth2AuthenticationFilter filter = new OpenApiOAuth2AuthenticationFilter(
        new StaticOAuth2TokenVerifier(), new DenyApiPermissionRepository(), auditRecorder, clock);

    filter.doFilter(request, response, chain);

    assertEquals(403, response.getStatus());
    assertEquals("PERMISSION_DENIED", response.getHeader("X-ZHYC-Openapi-Error"));
    assertOpenApiErrorBody(response, "PERMISSION_DENIED");
    assertEquals(false, chain.called);
    assertEquals("tenant_a", auditRecorder.recorded.get().getTenantId());
    assertEquals("purchase-app", auditRecorder.recorded.get().getAppCode());
    assertEquals("oauth2:purchase-portal-client", auditRecorder.recorded.get().getAccessKey());
    assertEquals("PERMISSION_DENIED", auditRecorder.recorded.get().getErrorCode());
  }

  /**
   * 验证 Bearer Token 有效但客户端 IP 不在应用白名单时会拒绝并记录审计。
   */
  @Test
  void shouldRejectBearerTokenWhenClientIpNotAllowedAndRecordAudit()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader("Authorization", "Bearer valid-token");
    request.setRemoteAddr("10.0.0.99");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiOAuth2AuthenticationFilter filter = new OpenApiOAuth2AuthenticationFilter(
        new StaticOAuth2TokenVerifier(), new AllowApiPermissionRepository(),
        new DenyApiClientIpWhitelistRepository(), auditRecorder, clock);

    filter.doFilter(request, response, chain);

    assertEquals(403, response.getStatus());
    assertEquals("IP_NOT_ALLOWED", response.getHeader("X-ZHYC-Openapi-Error"));
    assertOpenApiErrorBody(response, "IP_NOT_ALLOWED");
    assertEquals(false, chain.called);
    assertEquals("tenant_a", auditRecorder.recorded.get().getTenantId());
    assertEquals("purchase-app", auditRecorder.recorded.get().getAppCode());
    assertEquals("oauth2:purchase-portal-client", auditRecorder.recorded.get().getAccessKey());
    assertEquals("IP_NOT_ALLOWED", auditRecorder.recorded.get().getErrorCode());
    assertEquals("10.0.0.99", auditRecorder.recorded.get().getClientIp());
  }

  /**
   * 验证非 Open API 路径的 Bearer Token 请求不参与本过滤器处理。
   */
  @Test
  void shouldPassThroughNonOpenApiRequestWithBearerToken() throws ServletException, IOException {
    AtomicInteger verifyTimes = new AtomicInteger(0);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    OpenApiOAuth2AuthenticationFilter filter = new OpenApiOAuth2AuthenticationFilter(
        new CountingOAuth2TokenVerifier(verifyTimes), new AllowApiPermissionRepository(),
        new RecordingApiCallAuditRecorder(), clock);

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(0, verifyTimes.get());
  }

  /**
   * 验证 Open API 路径但未携带 Bearer Token 的请求透传，不触发 Token 校验。
   */
  @Test
  void shouldPassThroughOpenApiRequestWithoutBearerToken() throws ServletException, IOException {
    AtomicInteger verifyTimes = new AtomicInteger(0);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    OpenApiOAuth2AuthenticationFilter filter = new OpenApiOAuth2AuthenticationFilter(
        new CountingOAuth2TokenVerifier(verifyTimes), new AllowApiPermissionRepository(),
        new RecordingApiCallAuditRecorder(), clock);

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(0, verifyTimes.get());
  }

  /**
   * 测试用 OAuth2 Token 校验器。
   */
  private static final class StaticOAuth2TokenVerifier implements OAuth2TokenVerifier {

    @Override
    public OAuth2TokenAuthenticationResult verify(String accessToken) {
      if ("valid-token".equals(accessToken)) {
        return OAuth2TokenAuthenticationResult.success("tenant_a", "purchase-app",
            "purchase-portal-client");
      }
      return OAuth2TokenAuthenticationResult.failure("INVALID_TOKEN");
    }
  }

  /**
   * 断言开放 API 鉴权失败响应包含稳定 JSON 错误体。
   *
   * @param response HTTP 响应
   * @param code 期望错误码
   */
  private void assertOpenApiErrorBody(MockHttpServletResponse response, String code) throws IOException {
    assertTrue(response.getContentType().startsWith("application/json"));
    assertTrue(response.getContentAsString().contains("\"code\":\"" + code + "\""));
  }

  /**
   * 测试用计数 OAuth2 Token 校验器。
   */
  private static final class CountingOAuth2TokenVerifier implements OAuth2TokenVerifier {

    /** 校验次数。 */
    private final AtomicInteger verifyTimes;

    private CountingOAuth2TokenVerifier(AtomicInteger verifyTimes) {
      this.verifyTimes = verifyTimes;
    }

    @Override
    public OAuth2TokenAuthenticationResult verify(String accessToken) {
      verifyTimes.incrementAndGet();
      if ("valid-token".equals(accessToken)) {
        return OAuth2TokenAuthenticationResult.success("tenant_a", "purchase-app",
            "purchase-portal-client");
      }
      return OAuth2TokenAuthenticationResult.failure("INVALID_TOKEN");
    }
  }

  /**
   * 测试用过滤器链。
   */
  private static final class RecordingFilterChain implements FilterChain {

    /** 是否已调用后续过滤器链。 */
    private boolean called;
    /** 后续链路设置的响应状态码。 */
    private final int responseStatus;

    private RecordingFilterChain() {
      this(200);
    }

    private RecordingFilterChain(int responseStatus) {
      this.responseStatus = responseStatus;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {
      called = true;
      request.getInputStream().readAllBytes();
      if (response instanceof MockHttpServletResponse httpResponse) {
        httpResponse.setStatus(responseStatus);
      }
    }
  }

  /**
   * 测试用允许访问的权限仓储。
   */
  private static final class AllowApiPermissionRepository implements ApiPermissionRepository {

    @Override
    public boolean isAllowed(String tenantId, String appCode, String httpMethod, String requestPath) {
      return true;
    }

    @Override
    public String resolveApiCode(String tenantId, String appCode, String httpMethod, String requestPath) {
      return "purchase.request.view";
    }
  }

  /**
   * 测试用拒绝访问的权限仓储。
   */
  private static final class DenyApiPermissionRepository implements ApiPermissionRepository {

    @Override
    public boolean isAllowed(String tenantId, String appCode, String httpMethod, String requestPath) {
      return false;
    }
  }

  /**
   * 测试用拒绝所有客户端 IP 的应用白名单仓储。
   */
  private static final class DenyApiClientIpWhitelistRepository implements ApiClientIpWhitelistRepository {

    @Override
    public boolean isAllowed(String tenantId, String appCode, String clientIp) {
      return false;
    }
  }

  /**
   * 测试用调用审计记录器。
   */
  private static final class RecordingApiCallAuditRecorder implements ApiCallAuditRecorder {

    /** 最近一次记录的审计内容。 */
    private final AtomicReference<ApiCallAuditRecord> recorded = new AtomicReference<>();

    @Override
    public void record(ApiCallAuditRecord record) {
      recorded.set(record);
    }
  }
}
