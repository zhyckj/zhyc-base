/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.List;

/**
 * 代码生成器接口。
 */
public interface CodeGenerator {

  /**
   * 根据请求生成文件清单。
   *
   * @param request 代码生成请求
   * @return 生成文件清单
   */
  List<GeneratedFile> generate(CodeGenerationRequest request);
}
