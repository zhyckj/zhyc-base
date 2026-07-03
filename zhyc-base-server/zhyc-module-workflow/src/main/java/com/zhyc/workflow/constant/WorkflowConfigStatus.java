/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.constant;

/**
 * 工作流配置状态枚举。
 *
 * <p>用于统一流程分类、流程模型和表单绑定等配置类数据的启停状态，避免配置入口保存未定义状态。</p>
 */
public enum WorkflowConfigStatus {

  /** 启用状态，配置可参与流程发布、表单跳转和后台查询。 */
  ENABLED("enabled", "启用"),

  /** 禁用状态，配置保留但不应参与新的流程发布或表单绑定使用。 */
  DISABLED("disabled", "禁用");

  /** 持久化状态编码；对应工作流配置表 status 字段。 */
  private final String code;

  /** 状态中文说明；用于后台展示和审计解释。 */
  private final String description;

  /**
   * 创建工作流配置状态枚举。
   *
   * @param code 持久化状态编码
   * @param description 状态中文说明
   */
  WorkflowConfigStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 获取持久化状态编码。
   *
   * @return 工作流配置表 status 字段使用的状态编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 获取状态中文说明。
   *
   * @return 状态中文说明
   */
  public String getDescription() {
    return description;
  }

  /**
   * 根据持久化编码解析工作流配置状态。
   *
   * <p>只允许首期支持的配置启停状态，避免模型发布和表单绑定出现未定义生命周期。</p>
   *
   * @param code 持久化状态编码
   * @return 匹配的工作流配置状态
   */
  public static WorkflowConfigStatus fromCode(String code) {
    for (WorkflowConfigStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("工作流配置状态不支持: " + code);
  }
}
