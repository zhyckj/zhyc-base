/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 开放 API 限流过滤器测试。
 */
class OpenApiRateLimitFilterTest {

  /** 固定测试时钟。 */
  private final Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);

  /**
   * 验证超过应用 API 限流策略后请求被拒绝。
   */
  @Test
  void shouldRejectRequestWhenRateLimitExceeded() throws ServletException, IOException {
    OpenApiRateLimitFilter filter = new OpenApiRateLimitFilter(new StaticPolicyRepository(),
        new InMemoryOpenApiRateLimiter(clock));
    MockHttpServletRequest first = request();
    MockHttpServletResponse firstResponse = new MockHttpServletResponse();
    RecordingFilterChain firstChain = new RecordingFilterChain();
    MockHttpServletRequest second = request();
    MockHttpServletResponse secondResponse = new MockHttpServletResponse();
    RecordingFilterChain secondChain = new RecordingFilterChain();

    filter.doFilter(first, firstResponse, firstChain);
    filter.doFilter(second, secondResponse, secondChain);

    assertTrue(firstChain.called);
    assertEquals(200, firstResponse.getStatus());
    assertEquals(false, secondChain.called);
    assertEquals(429, secondResponse.getStatus());
    assertEquals("RATE_LIMITED", secondResponse.getHeader("X-ZHYC-Openapi-Error"));
    assertEquals("60", secondResponse.getHeader("Retry-After"));
    assertTrue(secondResponse.getContentType().startsWith("application/json"));
    assertEquals("{\"code\":\"RATE_LIMITED\",\"message\":\"开放 API 调用已超过限流阈值\"}",
        secondResponse.getContentAsString());
  }

  /**
   * 验证没有限流策略时请求继续进入后续链路。
   */
  @Test
  void shouldPassThroughWhenPolicyMissing() throws ServletException, IOException {
    OpenApiRateLimitFilter filter = new OpenApiRateLimitFilter(new EmptyPolicyRepository(),
        new InMemoryOpenApiRateLimiter(clock));
    MockHttpServletRequest request = request();
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
  }

  /**
   * 验证缺失租户/应用/API 编码时直接透传，不触发限流策略查询。
   */
  @Test
  void shouldPassThroughWhenRateLimitAttributesMissing() throws ServletException, IOException {
    StaticPolicyRepository policyRepository = new StaticPolicyRepository();
    OpenApiRateLimitFilter filter = new OpenApiRateLimitFilter(policyRepository,
        new InMemoryOpenApiRateLimiter(clock));
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingFilterChain chain = new RecordingFilterChain();

    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_TENANT_ID, "tenant_a");
    // APP_CODE 和 API_CODE 故意不设置
    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(200, response.getStatus());
    assertEquals(0, policyRepository.calledTimes);
  }

  private MockHttpServletRequest request() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_TENANT_ID, "tenant_a");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_APP_CODE, "purchase-app");
    request.setAttribute(OpenApiRateLimitFilter.ATTRIBUTE_API_CODE, "purchase.request.create");
    return request;
  }

  /**
   * 测试用固定限流策略仓储。
   */
  private static final class StaticPolicyRepository implements OpenApiRateLimitPolicyRepository {

    /** 被调用次数。 */
    private int calledTimes;

    @Override
    public Optional<OpenApiRateLimitPolicy> findEnabledPolicy(String tenantId, String appCode,
        String apiCode) {
      calledTimes++;
      return Optional.of(new OpenApiRateLimitPolicy(tenantId, appCode, apiCode, 1,
          Duration.ofMinutes(1)));
    }
  }

  /**
   * 测试用空限流策略仓储。
   */
  private static final class EmptyPolicyRepository implements OpenApiRateLimitPolicyRepository {

    @Override
    public Optional<OpenApiRateLimitPolicy> findEnabledPolicy(String tenantId, String appCode,
        String apiCode) {
      return Optional.empty();
    }
  }

  /**
   * 测试用过滤器链。
   */
  private static final class RecordingFilterChain implements FilterChain {

    /** 是否已调用后续过滤器链。 */
    private boolean called;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) {
      called = true;
      if (response instanceof MockHttpServletResponse httpResponse) {
        httpResponse.setStatus(200);
      }
    }
  }
}
