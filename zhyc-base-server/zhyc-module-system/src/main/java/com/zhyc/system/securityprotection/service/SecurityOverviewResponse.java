/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.service;

/**
 * 安全防护总览响应。
 */
public class SecurityOverviewResponse {

    /** 统计日期，格式 yyyyMMdd。 */
    private final String statDate;
    /** 今日请求来源 IP 数。 */
    private final long todaySourceCount;
    /** 今日单 IP 最高请求次数。 */
    private final long maxIpRequestCount;
    /** 今日违规 IP 数。 */
    private final long violationIpCount;
    /** 当前封禁 IP 数。 */
    private final long blockedIpCount;

    public SecurityOverviewResponse(String statDate, long todaySourceCount, long maxIpRequestCount,
                                    long violationIpCount, long blockedIpCount) {
        this.statDate = statDate;
        this.todaySourceCount = todaySourceCount;
        this.maxIpRequestCount = maxIpRequestCount;
        this.violationIpCount = violationIpCount;
        this.blockedIpCount = blockedIpCount;
    }

    public String getStatDate() {
        return statDate;
    }

    public long getTodaySourceCount() {
        return todaySourceCount;
    }

    public long getMaxIpRequestCount() {
        return maxIpRequestCount;
    }

    public long getViolationIpCount() {
        return violationIpCount;
    }

    public long getBlockedIpCount() {
        return blockedIpCount;
    }
}
