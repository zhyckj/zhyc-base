/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

/**
 * 可视化数据集保存命令。
 *
 * @param tenantId 租户业务编码
 * @param datasetCode 数据集编码，租户内唯一
 * @param datasetName 数据集名称
 * @param datasourceCode 数据源编码
 * @param sqlText 查询 SQL
 * @param status 数据集状态
 */
public record VisualDatasetSaveCommand(String tenantId, String datasetCode, String datasetName,
                                       String datasourceCode, String sqlText, String status) {
}
