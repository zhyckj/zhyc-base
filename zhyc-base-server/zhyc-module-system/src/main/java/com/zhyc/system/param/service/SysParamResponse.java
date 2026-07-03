/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param.service;

import java.io.Serializable;

/**
 * 系统参数响应对象。
 */
public class SysParamResponse implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 系统参数主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 参数键。 */
    private final String paramKey;
    /** 参数值。 */
    private final String paramValue;
    /** 参数值类型。 */
    private final String valueType;
    /** 是否系统内置参数。 */
    private final boolean systemFlag;
    /** 是否允许后台编辑。 */
    private final boolean editable;

    /**
     * 创建系统参数响应对象。
     *
     * @param id 系统参数主键
     * @param tenantId 租户业务编码
     * @param paramKey 参数键
     * @param paramValue 参数值
     * @param valueType 参数值类型
     * @param systemFlag 是否系统内置参数
     * @param editable 是否允许后台编辑
     */
    public SysParamResponse(Long id, String tenantId, String paramKey, String paramValue, String valueType,
                            boolean systemFlag, boolean editable) {
        this.id = id;
        this.tenantId = tenantId;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.valueType = valueType;
        this.systemFlag = systemFlag;
        this.editable = editable;
    }

    /**
     * 返回系统参数主键。
     *
     * @return 系统参数主键
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
     * 返回是否系统内置参数。
     *
     * @return 系统内置参数返回 {@code true}
     */
    public boolean isSystemFlag() {
        return systemFlag;
    }

    /**
     * 返回是否允许后台编辑。
     *
     * @return 允许编辑返回 {@code true}
     */
    public boolean isEditable() {
        return editable;
    }
}
