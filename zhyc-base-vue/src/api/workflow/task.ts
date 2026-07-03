/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 工作流待办任务。
 */
export interface WorkflowTaskTodoItem {
  /** 租户编码。 */
  tenantId: string;
  /** 任务主键。 */
  taskId: string;
  /** 流程实例主键。 */
  processInstanceId: string;
  /** 任务名称。 */
  taskName: string;
  /** 业务单号。 */
  businessKey: string;
  /** 当前处理人用户 ID。 */
  assigneeUserId: number;
  /** 任务状态。 */
  status: string;
  /** 创建时间。 */
  createdAt: string;
}

/**
 * 审批处理命令。
 */
export interface WorkflowTaskHandleCommand {
  /** 审批意见。 */
  comment: string;
  /** 工作流变量。 */
  variables?: Record<string, unknown>;
}

/**
 * 流程撤回命令。
 */
export interface WorkflowProcessRevokeCommand {
  /** 撤回原因。 */
  reason: string;
}

/**
 * 工作流审批记录。
 */
export interface WorkflowApprovalRecordItem {
  /** 任务 ID。 */
  taskId: string;
  /** 操作用户 ID。 */
  operatorUserId: number;
  /** 审批动作。 */
  action: string;
  /** 审批意见。 */
  approvalComment: string;
  /** 操作时间。 */
  operatedAt: string;
}

/**
 * 工作流任务详情。
 */
export interface WorkflowTaskDetailResponse extends WorkflowTaskTodoItem {
  /** 流程定义 key。 */
  processKey: string;
  /** 审批记录列表。 */
  approvalRecords: WorkflowApprovalRecordItem[];
}

/**
 * 当前用户发起的流程。
 */
export interface WorkflowStartedProcessItem {
  /** 租户编码。 */
  tenantId: string;
  /** 流程实例主键。 */
  processInstanceId: string;
  /** 流程定义 key。 */
  processKey: string;
  /** 业务单号。 */
  businessKey: string;
  /** 发起人用户 ID。 */
  starterUserId: number;
  /** 流程实例状态。 */
  status: string;
  /** 发起时间。 */
  startedAt: string;
}

/**
 * 当前用户收到的抄送任务。
 */
export interface WorkflowCcTaskItem {
  /** 租户编码。 */
  tenantId: string;
  /** 抄送记录 ID。 */
  ccRecordId: number;
  /** 流程实例主键。 */
  processInstanceId: string;
  /** 流程定义 key。 */
  processKey: string;
  /** 业务单号。 */
  businessKey: string;
  /** 抄送接收人用户 ID。 */
  receiverId: number;
  /** 阅读标识，0 未读，1 已读。 */
  readFlag: number;
  /** 抄送创建时间。 */
  createdAt: string;
}

/**
 * 流程监控列表项。
 */
export interface WorkflowProcessMonitorItem {
  /** 租户编码，用于后台展示当前隔离范围。 */
  tenantId: string;
  /** 流程实例主键，用于定位运行态流程。 */
  processInstanceId: string;
  /** 流程定义 key，用于识别业务流程类型。 */
  processKey: string;
  /** 业务单号，用于关联业务单据。 */
  businessKey: string;
  /** 发起人用户 ID，用于运维审计。 */
  starterUserId: number;
  /** 流程实例状态，用于区分运行、通过、驳回或撤回。 */
  status: string;
  /** 发起时间，用于按最近流程排序。 */
  startedAt: string;
  /** 结束时间，运行中流程为空。 */
  endedAt?: string;
}

/**
 * 查询当前用户待办任务。
 */
export function listTodoTasks(): Promise<WorkflowTaskTodoItem[]> {
  return request<WorkflowTaskTodoItem[]>('/workflow/tasks');
}

/**
 * 查询当前用户已办任务。
 */
export function listDoneTasks(): Promise<WorkflowTaskTodoItem[]> {
  return request<WorkflowTaskTodoItem[]>('/workflow/tasks/done');
}

/**
 * 查询当前用户发起的流程实例。
 */
export function listStartedProcesses(): Promise<WorkflowStartedProcessItem[]> {
  return request<WorkflowStartedProcessItem[]>('/workflow/tasks/started');
}

/**
 * 查询当前用户收到的抄送任务。
 */
export function listCcTasks(): Promise<WorkflowCcTaskItem[]> {
  return request<WorkflowCcTaskItem[]>('/workflow/tasks/cc');
}

/**
 * 查询租户下流程实例监控列表。
 */
export function listMonitoredProcesses(): Promise<WorkflowProcessMonitorItem[]> {
  return request<WorkflowProcessMonitorItem[]>('/workflow/tasks/monitor');
}

/**
 * 查询当前用户可访问的任务详情。
 */
export function getTaskDetail(taskId: string): Promise<WorkflowTaskDetailResponse> {
  return request<WorkflowTaskDetailResponse>(`/workflow/tasks/${taskId}`);
}

/**
 * 审批通过待办任务。
 */
export function approveTask(taskId: string, command: WorkflowTaskHandleCommand): Promise<void> {
  return request<void, WorkflowTaskHandleCommand>(`/workflow/tasks/${taskId}/approve`, {
    method: 'POST',
    body: command,
  });
}

/**
 * 驳回待办任务。
 */
export function rejectTask(taskId: string, command: WorkflowTaskHandleCommand): Promise<void> {
  return request<void, WorkflowTaskHandleCommand>(`/workflow/tasks/${taskId}/reject`, {
    method: 'POST',
    body: command,
  });
}

/**
 * 撤回流程实例。
 */
export function revokeProcessInstance(
  processInstanceId: string,
  command: WorkflowProcessRevokeCommand,
): Promise<void> {
  return request<void, WorkflowProcessRevokeCommand>(`/workflow/tasks/process-instances/${processInstanceId}/revoke`, {
    method: 'POST',
    body: command,
  });
}
