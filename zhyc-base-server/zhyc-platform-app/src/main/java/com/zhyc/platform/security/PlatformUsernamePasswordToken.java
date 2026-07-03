/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 平台后台租户用户名密码令牌。
 *
 * <p>Shiro 原生 {@link UsernamePasswordToken} 不包含租户信息，本令牌显式携带租户业务编码，
 * 避免认证查询在多租户共享表中发生跨租户回退。</p>
 */
public class PlatformUsernamePasswordToken extends UsernamePasswordToken {

    /** 租户业务编码，用于认证查询和后续授权隔离。 */
    private final String tenantId;

    /**
     * 创建平台后台租户用户名密码令牌。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param password 登录密码字符数组
     */
    public PlatformUsernamePasswordToken(String tenantId, String username, char[] password) {
        super(username, password);
        this.tenantId = tenantId;
    }

    /**
     * 创建平台后台租户用户名密码令牌。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param password 登录密码
     */
    public PlatformUsernamePasswordToken(String tenantId, String username, String password) {
        super(username, password);
        this.tenantId = tenantId;
    }

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }
}
