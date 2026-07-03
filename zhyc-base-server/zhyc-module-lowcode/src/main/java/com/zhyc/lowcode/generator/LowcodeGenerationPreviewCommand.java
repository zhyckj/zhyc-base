/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.Objects;

/**
 * 低代码生成预览命令。
 *
 * <p>该命令只描述生成预览所需的稳定输入，不包含写入目录和覆盖策略，避免预览接口直接修改工程文件。</p>
 */
public class LowcodeGenerationPreviewCommand {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 表模型编码。 */
  private final String tableModelCode;
  /** 生成目标端。 */
  private final GenerationTarget target;
  /** 业务模块名称。 */
  private final String moduleName;
  /** 业务实体名称。 */
  private final String entityName;

  /**
   * 创建低代码生成预览命令。
   *
   * @param tenantId 租户业务编码
   * @param tableModelCode 表模型编码
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   */
  public LowcodeGenerationPreviewCommand(String tenantId, String tableModelCode, GenerationTarget target,
                                         String moduleName, String entityName) {
    this.tenantId = requireText(tenantId, "租户业务编码不能为空");
    this.tableModelCode = requireText(tableModelCode, "表模型编码不能为空");
    this.target = Objects.requireNonNull(target, "生成目标不能为空");
    this.moduleName = requireCodeName(moduleName, "业务模块名称不能为空", "业务模块名称不能包含空白字符");
    this.entityName = requireCodeName(entityName, "业务实体名称不能为空", "业务实体名称不能包含空白字符");
  }

  /**
   * 返回租户业务编码。
   *
   * @return 租户业务编码
   */
  public String getTenantId() {
    return tenantId;
  }

  /**
   * 返回表模型编码。
   *
   * @return 表模型编码
   */
  public String getTableModelCode() {
    return tableModelCode;
  }

  /**
   * 返回生成目标端。
   *
   * @return 生成目标端
   */
  public GenerationTarget getTarget() {
    return target;
  }

  /**
   * 返回业务模块名称。
   *
   * @return 业务模块名称
   */
  public String getModuleName() {
    return moduleName;
  }

  /**
   * 返回业务实体名称。
   *
   * @return 业务实体名称
   */
  public String getEntityName() {
    return entityName;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  /**
   * 校验生成命名不能为空且不能包含空白字符。
   *
   * @param value 原始命名
   * @param blankMessage 空值错误消息
   * @param whitespaceMessage 包含空白字符错误消息
   * @return 清理后的命名
   */
  private static String requireCodeName(String value, String blankMessage, String whitespaceMessage) {
    String normalized = requireText(value, blankMessage);
    if (normalized.chars().anyMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException(whitespaceMessage);
    }
    return normalized;
  }
}
