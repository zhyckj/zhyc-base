/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.service;

import java.util.List;

/**
 * 开放 API 权限授权业务服务。
 */
public interface OpenApiPermissionService {

    /**
     * 查询租户指定应用的开放 API 授权列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 授权列表
     */
    List<OpenApiPermissionResponse> listPermissions(String tenantId, String appCode);

    /**
     * 保存或更新开放 API 授权。
     *
     * @param command 开放 API 权限授权保存命令
     */
    void save(OpenApiPermissionSaveCommand command);
}
