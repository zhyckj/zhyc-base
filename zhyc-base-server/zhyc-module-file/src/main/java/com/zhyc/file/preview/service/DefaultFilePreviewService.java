/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.preview.domain.FilePreviewLog;
import com.zhyc.file.preview.repository.FilePreviewRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认文件预览业务服务实现。
 */
@Service
public class DefaultFilePreviewService implements FilePreviewService {

  /** 首期支持的文件预览类型。 */
  private static final Set<String> PREVIEW_TYPES = Set.of("default", "pdf", "image", "text", "office");

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_FILE_PREVIEW_TENANT_REQUIRED";

  /** 文件业务编码为空错误码。 */
  private static final String ERROR_FILE_CODE_REQUIRED = "ZHYC_FILE_PREVIEW_FILE_CODE_REQUIRED";

  /** 预览类型为空错误码。 */
  private static final String ERROR_PREVIEW_TYPE_REQUIRED = "ZHYC_FILE_PREVIEW_TYPE_REQUIRED";

  /** 预览类型不支持错误码。 */
  private static final String ERROR_PREVIEW_TYPE_UNSUPPORTED = "ZHYC_FILE_PREVIEW_TYPE_UNSUPPORTED";

  /** 文件预览仓储。 */
  private final FilePreviewRepository filePreviewRepository;

  /**
   * 创建文件预览业务服务。
   *
   * @param filePreviewRepository 文件预览仓储
   */
  public DefaultFilePreviewService(FilePreviewRepository filePreviewRepository) {
    this.filePreviewRepository = Objects.requireNonNull(filePreviewRepository, "文件预览仓储不能为空");
  }

  @Override
  @Transactional
  public FilePreviewResponse createPreview(FilePreviewCreateCommand command) {
    Objects.requireNonNull(command, "文件预览创建命令不能为空");
    String tenantId = requireText(command.tenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String fileCode = requireText(command.fileCode(), ERROR_FILE_CODE_REQUIRED, "文件业务编码不能为空");
    String previewType = requirePreviewType(defaultText(command.previewType(), "default"));
    String previewUrl = buildPreviewUrl(fileCode, previewType);
    saveSuccessLog(tenantId, fileCode, previewType, previewUrl);
    return new FilePreviewResponse(fileCode, previewType, previewUrl, "success");
  }

  @Override
  @Transactional
  public FilePreviewRenderResponse renderPreview(String tenantId, String fileCode, String previewType) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedFileCode = requireText(fileCode, ERROR_FILE_CODE_REQUIRED, "文件业务编码不能为空");
    String normalizedPreviewType = requirePreviewType(defaultText(previewType, "default"));
    String previewUrl = buildPreviewUrl(normalizedFileCode, normalizedPreviewType);
    saveSuccessLog(normalizedTenantId, normalizedFileCode, normalizedPreviewType, previewUrl);
    return new FilePreviewRenderResponse(normalizedFileCode, normalizedPreviewType, previewUrl, "success");
  }

  @Override
  public List<FilePreviewLogResponse> listPreviewLogs(FilePreviewLogQuery query) {
    Objects.requireNonNull(query, "文件预览日志查询条件不能为空");
    FilePreviewLogQuery normalized = new FilePreviewLogQuery(
        requireText(query.tenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"), trimToNull(query.fileCode()));
    return filePreviewRepository.findLogs(normalized).stream().map(this::toResponse).toList();
  }

  /**
   * 校验文件预览类型必须属于首期支持范围。
   *
   * @param value 原始预览类型
   * @return 小写规范化后的预览类型
   */
  private String requirePreviewType(String value) {
    String normalized = requireText(value, ERROR_PREVIEW_TYPE_REQUIRED, "预览类型不能为空").toLowerCase(Locale.ROOT);
    if (!PREVIEW_TYPES.contains(normalized)) {
      throw new BusinessException(ERROR_PREVIEW_TYPE_UNSUPPORTED, "预览类型不支持: " + normalized);
    }
    return normalized;
  }

  private FilePreviewLogResponse toResponse(FilePreviewLog log) {
    return new FilePreviewLogResponse(log.getId(), log.getTenantId(), log.getFileCode(),
        log.getPreviewType(), log.getPreviewUrl(), log.getResult(), log.getCostMs(), log.getCreatedAt());
  }

  private void saveSuccessLog(String tenantId, String fileCode, String previewType, String previewUrl) {
    filePreviewRepository.saveLog(new FilePreviewLog(null, tenantId, fileCode, previewType,
        previewUrl, "success", 1L, null));
  }

  private String buildPreviewUrl(String fileCode, String previewType) {
    return "/file/preview/render/" + fileCode + "?type=" + previewType;
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
