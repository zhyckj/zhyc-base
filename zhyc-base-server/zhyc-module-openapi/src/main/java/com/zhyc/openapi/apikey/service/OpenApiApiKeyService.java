/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.service;

import java.util.List;

/**
 * API Key 业务服务。
 */
public interface OpenApiApiKeyService {

    /**
     * 查询租户指定应用的 API Key 列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return API Key 列表
     */
    List<OpenApiApiKeyResponse> listApiKeys(String tenantId, String appCode);

    /**
     * 保存或更新 API Key。
     *
     * @param command API Key 保存命令
     */
    void save(OpenApiApiKeySaveCommand command);

    /**
     * 轮换 API Key Secret。
     *
     * <p>只写入新 Secret 密文，不读取或返回旧 Secret 明文。</p>
     *
     * @param command API Key Secret 轮换命令
     */
    void rotateSecret(OpenApiApiKeyRotateCommand command);
}
