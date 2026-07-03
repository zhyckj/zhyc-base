/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 低代码生成记录响应对象。
 */
public class LowcodeGenerationRecordResponse {

  /** 数据库主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 表模型编码。 */
  private final String tableModelCode;
  /** 生成目标端编码。 */
  private final String target;
  /** 业务模块名称。 */
  private final String moduleName;
  /** 业务实体名称。 */
  private final String entityName;
  /** 生成文件覆盖策略编码。 */
  private final String overwriteStrategy;
  /** 生成文件数量。 */
  private final int fileCount;
  /** 生成文件清单 JSON。 */
  private final String fileManifestJson;
  /** 生成记录状态编码。 */
  private final String status;
  /** 失败原因。 */
  private final String errorMessage;

  private LowcodeGenerationRecordResponse(LowcodeGenerationRecord record) {
    this.id = record.getId();
    this.tenantId = record.getTenantId();
    this.tableModelCode = record.getTableModelCode();
    this.target = record.getTarget().getCode();
    this.moduleName = record.getModuleName();
    this.entityName = record.getEntityName();
    this.overwriteStrategy = record.getOverwriteStrategy().name();
    this.fileCount = record.getFileCount();
    this.fileManifestJson = record.getFileManifestJson();
    this.status = record.getStatus().name();
    this.errorMessage = record.getErrorMessage();
  }

  /**
   * 从生成记录转换为响应对象。
   *
   * @param record 低代码生成记录
   * @return 生成记录响应对象
   */
  public static LowcodeGenerationRecordResponse from(LowcodeGenerationRecord record) {
    return new LowcodeGenerationRecordResponse(record);
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
   * 返回生成目标端编码。
   *
   * @return 生成目标端编码
   */
  public String getTarget() {
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
   * 返回生成文件覆盖策略编码。
   *
   * @return 生成文件覆盖策略编码
   */
  public String getOverwriteStrategy() {
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
   * 返回生成记录状态编码。
   *
   * @return 生成记录状态编码
   */
  public String getStatus() {
    return status;
  }

  /**
   * 返回失败原因。
   *
   * @return 失败原因
   */
  public String getErrorMessage() {
    return errorMessage;
  }
}
