/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.service;

import java.util.List;

/**
 * 国际化词条业务服务。
 */
public interface I18nService {

  /**
   * 查询国际化词条列表。
   *
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param status 词条状态
   * @return 国际化词条列表
   */
  List<I18nMessageResponse> listMessages(String tenantId, String locale, String status);

  /**
   * 保存国际化词条。
   *
   * @param command 词条保存命令
   */
  void saveMessage(I18nMessageSaveCommand command);

  /**
   * 解析国际化词条。
   *
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param messageKey 词条键
   * @param defaultMessage 默认文案
   * @return 解析后的文案
   */
  String resolveMessage(String tenantId, String locale, String messageKey, String defaultMessage);

  /**
   * 批量解析国际化词条。
   *
   * @param command 批量解析命令
   * @return 批量解析响应
   */
  I18nResolveResponse resolveMessages(I18nResolveCommand command);
}
