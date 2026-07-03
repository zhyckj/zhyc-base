/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 基于 JDBC 的 Open API 防重放 nonce 存储。
 *
 * <p>通过唯一键 {@code app_key + nonce_value} 保证多实例并发下同一个 nonce 只能写入一次。</p>
 */
public class JdbcOpenApiReplayNonceStore implements OpenApiReplayNonceStore {

  /** 删除过期 nonce SQL。 */
  private static final String DELETE_EXPIRED_SQL = """
      DELETE FROM openapi_replay_nonce
      WHERE expires_at <= ?
      """;

  /** 统计有效 nonce 数量 SQL。 */
  private static final String COUNT_ACTIVE_SQL = """
      SELECT COUNT(1)
      FROM openapi_replay_nonce
      WHERE expires_at > ?
      """;

  /** 写入 nonce SQL。 */
  private static final String INSERT_SQL = """
      INSERT INTO openapi_replay_nonce (
          app_key, nonce_value, expires_at, created_at
      ) VALUES (?, ?, ?, ?)
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;

  /**
   * 创建 JDBC nonce 存储。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  public JdbcOpenApiReplayNonceStore(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
  }

  /**
   * 删除数据库中过期的 nonce。
   *
   * @param now 当前时间
   */
  @Override
  public void deleteExpired(Instant now) {
    jdbcTemplate.update(DELETE_EXPIRED_SQL, Timestamp.from(now));
  }

  /**
   * 判断 nonce 存储是否达到容量上限。
   *
   * @param maxEntries 最大有效 nonce 数量
   * @param now 当前时间
   * @return 达到容量上限时返回 {@code true}
   */
  @Override
  public boolean isFull(int maxEntries, Instant now) {
    Long count = jdbcTemplate.queryForObject(COUNT_ACTIVE_SQL, Long.class, Timestamp.from(now));
    return count != null && count >= maxEntries;
  }

  /**
   * 写入尚未出现过的 appKey 与 nonce 组合。
   *
   * @param appKey Open API 应用标识
   * @param nonce 客户端提交的一次性随机串
   * @param expiresAt nonce 过期时间
   * @return 首次写入成功返回 {@code true}
   */
  @Override
  public boolean recordIfAbsent(String appKey, String nonce, Instant expiresAt) {
    try {
      Instant now = Instant.now();
      return jdbcTemplate.update(INSERT_SQL, appKey, nonce, Timestamp.from(expiresAt),
          Timestamp.from(now)) == 1;
    } catch (DuplicateKeyException ex) {
      return false;
    }
  }
}
