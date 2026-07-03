/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.service;

import java.util.Map;

/**
 * 国际化词条批量解析命令。
 *
 * @param tenantId 租户业务编码
 * @param locale 语言标识
 * @param defaults 词条键与默认文案映射，解析缺失时返回默认文案
 */
public record I18nResolveCommand(String tenantId, String locale, Map<String, String> defaults) {
}
