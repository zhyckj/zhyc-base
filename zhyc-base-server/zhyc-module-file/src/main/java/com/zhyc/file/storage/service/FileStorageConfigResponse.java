/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.service;

import java.time.LocalDateTime;

/**
 * 文件存储配置响应对象。
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
public record FileStorageConfigResponse(Long id, String tenantId, String storageCode, String storageName,
    String storageType, String endpoint, String status, boolean defaultFlag, LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
