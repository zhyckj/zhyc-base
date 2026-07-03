/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 低代码生成模板响应对象。
 */
public class LowcodeTemplateResponse {

  /** 模板唯一编码。 */
  private final String code;
  /** 生成目标端编码。 */
  private final String target;
  /** 模板名称。 */
  private final String name;
  /** 输出路径模式。 */
  private final String outputPathPattern;

  private LowcodeTemplateResponse(CodeTemplateDescriptor template) {
    this.code = template.getCode();
    this.target = template.getTarget().getCode();
    this.name = template.getName();
    this.outputPathPattern = template.getOutputPathPattern();
  }

  /**
   * 从模板描述转换为响应对象。
   *
   * @param template 模板描述
   * @return 模板响应对象
   */
  public static LowcodeTemplateResponse from(CodeTemplateDescriptor template) {
    return new LowcodeTemplateResponse(template);
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
   * 返回生成目标端编码。
   *
   * @return 生成目标端编码
   */
  public String getTarget() {
    return target;
  }

  /**
   * 返回模板名称。
   *
   * @return 模板名称
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
}
