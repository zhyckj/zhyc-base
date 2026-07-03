/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.service;

import java.io.Serializable;

/**
 * AI 模型配置响应对象。
 */
public class AiModelConfigResponse implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 模型配置主键；用于应用接入绑定默认模型。 */
    private final Long id;
    /** 供应商主键；仅用于前端供应商下拉回填和提交。 */
    private final Long providerId;
    /** 模型编码。 */
    private final String modelCode;
    /** 模型名称。 */
    private final String modelName;
    /** 模型类型。 */
    private final String modelType;
    /** 上下文长度。 */
    private final int contextWindow;
    /** 是否支持流式输出。 */
    private final boolean supportStream;
    /** 是否支持工具调用。 */
    private final boolean supportTool;
    /** 模型状态。 */
    private final String status;

    public AiModelConfigResponse(Long id, Long providerId, String modelCode, String modelName, String modelType,
                                 int contextWindow, boolean supportStream, boolean supportTool, String status) {
        this.id = id;
        this.providerId = providerId;
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.modelType = modelType;
        this.contextWindow = contextWindow;
        this.supportStream = supportStream;
        this.supportTool = supportTool;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getModelCode() {
        return modelCode;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelType() {
        return modelType;
    }

    public int getContextWindow() {
        return contextWindow;
    }

    public boolean isSupportStream() {
        return supportStream;
    }

    public boolean isSupportTool() {
        return supportTool;
    }

    public String getStatus() {
        return status;
    }
}
