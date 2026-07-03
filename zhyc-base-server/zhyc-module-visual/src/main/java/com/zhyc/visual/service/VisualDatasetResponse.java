/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

import java.time.LocalDateTime;

/**
 * 可视化数据集响应。
 *
 * @param id 主键
 * @param tenantId 租户业务编码
 * @param datasetCode 数据集编码
 * @param datasetName 数据集名称
 * @param datasourceCode 数据源编码
 * @param sqlText 查询 SQL
 * @param status 数据集状态
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record VisualDatasetResponse(Long id, String tenantId, String datasetCode, String datasetName,
                                    String datasourceCode, String sqlText, String status,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
}
