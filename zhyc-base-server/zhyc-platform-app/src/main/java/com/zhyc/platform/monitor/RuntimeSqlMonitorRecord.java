/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

/**
 * SQL 执行效率监控记录。
 *
 * @param sourceCode 数据源编码
 * @param sqlDigest 归一化 SQL 摘要，不能包含明文参数
 * @param executeCount 执行次数
 * @param avgCostMs 平均执行耗时，单位毫秒
 * @param maxCostMs 最大执行耗时，单位毫秒
 * @param rowsExamined 扫描行数
 * @param rowsSent 返回行数
 * @param severity 慢 SQL 等级
 * @param suggestion 优化建议
 * @param lastSeen 最近一次采集时间
 */
public record RuntimeSqlMonitorRecord(String sourceCode, String sqlDigest, long executeCount, long avgCostMs,
    long maxCostMs, long rowsExamined, long rowsSent, String severity, String suggestion, String lastSeen) {
}
