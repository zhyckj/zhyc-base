/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.service;

import com.zhyc.cms.domain.CmsChannel;
import com.zhyc.cms.domain.CmsContent;
import com.zhyc.cms.repository.CmsRepository;
import com.zhyc.common.exception.BusinessException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认内容管理业务服务实现。
 */
@Service
public class DefaultCmsService implements CmsService {

  /** 首期支持的内容栏目启停状态。 */
  private static final Set<String> CHANNEL_STATUSES = Set.of("enabled", "disabled");
  /** 首期支持的内容文章发布状态。 */
  private static final Set<String> CONTENT_STATUSES = Set.of("draft", "published", "offline");

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_CMS_TENANT_REQUIRED";

  /** 内容栏目编码为空错误码。 */
  private static final String ERROR_CHANNEL_CODE_REQUIRED = "ZHYC_CMS_CHANNEL_CODE_REQUIRED";

  /** 内容栏目名称为空错误码。 */
  private static final String ERROR_CHANNEL_NAME_REQUIRED = "ZHYC_CMS_CHANNEL_NAME_REQUIRED";

  /** 内容栏目状态为空错误码。 */
  private static final String ERROR_CHANNEL_STATUS_REQUIRED = "ZHYC_CMS_CHANNEL_STATUS_REQUIRED";

  /** 内容栏目状态不支持错误码。 */
  private static final String ERROR_CHANNEL_STATUS_UNSUPPORTED = "ZHYC_CMS_CHANNEL_STATUS_UNSUPPORTED";

  /** 内容文章主键为空错误码。 */
  private static final String ERROR_CONTENT_ID_REQUIRED = "ZHYC_CMS_CONTENT_ID_REQUIRED";

  /** 内容文章标题为空错误码。 */
  private static final String ERROR_CONTENT_TITLE_REQUIRED = "ZHYC_CMS_CONTENT_TITLE_REQUIRED";

  /** 内容文章状态为空错误码。 */
  private static final String ERROR_CONTENT_STATUS_REQUIRED = "ZHYC_CMS_CONTENT_STATUS_REQUIRED";

  /** 内容文章状态不支持错误码。 */
  private static final String ERROR_CONTENT_STATUS_UNSUPPORTED = "ZHYC_CMS_CONTENT_STATUS_UNSUPPORTED";

  /** 内容管理仓储。 */
  private final CmsRepository cmsRepository;

  /**
   * 创建内容管理业务服务。
   *
   * @param cmsRepository 内容管理仓储
   */
  public DefaultCmsService(CmsRepository cmsRepository) {
    this.cmsRepository = Objects.requireNonNull(cmsRepository, "内容管理仓储不能为空");
  }

  @Override
  public List<CmsChannelResponse> listChannels(String tenantId, String status) {
    return cmsRepository.findChannels(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(status)).stream()
        .map(this::toChannelResponse)
        .toList();
  }

  @Override
  @Transactional
  public void saveChannel(CmsChannelSaveCommand command) {
    Objects.requireNonNull(command, "内容栏目保存命令不能为空");
    cmsRepository.saveChannel(new CmsChannel(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), command.parentId(),
        requireText(command.channelCode(), ERROR_CHANNEL_CODE_REQUIRED, "栏目编码不能为空"),
        requireText(command.channelName(), ERROR_CHANNEL_NAME_REQUIRED, "栏目名称不能为空"),
        command.sortOrder() == null ? 0 : command.sortOrder(),
        requireChannelStatus(defaultText(command.status(), "enabled")), null, null));
  }

  @Override
  public List<CmsContentResponse> listContents(String tenantId, String channelCode, String status) {
    return cmsRepository.findContents(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(channelCode), trimToNull(status)).stream().map(this::toContentResponse).toList();
  }

  @Override
  @Transactional
  public void saveContent(CmsContentSaveCommand command) {
    Objects.requireNonNull(command, "内容文章保存命令不能为空");
    cmsRepository.saveContent(new CmsContent(normalizeOptionalId(command.id()), requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), requireText(command.channelCode(), ERROR_CHANNEL_CODE_REQUIRED, "栏目编码不能为空"),
        requireText(command.title(), ERROR_CONTENT_TITLE_REQUIRED, "文章标题不能为空"),
        trimToNull(command.summary()), trimToNull(command.bodyContent()),
        requireContentStatus(defaultText(command.status(), "draft")), command.authorId(), null, null));
  }

  @Override
  @Transactional
  public void updateContentStatus(String tenantId, Long id, String status) {
    cmsRepository.updateContentStatus(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireId(id, ERROR_CONTENT_ID_REQUIRED, "文章主键不能为空"), requireContentStatus(status));
  }

  private CmsChannelResponse toChannelResponse(CmsChannel channel) {
    return new CmsChannelResponse(channel.getId(), channel.getTenantId(), channel.getParentId(),
        channel.getChannelCode(), channel.getChannelName(), channel.getSortOrder(), channel.getStatus(),
        channel.getCreatedAt(), channel.getUpdatedAt());
  }

  private CmsContentResponse toContentResponse(CmsContent content) {
    return new CmsContentResponse(content.getId(), content.getTenantId(), content.getChannelCode(),
        content.getTitle(), content.getSummary(), content.getBodyContent(), content.getStatus(),
        content.getAuthorId(), content.getCreatedAt(), content.getUpdatedAt());
  }

  private Long requireId(Long value, String code, String message) {
    if (value == null || value <= 0) {
      throw new BusinessException(code, message);
    }
    return value;
  }

  private Long normalizeOptionalId(Long value) {
    if (value == null) {
      return null;
    }
    return requireId(value, ERROR_CONTENT_ID_REQUIRED, "文章主键不能为空");
  }

  /**
   * 校验内容栏目状态必须属于首期支持范围。
   *
   * @param value 原始栏目状态
   * @return 小写规范化后的栏目状态
   */
  private String requireChannelStatus(String value) {
    String normalized = requireText(value, ERROR_CHANNEL_STATUS_REQUIRED, "栏目状态不能为空").toLowerCase(Locale.ROOT);
    if (!CHANNEL_STATUSES.contains(normalized)) {
      throw new BusinessException(ERROR_CHANNEL_STATUS_UNSUPPORTED, "栏目状态不支持: " + normalized);
    }
    return normalized;
  }

  /**
   * 校验内容文章状态必须属于首期支持范围。
   *
   * @param value 原始文章状态
   * @return 小写规范化后的文章状态
   */
  private String requireContentStatus(String value) {
    String normalized = requireText(value, ERROR_CONTENT_STATUS_REQUIRED, "文章状态不能为空").toLowerCase(Locale.ROOT);
    if (!CONTENT_STATUSES.contains(normalized)) {
      throw new BusinessException(ERROR_CONTENT_STATUS_UNSUPPORTED, "文章状态不支持: " + normalized);
    }
    return normalized;
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

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
