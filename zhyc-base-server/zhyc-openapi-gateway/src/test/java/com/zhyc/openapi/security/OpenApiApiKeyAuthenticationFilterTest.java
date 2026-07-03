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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Open API API Key 鉴权过滤器测试。
 */
class OpenApiApiKeyAuthenticationFilterTest {

  /** 固定测试时间戳。 */
  private static final String TIMESTAMP = "1782259200";

  /** API Key 签名校验器。 */
  private final ApiKeySignatureVerifier verifier = new ApiKeySignatureVerifier();
  /** 固定时钟。 */
  private final Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);
  /** API Key 鉴权服务。 */
  private final ApiKeyAuthenticator authenticator = new ApiKeyAuthenticator(
      new StaticCredentialRepository(), verifier, new ReplayProtector(Duration.ofMinutes(5), clock, 100),
      clock);
  /** Open API API Key 鉴权过滤器。 */
  private final OpenApiApiKeyAuthenticationFilter filter = new OpenApiApiKeyAuthenticationFilter(authenticator);

  /**
   * 验证 Open API 请求在 API Key 鉴权通过但未授权访问接口时被拒绝，并记录失败审计。
   */
  @Test
  void shouldRejectOpenApiRequestWhenApiPermissionDeniedAndRecordAudit()
      throws ServletException, IOException {
    String body = "{\"amount\":100}";
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.setContent(body.getBytes(StandardCharsets.UTF_8));
    request.setRemoteAddr("10.0.0.8");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_ACCESS_KEY, "ak_test");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_TIMESTAMP, TIMESTAMP);
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_NONCE, "nonce-filter-003");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_SIGNATURE,
        sign("POST", "/openapi/v1/purchase/requests", TIMESTAMP, "nonce-filter-003", body));
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiApiKeyAuthenticationFilter permissionFilter = new OpenApiApiKeyAuthenticationFilter(
        authenticator, new DenyApiPermissionRepository(), auditRecorder, clock);

    permissionFilter.doFilter(request, response, chain);

    assertEquals(403, response.getStatus());
    assertEquals("PERMISSION_DENIED", response.getHeader("X-ZHYC-Openapi-Error"));
    assertOpenApiErrorBody(response, "PERMISSION_DENIED");
    assertEquals(false, chain.called);
    assertEquals("tenant_a", auditRecorder.recorded.get().getTenantId());
    assertEquals("purchase-app", auditRecorder.recorded.get().getAppCode());
    assertEquals("ak_test", auditRecorder.recorded.get().getAccessKey());
    assertEquals("/openapi/v1/purchase/requests", auditRecorder.recorded.get().getApiCode());
    assertEquals(403, auditRecorder.recorded.get().getResponseStatus());
    assertEquals(false, auditRecorder.recorded.get().isSuccess());
    assertEquals("PERMISSION_DENIED", auditRecorder.recorded.get().getErrorCode());
    assertEquals("10.0.0.8", auditRecorder.recorded.get().getClientIp());
  }

  /**
   * 验证 API Key 鉴权通过但客户端 IP 不在应用白名单时会被拒绝，并记录失败审计。
   */
  @Test
  void shouldRejectOpenApiRequestWhenClientIpNotAllowedAndRecordAudit()
      throws ServletException, IOException {
    String body = "{\"amount\":100}";
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.setContent(body.getBytes(StandardCharsets.UTF_8));
    request.setRemoteAddr("10.0.0.99");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_ACCESS_KEY, "ak_test");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_TIMESTAMP, TIMESTAMP);
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_NONCE, "nonce-filter-005");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_SIGNATURE,
        sign("POST", "/openapi/v1/purchase/requests", TIMESTAMP, "nonce-filter-005", body));
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiApiKeyAuthenticationFilter whitelistFilter = new OpenApiApiKeyAuthenticationFilter(
        authenticator, new AllowApiPermissionRepository(), new DenyApiClientIpWhitelistRepository(),
        auditRecorder, clock);

    whitelistFilter.doFilter(request, response, chain);

    assertEquals(403, response.getStatus());
    assertEquals("IP_NOT_ALLOWED", response.getHeader("X-ZHYC-Openapi-Error"));
    assertOpenApiErrorBody(response, "IP_NOT_ALLOWED");
    assertEquals(false, chain.called);
    assertEquals("tenant_a", auditRecorder.recorded.get().getTenantId());
    assertEquals("purchase-app", auditRecorder.recorded.get().getAppCode());
    assertEquals("ak_test", auditRecorder.recorded.get().getAccessKey());
    assertEquals("/openapi/v1/purchase/requests", auditRecorder.recorded.get().getApiCode());
    assertEquals(403, auditRecorder.recorded.get().getResponseStatus());
    assertEquals(false, auditRecorder.recorded.get().isSuccess());
    assertEquals("IP_NOT_ALLOWED", auditRecorder.recorded.get().getErrorCode());
    assertEquals("10.0.0.99", auditRecorder.recorded.get().getClientIp());
  }

  /**
   * 验证 Open API 请求授权通过并完成业务链路后会写入成功调用审计。
   */
  @Test
  void shouldRecordSuccessfulOpenApiCallAuditAfterChain()
      throws ServletException, IOException {
    String body = "{\"amount\":100}";
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.setContent(body.getBytes(StandardCharsets.UTF_8));
    request.setRemoteAddr("10.0.0.9");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_REQUEST_ID, "req-openapi-001");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_ACCESS_KEY, "ak_test");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_TIMESTAMP, TIMESTAMP);
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_NONCE, "nonce-filter-004");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_SIGNATURE,
        sign("POST", "/openapi/v1/purchase/requests", TIMESTAMP, "nonce-filter-004", body));
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain(201);
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiApiKeyAuthenticationFilter permissionFilter = new OpenApiApiKeyAuthenticationFilter(
        authenticator, new AllowApiPermissionRepository(), auditRecorder, clock);

    permissionFilter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(201, response.getStatus());
    assertEquals("tenant_a", auditRecorder.recorded.get().getTenantId());
    assertEquals("purchase-app", auditRecorder.recorded.get().getAppCode());
    assertEquals("ak_test", auditRecorder.recorded.get().getAccessKey());
    assertEquals("purchase.request.create", auditRecorder.recorded.get().getApiCode());
    assertEquals("POST", auditRecorder.recorded.get().getHttpMethod());
    assertEquals("/openapi/v1/purchase/requests", auditRecorder.recorded.get().getRequestPath());
    assertEquals(201, auditRecorder.recorded.get().getResponseStatus());
    assertTrue(auditRecorder.recorded.get().isSuccess());
    assertEquals(null, auditRecorder.recorded.get().getErrorCode());
    assertEquals("10.0.0.9", auditRecorder.recorded.get().getClientIp());
    assertEquals("req-openapi-001", auditRecorder.recorded.get().getRequestId());
    assertEquals(Instant.parse("2026-06-24T00:00:00Z"), auditRecorder.recorded.get().getCalledAt());
  }

  @Test
  void shouldPassThroughNonOpenApiRequestWithoutAuthenticationHeaders()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
  }

  /**
   * 验证携带 OAuth2 Bearer Token 的开放 API 请求不会被 API Key 过滤器误拦截。
   */
  @Test
  void shouldPassThroughOpenApiRequestWithBearerTokenForOauth2Filter()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader("Authorization", "Bearer jwt-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
  }

  /**
   * 验证小写 Bearer 前缀的 OAuth2 请求不会被 API Key 过滤器误拦截。
   */
  @Test
  void shouldPassThroughOpenApiRequestWithLowercaseBearerTokenForOauth2Filter()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader("Authorization", "bearer jwt-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
  }

  @Test
  void shouldAuthenticateOpenApiRequestAndKeepBodyReadableForNextFilter()
      throws ServletException, IOException {
    String body = "{\"amount\":100}";
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.setContent(body.getBytes(StandardCharsets.UTF_8));
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_ACCESS_KEY, "ak_test");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_TIMESTAMP, TIMESTAMP);
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_NONCE, "nonce-filter-001");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_SIGNATURE,
        sign("POST", "/openapi/v1/purchase/requests", TIMESTAMP, "nonce-filter-001", body));
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
    assertEquals(body, chain.bodyReadByNextFilter);
  }

  /**
   * 验证 API Key 签名会绑定原始查询字符串，避免查询参数在网关前被篡改后仍可通过验签。
   */
  @Test
  void shouldAuthenticateOpenApiRequestWhenSignatureIncludesQueryString()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests");
    request.setQueryString("status=pending&page=1");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_ACCESS_KEY, "ak_test");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_TIMESTAMP, TIMESTAMP);
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_NONCE, "nonce-filter-006");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_SIGNATURE,
        sign("GET", "/openapi/v1/purchase/requests?status=pending&page=1", TIMESTAMP,
            "nonce-filter-006", null));
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
  }

  @Test
  void shouldRejectOpenApiRequestWhenSignatureInvalid() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_ACCESS_KEY, "ak_test");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_TIMESTAMP, TIMESTAMP);
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_NONCE, "nonce-filter-002");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_SIGNATURE, "bad-signature");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    assertEquals("INVALID_SIGNATURE", response.getHeader("X-ZHYC-Openapi-Error"));
    assertOpenApiErrorBody(response, "INVALID_SIGNATURE");
    assertEquals(false, chain.called);
  }

  /**
   * 验证 API Key 鉴权失败也会写入调用审计，便于追踪签名攻击和错误接入。
   */
  @Test
  void shouldRecordAuditWhenApiKeyAuthenticationFails() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.setRemoteAddr("10.0.0.7");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_REQUEST_ID, "req-invalid-signature");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_ACCESS_KEY, "ak_test");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_TIMESTAMP, TIMESTAMP);
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_NONCE, "nonce-filter-007");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_SIGNATURE, "bad-signature");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();
    RecordingApiCallAuditRecorder auditRecorder = new RecordingApiCallAuditRecorder();
    OpenApiApiKeyAuthenticationFilter auditFilter = new OpenApiApiKeyAuthenticationFilter(
        authenticator, new AllowApiPermissionRepository(), auditRecorder, clock);

    auditFilter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    assertEquals(false, chain.called);
    assertEquals("UNKNOWN_TENANT", auditRecorder.recorded.get().getTenantId());
    assertEquals("UNKNOWN_APP", auditRecorder.recorded.get().getAppCode());
    assertEquals("ak_test", auditRecorder.recorded.get().getAccessKey());
    assertEquals("OPENAPI_AUTHENTICATION", auditRecorder.recorded.get().getApiCode());
    assertEquals("GET", auditRecorder.recorded.get().getHttpMethod());
    assertEquals("/openapi/v1/purchase/requests/1", auditRecorder.recorded.get().getRequestPath());
    assertEquals(401, auditRecorder.recorded.get().getResponseStatus());
    assertEquals(false, auditRecorder.recorded.get().isSuccess());
    assertEquals("INVALID_SIGNATURE", auditRecorder.recorded.get().getErrorCode());
    assertEquals("10.0.0.7", auditRecorder.recorded.get().getClientIp());
    assertEquals("req-invalid-signature", auditRecorder.recorded.get().getRequestId());
  }

  /**
   * 验证 API Key 鉴权失败审计写入异常时仍返回原始鉴权失败响应，避免审计存储故障放大为网关 500。
   */
  @Test
  void shouldKeepAuthenticationFailureResponseWhenAuditRecorderThrows() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests/1");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_ACCESS_KEY, "ak_test");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_TIMESTAMP, TIMESTAMP);
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_NONCE, "nonce-filter-008");
    request.addHeader(OpenApiApiKeyAuthenticationFilter.HEADER_SIGNATURE, "bad-signature");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();
    OpenApiApiKeyAuthenticationFilter auditFilter = new OpenApiApiKeyAuthenticationFilter(
        authenticator, new AllowApiPermissionRepository(), record -> {
          throw new IllegalStateException("审计存储异常");
        }, clock);

    assertDoesNotThrow(() -> auditFilter.doFilter(request, response, chain));

    assertEquals(401, response.getStatus());
    assertEquals("INVALID_SIGNATURE", response.getHeader("X-ZHYC-Openapi-Error"));
    assertEquals(false, chain.called);
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

  private String sign(String method, String path, String timestamp, String nonce, String body) {
    return verifier.sign(method, path, timestamp, nonce, body, "runtime-secret");
  }

  /**
   * 测试用固定凭证仓储。
   */
  private static final class StaticCredentialRepository implements ApiKeyCredentialRepository {

    @Override
    public Optional<ApiKeyCredential> findByAccessKey(String accessKey) {
      if ("ak_test".equals(accessKey)) {
        return Optional.of(new ApiKeyCredential("tenant_a", "purchase-app", "ak_test",
            "runtime-secret", "enabled", Instant.parse("2026-12-31T00:00:00Z")));
      }
      return Optional.empty();
    }
  }

  /**
   * 测试用过滤器链，记录是否放行和后续链路读取到的请求体。
   */
  private static final class RecordingFilterChain implements FilterChain {

    /** 是否已调用后续过滤器链。 */
    private boolean called;
    /** 后续链路读取到的请求体。 */
    private String bodyReadByNextFilter;
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
      bodyReadByNextFilter = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      if (response instanceof MockHttpServletResponse httpResponse) {
        httpResponse.setStatus(responseStatus);
      }
    }
  }

  /**
   * 测试用拒绝所有访问的权限仓储。
   */
  private static final class DenyApiPermissionRepository implements ApiPermissionRepository {

    @Override
    public boolean isAllowed(String tenantId, String appCode, String httpMethod, String requestPath) {
      return false;
    }
  }

  /**
   * 测试用允许采购申请创建接口的权限仓储。
   */
  private static final class AllowApiPermissionRepository implements ApiPermissionRepository {

    @Override
    public boolean isAllowed(String tenantId, String appCode, String httpMethod, String requestPath) {
      return true;
    }

    @Override
    public String resolveApiCode(String tenantId, String appCode, String httpMethod, String requestPath) {
      return "purchase.request.create";
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
