/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 开放 API 路由转发过滤器。
 */
public class OpenApiRoutingFilter implements Filter {

  /** 路由缺失错误编码。 */
  private static final String ROUTE_NOT_FOUND = "OPENAPI_ROUTE_NOT_FOUND";
  /** 路由缺失错误消息。 */
  private static final String ROUTE_NOT_FOUND_MESSAGE = "开放 API 路由不存在";
  /** 鉴权上下文缺失错误编码。 */
  private static final String AUTH_CONTEXT_MISSING = "OPENAPI_AUTH_CONTEXT_MISSING";
  /** 鉴权上下文缺失错误消息。 */
  private static final String AUTH_CONTEXT_MISSING_MESSAGE = "开放 API 鉴权上下文缺失";
  /** 鉴权失败原因响应头。 */
  private static final String HEADER_ERROR = "X-ZHYC-Openapi-Error";
  /** 转发给业务后端的租户头。 */
  private static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
  /** 转发给业务后端的请求追踪头。 */
  private static final String HEADER_REQUEST_ID = "X-ZHYC-Request-Id";
  /** 转发给业务后端的开发者应用编码头。 */
  private static final String HEADER_APP_CODE = "X-ZHYC-App-Code";
  /** 转发给业务后端的开放 API 编码头。 */
  private static final String HEADER_API_CODE = "X-ZHYC-Api-Code";

  /** 运行态路由仓储。 */
  private final OpenApiRouteRepository routeRepository;
  /** 后端服务调用器。 */
  private final OpenApiBackendInvoker backendInvoker;

  /**
   * 创建开放 API 路由转发过滤器。
   *
   * @param routeRepository 运行态路由仓储
   * @param backendInvoker 后端服务调用器
   */
  public OpenApiRoutingFilter(OpenApiRouteRepository routeRepository, OpenApiBackendInvoker backendInvoker) {
    this.routeRepository = Objects.requireNonNull(routeRepository, "运行态路由仓储不能为空");
    this.backendInvoker = Objects.requireNonNull(backendInvoker, "后端服务调用器不能为空");
  }

