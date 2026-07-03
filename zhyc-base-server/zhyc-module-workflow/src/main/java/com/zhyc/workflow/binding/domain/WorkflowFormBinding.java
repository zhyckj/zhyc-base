/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.domain;

import java.time.LocalDateTime;

/**
 * 工作流表单绑定模型。
 *
 * <p>用于把流程定义 key 与后台表单、移动端表单及业务表建立租户内绑定关系。</p>
 */
public class WorkflowFormBinding {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 流程定义 key。 */
  private String processKey;
  /** 业务模块编码。 */
  private String businessModule;
  /** 业务表名。 */
  private String businessTable;
  /** 后台表单路由。 */
  private String formRoute;
  /** 移动端表单路由。 */
  private String mobileRoute;
  /** 绑定状态。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;
  /** 备注说明。 */
  private String remark;

  /** 创建空工作流表单绑定对象。 */
  public WorkflowFormBinding() {
  }

  /**
   * 创建完整工作流表单绑定对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param processKey 流程定义 key
   * @param businessModule 业务模块编码
   * @param businessTable 业务表名
   * @param formRoute 后台表单路由
   * @param mobileRoute 移动端表单路由
   * @param status 绑定状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   * @param remark 备注说明
   */
  public WorkflowFormBinding(Long id, String tenantId, String processKey, String businessModule,
      String businessTable, String formRoute, String mobileRoute, String status,
      LocalDateTime createdAt, LocalDateTime updatedAt, String remark) {
    this.id = id;
    this.tenantId = tenantId;
    this.processKey = processKey;
    this.businessModule = businessModule;
    this.businessTable = businessTable;
    this.formRoute = formRoute;
    this.mobileRoute = mobileRoute;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.remark = remark;
  }

  /** @return 数据库主键 */
  public Long getId() {
    return id;
  }

  /** @param id 数据库主键 */
  public void setId(Long id) {
    this.id = id;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @param tenantId 租户业务编码 */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  /** @return 流程定义 key */
  public String getProcessKey() {
    return processKey;
  }

  /** @param processKey 流程定义 key */
  public void setProcessKey(String processKey) {
    this.processKey = processKey;
  }

  /** @return 业务模块编码 */
  public String getBusinessModule() {
    return businessModule;
  }

  /** @param businessModule 业务模块编码 */
  public void setBusinessModule(String businessModule) {
    this.businessModule = businessModule;
  }

  /** @return 业务表名 */
  public String getBusinessTable() {
    return businessTable;
  }

  /** @param businessTable 业务表名 */
  public void setBusinessTable(String businessTable) {
    this.businessTable = businessTable;
  }

  /** @return 后台表单路由 */
  public String getFormRoute() {
    return formRoute;
  }

  /** @param formRoute 后台表单路由 */
  public void setFormRoute(String formRoute) {
    this.formRoute = formRoute;
  }

  /** @return 移动端表单路由 */
  public String getMobileRoute() {
    return mobileRoute;
  }

  /** @param mobileRoute 移动端表单路由 */
  public void setMobileRoute(String mobileRoute) {
    this.mobileRoute = mobileRoute;
  }

  /** @return 绑定状态 */
  public String getStatus() {
    return status;
  }

  /** @param status 绑定状态 */
  public void setStatus(String status) {
    this.status = status;
  }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /** @param createdAt 创建时间 */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /** @return 更新时间 */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /** @param updatedAt 更新时间 */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  /** @return 备注说明 */
  public String getRemark() {
    return remark;
  }

  /** @param remark 备注说明 */
  public void setRemark(String remark) {
    this.remark = remark;
  }
}
