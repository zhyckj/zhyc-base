/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import org.apache.shiro.authc.AuthenticationToken;

import java.util.Map;

/**
 * 平台认证中心 Claims 认证令牌。
 *
 * <p>该令牌只承载已由认证中心或资源过滤器校验通过的 JWT/OIDC Claims，
 * Realm 仍需回查本地用户状态和用户主键，避免绕过平台授权模型。</p>
 */
public class PlatformAuthCenterAuthenticationToken implements AuthenticationToken {

    /** 认证中心令牌解析后的 Claims 快照，用于映射平台 Shiro 主体。 */
    private final Map<String, Object> claims;

    /**
     * 创建平台认证中心 Claims 认证令牌。
     *
     * @param claims 认证中心令牌解析后的 Claims
     */
    public PlatformAuthCenterAuthenticationToken(Map<String, Object> claims) {
        this.claims = Map.copyOf(claims);
    }

    /**
     * 返回认证中心 Claims 快照。
     *
     * @return 认证中心 Claims
     */
    public Map<String, Object> getClaims() {
        return claims;
    }

    /**
     * 返回认证主体标识。
     *
     * <p>优先返回 {@code preferred_username}，缺失时回退 {@code sub}，仅供 Shiro 记录主体线索。</p>
     *
     * @return 认证主体标识
     */
    @Override
    public Object getPrincipal() {
        Object preferredUsername = claims.get("preferred_username");
        if (preferredUsername != null && !preferredUsername.toString().isBlank()) {
            return preferredUsername.toString().trim();
        }
        Object subject = claims.get("sub");
        return subject == null ? null : subject.toString().trim();
    }

    /**
     * 返回认证凭据占位。
     *
     * <p>该令牌进入 Realm 前必须已完成 Token 签名、有效期和授权方校验，Realm 不再做密码匹配。</p>
     *
     * @return 认证中心已验证凭据占位
     */
    @Override
    public Object getCredentials() {
        return "AUTH_CENTER_VERIFIED";
    }
}
