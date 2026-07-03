/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.service;

/**
 * 全文检索索引配置保存命令。
 *
 * @param tenantId 租户业务编码
 * @param indexCode 索引编码
 * @param indexName 索引名称
 * @param sourceTable 数据来源表名
 * @param searchFields 可检索字段列表
 * @param filterFields 可过滤字段列表
 * @param status 索引状态
 * @param remark 配置备注
 */
public record SearchIndexConfigSaveCommand(String tenantId, String indexCode, String indexName,
                                           String sourceTable, String searchFields, String filterFields,
                                           String status, String remark) {
}
