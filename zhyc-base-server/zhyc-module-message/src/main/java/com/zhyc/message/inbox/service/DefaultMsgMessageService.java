/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.service;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.message.inbox.domain.MsgMessage;
import com.zhyc.message.inbox.repository.MsgMessageRepository;
import com.zhyc.message.template.domain.MsgTemplate;
import com.zhyc.message.template.repository.MsgTemplateRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认站内消息业务服务实现。
 */
@Service
public class DefaultMsgMessageService implements MsgMessageService {

  /** 消息编码日期格式。 */
  private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  /** 消息编码随机数生成器。 */
  private static final SecureRandom RANDOM = new SecureRandom();
  /** 模板变量占位符匹配模式。 */
  private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");
  /** 首期支持的站内消息类型。 */
  private static final Set<String> MESSAGE_TYPES = Set.of("notice", "approval", "system", "workflow");

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_MESSAGE_INBOX_TENANT_REQUIRED";

  /** 接收人用户 ID 为空错误码。 */
  private static final String ERROR_RECEIVER_REQUIRED = "ZHYC_MESSAGE_INBOX_RECEIVER_REQUIRED";

  /** 消息类型为空错误码。 */
  private static final String ERROR_MESSAGE_TYPE_REQUIRED = "ZHYC_MESSAGE_INBOX_TYPE_REQUIRED";

  /** 消息类型不支持错误码。 */
  private static final String ERROR_MESSAGE_TYPE_UNSUPPORTED = "ZHYC_MESSAGE_INBOX_TYPE_UNSUPPORTED";

  /** 消息标题为空错误码。 */
  private static final String ERROR_TITLE_REQUIRED = "ZHYC_MESSAGE_INBOX_TITLE_REQUIRED";

  /** 消息内容为空错误码。 */
  private static final String ERROR_CONTENT_REQUIRED = "ZHYC_MESSAGE_INBOX_CONTENT_REQUIRED";

  /** 消息编码为空错误码。 */
  private static final String ERROR_MESSAGE_CODE_REQUIRED = "ZHYC_MESSAGE_INBOX_CODE_REQUIRED";

  /** 消息不存在或无权读取错误码。 */
  private static final String ERROR_MESSAGE_NOT_FOUND = "ZHYC_MESSAGE_INBOX_NOT_FOUND";

  /** 消息模板不存在或未启用错误码。 */
  private static final String ERROR_TEMPLATE_NOT_FOUND = "ZHYC_MESSAGE_TEMPLATE_NOT_FOUND";

  /** 模板变量缺失错误码。 */
  private static final String ERROR_TEMPLATE_VARIABLE_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_VARIABLE_REQUIRED";

  /** 消息模板编码为空错误码。 */
  private static final String ERROR_TEMPLATE_CODE_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_CODE_REQUIRED";

  /** 站内消息仓储。 */
  private final MsgMessageRepository messageRepository;
  /** 消息模板仓储，用于按模板发送站内消息。 */
  private final MsgTemplateRepository templateRepository;

  /**
   * 创建站内消息业务服务。
   *
   * @param messageRepository 站内消息仓储
   */
  public DefaultMsgMessageService(MsgMessageRepository messageRepository) {
    this.messageRepository = Objects.requireNonNull(messageRepository, "站内消息仓储不能为空");
    this.templateRepository = null;
  }

  /**
   * 创建支持模板发送的站内消息业务服务。
   *
   * @param messageRepository 站内消息仓储
   * @param templateRepository 消息模板仓储
   */
  @Autowired
  public DefaultMsgMessageService(MsgMessageRepository messageRepository,
      MsgTemplateRepository templateRepository) {
    this.messageRepository = Objects.requireNonNull(messageRepository, "站内消息仓储不能为空");
    this.templateRepository = Objects.requireNonNull(templateRepository, "消息模板仓储不能为空");
  }

