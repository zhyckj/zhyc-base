/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam.service;

import java.io.Serializable;

/**
 * 租户参数响应对象。
 */
public class SysTenantParamResponse implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 数据库主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 参数键。 */
    private final String paramKey;
    /** 参数值。 */
    private final String paramValue;
    /** 参数值类型。 */
    private final String valueType;
    /** 是否显示给租户管理员。 */
    private final boolean visible;

    /**
     * 创建租户参数响应对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param paramKey 参数键
     * @param paramValue 参数值
     * @param valueType 参数值类型
     * @param visible 是否显示给租户管理员
     */
    public SysTenantParamResponse(Long id, String tenantId, String paramKey, String paramValue,
                                  String valueType, boolean visible) {
        this.id = id;
        this.tenantId = tenantId;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.valueType = valueType;
        this.visible = visible;
    }

    /**
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 返回参数键。
     *
     * @return 参数键
     */
    public String getParamKey() {
        return paramKey;
    }

    /**
     * 返回参数值。
     *
     * @return 参数值
     */
    public String getParamValue() {
        return paramValue;
    }

    /**
     * 返回参数值类型。
     *
     * @return 参数值类型
     */
    public String getValueType() {
        return valueType;
    }

    /**
     * 返回是否显示给租户管理员。
     *
     * @return 是否显示给租户管理员
     */
    public boolean isVisible() {
        return visible;
    }
}
