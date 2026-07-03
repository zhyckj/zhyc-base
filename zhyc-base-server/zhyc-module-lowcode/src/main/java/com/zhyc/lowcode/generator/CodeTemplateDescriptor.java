/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.Objects;

/**
 * 代码生成模板描述。
 */
public class CodeTemplateDescriptor {

  /** 模板唯一编码。 */
  private final String code;
  /** 模板适用的生成目标。 */
  private final GenerationTarget target;
  /** 模板名称或说明。 */
  private final String name;
  /** 输出路径模式，支持由具体生成器解释占位符。 */
  private final String outputPathPattern;
  /** 模板内容，首期可使用简单字符串占位符。 */
  private final String templateContent;

  /**
   * 创建代码生成模板描述。
   *
   * @param code 模板唯一编码
   * @param target 模板适用的生成目标
   * @param name 模板名称或说明
   * @param outputPathPattern 输出路径模式
   */
  public CodeTemplateDescriptor(
      String code,
      GenerationTarget target,
      String name,
      String outputPathPattern) {
    this(code, target, name, outputPathPattern, "");
  }

  /**
   * 创建代码生成模板描述。
   *
   * @param code 模板唯一编码
   * @param target 模板适用的生成目标
   * @param name 模板名称或说明
   * @param outputPathPattern 输出路径模式
   * @param templateContent 模板内容
   */
  public CodeTemplateDescriptor(
      String code,
      GenerationTarget target,
      String name,
      String outputPathPattern,
      String templateContent) {
    this.code = requireText(code, "模板编码不能为空");
    this.target = Objects.requireNonNull(target, "生成目标不能为空");
    this.name = requireText(name, "模板名称不能为空");
    this.outputPathPattern = requireText(outputPathPattern, "输出路径模式不能为空");
    this.templateContent = templateContent == null ? "" : templateContent;
  }

  /**
   * 返回模板唯一编码。
   *
   * @return 模板唯一编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回模板适用的生成目标。
   *
   * @return 生成目标
   */
  public GenerationTarget getTarget() {
    return target;
  }

  /**
   * 返回模板名称或说明。
   *
   * @return 模板名称或说明
   */
  public String getName() {
    return name;
  }

  /**
   * 返回输出路径模式。
   *
   * @return 输出路径模式
   */
  public String getOutputPathPattern() {
    return outputPathPattern;
  }

  /**
   * 返回模板内容。
   *
   * @return 模板内容，未配置时返回空字符串
   */
  public String getTemplateContent() {
    return templateContent;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
