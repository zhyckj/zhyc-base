/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.service;

import java.time.LocalDateTime;

/**
 * 消息模板响应对象。
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param templateCode 模板编码
 * @param templateName 模板名称
 * @param channelType 消息通道类型
 * @param titleTemplate 标题模板
 * @param contentTemplate 内容模板
 * @param status 模板状态
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record MsgTemplateResponse(Long id, String tenantId, String templateCode, String templateName,
    String channelType, String titleTemplate, String contentTemplate, String status,
    LocalDateTime createdAt, LocalDateTime updatedAt) {
}
