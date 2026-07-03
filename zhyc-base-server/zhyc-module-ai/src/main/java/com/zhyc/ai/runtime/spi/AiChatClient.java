/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.spi;

/**
 * AI 对话模型供应商适配器。
 *
 * <p>运行时服务只依赖该统一接口，具体供应商调用细节由适配器封装，避免业务侧直接耦合第三方 SDK。</p>
 */
public interface AiChatClient {

    /**
     * 判断当前适配器是否支持供应商类型。
     *
     * @param providerType 供应商类型编码
     * @return 支持返回 {@code true}
     */
    boolean supports(String providerType);

    /**
     * 发起非流式对话调用。
     *
     * @param request 对话请求上下文
     * @param apiKey 已解析的模型供应商密钥
     * @return 对话响应
     */
    AiChatResponse chat(AiChatRequest request, String apiKey);
}
