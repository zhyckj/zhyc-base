/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.service;

import java.time.LocalDateTime;

/**
 * 全文检索查询日志响应。
 *
 * @param id 查询日志主键
 * @param tenantId 租户业务编码
 * @param indexCode 查询索引编码
 * @param keyword 查询关键词
 * @param resultCount 返回结果数量
 * @param costMs 查询耗时毫秒
 * @param queryStatus 查询状态
 * @param createdAt 创建时间
 */
public record SearchQueryLogResponse(Long id, String tenantId, String indexCode, String keyword,
                                     Integer resultCount, Long costMs, String queryStatus,
                                     LocalDateTime createdAt) {
}
