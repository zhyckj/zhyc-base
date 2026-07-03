/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.controller;

/**
 * 工作流表单绑定保存请求。
 */
public class WorkflowFormBindingSaveRequest {

  /** 绑定主键。 */
  private Long id;
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
  /** 备注说明。 */
  private String remark;

  /** @return 绑定主键 */
  public Long getId() {
    return id;
  }

  /** @param id 绑定主键 */
  public void setId(Long id) {
    this.id = id;
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

  /** @return 备注说明 */
  public String getRemark() {
    return remark;
  }

  /** @param remark 备注说明 */
  public void setRemark(String remark) {
    this.remark = remark;
  }
}
