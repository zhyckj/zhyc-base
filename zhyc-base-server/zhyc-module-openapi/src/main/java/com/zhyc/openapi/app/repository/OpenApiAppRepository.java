/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.repository;

import com.zhyc.openapi.app.domain.OpenApiApp;

import java.util.List;
import java.util.Optional;

/**
 * 开发者应用仓储。
 */
public interface OpenApiAppRepository {

    /**
     * 查询租户开发者应用列表。
     *
     * @param tenantId 租户业务编码
     * @return 开发者应用列表
     */
    List<OpenApiApp> findByTenantId(String tenantId);

    /**
     * 按租户和应用编码查询开发者应用。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 匹配的开发者应用，不存在时返回空
     */
    default Optional<OpenApiApp> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return findByTenantId(tenantId).stream()
                .filter(app -> appCode.equals(app.getAppCode()))
                .findFirst();
    }

    /**
     * 保存或更新开发者应用。
     *
     * @param app 开发者应用
     */
    void save(OpenApiApp app);
}
