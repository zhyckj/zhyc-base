/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

/**
 * 可视化报表保存命令。
 *
 * @param tenantId 租户业务编码
 * @param reportCode 报表编码，租户内唯一
 * @param reportName 报表名称
 * @param datasetCode 数据集编码
 * @param chartType 图表类型
 * @param configJson 图表配置 JSON
 * @param status 报表状态
 */
public record VisualReportSaveCommand(String tenantId, String reportCode, String reportName,
                                      String datasetCode, String chartType, String configJson,
                                      String status) {
}
