/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.service;

import java.util.List;

/**
 * AI 应用接入业务服务。
 */
public interface AiAppService {

    List<AiAppResponse> listApps(String tenantId);

    void save(AiAppSaveCommand command);
}
