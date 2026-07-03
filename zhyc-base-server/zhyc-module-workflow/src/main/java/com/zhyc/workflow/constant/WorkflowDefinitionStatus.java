/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.constant;

/**
 * 工作流流程定义状态枚举。
 *
 * <p>用于统一流程定义版本的生命周期状态，避免已发布定义出现未定义状态影响运行期匹配。</p>
 */
public enum WorkflowDefinitionStatus {

  /** 激活状态，流程定义可被业务流程发起和任务路由使用。 */
  ACTIVE("active", "激活"),

  /** 停用状态，流程定义保留版本记录但不应继续发起新流程。 */
  DISABLED("disabled", "停用");

  /** 持久化状态编码；对应流程定义表 status 字段。 */
  private final String code;

  /** 状态中文说明；用于后台展示和审计解释。 */
  private final String description;

  /**
   * 创建工作流流程定义状态枚举。
   *
   * @param code 持久化状态编码
   * @param description 状态中文说明
   */
  WorkflowDefinitionStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 获取持久化状态编码。
   *
   * @return 流程定义表 status 字段使用的状态编码
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
   * 根据持久化编码解析流程定义状态。
   *
   * <p>只允许首期支持的定义生命周期状态，避免运行期流程匹配出现未定义分支。</p>
   *
   * @param code 持久化状态编码
   * @return 匹配的流程定义状态
   */
  public static WorkflowDefinitionStatus fromCode(String code) {
    for (WorkflowDefinitionStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("工作流流程定义状态不支持: " + code);
  }
}
