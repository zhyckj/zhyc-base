/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.service;

import java.util.List;

/**
 * 密码策略校验结果。
 */
public class PasswordPolicyValidationResult {

    /** 是否通过密码策略校验。 */
    private final boolean valid;
    /** 未满足的策略项编码。 */
    private final List<String> violationCodes;

    /**
     * 创建密码策略校验结果。
     *
     * @param valid 是否通过密码策略校验
     * @param violationCodes 未满足的策略项编码
     */
    public PasswordPolicyValidationResult(boolean valid, List<String> violationCodes) {
        this.valid = valid;
        this.violationCodes = List.copyOf(violationCodes);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getViolationCodes() {
        return violationCodes;
    }
}
