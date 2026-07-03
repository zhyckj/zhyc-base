/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.domain;

import java.time.LocalDateTime;

/**
 * 可视化数据集实体。
 *
 * <p>数据集描述报表设计器可使用的数据来源、查询语句和启停状态，是报表图表的数据基础。</p>
 */
public class VisualDataset {

  /** 主键。 */
  private Long id;
  /** 租户业务编码，用于共享表模式下的数据隔离。 */
  private String tenantId;
  /** 数据集编码，租户内唯一，用于报表引用。 */
  private String datasetCode;
  /** 数据集名称，用于后台管理端展示。 */
  private String datasetName;
  /** 数据源编码，对应低代码数据源或平台默认数据源。 */
  private String datasourceCode;
  /** 查询 SQL，首期仅存储受控查询语句，后续由数据集执行器统一校验。 */
  private String sqlText;
  /** 数据集状态，取值如 enabled、disabled。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建可视化数据集实体。
   *
   * @param id 主键
   * @param tenantId 租户业务编码
   * @param datasetCode 数据集编码
   * @param datasetName 数据集名称
   * @param datasourceCode 数据源编码
   * @param sqlText 查询 SQL
   * @param status 数据集状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public VisualDataset(Long id, String tenantId, String datasetCode, String datasetName,
      String datasourceCode, String sqlText, String status, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.datasetCode = datasetCode;
    this.datasetName = datasetName;
    this.datasourceCode = datasourceCode;
    this.sqlText = sqlText;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** @return 主键 */
  public Long getId() {
    return id;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 数据集编码 */
  public String getDatasetCode() {
    return datasetCode;
  }

  /** @return 数据集名称 */
  public String getDatasetName() {
    return datasetName;
  }

  /** @return 数据源编码 */
  public String getDatasourceCode() {
    return datasourceCode;
  }

  /** @return 查询 SQL */
  public String getSqlText() {
    return sqlText;
  }

  /** @return 数据集状态 */
  public String getStatus() {
    return status;
  }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /** @return 更新时间 */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
