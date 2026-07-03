/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.service;

import java.util.List;

/**
 * 开发者应用业务服务。
 */
public interface OpenApiAppService {

    /**
     * 查询租户开发者应用列表。
     *
     * @param tenantId 租户业务编码
     * @return 开发者应用列表
     */
    List<OpenApiAppResponse> listApps(String tenantId);

    /**
     * 保存或更新开发者应用。
     *
     * @param command 开发者应用保存命令
     */
    void save(OpenApiAppSaveCommand command);
}
