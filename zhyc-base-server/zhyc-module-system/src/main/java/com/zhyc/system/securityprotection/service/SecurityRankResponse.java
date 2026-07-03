/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.service;

/**
 * 安全排行响应。
 */
public class SecurityRankResponse {

    /** 排行名称，例如 IP 或接口路径。 */
    private final String name;
    /** 请求次数。 */
    private final long requestCount;

    public SecurityRankResponse(String name, long requestCount) {
        this.name = name;
        this.requestCount = requestCount;
    }

    public String getName() {
        return name;
    }

    public long getRequestCount() {
        return requestCount;
    }
}
