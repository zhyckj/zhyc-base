/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.i18n.domain.I18nMessage;
import com.zhyc.i18n.repository.I18nRepository;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认国际化词条业务服务实现。
 */
@Service
public class DefaultI18nService implements I18nService {

  /** 首期支持的国际化词条启停状态。 */
  private static final Set<String> MESSAGE_STATUSES = Set.of("enabled", "disabled");

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_I18N_TENANT_REQUIRED";

  /** 语言标识为空错误码。 */
  private static final String ERROR_LOCALE_REQUIRED = "ZHYC_I18N_LOCALE_REQUIRED";

  /** 词条键为空错误码。 */
  private static final String ERROR_MESSAGE_KEY_REQUIRED = "ZHYC_I18N_MESSAGE_KEY_REQUIRED";

  /** 词条值为空错误码。 */
  private static final String ERROR_MESSAGE_VALUE_REQUIRED = "ZHYC_I18N_MESSAGE_VALUE_REQUIRED";

  /** 词条状态为空错误码。 */
  private static final String ERROR_MESSAGE_STATUS_REQUIRED = "ZHYC_I18N_MESSAGE_STATUS_REQUIRED";

  /** 词条状态不支持错误码。 */
  private static final String ERROR_MESSAGE_STATUS_UNSUPPORTED = "ZHYC_I18N_MESSAGE_STATUS_UNSUPPORTED";

  /** 批量解析默认文案映射为空错误码。 */
  private static final String ERROR_RESOLVE_DEFAULTS_REQUIRED = "ZHYC_I18N_RESOLVE_DEFAULTS_REQUIRED";

  /** 国际化词条仓储。 */
  private final I18nRepository i18nRepository;

  /**
   * 创建国际化业务服务。
   *
   * @param i18nRepository 国际化词条仓储
   */
  public DefaultI18nService(I18nRepository i18nRepository) {
    this.i18nRepository = Objects.requireNonNull(i18nRepository, "国际化词条仓储不能为空");
  }

  @Override
  public List<I18nMessageResponse> listMessages(String tenantId, String locale, String status) {
    return i18nRepository.findMessages(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(locale), trimToNull(status)).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public void saveMessage(I18nMessageSaveCommand command) {
    Objects.requireNonNull(command, "国际化词条保存命令不能为空");
    i18nRepository.saveMessage(new I18nMessage(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), requireText(command.locale(), ERROR_LOCALE_REQUIRED, "语言标识不能为空"),
        requireText(command.messageKey(), ERROR_MESSAGE_KEY_REQUIRED, "词条键不能为空"),
        requireText(command.messageValue(), ERROR_MESSAGE_VALUE_REQUIRED, "词条值不能为空"),
        requireStatus(defaultText(command.status(), "enabled")), null, null));
  }

  @Override
  public String resolveMessage(String tenantId, String locale, String messageKey, String defaultMessage) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedLocale = requireText(locale, ERROR_LOCALE_REQUIRED, "语言标识不能为空");
    String normalizedKey = requireText(messageKey, ERROR_MESSAGE_KEY_REQUIRED, "词条键不能为空");
    return i18nRepository.findEnabledMessage(normalizedTenantId, normalizedLocale, normalizedKey)
        .map(I18nMessage::getMessageValue)
        .orElse(defaultMessage);
  }

  @Override
  public I18nResolveResponse resolveMessages(I18nResolveCommand command) {
    Objects.requireNonNull(command, "国际化词条批量解析命令不能为空");
    String normalizedTenantId = requireText(command.tenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedLocale = requireText(command.locale(), ERROR_LOCALE_REQUIRED, "语言标识不能为空");
    if (command.defaults() == null || command.defaults().isEmpty()) {
      throw new BusinessException(ERROR_RESOLVE_DEFAULTS_REQUIRED, "国际化默认文案映射不能为空");
    }
    Map<String, String> resolvedMessages = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : command.defaults().entrySet()) {
      String messageKey = requireText(entry.getKey(), ERROR_MESSAGE_KEY_REQUIRED, "词条键不能为空");
      String defaultMessage = defaultText(entry.getValue(), messageKey);
      String messageValue = i18nRepository.findEnabledMessage(normalizedTenantId, normalizedLocale, messageKey)
          .map(I18nMessage::getMessageValue)
          .orElse(defaultMessage);
      resolvedMessages.put(messageKey, messageValue);
    }
    return new I18nResolveResponse(normalizedLocale, resolvedMessages);
  }

  private I18nMessageResponse toResponse(I18nMessage message) {
    return new I18nMessageResponse(message.getId(), message.getTenantId(), message.getLocale(),
        message.getMessageKey(), message.getMessageValue(), message.getStatus(),
        message.getCreatedAt(), message.getUpdatedAt());
  }

  private String defaultText(String value, String defaultValue) {
    String normalized = trimToNull(value);
    return normalized == null ? defaultValue : normalized;
  }

  /**
   * 校验国际化词条状态必须属于首期支持范围。
   *
   * @param value 原始词条状态
   * @return 小写规范化后的词条状态
   */
  private String requireStatus(String value) {
    String normalized = requireText(value, ERROR_MESSAGE_STATUS_REQUIRED, "词条状态不能为空").toLowerCase(Locale.ROOT);
    if (!MESSAGE_STATUSES.contains(normalized)) {
      throw new BusinessException(ERROR_MESSAGE_STATUS_UNSUPPORTED, "词条状态不支持: " + normalized);
    }
    return normalized;
  }

  private String requireText(String value, String code, String message) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      throw new BusinessException(code, message);
    }
    return normalized;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
