/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.storage.domain.FileStorageConfig;
import com.zhyc.file.storage.repository.FileStorageConfigRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 本地文件对象上传服务实现。
 */
@Service
public class LocalFileObjectUploadService implements FileObjectUploadService {

  /** 默认本地存储配置编码。 */
  private static final String DEFAULT_STORAGE_CODE = "local-default";
  /** 默认本地上传根目录。 */
  private static final String DEFAULT_UPLOAD_ROOT = "data/uploads";
  /** 日期目录格式。 */
  private static final DateTimeFormatter OBJECT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
  /** 最大上传文件大小，100MB。 */
  private static final long MAX_UPLOAD_SIZE = 100L * 1024L * 1024L;
  /** 支持的文件扩展名。 */
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "gif", "webp",
      "txt", "csv", "json", "xml", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip", "rar");
  /** 支持的固定内容类型。 */
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("application/pdf", "application/json",
      "application/xml", "application/zip", "application/x-rar-compressed", "application/octet-stream",
      "application/msword", "application/vnd.ms-excel", "application/vnd.ms-powerpoint",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation");

  /** 上传文件为空错误码。 */
  private static final String ERROR_UPLOAD_EMPTY = "ZHYC_FILE_OBJECT_UPLOAD_EMPTY";
  /** 上传文件过大错误码。 */
  private static final String ERROR_UPLOAD_TOO_LARGE = "ZHYC_FILE_OBJECT_UPLOAD_TOO_LARGE";
  /** 上传文件名非法错误码。 */
  private static final String ERROR_UPLOAD_NAME_INVALID = "ZHYC_FILE_OBJECT_UPLOAD_NAME_INVALID";
  /** 上传文件类型不支持错误码。 */
  private static final String ERROR_UPLOAD_TYPE_UNSUPPORTED = "ZHYC_FILE_OBJECT_UPLOAD_TYPE_UNSUPPORTED";
  /** 文件存储类型不支持错误码。 */
  private static final String ERROR_STORAGE_TYPE_UNSUPPORTED = "ZHYC_FILE_OBJECT_STORAGE_TYPE_UNSUPPORTED";
  /** 文件存储配置不可用错误码。 */
  private static final String ERROR_STORAGE_UNAVAILABLE = "ZHYC_FILE_OBJECT_STORAGE_UNAVAILABLE";
  /** 文件写入失败错误码。 */
  private static final String ERROR_UPLOAD_WRITE_FAILED = "ZHYC_FILE_OBJECT_UPLOAD_WRITE_FAILED";

  /** 文件对象服务。 */
  private final FileObjectService fileObjectService;
  /** 文件存储配置仓储。 */
  private final FileStorageConfigRepository storageConfigRepository;

  /**
   * 创建本地文件对象上传服务。
   *
   * @param fileObjectService 文件对象服务
   * @param storageConfigRepository 文件存储配置仓储
   */
  public LocalFileObjectUploadService(FileObjectService fileObjectService,
      FileStorageConfigRepository storageConfigRepository) {
    this.fileObjectService = Objects.requireNonNull(fileObjectService, "文件对象服务不能为空");
    this.storageConfigRepository = Objects.requireNonNull(storageConfigRepository, "文件存储配置仓储不能为空");
  }

  @Override
  @Transactional
  public FileObjectUploadResponse upload(FileObjectUploadCommand command) {
    Objects.requireNonNull(command, "文件对象上传命令不能为空");
    String tenantId = requireText(command.tenantId(), "ZHYC_FILE_OBJECT_TENANT_REQUIRED", "租户业务编码不能为空");
    MultipartFile file = requireFile(command.file());
    String originalName = normalizeOriginalName(file.getOriginalFilename());
    String extension = requireSupportedExtension(originalName);
    String contentType = requireSupportedContentType(file.getContentType());
    StorageTarget storageTarget = resolveStorageTarget(tenantId, trimToNull(command.storageCode()));
    String objectKey = buildObjectKey(tenantId, extension);
    Path targetPath = storageTarget.rootPath().resolve(objectKey).normalize();
    if (!targetPath.startsWith(storageTarget.rootPath())) {
      throw new BusinessException(ERROR_UPLOAD_NAME_INVALID, "文件路径非法");
    }

    storeFile(file, targetPath);
    try {
      String fileCode = fileObjectService.register(new FileObjectRegisterCommand(tenantId, storageTarget.storageCode(),
          originalName, contentType, file.getSize(), objectKey, command.uploaderId()));
      return new FileObjectUploadResponse(fileCode, storageTarget.storageCode(), objectKey, originalName,
          contentType, file.getSize());
    } catch (RuntimeException ex) {
      deleteQuietly(targetPath);
      throw ex;
    }
  }

  private MultipartFile requireFile(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getSize() <= 0) {
      throw new BusinessException(ERROR_UPLOAD_EMPTY, "上传文件不能为空");
    }
    if (file.getSize() > MAX_UPLOAD_SIZE) {
      throw new BusinessException(ERROR_UPLOAD_TOO_LARGE, "上传文件不能超过 100MB");
    }
    return file;
  }

