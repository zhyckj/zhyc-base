/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.service;

/**
 * 系统访问限制判定结果。
 *
 * <p>用于登录、网关或后台安全测试判断指定访问标识是否允许进入系统，不包含敏感凭证。</p>
 */
public class SysAccessRestrictionEvaluationResult {

    /** 是否允许访问。 */
    private final boolean allowed;
    /** 命中的生效动作；未命中规则时为 allow。 */
    private final String effect;
    /** 命中的规则值；未命中规则时为空。 */
    private final String matchedRuleValue;

    /**
     * 创建系统访问限制判定结果。
     *
     * @param allowed 是否允许访问
     * @param effect 命中的生效动作
     * @param matchedRuleValue 命中的规则值
     */
    public SysAccessRestrictionEvaluationResult(boolean allowed, String effect, String matchedRuleValue) {
        this.allowed = allowed;
        this.effect = effect;
        this.matchedRuleValue = matchedRuleValue;
    }

    /**
     * 返回是否允许访问。
     *
     * @return 允许访问时返回 {@code true}
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * 返回命中的生效动作。
     *
     * @return 生效动作
     */
    public String getEffect() {
        return effect;
    }

    /**
     * 返回命中的规则值。
     *
     * @return 命中的规则值，未命中时为 {@code null}
     */
    public String getMatchedRuleValue() {
        return matchedRuleValue;
    }
}
