/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.service;

import java.time.LocalDateTime;

/**
 * 站内消息响应对象。
 *
 * @param id 数据库主键
 * @param tenantId 租户业务编码
 * @param messageCode 消息编码
 * @param receiverId 接收人用户 ID
 * @param receiverName 接收人名称
 * @param messageType 消息类型
 * @param title 消息标题
 * @param content 消息内容
 * @param readFlag 是否已读
 * @param readAt 阅读时间
 * @param createdAt 创建时间
 */
public record MsgMessageResponse(Long id, String tenantId, String messageCode, Long receiverId,
    String receiverName, String messageType, String title, String content, boolean readFlag,
    LocalDateTime readAt, LocalDateTime createdAt) {
}
