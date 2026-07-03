/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.repository;

/**
 * 全文检索运行时数据库查询。
 *
 * @param tenantId 租户业务编码
 * @param sourceTable 数据来源表名
 * @param selectExpression 命中文本拼接表达式
 * @param whereExpression 检索字段匹配表达式
 * @param keyword 查询关键词
 * @param limit 返回记录上限
 */
public record SearchRuntimeQuery(String tenantId, String sourceTable, String selectExpression,
                                 String whereExpression, String keyword, int limit) {
}
