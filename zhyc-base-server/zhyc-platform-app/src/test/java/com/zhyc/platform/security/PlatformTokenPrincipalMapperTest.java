/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 认证中心令牌主体映射器测试。
 */
class PlatformTokenPrincipalMapperTest {

    /**
     * 验证认证中心 Claims 可以映射为平台 Shiro 主体，供后续权限和审计链路使用。
     */
    @Test
    void shouldMapAuthCenterClaimsToPlatformPrincipal() {
        PlatformTokenPrincipalMapper mapper = new PlatformTokenPrincipalMapper();

        PlatformUserPrincipal principal = mapper.mapClaims(Map.of(
                "tenant_id", "tenant_a",
                "user_id", 1001L,
                "preferred_username", "admin",
                "name", "管理员"));

        assertEquals(1001L, principal.getUserId());
        assertEquals("tenant_a", principal.getTenantId());
        assertEquals("admin", principal.getUsername());
        assertEquals("管理员", principal.getNickname());
    }

    /**
     * 验证令牌缺少租户编码时拒绝映射，避免进入无租户授权上下文。
     */
    @Test
    void shouldRejectMissingTenantId() {
        PlatformTokenPrincipalMapper mapper = new PlatformTokenPrincipalMapper();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mapper.mapClaims(Map.of("user_id", 1001L, "preferred_username", "admin")));

        assertEquals("认证中心令牌缺少租户业务编码", exception.getMessage());
    }

    /**
     * 验证令牌缺少用户主键时拒绝映射，避免授权链路无法定位本地用户权限。
     */
    @Test
    void shouldRejectMissingUserId() {
        PlatformTokenPrincipalMapper mapper = new PlatformTokenPrincipalMapper();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mapper.mapClaims(Map.of("tenant_id", "tenant_a", "preferred_username", "admin")));

        assertEquals("认证中心令牌缺少用户主键", exception.getMessage());
    }

    /**
     * 验证用户主键格式异常时拒绝映射，避免非法 Claims 进入平台主体。
     */
    @Test
    void shouldRejectInvalidUserId() {
        PlatformTokenPrincipalMapper mapper = new PlatformTokenPrincipalMapper();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mapper.mapClaims(Map.of(
                        "tenant_id", "tenant_a",
                        "user_id", "invalid",
                        "preferred_username", "admin")));

        assertEquals("认证中心令牌用户主键格式不正确", exception.getMessage());
    }

    /**
     * 验证认证中心未提供 preferred_username 时可以回退使用 OIDC subject 作为登录账号。
     */
    @Test
    void shouldUseSubjectWhenPreferredUsernameMissing() {
        PlatformTokenPrincipalMapper mapper = new PlatformTokenPrincipalMapper();

        PlatformUserPrincipal principal = mapper.mapClaims(Map.of(
                "tenant_id", "tenant_a",
                "user_id", "1001",
                "sub", "auth-user-1001"));

        assertEquals("auth-user-1001", principal.getUsername());
        assertEquals("auth-user-1001", principal.getNickname());
    }
}
