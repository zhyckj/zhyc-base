/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;

import java.util.Objects;

/**
 * 代码生成请求。
 */
public class CodeGenerationRequest {

  /** 生成目标端。 */
  private final GenerationTarget target;
  /** 业务模块名称。 */
  private final String moduleName;
  /** 业务实体名称。 */
  private final String entityName;
  /** 低代码表模型。 */
  private final LowcodeTableModel tableModel;
  /** 预先生成的建表 DDL，用于按数据源方言渲染 SQL 模板。 */
  private final String ddl;

  /**
   * 创建代码生成请求。
   *
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param tableModel 低代码表模型
   */
  public CodeGenerationRequest(GenerationTarget target, String moduleName, String entityName,
                               LowcodeTableModel tableModel) {
    this(target, moduleName, entityName, tableModel, null);
  }

  /**
   * 创建代码生成请求。
   *
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param tableModel 低代码表模型
   * @param ddl 按目标数据源方言生成的建表 DDL，未提供时由模板渲染器兜底
   */
  public CodeGenerationRequest(GenerationTarget target, String moduleName, String entityName,
                               LowcodeTableModel tableModel, String ddl) {
    this.target = Objects.requireNonNull(target, "生成目标不能为空");
    this.moduleName = requireCodeName(moduleName, "业务模块名称不能为空", "业务模块名称不能包含空白字符");
    this.entityName = requireCodeName(entityName, "业务实体名称不能为空", "业务实体名称不能包含空白字符");
    this.tableModel = Objects.requireNonNull(tableModel, "低代码表模型不能为空");
    this.ddl = trimToNull(ddl);
  }

  /**
   * 返回生成目标端。
   *
   * @return 生成目标端
   */
  public GenerationTarget getTarget() {
    return target;
  }

  /**
   * 返回业务模块名称。
   *
   * @return 业务模块名称
   */
  public String getModuleName() {
    return moduleName;
  }

  /**
   * 返回业务实体名称。
   *
   * @return 业务实体名称
   */
  public String getEntityName() {
    return entityName;
  }

  /**
   * 返回低代码表模型。
   *
   * @return 低代码表模型
   */
  public LowcodeTableModel getTableModel() {
    return tableModel;
  }

  /**
   * 返回按目标数据源方言生成的建表 DDL。
   *
   * @return 建表 DDL，未预生成时返回 {@code null}
   */
  public String getDdl() {
    return ddl;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  private static String trimToNull(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim();
  }

  /**
   * 校验生成命名不能为空且不能包含空白字符。
   *
   * @param value 原始命名
   * @param blankMessage 空值错误消息
   * @param whitespaceMessage 包含空白字符错误消息
   * @return 清理后的命名
   */
  private static String requireCodeName(String value, String blankMessage, String whitespaceMessage) {
    String normalized = requireText(value, blankMessage);
    if (normalized.chars().anyMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException(whitespaceMessage);
    }
    return normalized;
  }
}
