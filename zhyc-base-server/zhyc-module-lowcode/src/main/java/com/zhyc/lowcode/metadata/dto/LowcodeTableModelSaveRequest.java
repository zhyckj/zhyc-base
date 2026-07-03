/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodeModelStatus;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 低代码表模型保存请求。
 */
public class LowcodeTableModelSaveRequest {

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
  /** 字段模型保存请求列表。 */
  private List<LowcodeColumnModelRequest> columns = new ArrayList<>();

  /**
   * 转换成表模型领域对象。
   *
   * @return 表模型领域对象
   */
  public LowcodeTableModel toDomain() {
    return new LowcodeTableModel(null, tenantId, dataSourceId, code, name, tableName,
        status == null ? LowcodeModelStatus.DRAFT : LowcodeModelStatus.valueOf(status),
        columns.stream().map(LowcodeColumnModelRequest::toDomain).toList());
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
   * 设置租户业务编码。
   *
   * @param tenantId 租户业务编码
   */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
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
   * 设置所属数据源主键。
   *
   * @param dataSourceId 数据源主键
   */
  public void setDataSourceId(Long dataSourceId) {
    this.dataSourceId = dataSourceId;
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
   * 设置模型编码。
   *
   * @param code 模型编码
   */
  public void setCode(String code) {
    this.code = code;
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
   * 设置模型名称。
   *
   * @param name 模型名称
   */
  public void setName(String name) {
    this.name = name;
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
   * 返回模型状态编码。
   *
   * @return 模型状态编码
   */
  public String getStatus() {
    return status;
  }

  /**
   * 设置模型状态编码。
   *
   * @param status 模型状态编码
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * 返回字段模型保存请求列表。
   *
   * @return 字段模型保存请求列表
   */
  public List<LowcodeColumnModelRequest> getColumns() {
    return columns;
  }

  /**
   * 设置字段模型保存请求列表。
   *
   * @param columns 字段模型保存请求列表
   */
  public void setColumns(List<LowcodeColumnModelRequest> columns) {
    this.columns = columns == null ? new ArrayList<>() : new ArrayList<>(columns);
  }
}
