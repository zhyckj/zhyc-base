/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

/**
 * 平台后台用户 Shiro 主体。
 *
 * <p>主体中保留租户、用户和账号信息，供授权、审计和后续数据权限计算使用。</p>
 */
public class PlatformUserPrincipal {

    /** 用户主键，用于加载角色、菜单按钮权限和审计记录。 */
    private final Long userId;
    /** 租户业务编码，用于共享表模式下的授权隔离。 */
    private final String tenantId;
    /** 登录账号，租户内唯一。 */
    private final String username;
    /** 用户显示名称，用于后台展示和审计日志。 */
    private final String nickname;

    /**
     * 创建平台后台用户主体。
     *
     * @param userId 用户主键
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param nickname 用户显示名称
     */
    public PlatformUserPrincipal(Long userId, String tenantId, String username, String nickname) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.nickname = nickname;
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
}
