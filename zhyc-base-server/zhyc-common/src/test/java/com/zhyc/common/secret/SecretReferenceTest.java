/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.secret;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 密钥引用测试。
 */
class SecretReferenceTest {

    /**
     * 验证密钥引用可以解析出密钥编码，并返回规范化后的引用值。
     */
    @Test
    void shouldParseSecretReferenceAndNormalizeValue() {
        SecretReference reference = SecretReference.parse("secret:gateway-signing-key");

        assertEquals("gateway-signing-key", reference.getCode());
        assertEquals("secret:gateway-signing-key", reference.getValue());
        assertEquals("secret:gateway-signing-key", reference.toString());
    }

    /**
     * 验证仅凭密钥编码也可以生成稳定引用值。
     */
    @Test
    void shouldBuildNormalizedReferenceFromCode() {
        SecretReference reference = SecretReference.ofCode("oauth-client-secret");

        assertEquals("oauth-client-secret", reference.getCode());
        assertEquals("secret:oauth-client-secret", reference.getValue());
    }

    /**
     * 验证空值会被拒绝，避免低代码数据源引用出现不可预期行为。
     */
    @Test
    void shouldRejectBlankReference() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SecretReference.parse(" "));

        assertEquals("密钥引用不能为空", exception.getMessage());
    }

    /**
     * 验证缺少 secret: 前缀的引用会被拒绝。
     */
    @Test
    void shouldRejectReferenceWithoutPrefix() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SecretReference.parse("gateway-signing-key"));

        assertEquals("密钥引用必须以 secret: 开头", exception.getMessage());
    }

    /**
     * 验证引用中包含空白字符时会被拒绝，保证引用格式稳定。
     */
    @Test
    void shouldRejectReferenceWithWhitespace() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SecretReference.parse("secret:gateway signing key"));

        assertEquals("密钥引用不能包含空白", exception.getMessage());
    }
}
