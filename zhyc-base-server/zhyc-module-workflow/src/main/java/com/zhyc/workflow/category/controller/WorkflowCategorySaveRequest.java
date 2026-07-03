/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.controller;

/**
 * 工作流分类保存请求。
 */
public class WorkflowCategorySaveRequest {

  /** 分类主键；为空时按租户和分类编码新增或更新。 */
  private Long id;
  /** 流程分类编码。 */
  private String categoryCode;
  /** 流程分类名称。 */
  private String categoryName;
  /** 排序号。 */
  private Integer sortOrder;
  /** 分类状态。 */
  private String status;
  /** 备注说明。 */
  private String remark;

  /**
   * 返回分类主键。
   *
   * @return 分类主键
   */
  public Long getId() {
    return id;
  }

  /**
   * 设置分类主键。
   *
   * @param id 分类主键
   */
  public void setId(Long id) {
    this.id = id;
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
   * 设置流程分类编码。
   *
   * @param categoryCode 流程分类编码
   */
  public void setCategoryCode(String categoryCode) {
    this.categoryCode = categoryCode;
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
   * 设置流程分类名称。
   *
   * @param categoryName 流程分类名称
   */
  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
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
   * 设置排序号。
   *
   * @param sortOrder 排序号
   */
  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
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
   * 设置分类状态。
   *
   * @param status 分类状态
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * 返回备注说明。
   *
   * @return 备注说明
   */
  public String getRemark() {
    return remark;
  }

  /**
   * 设置备注说明。
   *
   * @param remark 备注说明
   */
  public void setRemark(String remark) {
    this.remark = remark;
  }
}
