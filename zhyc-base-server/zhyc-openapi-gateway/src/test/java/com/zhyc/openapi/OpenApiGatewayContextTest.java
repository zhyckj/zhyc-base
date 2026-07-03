/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zhyc.openapi.security.OpenApiOAuth2AuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 开放 API 网关应用上下文启动测试。
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:openapi_gateway_context;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "zhyc.cache.redis.enabled=false",
    "zhyc.openapi.oauth2.introspection-uri="
})
class OpenApiGatewayContextTest {

  /** Spring Boot 自动配置创建的 JDBC 操作模板。 */
  @Autowired
  private JdbcTemplate jdbcTemplate;

  /** OAuth2 过滤器依赖 JDBC 仓储链路，是本次启动失败的关键 Bean。 */
  @Autowired
  private OpenApiOAuth2AuthenticationFilter openApiOAuth2AuthenticationFilter;

  /**
   * 验证网关上下文可通过 Spring Boot JDBC 自动配置完成启动。
   */
  @Test
  void shouldLoadGatewayContextWithAutoConfiguredJdbcTemplate() {
    assertNotNull(jdbcTemplate);
    assertNotNull(openApiOAuth2AuthenticationFilter);
  }
}
