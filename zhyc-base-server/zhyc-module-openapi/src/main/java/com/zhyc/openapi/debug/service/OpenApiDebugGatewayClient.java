/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.service;

/**
 * 开放 API 调试网关客户端。
 */
public interface OpenApiDebugGatewayClient {

    /**
     * 调用开放 API 网关。
     *
     * @param request 调试网关请求
     * @return 调试网关响应
     */
    OpenApiDebugGatewayResponse invoke(OpenApiDebugGatewayRequest request);
}
