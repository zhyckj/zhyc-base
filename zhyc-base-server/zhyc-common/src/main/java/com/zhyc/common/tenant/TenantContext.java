/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.tenant;

/**
 * 当前线程的租户上下文。
 *
 * <p>该上下文只在当前线程内可见，请求或任务结束时必须调用 {@link #clear()} 清理。</p>
 */
public final class TenantContext {

    /** 当前线程绑定的租户 ID。 */
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * 设置当前线程的租户 ID。
     *
     * @param tenantId 租户 ID；传入 {@code null} 时等价于调用 {@link #clear()}
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null) {
            clear();
            return;
        }
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前线程的租户 ID。
     *
     * @return 当前线程租户 ID；未设置时返回 {@code null}
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 清理当前线程绑定的租户 ID。
     */
    public static void clear() {
        TENANT_ID.remove();
    }
}
