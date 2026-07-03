/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.domain;

import java.time.LocalDateTime;

/**
 * 系统用户基础模型。
 *
 * <p>用户数据按 {@code tenantId} 进行租户隔离，仅包含系统模块基础字段。</p>
 */
public class SysUser {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码，用于限制用户所属租户。 */
    private String tenantId;
    /** 登录账号。 */
    private String username;
    /** 用户显示名称。 */
    private String nickname;
    /** 密码哈希值。 */
    private String passwordHash;
    /** 用户状态，例如 enabled、disabled。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空用户对象。
     */
    public SysUser() {
    }

    /**
     * 创建完整用户对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param nickname 用户显示名称
     * @param passwordHash 密码哈希值
     * @param status 用户状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysUser(Long id, String tenantId, String username, String nickname, String passwordHash,
                   String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.username = username;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
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
     * 返回登录账号。
     *
     * @return 登录账号
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置登录账号。
     *
     * @param username 登录账号
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 返回用户显示名称。
     *
     * @return 用户显示名称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置用户显示名称。
     *
     * @param nickname 用户显示名称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 返回密码哈希值。
     *
     * @return 密码哈希值
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置密码哈希值。
     *
     * @param passwordHash 密码哈希值
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 返回用户状态。
     *
     * @return 用户状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置用户状态。
     *
     * @param status 用户状态
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
