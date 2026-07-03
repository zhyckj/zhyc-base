/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.service;

import java.util.List;

/**
 * AI 提示词模板业务服务。
 */
public interface AiPromptTemplateService {

    List<AiPromptTemplateResponse> listTemplates(String tenantId);

    void save(AiPromptTemplateSaveCommand command);
}
