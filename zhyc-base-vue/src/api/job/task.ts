/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 在线作业任务。
 */
export interface JobTaskRecord {
  /** 作业任务主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 作业任务编码。 */
  jobCode: string;
  /** 作业任务名称。 */
  jobName: string;
  /** Cron 表达式。 */
  cronExpression: string;
  /** 任务处理器名称。 */
  handlerName: string;
  /** 作业任务说明。 */
  jobDescription?: string;
  /** 作业状态。 */
  status: string;
  /** 创建时间。 */
  createdAt?: string;
  /** 更新时间。 */
  updatedAt?: string;
}

/**
 * 作业执行日志。
 */
export interface JobTaskLogRecord {
  /** 作业执行日志主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 作业任务主键。 */
  jobId: number;
  /** 触发类型。 */
  triggerType: string;
  /** 开始时间。 */
  startAt: string;
  /** 结束时间。 */
  endAt?: string;
  /** 执行结果。 */
  result: string;
  /** 错误信息。 */
  errorMessage?: string;
  /** 操作人用户主键。 */
  operatorId?: number;
}

/**
 * 作业任务保存参数。
 */
export interface JobTaskSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 作业任务编码。 */
  jobCode: string;
  /** 作业任务名称。 */
  jobName: string;
  /** Cron 表达式。 */
  cronExpression: string;
  /** 任务处理器名称。 */
  handlerName: string;
  /** 作业任务说明。 */
  jobDescription?: string;
  /** 作业状态。 */
  status: string;
}

/**
 * 查询作业任务列表。
 *
 * @param status 作业状态
 * @returns 作业任务列表
 */
export function listJobTasks(status?: string): Promise<JobTaskRecord[]> {
  return request<JobTaskRecord[]>('/job/tasks', {
    query: {
      status,
    },
  });
}

/**
 * 保存作业任务。
 *
 * @param payload 作业任务保存参数
 */
export function saveJobTask(payload: JobTaskSavePayload): Promise<void> {
  return request<void, JobTaskSavePayload>('/job/tasks', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 变更作业任务状态。
 *
 * @param id 作业任务主键
 * @param status 作业状态
 */
export function changeJobTaskStatus(id: number, status: string): Promise<void> {
  return request<void, { status: string }>(`/job/tasks/${id}/status`, {
    method: 'POST',
    body: { status },
  });
}

/**
 * 手动触发作业任务。
 *
 * @param id 作业任务主键
 */
export function triggerJobTask(id: number): Promise<void> {
  return request<void>(`/job/tasks/${id}/trigger`, {
    method: 'POST',
  });
}

/**
 * 查询作业执行日志。
 *
 * @param id 作业任务主键
 * @returns 作业执行日志列表
 */
export function listJobTaskLogs(id: number): Promise<JobTaskLogRecord[]> {
  return request<JobTaskLogRecord[]>(`/job/tasks/${id}/logs`);
}
