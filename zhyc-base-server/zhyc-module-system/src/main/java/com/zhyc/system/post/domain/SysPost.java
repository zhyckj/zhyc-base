/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.domain;

import java.time.LocalDateTime;

/**
 * 系统岗位基础模型。
 *
 * <p>岗位归属于租户和组织机构，用于用户任职、权限扩展和组织维度筛选。</p>
 */
public class SysPost {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 所属组织主键。 */
    private Long orgId;
    /** 岗位编码，租户内唯一。 */
    private String postCode;
    /** 岗位名称。 */
    private String postName;
    /** 排序号，数值越小越靠前。 */
    private Integer sortOrder;
    /** 岗位状态，例如 enabled、disabled。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空系统岗位对象。
     */
    public SysPost() {
    }

    /**
     * 创建完整系统岗位对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param orgId 所属组织主键
     * @param postCode 岗位编码
     * @param postName 岗位名称
     * @param sortOrder 排序号
     * @param status 岗位状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysPost(Long id, String tenantId, Long orgId, String postCode, String postName, Integer sortOrder,
                   String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.orgId = orgId;
        this.postCode = postCode;
        this.postName = postName;
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
     * 返回所属组织主键。
     *
     * @return 所属组织主键
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * 设置所属组织主键。
     *
     * @param orgId 所属组织主键
     */
    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    /**
     * 返回岗位编码。
     *
     * @return 岗位编码
     */
    public String getPostCode() {
        return postCode;
    }

    /**
     * 设置岗位编码。
     *
     * @param postCode 岗位编码
     */
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    /**
     * 返回岗位名称。
     *
     * @return 岗位名称
     */
    public String getPostName() {
        return postName;
    }

    /**
     * 设置岗位名称。
     *
     * @param postName 岗位名称
     */
    public void setPostName(String postName) {
        this.postName = postName;
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
     * 返回岗位状态。
     *
     * @return 岗位状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置岗位状态。
     *
     * @param status 岗位状态
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
