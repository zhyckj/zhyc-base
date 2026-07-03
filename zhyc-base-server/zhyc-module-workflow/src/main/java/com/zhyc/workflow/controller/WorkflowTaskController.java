/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.workflow.service.WorkflowProcessRevokeCommand;
import com.zhyc.workflow.service.WorkflowProcessMonitorItem;
import com.zhyc.workflow.service.WorkflowTaskHandleCommand;
import com.zhyc.workflow.service.WorkflowTaskService;
import com.zhyc.workflow.service.WorkflowCcTaskItem;
import com.zhyc.workflow.service.WorkflowTaskDetailResponse;
import com.zhyc.workflow.service.WorkflowTaskTodoItem;
import com.zhyc.workflow.service.WorkflowStartedProcessItem;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作流任务后台接口。
 */
@RestController
@RequestMapping("/workflow/tasks")
public class WorkflowTaskController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
  /** 当前操作用户 ID 请求头。 */
  public static final String HEADER_USER_ID = "X-ZHYC-User-Id";

  /** 工作流任务服务。 */
  private final WorkflowTaskService workflowTaskService;

  /**
   * 创建工作流任务后台接口。
   *
   * @param workflowTaskService 工作流任务服务
   */
  public WorkflowTaskController(WorkflowTaskService workflowTaskService) {
    this.workflowTaskService = Objects.requireNonNull(workflowTaskService,
        "工作流任务服务不能为空");
  }

  /**
   * 查询当前用户待办任务。
   *
   * @param tenantId 租户业务编码
   * @param assigneeUserId 当前用户 ID
   * @return 待办任务列表
   */
  @RequiresPermissions("workflow:task:todo")
  @GetMapping("")
  public ApiResult<List<WorkflowTaskTodoItem>> todo(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long assigneeUserId) {
    return ApiResult.ok(workflowTaskService.listTodoTasks(tenantId, assigneeUserId));
  }

  /**
   * 查询当前用户已办任务。
   *
   * @param tenantId 租户业务编码
   * @param operatorUserId 当前用户 ID
   * @return 已办任务列表
   */
  @RequiresPermissions("workflow:task:done")
  @GetMapping("/done")
  public ApiResult<List<WorkflowTaskTodoItem>> done(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long operatorUserId) {
    return ApiResult.ok(workflowTaskService.listDoneTasks(tenantId, operatorUserId));
  }

  /**
   * 查询当前用户发起的流程实例。
   *
   * @param tenantId 租户业务编码
   * @param starterUserId 当前用户 ID
   * @return 发起流程列表
   */
  @RequiresPermissions("workflow:task:started")
  @GetMapping("/started")
  public ApiResult<List<WorkflowStartedProcessItem>> started(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long starterUserId) {
    return ApiResult.ok(workflowTaskService.listStartedProcesses(tenantId, starterUserId));
  }

  /**
   * 查询当前用户收到的抄送任务。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 当前用户 ID
   * @return 抄送任务列表
   */
  @RequiresPermissions("workflow:task:cc")
  @GetMapping("/cc")
  public ApiResult<List<WorkflowCcTaskItem>> cc(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long receiverId) {
    return ApiResult.ok(workflowTaskService.listCcTasks(tenantId, receiverId));
  }

  /**
   * 查询租户下流程实例监控列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程监控列表
   */
  @RequiresPermissions("workflow:task:monitor")
  @GetMapping("/monitor")
  public ApiResult<List<WorkflowProcessMonitorItem>> monitor(
      @RequestHeader(HEADER_TENANT_ID) String tenantId) {
    return ApiResult.ok(workflowTaskService.listMonitoredProcesses(tenantId));
  }

  /**
   * 查询当前用户可访问的任务详情。
   *
   * @param taskId 任务 ID
   * @param tenantId 租户业务编码
   * @param assigneeUserId 当前用户 ID
   * @return 任务详情
   */
  @RequiresPermissions("workflow:task:detail")
  @GetMapping("/{taskId}")
  public ApiResult<WorkflowTaskDetailResponse> detail(@PathVariable("taskId") String taskId,
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long assigneeUserId) {
    return ApiResult.ok(workflowTaskService.getTaskDetail(tenantId, taskId, assigneeUserId));
  }

  /**
   * 审批通过任务。
   *
   * @param taskId 任务 ID
   * @param command 任务处理命令
   * @param tenantId 租户业务编码
   * @param operatorUserId 当前用户 ID
   * @return 空响应
   */
  @RequiresPermissions("workflow:task:approve")
  @PostMapping("/{taskId}/approve")
  public ApiResult<Void> approve(@PathVariable("taskId") String taskId,
      @RequestBody WorkflowTaskHandleCommand command,
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long operatorUserId) {
    workflowTaskService.approve(mergeCommand(taskId, command, tenantId, operatorUserId));
    return ApiResult.ok(null);
  }

  /**
   * 驳回任务。
   *
   * @param taskId 任务 ID
   * @param command 任务处理命令
   * @param tenantId 租户业务编码
   * @param operatorUserId 当前用户 ID
   * @return 空响应
   */
  @RequiresPermissions("workflow:task:reject")
  @PostMapping("/{taskId}/reject")
  public ApiResult<Void> reject(@PathVariable("taskId") String taskId,
      @RequestBody WorkflowTaskHandleCommand command,
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long operatorUserId) {
    workflowTaskService.reject(mergeCommand(taskId, command, tenantId, operatorUserId));
    return ApiResult.ok(null);
  }

  /**
   * 撤回流程实例。
   *
   * @param processInstanceId 流程实例 ID
   * @param command 流程撤回命令
   * @param tenantId 租户业务编码
   * @param operatorUserId 当前用户 ID
   * @return 空响应
   */
  @RequiresPermissions("workflow:task:revoke")
  @PostMapping("/process-instances/{processInstanceId}/revoke")
  public ApiResult<Void> revoke(@PathVariable("processInstanceId") String processInstanceId,
      @RequestBody WorkflowProcessRevokeCommand command,
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long operatorUserId) {
    WorkflowProcessRevokeCommand safeCommand = command == null
        ? new WorkflowProcessRevokeCommand(null, null, null, null)
        : command;
    workflowTaskService.revoke(new WorkflowProcessRevokeCommand(tenantId, processInstanceId,
        operatorUserId, safeCommand.getReason()));
    return ApiResult.ok(null);
  }

  /**
   * 合并路径、请求头和请求体中的任务处理参数。
   *
   * @param taskId 路径任务 ID
   * @param command 请求体命令
   * @param tenantId 租户业务编码
   * @param operatorUserId 当前用户 ID
   * @return 合并后的任务处理命令
   */
  private WorkflowTaskHandleCommand mergeCommand(String taskId, WorkflowTaskHandleCommand command,
      String tenantId, Long operatorUserId) {
    WorkflowTaskHandleCommand safeCommand = command == null
        ? new WorkflowTaskHandleCommand(null, null, null, null, null)
        : command;
    return new WorkflowTaskHandleCommand(tenantId, taskId, operatorUserId,
        safeCommand.getComment(), safeCommand.getVariables());
  }
}
