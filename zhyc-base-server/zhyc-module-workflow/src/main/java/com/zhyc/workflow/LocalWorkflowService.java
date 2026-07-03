/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow;

import com.zhyc.common.workflow.WorkflowService;
import com.zhyc.workflow.repository.WorkflowRuntimeRepository;
import com.zhyc.workflow.support.WorkflowServiceValidation;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 本地工作流服务实现。
 *
 * <p>该实现用于首期完成业务模块与工作流门面的稳定对接，不绑定具体流程引擎。
 * 后续接入 Flowable 等引擎时，只需要替换该接口实现，采购等业务模块无需调整。</p>
 */
@Service
public class LocalWorkflowService implements WorkflowService {

  /** 本地流程实例 ID 前缀。 */
  private static final String LOCAL_INSTANCE_PREFIX = "local";
  /** 首个待办任务名称。 */
  private static final String FIRST_TASK_NAME = "发起审批";

  /** 工作流运行仓储，单元测试无参构造时允许为空。 */
  private final WorkflowRuntimeRepository workflowRuntimeRepository;

  /**
   * 创建无持久化能力的本地工作流服务，主要用于轻量单元测试。
   */
  public LocalWorkflowService() {
    this.workflowRuntimeRepository = null;
  }

  /**
   * 创建具备运行数据持久化能力的本地工作流服务。
   *
   * @param workflowRuntimeRepository 工作流运行仓储
   */
  @Autowired
  public LocalWorkflowService(WorkflowRuntimeRepository workflowRuntimeRepository) {
    this.workflowRuntimeRepository = Objects.requireNonNull(workflowRuntimeRepository,
        "工作流运行仓储不能为空");
  }

  /**
   * 启动本地流程实例。
   *
   * <p>首期本地实现会生成稳定流程实例 ID，并在配置运行仓储时写入流程实例和首个待办任务。</p>
   *
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param variables 流程变量，包含租户和发起人上下文
   * @return 本地流程实例 ID
   */
  @Override
  public String startProcess(String processKey, String businessKey, Map<String, Object> variables) {
    String requiredProcessKey = requireText(processKey, "流程定义 key 不能为空");
    String requiredBusinessKey = requireText(businessKey, "业务对象唯一标识不能为空");
    Map<String, Object> normalizedVariables = normalizeVariables(variables);
    String processInstanceId = LOCAL_INSTANCE_PREFIX + "-" + normalizeIdentifier(requiredProcessKey) + "-"
        + requiredBusinessKey;
    if (workflowRuntimeRepository != null) {
      workflowRuntimeRepository.startProcess(resolveTenantId(normalizedVariables),
          processInstanceId, requiredProcessKey, requiredBusinessKey,
          resolveStarterUserId(normalizedVariables), "task-" + processInstanceId, FIRST_TASK_NAME,
          resolveStarterUserId(normalizedVariables));
    }
    return processInstanceId;
  }

  /**
   * 本地审批通过任务。
   *
   * @param taskId 待审批任务 ID
   * @param comment 审批意见
   * @param variables 审批变量
   */
  @Override
  public void approve(String taskId, String comment, Map<String, Object> variables) {
    requireText(taskId, "待审批任务 ID 不能为空");
    normalizeVariables(variables);
  }

  /**
   * 本地驳回任务。
   *
   * @param taskId 待驳回任务 ID
   * @param comment 驳回意见
   */
  @Override
  public void reject(String taskId, String comment) {
    requireText(taskId, "待驳回任务 ID 不能为空");
  }

  /**
   * 本地撤回流程实例。
   *
   * @param processInstanceId 流程实例 ID
   * @param reason 撤回原因
   */
  @Override
  public void revoke(String processInstanceId, String reason) {
    requireText(processInstanceId, "流程实例 ID 不能为空");
  }

  /**
   * 规范化流程变量，当前本地实现只做空值兼容，真实引擎适配器可在此处扩展变量序列化策略。
   *
   * @param variables 原始流程变量
   * @return 非空流程变量
   */
  private Map<String, Object> normalizeVariables(Map<String, Object> variables) {
    return variables == null ? Collections.emptyMap() : variables;
  }

  /**
   * 规范化流程定义 key，避免流程实例 ID 中出现不稳定分隔符。
   *
   * @param value 原始标识
   * @return 规范化后的标识
   */
  private String normalizeIdentifier(String value) {
    return value.replaceAll("[^A-Za-z0-9_-]", "_");
  }

  /**
   * 从流程变量中解析租户业务编码。
   *
   * @param variables 流程变量
   * @return 租户业务编码
   */
  private String resolveTenantId(Map<String, Object> variables) {
    Object tenantId = variables.get("tenantId");
    if (tenantId == null) {
      throw WorkflowServiceValidation.businessFailure("流程变量 tenantId 不能为空");
    }
    return requireText(String.valueOf(tenantId), "流程变量 tenantId 不能为空");
  }

  /**
   * 从流程变量中解析发起人用户 ID。
   *
   * @param variables 流程变量
   * @return 发起人用户 ID
   */
  private Long resolveStarterUserId(Map<String, Object> variables) {
    Object starterUserId = variables.get("starterUserId");
    if (starterUserId == null) {
      return null;
    }
    if (starterUserId instanceof Number number) {
      return number.longValue();
    }
    return Long.valueOf(String.valueOf(starterUserId));
  }

  /**
   * 校验文本不能为空并去除首尾空白。
   *
   * @param value 原始文本
   * @param message 为空时的异常消息
   * @return 清理后的文本
   */
  private String requireText(String value, String message) {
    if (value == null) {
      throw WorkflowServiceValidation.businessFailure(message);
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw WorkflowServiceValidation.businessFailure(message);
    }
    return trimmed;
  }
}
