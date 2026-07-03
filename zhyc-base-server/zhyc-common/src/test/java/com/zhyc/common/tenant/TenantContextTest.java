/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TenantContextTest {

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void setTenantIdStoresTenantForCurrentThread() {
        TenantContext.setTenantId("tenant-1");

        assertEquals("tenant-1", TenantContext.getTenantId());
    }

    @Test
    void clearRemovesTenantForCurrentThread() {
        TenantContext.setTenantId("tenant-1");
        TenantContext.clear();

        assertNull(TenantContext.getTenantId());
    }

    @Test
    void setTenantIdWithNullClearsTenantForCurrentThread() {
        TenantContext.setTenantId("tenant-1");
        TenantContext.setTenantId(null);

        assertNull(TenantContext.getTenantId());
    }

    @Test
    void tenantIdDoesNotLeakAcrossThreads() throws InterruptedException {
        TenantContext.setTenantId("main-tenant");
        AtomicReference<String> tenantInOtherThread = new AtomicReference<>();
        Thread thread = new Thread(() -> tenantInOtherThread.set(TenantContext.getTenantId()));

        thread.start();
        thread.join();

        assertNull(tenantInOtherThread.get());
        assertEquals("main-tenant", TenantContext.getTenantId());
    }
}
