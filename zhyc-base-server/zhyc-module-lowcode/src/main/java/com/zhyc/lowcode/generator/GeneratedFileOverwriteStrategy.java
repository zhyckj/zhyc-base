/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 生成文件覆盖策略。
 */
public enum GeneratedFileOverwriteStrategy {

  /** 文件已存在时直接失败，默认用于保护人工修改代码。 */
  FAIL_IF_EXISTS,
  /** 文件已存在时跳过写入，只写入不存在的新文件。 */
  SKIP_IF_EXISTS,
  /** 文件已存在时显式覆盖，用于用户确认重新生成的场景。 */
  OVERWRITE
}
