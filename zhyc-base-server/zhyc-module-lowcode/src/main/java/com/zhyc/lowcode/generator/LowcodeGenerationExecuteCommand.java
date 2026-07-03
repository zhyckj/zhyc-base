/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.Objects;

/**
 * 低代码生成执行命令。
 *
 * <p>相比预览命令，执行命令额外携带覆盖策略，用于写入代码文件并记录生成审计。</p>
 */
public class LowcodeGenerationExecuteCommand {

  /** 生成预览命令。 */
  private final LowcodeGenerationPreviewCommand previewCommand;
  /** 生成文件覆盖策略。 */
  private final GeneratedFileOverwriteStrategy overwriteStrategy;

  /**
   * 创建低代码生成执行命令。
   *
   * @param tenantId 租户业务编码
   * @param tableModelCode 表模型编码
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param overwriteStrategy 生成文件覆盖策略
   */
  public LowcodeGenerationExecuteCommand(String tenantId, String tableModelCode, GenerationTarget target,
                                         String moduleName, String entityName,
                                         GeneratedFileOverwriteStrategy overwriteStrategy) {
    this.previewCommand = new LowcodeGenerationPreviewCommand(tenantId, tableModelCode, target, moduleName, entityName);
    this.overwriteStrategy = Objects.requireNonNull(overwriteStrategy, "生成文件覆盖策略不能为空");
  }

  /**
   * 返回生成预览命令。
   *
   * @return 生成预览命令
   */
  public LowcodeGenerationPreviewCommand getPreviewCommand() {
    return previewCommand;
  }

  /**
   * 返回生成文件覆盖策略。
   *
   * @return 生成文件覆盖策略
   */
  public GeneratedFileOverwriteStrategy getOverwriteStrategy() {
    return overwriteStrategy;
  }
}
