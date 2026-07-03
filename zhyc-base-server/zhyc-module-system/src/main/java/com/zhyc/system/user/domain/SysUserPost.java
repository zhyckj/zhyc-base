/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.domain;

import java.time.LocalDateTime;

/**
 * 系统用户岗位绑定模型。
 *
 * <p>用户岗位绑定按 {@code tenantId} 做租户隔离，用于描述用户在组织内承担的岗位。</p>
 */
public class SysUserPost {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 用户主键。 */
    private Long userId;
    /** 岗位主键。 */
    private Long postId;
    /** 岗位编码，仅查询展示时返回。 */
    private String postCode;
    /** 岗位名称，仅查询展示时返回。 */
    private String postName;
    /** 是否主岗位。 */
    private boolean primaryFlag;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空用户岗位绑定对象。
     */
    public SysUserPost() {
    }

    /**
     * 创建完整用户岗位绑定对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param postId 岗位主键
     * @param postCode 岗位编码
     * @param postName 岗位名称
     * @param primaryFlag 是否主岗位
     * @param createdAt 创建时间
     */
    public SysUserPost(Long id, String tenantId, Long userId, Long postId, String postCode, String postName,
                       boolean primaryFlag, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.postId = postId;
        this.postCode = postCode;
        this.postName = postName;
        this.primaryFlag = primaryFlag;
        this.createdAt = createdAt;
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
     * 返回用户主键。
     *
     * @return 用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户主键。
     *
     * @param userId 用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 返回岗位主键。
     *
     * @return 岗位主键
     */
    public Long getPostId() {
        return postId;
    }

    /**
     * 设置岗位主键。
     *
     * @param postId 岗位主键
     */
    public void setPostId(Long postId) {
        this.postId = postId;
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
     * 返回是否主岗位。
     *
     * @return 是否主岗位
     */
    public boolean isPrimaryFlag() {
        return primaryFlag;
    }

    /**
     * 设置是否主岗位。
     *
     * @param primaryFlag 是否主岗位
     */
    public void setPrimaryFlag(boolean primaryFlag) {
        this.primaryFlag = primaryFlag;
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
}
