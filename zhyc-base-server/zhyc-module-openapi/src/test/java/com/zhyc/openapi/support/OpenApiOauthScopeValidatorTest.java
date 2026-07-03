/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放 API OAuth2 授权范围校验器测试。
 */
class OpenApiOauthScopeValidatorTest {

    /**
     * 验证授权范围校验器允许字母、数字、点、下划线、短横线和冒号。
     */
    @Test
    void shouldAcceptSupportedScopeCharacters() {
        assertDoesNotThrow(() -> OpenApiOauthScopeValidator.requireSupportedScope("purchase.request:read_v1"));
    }

    /**
     * 验证授权范围校验器会拒绝通配符，避免第三方应用获得过宽权限。
     */
    @Test
    void shouldRejectWildcardScope() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiOauthScopeValidator.requireSupportedScope("purchase.*"));

        assertEquals("OAuth2 授权范围不能包含通配符 *", exception.getMessage());
    }

    /**
     * 验证授权范围校验器会拒绝逗号等不受支持字符。
     */
    @Test
    void shouldRejectUnsupportedScopeCharacter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiOauthScopeValidator.requireSupportedScope("openid,profile"));

        assertEquals("OAuth2 授权范围只能包含字母、数字、点、下划线、短横线或冒号", exception.getMessage());
    }
}
