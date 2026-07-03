/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.controller;

/**
 * AI 提示词模板保存请求。
 */
public class AiPromptTemplateSaveRequest {

    private String tenantId;
    private String promptCode;
    private String promptName;
    private String version;
    private String templateContent;
    private String variables;
    private String status;

    public String getTenantId() {
        return tenantId;
    }

    public String getPromptCode() {
        return promptCode;
    }

    public String getPromptName() {
        return promptName;
    }

    public String getVersion() {
        return version;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public String getVariables() {
        return variables;
    }

    public String getStatus() {
        return status;
    }
}
