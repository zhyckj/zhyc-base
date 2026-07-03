/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.domain;

/**
 * 系统访问限制类型枚举。
 *
 * <p>类型编码持久化到 sys_access_restriction.restriction_type 字段，
 * 用于区分 IP、账号和设备维度的访问控制策略。</p>
 */
public enum SysAccessRestrictionType {

    /** IP 访问限制，用于匹配单个 IPv4 地址或 IPv4 CIDR 网段。 */
    IP("ip", "IP 地址"),

    /** 账号访问限制，用于匹配登录账号或账号业务标识。 */
    ACCOUNT("account", "账号"),

    /** 设备访问限制，用于匹配设备指纹或设备业务标识。 */
    DEVICE("device", "设备");

    /** 持久化类型编码；对应访问限制表 restriction_type 字段。 */
    private final String code;

    /** 类型中文说明；用于后台展示和审计说明。 */
    private final String description;

    /**
     * 创建系统访问限制类型枚举。
     *
     * @param code 持久化类型编码
     * @param description 类型中文说明
     */
    SysAccessRestrictionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化类型编码。
     *
     * @return 访问限制表 restriction_type 字段使用的类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型中文说明。
     *
     * @return 类型中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析访问限制类型。
     *
     * <p>只允许平台安全策略支持的访问维度，避免未定义策略类型绕过统一判定逻辑。</p>
     *
     * @param code 持久化类型编码
     * @return 匹配的访问限制类型
     */
    public static SysAccessRestrictionType fromCode(String code) {
        for (SysAccessRestrictionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("限制类型不支持: " + code);
    }
}
