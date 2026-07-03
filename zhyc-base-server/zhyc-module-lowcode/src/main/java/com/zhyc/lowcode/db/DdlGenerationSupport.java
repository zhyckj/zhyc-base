/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据库 DDL 生成辅助工具。
 *
 * <p>集中处理标识符校验、注释转义、主键拼接和租户逻辑删除索引判断，避免各方言实现出现安全边界差异。</p>
 */
public final class DdlGenerationSupport {

  private DdlGenerationSupport() {
  }

  /**
   * 校验并返回数据库标识符。
   *
   * @param identifier 表名、字段名、索引名或约束名
   * @return 原始标识符
   */
  public static String requireIdentifier(String identifier) {
    if (identifier == null || !identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
      throw new IllegalArgumentException("非法数据库标识符：" + identifier);
    }
    return identifier;
  }

  /**
   * 判断文本是否包含非空白字符。
   *
   * @param value 待检查文本
   * @return 包含有效字符时返回 {@code true}
   */
  public static boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  /**
   * 转义 SQL 注释文本中的单引号。
   *
   * @param comment 表或字段中文注释
   * @return 可安全写入 SQL 字符串字面量的注释文本
   */
  public static String escapeComment(String comment) {
    return comment.replace("'", "''");
  }

  /**
   * 生成主键字段列表。
   *
   * @param columns 字段模型列表
   * @param quoter 数据库标识符引用函数
   * @return 主键字段列表
   */
  public static String primaryKeys(List<LowcodeColumn> columns, Function<String, String> quoter) {
    String primaryKeys = columns.stream()
        .filter(LowcodeColumn::isPrimaryKey)
        .map(column -> quoter.apply(column.getName()))
        .collect(Collectors.joining(", "));
    if (primaryKeys.isEmpty()) {
      throw new IllegalArgumentException("数据表必须至少配置一个主键字段");
    }
    return primaryKeys;
  }

  /**
   * 判断数据表是否需要自动补充租户逻辑删除组合索引。
   *
   * @param columns 字段模型列表
   * @return 同时存在 tenant_id 和 deleted 字段时返回 {@code true}
   */
  public static boolean needsTenantDeletedIndex(List<LowcodeColumn> columns) {
    return hasColumn(columns, "tenant_id") && hasColumn(columns, "deleted");
  }

  /**
   * 生成租户逻辑删除索引名称。
   *
   * @param tableName 数据表名称
   * @return 租户逻辑删除索引名称
   */
  public static String tenantDeletedIndexName(String tableName) {
    return requireIdentifier("idx_" + tableName + "_tenant_deleted");
  }

  private static boolean hasColumn(List<LowcodeColumn> columns, String columnName) {
    return columns.stream().anyMatch(column -> column.getName().equalsIgnoreCase(columnName));
  }
}
