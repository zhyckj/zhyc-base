/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 低代码生成执行请求。
 */
public class LowcodeGenerationExecuteRequest {

  /** 租户业务编码。 */
  private String tenantId;
  /** 表模型编码。 */
  private String tableModelCode;
  /** 生成目标端编码。 */
  private String target;
  /** 业务模块名称。 */
  private String moduleName;
  /** 业务实体名称。 */
  private String entityName;
  /** 生成文件覆盖策略编码。 */
  private String overwriteStrategy;

  /**
   * 转换为生成执行命令。
   *
   * @return 生成执行命令
   */
  public LowcodeGenerationExecuteCommand toCommand() {
    return new LowcodeGenerationExecuteCommand(
        tenantId,
        tableModelCode,
        GenerationTarget.fromCode(target),
        moduleName,
        entityName,
        resolveOverwriteStrategy());
  }

  /**
   * 解析覆盖策略，未传时使用保护人工代码的默认策略。
   *
   * @return 生成文件覆盖策略
   */
  private GeneratedFileOverwriteStrategy resolveOverwriteStrategy() {
    if (overwriteStrategy == null || overwriteStrategy.trim().isEmpty()) {
      return GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS;
    }
    return GeneratedFileOverwriteStrategy.valueOf(overwriteStrategy.trim());
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
   * 设置租户业务编码。
   *
   * @param tenantId 租户业务编码
   */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
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
   * 设置表模型编码。
   *
   * @param tableModelCode 表模型编码
   */
  public void setTableModelCode(String tableModelCode) {
    this.tableModelCode = tableModelCode;
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
   * 设置生成目标端编码。
   *
   * @param target 生成目标端编码
   */
  public void setTarget(String target) {
    this.target = target;
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
   * 设置业务模块名称。
   *
   * @param moduleName 业务模块名称
   */
  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
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
   * 设置业务实体名称。
   *
   * @param entityName 业务实体名称
   */
  public void setEntityName(String entityName) {
    this.entityName = entityName;
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
   * 设置生成文件覆盖策略编码。
   *
   * @param overwriteStrategy 生成文件覆盖策略编码
   */
  public void setOverwriteStrategy(String overwriteStrategy) {
    this.overwriteStrategy = overwriteStrategy;
  }
}
