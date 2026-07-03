/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { mobileRequest } from './request';

/**
 * 移动端待办任务。
 */
export interface MobileWorkflowTask {
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
 * 移动端审批命令。
 */
export interface MobileWorkflowHandleCommand {
  /** 审批意见。 */
  comment: string;
  /** 流程变量。 */
  variables?: Record<string, unknown>;
}

/**
 * 移动端撤回命令。
 */
export interface MobileWorkflowRevokeCommand {
  /** 撤回原因。 */
  reason: string;
}

/**
 * 移动端审批记录。
 */
export interface MobileWorkflowApprovalRecord {
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
 * 移动端任务详情。
 */
export interface MobileWorkflowTaskDetail extends MobileWorkflowTask {
  /** 流程定义 key。 */
  processKey: string;
  /** 审批记录列表。 */
  approvalRecords: MobileWorkflowApprovalRecord[];
}

/**
 * 移动端我发起的流程。
 */
export interface MobileWorkflowStartedProcess {
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
 * 移动端抄送任务。
 */
export interface MobileWorkflowCcTask {
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
 * 查询移动端待办。
 */
export function listMobileTodoTasks(): Promise<MobileWorkflowTask[]> {
  return mobileRequest<MobileWorkflowTask[]>('/workflow/tasks');
}

/**
 * 查询移动端已办任务。
 */
export function listMobileDoneTasks(): Promise<MobileWorkflowTask[]> {
  return mobileRequest<MobileWorkflowTask[]>('/workflow/tasks/done');
}

/**
 * 查询移动端我发起的流程。
 */
export function listMobileStartedProcesses(): Promise<MobileWorkflowStartedProcess[]> {
  return mobileRequest<MobileWorkflowStartedProcess[]>('/workflow/tasks/started');
}

/**
 * 查询移动端抄送我的任务。
 */
export function listMobileCcTasks(): Promise<MobileWorkflowCcTask[]> {
  return mobileRequest<MobileWorkflowCcTask[]>('/workflow/tasks/cc');
}

/**
 * 查询移动端任务详情。
 */
export function getMobileTaskDetail(
  taskId: string,
): Promise<MobileWorkflowTaskDetail> {
  return mobileRequest<MobileWorkflowTaskDetail>(`/workflow/tasks/${taskId}`);
}

/**
 * 移动端审批通过。
 *
 * @param taskId 工作流任务 ID
 * @param command 审批命令
 */
export function approveMobileTask(
  taskId: string,
  command: MobileWorkflowHandleCommand,
): Promise<void> {
  return mobileRequest<void, MobileWorkflowHandleCommand>(`/workflow/tasks/${taskId}/approve`, {
    method: 'POST',
    data: command,
  });
}

/**
 * 移动端驳回。
 *
 * @param taskId 工作流任务 ID
 * @param command 驳回命令
 */
export function rejectMobileTask(
  taskId: string,
  command: MobileWorkflowHandleCommand,
): Promise<void> {
  return mobileRequest<void, MobileWorkflowHandleCommand>(`/workflow/tasks/${taskId}/reject`, {
    method: 'POST',
    data: command,
  });
}

/**
 * 审批通过任务的跨端统一别名，便于低代码模板复用。
 *
 * @param taskId 工作流任务 ID
 * @param command 审批命令
 */
export function approveTask(
  taskId: string,
  command: MobileWorkflowHandleCommand,
): Promise<void> {
  return approveMobileTask(taskId, command);
}

/**
 * 驳回任务的跨端统一别名，便于低代码模板复用。
 *
 * @param taskId 工作流任务 ID
 * @param command 驳回命令
 */
export function rejectTask(
  taskId: string,
  command: MobileWorkflowHandleCommand,
): Promise<void> {
  return rejectMobileTask(taskId, command);
}

/**
 * 移动端撤回流程任务。
 *
 * @param processInstanceId 流程实例 ID
 * @param command 撤回命令
 */
export function revokeMobileTask(
  processInstanceId: string,
  command: MobileWorkflowRevokeCommand,
): Promise<void> {
  return mobileRequest<void, MobileWorkflowRevokeCommand>(`/workflow/tasks/process-instances/${processInstanceId}/revoke`, {
    method: 'POST',
    data: command,
  });
}