  @Override
  @Transactional
  public String send(MsgMessageSendCommand command) {
    Objects.requireNonNull(command, "站内消息发送命令不能为空");
    String messageCode = nextMessageCode();
    MsgMessage message = new MsgMessage(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), messageCode,
        requirePositive(command.receiverId(), ERROR_RECEIVER_REQUIRED, "接收人用户 ID 不能为空"),
        trimToNull(command.receiverName()), requireMessageType(defaultText(command.messageType(), "notice")),
        requireText(command.title(), ERROR_TITLE_REQUIRED, "消息标题不能为空"),
        requireText(command.content(), ERROR_CONTENT_REQUIRED, "消息内容不能为空"),
        false, null, null);
    messageRepository.save(message);
    return messageCode;
  }

  @Override
  @Transactional
  public String sendByTemplate(MsgMessageTemplateSendCommand command) {
    Objects.requireNonNull(command, "站内消息模板发送命令不能为空");
    MsgTemplateRepository requiredTemplateRepository = Objects.requireNonNull(templateRepository,
        "消息模板仓储不能为空");
    String tenantId = requireText(command.tenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String templateCode = requireText(command.templateCode(), ERROR_TEMPLATE_CODE_REQUIRED,
        "消息模板编码不能为空");
    MsgTemplate template = requiredTemplateRepository.findEnabledByTenantIdAndTemplateCode(tenantId,
        templateCode);
    if (template == null) {
      throw new BusinessException(ERROR_TEMPLATE_NOT_FOUND, "消息模板不存在或未启用");
    }
    Map<String, String> variables = command.variables() == null ? Collections.emptyMap() : command.variables();
    return send(new MsgMessageSendCommand(tenantId, command.receiverId(), command.receiverName(),
        command.messageType(), renderTemplate(template.getTitleTemplate(), variables),
        renderTemplate(template.getContentTemplate(), variables)));
  }

  @Override
  public PageResult<MsgMessageResponse> listMessages(MsgMessageQuery query) {
    Objects.requireNonNull(query, "站内消息查询条件不能为空");
    MsgMessageQuery normalized = new MsgMessageQuery(requireText(query.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), requirePositive(query.receiverId(), ERROR_RECEIVER_REQUIRED,
        "接收人用户 ID 不能为空"), query.readFlag(),
        Math.max(query.pageNo(), 1), Math.min(Math.max(query.pageSize(), 1), 100));
    int offset = (normalized.pageNo() - 1) * normalized.pageSize();
    long total = messageRepository.countByQuery(normalized);
    List<MsgMessageResponse> records = messageRepository.findPageByQuery(normalized, offset).stream()
        .map(this::toResponse)
        .toList();
    return PageResult.of(total, normalized.pageNo(), normalized.pageSize(), records);
  }

  @Override
  @Transactional
  public void markRead(String tenantId, String messageCode, Long receiverId) {
    int updated = messageRepository.markRead(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireText(messageCode, ERROR_MESSAGE_CODE_REQUIRED, "消息编码不能为空"),
        requirePositive(receiverId, ERROR_RECEIVER_REQUIRED, "接收人用户 ID 不能为空"));
    if (updated == 0) {
      throw new BusinessException(ERROR_MESSAGE_NOT_FOUND, "消息不存在或无权读取");
    }
  }

  private MsgMessageResponse toResponse(MsgMessage message) {
    return new MsgMessageResponse(message.getId(), message.getTenantId(), message.getMessageCode(),
        message.getReceiverId(), message.getReceiverName(), message.getMessageType(), message.getTitle(),
        message.getContent(), message.isReadFlag(), message.getReadAt(), message.getCreatedAt());
  }

  private String nextMessageCode() {
    return "MSG" + LocalDateTime.now().format(CODE_DATE_FORMAT) + String.format("%04d", RANDOM.nextInt(10000));
  }

  /**
   * 渲染消息模板变量。
   *
   * @param templateContent 模板内容
   * @param variables 模板变量
   * @return 渲染后的消息内容
   */
  private String renderTemplate(String templateContent, Map<String, String> variables) {
    Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(templateContent);
    StringBuilder rendered = new StringBuilder();
    while (matcher.find()) {
      String variableName = matcher.group(1);
      String variableValue = variables.get(variableName);
      if (variableValue == null) {
        throw new BusinessException(ERROR_TEMPLATE_VARIABLE_REQUIRED,
            "消息模板变量不能为空: " + variableName);
      }
      matcher.appendReplacement(rendered, Matcher.quoteReplacement(variableValue));
    }
    matcher.appendTail(rendered);
    return rendered.toString();
  }

  private Long requirePositive(Long value, String code, String message) {
    if (value == null || value <= 0) {
      throw new BusinessException(code, message);
    }
    return value;
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
   * 校验站内消息类型必须属于首期支持范围。
   *
   * @param value 原始消息类型
   * @return 小写规范化后的消息类型
   */
  private String requireMessageType(String value) {
    String normalized = requireText(value, ERROR_MESSAGE_TYPE_REQUIRED, "消息类型不能为空").toLowerCase(Locale.ROOT);
    if (!MESSAGE_TYPES.contains(normalized)) {
      throw new BusinessException(ERROR_MESSAGE_TYPE_UNSUPPORTED, "消息类型不支持: " + normalized);
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
