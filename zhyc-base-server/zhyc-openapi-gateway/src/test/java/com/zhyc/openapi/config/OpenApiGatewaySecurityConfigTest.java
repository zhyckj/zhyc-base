/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.zhyc.openapi.security.ApiCallAuditRecorder;
import com.zhyc.openapi.security.ApiKeyAuthenticator;
import com.zhyc.openapi.security.ApiPermissionRepository;
import com.zhyc.openapi.security.FallbackOpenApiRateLimiter;
import com.zhyc.openapi.security.FallbackOpenApiReplayNonceStore;
import com.zhyc.openapi.security.JdbcApiCallAuditRecorder;
import com.zhyc.openapi.security.JdbcApiPermissionRepository;
import com.zhyc.openapi.security.JdbcOpenApiRateLimiter;
import com.zhyc.openapi.security.JdbcOpenApiRouteRepository;
import com.zhyc.openapi.security.OAuth2TokenVerifier;
import com.zhyc.openapi.security.OpenApiApiKeyAuthenticationFilter;
import com.zhyc.openapi.security.OpenApiBackendInvoker;
import com.zhyc.openapi.security.OpenApiOAuth2AuthenticationFilter;
import com.zhyc.openapi.security.OpenApiRateLimitFilter;
import com.zhyc.openapi.security.OpenApiRateLimiter;
import com.zhyc.openapi.security.OpenApiReplayNonceStore;
import com.zhyc.openapi.security.OpenApiRouteRepository;
import com.zhyc.openapi.security.OpenApiRoutingFilter;
import com.zhyc.openapi.security.ReplayProtector;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Open API 网关安全配置测试。
 */
class OpenApiGatewaySecurityConfigTest {

  /** 应用上下文启动器。 */
  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(OpenApiGatewaySecurityConfig.class, TestJdbcConfiguration.class);

  /** Redis 应用上下文启动器。 */
  private final ApplicationContextRunner redisContextRunner = new ApplicationContextRunner()
      .withUserConfiguration(OpenApiGatewaySecurityConfig.class, TestJdbcConfiguration.class,
          TestRedisConfiguration.class)
      .withPropertyValues("zhyc.cache.redis.enabled=true", "zhyc.cache.prefix=zhyc-test");

  /**
   * 验证网关安全配置会注册 API Key 鉴权、授权仓储、审计记录器和过滤器。
   */
  @Test
  void shouldRegisterOpenApiGatewaySecurityBeans() {
    contextRunner.run(context -> {
      assertNotNull(context.getBean(ApiKeyAuthenticator.class));
      assertInstanceOf(JdbcApiPermissionRepository.class, context.getBean(ApiPermissionRepository.class));
      assertInstanceOf(JdbcOpenApiRouteRepository.class, context.getBean(OpenApiRouteRepository.class));
      assertInstanceOf(JdbcApiCallAuditRecorder.class, context.getBean(ApiCallAuditRecorder.class));
      assertInstanceOf(JdbcOpenApiRateLimiter.class, context.getBean(OpenApiRateLimiter.class));
      assertNotNull(context.getBean(ReplayProtector.class));
      assertNotNull(context.getBean(OAuth2TokenVerifier.class));
      assertNotNull(context.getBean(OpenApiBackendInvoker.class));
      assertNotNull(context.getBean(OpenApiOAuth2AuthenticationFilter.class));
      assertNotNull(context.getBean(OpenApiApiKeyAuthenticationFilter.class));
      assertNotNull(context.getBean(OpenApiRateLimitFilter.class));
      assertNotNull(context.getBean(OpenApiRoutingFilter.class));
      assertEquals(4, context.getBeansOfType(FilterRegistrationBean.class).size());
      context.getBeansOfType(FilterRegistrationBean.class).values().forEach(registrationBean -> {
        assertEquals(1, registrationBean.getUrlPatterns().size());
        assertEquals("/openapi/*", registrationBean.getUrlPatterns().iterator().next());
      });
    });
  }

  /**
   * 验证开启 Redis 后会注册 Redis 优先且 JDBC 兜底的限流器和 nonce 存储。
   */
  @Test
  void shouldRegisterRedisFirstGatewayBeansWhenRedisEnabled() {
    redisContextRunner.run(context -> {
      assertInstanceOf(FallbackOpenApiRateLimiter.class, context.getBean(OpenApiRateLimiter.class));
      assertInstanceOf(FallbackOpenApiReplayNonceStore.class, context.getBean(OpenApiReplayNonceStore.class));
      assertNotNull(context.getBean(ReplayProtector.class));
    });
  }

  /**
   * 测试用 JDBC 配置。
   */
  @Configuration
  static class TestJdbcConfiguration {

    /**
     * 创建测试数据源。
     *
     * @return H2 内存数据源
     */
    @Bean
    DataSource dataSource() {
      DriverManagerDataSource dataSource = new DriverManagerDataSource(
          "jdbc:h2:mem:openapi_gateway_config_" + System.nanoTime()
              + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
          "sa", "");
      dataSource.setDriverClassName("org.h2.Driver");
      return dataSource;
    }

  }

  /**
   * 测试用 Redis 配置。
   */
  @Configuration
  static class TestRedisConfiguration {

    /**
     * 创建测试 Redis 操作模板。
     *
     * @return Redis 字符串操作模板
     */
    @Bean
    StringRedisTemplate stringRedisTemplate() {
      return mock(StringRedisTemplate.class);
    }
  }
}
