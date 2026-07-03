/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.service;

import java.io.Serializable;

/**
 * 系统字典类型响应对象。
 */
public class SysDictTypeResponse implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 字典类型主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 字典编码。 */
    private final String dictCode;
    /** 字典名称。 */
    private final String dictName;
    /** 是否系统内置字典。 */
    private final boolean systemFlag;
    /** 字典状态。 */
    private final String status;

    /**
     * 创建系统字典类型响应对象。
     *
     * @param id 字典类型主键
     * @param tenantId 租户业务编码
     * @param dictCode 字典编码
     * @param dictName 字典名称
     * @param systemFlag 是否系统内置字典
     * @param status 字典状态
     */
    public SysDictTypeResponse(Long id, String tenantId, String dictCode, String dictName,
                               boolean systemFlag, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.systemFlag = systemFlag;
        this.status = status;
    }

    /**
     * 返回字典类型主键。
     *
     * @return 字典类型主键
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
     * 返回字典编码。
     *
     * @return 字典编码
     */
    public String getDictCode() {
        return dictCode;
    }

    /**
     * 返回字典名称。
     *
     * @return 字典名称
     */
    public String getDictName() {
        return dictName;
    }

    /**
     * 返回是否系统内置字典。
     *
     * @return 系统内置字典返回 {@code true}
     */
    public boolean isSystemFlag() {
        return systemFlag;
    }

    /**
     * 返回字典状态。
     *
     * @return 字典状态
     */
    public String getStatus() {
        return status;
    }
}
