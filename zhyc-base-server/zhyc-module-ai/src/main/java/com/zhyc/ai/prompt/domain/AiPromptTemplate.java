/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.domain;

import java.time.LocalDateTime;

/**
 * AI 提示词模板领域对象。
 */
public class AiPromptTemplate {

    private final Long id;
    private final String tenantId;
    private final String promptCode;
    private final String promptName;
    private final String version;
    private final String templateContent;
    private final String variables;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AiPromptTemplate(Long id, String tenantId, String promptCode, String promptName, String version,
                            String templateContent, String variables, String status, LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.promptCode = promptCode;
        this.promptName = promptName;
        this.version = version;
        this.templateContent = templateContent;
        this.variables = variables;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
