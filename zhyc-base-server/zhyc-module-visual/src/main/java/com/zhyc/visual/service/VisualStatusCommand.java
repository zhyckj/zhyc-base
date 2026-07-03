/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

/**
 * 可视化资源状态变更命令。
 *
 * @param status 目标状态
 */
public record VisualStatusCommand(String status) {
}
