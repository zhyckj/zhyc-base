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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Flowable 工作流门面适配器。
 *
 * <p>该实现只暴露平台统一 {@link WorkflowService} 门面，业务模块不得直接依赖 Flowable API。
 * 当运行环境提供 Flowable {@link RuntimeService} 和 {@link TaskService} Bean 时，本适配器作为首选工作流实现。</p>
 */
@Primary
@Service
@ConditionalOnBean({RuntimeService.class, TaskService.class})
public class FlowableWorkflowService implements WorkflowService {

  /** 未查询到 Flowable 首个任务时的平台兜底任务名称。 */
  private static final String DEFAULT_FIRST_TASK_NAME = "发起审批";

  /** Flowable 运行时服务，用于启动和撤回流程实例。 */
  private final RuntimeService runtimeService;
  /** Flowable 任务服务，用于完成审批任务。 */
  private final TaskService taskService;
  /** 平台工作流运行仓储，用于同步待办、发起和监控查询数据。 */
  private final WorkflowRuntimeRepository workflowRuntimeRepository;

  /**
   * 创建 Flowable 工作流门面适配器。
   *
   * @param runtimeService Flowable 运行时服务
   * @param taskService Flowable 任务服务
   */
  public FlowableWorkflowService(RuntimeService runtimeService, TaskService taskService) {
    this(runtimeService, taskService, (WorkflowRuntimeRepository) null);
  }

  /**
   * 创建可同步平台运行表的 Flowable 工作流门面适配器。
   *
   * @param runtimeService Flowable 运行时服务
   * @param taskService Flowable 任务服务
   * @param workflowRuntimeRepository 平台工作流运行仓储，允许为空以兼容轻量单元测试
   */
  public FlowableWorkflowService(RuntimeService runtimeService, TaskService taskService,
      WorkflowRuntimeRepository workflowRuntimeRepository) {
    this.runtimeService = Objects.requireNonNull(runtimeService, "Flowable 运行时服务不能为空");
    this.taskService = Objects.requireNonNull(taskService, "Flowable 任务服务不能为空");
    this.workflowRuntimeRepository = workflowRuntimeRepository;
  }

  /**
   * 创建 Spring 管理的 Flowable 工作流门面适配器。
   *
   * @param runtimeService Flowable 运行时服务
   * @param taskService Flowable 任务服务
   * @param workflowRuntimeRepositoryProvider 平台运行仓储提供器，未配置时只调用 Flowable
   */
  @Autowired
  public FlowableWorkflowService(RuntimeService runtimeService, TaskService taskService,
      ObjectProvider<WorkflowRuntimeRepository> workflowRuntimeRepositoryProvider) {
    this(runtimeService, taskService, workflowRuntimeRepositoryProvider.getIfAvailable());
  }

  /**
   * 通过 Flowable 启动流程实例。
   *
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param variables 流程变量
   * @return Flowable 流程实例 ID
   */
  @Override
  public String startProcess(String processKey, String businessKey, Map<String, Object> variables) {
    String requiredProcessKey = requireText(processKey, "流程定义 key 不能为空");
    String requiredBusinessKey = requireText(businessKey, "业务对象唯一标识不能为空");
    Map<String, Object> normalizedVariables = normalizeVariables(variables);
    String tenantId = null;
    Long starterUserId = null;
    if (workflowRuntimeRepository != null) {
      tenantId = resolveTenantId(normalizedVariables);
      starterUserId = resolveStarterUserId(normalizedVariables);
    }
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(requiredProcessKey,
        requiredBusinessKey, normalizedVariables);
    String processInstanceId = processInstance.getId();
    syncRuntimeRepository(tenantId, processInstanceId, requiredProcessKey, requiredBusinessKey,
        starterUserId);
    return processInstanceId;
  }

  /**
   * 审批通过 Flowable 任务。
   *
   * @param taskId 待审批任务 ID
   * @param comment 审批意见
   * @param variables 审批变量
   */
  @Override
  public void approve(String taskId, String comment, Map<String, Object> variables) {
    String requiredTaskId = requireText(taskId, "待审批任务 ID 不能为空");
    addCommentIfPresent(requiredTaskId, comment);
    taskService.complete(requiredTaskId, mergeVariables(variables, true, null));
  }

  /**
   * 驳回 Flowable 任务。
   *
   * @param taskId 待驳回任务 ID
   * @param comment 驳回意见
   */
  @Override
  public void reject(String taskId, String comment) {
    String requiredTaskId = requireText(taskId, "待驳回任务 ID 不能为空");
    String rejectComment = trimToNull(comment);
    addCommentIfPresent(requiredTaskId, rejectComment);
    taskService.complete(requiredTaskId, mergeVariables(Map.of(), false, rejectComment));
  }

  /**
   * 撤回 Flowable 流程实例。
   *
   * @param processInstanceId 流程实例 ID
   * @param reason 撤回原因
   */
  @Override
  public void revoke(String processInstanceId, String reason) {
    runtimeService.deleteProcessInstance(requireText(processInstanceId, "流程实例 ID 不能为空"),
        trimToNull(reason));
  }

