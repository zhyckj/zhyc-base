/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 路由过滤器测试。
 */
class OpenApiRoutingFilterTest {

  /**
   * 验证命中已发布路由后会调用后端转发器并写回响应。
   *
   * @throws Exception 路由过滤失败时抛出
   */
  @Test
  void shouldInvokeBackendWhenRouteFound() throws Exception {
    RecordingBackendInvoker invoker = new RecordingBackendInvoker();
    OpenApiRoutingFilter filter = new OpenApiRoutingFilter(new StaticRouteRepository(), invoker);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.setContent("{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
    request.addHeader("X-ZHYC-Request-Id", "req-001");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_TENANT_ID, "tenant_a");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_APP_CODE, "purchase-app");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_API_CODE, "purchase.request.create");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new RecordingFilterChain());

    assertEquals("http://purchase-service/internal/purchase/requests", invoker.lastRoute.getBackendRoute());
    assertEquals("POST", invoker.lastMethod);
    assertEquals("{\"amount\":100}", invoker.lastBody);
    assertEquals("tenant_a", invoker.lastHeaders.get("X-ZHYC-Tenant-Id"));
    assertEquals("req-001", invoker.lastHeaders.get("X-ZHYC-Request-Id"));
    assertEquals("purchase-app", invoker.lastHeaders.get("X-ZHYC-App-Code"));
    assertEquals("purchase.request.create", invoker.lastHeaders.get("X-ZHYC-Api-Code"));
    assertEquals(202, response.getStatus());
    assertEquals("accepted", response.getContentAsString());
  }

  /**
   * 验证存在 contextPath 时路由匹配使用去除 contextPath 后的路径。
   *
   * @throws Exception 路由过滤失败时抛出
   */
  @Test
  void shouldStripContextPathBeforeFindRoute() throws Exception {
    RecordingBackendInvoker invoker = new RecordingBackendInvoker();
    ContextPathRouteRepository routeRepository = new ContextPathRouteRepository("/api");
    OpenApiRoutingFilter filter = new OpenApiRoutingFilter(routeRepository, invoker);

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/openapi/v1/purchase/requests");
    request.setContextPath("/api");
    request.setContent("{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
    setOpenApiAuthenticationContext(request);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new RecordingFilterChain());

    assertEquals("/openapi/v1/purchase/requests", routeRepository.lastPath);
    assertEquals("http://purchase-service/internal/purchase/requests", invoker.lastRoute.getBackendRoute());
    assertEquals(202, response.getStatus());
  }

  /**
   * 验证开放 API 查询参数会传递给后端服务，避免查询类接口参数丢失。
   *
   * @throws Exception 路由过滤失败时抛出
   */
  @Test
  void shouldForwardQueryStringToBackendRequest() throws Exception {
    RecordingBackendInvoker invoker = new RecordingBackendInvoker();
    OpenApiRoutingFilter filter = new OpenApiRoutingFilter(new StaticRouteRepository(), invoker);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/purchase/requests");
    request.setQueryString("status=pending&page=1");
    setOpenApiAuthenticationContext(request);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new RecordingFilterChain());

    assertEquals("status=pending&page=1", invoker.lastQueryString);
    assertEquals(202, response.getStatus());
  }

  /**
   * 验证路由转发只携带网关生成的上下文头，不透传外部开放平台凭据。
   *
   * @throws Exception 路由过滤失败时抛出
   */
  @Test
  void shouldOnlyForwardGatewayContextHeadersToBackend() throws Exception {
    RecordingBackendInvoker invoker = new RecordingBackendInvoker();
    OpenApiRoutingFilter filter = new OpenApiRoutingFilter(new StaticRouteRepository(), invoker);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.addHeader("Authorization", "Bearer external-token");
    request.addHeader("X-ZHYC-Access-Key", "ak_external");
    request.addHeader("X-ZHYC-Signature", "external-signature");
    request.addHeader("X-ZHYC-Tenant-Id", "forged-tenant");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_TENANT_ID, "tenant_a");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_APP_CODE, "purchase-app");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_API_CODE, "purchase.request.create");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new RecordingFilterChain());

    assertEquals(null, invoker.lastHeaders.get("Authorization"));
    assertEquals(null, invoker.lastHeaders.get("X-ZHYC-Access-Key"));
    assertEquals(null, invoker.lastHeaders.get("X-ZHYC-Signature"));
    assertEquals("tenant_a", invoker.lastHeaders.get("X-ZHYC-Tenant-Id"));
    assertEquals(202, response.getStatus());
  }

  /**
   * 验证路由过滤器会把允许外显的后端响应头写回调用方。
   *
   * @throws Exception 路由过滤失败时抛出
   */
  @Test
  void shouldWriteAllowedBackendResponseHeaders() throws Exception {
    RecordingBackendInvoker invoker = new RecordingBackendInvoker();
    invoker.responseHeaders = Map.of("X-ZHYC-Request-Id", "req-001",
        "Content-Disposition", "attachment; filename=result.json");
    OpenApiRoutingFilter filter = new OpenApiRoutingFilter(new StaticRouteRepository(), invoker);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    setOpenApiAuthenticationContext(request);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new RecordingFilterChain());

    assertEquals("req-001", response.getHeader("X-ZHYC-Request-Id"));
    assertEquals("attachment; filename=result.json", response.getHeader("Content-Disposition"));
    assertEquals(202, response.getStatus());
  }

  /**
   * 验证路由过滤器二次拦截敏感后端响应头，避免自定义后端调用器绕过响应头白名单。
   *
   * @throws Exception 路由过滤失败时抛出
   */
  @Test
  void shouldFilterSensitiveBackendResponseHeadersBeforeReturnToCaller() throws Exception {
    RecordingBackendInvoker invoker = new RecordingBackendInvoker();
    invoker.responseHeaders = Map.of("X-ZHYC-Request-Id", "req-001",
        "X-ZHYC-Internal-Token", "internal-secret", "Set-Cookie", "SESSION=unsafe",
        "Authorization", "Bearer internal-token");
    OpenApiRoutingFilter filter = new OpenApiRoutingFilter(new StaticRouteRepository(), invoker);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    setOpenApiAuthenticationContext(request);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new RecordingFilterChain());

    assertEquals("req-001", response.getHeader("X-ZHYC-Request-Id"));
    assertEquals(null, response.getHeader("X-ZHYC-Internal-Token"));
    assertEquals(null, response.getHeader("Set-Cookie"));
    assertEquals(null, response.getHeader("Authorization"));
    assertEquals(202, response.getStatus());
  }

  /**
   * 验证未找到路由时返回 404，避免请求落到空控制器。
   *
   * @throws Exception 路由过滤失败时抛出
   */
  @Test
  void shouldReturnNotFoundWhenRouteMissing() throws Exception {
    OpenApiRoutingFilter filter = new OpenApiRoutingFilter((method, path) -> Optional.empty(),
        new RecordingBackendInvoker());
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openapi/v1/missing");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new RecordingFilterChain());

    assertEquals(404, response.getStatus());
    assertEquals("OPENAPI_ROUTE_NOT_FOUND", response.getHeader("X-ZHYC-Openapi-Error"));
    assertTrue(response.getContentType().startsWith("application/json"));
    assertEquals("{\"code\":\"OPENAPI_ROUTE_NOT_FOUND\",\"message\":\"开放 API 路由不存在\"}",
        response.getContentAsString());
  }

  /**
   * 验证缺失鉴权上下文时不会执行后端转发，避免过滤器顺序异常导致开放 API 绕过鉴权。
   *
   * @throws Exception 路由过滤失败时抛出
   */
  @Test
  void shouldRejectRoutingWhenAuthenticationContextMissing() throws Exception {
    RecordingBackendInvoker invoker = new RecordingBackendInvoker();
    OpenApiRoutingFilter filter = new OpenApiRoutingFilter(new StaticRouteRepository(), invoker);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.setContent("{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new RecordingFilterChain());

    assertEquals(401, response.getStatus());
    assertEquals("OPENAPI_AUTH_CONTEXT_MISSING", response.getHeader("X-ZHYC-Openapi-Error"));
    assertEquals(null, invoker.lastRoute);
  }

  private static final class StaticRouteRepository implements OpenApiRouteRepository {

    @Override
    public Optional<OpenApiRoute> findRoute(String httpMethod, String requestPath) {
      return Optional.of(new OpenApiRoute("purchase.request.create",
          "http://purchase-service/internal/purchase/requests"));
    }
  }

  /**
   * 写入测试用开放 API 鉴权上下文。
   *
   * @param request 测试 HTTP 请求
   */
  private void setOpenApiAuthenticationContext(MockHttpServletRequest request) {
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_TENANT_ID, "tenant_a");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_APP_CODE, "purchase-app");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_API_CODE, "purchase.request.create");
  }

  /**
   * 测试用 contextPath 场景路由仓储。
   */
  private static final class ContextPathRouteRepository implements OpenApiRouteRepository {

    /** 待验证的 contextPath。 */
    private final String contextPath;
    /** 最近一次查询到的 requestPath。 */
    private String lastPath;

    private ContextPathRouteRepository(String contextPath) {
      this.contextPath = contextPath;
    }

    @Override
    public Optional<OpenApiRoute> findRoute(String httpMethod, String requestPath) {
      lastPath = requestPath;
      if (requestPath.equals(contextPath + "/openapi/v1/purchase/requests")) {
        return Optional.empty();
      }
      if (!requestPath.equals("/openapi/v1/purchase/requests")) {
        return Optional.empty();
      }
      return Optional.of(new OpenApiRoute("purchase.request.create",
          "http://purchase-service/internal/purchase/requests"));
    }
  }

  private static final class RecordingBackendInvoker implements OpenApiBackendInvoker {

    /** 最近一次转发路由。 */
    private OpenApiRoute lastRoute;
    /** 最近一次 HTTP 方法。 */
    private String lastMethod;
    /** 最近一次请求体。 */
    private String lastBody;
    /** 最近一次查询字符串。 */
    private String lastQueryString;
    /** 最近一次转发请求头。 */
    private Map<String, String> lastHeaders;
    /** 模拟后端响应头。 */
    private Map<String, String> responseHeaders = Map.of();

    @Override
    public OpenApiBackendResponse invoke(OpenApiRoute route, OpenApiBackendRequest request) {
      lastRoute = route;
      lastMethod = request.getHttpMethod();
      lastBody = request.getBody();
      lastQueryString = request.getQueryString();
      lastHeaders = request.getHeaders();
      return new OpenApiBackendResponse(202, "text/plain", "accepted", responseHeaders);
    }
  }

  private static final class RecordingFilterChain implements FilterChain {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
      throw new AssertionError("开放 API 路由过滤器处理后不应继续进入后续链路");
    }
  }
}
