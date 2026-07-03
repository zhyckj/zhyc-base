/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.service;

/**
 * 文件预览渲染响应。
 *
 * @param fileCode 文件业务编码
 * @param previewType 预览类型
 * @param previewUrl 预览访问地址
 * @param result 预览渲染结果
 */
public record FilePreviewRenderResponse(
    String fileCode,
    String previewType,
    String previewUrl,
    String result) {
}
