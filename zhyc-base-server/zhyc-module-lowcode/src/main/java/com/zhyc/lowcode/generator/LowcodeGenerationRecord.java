/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.Objects;

/**
 * 低代码生成记录。
 *
 * <p>用于保存一次生成动作的核心审计信息，由生成记录仓储写入生成记录表。</p>
 */
public class LowcodeGenerationRecord {

  /** 数据库主键。 */
  private final Long id;
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
  /** 生成文件覆盖策略。 */
  private final GeneratedFileOverwriteStrategy overwriteStrategy;
  /** 生成文件数量。 */
  private final int fileCount;
  /** 生成文件清单 JSON。 */
  private final String fileManifestJson;
  /** 生成记录状态。 */
  private final LowcodeGenerationRecordStatus status;
  /** 失败原因，成功时为空字符串。 */
  private final String errorMessage;

  private LowcodeGenerationRecord(Long id, String tenantId, String tableModelCode, GenerationTarget target,
                                  String moduleName, String entityName,
                                  GeneratedFileOverwriteStrategy overwriteStrategy, int fileCount,
                                  String fileManifestJson,
                                  LowcodeGenerationRecordStatus status, String errorMessage) {
    if (fileCount < 0) {
      throw new IllegalArgumentException("生成文件数量不能为负数");
    }
    this.id = id;
    this.tenantId = requireText(tenantId, "租户业务编码不能为空");
    this.tableModelCode = requireText(tableModelCode, "表模型编码不能为空");
    this.target = Objects.requireNonNull(target, "生成目标端不能为空");
    this.moduleName = requireCodeName(moduleName, "业务模块名称不能为空", "业务模块名称不能包含空白字符");
    this.entityName = requireCodeName(entityName, "业务实体名称不能为空", "业务实体名称不能包含空白字符");
    this.overwriteStrategy = Objects.requireNonNull(overwriteStrategy, "生成文件覆盖策略不能为空");
    this.fileCount = fileCount;
    this.fileManifestJson = fileManifestJson == null ? "[]" : fileManifestJson.trim();
    this.status = Objects.requireNonNull(status, "生成记录状态不能为空");
    this.errorMessage = errorMessage == null ? "" : errorMessage.trim();
  }

  /**
   * 创建成功生成记录。
   *
   * @param tenantId 租户业务编码
   * @param tableModelCode 表模型编码
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param overwriteStrategy 生成文件覆盖策略
   * @param fileCount 生成文件数量
   * @return 成功生成记录
   */
  public static LowcodeGenerationRecord success(String tenantId, String tableModelCode, GenerationTarget target,
                                                String moduleName, String entityName,
                                                GeneratedFileOverwriteStrategy overwriteStrategy, int fileCount) {
    return success(tenantId, tableModelCode, target, moduleName, entityName, overwriteStrategy, fileCount, "[]");
  }

  /**
   * 创建成功生成记录。
   *
   * @param tenantId 租户业务编码
   * @param tableModelCode 表模型编码
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param overwriteStrategy 生成文件覆盖策略
   * @param fileCount 生成文件数量
   * @param fileManifestJson 生成文件清单 JSON
   * @return 成功生成记录
   */
  public static LowcodeGenerationRecord success(String tenantId, String tableModelCode, GenerationTarget target,
                                                String moduleName, String entityName,
                                                GeneratedFileOverwriteStrategy overwriteStrategy, int fileCount,
                                                String fileManifestJson) {
    return new LowcodeGenerationRecord(null, tenantId, tableModelCode, target, moduleName, entityName,
        overwriteStrategy, fileCount, fileManifestJson, LowcodeGenerationRecordStatus.SUCCESS, "");
  }

  /**
   * 创建失败生成记录。
   *
   * @param tenantId 租户业务编码
   * @param tableModelCode 表模型编码
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param overwriteStrategy 生成文件覆盖策略
   * @param fileCount 已生成或尝试生成的文件数量
   * @param errorMessage 失败原因
   * @return 失败生成记录
   */
  public static LowcodeGenerationRecord failed(String tenantId, String tableModelCode, GenerationTarget target,
                                               String moduleName, String entityName,
                                               GeneratedFileOverwriteStrategy overwriteStrategy, int fileCount,
                                               String errorMessage) {
    return failed(tenantId, tableModelCode, target, moduleName, entityName, overwriteStrategy, fileCount, "[]",
        errorMessage);
  }

