/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

/**
 * 低代码物理表导入请求。
 */
public class LowcodePhysicalTableImportRequest {

  /** 租户业务编码。 */
  private String tenantId;
  /** 数据源主键。 */
  private Long dataSourceId;
  /** 物理表名。 */
  private String tableName;
  /** 导入后的模型编码。 */
  private String modelCode;
  /** 导入后的模型名称。 */
  private String modelName;

  /**
   * 返回租户业务编码。
   *
   * @return 租户业务编码
   */
  public String getTenantId() {
    return tenantId;
  }

  /**
   * 设置租户业务编码。
   *
   * @param tenantId 租户业务编码
   */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  /**
   * 返回数据源主键。
   *
   * @return 数据源主键
   */
  public Long getDataSourceId() {
    return dataSourceId;
  }

  /**
   * 设置数据源主键。
   *
   * @param dataSourceId 数据源主键
   */
  public void setDataSourceId(Long dataSourceId) {
    this.dataSourceId = dataSourceId;
  }

  /**
   * 返回物理表名。
   *
   * @return 物理表名
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * 设置物理表名。
   *
   * @param tableName 物理表名
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /**
   * 返回导入后的模型编码。
   *
   * @return 模型编码
   */
  public String getModelCode() {
    return modelCode;
  }

  /**
   * 设置导入后的模型编码。
   *
   * @param modelCode 模型编码
   */
  public void setModelCode(String modelCode) {
    this.modelCode = modelCode;
  }

  /**
   * 返回导入后的模型名称。
   *
   * @return 模型名称
   */
  public String getModelName() {
    return modelName;
  }

  /**
   * 设置导入后的模型名称。
   *
   * @param modelName 模型名称
   */
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }
}
