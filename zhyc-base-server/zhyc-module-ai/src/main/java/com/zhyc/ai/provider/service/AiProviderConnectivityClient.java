/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

/**
 * AI 供应商连通性测试客户端。
 */
public interface AiProviderConnectivityClient {

    /**
     * 判断当前客户端是否支持供应商类型。
     *
     * @param providerType 供应商类型
     * @return 支持返回 {@code true}
     */
    boolean supports(String providerType);

    /**
     * 测试供应商配置是否可用。
     *
     * @param providerType 供应商类型
     * @param providerCode 供应商编码
     * @param secretRef 密钥引用
     * @param baseUrl 基础地址
     * @param apiKey 已解析的 API Key
     */
    void test(String providerType, String providerCode, String secretRef, String baseUrl, String apiKey);
}
