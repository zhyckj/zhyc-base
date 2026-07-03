/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.service;

import java.util.List;

/**
 * AI 模型配置业务服务。
 */
public interface AiModelConfigService {

    List<AiModelConfigResponse> listModels(String tenantId);

    void save(AiModelConfigSaveCommand command);
}
