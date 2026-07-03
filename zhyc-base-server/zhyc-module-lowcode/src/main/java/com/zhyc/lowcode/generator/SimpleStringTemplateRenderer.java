/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.lowcode.db.mysql.MySqlDdlGenerator;
import com.zhyc.lowcode.db.mysql.MySqlFieldTypeMapper;
import com.zhyc.lowcode.metadata.converter.LowcodeTableModelConverter;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;

import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * 简单字符串模板渲染器。
 *
 * <p>首期用于轻量模板场景，支持常用低代码占位符替换；后续可替换为 FreeMarker 等模板引擎实现。</p>
 */
public class SimpleStringTemplateRenderer implements CodeTemplateRenderer {

  /** 未解析占位符匹配模式。 */
  private static final Pattern UNRESOLVED_PLACEHOLDER = Pattern.compile("(?<![#\\$/])\\{[A-Za-z][A-Za-z0-9_]*}");

  /**
   * 渲染代码模板内容。
   *
   * <p>统一替换模块、实体、表模型、字段和 DDL 占位符，并阻断未解析占位符进入生成结果。</p>
   *
   * @param template 代码模板描述
   * @param context 代码生成上下文
   * @return 已渲染的模板内容
   */
  @Override
  public String render(CodeTemplateDescriptor template, CodeGenerationContext context) {
    if (template == null) {
      throw new IllegalArgumentException("模板描述不能为空");
    }
    if (context == null) {
      throw new IllegalArgumentException("代码生成上下文不能为空");
    }
    String content = template.getTemplateContent()
        .replace("{module}", context.getModuleName())
        .replace("{entity}", context.getEntityName())
        .replace("{Entity}", upperFirst(context.getEntityName()))
        .replace("{table}", context.getTableModel().getTableName())
        .replace("{tableLabel}", context.getTableModel().getName())
        .replace("{tenant}", context.getTableModel().getTenantId())
        .replace("{target}", context.getTarget().getCode())
        .replace("{fields}", fieldCodes(context))
        .replace("{firstDataField}", firstDataField(context))
        .replace("{ddl}", renderDdl(context));
    if (UNRESOLVED_PLACEHOLDER.matcher(content).find()) {
      throw new IllegalArgumentException("模板内容存在未解析占位符: " + content);
    }
    return content;
  }

  private String fieldCodes(CodeGenerationContext context) {
    return context.getTableModel().getColumns().stream()
        .map(LowcodeColumnModel::getCode)
        .collect(Collectors.joining(","));
  }

  private String firstDataField(CodeGenerationContext context) {
    return context.getTableModel().getColumns().stream()
        .filter(column -> !column.isPrimaryKey())
        .map(LowcodeColumnModel::getCode)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("生成保存模板至少需要一个非主键业务字段"));
  }

  /**
   * 渲染表模型对应的建表 DDL。
   *
   * <p>优先使用生成服务按数据源方言预先生成的 DDL；旧调用链未提供时，保留 MySQL 兜底。</p>
   *
   * @param context 代码生成上下文
   * @return 建表 DDL
   */
  private String renderDdl(CodeGenerationContext context) {
    if (context.getDdl() != null) {
      return context.getDdl();
    }
    return new MySqlDdlGenerator(new MySqlFieldTypeMapper())
        .generateCreateTable(new LowcodeTableModelConverter().toDdlTable(context.getTableModel()));
  }

  private String upperFirst(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    return Character.toUpperCase(value.charAt(0)) + value.substring(1);
  }
}
