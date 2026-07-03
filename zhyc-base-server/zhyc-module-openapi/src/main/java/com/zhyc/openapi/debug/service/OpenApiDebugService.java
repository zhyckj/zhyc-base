/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.service;

/**
 * 开放 API 调试代理业务服务。
 */
public interface OpenApiDebugService {

    /**
     * 通过后台代理发送开放 API 调试请求。
     *
     * @param command 开放 API 调试命令
     * @return 开放 API 调试响应
     */
    OpenApiDebugResponse invoke(OpenApiDebugCommand command);
}
