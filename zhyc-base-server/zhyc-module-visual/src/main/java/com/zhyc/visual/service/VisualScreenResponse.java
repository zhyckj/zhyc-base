/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

import java.time.LocalDateTime;

/**
 * 可视化大屏响应。
 *
 * @param id 主键
 * @param tenantId 租户业务编码
 * @param screenCode 大屏编码
 * @param screenName 大屏名称
 * @param layoutJson 布局 JSON
 * @param status 大屏状态
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record VisualScreenResponse(Long id, String tenantId, String screenCode, String screenName,
                                   String layoutJson, String status, LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
}
