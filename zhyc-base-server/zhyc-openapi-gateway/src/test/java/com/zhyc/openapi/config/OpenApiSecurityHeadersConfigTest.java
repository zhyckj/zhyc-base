/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.zhyc.openapi.security.OpenApiSecurityHeadersFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 开放 API 网关安全响应头配置测试。
 */
class OpenApiSecurityHeadersConfigTest {

  /** 应用上下文启动器。 */
  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(OpenApiSecurityHeadersConfig.class);

  /**
   * 验证开放 API 安全响应头过滤器注册到所有请求，并位于认证过滤器之前。
   */
  @Test
  void shouldRegisterOpenApiSecurityHeadersFilter() {
    contextRunner.run(context -> {
      FilterRegistrationBean<?> registrationBean =
          context.getBean("openApiSecurityHeadersFilterRegistration", FilterRegistrationBean.class);

      assertEquals(1, registrationBean.getUrlPatterns().size());
      assertEquals("/*", registrationBean.getUrlPatterns().iterator().next());
      assertEquals(Ordered.HIGHEST_PRECEDENCE, registrationBean.getOrder());
      assertInstanceOf(OpenApiSecurityHeadersFilter.class, registrationBean.getFilter());
    });
  }

  /**
   * 验证过滤器会补充等保三级 Web 安全基线需要的浏览器安全响应头。
   *
   * @throws Exception 过滤器执行异常
   */
  @Test
  void shouldAppendSecurityHeaders() throws Exception {
    OpenApiSecurityHeadersFilter filter = new OpenApiSecurityHeadersFilter();
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openapi/v1/purchase/requests");
    request.addHeader("X-Forwarded-Proto", "https");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new NoopFilterChain());

    assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    assertEquals("DENY", response.getHeader("X-Frame-Options"));
    assertEquals("no-referrer", response.getHeader("Referrer-Policy"));
    assertEquals("camera=(), microphone=(), geolocation=(), payment=()",
        response.getHeader("Permissions-Policy"));
    assertEquals("frame-ancestors 'none'; base-uri 'self'; object-src 'none'",
        response.getHeader("Content-Security-Policy"));
    assertEquals("no-store, no-cache, must-revalidate, max-age=0", response.getHeader("Cache-Control"));
    assertEquals("no-cache", response.getHeader("Pragma"));
    assertEquals("0", response.getHeader("Expires"));
    assertEquals("max-age=31536000; includeSubDomains", response.getHeader("Strict-Transport-Security"));
  }

  private static final class NoopFilterChain implements FilterChain {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException {
      // 测试只关注响应头补充行为。
    }
  }
}
