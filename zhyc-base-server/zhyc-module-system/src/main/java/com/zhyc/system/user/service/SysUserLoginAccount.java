/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

/**
 * 系统用户登录账号视图。
 *
 * <p>该对象只暴露认证授权链路需要的最小字段，避免 Shiro Realm 直接依赖完整用户实体。</p>
 */
public class SysUserLoginAccount {

    /** 用户主键，用于后续按用户加载角色、菜单和按钮权限。 */
    private final Long userId;
    /** 租户业务编码，用于共享表模式下的认证和授权隔离。 */
    private final String tenantId;
    /** 登录账号，租户内唯一。 */
    private final String username;
    /** 用户显示名称，用于审计日志和后台展示。 */
    private final String nickname;
    /** 密码哈希值，由 Shiro 凭证匹配器校验，禁止明文存储。 */
    private final String passwordHash;
    /** 用户状态，启用用户才允许进入后台管理端。 */
    private final String status;

    /**
     * 创建系统用户登录账号视图。
     *
     * @param userId 用户主键
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param nickname 用户显示名称
     * @param passwordHash 密码哈希值
     * @param status 用户状态
     */
    public SysUserLoginAccount(Long userId, String tenantId, String username, String nickname,
                               String passwordHash, String status) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
        this.status = status;
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
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
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
     * 返回用户显示名称。
     *
     * @return 用户显示名称
     */
    public String getNickname() {
        return nickname;
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
     * 返回用户状态。
     *
     * @return 用户状态
     */
    public String getStatus() {
        return status;
    }
}
