/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;

import java.util.List;

/**
 * 低代码表模型响应。
 */
public class LowcodeTableModelResponse {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 所属数据源主键。 */
  private Long dataSourceId;
  /** 模型编码。 */
  private String code;
  /** 模型名称。 */
  private String name;
  /** 物理表名。 */
  private String tableName;
  /** 模型状态编码。 */
  private String status;
  /** 字段模型响应列表。 */
  private List<LowcodeColumnModelResponse> columns;

  /**
   * 从领域模型创建响应。
   *
   * @param tableModel 表模型领域对象
   * @return 表模型响应
   */
  public static LowcodeTableModelResponse from(LowcodeTableModel tableModel) {
    LowcodeTableModelResponse response = new LowcodeTableModelResponse();
    response.id = tableModel.getId();
    response.tenantId = tableModel.getTenantId();
    response.dataSourceId = tableModel.getDataSourceId();
    response.code = tableModel.getCode();
    response.name = tableModel.getName();
    response.tableName = tableModel.getTableName();
    response.status = tableModel.getStatus().name();
    response.columns = tableModel.getColumns().stream()
        .map(LowcodeColumnModelResponse::from)
        .toList();
    return response;
  }

  /**
   * 返回数据库主键。
   *
   * @return 数据库主键
   */
  public Long getId() {
    return id;
  }

  /**
   * 返回租户业务编码。
   *
   * @return 租户业务编码
   */
  public String getTenantId() {
    return tenantId;
  }

  /**
   * 返回所属数据源主键。
   *
   * @return 数据源主键
   */
  public Long getDataSourceId() {
    return dataSourceId;
  }

  /**
   * 返回模型编码。
   *
   * @return 模型编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回模型名称。
   *
   * @return 模型名称
   */
  public String getName() {
    return name;
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
   * 返回模型状态编码。
   *
   * @return 模型状态编码
   */
  public String getStatus() {
    return status;
  }

  /**
   * 返回字段模型响应列表。
   *
   * @return 字段模型响应列表
   */
  public List<LowcodeColumnModelResponse> getColumns() {
    return columns;
  }
}
