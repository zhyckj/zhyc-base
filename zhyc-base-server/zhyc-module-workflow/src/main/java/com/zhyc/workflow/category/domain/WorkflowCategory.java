/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.domain;

import java.time.LocalDateTime;

/**
 * 工作流分类模型。
 *
 * <p>用于对流程模型进行租户内分组展示，首期服务于流程设计、流程部署和表单绑定入口。</p>
 */
public class WorkflowCategory {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 流程分类编码，租户内唯一。 */
  private String categoryCode;
  /** 流程分类名称。 */
  private String categoryName;
  /** 排序号，数值越小越靠前。 */
  private Integer sortOrder;
  /** 分类状态，例如 enabled、disabled。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;
  /** 备注说明。 */
  private String remark;

  /**
   * 创建空工作流分类对象。
   */
  public WorkflowCategory() {
  }

  /**
   * 创建完整工作流分类对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param categoryCode 流程分类编码
   * @param categoryName 流程分类名称
   * @param sortOrder 排序号
   * @param status 分类状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   * @param remark 备注说明
   */
  public WorkflowCategory(Long id, String tenantId, String categoryCode, String categoryName,
      Integer sortOrder, String status, LocalDateTime createdAt, LocalDateTime updatedAt,
      String remark) {
    this.id = id;
    this.tenantId = tenantId;
    this.categoryCode = categoryCode;
    this.categoryName = categoryName;
    this.sortOrder = sortOrder;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.remark = remark;
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
   * 设置数据库主键。
   *
   * @param id 数据库主键
   */
  public void setId(Long id) {
    this.id = id;
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
   * 返回创建时间。
   *
   * @return 创建时间
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * 设置创建时间。
   *
   * @param createdAt 创建时间
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * 返回更新时间。
   *
   * @return 更新时间
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * 设置更新时间。
   *
   * @param updatedAt 更新时间
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
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
