/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.service;

import java.util.List;

/**
 * 开放平台 OAuth2 客户端映射业务服务。
 */
public interface OpenApiOauthClientService {

    /**
     * 查询租户指定应用的 OAuth2 客户端映射列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return OAuth2 客户端映射列表
     */
    List<OpenApiOauthClientResponse> listClients(String tenantId, String appCode);

    /**
     * 保存或更新 OAuth2 客户端映射。
     *
     * @param command OAuth2 客户端映射保存命令
     */
    void save(OpenApiOauthClientSaveCommand command);
}
