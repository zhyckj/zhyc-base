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
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open API OAuth2 Token 鉴权过滤器。
 *
 * <p>该过滤器保护携带 {@code Authorization: Bearer ...} 的 {@code /openapi/**} 请求，
 * 通过认证中心 Token 校验结果映射开发者应用，再复用开放 API 授权和调用审计能力。</p>
 */
public class OpenApiOAuth2AuthenticationFilter implements Filter {

  /** 日志记录器，用于记录审计写入失败等网关内部异常。 */
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiOAuth2AuthenticationFilter.class);

  /** OAuth2 Authorization 请求头。 */
  public static final String HEADER_AUTHORIZATION = "Authorization";
  /** 请求追踪 ID 请求头。 */
  public static final String HEADER_REQUEST_ID = "X-ZHYC-Request-Id";
  /** 鉴权失败原因响应头。 */
  public static final String HEADER_ERROR = "X-ZHYC-Openapi-Error";
  /** 权限拒绝错误编码。 */
  public static final String ERROR_PERMISSION_DENIED = "PERMISSION_DENIED";
  /** IP 白名单拒绝错误编码。 */
  public static final String ERROR_IP_NOT_ALLOWED = "IP_NOT_ALLOWED";
  /** OAuth2 Bearer Token 前缀。 */
  private static final String BEARER_PREFIX = "Bearer ";
  /** Open API 请求路径前缀。 */
  private static final String OPENAPI_PATH_PREFIX = "/openapi/";
  /** 鉴权失败审计使用的未知租户编码。 */
  private static final String UNKNOWN_TENANT_ID = "UNKNOWN_TENANT";
  /** 鉴权失败审计使用的未知应用编码。 */
  private static final String UNKNOWN_APP_CODE = "UNKNOWN_APP";
  /** OAuth2 鉴权失败审计使用的 API 编码。 */
  private static final String OAUTH2_AUTHENTICATION_API_CODE = "OPENAPI_OAUTH2_AUTHENTICATION";
  /** OAuth2 鉴权失败审计使用的未知客户端标识。 */
  private static final String UNKNOWN_OAUTH2_CLIENT_ACCESS_KEY = "oauth2:UNKNOWN_CLIENT";

  /** OAuth2 Token 校验器。 */
  private final OAuth2TokenVerifier tokenVerifier;
  /** 开放 API 运行态权限仓储。 */
  private final ApiPermissionRepository permissionRepository;
  /** 开放 API 应用客户端 IP 白名单仓储。 */
  private final ApiClientIpWhitelistRepository clientIpWhitelistRepository;
  /** 开放 API 调用审计记录器。 */
  private final ApiCallAuditRecorder auditRecorder;
  /** 运行期时钟。 */
  private final Clock clock;

  /**
   * 创建 Open API OAuth2 Token 鉴权过滤器。
   *
   * @param tokenVerifier OAuth2 Token 校验器
   * @param permissionRepository 开放 API 运行态权限仓储
   * @param auditRecorder 开放 API 调用审计记录器
   * @param clock 运行期时钟
   */
  public OpenApiOAuth2AuthenticationFilter(OAuth2TokenVerifier tokenVerifier,
      ApiPermissionRepository permissionRepository, ApiCallAuditRecorder auditRecorder, Clock clock) {
    this(tokenVerifier, permissionRepository, new AllowAllApiClientIpWhitelistRepository(), auditRecorder, clock);
  }

  /**
   * 创建 Open API OAuth2 Token 鉴权过滤器。
   *
   * @param tokenVerifier OAuth2 Token 校验器
   * @param permissionRepository 开放 API 运行态权限仓储
   * @param clientIpWhitelistRepository 开放 API 应用客户端 IP 白名单仓储
   * @param auditRecorder 开放 API 调用审计记录器
   * @param clock 运行期时钟
   */
  public OpenApiOAuth2AuthenticationFilter(OAuth2TokenVerifier tokenVerifier,
      ApiPermissionRepository permissionRepository, ApiClientIpWhitelistRepository clientIpWhitelistRepository,
      ApiCallAuditRecorder auditRecorder, Clock clock) {
    this.tokenVerifier = Objects.requireNonNull(tokenVerifier, "OAuth2 Token 校验器不能为空");
    this.permissionRepository = Objects.requireNonNull(permissionRepository, "开放 API 权限仓储不能为空");
    this.clientIpWhitelistRepository = Objects.requireNonNull(clientIpWhitelistRepository,
        "开放 API 应用客户端 IP 白名单仓储不能为空");
    this.auditRecorder = Objects.requireNonNull(auditRecorder, "开放 API 调用审计记录器不能为空");
    this.clock = Objects.requireNonNull(clock, "运行期时钟不能为空");
  }

  /**
   * 执行 Open API OAuth2 鉴权链路。
   *
   * <p>仅处理携带 Bearer Token 且属于 Open API 路径的请求；其它请求透传。</p>
   * <p>权限影响：Token 校验失败/未授权返回 401，权限不足返回 403。</p>
   *
   * <p>失败边界：Token 解析或鉴权失败均不进入下游路由，避免未鉴权流量进入内部服务。</p>
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
    if (!isOpenApiRequest(httpRequest) || !hasBearerToken(httpRequest)) {
      chain.doFilter(request, response);
      return;
    }

    String requestPath = buildRequestPath(httpRequest);
    Instant startAt = clock.instant();
    OAuth2TokenAuthenticationResult result = tokenVerifier.verify(resolveBearerToken(httpRequest));
    if (!result.isAuthenticated()) {
      writeError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, result.getFailureCode(),
          "开放 API OAuth2 Token 鉴权失败");
      recordAuthenticationFailure(httpRequest, requestPath, startAt, result.getFailureCode());
      return;
    }

    String clientIp = resolveClientIp(httpRequest);
    if (!clientIpWhitelistRepository.isAllowed(result.getTenantId(), result.getAppCode(), clientIp)) {
      writeError(httpResponse, HttpServletResponse.SC_FORBIDDEN, ERROR_IP_NOT_ALLOWED,
          "客户端 IP 不在开放 API 应用白名单内");
      recordAudit(httpRequest, result, requestPath, requestPath, HttpServletResponse.SC_FORBIDDEN,
          startAt, false, ERROR_IP_NOT_ALLOWED);
      return;
    }

    if (!permissionRepository.isAllowed(result.getTenantId(), result.getAppCode(), httpRequest.getMethod(),
        requestPath)) {
      writeError(httpResponse, HttpServletResponse.SC_FORBIDDEN, ERROR_PERMISSION_DENIED,
          "开放 API 应用无权访问当前接口");
      recordAudit(httpRequest, result, requestPath, requestPath, HttpServletResponse.SC_FORBIDDEN,
          startAt, false, ERROR_PERMISSION_DENIED);
      return;
    }

    String apiCode = permissionRepository.resolveApiCode(result.getTenantId(), result.getAppCode(),
        httpRequest.getMethod(), requestPath);
    httpRequest.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_TENANT_ID, result.getTenantId());
    httpRequest.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_APP_CODE, result.getAppCode());
    httpRequest.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_API_CODE, apiCode);
    try {
      chain.doFilter(request, response);
      int responseStatus = httpResponse.getStatus();
      boolean success = responseStatus < HttpServletResponse.SC_BAD_REQUEST;
      recordAudit(httpRequest, result, apiCode, requestPath, responseStatus, startAt, success,
          success ? null : "HTTP_" + responseStatus);
    } catch (IOException | ServletException | RuntimeException ex) {
      recordAudit(httpRequest, result, apiCode, requestPath, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          startAt, false, "UPSTREAM_ERROR");
      throw ex;
    }
  }

  /**
   * 判断当前请求是否为开放 API 请求。
   *
   * @param request HTTP 请求
   * @return 属于开放 API 路径返回 {@code true}
   */
  private boolean isOpenApiRequest(HttpServletRequest request) {
    return buildRequestPath(request).startsWith(OPENAPI_PATH_PREFIX);
  }

  /**
   * 构造参与鉴权的请求路径。
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
   * 判断请求是否携带 Bearer Token。
   *
   * @param request HTTP 请求
   * @return 携带 Bearer Token 返回 {@code true}
   */
  private boolean hasBearerToken(HttpServletRequest request) {
    String authorization = request.getHeader(HEADER_AUTHORIZATION);
    return startsWithBearerPrefix(authorization);
  }

  /**
   * 判断授权头是否使用 Bearer Token 认证方案。
   *
   * @param authorization Authorization 请求头
   * @return 使用 Bearer Token 返回 {@code true}
   */
  private boolean startsWithBearerPrefix(String authorization) {
    return authorization != null && authorization.length() >= BEARER_PREFIX.length()
        && authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length());
  }

  /**
   * 解析 Bearer Token。
   *
   * @param request HTTP 请求
   * @return OAuth2 访问令牌
   */
  private String resolveBearerToken(HttpServletRequest request) {
    return request.getHeader(HEADER_AUTHORIZATION).substring(BEARER_PREFIX.length()).trim();
  }

  /**
   * 写入开放 API 鉴权失败响应。
   *
   * @param response HTTP 响应
   * @param status HTTP 状态码
   * @param code 稳定错误码
   * @param message 面向调用方的中文错误消息
   * @throws IOException 响应写入失败
   */
  private void writeError(HttpServletResponse response, int status, String code, String message)
      throws IOException {
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

  /**
   * 写入开放 API 调用审计。
   *
   * @param request HTTP 请求
   * @param result OAuth2 Token 鉴权结果
   * @param apiCode API 业务编码
   * @param requestPath 请求路径
   * @param responseStatus 响应状态码
   * @param startAt 调用开始时间
   * @param success 是否成功
   * @param errorCode 错误编码
   */
  private void recordAudit(HttpServletRequest request, OAuth2TokenAuthenticationResult result,
      String apiCode, String requestPath, int responseStatus, Instant startAt, boolean success,
      String errorCode) {
    Instant completedAt = clock.instant();
    recordAuditSafely(new ApiCallAuditRecord(result.getTenantId(), result.getAppCode(),
        "oauth2:" + result.getClientId(), apiCode, request.getMethod(), requestPath, responseStatus,
        Math.max(0, completedAt.toEpochMilli() - startAt.toEpochMilli()), success, errorCode,
        resolveClientIp(request), resolveRequestId(request), startAt));
  }

  /**
   * 写入 OAuth2 Token 鉴权失败审计。
   *
   * <p>Token 校验失败时可能无法确定真实租户、应用和客户端，因此使用稳定占位编码；审计内容不包含 Token 原文。</p>
   *
   * @param request HTTP 请求
   * @param requestPath 请求路径
   * @param startAt 调用开始时间
   * @param errorCode 鉴权失败错误编码
   */
  private void recordAuthenticationFailure(HttpServletRequest request, String requestPath, Instant startAt,
      String errorCode) {
    Instant completedAt = clock.instant();
    recordAuditSafely(new ApiCallAuditRecord(UNKNOWN_TENANT_ID, UNKNOWN_APP_CODE,
        UNKNOWN_OAUTH2_CLIENT_ACCESS_KEY, OAUTH2_AUTHENTICATION_API_CODE, request.getMethod(), requestPath,
        HttpServletResponse.SC_UNAUTHORIZED,
        Math.max(0, completedAt.toEpochMilli() - startAt.toEpochMilli()), false, errorCode,
        resolveClientIp(request), resolveRequestId(request), startAt));
  }

  /**
   * 安全写入开放 API 调用审计。
   *
   * <p>审计存储故障只记录内部告警，不影响已经确定的调用方响应；日志不输出 Token 或客户端密钥。</p>
   *
   * @param record 开放 API 调用审计记录
   */
  private void recordAuditSafely(ApiCallAuditRecord record) {
    try {
      auditRecorder.record(record);
    } catch (RuntimeException exception) {
      LOGGER.warn("开放 API 调用审计写入失败，apiCode={}, requestPath={}, status={}, errorCode={}",
          record.getApiCode(), record.getRequestPath(), record.getResponseStatus(), record.getErrorCode(),
          exception);
    }
  }

  /**
   * 解析客户端 IP。
   *
   * @param request HTTP 请求
   * @return 客户端 IP
   */
  private String resolveClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  /**
   * 解析或生成请求追踪 ID。
   *
   * @param request HTTP 请求
   * @return 请求追踪 ID
   */
  private String resolveRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(HEADER_REQUEST_ID);
    if (requestId != null && !requestId.isBlank()) {
      return requestId.trim();
    }
    return UUID.randomUUID().toString();
  }

  /**
   * 默认放行的开放 API 应用客户端 IP 白名单仓储。
   *
   * <p>仅用于兼容旧构造器。生产装配应注入真实仓储，否则无法启用来源 IP 限制。</p>
   */
  private static final class AllowAllApiClientIpWhitelistRepository implements ApiClientIpWhitelistRepository {

    /**
     * 兼容模式下放行所有来源 IP。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param clientIp 客户端 IP
     * @return 固定返回 {@code true}
     */
    @Override
    public boolean isAllowed(String tenantId, String appCode, String clientIp) {
      return true;
    }
  }
}
