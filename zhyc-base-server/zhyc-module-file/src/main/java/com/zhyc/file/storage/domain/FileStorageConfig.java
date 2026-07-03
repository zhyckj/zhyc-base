/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.domain;

import java.time.LocalDateTime;

/**
 * 文件存储配置领域模型。
 *
 * <p>用于描述租户内本地存储、MinIO、OSS、S3 等对象存储接入配置。</p>
 */
public class FileStorageConfig {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 存储配置编码。 */
  private String storageCode;
  /** 存储配置名称。 */
  private String storageName;
  /** 存储类型。 */
  private String storageType;
  /** 存储端点或本地根路径。 */
  private String endpoint;
  /** 配置状态。 */
  private String status;
  /** 是否默认存储配置。 */
  private boolean defaultFlag;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建空文件存储配置对象。
   */
  public FileStorageConfig() {
  }

  /**
   * 创建完整文件存储配置对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param storageCode 存储配置编码
   * @param storageName 存储配置名称
   * @param storageType 存储类型
   * @param endpoint 存储端点或本地根路径
   * @param status 配置状态
   * @param defaultFlag 是否默认存储配置
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public FileStorageConfig(Long id, String tenantId, String storageCode, String storageName,
      String storageType, String endpoint, String status, boolean defaultFlag,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.storageCode = storageCode;
    this.storageName = storageName;
    this.storageType = storageType;
    this.endpoint = endpoint;
    this.status = status;
    this.defaultFlag = defaultFlag;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** @return 数据库主键 */
  public Long getId() {
    return id;
  }

  /** @param id 数据库主键 */
  public void setId(Long id) {
    this.id = id;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @param tenantId 租户业务编码 */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  /** @return 存储配置编码 */
  public String getStorageCode() {
    return storageCode;
  }

  /** @param storageCode 存储配置编码 */
  public void setStorageCode(String storageCode) {
    this.storageCode = storageCode;
  }

  /** @return 存储配置名称 */
  public String getStorageName() {
    return storageName;
  }

  /** @param storageName 存储配置名称 */
  public void setStorageName(String storageName) {
    this.storageName = storageName;
  }

  /** @return 存储类型 */
  public String getStorageType() {
    return storageType;
  }

  /** @param storageType 存储类型 */
  public void setStorageType(String storageType) {
    this.storageType = storageType;
  }

  /** @return 存储端点或本地根路径 */
  public String getEndpoint() {
    return endpoint;
  }

  /** @param endpoint 存储端点或本地根路径 */
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  /** @return 配置状态 */
  public String getStatus() {
    return status;
  }

  /** @param status 配置状态 */
  public void setStatus(String status) {
    this.status = status;
  }

  /** @return 默认配置返回 {@code true} */
  public boolean isDefaultFlag() {
    return defaultFlag;
  }

  /** @param defaultFlag 是否默认存储配置 */
  public void setDefaultFlag(boolean defaultFlag) {
    this.defaultFlag = defaultFlag;
  }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /** @param createdAt 创建时间 */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /** @return 更新时间 */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /** @param updatedAt 更新时间 */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
