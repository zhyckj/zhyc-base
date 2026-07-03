/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.service;

import java.time.LocalDateTime;

/**
 * 国际化词条响应。
 *
 * @param id 主键
 * @param tenantId 租户业务编码
 * @param locale 语言标识
 * @param messageKey 词条键
 * @param messageValue 词条值
 * @param status 词条状态
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record I18nMessageResponse(Long id, String tenantId, String locale, String messageKey,
                                  String messageValue, String status, LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
}