  /**
   * 创建失败生成记录。
   *
   * @param tenantId 租户业务编码
   * @param tableModelCode 表模型编码
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param overwriteStrategy 生成文件覆盖策略
   * @param fileCount 已生成或尝试生成的文件数量
   * @param fileManifestJson 生成文件清单 JSON
   * @param errorMessage 失败原因
   * @return 失败生成记录
   */
  public static LowcodeGenerationRecord failed(String tenantId, String tableModelCode, GenerationTarget target,
                                               String moduleName, String entityName,
                                               GeneratedFileOverwriteStrategy overwriteStrategy, int fileCount,
                                               String fileManifestJson,
                                               String errorMessage) {
    return new LowcodeGenerationRecord(null, tenantId, tableModelCode, target, moduleName, entityName,
        overwriteStrategy, fileCount, fileManifestJson, LowcodeGenerationRecordStatus.FAILED, errorMessage);
  }

  /**
   * 从持久化数据恢复生成记录。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param tableModelCode 表模型编码
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param overwriteStrategy 生成文件覆盖策略
   * @param fileCount 生成文件数量
   * @param status 生成记录状态
   * @param errorMessage 失败原因
   * @return 生成记录
   */
  public static LowcodeGenerationRecord restore(Long id, String tenantId, String tableModelCode,
                                                GenerationTarget target, String moduleName, String entityName,
                                                GeneratedFileOverwriteStrategy overwriteStrategy, int fileCount,
                                                LowcodeGenerationRecordStatus status, String errorMessage) {
    return restore(id, tenantId, tableModelCode, target, moduleName, entityName, overwriteStrategy, fileCount,
        "[]", status, errorMessage);
  }

  /**
   * 从持久化数据恢复生成记录。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param tableModelCode 表模型编码
   * @param target 生成目标端
   * @param moduleName 业务模块名称
   * @param entityName 业务实体名称
   * @param overwriteStrategy 生成文件覆盖策略
   * @param fileCount 生成文件数量
   * @param fileManifestJson 生成文件清单 JSON
   * @param status 生成记录状态
   * @param errorMessage 失败原因
   * @return 生成记录
   */
  public static LowcodeGenerationRecord restore(Long id, String tenantId, String tableModelCode,
                                                GenerationTarget target, String moduleName, String entityName,
                                                GeneratedFileOverwriteStrategy overwriteStrategy, int fileCount,
                                                String fileManifestJson,
                                                LowcodeGenerationRecordStatus status, String errorMessage) {
    return new LowcodeGenerationRecord(id, tenantId, tableModelCode, target, moduleName, entityName,
        overwriteStrategy, fileCount, fileManifestJson, status, errorMessage);
  }

  /**
   * 返回数据库主键。
   *
   * @return 数据库主键
   */
  public Long getId() {
    return id;
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

  /**
   * 返回生成文件覆盖策略。
   *
   * @return 生成文件覆盖策略
   */
  public GeneratedFileOverwriteStrategy getOverwriteStrategy() {
    return overwriteStrategy;
  }

  /**
   * 返回生成文件数量。
   *
   * @return 生成文件数量
   */
  public int getFileCount() {
    return fileCount;
  }

  /**
   * 返回生成文件清单 JSON。
   *
   * @return 生成文件清单 JSON
   */
  public String getFileManifestJson() {
    return fileManifestJson;
  }

  /**
   * 返回生成记录状态。
   *
   * @return 生成记录状态
   */
  public LowcodeGenerationRecordStatus getStatus() {
    return status;
  }

  /**
   * 返回失败原因。
   *
   * @return 失败原因，成功时为空字符串
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  /**
   * 校验生成记录命名不能为空且不能包含空白字符。
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