  /**
   * 标准化流程变量。
   *
   * @param variables 原始流程变量
   * @return 非空流程变量
   */
  private Map<String, Object> normalizeVariables(Map<String, Object> variables) {
    return variables == null ? Collections.emptyMap() : variables;
  }

  /**
   * 同步 Flowable 流程实例到平台运行仓储。
   *
   * <p>后台待办、我发起的和流程监控统一读取平台运行表，因此真实 Flowable 启动后必须写入
   * `wf_process_instance` 和首个 `wf_task`。</p>
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId Flowable 流程实例 ID
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param starterUserId 流程发起人用户 ID
   */
  private void syncRuntimeRepository(String tenantId, String processInstanceId, String processKey,
      String businessKey, Long starterUserId) {
    if (workflowRuntimeRepository == null) {
      return;
    }
    Task firstTask = findFirstTask(processInstanceId);
    workflowRuntimeRepository.startProcess(tenantId, processInstanceId, processKey, businessKey,
        starterUserId, resolveFirstTaskId(processInstanceId, firstTask),
        resolveFirstTaskName(firstTask), resolveFirstTaskAssigneeUserId(firstTask, starterUserId));
  }

  /**
   * 查询 Flowable 流程实例的首个待办任务。
   *
   * @param processInstanceId Flowable 流程实例 ID
   * @return 首个待办任务，未产生任务时返回 {@code null}
   */
  private Task findFirstTask(String processInstanceId) {
    return taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
  }

  /**
   * 解析平台运行表使用的首个任务 ID。
   *
   * @param processInstanceId Flowable 流程实例 ID
   * @param firstTask Flowable 首个任务
   * @return 非空任务 ID
   */
  private String resolveFirstTaskId(String processInstanceId, Task firstTask) {
    if (firstTask == null) {
      return "task-" + processInstanceId;
    }
    String taskId = trimToNull(firstTask.getId());
    return taskId == null ? "task-" + processInstanceId : taskId;
  }

  /**
   * 解析平台运行表使用的首个任务名称。
   *
   * @param firstTask Flowable 首个任务
   * @return 非空任务名称
   */
  private String resolveFirstTaskName(Task firstTask) {
    if (firstTask == null) {
      return DEFAULT_FIRST_TASK_NAME;
    }
    String taskName = trimToNull(firstTask.getName());
    return taskName == null ? DEFAULT_FIRST_TASK_NAME : taskName;
  }

  /**
   * 解析平台运行表使用的首个任务处理人。
   *
   * @param firstTask Flowable 首个任务
   * @param fallbackUserId 未配置 Flowable 处理人时回退的发起人用户 ID
   * @return 首个任务处理人用户 ID，无法确定时返回 {@code null}
   */
  private Long resolveFirstTaskAssigneeUserId(Task firstTask, Long fallbackUserId) {
    if (firstTask == null) {
      return fallbackUserId;
    }
    String assignee = trimToNull(firstTask.getAssignee());
    if (assignee == null) {
      return fallbackUserId;
    }
    try {
      return Long.valueOf(assignee);
    } catch (NumberFormatException exception) {
      throw WorkflowServiceValidation.businessFailure("Flowable 任务处理人必须为用户 ID 数字");
    }
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
   * 从流程变量中解析流程发起人用户 ID。
   *
   * @param variables 流程变量
   * @return 发起人用户 ID，未提供时返回 {@code null}
   */
  private Long resolveStarterUserId(Map<String, Object> variables) {
    Object starterUserId = variables.get("starterUserId");
    if (starterUserId == null) {
      return null;
    }
    if (starterUserId instanceof Number number) {
      return number.longValue();
    }
    String normalizedStarterUserId = requireText(String.valueOf(starterUserId),
        "流程变量 starterUserId 不能为空");
    return Long.valueOf(normalizedStarterUserId);
  }

  /**
   * 合并审批动作变量。
   *
   * @param variables 原始流程变量
   * @param approved 是否审批通过
   * @param rejectReason 驳回原因
   * @return 合并后的流程变量
   */
  private Map<String, Object> mergeVariables(Map<String, Object> variables, boolean approved,
      String rejectReason) {
    Map<String, Object> mergedVariables = new LinkedHashMap<>(normalizeVariables(variables));
    mergedVariables.put("approved", approved);
    if (rejectReason != null) {
      mergedVariables.put("rejectReason", rejectReason);
    }
    return mergedVariables;
  }

  /**
   * 写入任务审批意见。
   *
   * @param taskId 工作流任务 ID
   * @param comment 审批意见
   */
  private void addCommentIfPresent(String taskId, String comment) {
    String normalizedComment = trimToNull(comment);
    if (normalizedComment != null) {
      taskService.addComment(taskId, null, normalizedComment);
    }
  }

  /**
   * 校验文本不能为空并去除首尾空白。
   *
   * @param value 原始文本
   * @param message 为空时的异常消息
   * @return 清理后的文本
   */
  private String requireText(String value, String message) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      throw WorkflowServiceValidation.businessFailure(message);
    }
    return normalized;
  }

  /**
   * 将空白文本转换为 {@code null}。
   *
   * @param value 原始文本
   * @return 清理后的文本或 {@code null}
   */
  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
