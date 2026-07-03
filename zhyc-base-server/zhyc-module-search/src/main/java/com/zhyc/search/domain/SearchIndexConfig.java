/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.domain;

import java.time.LocalDateTime;

/**
 * 全文检索索引配置。
 *
 * <p>描述租户下某个业务表如何进入检索索引。首期运行态使用数据库 LIKE 检索适配器，
 * 后续可无损扩展 Elasticsearch、OpenSearch 或数据库全文索引实现。</p>
 */
public class SearchIndexConfig {

  /** 索引配置主键。 */
  private final Long id;
  /** 租户业务编码，用于 SaaS 数据隔离。 */
  private final String tenantId;
  /** 索引编码，租户内唯一。 */
  private final String indexCode;
  /** 索引名称，用于后台展示。 */
  private final String indexName;
  /** 数据来源表名。 */
  private final String sourceTable;
  /** 可检索字段列表，逗号分隔。 */
  private final String searchFields;
  /** 可过滤字段列表，逗号分隔。 */
  private final String filterFields;
  /** 索引状态，enabled 表示可用。 */
  private final String status;
  /** 配置备注。 */
  private final String remark;
  /** 创建时间。 */
  private final LocalDateTime createdAt;
  /** 更新时间。 */
  private final LocalDateTime updatedAt;

  /**
   * 创建全文检索索引配置。
   *
   * @param id 索引配置主键
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @param indexName 索引名称
   * @param sourceTable 数据来源表名
   * @param searchFields 可检索字段列表
   * @param filterFields 可过滤字段列表
   * @param status 索引状态
   * @param remark 配置备注
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public SearchIndexConfig(Long id, String tenantId, String indexCode, String indexName, String sourceTable,
      String searchFields, String filterFields, String status, String remark,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.indexCode = indexCode;
    this.indexName = indexName;
    this.sourceTable = sourceTable;
    this.searchFields = searchFields;
    this.filterFields = filterFields;
    this.status = status;
    this.remark = remark;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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

  public String getIndexName() {
    return indexName;
  }

  public String getSourceTable() {
    return sourceTable;
  }

  public String getSearchFields() {
    return searchFields;
  }

  public String getFilterFields() {
    return filterFields;
  }

  public String getStatus() {
    return status;
  }

  public String getRemark() {
    return remark;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
