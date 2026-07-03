/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.message.template.domain.MsgTemplate;
import com.zhyc.message.template.repository.MsgTemplateRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认消息模板业务服务实现。
 */
@Service
public class DefaultMsgTemplateService implements MsgTemplateService {

  /** 首期支持的消息模板通道类型。 */
  private static final Set<String> CHANNEL_TYPES = Set.of("in_app", "push", "sms", "email");
  /** 首期支持的消息模板启停状态。 */
  private static final Set<String> TEMPLATE_STATUSES = Set.of("enabled", "disabled");

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_TENANT_REQUIRED";

  /** 模板编码为空错误码。 */
  private static final String ERROR_TEMPLATE_CODE_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_CODE_REQUIRED";

  /** 模板名称为空错误码。 */
  private static final String ERROR_TEMPLATE_NAME_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_NAME_REQUIRED";

  /** 消息通道类型为空错误码。 */
  private static final String ERROR_CHANNEL_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_CHANNEL_REQUIRED";

  /** 消息通道类型不支持错误码。 */
  private static final String ERROR_CHANNEL_UNSUPPORTED = "ZHYC_MESSAGE_TEMPLATE_CHANNEL_UNSUPPORTED";

  /** 标题模板为空错误码。 */
  private static final String ERROR_TITLE_TEMPLATE_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_TITLE_REQUIRED";

  /** 内容模板为空错误码。 */
  private static final String ERROR_CONTENT_TEMPLATE_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_CONTENT_REQUIRED";

  /** 模板状态为空错误码。 */
  private static final String ERROR_STATUS_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_STATUS_REQUIRED";

  /** 模板状态不支持错误码。 */
  private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_MESSAGE_TEMPLATE_STATUS_UNSUPPORTED";

  /** 消息模板仓储。 */
  private final MsgTemplateRepository templateRepository;

  /**
   * 创建消息模板业务服务。
   *
   * @param templateRepository 消息模板仓储
   */
  public DefaultMsgTemplateService(MsgTemplateRepository templateRepository) {
    this.templateRepository = Objects.requireNonNull(templateRepository, "消息模板仓储不能为空");
  }

  @Override
  public List<MsgTemplateResponse> listTemplates(String tenantId) {
    String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    return templateRepository.findByTenantId(requiredTenantId).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public void save(MsgTemplateSaveCommand command) {
    Objects.requireNonNull(command, "消息模板保存命令不能为空");
    MsgTemplate template = new MsgTemplate(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), requireText(command.templateCode(), ERROR_TEMPLATE_CODE_REQUIRED, "模板编码不能为空"),
        requireText(command.templateName(), ERROR_TEMPLATE_NAME_REQUIRED, "模板名称不能为空"),
        requireChannelType(command.channelType()),
        requireText(command.titleTemplate(), ERROR_TITLE_TEMPLATE_REQUIRED, "标题模板不能为空"),
        requireText(command.contentTemplate(), ERROR_CONTENT_TEMPLATE_REQUIRED, "内容模板不能为空"),
        requireStatus(defaultText(command.status(), "enabled")), null, null);
    templateRepository.save(template);
  }

  private MsgTemplateResponse toResponse(MsgTemplate template) {
    return new MsgTemplateResponse(template.getId(), template.getTenantId(), template.getTemplateCode(),
        template.getTemplateName(), template.getChannelType(), template.getTitleTemplate(),
        template.getContentTemplate(), template.getStatus(), template.getCreatedAt(), template.getUpdatedAt());
  }

  private String defaultText(String value, String defaultValue) {
    String normalized = trimToNull(value);
    return normalized == null ? defaultValue : normalized;
  }

  private String requireText(String value, String code, String message) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      throw new BusinessException(code, message);
    }
    return normalized;
  }

  /**
   * 校验消息模板通道类型必须属于首期支持范围。
   *
   * @param value 原始通道类型
   * @return 小写规范化后的通道类型
   */
  private String requireChannelType(String value) {
    String normalized = requireText(value, ERROR_CHANNEL_REQUIRED, "消息通道类型不能为空").toLowerCase(Locale.ROOT);
    if (!CHANNEL_TYPES.contains(normalized)) {
      throw new BusinessException(ERROR_CHANNEL_UNSUPPORTED, "消息通道类型不支持: " + normalized);
    }
    return normalized;
  }

  /**
   * 校验消息模板状态必须属于首期支持范围。
   *
   * @param value 原始模板状态
   * @return 小写规范化后的模板状态
   */
  private String requireStatus(String value) {
    String normalized = requireText(value, ERROR_STATUS_REQUIRED, "模板状态不能为空").toLowerCase(Locale.ROOT);
    if (!TEMPLATE_STATUSES.contains(normalized)) {
      throw new BusinessException(ERROR_STATUS_UNSUPPORTED, "模板状态不支持: " + normalized);
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
