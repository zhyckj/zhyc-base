/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.service;

import java.util.Map;

/**
 * AI 运行时对话调用命令。
 *
 * @param tenantId 租户业务编码
 * @param appCode AI 应用编码
 * @param promptCode 提示词模板编码
 * @param promptVersion 提示词模板版本，空值默认使用 v1
 * @param variables 提示词变量值
 * @param stream 是否请求流式输出
 */
public record AiRuntimeChatCommand(String tenantId, String appCode, String promptCode, String promptVersion,
                                   Map<String, String> variables, boolean stream) {
}
