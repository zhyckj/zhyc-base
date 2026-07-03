/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.repository;

import com.zhyc.i18n.domain.I18nMessage;
import java.util.List;
import java.util.Optional;

/**
 * 国际化词条仓储接口。
 */
public interface I18nRepository {

  /**
   * 查询国际化词条列表。
   *
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param status 词条状态
   * @return 国际化词条列表
   */
  List<I18nMessage> findMessages(String tenantId, String locale, String status);

  /**
   * 保存国际化词条。
   *
   * @param message 国际化词条实体
   */
  void saveMessage(I18nMessage message);

  /**
   * 查询启用状态的国际化词条。
   *
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param messageKey 词条键
   * @return 启用词条
   */
  Optional<I18nMessage> findEnabledMessage(String tenantId, String locale, String messageKey);
}
