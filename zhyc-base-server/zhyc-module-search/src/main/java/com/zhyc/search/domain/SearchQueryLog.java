/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.domain;

import java.time.LocalDateTime;

/**
 * 全文检索查询日志。
 *
 * <p>记录租户、索引、关键词、结果数和耗时，用于审计、性能分析和后续等保留痕。</p>
 */
public class SearchQueryLog {

  /** 查询日志主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 查询索引编码。 */
  private final String indexCode;
  /** 查询关键词，禁止保存敏感全文内容。 */
  private final String keyword;
  /** 返回结果数量。 */
  private final Integer resultCount;
  /** 查询耗时毫秒。 */
  private final Long costMs;
  /** 查询状态，success 表示成功记录。 */
  private final String queryStatus;
  /** 创建时间。 */
  private final LocalDateTime createdAt;

  /**
   * 创建全文检索查询日志。
   *
   * @param id 查询日志主键
   * @param tenantId 租户业务编码
   * @param indexCode 查询索引编码
   * @param keyword 查询关键词
   * @param resultCount 返回结果数量
   * @param costMs 查询耗时毫秒
   * @param queryStatus 查询状态
   * @param createdAt 创建时间
   */
  public SearchQueryLog(Long id, String tenantId, String indexCode, String keyword, Integer resultCount,
      Long costMs, String queryStatus, LocalDateTime createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.indexCode = indexCode;
    this.keyword = keyword;
    this.resultCount = resultCount;
    this.costMs = costMs;
    this.queryStatus = queryStatus;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getIndexCode() {
    return indexCode;
  }

  public String getKeyword() {
    return keyword;
  }

  public Integer getResultCount() {
    return resultCount;
  }

  public Long getCostMs() {
    return costMs;
  }

  public String getQueryStatus() {
    return queryStatus;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
