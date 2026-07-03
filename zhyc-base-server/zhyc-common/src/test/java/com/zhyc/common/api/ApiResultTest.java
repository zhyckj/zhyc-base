/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResultTest {

    @Test
    void okCreatesSuccessfulResultWithDataAndTimestamp() {
        ApiResult<String> result = ApiResult.ok("payload");

        assertTrue(result.isSuccess());
        assertEquals("0", result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("payload", result.getData());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    void failCreatesFailedResultWithCodeMessageAndTimestamp() {
        ApiResult<Object> result = ApiResult.fail("E001", "failed");

        assertFalse(result.isSuccess());
        assertEquals("E001", result.getCode());
        assertEquals("failed", result.getMessage());
        assertNull(result.getData());
        assertTrue(result.getTimestamp() > 0);
    }
}
