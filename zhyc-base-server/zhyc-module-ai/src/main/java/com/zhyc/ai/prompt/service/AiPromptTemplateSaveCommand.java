/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.service;

/**
 * AI 提示词模板保存命令。
 */
public class AiPromptTemplateSaveCommand {

    private final String tenantId;
    private final String promptCode;
    private final String promptName;
    private final String version;
    private final String templateContent;
    private final String variables;
    private final String status;

    public AiPromptTemplateSaveCommand(String tenantId, String promptCode, String promptName, String version,
                                       String templateContent, String variables, String status) {
        this.tenantId = tenantId;
        this.promptCode = promptCode;
        this.promptName = promptName;
        this.version = version;
        this.templateContent = templateContent;
        this.variables = variables;
        this.status = status;
    }

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