  private String normalizeOriginalName(String originalFilename) {
    String filename = requireText(originalFilename, ERROR_UPLOAD_NAME_INVALID, "上传文件名不能为空")
        .replace('\\', '/');
    String basename = filename.substring(filename.lastIndexOf('/') + 1).trim();
    if (basename.isEmpty() || ".".equals(basename) || "..".equals(basename)) {
      throw new BusinessException(ERROR_UPLOAD_NAME_INVALID, "上传文件名非法");
    }
    return basename;
  }

  private String requireSupportedExtension(String originalName) {
    int dotIndex = originalName.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == originalName.length() - 1) {
      throw new BusinessException(ERROR_UPLOAD_NAME_INVALID, "上传文件必须包含扩展名");
    }
    String extension = originalName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new BusinessException(ERROR_UPLOAD_TYPE_UNSUPPORTED, "上传文件扩展名不支持: " + extension);
    }
    return extension;
  }

  private String requireSupportedContentType(String contentType) {
    String normalized = defaultText(contentType, "application/octet-stream").toLowerCase(Locale.ROOT);
    if (normalized.startsWith("image/") || normalized.startsWith("text/")
        || ALLOWED_CONTENT_TYPES.contains(normalized)) {
      return normalized;
    }
    throw new BusinessException(ERROR_UPLOAD_TYPE_UNSUPPORTED, "上传文件内容类型不支持: " + normalized);
  }

  private StorageTarget resolveStorageTarget(String tenantId, String storageCode) {
    var enabledConfigs = storageConfigRepository.findByTenantId(tenantId).stream()
        .filter(config -> "enabled".equalsIgnoreCase(config.getStatus()))
        .toList();
    if (storageCode != null) {
      return enabledConfigs.stream()
          .filter(config -> storageCode.equals(config.getStorageCode()))
          .findFirst()
          .map(this::toStorageTarget)
          .orElseThrow(() -> new BusinessException(ERROR_STORAGE_UNAVAILABLE, "存储配置不存在或未启用"));
    }
    return enabledConfigs.stream()
        .filter(this::isLocalStorage)
        .filter(config -> config.isDefaultFlag() || DEFAULT_STORAGE_CODE.equals(config.getStorageCode()))
        .findFirst()
        .or(() -> enabledConfigs.stream().filter(this::isLocalStorage).findFirst())
        .map(this::toStorageTarget)
        .orElseGet(() -> new StorageTarget(DEFAULT_STORAGE_CODE,
            Path.of(DEFAULT_UPLOAD_ROOT).toAbsolutePath().normalize()));
  }

  private boolean isLocalStorage(FileStorageConfig config) {
    return "local".equalsIgnoreCase(config.getStorageType());
  }

  private StorageTarget toStorageTarget(FileStorageConfig config) {
    if (!isLocalStorage(config)) {
      throw new BusinessException(ERROR_STORAGE_TYPE_UNSUPPORTED, "当前上传仅支持本地存储");
    }
    return new StorageTarget(config.getStorageCode(), Path.of(requireText(config.getEndpoint(),
        ERROR_STORAGE_TYPE_UNSUPPORTED, "本地存储根路径不能为空")).toAbsolutePath().normalize());
  }

  private String buildObjectKey(String tenantId, String extension) {
    String safeTenantId = tenantId.replaceAll("[^A-Za-z0-9_-]", "_");
    return safeTenantId + "/" + LocalDate.now().format(OBJECT_DATE_FORMAT) + "/"
        + UUID.randomUUID().toString().replace("-", "") + "." + extension;
  }

  private void storeFile(MultipartFile file, Path targetPath) {
    try {
      Files.createDirectories(targetPath.getParent());
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, targetPath);
      }
    } catch (IOException ex) {
      throw new BusinessException(ERROR_UPLOAD_WRITE_FAILED, "上传文件写入失败");
    }
  }

  private void deleteQuietly(Path targetPath) {
    try {
      Files.deleteIfExists(targetPath);
    } catch (IOException ignored) {
      // 登记失败时尽力清理已上传文件，清理失败不覆盖真实业务异常。
    }
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

  /**
   * 文件上传落盘目标。
   *
   * @param storageCode 存储配置编码
   * @param rootPath 本地存储根路径
   */
  private record StorageTarget(String storageCode, Path rootPath) {
  }
}
