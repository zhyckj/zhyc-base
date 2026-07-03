/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * JDBC 防重放 nonce 存储测试。
 */
class JdbcOpenApiReplayNonceStoreTest {

  /**
   * 验证 JDBC nonce 存储通过唯一键拒绝重复 nonce，并允许过期清理后重新使用。
   */
  @Test
  void shouldRejectRepeatedNonceAndAllowReuseAfterExpiration() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);
    ReplayProtector protector = new ReplayProtector(Duration.ofSeconds(10), clock, 100,
        new JdbcOpenApiReplayNonceStore(jdbcTemplate));

    assertTrue(protector.accept("purchase-app", "nonce-001", clock.instant()));
    assertFalse(protector.accept("purchase-app", "nonce-001", clock.instant()));

    Clock nextClock = Clock.fixed(Instant.parse("2026-06-24T00:00:11Z"), ZoneOffset.UTC);
    ReplayProtector nextProtector = new ReplayProtector(Duration.ofSeconds(10), nextClock, 100,
        new JdbcOpenApiReplayNonceStore(jdbcTemplate));

    assertTrue(nextProtector.accept("purchase-app", "nonce-001", nextClock.instant()));
  }

  /**
   * 验证不同应用的相同 nonce 互不影响。
   */
  @Test
  void shouldScopeNonceByAppKey() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);
    ReplayProtector protector = new ReplayProtector(Duration.ofSeconds(10), clock, 100,
        new JdbcOpenApiReplayNonceStore(jdbcTemplate));

    assertTrue(protector.accept("app-a", "same-nonce", clock.instant()));
    assertTrue(protector.accept("app-b", "same-nonce", clock.instant()));
    assertFalse(protector.accept("app-a", "same-nonce", clock.instant()));
  }

  private void createTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_replay_nonce (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            app_key VARCHAR(128) NOT NULL,
            nonce_value VARCHAR(128) NOT NULL,
            expires_at TIMESTAMP NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE (app_key, nonce_value)
        )
        """);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_replay_nonce_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
