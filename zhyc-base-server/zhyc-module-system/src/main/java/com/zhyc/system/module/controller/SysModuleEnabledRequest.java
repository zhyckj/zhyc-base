/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.controller;

/**
 * 系统模块启停请求。
 */
public class SysModuleEnabledRequest {

    /** 是否启用模块；必须由调用方显式传入，避免空请求被误判为禁用。 */
    private Boolean enabled;

    /**
     * 返回是否启用模块。
     *
     * @return 启用状态，未传入时返回 {@code null}
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用模块。
     *
     * @param enabled 是否启用模块
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
