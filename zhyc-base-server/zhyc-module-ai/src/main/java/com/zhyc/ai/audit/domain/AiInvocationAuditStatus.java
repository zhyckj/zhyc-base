/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.domain;

import java.util.Arrays;

/**
 * AI 调用审计状态枚举。
 */
public enum AiInvocationAuditStatus {

    SUCCESS("success"),
    FAILED("failed"),
    BLOCKED("blocked");

    private final String code;

    AiInvocationAuditStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AiInvocationAuditStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("AI 调用状态只支持 success、failed 或 blocked"));
    }
}
