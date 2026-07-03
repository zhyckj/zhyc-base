/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 低代码数据表建模定义。
 */
public class LowcodeTableModel {

  /** 数据库主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 所属低代码数据源主键。 */
  private final Long dataSourceId;
  /** 模型编码，同一租户内唯一。 */
  private final String code;
  /** 模型名称。 */
  private final String name;
  /** 物理表名。 */
  private final String tableName;
  /** 字段模型列表。 */
  private final List<LowcodeColumnModel> columns;
  /** 模型发布状态。 */
  private LowcodeModelStatus status = LowcodeModelStatus.DRAFT;

  /**
   * 创建低代码数据表模型。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param code 模型编码，同一租户内唯一
   * @param name 模型名称
   * @param tableName 物理表名
   * @param columns 字段模型列表
   */
  public LowcodeTableModel(Long id, String tenantId, String code, String name, String tableName,
                           List<LowcodeColumnModel> columns) {
    this(id, tenantId, null, code, name, tableName, LowcodeModelStatus.DRAFT, columns);
  }

  /**
   * 创建低代码数据表模型。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param dataSourceId 所属低代码数据源主键
   * @param code 模型编码，同一租户内唯一
   * @param name 模型名称
   * @param tableName 物理表名
   * @param status 模型发布状态
   * @param columns 字段模型列表
   */
  public LowcodeTableModel(Long id, String tenantId, Long dataSourceId, String code, String name,
                           String tableName, LowcodeModelStatus status, List<LowcodeColumnModel> columns) {
    this.id = id;
    this.tenantId = requireText(tenantId, "租户业务编码不能为空");
    this.dataSourceId = dataSourceId;
    this.code = requireText(code, "模型编码不能为空");
    this.name = requireText(name, "模型名称不能为空");
    this.tableName = requireText(tableName, "物理表名不能为空");
    if (columns == null || columns.isEmpty()) {
      throw new IllegalArgumentException("字段模型不能为空");
    }
    this.columns = new ArrayList<>(columns);
    this.status = status == null ? LowcodeModelStatus.DRAFT : status;
  }

  /**
   * 校验表模型是否满足生成和发布前置规则。
   */
  public void validate() {
    Set<String> columnCodes = new HashSet<>();
    for (LowcodeColumnModel column : columns) {
      if (!columnCodes.add(column.getCode())) {
        throw new IllegalArgumentException("字段编码不能重复: " + column.getCode());
      }
    }
  }

  /**
   * 发布表模型。
   *
   * <p>发布后的模型可进入 DDL 生成、后端代码生成、Vue 页面生成、UniApp 页面生成和开放 API 生成流程。</p>
   */
  public void publish() {
    validate();
    if (!hasPrimaryKey()) {
      throw new IllegalStateException("发布表模型前必须配置主键字段");
    }
    this.status = LowcodeModelStatus.PUBLISHED;
  }

  /**
   * 判断表模型是否配置主键字段。
   *
   * @return 存在主键字段时返回 {@code true}
   */
  public boolean hasPrimaryKey() {
    return columns.stream().anyMatch(LowcodeColumnModel::isPrimaryKey);
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
   * 返回所属低代码数据源主键。
   *
   * @return 数据源主键，未绑定数据源时返回 {@code null}
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
   * 返回字段模型列表副本。
   *
   * @return 字段模型列表副本
   */
  public List<LowcodeColumnModel> getColumns() {
    return new ArrayList<>(columns);
  }

  /**
   * 返回模型发布状态。
   *
   * @return 模型发布状态
   */
  public LowcodeModelStatus getStatus() {
    return status;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
