/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.controller;

/**
 * 工作流流程模型发布请求。
 */
public class WorkflowProcessModelDeployRequest {

  /** BPMN XML 文本，由后台流程设计器或模型转换器提交。 */
  private String bpmnXml;
  /** 发布备注，写入平台流程定义版本。 */
  private String remark;

  /** @return BPMN XML 文本 */
  public String getBpmnXml() {
    return bpmnXml;
  }

  /** @param bpmnXml BPMN XML 文本 */
  public void setBpmnXml(String bpmnXml) {
    this.bpmnXml = bpmnXml;
  }

  /** @return 发布备注 */
  public String getRemark() {
    return remark;
  }

  /** @param remark 发布备注 */
  public void setRemark(String remark) {
    this.remark = remark;
  }
}
