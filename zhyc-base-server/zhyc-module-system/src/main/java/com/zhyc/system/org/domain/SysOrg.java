/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.domain;

import java.time.LocalDateTime;

/**
 * 系统组织机构基础模型。
 *
 * <p>组织机构按 {@code tenantId} 做租户隔离，用于维护企业部门、分支机构和组织树。</p>
 */
public class SysOrg {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 父级组织主键，根组织可为空。 */
    private Long parentId;
    /** 祖级组织路径，例如 0,1,2。 */
    private String ancestors;
    /** 组织编码。 */
    private String orgCode;
    /** 组织名称。 */
    private String orgName;
    /** 负责人用户主键。 */
    private Long leaderUserId;
    /** 排序号，数值越小越靠前。 */
    private Integer sortOrder;
    /** 组织状态，例如 enabled、disabled。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空组织机构对象。
     */
    public SysOrg() {
    }

    /**
     * 创建完整组织机构对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param parentId 父级组织主键
     * @param ancestors 祖级组织路径
     * @param orgCode 组织编码
     * @param orgName 组织名称
     * @param leaderUserId 负责人用户主键
     * @param sortOrder 排序号
     * @param status 组织状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysOrg(Long id, String tenantId, Long parentId, String ancestors, String orgCode, String orgName,
                  Long leaderUserId, Integer sortOrder, String status, LocalDateTime createdAt,
                  LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.parentId = parentId;
        this.ancestors = ancestors;
        this.orgCode = orgCode;
        this.orgName = orgName;
        this.leaderUserId = leaderUserId;
        this.sortOrder = sortOrder;
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
     * 返回父级组织主键。
     *
     * @return 父级组织主键
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 设置父级组织主键。
     *
     * @param parentId 父级组织主键
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * 返回祖级组织路径。
     *
     * @return 祖级组织路径
     */
    public String getAncestors() {
        return ancestors;
    }

    /**
     * 设置祖级组织路径。
     *
     * @param ancestors 祖级组织路径
     */
    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
    }

    /**
     * 返回组织编码。
     *
     * @return 组织编码
     */
    public String getOrgCode() {
        return orgCode;
    }

    /**
     * 设置组织编码。
     *
     * @param orgCode 组织编码
     */
    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    /**
     * 返回组织名称。
     *
     * @return 组织名称
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * 设置组织名称。
     *
     * @param orgName 组织名称
     */
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    /**
     * 返回负责人用户主键。
     *
     * @return 负责人用户主键
     */
    public Long getLeaderUserId() {
        return leaderUserId;
    }

    /**
     * 设置负责人用户主键。
     *
     * @param leaderUserId 负责人用户主键
     */
    public void setLeaderUserId(Long leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    /**
     * 返回排序号。
     *
     * @return 排序号
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置排序号。
     *
     * @param sortOrder 排序号
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 返回组织状态。
     *
     * @return 组织状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置组织状态。
     *
     * @param status 组织状态
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
