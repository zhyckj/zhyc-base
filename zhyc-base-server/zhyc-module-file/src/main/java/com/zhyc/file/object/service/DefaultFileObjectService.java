/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.service;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.object.domain.FileObject;
import com.zhyc.file.object.repository.FileObjectRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认文件对象业务服务实现。
 */
@Service
public class DefaultFileObjectService implements FileObjectService {

  /** 文件编码日期格式。 */
  private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  /** 文件编码随机数生成器。 */
  private static final SecureRandom RANDOM = new SecureRandom();

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_FILE_OBJECT_TENANT_REQUIRED";

  /** 存储配置编码为空错误码。 */
  private static final String ERROR_STORAGE_CODE_REQUIRED = "ZHYC_FILE_OBJECT_STORAGE_CODE_REQUIRED";

  /** 原始文件名为空错误码。 */
  private static final String ERROR_ORIGINAL_NAME_REQUIRED = "ZHYC_FILE_OBJECT_ORIGINAL_NAME_REQUIRED";

  /** 文件大小非法错误码。 */
  private static final String ERROR_FILE_SIZE_INVALID = "ZHYC_FILE_OBJECT_SIZE_INVALID";

  /** 对象键为空错误码。 */
  private static final String ERROR_OBJECT_KEY_REQUIRED = "ZHYC_FILE_OBJECT_KEY_REQUIRED";

  /** 文件对象仓储。 */
  private final FileObjectRepository fileObjectRepository;

  /**
   * 创建文件对象业务服务。
   *
   * @param fileObjectRepository 文件对象仓储
   */
  public DefaultFileObjectService(FileObjectRepository fileObjectRepository) {
    this.fileObjectRepository = Objects.requireNonNull(fileObjectRepository, "文件对象仓储不能为空");
  }

  @Override
  @Transactional
  public String register(FileObjectRegisterCommand command) {
    Objects.requireNonNull(command, "文件对象登记命令不能为空");
    String fileCode = nextFileCode();
    FileObject fileObject = new FileObject(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), fileCode,
        requireText(command.storageCode(), ERROR_STORAGE_CODE_REQUIRED, "存储配置编码不能为空"),
        requireText(command.originalName(), ERROR_ORIGINAL_NAME_REQUIRED, "原始文件名不能为空"),
        defaultText(command.contentType(), "application/octet-stream"),
        requireNonNegative(command.fileSize(), ERROR_FILE_SIZE_INVALID, "文件大小不能为空"),
        requireText(command.objectKey(), ERROR_OBJECT_KEY_REQUIRED, "对象键不能为空"),
        "stored", command.uploaderId(), null);
    fileObjectRepository.save(fileObject);
    return fileCode;
  }

  @Override
  public PageResult<FileObjectResponse> listFiles(FileObjectQuery query) {
    Objects.requireNonNull(query, "文件对象查询条件不能为空");
    FileObjectQuery normalized = new FileObjectQuery(requireText(query.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"),
        trimToNull(query.keyword()), Math.max(query.pageNo(), 1), Math.min(Math.max(query.pageSize(), 1), 100));
    int offset = (normalized.pageNo() - 1) * normalized.pageSize();
    long total = fileObjectRepository.countByQuery(normalized);
    List<FileObjectResponse> records = fileObjectRepository.findPageByQuery(normalized, offset).stream()
        .map(this::toResponse)
        .toList();
    return PageResult.of(total, normalized.pageNo(), normalized.pageSize(), records);
  }

  private FileObjectResponse toResponse(FileObject fileObject) {
    return new FileObjectResponse(fileObject.getId(), fileObject.getTenantId(), fileObject.getFileCode(),
        fileObject.getStorageCode(), fileObject.getOriginalName(), fileObject.getContentType(),
        fileObject.getFileSize(), fileObject.getObjectKey(), fileObject.getFileStatus(),
        fileObject.getUploaderId(), fileObject.getCreatedAt());
  }

  private String nextFileCode() {
    return "FILE" + LocalDateTime.now().format(CODE_DATE_FORMAT) + String.format("%04d", RANDOM.nextInt(10000));
  }

  private Long requireNonNegative(Long value, String code, String message) {
    if (value == null || value < 0) {
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

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
