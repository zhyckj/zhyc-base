/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.repository;

import com.zhyc.openapi.oauthclient.domain.OpenApiOauthClient;

import java.util.List;

/**
 * 开放平台 OAuth2 客户端映射仓储。
 */
public interface OpenApiOauthClientRepository {

    /**
     * 查询租户指定应用的 OAuth2 客户端映射列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return OAuth2 客户端映射列表
     */
    List<OpenApiOauthClient> findByTenantIdAndAppCode(String tenantId, String appCode);

    /**
     * 保存或更新 OAuth2 客户端映射。
     *
     * @param client OAuth2 客户端映射领域对象
     */
    void save(OpenApiOauthClient client);
}
