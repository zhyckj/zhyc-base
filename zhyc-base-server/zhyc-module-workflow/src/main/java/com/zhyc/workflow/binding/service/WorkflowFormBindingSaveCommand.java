/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.service;

/**
 * 工作流表单绑定保存命令。
 */
public class WorkflowFormBindingSaveCommand {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 绑定主键。 */
  private final Long id;
  /** 流程定义 key。 */
  private final String processKey;
  /** 业务模块编码。 */
  private final String businessModule;
  /** 业务表名。 */
  private final String businessTable;
  /** 后台表单路由。 */
  private final String formRoute;
  /** 移动端表单路由。 */
  private final String mobileRoute;
  /** 绑定状态。 */
  private final String status;
  /** 备注说明。 */
  private final String remark;

  /**
   * 创建工作流表单绑定保存命令。
   *
   * @param tenantId 租户业务编码
   * @param id 绑定主键
   * @param processKey 流程定义 key
   * @param businessModule 业务模块编码
   * @param businessTable 业务表名
   * @param formRoute 后台表单路由
   * @param mobileRoute 移动端表单路由
   * @param status 绑定状态
   * @param remark 备注说明
   */
  public WorkflowFormBindingSaveCommand(String tenantId, Long id, String processKey,
      String businessModule, String businessTable, String formRoute, String mobileRoute,
      String status, String remark) {
    this.tenantId = tenantId;
    this.id = id;
    this.processKey = processKey;
    this.businessModule = businessModule;
    this.businessTable = businessTable;
    this.formRoute = formRoute;
    this.mobileRoute = mobileRoute;
    this.status = status;
    this.remark = remark;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 绑定主键 */
  public Long getId() {
    return id;
  }

  /** @return 流程定义 key */
  public String getProcessKey() {
    return processKey;
  }

  /** @return 业务模块编码 */
  public String getBusinessModule() {
    return businessModule;
  }

  /** @return 业务表名 */
  public String getBusinessTable() {
    return businessTable;
  }

  /** @return 后台表单路由 */
  public String getFormRoute() {
    return formRoute;
  }

  /** @return 移动端表单路由 */
  public String getMobileRoute() {
    return mobileRoute;
  }

  /** @return 绑定状态 */
  public String getStatus() {
    return status;
  }

  /** @return 备注说明 */
  public String getRemark() {
    return remark;
  }
}
