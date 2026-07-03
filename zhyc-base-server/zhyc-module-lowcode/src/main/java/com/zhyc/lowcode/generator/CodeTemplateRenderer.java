/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 代码模板渲染接口。
 */
public interface CodeTemplateRenderer {

  /**
   * 渲染模板内容。
   *
   * @param template 模板描述
   * @param context 代码生成上下文
   * @return 渲染后的文件内容
   */
  String render(CodeTemplateDescriptor template, CodeGenerationContext context);
}
