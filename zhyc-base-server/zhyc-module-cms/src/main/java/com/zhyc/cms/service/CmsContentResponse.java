/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.service;

import java.time.LocalDateTime;

/**
 * 内容文章响应。
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param channelCode 栏目编码
 * @param title 文章标题
 * @param summary 文章摘要
 * @param bodyContent 文章正文
 * @param status 文章状态
 * @param authorId 作者用户主键
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record CmsContentResponse(Long id, String tenantId, String channelCode, String title,
                                 String summary, String bodyContent, String status, Long authorId,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
}
