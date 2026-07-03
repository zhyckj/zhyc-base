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
import java.util.Objects;

/**
 * 开放 API 限流过滤器。
 *
 * <p>该过滤器依赖前置鉴权过滤器写入的租户、应用和 API 编码属性，只对已经鉴权并命中 API
 * 授权的开放 API 请求执行限流。</p>
 */
public class OpenApiRateLimitFilter implements Filter {

  /** 租户业务编码请求属性。 */
  public static final String ATTRIBUTE_TENANT_ID = "zhyc.openapi.tenantId";
  /** 开发者应用编码请求属性。 */
  public static final String ATTRIBUTE_APP_CODE = "zhyc.openapi.appCode";
  /** API 业务编码请求属性。 */
  public static final String ATTRIBUTE_API_CODE = "zhyc.openapi.apiCode";
  /** 鉴权失败原因响应头。 */
  public static final String HEADER_ERROR = "X-ZHYC-Openapi-Error";
  /** HTTP 标准重试等待时间响应头。 */
  public static final String HEADER_RETRY_AFTER = "Retry-After";
  /** 限流拒绝错误消息。 */
  private static final String RATE_LIMITED_MESSAGE = "开放 API 调用已超过限流阈值";

  /** 限流策略仓储。 */
  private final OpenApiRateLimitPolicyRepository policyRepository;
  /** 开放 API 限流器。 */
  private final OpenApiRateLimiter rateLimiter;

  /**
   * 创建开放 API 限流过滤器。
   *
   * @param policyRepository 限流策略仓储
   * @param rateLimiter 开放 API 限流器
   */
  public OpenApiRateLimitFilter(OpenApiRateLimitPolicyRepository policyRepository,
      OpenApiRateLimiter rateLimiter) {
    this.policyRepository = Objects.requireNonNull(policyRepository, "开放 API 限流策略仓储不能为空");
    this.rateLimiter = Objects.requireNonNull(rateLimiter, "开放 API 限流器不能为空");
  }

  /**
   * 执行 Open API 限流链路。
   *
   * <p>仅对已鉴权请求执行限流检查；缺失租户/应用/API 编码的请求透传，不阻断鉴权链路。
   * 超出限流时返回 429 并携带统一错误码。</p>
   *
   * <p>安全边界：限流只影响鉴权通过后的调用，防止不同租户/应用间串流。
   * 幂等性：同一请求只做一次限流判定。</p>
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
    String tenantId = attributeAsString(httpRequest, ATTRIBUTE_TENANT_ID);
    String appCode = attributeAsString(httpRequest, ATTRIBUTE_APP_CODE);
    String apiCode = attributeAsString(httpRequest, ATTRIBUTE_API_CODE);
    if (tenantId == null || appCode == null || apiCode == null) {
      chain.doFilter(request, response);
      return;
    }

    OpenApiRateLimitPolicy policy = policyRepository.findEnabledPolicy(tenantId, appCode, apiCode)
        .orElse(null);
    if (policy == null) {
      chain.doFilter(request, response);
      return;
    }
    OpenApiRateLimitResult result = rateLimiter.tryAcquire(policy, tenantId, appCode, apiCode);
    if (!result.isAllowed()) {
      httpResponse.setHeader(HEADER_RETRY_AFTER, String.valueOf(result.getRetryAfterSeconds()));
      writeError(httpResponse, 429, result.getFailureCode(), RATE_LIMITED_MESSAGE);
      return;
    }
    chain.doFilter(request, response);
  }

  private String attributeAsString(HttpServletRequest request, String attributeName) {
    Object value = request.getAttribute(attributeName);
    return value == null ? null : String.valueOf(value);
  }

  /**
   * 写入开放 API 限流失败响应。
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
}
