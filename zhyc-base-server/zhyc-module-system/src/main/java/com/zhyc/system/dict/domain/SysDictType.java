/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.domain;

import java.time.LocalDateTime;

/**
 * 系统字典类型领域模型。
 *
 * <p>字典类型按 {@code tenantId} 进行租户隔离，定义一组字典项的业务含义。</p>
 */
public class SysDictType {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 字典编码，租户内唯一。 */
    private String dictCode;
    /** 字典名称。 */
    private String dictName;
    /** 是否系统内置字典。 */
    private boolean systemFlag;
    /** 字典状态，例如 enabled、disabled。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空字典类型对象。
     */
    public SysDictType() {
    }

    /**
     * 创建完整字典类型对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param dictCode 字典编码
     * @param dictName 字典名称
     * @param systemFlag 是否系统内置字典
     * @param status 字典状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysDictType(Long id, String tenantId, String dictCode, String dictName, boolean systemFlag,
                       String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.systemFlag = systemFlag;
        this.status = status;
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
     * 返回字典编码。
     *
     * @return 字典编码
     */
    public String getDictCode() {
        return dictCode;
    }

    /**
     * 设置字典编码。
     *
     * @param dictCode 字典编码
     */
    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
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
     * 设置字典名称。
     *
     * @param dictName 字典名称
     */
    public void setDictName(String dictName) {
        this.dictName = dictName;
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
     * 设置是否系统内置字典。
     *
     * @param systemFlag 是否系统内置字典
     */
    public void setSystemFlag(boolean systemFlag) {
        this.systemFlag = systemFlag;
    }

    /**
     * 返回字典状态。
     *
     * @return 字典状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置字典状态。
     *
     * @param status 字典状态
     */
    public void setStatus(String status) {
        this.status = status;
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
