/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param.domain;

import java.time.LocalDateTime;

/**
 * 系统参数领域模型。
 *
 * <p>系统参数按 {@code tenantId} 进行租户隔离，用于保存租户内基础配置项。</p>
 */
public class SysParam {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 参数键，租户内唯一。 */
    private String paramKey;
    /** 参数值。 */
    private String paramValue;
    /** 参数值类型，例如 string、number、boolean、json。 */
    private String valueType;
    /** 是否系统内置参数。 */
    private boolean systemFlag;
    /** 是否允许后台编辑。 */
    private boolean editable;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空系统参数对象。
     */
    public SysParam() {
    }

    /**
     * 创建完整系统参数对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param paramKey 参数键
     * @param paramValue 参数值
     * @param valueType 参数值类型
     * @param systemFlag 是否系统内置参数
     * @param editable 是否允许后台编辑
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysParam(Long id, String tenantId, String paramKey, String paramValue, String valueType,
                    boolean systemFlag, boolean editable, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.valueType = valueType;
        this.systemFlag = systemFlag;
        this.editable = editable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
     * 设置数据库主键。
     *
     * @param id 数据库主键
     */
    public void setId(Long id) {
        this.id = id;
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
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
     * 设置参数键。
     *
     * @param paramKey 参数键
     */
    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
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
     * 设置参数值。
     *
     * @param paramValue 参数值
     */
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
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
     * 设置参数值类型。
     *
     * @param valueType 参数值类型
     */
    public void setValueType(String valueType) {
        this.valueType = valueType;
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
     * 设置是否系统内置参数。
     *
     * @param systemFlag 是否系统内置参数
     */
    public void setSystemFlag(boolean systemFlag) {
        this.systemFlag = systemFlag;
    }

    /**
     * 返回是否允许后台编辑。
     *
     * @return 允许编辑返回 {@code true}
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * 设置是否允许后台编辑。
     *
     * @param editable 是否允许后台编辑
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 返回更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
