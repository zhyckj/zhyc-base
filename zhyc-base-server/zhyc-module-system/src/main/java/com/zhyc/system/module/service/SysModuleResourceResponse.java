/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.service;

/**
 * 系统模块资源响应对象。
 */
public class SysModuleResourceResponse {

    /** 资源类型，例如 menu、permission、dict、route、template。 */
    private final String resourceType;
    /** 资源编码。 */
    private final String resourceCode;
    /** 资源路径或权限标识。 */
    private final String resourcePath;

    /**
     * 创建系统模块资源响应对象。
     *
     * @param resourceType 资源类型
     * @param resourceCode 资源编码
     * @param resourcePath 资源路径或权限标识
     */
    public SysModuleResourceResponse(String resourceType, String resourceCode, String resourcePath) {
        this.resourceType = resourceType;
        this.resourceCode = resourceCode;
        this.resourcePath = resourcePath;
    }

    /**
     * 返回资源类型。
     *
     * @return 资源类型
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * 返回资源编码。
     *
     * @return 资源编码
     */
    public String getResourceCode() {
        return resourceCode;
    }

    /**
     * 返回资源路径或权限标识。
     *
     * @return 资源路径或权限标识
     */
    public String getResourcePath() {
        return resourcePath;
    }
}
