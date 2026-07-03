/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.storage.domain.FileStorageConfig;
import com.zhyc.file.storage.repository.FileStorageConfigRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认文件存储配置业务服务实现。
 */
@Service
public class DefaultFileStorageConfigService implements FileStorageConfigService {

  /** 首期支持的文件存储类型。 */
  private static final Set<String> STORAGE_TYPES = Set.of("local", "s3", "minio", "oss");
  /** 首期支持的文件存储配置启停状态。 */
  private static final Set<String> CONFIG_STATUSES = Set.of("enabled", "disabled");

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_FILE_STORAGE_TENANT_REQUIRED";

  /** 存储配置编码为空错误码。 */
  private static final String ERROR_STORAGE_CODE_REQUIRED = "ZHYC_FILE_STORAGE_CODE_REQUIRED";

  /** 存储配置名称为空错误码。 */
  private static final String ERROR_STORAGE_NAME_REQUIRED = "ZHYC_FILE_STORAGE_NAME_REQUIRED";

  /** 存储类型为空错误码。 */
  private static final String ERROR_STORAGE_TYPE_REQUIRED = "ZHYC_FILE_STORAGE_TYPE_REQUIRED";

  /** 存储类型不支持错误码。 */
  private static final String ERROR_STORAGE_TYPE_UNSUPPORTED = "ZHYC_FILE_STORAGE_TYPE_UNSUPPORTED";

  /** 存储端点为空错误码。 */
  private static final String ERROR_ENDPOINT_REQUIRED = "ZHYC_FILE_STORAGE_ENDPOINT_REQUIRED";

  /** 存储配置状态为空错误码。 */
  private static final String ERROR_STATUS_REQUIRED = "ZHYC_FILE_STORAGE_STATUS_REQUIRED";

  /** 存储配置状态不支持错误码。 */
  private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_FILE_STORAGE_STATUS_UNSUPPORTED";

  /** 文件存储配置仓储。 */
  private final FileStorageConfigRepository configRepository;

  /**
   * 创建文件存储配置业务服务。
   *
   * @param configRepository 文件存储配置仓储
   */
  public DefaultFileStorageConfigService(FileStorageConfigRepository configRepository) {
    this.configRepository = Objects.requireNonNull(configRepository, "文件存储配置仓储不能为空");
  }

  @Override
  public List<FileStorageConfigResponse> listConfigs(String tenantId) {
    String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    return configRepository.findByTenantId(requiredTenantId).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public void save(FileStorageConfigSaveCommand command) {
    Objects.requireNonNull(command, "文件存储配置保存命令不能为空");
    FileStorageConfig config = new FileStorageConfig(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), requireText(command.storageCode(), ERROR_STORAGE_CODE_REQUIRED, "存储配置编码不能为空"),
        requireText(command.storageName(), ERROR_STORAGE_NAME_REQUIRED, "存储配置名称不能为空"),
        requireStorageType(command.storageType()),
        requireText(command.endpoint(), ERROR_ENDPOINT_REQUIRED, "存储端点不能为空"),
        requireStatus(defaultText(command.status(), "enabled")), command.defaultFlag(), null, null);
    configRepository.save(config);
  }

  private FileStorageConfigResponse toResponse(FileStorageConfig config) {
    return new FileStorageConfigResponse(config.getId(), config.getTenantId(), config.getStorageCode(),
        config.getStorageName(), config.getStorageType(), config.getEndpoint(), config.getStatus(),
        config.isDefaultFlag(), config.getCreatedAt(), config.getUpdatedAt());
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
   * 校验文件存储类型必须属于首期支持范围。
   *
   * @param value 原始存储类型
   * @return 小写规范化后的存储类型
   */
  private String requireStorageType(String value) {
    String normalized = requireText(value, ERROR_STORAGE_TYPE_REQUIRED, "存储类型不能为空").toLowerCase(Locale.ROOT);
    if (!STORAGE_TYPES.contains(normalized)) {
      throw new BusinessException(ERROR_STORAGE_TYPE_UNSUPPORTED, "存储类型不支持: " + normalized);
    }
    return normalized;
  }

  /**
   * 校验文件存储配置状态必须属于首期支持范围。
   *
   * @param value 原始配置状态
   * @return 小写规范化后的配置状态
   */
  private String requireStatus(String value) {
    String normalized = requireText(value, ERROR_STATUS_REQUIRED, "配置状态不能为空").toLowerCase(Locale.ROOT);
    if (!CONFIG_STATUSES.contains(normalized)) {
      throw new BusinessException(ERROR_STATUS_UNSUPPORTED, "配置状态不支持: " + normalized);
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
