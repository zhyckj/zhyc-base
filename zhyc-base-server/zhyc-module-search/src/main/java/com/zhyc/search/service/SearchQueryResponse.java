/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.service;

import java.util.List;

/**
 * 全文检索查询响应。
 *
 * @param indexCode 查询索引编码
 * @param keyword 查询关键词
 * @param total 命中数量
 * @param items 命中文本记录
 */
public record SearchQueryResponse(String indexCode, String keyword, int total, List<String> items) {
}
