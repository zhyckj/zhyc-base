/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放 API HTTP 方法枚举测试。
 */
class OpenApiHttpMethodTest {

    /**
     * 验证 HTTP 方法枚举会将小写编码规范化为大写编码。
     */
    @Test
    void shouldNormalizeHttpMethodCodeToUpperCase() {
        assertEquals("POST", OpenApiHttpMethod.fromCode("post").getCode());
    }

    /**
     * 验证 HTTP 方法枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedHttpMethodCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiHttpMethod.fromCode("TRACE"));

        assertEquals("HTTP 方法只支持 GET、POST、PUT、DELETE 或 PATCH", exception.getMessage());
    }
}
