/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.service;

/**
 * 作业任务执行上下文。
 *
 * @param tenantId 租户业务编码
 * @param jobId 作业任务主键
 * @param jobCode 作业任务编码
 * @param jobName 作业任务名称
 * @param handlerName 任务处理器名称
 * @param triggerType 触发类型
 * @param operatorId 操作人用户主键，系统调度触发时可为空
 */
public record JobTaskExecutionContext(String tenantId, Long jobId, String jobCode,
                                      String jobName, String handlerName,
                                      String triggerType, Long operatorId) {
}
