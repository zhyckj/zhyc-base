/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.service;

/**
 * 工作流分类保存命令。
 */
public class WorkflowCategorySaveCommand {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 分类主键；为空时按租户和分类编码新增或更新。 */
  private final Long id;
  /** 流程分类编码。 */
  private final String categoryCode;
  /** 流程分类名称。 */
  private final String categoryName;
  /** 排序号。 */
  private final Integer sortOrder;
  /** 分类状态。 */
  private final String status;
  /** 备注说明。 */
  private final String remark;

  /**
   * 创建工作流分类保存命令。
   *
   * @param tenantId 租户业务编码
   * @param id 分类主键
   * @param categoryCode 流程分类编码
   * @param categoryName 流程分类名称
   * @param sortOrder 排序号
   * @param status 分类状态
   * @param remark 备注说明
   */
  public WorkflowCategorySaveCommand(String tenantId, Long id, String categoryCode,
      String categoryName, Integer sortOrder, String status, String remark) {
    this.tenantId = tenantId;
    this.id = id;
    this.categoryCode = categoryCode;
    this.categoryName = categoryName;
    this.sortOrder = sortOrder;
    this.status = status;
    this.remark = remark;
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
   * 返回分类主键。
   *
   * @return 分类主键
   */
  public Long getId() {
    return id;
  }

  /**
   * 返回流程分类编码。
   *
   * @return 流程分类编码
   */
  public String getCategoryCode() {
    return categoryCode;
  }

  /**
   * 返回流程分类名称。
   *
   * @return 流程分类名称
   */
  public String getCategoryName() {
    return categoryName;
  }

  /**
   * 返回排序号。
   *
   * @return 排序号
   */
  public Integer getSortOrder() {
    return sortOrder;
  }

  /**
   * 返回分类状态。
   *
   * @return 分类状态
   */
  public String getStatus() {
    return status;
  }

  /**
   * 返回备注说明。
   *
   * @return 备注说明
   */
  public String getRemark() {
    return remark;
  }
}
