/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

/**
 * 可视化大屏保存命令。
 *
 * @param tenantId 租户业务编码
 * @param screenCode 大屏编码，租户内唯一
 * @param screenName 大屏名称
 * @param layoutJson 布局 JSON
 * @param status 大屏状态
 */
public record VisualScreenSaveCommand(String tenantId, String screenCode, String screenName,
                                      String layoutJson, String status) {
}
