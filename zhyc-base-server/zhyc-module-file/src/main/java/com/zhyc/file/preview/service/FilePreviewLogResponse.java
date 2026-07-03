/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.service;

import java.time.LocalDateTime;

/**
 * 文件预览日志响应。
 *
 * @param id 预览日志主键
 * @param tenantId 租户业务编码
 * @param fileCode 文件业务编码
 * @param previewType 预览类型
 * @param previewUrl 预览访问地址
 * @param result 预览结果
 * @param costMs 预览耗时毫秒
 * @param createdAt 创建时间
 */
public record FilePreviewLogResponse(Long id, String tenantId, String fileCode, String previewType,
                                     String previewUrl, String result, Long costMs, LocalDateTime createdAt) {
}
