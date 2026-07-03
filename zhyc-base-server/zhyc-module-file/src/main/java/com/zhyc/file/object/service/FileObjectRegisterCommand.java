/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.service;

/**
 * 文件对象登记命令。
 *
 * @param tenantId 租户业务编码
 * @param storageCode 存储配置编码
 * @param originalName 原始文件名
 * @param contentType 文件内容类型
 * @param fileSize 文件大小，单位字节
 * @param objectKey 存储对象键或相对路径
 * @param uploaderId 上传人用户 ID
 */
public record FileObjectRegisterCommand(String tenantId, String storageCode, String originalName,
    String contentType, Long fileSize, String objectKey, Long uploaderId) {
}
