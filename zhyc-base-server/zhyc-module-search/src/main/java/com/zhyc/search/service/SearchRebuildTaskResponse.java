/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.service;

import java.time.LocalDateTime;

/**
 * 全文检索索引重建任务响应。
 *
 * @param id 重建任务主键
 * @param tenantId 租户业务编码
 * @param indexCode 待重建索引编码
 * @param taskStatus 任务状态
 * @param triggerType 触发类型
 * @param startedAt 任务开始时间
 * @param finishedAt 任务完成时间
 * @param errorMessage 失败错误信息
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record SearchRebuildTaskResponse(Long id, String tenantId, String indexCode, String taskStatus,
                                        String triggerType, LocalDateTime startedAt, LocalDateTime finishedAt,
                                        String errorMessage, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
