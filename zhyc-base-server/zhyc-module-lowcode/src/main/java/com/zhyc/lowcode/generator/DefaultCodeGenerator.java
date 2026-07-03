/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.List;
import java.util.Objects;

/**
 * 默认代码生成器实现。
 */
public class DefaultCodeGenerator implements CodeGenerator {

  /** 模板注册表。 */
  private final CodeTemplateRegistry templateRegistry;
  /** 模板渲染器。 */
  private final CodeTemplateRenderer templateRenderer;

  /**
   * 创建默认代码生成器。
   *
   * @param templateRegistry 模板注册表
   * @param templateRenderer 模板渲染器
   */
  public DefaultCodeGenerator(CodeTemplateRegistry templateRegistry, CodeTemplateRenderer templateRenderer) {
    this.templateRegistry = Objects.requireNonNull(templateRegistry, "模板注册表不能为空");
    this.templateRenderer = Objects.requireNonNull(templateRenderer, "模板渲染器不能为空");
  }

  /**
   * 按生成目标渲染全部匹配模板。
   *
   * <p>该方法只负责模板选择、上下文组装和输出路径解析，不直接写入文件，避免覆盖人工代码。</p>
   *
   * @param request 代码生成请求，包含目标端、模块名、实体名和表模型
   * @return 生成文件预览列表
   */
  @Override
  public List<GeneratedFile> generate(CodeGenerationRequest request) {
    CodeGenerationContext context = new CodeGenerationContext(request);
    return templateRegistry.findByTarget(request.getTarget()).stream()
        .map(template -> generateFile(template, context))
        .toList();
  }

  private GeneratedFile generateFile(CodeTemplateDescriptor template, CodeGenerationContext context) {
    return new GeneratedFile(
        template.getTarget(),
        template.getCode(),
        resolvePath(template.getOutputPathPattern(), context),
        templateRenderer.render(template, context));
  }

  private String resolvePath(String pattern, CodeGenerationContext context) {
    String resolvedPath = pattern
        .replace("{module}", context.getModuleName())
        .replace("{entity}", context.getEntityName())
        .replace("{Entity}", upperFirst(context.getEntityName()))
        .replace("{table}", context.getTableModel().getTableName())
        .replace("{tenant}", context.getTableModel().getTenantId());
    if (resolvedPath.contains("{") || resolvedPath.contains("}")) {
      throw new IllegalArgumentException("输出路径存在未解析占位符: " + resolvedPath);
    }
    return resolvedPath;
  }

  private String upperFirst(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    return Character.toUpperCase(value.charAt(0)) + value.substring(1);
  }
}
