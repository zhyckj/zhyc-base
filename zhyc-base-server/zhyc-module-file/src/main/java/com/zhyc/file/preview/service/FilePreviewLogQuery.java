/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.service;

/**
 * 文件预览日志查询条件。
 *
 * @param tenantId 租户业务编码
 * @param fileCode 文件业务编码
 */
public record FilePreviewLogQuery(String tenantId, String fileCode) {
}
