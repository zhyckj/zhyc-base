/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.db;

/**
 * 数据库方言接口。
 *
 * <p>用于封装不同数据库在标识符引用和分页语法上的差异。</p>
 */
public interface DbDialect {

    /**
     * 返回方言名称。
     *
     * @return 数据库方言名称，例如 mysql、postgresql
     */
    String getName();

    /**
     * 引用数据库标识符。
     *
     * @param identifier 表名、字段名等数据库标识符
     * @return 按当前数据库规则引用后的标识符
     */
    String quoteIdentifier(String identifier);

    /**
     * 为 SQL 应用分页语句。
     *
     * @param sql 原始 SQL
     * @param offset 起始偏移量，从 0 开始
     * @param pageSize 最大返回行数
     * @return 添加分页限制后的 SQL
     */
    String applyPagination(String sql, long offset, long pageSize);
}
