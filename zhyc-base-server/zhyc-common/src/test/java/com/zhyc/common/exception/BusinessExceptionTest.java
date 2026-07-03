/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 业务异常测试。
 */
class BusinessExceptionTest {

    /**
     * 验证业务异常携带稳定错误码和面向用户的中文消息。
     */
    @Test
    void shouldExposeStableErrorCodeAndUserMessage() {
        BusinessException exception = new BusinessException("MODULE_DEPENDENCY_CONFLICT", "模块仍被启用模块依赖");

        assertEquals("MODULE_DEPENDENCY_CONFLICT", exception.getCode());
        assertEquals("模块仍被启用模块依赖", exception.getMessage());
    }

    /**
     * 验证业务错误码不能为空白，避免调用方无法基于稳定错误码处理异常。
     */
    @Test
    void shouldRejectBlankErrorCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new BusinessException(" ", "模块仍被启用模块依赖"));

        assertEquals("业务错误码不能为空", exception.getMessage());
    }

    /**
     * 验证业务异常消息不能为空白，避免接口返回不可读的错误说明。
     */
    @Test
    void shouldRejectBlankUserMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new BusinessException("MODULE_DEPENDENCY_CONFLICT", " "));

        assertEquals("业务异常消息不能为空", exception.getMessage());
    }
}
