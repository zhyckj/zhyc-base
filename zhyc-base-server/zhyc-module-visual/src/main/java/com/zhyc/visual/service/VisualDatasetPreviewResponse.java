/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

import java.util.List;
import java.util.Map;

/**
 * 可视化数据集预览响应。
 *
 * @param datasetCode 数据集编码
 * @param columns 可绑定字段列表
 * @param rows 预览数据行
 * @param executable 是否已执行真实数据源查询
 * @param message 预览说明
 */
public record VisualDatasetPreviewResponse(String datasetCode, List<String> columns,
                                           List<Map<String, Object>> rows, boolean executable,
                                           String message) {
}
