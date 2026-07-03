/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.service;

import java.util.Map;

/**
 * 国际化词条批量解析响应。
 *
 * @param locale 语言标识
 * @param messages 词条键与解析文案映射
 */
public record I18nResolveResponse(String locale, Map<String, String> messages) {
}
