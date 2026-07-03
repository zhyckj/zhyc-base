/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.service;

/**
 * 全文检索索引重建任务创建命令。
 *
 * @param tenantId 租户业务编码
 * @param indexCode 待重建索引编码
 * @param triggerType 触发类型
 */
public record SearchRebuildTaskCommand(String tenantId, String indexCode, String triggerType) {
}
