/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.domain;

import java.time.LocalDateTime;

/**
 * 文件对象领域模型。
 */
public class FileObject {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 文件业务编码。 */
  private String fileCode;
  /** 存储配置编码。 */
  private String storageCode;
  /** 原始文件名。 */
  private String originalName;
  /** 文件内容类型。 */
  private String contentType;
  /** 文件大小，单位字节。 */
  private Long fileSize;
  /** 存储对象键或相对路径。 */
  private String objectKey;
  /** 文件状态。 */
  private String fileStatus;
  /** 上传人用户 ID。 */
  private Long uploaderId;
  /** 创建时间。 */
  private LocalDateTime createdAt;

  /**
   * 创建空文件对象。
   */
  public FileObject() {
  }

  /**
   * 创建完整文件对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param fileCode 文件业务编码
   * @param storageCode 存储配置编码
   * @param originalName 原始文件名
   * @param contentType 文件内容类型
   * @param fileSize 文件大小，单位字节
   * @param objectKey 存储对象键或相对路径
   * @param fileStatus 文件状态
   * @param uploaderId 上传人用户 ID
   * @param createdAt 创建时间
   */
  public FileObject(Long id, String tenantId, String fileCode, String storageCode, String originalName,
      String contentType, Long fileSize, String objectKey, String fileStatus, Long uploaderId,
      LocalDateTime createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.fileCode = fileCode;
    this.storageCode = storageCode;
    this.originalName = originalName;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.objectKey = objectKey;
    this.fileStatus = fileStatus;
    this.uploaderId = uploaderId;
    this.createdAt = createdAt;
  }

  /** @return 数据库主键 */
  public Long getId() { return id; }

  /** @param id 数据库主键 */
  public void setId(Long id) { this.id = id; }

  /** @return 租户业务编码 */
  public String getTenantId() { return tenantId; }

  /** @param tenantId 租户业务编码 */
  public void setTenantId(String tenantId) { this.tenantId = tenantId; }

  /** @return 文件业务编码 */
  public String getFileCode() { return fileCode; }

  /** @param fileCode 文件业务编码 */
  public void setFileCode(String fileCode) { this.fileCode = fileCode; }

  /** @return 存储配置编码 */
  public String getStorageCode() { return storageCode; }

  /** @param storageCode 存储配置编码 */
  public void setStorageCode(String storageCode) { this.storageCode = storageCode; }

  /** @return 原始文件名 */
  public String getOriginalName() { return originalName; }

  /** @param originalName 原始文件名 */
  public void setOriginalName(String originalName) { this.originalName = originalName; }

  /** @return 文件内容类型 */
  public String getContentType() { return contentType; }

  /** @param contentType 文件内容类型 */
  public void setContentType(String contentType) { this.contentType = contentType; }

  /** @return 文件大小，单位字节 */
  public Long getFileSize() { return fileSize; }

  /** @param fileSize 文件大小，单位字节 */
  public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

  /** @return 存储对象键或相对路径 */
  public String getObjectKey() { return objectKey; }

  /** @param objectKey 存储对象键或相对路径 */
  public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

  /** @return 文件状态 */
  public String getFileStatus() { return fileStatus; }

  /** @param fileStatus 文件状态 */
  public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }

  /** @return 上传人用户 ID */
  public Long getUploaderId() { return uploaderId; }

  /** @param uploaderId 上传人用户 ID */
  public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() { return createdAt; }

  /** @param createdAt 创建时间 */
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
