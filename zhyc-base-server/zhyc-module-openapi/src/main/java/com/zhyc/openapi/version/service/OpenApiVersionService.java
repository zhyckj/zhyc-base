/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.service;

import java.util.List;

/**
 * 开放 API 版本发布业务服务。
 */
public interface OpenApiVersionService {

    /**
     * 按 API 编码查询版本列表。
     *
     * @param apiCode API 业务编码
     * @return API 版本列表
     */
    List<OpenApiVersionResponse> listVersions(String apiCode);

    /**
     * 发布或更新 API 版本。
     *
     * @param command API 版本发布命令
     */
    void publish(OpenApiVersionPublishCommand command);
}
