/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.domain;

/**
 * 系统访问限制生效动作枚举。
 *
 * <p>动作编码持久化到 sys_access_restriction.effect 字段，
 * 用于表达命中访问限制规则后允许或拒绝访问。</p>
 */
public enum SysAccessRestrictionEffect {

    /** 允许访问；用于白名单策略或未命中拒绝规则时的通过语义。 */
    ALLOW("allow", "允许"),

    /** 拒绝访问；用于黑名单策略，命中后应阻断访问。 */
    DENY("deny", "拒绝");

    /** 持久化动作编码；对应访问限制表 effect 字段。 */
    private final String code;

    /** 动作中文说明；用于后台展示和审计说明。 */
    private final String description;

    /**
     * 创建系统访问限制生效动作枚举。
     *
     * @param code 持久化动作编码
     * @param description 动作中文说明
     */
    SysAccessRestrictionEffect(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化动作编码。
     *
     * @return 访问限制表 effect 字段使用的动作编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取动作中文说明。
     *
     * @return 动作中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析访问限制生效动作。
     *
     * <p>只允许允许或拒绝两类动作，避免未定义动作影响访问判定结果。</p>
     *
     * @param code 持久化动作编码
     * @return 匹配的访问限制生效动作
     */
    public static SysAccessRestrictionEffect fromCode(String code) {
        for (SysAccessRestrictionEffect effect : values()) {
            if (effect.code.equals(code)) {
                return effect;
            }
        }
        throw new IllegalArgumentException("生效动作不支持: " + code);
    }
}
