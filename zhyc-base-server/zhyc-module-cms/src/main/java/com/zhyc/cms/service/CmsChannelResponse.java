/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.service;

import java.time.LocalDateTime;

/**
 * 内容栏目响应。
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param parentId 父栏目主键
 * @param channelCode 栏目编码
 * @param channelName 栏目名称
 * @param sortOrder 排序号
 * @param status 栏目状态
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record CmsChannelResponse(Long id, String tenantId, Long parentId, String channelCode,
                                 String channelName, Integer sortOrder, String status,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
}
