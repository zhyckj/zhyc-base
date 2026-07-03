/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件对象上传命令。
 *
 * @param tenantId 租户业务编码
 * @param storageCode 存储配置编码
 * @param uploaderId 上传人用户 ID
 * @param file 上传文件
 */
public record FileObjectUploadCommand(String tenantId, String storageCode, Long uploaderId, MultipartFile file) {
}
