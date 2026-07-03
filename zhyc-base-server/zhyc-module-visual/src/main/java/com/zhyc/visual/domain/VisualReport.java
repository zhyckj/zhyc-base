/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.domain;

import java.time.LocalDateTime;

/**
 * 可视化报表实体。
 *
 * <p>报表定义图表类型、绑定数据集和前端渲染配置，是大屏布局中的可复用组件。</p>
 */
public class VisualReport {

  /** 主键。 */
  private Long id;
  /** 租户业务编码，用于共享表模式下的数据隔离。 */
  private String tenantId;
  /** 报表编码，租户内唯一，用于大屏布局引用。 */
  private String reportCode;
  /** 报表名称，用于后台管理端展示。 */
  private String reportName;
  /** 数据集编码，指向同租户下的数据集。 */
  private String datasetCode;
  /** 图表类型，例如 table、line、bar、pie、number。 */
  private String chartType;
  /** 图表配置 JSON，保存维度、指标、颜色和渲染选项。 */
  private String configJson;
  /** 报表状态，取值如 enabled、disabled。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建可视化报表实体。
   *
   * @param id 主键
   * @param tenantId 租户业务编码
   * @param reportCode 报表编码
   * @param reportName 报表名称
   * @param datasetCode 数据集编码
   * @param chartType 图表类型
   * @param configJson 图表配置 JSON
   * @param status 报表状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public VisualReport(Long id, String tenantId, String reportCode, String reportName,
      String datasetCode, String chartType, String configJson, String status,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.reportCode = reportCode;
    this.reportName = reportName;
    this.datasetCode = datasetCode;
    this.chartType = chartType;
    this.configJson = configJson;
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

  /** @return 报表编码 */
  public String getReportCode() {
    return reportCode;
  }

  /** @return 报表名称 */
  public String getReportName() {
    return reportName;
  }

  /** @return 数据集编码 */
  public String getDatasetCode() {
    return datasetCode;
  }

  /** @return 图表类型 */
  public String getChartType() {
    return chartType;
  }

  /** @return 图表配置 JSON */
  public String getConfigJson() {
    return configJson;
  }

  /** @return 报表状态 */
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
