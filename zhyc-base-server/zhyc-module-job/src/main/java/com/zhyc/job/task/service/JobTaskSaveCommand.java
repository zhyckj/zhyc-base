/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.service;

/**
 * 作业任务保存命令。
 *
 * @param tenantId 租户业务编码
 * @param jobCode 作业任务编码
 * @param jobName 作业任务名称
 * @param cronExpression Cron 表达式
 * @param handlerName 任务处理器名称
 * @param jobDescription 作业任务说明
 * @param status 作业状态
 */
public record JobTaskSaveCommand(String tenantId, String jobCode, String jobName,
                                 String cronExpression, String handlerName,
                                 String jobDescription, String status) {
}
