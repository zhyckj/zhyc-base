/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.service;

import java.io.Serializable;

/**
 * AI 应用接入响应。
 *
 * @param appCode 应用编码
 * @param appName 应用名称
 * @param defaultModelId 默认模型配置主键
 * @param systemPrompt 系统提示词
 * @param dailyTokenQuota 每日令牌额度
 * @param status 应用状态
 */
public record AiAppResponse(String appCode, String appName, Long defaultModelId, String systemPrompt,
                            int dailyTokenQuota, String status) implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;
}
