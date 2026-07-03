/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * 低代码生成前校验结果。
 *
 * <p>错误项会阻断生成预览和执行，警告项仅提示命名或治理风险，不影响首期生成流程。</p>
 */
public class LowcodeGenerationValidationResult {

  /** 阻断生成的错误项列表。 */
  private final List<LowcodeGenerationValidationItem> errors;
  /** 不阻断生成的警告项列表。 */
  private final List<LowcodeGenerationValidationItem> warnings;

  /**
   * 创建生成前校验结果。
   *
   * @param errors 阻断生成的错误项列表
   * @param warnings 不阻断生成的警告项列表
   */
  public LowcodeGenerationValidationResult(List<LowcodeGenerationValidationItem> errors,
                                           List<LowcodeGenerationValidationItem> warnings) {
    this.errors = errors == null ? List.of() : new ArrayList<>(errors);
    this.warnings = warnings == null ? List.of() : new ArrayList<>(warnings);
  }

  /**
   * 判断校验是否通过。
   *
   * @return 无错误项时返回 {@code true}
   */
  public boolean isPassed() {
    return errors.isEmpty();
  }

  /**
   * 返回阻断生成的错误项列表。
   *
   * @return 错误项列表副本
   */
  public List<LowcodeGenerationValidationItem> getErrors() {
    return new ArrayList<>(errors);
  }

  /**
   * 返回不阻断生成的警告项列表。
   *
   * @return 警告项列表副本
   */
  public List<LowcodeGenerationValidationItem> getWarnings() {
    return new ArrayList<>(warnings);
  }
}
