/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.service;

import java.time.LocalDateTime;

/**
 * 作业执行日志响应。
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param jobId 作业任务主键
 * @param triggerType 触发类型
 * @param startAt 开始时间
 * @param endAt 结束时间
 * @param result 执行结果
 * @param errorMessage 错误信息
 * @param operatorId 操作人用户主键
 */
public record JobTaskLogResponse(Long id, String tenantId, Long jobId, String triggerType,
                                 LocalDateTime startAt, LocalDateTime endAt, String result,
                                 String errorMessage, Long operatorId) {
}
