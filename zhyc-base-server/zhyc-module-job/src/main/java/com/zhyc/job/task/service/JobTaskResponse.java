/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.service;

import java.time.LocalDateTime;

/**
 * 作业任务响应。
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param jobCode 作业任务编码
 * @param jobName 作业任务名称
 * @param cronExpression Cron 表达式
 * @param handlerName 任务处理器名称
 * @param jobDescription 作业任务说明
 * @param status 作业状态
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record JobTaskResponse(Long id, String tenantId, String jobCode, String jobName,
                              String cronExpression, String handlerName, String jobDescription,
                              String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
