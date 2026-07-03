/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.controller;

import java.util.Map;

/**
 * AI 运行时对话调用请求。
 */
public class AiRuntimeChatRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** AI 应用编码。 */
    private String appCode;
    /** 提示词模板编码。 */
    private String promptCode;
    /** 提示词模板版本，空值默认使用 v1。 */
    private String promptVersion;
    /** 提示词变量值。 */
    private Map<String, String> variables;
    /** 是否请求流式输出。 */
    private boolean stream;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getPromptCode() {
        return promptCode;
    }

    public void setPromptCode(String promptCode) {
        this.promptCode = promptCode;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