  /**
   * 执行 Open API 路由转发链路。
   *
   * <p>按请求路径与方法查询运行态路由，命中后构造统一请求体并转发到后端服务；
   * 未命中返回 404。</p>
   *
   * <p>风险边界：仅转发鉴权链路写入属性后的请求；无路由元数据时不尝试兜底。
   * 失败场景：读取请求体失败或后端返回异常会抛出异常并交由上层统一处理。</p>
   *
   * @param request servlet 请求
   * @param response servlet 响应
   * @param chain 过滤器链
   * @throws IOException io 异常
   * @throws ServletException servlet 异常
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest httpRequest)
        || !(response instanceof HttpServletResponse httpResponse)) {
      chain.doFilter(request, response);
      return;
    }
    String requestPath = buildRequestPath(httpRequest);
    Optional<OpenApiRoute> route = routeRepository.findRoute(httpRequest.getMethod(), requestPath);
    if (route.isEmpty()) {
      writeError(httpResponse, HttpServletResponse.SC_NOT_FOUND, ROUTE_NOT_FOUND, ROUTE_NOT_FOUND_MESSAGE);
      return;
    }
    if (!hasAuthenticationContext(httpRequest)) {
      writeError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, AUTH_CONTEXT_MISSING,
          AUTH_CONTEXT_MISSING_MESSAGE);
      return;
    }
    OpenApiBackendRequest backendRequest = new OpenApiBackendRequest(httpRequest.getMethod(),
        readBody(httpRequest), httpRequest.getContentType(), httpRequest.getQueryString(),
        buildForwardHeaders(httpRequest));
    OpenApiBackendResponse backendResponse = backendInvoker.invoke(route.get(), backendRequest);
    writeBackendResponse(httpResponse, backendResponse);
  }

  private String readBody(HttpServletRequest request) throws IOException {
    return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  /**
   * 规范化请求路径，去除 contextPath，避免上下文部署导致路由匹配失败。
   *
   * @param request HTTP 请求
   * @return 去除 contextPath 后的请求路径
   */
  private String buildRequestPath(HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
      return requestUri.substring(contextPath.length());
    }
    return requestUri;
  }

  /**
   * 构造允许透传给业务后端的开放 API 上下文头。
   *
   * <p>租户、应用和 API 编码优先取鉴权链路写入的 request attribute；请求追踪编号取调用方请求头。
   * 外部签名、Access Key 和 Authorization 不透传，避免业务服务接触开放平台凭据。</p>
   *
   * @param request HTTP 请求
   * @return 后端转发头
   */
  private Map<String, String> buildForwardHeaders(HttpServletRequest request) {
    Map<String, String> headers = new LinkedHashMap<>();
    putIfPresent(headers, HEADER_TENANT_ID, attributeAsString(request, OpenApiRateLimitFilter.ATTRIBUTE_TENANT_ID));
    putIfPresent(headers, HEADER_APP_CODE, attributeAsString(request, OpenApiRateLimitFilter.ATTRIBUTE_APP_CODE));
    putIfPresent(headers, HEADER_API_CODE, attributeAsString(request, OpenApiRateLimitFilter.ATTRIBUTE_API_CODE));
    putIfPresent(headers, HEADER_REQUEST_ID, request.getHeader(HEADER_REQUEST_ID));
    return headers;
  }

  private String attributeAsString(HttpServletRequest request, String attributeName) {
    Object value = request.getAttribute(attributeName);
    return value == null ? null : String.valueOf(value);
  }

  /**
   * 判断请求是否已经由前置鉴权链路写入开放 API 上下文。
   *
   * <p>路由过滤器再次校验租户、应用和 API 编码，防止过滤器顺序异常或测试装配错误时绕过鉴权直接转发。</p>
   *
   * @param request HTTP 请求
   * @return 鉴权上下文完整返回 {@code true}
   */
  private boolean hasAuthenticationContext(HttpServletRequest request) {
    return hasText(attributeAsString(request, OpenApiRateLimitFilter.ATTRIBUTE_TENANT_ID))
        && hasText(attributeAsString(request, OpenApiRateLimitFilter.ATTRIBUTE_APP_CODE))
        && hasText(attributeAsString(request, OpenApiRateLimitFilter.ATTRIBUTE_API_CODE));
  }

  /**
   * 判断文本是否包含有效内容。
   *
   * @param value 待检查文本
   * @return 非空且非空白返回 {@code true}
   */
  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private void putIfPresent(Map<String, String> headers, String headerName, String value) {
    if (value != null && !value.isBlank()) {
      headers.put(headerName, value.trim());
    }
  }

  private void writeBackendResponse(HttpServletResponse response, OpenApiBackendResponse backendResponse)
      throws IOException {
    response.setStatus(backendResponse.getStatus());
    backendResponse.getHeaders().forEach((headerName, value) -> {
      if (OpenApiResponseHeaderPolicy.isAllowedResponseHeader(headerName)) {
        response.setHeader(headerName, value);
      }
    });
    if (backendResponse.getContentType() != null && !backendResponse.getContentType().isBlank()) {
      response.setContentType(backendResponse.getContentType());
    }
    response.getWriter().write(backendResponse.getBody() == null ? "" : backendResponse.getBody());
  }

  /**
   * 写入开放 API 路由失败响应。
   *
   * @param response HTTP 响应
   * @param status HTTP 状态码
   * @param code 稳定错误码
   * @param message 面向调用方的中文错误消息
   * @throws IOException 响应写入失败
   */
  private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
    response.setStatus(status);
    response.setHeader(HEADER_ERROR, code);
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.getWriter().write("{\"code\":\"" + escapeJson(code) + "\",\"message\":\""
        + escapeJson(message) + "\"}");
  }

  /**
   * 转义 JSON 字符串值。
   *
   * @param value 原始文本
   * @return JSON 安全文本
   */
  private String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
