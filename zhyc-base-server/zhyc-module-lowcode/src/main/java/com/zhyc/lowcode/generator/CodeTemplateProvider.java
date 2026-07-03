/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.List;

/**
 * 代码生成模板提供者扩展接口。
 */
public interface CodeTemplateProvider {

  /**
   * 返回当前提供者声明的模板清单。
   *
   * @return 模板清单
   */
  List<CodeTemplateDescriptor> listTemplates();
}
